package com.hkb48.keepdo

import com.hkb48.keepdo.db.entity.Task
import java.io.Serializable
import java.util.*

class Recurrence internal constructor(
    val monday: Boolean,
    val tuesday: Boolean,
    val wednesday: Boolean,
    val thursday: Boolean,
    val friday: Boolean,
    val saturday: Boolean,
    val sunday: Boolean
) : Serializable {
    fun isValidDay(week: Int): Boolean {
        return when (week) {
            Calendar.SUNDAY -> sunday
            Calendar.MONDAY -> monday
            Calendar.TUESDAY -> tuesday
            Calendar.WEDNESDAY -> wednesday
            Calendar.THURSDAY -> thursday
            Calendar.FRIDAY -> friday
            Calendar.SATURDAY -> saturday
            else -> false
        }
    }

    companion object {
        fun getFromTask(task: Task): Recurrence {
            return Recurrence(
                task.monFrequency,
                task.tueFrequency,
                task.wedFrequency,
                task.thrFrequency,
                task.friFrequency,
                task.satFrequency,
                task.sunFrequency
            )
        }
    }
}