package com.hkb48.keepdo

import android.database.sqlite.SQLiteException
import com.hkb48.keepdo.db.dao.DoneHistoryDao
import com.hkb48.keepdo.db.dao.TaskDao
import com.hkb48.keepdo.db.dao.TaskWithDoneHistoryDao
import com.hkb48.keepdo.db.entity.DoneHistory
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.db.entity.TaskWithDoneHistory
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val doneHistoryDao: DoneHistoryDao,
    private val taskWithDoneHistoryDao: TaskWithDoneHistoryDao
) {
    fun getTaskListFlow(): Flow<List<Task>> {
        return taskDao.getTaskListByOrderFlow()
    }

    suspend fun getTaskList(): List<Task> {
        return taskDao.getTaskListByOrder()
    }

    fun getTaskListWithDoneHistoryFlow(): Flow<List<TaskWithDoneHistory>> {
        return taskWithDoneHistoryDao.getTaskListWithDoneHistoryFlow()
    }

    fun getTaskWithDoneHistoryFlow(id: Int): Flow<TaskWithDoneHistory> {
        return taskWithDoneHistoryDao.getTaskWithDoneHistoryFlow(id)
    }

    suspend fun addTask(task: Task): Int {
        return try {
            taskDao.add(task).toInt()
        } catch (e: SQLiteException) {
            e.printStackTrace()
            Task.INVALID_TASKID
        }
    }

    suspend fun editTask(task: Task) {
        try {
            taskDao.update(task)
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }

    suspend fun deleteTask(taskId: Int) {
        try {
            taskDao.delete(taskId)
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }

    fun getTaskFlow(taskId: Int): Flow<Task> {
        return taskDao.getTaskFlow(taskId)
    }

    suspend fun getTask(taskId: Int): Task? {
        return taskDao.getTask(taskId)
    }

    suspend fun getMaxOrder(): Int {
        return taskDao.getMaxOrder() ?: 0
    }

    suspend fun getDoneStatus(taskId: Int, date: Date): Boolean {
        return doneHistoryDao.getByDate(taskId, date).count() > 0
    }

    suspend fun setDoneStatus(taskId: Int, date: Date, isDone: Boolean) {
        try {
            if (isDone) {
                val doneInfo = DoneHistory(0, taskId, date)
                doneHistoryDao.insert(doneInfo)
            } else {
                doneHistoryDao.delete(taskId, date)
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }
}