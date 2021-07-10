package com.hkb48.keepdo

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.hkb48.keepdo.db.entity.Task
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailActivity : AppCompatActivity() {
    private var mTaskId: Int = Task.INVALID_TASKID

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mTaskId = intent.getIntExtra(EXTRA_TASK_ID, Task.INVALID_TASKID)

        val taskViewModel: TaskViewModel by viewModels {
            TaskViewModelFactory(application)
        }
        subscribeToModel(taskViewModel)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_detail, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.menu_edit -> {
                startActivity(Intent(
                    this@TaskDetailActivity,
                    TaskSettingActivity::class.java
                ).apply {
                    putExtra(TaskSettingActivity.EXTRA_TASK_ID, mTaskId)
                })
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun subscribeToModel(model: TaskViewModel) {
        model.taskLiveData.observe(this, {
            model.getTask(mTaskId)?.let {
                title = it.name
            }
            updateDetails(model)
        })

    }

    private fun updateDetails(model: TaskViewModel) {
        val task = model.getTask(mTaskId)

        if (task != null) {
            // Recurrence
            val recurrenceView = findViewById<RecurrenceView>(R.id.recurrenceView)
            recurrenceView.update(Recurrence.getFromTask(task))

            // Reminder
            val reminderTextView = findViewById<TextView>(R.id.taskDetailReminderValue)
            val reminder = Reminder(task.reminderEnabled, task.reminderTime ?: 0)
            if (reminder.enabled) {
                reminderTextView.text =
                    getString(R.string.remind_at, reminder.hourOfDay, reminder.minute)
            } else {
                reminderTextView.setText(R.string.no_reminder)
            }

            // Context
            val contextTitleTextView = findViewById<TextView>(R.id.taskDetailContext)
            val contextTextView = findViewById<TextView>(R.id.taskDetailContextDescription)
            val contextStr = task.context
            if (contextStr == null || contextStr.isEmpty()) {
                val contextLayout = findViewById<View>(R.id.taskDetailContextContainer)
                contextLayout.visibility = View.GONE
                contextTitleTextView.visibility = View.INVISIBLE
                contextTextView.visibility = View.INVISIBLE
            } else {
                contextTitleTextView.visibility = View.VISIBLE
                contextTextView.visibility = View.VISIBLE
                contextTextView.text = contextStr
            }

            // Total number of done
            val numOfDoneTextView = findViewById<TextView>(R.id.taskDetailNumOfDoneValue)
            numOfDoneTextView.text =
                getString(R.string.number_of_times, model.getNumberOfDone(task._id!!))

            // Current combo / Max combo
            val comboTextView = findViewById<TextView>(R.id.taskDetailComboValue)
            val combo = model.getComboCount(task._id)
            val maxCombo = model.getMaxComboCount(task._id)
            comboTextView.text = getString(R.string.current_and_max_combo_num, combo, maxCombo)

            // First date that done is set
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
            val firstDoneDateTextView = findViewById<TextView>(R.id.taskDetailFirstDoneDateValue)
            val firstDoneDate = model.getFirstDoneDate(task._id)
            if (firstDoneDate != null) {
                firstDoneDateTextView.text = dateFormat.format(firstDoneDate)
            } else {
                val firstDoneDateLayout = findViewById<View>(R.id.taskDetailFirstDoneDateContainer)
                firstDoneDateLayout.visibility = View.GONE
            }

            // Last date that done is set
            val lastDoneDateTextView = findViewById<TextView>(R.id.taskDetailLastDoneDateValue)
            val lastDoneDate = model.getLastDoneDate(task._id)
            if (lastDoneDate != null) {
                lastDoneDateTextView.text = dateFormat.format(lastDoneDate)
            } else {
                val lastDoneDateLayout = findViewById<View>(R.id.taskDetailLastDoneDateContainer)
                lastDoneDateLayout.visibility = View.GONE
            }
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "TASK-ID"
    }
}