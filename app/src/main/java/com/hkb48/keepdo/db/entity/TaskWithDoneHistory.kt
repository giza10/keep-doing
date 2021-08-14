package com.hkb48.keepdo.db.entity

import androidx.room.Embedded
import androidx.room.Relation
import java.util.*

data class TaskWithDoneHistory(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "_id",
        entityColumn = "task_id"
    )
    private val doneHistory: List<DoneHistory>
) {
    fun getSortedDoneHistoryAsc(): List<Date> {
        return doneHistory.sortedWith(compareBy { it.date }).map { it.date }
    }

    fun getSortedDoneHistoryDesc(): List<Date> {
        return doneHistory.sortedWith(compareByDescending { it.date }).map { it.date }
    }
}