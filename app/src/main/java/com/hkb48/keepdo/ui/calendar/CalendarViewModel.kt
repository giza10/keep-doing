package com.hkb48.keepdo.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.hkb48.keepdo.TaskRepository
import com.hkb48.keepdo.db.entity.TaskWithDoneHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {
    fun getTaskWithDoneHistory(id: Int): LiveData<TaskWithDoneHistory> {
        return repository.getTaskWithDoneHistoryFlow(id).asLiveData()
    }

    suspend fun setDoneStatus(taskId: Int, date: Date, isDone: Boolean) {
        repository.setDoneStatus(taskId, date, isDone)
    }
}
