package com.hkb48.keepdo

import android.database.sqlite.SQLiteException
import com.hkb48.keepdo.db.dao.TaskCompletionDao
import com.hkb48.keepdo.db.dao.TaskDao
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.db.entity.TaskCompletion
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val taskCompletionDao: TaskCompletionDao
) {
    fun getTaskListFlow(): Flow<List<Task>> {
        return taskDao.getTaskListByOrderFlow()
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

    suspend fun setDone(taskCompletion: TaskCompletion) {
        try {
            taskCompletionDao.insert(taskCompletion)
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }

    suspend fun unsetDone(taskId: Int, date: Date) {
        try {
            taskCompletionDao.delete(taskId, date)
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }

    fun getDoneStatusListFlow(): Flow<List<TaskCompletion>> {
        return taskCompletionDao.getAllFlow()
    }

    suspend fun getNumberOfDone(taskId: Int): Int {
        return taskCompletionDao.getCount(taskId)
    }

    suspend fun getFirstDoneDate(taskId: Int, untilDate: Date): Date? {
        return taskCompletionDao.getFirstCompletionDate(taskId, untilDate)
    }

    suspend fun getLastDoneDate(taskId: Int, untilDate: Date): Date? {
        return taskCompletionDao.getLastCompletionDate(taskId, untilDate)
    }

    suspend fun getDoneHistoryBetween(
        taskId: Int, fromDate: Date, untilDate: Date
    ): List<Date> {
        return taskCompletionDao.getCompletionHistoryBetween(taskId, fromDate, untilDate)
    }

    suspend fun getDoneHistoryAsc(taskId: Int, untilDate: Date): List<Date> {
        return taskCompletionDao.getCompletionHistoryAsc(taskId, untilDate)
    }

    suspend fun getDoneHistoryDesc(taskId: Int, untilDate: Date): List<Date> {
        return taskCompletionDao.getCompletionHistoryDesc(taskId, untilDate)
    }
}