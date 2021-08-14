package com.hkb48.keepdo.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.hkb48.keepdo.db.entity.DoneHistory
import java.util.*

@Dao
interface DoneHistoryDao {
    @Insert
    suspend fun insert(doneHistory: DoneHistory): Long

    @Query(
        """DELETE FROM ${DoneHistory.TABLE_NAME}
            WHERE (${DoneHistory.TASK_NAME_ID} = :taskId AND
            ${DoneHistory.DONE_DATE} = :doneDate)"""
    )
    suspend fun delete(taskId: Int, doneDate: Date)

    @Query(
        """SELECT DISTINCT ${DoneHistory.DONE_DATE} FROM
            ${DoneHistory.TABLE_NAME} WHERE ${DoneHistory.TASK_NAME_ID} = :taskId AND
            ${DoneHistory.DONE_DATE} = :date"""
    )
    suspend fun getByDate(taskId: Int, date: Date): List<Date>

    @RawQuery
    suspend fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int
}