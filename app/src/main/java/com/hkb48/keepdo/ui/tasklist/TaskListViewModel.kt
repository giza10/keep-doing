package com.hkb48.keepdo.ui.tasklist

import androidx.lifecycle.*
import com.hkb48.keepdo.DateChangeTimeUtil
import com.hkb48.keepdo.Recurrence
import com.hkb48.keepdo.TaskRepository
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.db.entity.TaskCompletion
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {
    private val _forceUpdate = MutableLiveData(false)
    private var _taskList = _forceUpdate.switchMap {
        repository.getTaskListFlow().asLiveData()
    }
    private val taskList = _taskList

    fun refresh() {
        _forceUpdate.value = true
    }

    fun getObservableTaskList(): LiveData<List<Task>> {
        return taskList
    }

    suspend fun deleteTask(taskId: Int) {
        // Delete task from TASKS_TABLE_NAME
        // Records corresponding to the deleted task are also removed from TASK_COMPLETION_TABLE_NAME
        repository.deleteTask(taskId)
    }

    suspend fun setDoneStatus(taskId: Int, date: Date, doneSwitch: Boolean) {
        if (doneSwitch) {
            val taskCompletion = TaskCompletion(null, taskId, date)
            repository.setDone(taskCompletion)
        } else {
            repository.unsetDone(taskId, date)
        }
    }

    fun getObservableDoneStatusList(): LiveData<List<TaskCompletion>> {
        return repository.getDoneStatusListFlow().asLiveData()
    }

    suspend fun getLastDoneDate(taskId: Int): Date? {
        return repository.getLastDoneDate(taskId, todayDate)
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
