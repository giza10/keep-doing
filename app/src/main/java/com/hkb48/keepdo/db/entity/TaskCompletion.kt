package com.hkb48.keepdo.db.entity

import androidx.room.*
import java.util.*

@Entity(
    tableName = "table_completions", foreignKeys = [ForeignKey(
        entity = Task::class,
        parentColumns = arrayOf("_id"),
        childColumns = arrayOf("task_id"),
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )], indices = [Index("task_id")]
)
data class TaskCompletion(
    @PrimaryKey(autoGenerate = true) val _id: Int,
    @ColumnInfo(name = TASK_NAME_ID) val taskId: Int,
    @ColumnInfo(name = TASK_COMPLETION_DATE) val date: Date
) {
    companion object {
        const val TABLE_NAME = "table_completions"
        const val TASK_NAME_ID = "task_id"
        const val TASK_COMPLETION_DATE = "completion_date"
    }
}
