package com.hkb48.keepdo.ui.tasklist

import androidx.lifecycle.*
import com.hkb48.keepdo.TaskRepository
import com.hkb48.keepdo.db.entity.TaskWithDoneHistory
import com.hkb48.keepdo.util.DateChangeTimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {
    private val _forceUpdate = MutableLiveData(false)
    private var _taskList = _forceUpdate.switchMap {
        repository.getTaskListWithDoneHistoryFlow().asLiveData()
    }
    private val taskList = _taskList

    fun refresh() {
        _forceUpdate.value = true
    }

    fun getTaskListWithDoneHistory(): LiveData<List<TaskWithDoneHistory>> {
        return taskList
    }

    suspend fun deleteTask(taskId: Int) {
        // Records corresponding to the deleted task are also removed from related db table.
        repository.deleteTask(taskId)
    }

    suspend fun setDoneStatus(taskId: Int, isDone: Boolean) {
        val today = DateChangeTimeUtil.dateTime
        repository.setDoneStatus(taskId, today, isDone)
    }
}
