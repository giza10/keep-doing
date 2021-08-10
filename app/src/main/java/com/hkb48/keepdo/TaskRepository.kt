package com.hkb48.keepdo

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
        return taskDao.add(task).toInt()
    }

    suspend fun editTask(task: Task) {
        taskDao.update(task)
    }

    suspend fun deleteTask(taskId: Int) {
        taskDao.delete(taskId)
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
        taskCompletionDao.insert(taskCompletion)
    }

    suspend fun unsetDone(taskId: Int, date: Date) {
        taskCompletionDao.delete(taskId, date)
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