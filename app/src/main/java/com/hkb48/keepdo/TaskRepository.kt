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

    fun getTaskListWithDoneHistory(): Flow<List<TaskWithDoneHistory>> {
        return taskWithDoneHistoryDao.getTaskListWithDoneHistory()
    }

    fun getTaskWithDoneHistory(id: Int): Flow<TaskWithDoneHistory> {
        return taskWithDoneHistoryDao.getTaskWithDoneHistory(id)
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

    suspend fun getMaxOrder(): Int {
        return taskDao.getMaxOrder() ?: 0
    }

    suspend fun setDone(doneHistory: DoneHistory) {
        try {
            doneHistoryDao.insert(doneHistory)
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }

    suspend fun unsetDone(taskId: Int, date: Date) {
        try {
            doneHistoryDao.delete(taskId, date)
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }
}