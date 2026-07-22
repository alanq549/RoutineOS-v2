package com.alan.routineos.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alan.routineos.data.local.dao.RoutineDao
import com.alan.routineos.data.local.dao.TaskDao
import com.alan.routineos.data.local.entities.RoutineEntity
import com.alan.routineos.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {

    private lateinit var db: RoutineDatabase
    private lateinit var routineDao: RoutineDao
    private lateinit var taskDao: TaskDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RoutineDatabase::class.java).build()
        routineDao = db.routineDao()
        taskDao = db.taskDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndReadRoutine() = runBlocking {
        val routine = RoutineEntity(id = "1", title = "Morning Routine", description = "Test description")
        routineDao.insertRoutine(routine)
        val allRoutines = routineDao.getAllRoutines().first()
        assertEquals(1, allRoutines.size)
        assertEquals("Morning Routine", allRoutines[0].title)
    }

    @Test
    @Throws(Exception::class)
    fun updateRoutine() = runBlocking {
        val routine = RoutineEntity(id = "1", title = "Old Title", description = "Old")
        routineDao.insertRoutine(routine)
        
        val updatedRoutine = routine.copy(title = "New Title")
        routineDao.insertRoutine(updatedRoutine)
        
        val result = routineDao.getRoutineById("1")
        assertEquals("New Title", result?.title)
    }

    @Test
    @Throws(Exception::class)
    fun deleteRoutine() = runBlocking {
        val routine = RoutineEntity(id = "1", title = "Delete Me", description = "Test")
        routineDao.insertRoutine(routine)
        routineDao.deleteRoutine(routine)
        
        val allRoutines = routineDao.getAllRoutines().first()
        assertTrue(allRoutines.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun cascadeDeleteTasks() = runBlocking {
        val routine = RoutineEntity(id = "routine_1", title = "Parent", description = "Parent description")
        routineDao.insertRoutine(routine)
        
        val task = TaskEntity(id = "task_1", routineId = "routine_1", title = "Child Task")
        taskDao.insertTask(task)
        
        // Verify task exists
        val tasksBefore = taskDao.getTasksForRoutine("routine_1").first()
        assertEquals(1, tasksBefore.size)
        
        // Delete parent
        routineDao.deleteRoutine(routine)
        
        // Verify task is gone
        val tasksAfter = taskDao.getTasksForRoutine("routine_1").first()
        assertTrue(tasksAfter.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun readNonExistentRoutineReturnsNull() = runBlocking {
        val result = routineDao.getRoutineById("999")
        assertNull(result)
    }
}
