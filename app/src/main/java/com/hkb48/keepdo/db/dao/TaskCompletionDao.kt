package com.hkb48.keepdo.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.hkb48.keepdo.db.entity.TaskCompletion
import java.util.*

@Dao
interface TaskCompletionDao {
    @Insert
    suspend fun insert(taskCompletion: TaskCompletion): Long

    @Query(
        """DELETE FROM ${TaskCompletion.TABLE_NAME}
            WHERE (${TaskCompletion.TASK_NAME_ID} = :taskId AND
            ${TaskCompletion.TASK_COMPLETION_DATE} = :completionDate)"""
    )
    suspend fun delete(taskId: Int, completionDate: Date)

    @Query(
        """SELECT DISTINCT ${TaskCompletion.TASK_COMPLETION_DATE} FROM
            ${TaskCompletion.TABLE_NAME} WHERE ${TaskCompletion.TASK_NAME_ID} = :taskId AND
            ${TaskCompletion.TASK_COMPLETION_DATE} = :date"""
    )
    suspend fun getByDate(taskId: Int, date: Date): List<Date>

    @Query(
        """SELECT DISTINCT COUNT(*) FROM ${TaskCompletion.TABLE_NAME}
            WHERE ${TaskCompletion.TASK_NAME_ID} = :taskId"""
    )
    suspend fun getCount(taskId: Int): Int

    @Query(
        """SELECT MIN (${TaskCompletion.TASK_COMPLETION_DATE}) FROM
                ${TaskCompletion.TABLE_NAME} WHERE
                ${TaskCompletion.TASK_NAME_ID} = :taskId AND
                ${TaskCompletion.TASK_COMPLETION_DATE} <= :untilDate"""
    )
    suspend fun getFirstCompletionDate(taskId: Int, untilDate: Date): Date?

    @Query(
        """SELECT MAX (${TaskCompletion.TASK_COMPLETION_DATE}) FROM
                ${TaskCompletion.TABLE_NAME} WHERE
                ${TaskCompletion.TASK_NAME_ID} = :taskId AND
                ${TaskCompletion.TASK_COMPLETION_DATE} <= :untilDate"""
    )
    suspend fun getLastCompletionDate(taskId: Int, untilDate: Date): Date?

    @Query(
        """SELECT DISTINCT ${TaskCompletion.TASK_COMPLETION_DATE} FROM
                ${TaskCompletion.TABLE_NAME} WHERE
                ${TaskCompletion.TASK_NAME_ID} = :taskId AND
                ${TaskCompletion.TASK_COMPLETION_DATE} <= :untilDate ORDER BY
                ${TaskCompletion.TASK_COMPLETION_DATE} ASC"""
    )
    suspend fun getCompletionHistoryAsc(taskId: Int, untilDate: Date): List<Date>

    @Query(
        """SELECT DISTINCT ${TaskCompletion.TASK_COMPLETION_DATE} FROM
                ${TaskCompletion.TABLE_NAME} WHERE
                ${TaskCompletion.TASK_NAME_ID} = :taskId AND
                ${TaskCompletion.TASK_COMPLETION_DATE} <= :untilDate ORDER BY
                ${TaskCompletion.TASK_COMPLETION_DATE} DESC"""
    )
    suspend fun getCompletionHistoryDesc(taskId: Int, untilDate: Date): List<Date>

    @Query(
        """SELECT DISTINCT ${TaskCompletion.TASK_COMPLETION_DATE} FROM
                ${TaskCompletion.TABLE_NAME} WHERE
                ${TaskCompletion.TASK_NAME_ID} = :taskId AND
                ${TaskCompletion.TASK_COMPLETION_DATE} BETWEEN :fromDate AND :untilDate ORDER BY 
                ${TaskCompletion.TASK_COMPLETION_DATE} ASC"""
    )
    suspend fun getCompletionHistoryBetween(
        taskId: Int,
        fromDate: Date,
        untilDate: Date
    ): List<Date>

    @RawQuery
    suspend fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int
}