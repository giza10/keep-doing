package com.hkb48.keepdo.calendar

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.hkb48.keepdo.CheckSoundPlayer
import com.hkb48.keepdo.R
import com.hkb48.keepdo.TaskViewModel
import com.hkb48.keepdo.TaskViewModelFactory
import com.hkb48.keepdo.db.entity.Task

class TaskCalendarActivity : AppCompatActivity() {
    private val mCheckSound = CheckSoundPlayer(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_calendar)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val taskViewModel: TaskViewModel by viewModels {
            TaskViewModelFactory(application)
        }
        subscribeToModel(taskViewModel)
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

    private fun subscribeToModel(view: TaskViewModel) {
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, Task.INVALID_TASKID)
        view.taskLiveData.observe(this, {
            view.getTask(taskId)?.let {
                title = it.name
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, CalendarFragment()).commit()
        })
    }

    fun playCheckSound() {
        mCheckSound.play()
    }

    companion object {
        const val EXTRA_TASK_ID = "TASK-ID"
    }
}