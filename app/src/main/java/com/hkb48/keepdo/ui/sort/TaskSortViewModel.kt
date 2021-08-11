package com.hkb48.keepdo.ui.sort

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.hkb48.keepdo.TaskRepository
import com.hkb48.keepdo.db.entity.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskSortViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {
    private val taskList = repository.getTaskListFlow().asLiveData()

    fun getObservableTaskList(): LiveData<List<Task>> {
        return taskList
    }

    suspend fun editTask(task: Task) {
        repository.editTask(task)
    }
}
