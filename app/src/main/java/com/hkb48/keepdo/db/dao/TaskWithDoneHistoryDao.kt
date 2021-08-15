package com.hkb48.keepdo.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.db.entity.TaskWithDoneHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskWithDoneHistoryDao {
    @Transaction
    @Query("""SELECT * FROM ${Task.TABLE_NAME} ORDER BY ${Task.TASK_LIST_ORDER} asc""")
    fun getTaskListWithDoneHistoryFlow(): Flow<List<TaskWithDoneHistory>>

    @Transaction
    @Query("""SELECT * FROM ${Task.TABLE_NAME} WHERE _id = :id""")
    fun getTaskWithDoneHistoryFlow(id: Int): Flow<TaskWithDoneHistory>
}