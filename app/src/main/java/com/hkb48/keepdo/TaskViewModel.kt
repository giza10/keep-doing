package com.hkb48.keepdo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hkb48.keepdo.data.TaskDatabase

class TaskViewModel(taskDatabase: TaskDatabase) : ViewModel() {
    val taskLiveData = taskDatabase.taskDao().getTaskLiveDataListByOrder()
    val doneStatusLiveData = taskDatabase.taskCompletionDao().getAllLiveData()
}

class TaskViewModelFactory(private val taskDatabase: TaskDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(taskDatabase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}