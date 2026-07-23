package com.alan.routineos.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alan.routineos.data.local.RoutineDatabase
import com.alan.routineos.data.local.dao.RoutineDao
import com.alan.routineos.data.local.dao.TaskDao
import com.alan.routineos.domain.model.Routine
import com.alan.routineos.domain.model.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class OfflineRoutineRepositoryTest {

    private lateinit var db: RoutineDatabase
    private lateinit var routineDao: RoutineDao
    private lateinit var taskDao: TaskDao
    private lateinit var repository: OfflineRoutineRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RoutineDatabase::class.java).build()
        routineDao = db.routineDao()
        taskDao = db.taskDao()
        repository = OfflineRoutineRepository(routineDao, taskDao)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadRoutineFlow() = runBlocking {
        val routine = Routine(id = "1", title = "Morning", description = "Desc")
        repository.upsertRoutine(routine)
        
        val result = repository.getRoutines().first()
        assertEquals(1, result.size)
        assertEquals("Morning", result[0].title)
    }

    @Test
    fun getRoutineById() = runBlocking {
        val routine = Routine(id = "1", title = "Morning", description = "Desc")
        repository.upsertRoutine(routine)
        
        val result = repository.getRoutineById("1")
        assertNotNull(result)
        assertEquals("Morning", result?.title)
    }

    @Test
    fun upsertUpdatesExistingRoutine() = runBlocking {
        val routine = Routine(id = "1", title = "Old", description = "Desc")
        repository.upsertRoutine(routine)
        
        val updated = routine.copy(title = "New")
        repository.upsertRoutine(updated)
        
        val result = repository.getRoutineById("1")
        assertEquals("New", result?.title)
    }

    @Test
    fun deleteRoutine() = runBlocking {
        val routine = Routine(id = "1", title = "Delete", description = "Desc")
        repository.upsertRoutine(routine)
        repository.deleteRoutine(routine)
        
        val result = repository.getRoutines().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getTasksForRoutine() = runBlocking {
        val routine = Routine(id = "r1", title = "Routine", description = "Desc")
        repository.upsertRoutine(routine)
        
        val task = Task(id = "t1", routineId = "r1", title = "Task")
        repository.upsertTask(task)
        
        val result = repository.getTasksForRoutine("r1").first()
        assertEquals(1, result.size)
        assertEquals("Task", result[0].title)
    }

    @Test
    fun upsertTaskUpdatesExistingTask() = runBlocking {
        val routine = Routine(id = "r1", title = "Routine", description = "Desc")
        repository.upsertRoutine(routine)

        val task = Task(id = "t1", routineId = "r1", title = "Old Task")
        repository.upsertTask(task)

        val updated = task.copy(title = "New Task")
        repository.upsertTask(updated)

        val result = repository.getTasksForRoutine("r1").first()
        assertEquals(1, result.size)
        assertEquals("New Task", result[0].title)
    }

    @Test
    fun deleteTask() = runBlocking {
        val routine = Routine(id = "r1", title = "Routine", description = "Desc")
        repository.upsertRoutine(routine)

        val task = Task(id = "t1", routineId = "r1", title = "Task")
        repository.upsertTask(task)
        repository.deleteTask(task)

        val result = repository.getTasksForRoutine("r1").first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteRoutineCascadesToTasks() = runBlocking {
        val routine = Routine(id = "r1", title = "Routine", description = "Desc")
        repository.upsertRoutine(routine)

        val task = Task(id = "t1", routineId = "r1", title = "Task")
        repository.upsertTask(task)

        repository.deleteRoutine(routine)

        val result = repository.getTasksForRoutine("r1").first()
        assertTrue(result.isEmpty())
    }
}
