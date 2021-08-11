package com.hkb48.keepdo.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.hkb48.keepdo.DateChangeTimeUtil
import com.hkb48.keepdo.Recurrence
import com.hkb48.keepdo.TaskRepository
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.db.entity.TaskCompletion
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {
    fun getObservableTask(taskId: Int): LiveData<Task> {
        return repository.getTaskFlow(taskId).asLiveData()
    }

    suspend fun setDoneStatus(taskId: Int, date: Date, doneSwitch: Boolean) {
        if (doneSwitch) {
            val taskCompletion = TaskCompletion(null, taskId, date)
            repository.setDone(taskCompletion)
        } else {
            repository.unsetDone(taskId, date)
        }
    }

    suspend fun getHistoryInMonth(taskId: Int, year: Int, month: Int): ArrayList<Date> {
        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month
        val firstDayInMonth = calendar.clone() as Calendar
        firstDayInMonth[Calendar.DAY_OF_MONTH] = 1
        val lastDayInMonth = calendar.clone() as Calendar
        lastDayInMonth[Calendar.DAY_OF_MONTH] = lastDayInMonth.getActualMaximum(Calendar.DATE)

        val doneList = repository.getDoneHistoryBetween(
            taskId, firstDayInMonth.time, lastDayInMonth.time
        )
        return ArrayList(doneList)
    }

    suspend fun getComboCount(task: Task): Int {
        var count = 0
        val mDoneHistory = repository.getDoneHistoryDesc(task._id!!, todayDate)
        if (mDoneHistory.isNotEmpty()) {
            val calToday = getCalendar(DateChangeTimeUtil.date)
            val calIndex = calToday.clone() as Calendar
            var calDone = getCalendar(mDoneHistory[0])
            val recurrence = Recurrence.getFromTask(task)
            var listIndex = 0
            while (true) {
                if (calIndex == calDone) {
                    // count up combo
                    count++
                    calDone = if (++listIndex < mDoneHistory.size) {
                        getCalendar(mDoneHistory[listIndex])
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

    private val todayDate: Date
        get() {
            return DateChangeTimeUtil.dateTime
        }

    private fun getCalendar(date: Date): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }
}
