package com.hkb48.keepdo

import android.app.Application
import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hkb48.keepdo.db.TaskDatabase
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.db.entity.TaskCompletion
import java.util.*

class TaskViewModel(taskDatabase: TaskDatabase) : ViewModel() {
    private val db = taskDatabase
    val taskLiveData = db.taskDao().getTaskLiveDataListByOrder()
    val doneStatusLiveData = db.taskCompletionDao().getAllLiveData()

    fun getTaskList(): MutableList<Task> {
        return db.taskDao().getTaskListByOrder() as MutableList<Task>
    }

    fun addTask(task: Task): Int {
        return try {
            db.taskDao().add(task).toInt()
        } catch (e: SQLiteException) {
            e.printStackTrace()
            Task.INVALID_TASKID
        }
    }

    fun editTask(task: Task) {
        try {
            db.taskDao().update(task)
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }

    fun deleteTask(taskId: Int) {
        try {
            // Delete task from TASKS_TABLE_NAME
            // Records corresponding to the deleted task are also removed from TASK_COMPLETION_TABLE_NAME
            db.taskDao().delete(taskId)
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }

    fun setDoneStatus(taskId: Int, date: Date, doneSwitch: Boolean) {
        try {
            if (doneSwitch) {
                val taskCompletion = TaskCompletion(null, taskId, date)
                db.taskCompletionDao().insert(taskCompletion)
            } else {
                db.taskCompletionDao().delete(taskId, date)
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }

    fun getNumberOfDone(taskId: Int): Int {
        return db.taskCompletionDao().getAll(taskId).count()
    }

    fun getFirstDoneDate(taskId: Int): Date? {
        return db.taskCompletionDao().getFirstCompletionDate(taskId, todayDate)
    }

    fun getLastDoneDate(taskId: Int): Date? {
        return db.taskCompletionDao().getLastCompletionDate(taskId, todayDate)
    }

    fun getTask(taskId: Int): Task? {
        return db.taskDao().getTask(taskId)
    }

    fun getHistoryInMonth(taskId: Int, year: Int, month: Int): ArrayList<Date> {
        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month
        val firstDayInMonth = calendar.clone() as Calendar
        firstDayInMonth[Calendar.DAY_OF_MONTH] = 1
        val lastDayInMonth = calendar.clone() as Calendar
        lastDayInMonth[Calendar.DAY_OF_MONTH] = lastDayInMonth.getActualMaximum(Calendar.DATE)

        val doneList = db.taskCompletionDao().getCompletionHistoryBetween(
            taskId, firstDayInMonth.time, lastDayInMonth.time
        )
        return ArrayList(doneList)
    }

    fun getComboCount(taskId: Int): Int {
        var count = 0
        val mDoneHistory =
            db.taskCompletionDao().getCompletionHistoryDesc(taskId, todayDate)
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

    fun getMaxComboCount(taskId: Int): Int {
        var currentCount = 0
        var maxCount = 0
        val mDoneHistory =
            db.taskCompletionDao().getCompletionHistoryAsc(taskId, todayDate)
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

    fun getMaxSortOrderId(): Int {
        return db.taskDao().getMaxOrder()
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
            return TaskViewModel((application as KeepdoApplication).database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}