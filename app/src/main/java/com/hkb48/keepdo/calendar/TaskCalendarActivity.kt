package com.hkb48.keepdo.calendar

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hkb48.keepdo.CheckSoundPlayer
import com.hkb48.keepdo.R
import com.hkb48.keepdo.databinding.ActivityTaskCalendarBinding
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskCalendarActivity : AppCompatActivity() {
    private val taskViewModel: TaskViewModel by viewModels()
    private val mCheckSound = CheckSoundPlayer(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binder = ActivityTaskCalendarBinding.inflate(layoutInflater)
        setContentView(binder.root)
        setSupportActionBar(binder.includedToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        subscribeToModel()
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, CalendarFragment()).commit()
    }

    public override fun onResume() {
        mCheckSound.load()
        super.onResume()
    }

    override fun onPause() {
        mCheckSound.unload()
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun subscribeToModel() {
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, Task.INVALID_TASKID)
        taskViewModel.getObservableTask(taskId).observe(this, { task ->
            title = task.name
        })
    }

    fun playCheckSound() {
        mCheckSound.play()
    }

    companion object {
        const val EXTRA_TASK_ID = "TASK-ID"
    }
}