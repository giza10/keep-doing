package com.hkb48.keepdo.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.hkb48.keepdo.db.entity.Task

@Dao
interface TaskDao {
    @Insert
    fun add(task: Task): Long

    @Update
    fun update(task: Task): Int

    @Query("DELETE FROM " + Task.TABLE_NAME + " WHERE _id = :id")
    fun delete(id: Int)

    @Query("SELECT * fROM " + Task.TABLE_NAME + " ORDER BY + " + Task.TASK_LIST_ORDER + " asc")
    fun getTaskListByOrder(): List<Task>

    @Query("SELECT * fROM " + Task.TABLE_NAME + " ORDER BY + " + Task.TASK_LIST_ORDER + " asc")
    fun getTaskLiveDataListByOrder(): LiveData<List<Task>>

    @Query("SELECT * fROM " + Task.TABLE_NAME + " WHERE _id = :id")
    fun getTask(id: Int): Task?

    @Query("SELECT MAX (" + Task.TASK_LIST_ORDER + ") FROM " + Task.TABLE_NAME)
    fun getMaxOrder(): Int

    @RawQuery
    fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int
}