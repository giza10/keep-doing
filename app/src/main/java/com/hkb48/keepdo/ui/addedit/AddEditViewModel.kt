package com.hkb48.keepdo.ui.addedit

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.hkb48.keepdo.TaskRepository
import com.hkb48.keepdo.db.entity.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {
    suspend fun addTask(task: Task): Int {
        return repository.addTask(task)
    }

    suspend fun editTask(task: Task) {
        repository.editTask(task)
    }

    fun getObservableTask(taskId: Int): LiveData<Task> {
        return repository.getTaskFlow(taskId).asLiveData()
    }

    suspend fun getNewSortOrder(): Int {
        return repository.getMaxOrder() + 1
    }
}
