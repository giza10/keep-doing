package com.hkb48.keepdo.util

import com.hkb48.keepdo.Recurrence
import com.hkb48.keepdo.db.entity.TaskWithDoneHistory
import java.util.*

class DoneHistoryUtil(private val taskWithDoneHistory: TaskWithDoneHistory) {
    fun getNumberOfDone(): Int {
        return taskWithDoneHistory.getSortedDoneHistoryAsc().size
    }

    fun getFirstDoneDate(): Date? {
        val doneHistory = taskWithDoneHistory.getSortedDoneHistoryAsc()
        val today = DateChangeTimeUtil.dateTime
        val doneHistoryTillToday = doneHistory.filterNot { it.after(today) }
        return if (doneHistoryTillToday.isNotEmpty()) {
            doneHistoryTillToday[0]
        } else {
            null
        }
    }

    fun getLastDoneDate(): Date? {
        val doneHistory = taskWithDoneHistory.getSortedDoneHistoryDesc()
        val today = DateChangeTimeUtil.dateTime
        val doneHistoryTillToday = doneHistory.filterNot { it.after(today) }
        return if (doneHistoryTillToday.isNotEmpty()) {
            doneHistoryTillToday[0]
        } else {
            null
        }
    }

    fun getElapsedDaysSinceLastDone(): Int? {
        val lastDoneDate = getLastDoneDate()
        return if (lastDoneDate != null) {
            val today = DateChangeTimeUtil.dateTime
            ((today.time - lastDoneDate.time) / (1000 * 60 * 60 * 24).toLong()).toInt()
        } else {
            null
        }
    }

    fun getComboCount(): Int {
        var count = 0
        val doneHistory = taskWithDoneHistory.getSortedDoneHistoryDesc()
        val today = DateChangeTimeUtil.dateTime
        val doneHistoryTillToday = doneHistory.filterNot { it.after(today) }
        if (doneHistoryTillToday.isNotEmpty()) {
            val calToday = getCalendar(DateChangeTimeUtil.date)
            val calIndex = calToday.clone() as Calendar
            var calDone = getCalendar(doneHistoryTillToday[0])
            val recurrence = Recurrence.getFromTask(taskWithDoneHistory.task)
            var listIndex = 0
            while (true) {
                if (calIndex == calDone) {
                    // count up combo
                    count++
                    calDone = if (++listIndex < doneHistoryTillToday.size) {
                        getCalendar(doneHistoryTillToday[listIndex])
                    } else {
                        break
                    }
                } else {
                    if (recurrence.isValidDay(calIndex[Calendar.DAY_OF_WEEK])) {
                        if (calIndex != calToday) {
                            break
                        }
                    }
                }
                calIndex.add(Calendar.DAY_OF_MONTH, -1)
            }
        }
        return count
    }

    fun getMaxComboCount(): Int {
        var currentCount = 0
        var maxCount = 0
        val doneHistory = taskWithDoneHistory.getSortedDoneHistoryAsc()
        if (doneHistory.isNotEmpty()) {
            val calToday = getCalendar(DateChangeTimeUtil.date)
            var calIndex = calToday.clone() as Calendar
            var calDone = getCalendar(doneHistory[0])
            val recurrence = Recurrence.getFromTask(taskWithDoneHistory.task)
            var listIndex = 0
            var isCompleted = false
            do {
                if (calIndex == calDone) {
                    // count up combo
                    currentCount++
                    if (currentCount > maxCount) {
                        maxCount = currentCount
                    }
                    if (++listIndex < doneHistory.size) {
                        calDone = getCalendar(doneHistory[listIndex])
                    } else {
                        isCompleted = true
                    }
                    calIndex.add(Calendar.DAY_OF_MONTH, 1)
                } else {
                    if (recurrence.isValidDay(calIndex[Calendar.DAY_OF_WEEK])) {
                        // stop combo
                        if (calIndex != calToday) {
                            currentCount = 0
                        }
                        if (isCompleted.not()) {
                            calIndex = calDone.clone() as Calendar
                        } else {
                            calIndex.add(Calendar.DAY_OF_MONTH, 1)
                        }
                    } else {
                        calIndex.add(Calendar.DAY_OF_MONTH, 1)
                    }
                }
            } while (calIndex.after(calToday).not())
        }
        return maxCount
    }

    fun getHistoryInMonth(year: Int, month: Int): ArrayList<Date> {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month

        // firstDayInMonth: 20xx/xx/01 00:00:00
        val firstDayInMonth = calendar.clone() as Calendar
        firstDayInMonth[Calendar.DAY_OF_MONTH] = 1
        // lastDayInMonth: 20xx/xx/31 23:59:59
        val lastDayInMonth = firstDayInMonth.clone() as Calendar
        lastDayInMonth.add(Calendar.MONTH, 1)
        lastDayInMonth.add(Calendar.MILLISECOND, -1)

        val doneHistory = taskWithDoneHistory.getSortedDoneHistoryAsc()
        val doneHistoryInMonth = doneHistory.filterNot {
            it.before(firstDayInMonth.time) || it.after(lastDayInMonth.time)
        }
        return ArrayList(doneHistoryInMonth)
    }

    private fun getCalendar(date: Date): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }
}