package com.hkb48.keepdo

import com.hkb48.keepdo.db.entity.Task
import java.io.Serializable
import java.util.*

class Recurrence internal constructor(
    val monday: Boolean = true,
    val tuesday: Boolean = true,
    val wednesday: Boolean = true,
    val thursday: Boolean = true,
    val friday: Boolean = true,
    val saturday: Boolean = true,
    val sunday: Boolean = true
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