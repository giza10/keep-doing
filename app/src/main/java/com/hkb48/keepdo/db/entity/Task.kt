package com.hkb48.keepdo.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val _id: Int,
    @ColumnInfo(name = TASK_NAME) val name: String,
    @ColumnInfo(name = FREQUENCY_MON) val monFrequency: Boolean,
    @ColumnInfo(name = FREQUENCY_TUE) val tueFrequency: Boolean,
    @ColumnInfo(name = FREQUENCY_WED) val wedFrequency: Boolean,
    @ColumnInfo(name = FREQUENCY_THR) val thrFrequency: Boolean,
    @ColumnInfo(name = FREQUENCY_FRI) val friFrequency: Boolean,
    @ColumnInfo(name = FREQUENCY_SAT) val satFrequency: Boolean,
    @ColumnInfo(name = FREQUENCY_SUN) val sunFrequency: Boolean,
    @ColumnInfo(name = DESCRIPTION) val description: String?,
    @ColumnInfo(name = REMINDER_ENABLED) val reminderEnabled: Boolean,
    @ColumnInfo(name = REMINDER_TIME) val reminderTime: Long?,
    @ColumnInfo(name = TASK_LIST_ORDER) val listOrder: Int
) {
    companion object {
        const val TABLE_NAME = "table_tasks"
        const val TASK_NAME = "task_name"
        const val FREQUENCY_MON = "mon_frequency"
        const val FREQUENCY_TUE = "tue_frequency"
        const val FREQUENCY_WED = "wen_frequency"
        const val FREQUENCY_THR = "thr_frequency"
        const val FREQUENCY_FRI = "fri_frequency"
        const val FREQUENCY_SAT = "sat_frequency"
        const val FREQUENCY_SUN = "sun_frequency"
        const val DESCRIPTION = "task_context"
        const val REMINDER_ENABLED = "reminder_enabled"
        const val REMINDER_TIME = "reminder_time"
        const val TASK_LIST_ORDER = "task_list_order"

        const val INVALID_TASKID = -1
    }
}
