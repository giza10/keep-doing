package com.hkb48.keepdo.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.hkb48.keepdo.Recurrence
import com.hkb48.keepdo.TaskRepository
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.util.DateChangeTimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {
    fun getObservableTask(taskId: Int): LiveData<Task> {
        return repository.getTaskFlow(taskId).asLiveData()
    }

    suspend fun getNumberOfDone(taskId: Int): Int {
        return repository.getNumberOfDone(taskId)
    }

    suspend fun getFirstDoneDate(taskId: Int): Date? {
        return repository.getFirstDoneDate(taskId, todayDate)
    }

    suspend fun getLastDoneDate(taskId: Int): Date? {
        return repository.getLastDoneDate(taskId, todayDate)
    }

    suspend fun getComboCount(task: Task): Int {
        var count = 0
        val mDoneHistory = repository.getDoneHistoryDesc(task._id, todayDate)
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

    suspend fun getMaxComboCount(task: Task): Int {
        var currentCount = 0
        var maxCount = 0
        val mDoneHistory = repository.getDoneHistoryAsc(task._id, todayDate)
        if (mDoneHistory.isNotEmpty()) {
            val calToday = getCalendar(DateChangeTimeUtil.date)
            var calIndex = calToday.clone() as Calendar
            var calDone = getCalendar(mDoneHistory[0])
            val recurrence = Recurrence.getFromTask(task)
            var listIndex = 0
            var isCompleted = false
            do {
                if (calIndex == calDone) {
                    // count up combo
                    currentCount++
                    if (currentCount > maxCount) {
                        maxCount = currentCount
                    }
                    if (++listIndex < mDoneHistory.size) {
                        calDone = getCalendar(mDoneHistory[listIndex])
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
