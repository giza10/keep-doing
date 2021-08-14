package com.hkb48.keepdo.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.hkb48.keepdo.TaskRepository
import com.hkb48.keepdo.db.entity.DoneHistory
import com.hkb48.keepdo.db.entity.TaskWithDoneHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {
    fun getTaskWithDoneHistory(id: Int): LiveData<TaskWithDoneHistory> {
        return repository.getTaskWithDoneHistory(id).asLiveData()
    }

    suspend fun setDoneStatus(taskId: Int, date: Date, doneSwitch: Boolean) {
        if (doneSwitch) {
            val doneInfo = DoneHistory(0, taskId, date)
            repository.setDone(doneInfo)
        } else {
            repository.unsetDone(taskId, date)
        }
    }
}
