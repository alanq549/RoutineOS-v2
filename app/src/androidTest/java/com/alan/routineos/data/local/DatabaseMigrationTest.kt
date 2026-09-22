package com.alan.routineos.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        RoutineOSDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate5To6() {
        // Create earliest version of the database if needed, but here we start from V5
        var db = helper.createDatabase(TEST_DB, 5)

        // Add some data in V5 format
        db.execSQL("INSERT INTO activity_definitions (id, title, description, systemId, isDeleted) VALUES ('def_1', 'Gym', 'Work out', NULL, 0)")
        db.execSQL("INSERT INTO activity_nodes (id, activityDefinitionId, parentId, position, title, description, isDeleted) VALUES ('node_1', 'def_1', NULL, 0, 'Push', '', 0)")
        db.execSQL("INSERT INTO activity_executions (id, nodeId, dailyInstanceId, scheduledDate, completedAt, metadataJson) VALUES ('exec_1', 'node_1', NULL, 100, 200, '{}')")

        db.close()

        // Open database with V6 and run migrations
        db = helper.runMigrationsAndValidate(TEST_DB, 6, true, com.alan.routineos.data.di.DatabaseModule.MIGRATION_5_6)

        // Verify data and snapshots
        val cursor = db.query("SELECT * FROM activity_executions WHERE id = 'exec_1'")
        if (cursor.moveToFirst()) {
            val actId = cursor.getString(cursor.getColumnIndexOrThrow("activityIdSnapshot"))
            val title = cursor.getString(cursor.getColumnIndexOrThrow("titleSnapshot"))
            assert(actId == "def_1")
            assert(title == "Push")
        }
        cursor.close()
    }
}
