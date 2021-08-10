package com.hkb48.keepdo

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hkb48.keepdo.databinding.ActivityTaskDetailBinding
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.viewmodel.TaskViewModel
import com.hkb48.keepdo.viewmodel.TaskViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailActivity : AppCompatActivity() {
    private var mTaskId: Int = Task.INVALID_TASKID
    private lateinit var binding: ActivityTaskDetailBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.includedToolbar.toolbar)
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
        model.getObservableTask(mTaskId).observe(this, { task ->
            title = task.name
            updateDetails(model, task)
        })

    }

    private fun updateDetails(model: TaskViewModel, task: Task) = lifecycleScope.launch {
        // Recurrence
        binding.recurrenceView.update(Recurrence.getFromTask(task))

        // Reminder
        val reminderTextView = binding.taskDetailReminderValue
        val reminder = Reminder(task.reminderEnabled, task.reminderTime ?: 0)
        if (reminder.enabled) {
            reminderTextView.text =
                getString(R.string.remind_at, reminder.hourOfDay, reminder.minute)
        } else {
            reminderTextView.setText(R.string.no_reminder)
        }

        // Context
        val contextTitleTextView = binding.taskDetailContext
        val contextTextView = binding.taskDetailContextDescription
        val contextStr = task.context
        if (contextStr == null || contextStr.isEmpty()) {
            val contextLayout = binding.taskDetailContextContainer
            contextLayout.visibility = View.GONE
            contextTitleTextView.visibility = View.INVISIBLE
            contextTextView.visibility = View.INVISIBLE
        } else {
            contextTitleTextView.visibility = View.VISIBLE
            contextTextView.visibility = View.VISIBLE
            contextTextView.text = contextStr
        }

        // Total number of done
        val numOfDone = model.getNumberOfDone(task._id!!)
        binding.taskDetailNumOfDoneValue.text = getString(R.string.number_of_times, numOfDone)

        // Current combo / Max combo
        val combo = model.getComboCount(task._id)
        val maxCombo = model.getMaxComboCount(task._id)
        binding.taskDetailComboValue.text =
            getString(R.string.current_and_max_combo_num, combo, maxCombo)

        // First date that done is set
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
        val firstDoneDate = model.getFirstDoneDate(task._id)
        if (firstDoneDate != null) {
            binding.taskDetailFirstDoneDateValue.text = dateFormat.format(firstDoneDate)
        } else {
            binding.taskDetailFirstDoneDateContainer.visibility = View.GONE
        }

        // Last date that done is set
        val lastDoneDate = model.getLastDoneDate(task._id)
        if (lastDoneDate != null) {
            binding.taskDetailLastDoneDateValue.text = dateFormat.format(lastDoneDate)
        } else {
            binding.taskDetailLastDoneDateContainer.visibility = View.GONE
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "TASK-ID"
    }
}