package com.hkb48.keepdo.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import java.util.*

@Dao
interface TaskCompletionDao {
    @Insert
    fun insert(taskCompletion: TaskCompletion): Long

    @Query(
        """DELETE FROM ${TaskCompletion.TABLE_NAME}
            WHERE ${TaskCompletion.TASK_NAME_ID} = :taskId"""
    )
    fun delete(taskId: Int)

    @Query(
        """DELETE FROM ${TaskCompletion.TABLE_NAME}
            WHERE (${TaskCompletion.TASK_NAME_ID} = :taskId AND
            ${TaskCompletion.TASK_COMPLETION_DATE} = :completionDate)"""
    )
    fun delete(taskId: Int, completionDate: Date)

    @Query(
        """SELECT DISTINCT ${TaskCompletion.TASK_COMPLETION_DATE} FROM 
            ${TaskCompletion.TABLE_NAME} WHERE ${TaskCompletion.TASK_NAME_ID} = :taskId"""
    )
    fun getAll(taskId: Int): List<Date>

    @Query(
        """SELECT DISTINCT ${TaskCompletion.TASK_COMPLETION_DATE} FROM 
            ${TaskCompletion.TABLE_NAME}"""
    )
    fun getAllLiveData(): LiveData<List<Date>>

    @Query(
        """SELECT DISTINCT ${TaskCompletion.TASK_COMPLETION_DATE} FROM
            ${TaskCompletion.TABLE_NAME} WHERE ${TaskCompletion.TASK_NAME_ID} = :taskId AND
            ${TaskCompletion.TASK_COMPLETION_DATE} = :date"""
    )
    fun getByDate(taskId: Int, date: Date): List<Date>

    @Query(
        """SELECT MIN (${TaskCompletion.TASK_COMPLETION_DATE}) FROM
                ${TaskCompletion.TABLE_NAME} WHERE
                ${TaskCompletion.TASK_NAME_ID} = :taskId AND
                ${TaskCompletion.TASK_COMPLETION_DATE} <= :untilDate"""
    )
    fun getFirstCompletionDate(taskId: Int, untilDate: Date): Date?

    @Query(
        """SELECT MAX (${TaskCompletion.TASK_COMPLETION_DATE}) FROM
                ${TaskCompletion.TABLE_NAME} WHERE
                ${TaskCompletion.TASK_NAME_ID} = :taskId AND
                ${TaskCompletion.TASK_COMPLETION_DATE} <= :untilDate"""
    )
    fun getLastCompletionDate(taskId: Int, untilDate: Date): Date?

    @Query(
        """SELECT DISTINCT ${TaskCompletion.TASK_COMPLETION_DATE} FROM
                ${TaskCompletion.TABLE_NAME} WHERE
                ${TaskCompletion.TASK_NAME_ID} = :taskId AND
                ${TaskCompletion.TASK_COMPLETION_DATE} <= :untilDate ORDER BY
                ${TaskCompletion.TASK_COMPLETION_DATE} ASC"""
    )
    fun getCompletionHistoryAsc(taskId: Int, untilDate: Date): List<Date>

    @Query(
        """SELECT DISTINCT ${TaskCompletion.TASK_COMPLETION_DATE} FROM
                ${TaskCompletion.TABLE_NAME} WHERE
                ${TaskCompletion.TASK_NAME_ID} = :taskId AND
                ${TaskCompletion.TASK_COMPLETION_DATE} <= :untilDate ORDER BY
                ${TaskCompletion.TASK_COMPLETION_DATE} DESC"""
    )
    fun getCompletionHistoryDesc(taskId: Int, untilDate: Date): List<Date>

    @Query(
        """SELECT DISTINCT ${TaskCompletion.TASK_COMPLETION_DATE} FROM
                ${TaskCompletion.TABLE_NAME} WHERE
                ${TaskCompletion.TASK_NAME_ID} = :taskId AND
                ${TaskCompletion.TASK_COMPLETION_DATE} BETWEEN :fromDate AND :untilDate ORDER BY 
                ${TaskCompletion.TASK_COMPLETION_DATE} ASC"""
    )
    fun getCompletionHistoryBetween(taskId: Int, fromDate: Date, untilDate: Date): List<Date>

    @RawQuery
    fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int
}