package com.hkb48.keepdo.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteException
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.hkb48.keepdo.DateChangeTimeUtil
import com.hkb48.keepdo.KeepdoApplication
import com.hkb48.keepdo.Recurrence
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.db.entity.TaskCompletion
import java.util.*

class TaskViewModel(application: KeepdoApplication) : ViewModel() {
    private val repository = application.getRepository()
    private val taskList = repository.getTaskListFlow().asLiveData()

    fun getObservableTaskList(): LiveData<List<Task>> {
        return taskList
    }

    suspend fun getTaskList(): List<Task> {
        return repository.getTaskList()
    }

    suspend fun addTask(task: Task): Int {
        return try {
            repository.addTask(task)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            Task.INVALID_TASKID
        }
    }

    suspend fun editTask(task: Task) {
        try {
            repository.editTask(task)
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }

    suspend fun deleteTask(taskId: Int) {
        try {
            // Delete task from TASKS_TABLE_NAME
            // Records corresponding to the deleted task are also removed from TASK_COMPLETION_TABLE_NAME
            repository.deleteTask(taskId)
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }

    fun getObservableTask(taskId: Int): LiveData<Task> {
        return repository.getTaskFlow(taskId).asLiveData()
    }

    suspend fun getTask(taskId: Int): Task? {
        return repository.getTask(taskId)
    }

    suspend fun getMaxSortOrderId(): Int {
        return repository.getMaxOrder()
    }

    suspend fun setDoneStatus(taskId: Int, date: Date, doneSwitch: Boolean) {
        try {
            if (doneSwitch) {
                val taskCompletion = TaskCompletion(null, taskId, date)
                repository.setDone(taskCompletion)
            } else {
                repository.unsetDone(taskId, date)
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }

    fun getObservableDoneStatusList(): LiveData<List<TaskCompletion>> {
        return repository.getDoneStatusListFlow().asLiveData()
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

    suspend fun getComboCount(taskId: Int): Int {
        var count = 0
        val mDoneHistory = repository.getDoneHistoryDesc(taskId, todayDate)
        if (mDoneHistory.isNotEmpty()) {
            val calToday = getCalendar(DateChangeTimeUtil.date)
            val calIndex = calToday.clone() as Calendar
            var calDone = getCalendar(mDoneHistory[0])
            val recurrence = Recurrence.getFromTask(getTask(taskId)!!)
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

    suspend fun getMaxComboCount(taskId: Int): Int {
        var currentCount = 0
        var maxCount = 0
        val mDoneHistory = repository.getDoneHistoryAsc(taskId, todayDate)
        if (mDoneHistory.isNotEmpty()) {
            val calToday = getCalendar(DateChangeTimeUtil.date)
            var calIndex = calToday.clone() as Calendar
            var calDone = getCalendar(mDoneHistory[0])
            val recurrence = Recurrence.getFromTask(getTask(taskId)!!)
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

class TaskViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(application as KeepdoApplication) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}