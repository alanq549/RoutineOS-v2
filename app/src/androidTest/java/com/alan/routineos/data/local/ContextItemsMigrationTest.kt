package com.alan.routineos.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import org.junit.Assert.*
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

@RunWith(AndroidJUnit4::class)
class ContextItemsMigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        RoutineOSDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate6To7_poblatesActionProtocolAsTimer() {
        // Create DB at version 6
        var db = helper.createDatabase(TEST_DB, 6)
        
        // Insert a V6 instance
        val values = ContentValues().apply {
            put("id", "inst_1")
            put("targetId", "node_1")
            put("targetType", "NODE")
            put("scheduledDate", 1000L)
            put("titleSnapshot", "Old Activity")
            put("descriptionSnapshot", "")
            put("status", "PLANNED")
            put("mobility", "FLEXIBLE")
        }
        db.insert("daily_instances", SQLiteDatabase.CONFLICT_REPLACE, values)
        db.close()

        // Migrate to 7
        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)

        // Verify data
        val cursor = db.query("SELECT actionProtocol, reminderAbs FROM daily_instances WHERE id = 'inst_1'")
        assertTrue(cursor.moveToFirst())
        assertEquals("TIMER", cursor.getString(0))
        assertTrue(cursor.isNull(1))
        cursor.close()
    }

    @Test
    fun sqlite_checkConstraint_preventsDoubleReminders() {
        val db = helper.createDatabase(TEST_DB, 7)
        helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)
        
        val invalidValues = ContentValues().apply {
            put("id", "bad_rem")
            put("targetType", "AD_HOC")
            put("scheduledDate", 2000L)
            put("titleSnapshot", "Bad")
            put("descriptionSnapshot", "")
            put("status", "PLANNED")
            put("mobility", "FLEXIBLE")
            put("reminderAbs", 600)
            put("reminderRel", -10) // BOTH SET
        }
        
        try {
            db.insert("daily_instances", SQLiteDatabase.CONFLICT_REPLACE, invalidValues)
            fail("Should have thrown SQLiteConstraintException due to CHECK constraint")
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            // Expected
        }
    }

    @Test
    fun migrate7To8_backfillsNotesAndChangesToSetNull() {
        val db7 = helper.createDatabase(TEST_DB, 7)
        // Ensure V7 schema exists
        MIGRATION_6_7.migrate(db7)
        
        // 1. Setup Data in V7
        db7.execSQL("INSERT INTO daily_instances (id, targetType, scheduledDate, titleSnapshot, descriptionSnapshot, status, mobility, actionProtocol) " +
                "VALUES ('inst_notes', 'AD_HOC', 5000, 'My Instance', '', 'PLANNED', 'FLEXIBLE', 'TIMER')")
        
        db7.execSQL("INSERT INTO notes (id, content, instanceId) VALUES ('note_1', 'Some content', 'inst_notes')")
        db7.close()

        // 2. Migrate to 8
        val db8 = helper.runMigrationsAndValidate(TEST_DB, 8, true, MIGRATION_7_8)

        // 3. Verify backfill
        val cursor = db8.query("SELECT dateSnapshot, titleSnapshot, targetTypeSnapshot FROM notes WHERE id = 'note_1'")
        assertTrue(cursor.moveToFirst())
        assertEquals(5000L, cursor.getLong(0))
        assertEquals("My Instance", cursor.getString(1))
        assertEquals("INSTANCE", cursor.getString(2))
        cursor.close()

        // 4. Verify SET NULL behavior
        db8.execSQL("DELETE FROM daily_instances WHERE id = 'inst_notes'")
        
        val cursorAfter = db8.query("SELECT instanceId, titleSnapshot FROM notes WHERE id = 'note_1'")
        assertTrue(cursorAfter.moveToFirst())
        assertTrue("FK should be NULL now", cursorAfter.isNull(0))
        assertEquals("My Instance", cursorAfter.getString(1)) // Context preserved
        cursorAfter.close()
    }
}
