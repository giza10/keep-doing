package com.hkb48.keepdo.db.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.hkb48.keepdo.db.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun add(task: Task): Long

    @Update
    suspend fun update(task: Task): Int

    @Query("DELETE FROM " + Task.TABLE_NAME + " WHERE _id = :id")
    suspend fun delete(id: Int)

    @Query("SELECT * fROM " + Task.TABLE_NAME + " ORDER BY + " + Task.TASK_LIST_ORDER + " asc")
    fun getTaskListByOrderFlow(): Flow<List<Task>>

    @Query("SELECT * fROM " + Task.TABLE_NAME + " ORDER BY + " + Task.TASK_LIST_ORDER + " asc")
    suspend fun getTaskListByOrder(): List<Task>

    @Query("SELECT * fROM " + Task.TABLE_NAME + " WHERE _id = :id")
    fun getTaskFlow(id: Int): Flow<Task>

    @Query("SELECT * fROM " + Task.TABLE_NAME + " WHERE _id = :id")
    suspend fun getTask(id: Int): Task?

    @Query("SELECT MAX (" + Task.TASK_LIST_ORDER + ") FROM " + Task.TABLE_NAME)
    suspend fun getMaxOrder(): Int

    @RawQuery
    suspend fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int
}