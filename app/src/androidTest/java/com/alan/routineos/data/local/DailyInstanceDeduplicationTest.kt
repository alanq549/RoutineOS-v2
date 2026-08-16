package com.alan.routineos.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alan.routineos.data.local.dao.DailyInstanceDao
import com.alan.routineos.data.local.entities.DailyInstanceEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DailyInstanceDeduplicationTest {

    private lateinit var db: RoutineOSDatabase
    private lateinit var dao: DailyInstanceDao

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RoutineOSDatabase::class.java
        ).build()
        dao = db.dailyInstanceDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun duplicateMaterializationThrowsException() = runBlocking {
        val instance1 = DailyInstanceEntity(
            id = "1",
            targetId = "target1",
            targetType = "NODE",
            scheduledDate = 100L,
            titleSnapshot = "Title",
            descriptionSnapshot = "Desc",
            status = "PLANNED"
        )
        
        val instance2 = instance1.copy(id = "2") // Same target and date, different ID

        dao.insertInstance(instance1)
        dao.insertInstance(instance2) // Should throw due to UNIQUE index
    }
}
