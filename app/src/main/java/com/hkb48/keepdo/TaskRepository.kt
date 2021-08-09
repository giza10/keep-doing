package com.hkb48.keepdo

import com.hkb48.keepdo.db.TaskDatabase
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.db.entity.TaskCompletion
import kotlinx.coroutines.flow.Flow
import java.util.*

class TaskRepository(private val database: TaskDatabase) {
    fun getTaskListFlow(): Flow<List<Task>> {
        return database.taskDao().getTaskListByOrderFlow()
    }

    suspend fun addTask(task: Task): Int {
        return database.taskDao().add(task).toInt()
    }

    suspend fun editTask(task: Task) {
        database.taskDao().update(task)
    }

    suspend fun deleteTask(taskId: Int) {
        database.taskDao().delete(taskId)
    }

    fun getTaskFlow(taskId: Int): Flow<Task> {
        return database.taskDao().getTaskFlow(taskId)
    }

    suspend fun getTask(taskId: Int): Task? {
        return database.taskDao().getTask(taskId)
    }

    suspend fun getMaxOrder(): Int {
        return database.taskDao().getMaxOrder() ?: 0
    }

    suspend fun setDone(taskCompletion: TaskCompletion) {
        database.taskCompletionDao().insert(taskCompletion)
    }

    suspend fun unsetDone(taskId: Int, date: Date) {
        database.taskCompletionDao().delete(taskId, date)
    }

    fun getDoneStatusListFlow(): Flow<List<TaskCompletion>> {
        return database.taskCompletionDao().getAllFlow()
    }

    suspend fun getNumberOfDone(taskId: Int): Int {
        return database.taskCompletionDao().getCount(taskId)
    }

    suspend fun getFirstDoneDate(taskId: Int, untilDate: Date): Date? {
        return database.taskCompletionDao().getFirstCompletionDate(taskId, untilDate)
    }

    suspend fun getLastDoneDate(taskId: Int, untilDate: Date): Date? {
        return database.taskCompletionDao().getLastCompletionDate(taskId, untilDate)
    }

    suspend fun getDoneHistoryBetween(
        taskId: Int, fromDate: Date, untilDate: Date
    ): List<Date> {
        return database.taskCompletionDao().getCompletionHistoryBetween(taskId, fromDate, untilDate)
    }

    suspend fun getDoneHistoryAsc(taskId: Int, untilDate: Date): List<Date> {
        return database.taskCompletionDao().getCompletionHistoryAsc(taskId, untilDate)
    }

    suspend fun getDoneHistoryDesc(taskId: Int, untilDate: Date): List<Date> {
        return database.taskCompletionDao().getCompletionHistoryDesc(taskId, untilDate)
    }

    companion object {
        @Volatile
        private var instance: TaskRepository? = null

        fun getInstance(database: TaskDatabase) = instance ?: synchronized(this) {
            instance = TaskRepository(database)
        }
    }
}