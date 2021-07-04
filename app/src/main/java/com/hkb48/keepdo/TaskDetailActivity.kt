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
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailActivity : AppCompatActivity() {
    private var mTaskId: Int = TaskInfo.INVALID_TASKID
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory((application as KeepdoApplication).database)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mTaskId = intent.getIntExtra(EXTRA_TASK_ID, TaskInfo.INVALID_TASKID)

        taskViewModel.taskLiveData.observe(this, {
            DatabaseAdapter.getInstance(this).getTask(mTaskId)?.let {
                title = it.name
            }
            updateDetails()
        })
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

    private fun updateDetails() {
        val dbAdapter = DatabaseAdapter.getInstance(this)
        val task = dbAdapter.getTask(mTaskId)

        if (task != null) {
            // Recurrence
            val recurrenceView = findViewById<RecurrenceView>(R.id.recurrenceView)
            recurrenceView.update(task.recurrence)

            // Reminder
            val reminderTextView = findViewById<TextView>(R.id.taskDetailReminderValue)
            val reminder = task.reminder
            if (task.reminder.enabled) {
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
                getString(R.string.number_of_times, dbAdapter.getNumberOfDone(task.taskId))

            // Current combo / Max combo
            val comboTextView = findViewById<TextView>(R.id.taskDetailComboValue)
            val combo = dbAdapter.getComboCount(task.taskId)
            val maxCombo = dbAdapter.getMaxComboCount(task.taskId)
            comboTextView.text = getString(R.string.current_and_max_combo_num, combo, maxCombo)

            // First date that done is set
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
            val firstDoneDateTextView = findViewById<TextView>(R.id.taskDetailFirstDoneDateValue)
            val firstDoneDate = dbAdapter.getFirstDoneDate(task.taskId)
            if (firstDoneDate != null) {
                firstDoneDateTextView.text = dateFormat.format(firstDoneDate)
            } else {
                val firstDoneDateLayout = findViewById<View>(R.id.taskDetailFirstDoneDateContainer)
                firstDoneDateLayout.visibility = View.GONE
            }

            // Last date that done is set
            val lastDoneDateTextView = findViewById<TextView>(R.id.taskDetailLastDoneDateValue)
            val lastDoneDate = dbAdapter.getLastDoneDate(task.taskId)
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