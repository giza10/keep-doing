package com.hkb48.keepdo

import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.widget.TasksWidgetProvider
import java.util.*

class TaskSettingActivity : AppCompatActivity() {
    private var mRecurrenceFlags = booleanArrayOf(
        true, true, true, true, true, true,
        true
    )
    private var mTask: Task? = null
    private var mMode = 0
    private var mReminder: Reminder = Reminder()
    private lateinit var mSaveButton: Button
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(application)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        setContentView(R.layout.activity_task_setting)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        var titleText: TextView? = null
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowCustomEnabled(true)
            toolbar.setNavigationIcon(R.drawable.ic_close)
            val v = View.inflate(applicationContext, R.layout.actionbar_task_setting, null)
            titleText = v.findViewById(R.id.title_text)
            mSaveButton = v.findViewById(R.id.button_save)
            mSaveButton.setOnClickListener { onSaveClicked() }
            actionBar.customView = v
        }
        val editTextTaskName = findViewById<EditText>(R.id.editTextTaskName)
        enableInputEmoji(editTextTaskName)
        val editTextDescription = findViewById<EditText>(R.id.editTextDescription)
        enableInputEmoji(editTextDescription)
        val recurrence: Recurrence
        val intent = intent
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, Task.INVALID_TASKID)
        if (taskId == Task.INVALID_TASKID) {
            mMode = MODE_NEW_TASK
            titleText?.setText(R.string.add_task)
            recurrence = Recurrence(
                monday = true,
                tuesday = true,
                wednesday = true,
                thursday = true,
                friday = true,
                saturday = true,
                sunday = true
            )
        } else {
            val task = taskViewModel.getTask(taskId)!!
            mMode = MODE_EDIT_TASK
            titleText?.setText(R.string.edit_task)
            task.name.let {
                editTextTaskName.setText(it)
                editTextTaskName.setSelection(it.length)
            }
            recurrence = Recurrence.getFromTask(task)
            mRecurrenceFlags[0] = task.sunFrequency
            mRecurrenceFlags[1] = task.monFrequency
            mRecurrenceFlags[2] = task.tueFrequency
            mRecurrenceFlags[3] = task.wedFrequency
            mRecurrenceFlags[4] = task.thrFrequency
            mRecurrenceFlags[5] = task.friFrequency
            mRecurrenceFlags[6] = task.satFrequency
            task.context?.let {
                editTextDescription.setText(it)
                editTextDescription.setSelection(it.length)
            }
            mTask = task
        }
        addTaskName(editTextTaskName)
        addRecurrence(recurrence)
        addReminder()
        if (::mSaveButton.isInitialized) {
            mSaveButton.isEnabled = canSave()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun addTaskName(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (::mSaveButton.isInitialized) {
                    mSaveButton.isEnabled = s.isNotEmpty()
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int,
                count: Int
            ) {
            }
        })
    }

    private fun addRecurrence(recurrence: Recurrence) {
        val weekNames = resources.getStringArray(
            R.array.week_names
        )
        val recurrenceView = findViewById<RecurrenceView>(R.id.recurrenceView)
        recurrenceView.update(recurrence)
        findViewById<View>(R.id.recurrenceView).setOnClickListener {
            val tmpRecurrenceFlags = mRecurrenceFlags.copyOf(mRecurrenceFlags.size)
            AlertDialog.Builder(this@TaskSettingActivity)
                .setTitle(getString(R.string.recurrence))
                .setMultiChoiceItems(
                    weekNames,
                    mRecurrenceFlags
                ) { _: DialogInterface?, which: Int, isChecked: Boolean ->
                    mRecurrenceFlags[which] = isChecked
                }
                .setPositiveButton(
                    getString(R.string.dialog_ok)
                ) { _: DialogInterface?, _: Int ->
                    recurrenceView.update(mRecurrenceFlags)
                }
                .setNegativeButton(
                    getString(R.string.dialog_cancel)
                ) { _: DialogInterface?, _: Int ->
                    mRecurrenceFlags = tmpRecurrenceFlags
                }
                .show()
        }
    }

    private fun addReminder() {
        if (mTask != null) {
            mReminder = Reminder(mTask!!.reminderEnabled, mTask!!.reminderTime ?: 0)
        }
        val reminderTime = findViewById<Button>(R.id.buttonReminderTime)
        val cancelButton = findViewById<ImageButton>(R.id.reminder_remove)
        val hourOfDay = mReminder.hourOfDay
        val minute = mReminder.minute
        if (mReminder.enabled) {
            reminderTime.text = getString(R.string.reminder_time, hourOfDay, minute)
            cancelButton.visibility = View.VISIBLE
        } else {
            reminderTime.setText(R.string.no_reminder)
            cancelButton.visibility = View.INVISIBLE
        }
        cancelButton.setOnClickListener {
            it.visibility = View.INVISIBLE
            reminderTime.setText(R.string.no_reminder)
            mReminder.enabled = false
        }
        val timePickerDialog = TimePickerDialog(
            this,
            { _: TimePicker?, hourOfDay1: Int, minute1: Int ->
                reminderTime.text = getString(R.string.reminder_time, hourOfDay1, minute1)
                cancelButton.visibility = View.VISIBLE
                mReminder.enabled = true
                mReminder.hourOfDay = hourOfDay1
                mReminder.minute = minute1
            }, hourOfDay, minute, true
        )
        reminderTime.setOnClickListener { timePickerDialog.show() }
    }

    private fun onSaveClicked() {
        val editTextTaskName = findViewById<EditText>(R.id.editTextTaskName)
        val editTextDescription = findViewById<EditText>(R.id.editTextDescription)
        val newTask = Task(
            mTask?._id,
            editTextTaskName.text.toString(),
            mRecurrenceFlags[1],
            mRecurrenceFlags[2],
            mRecurrenceFlags[3],
            mRecurrenceFlags[4],
            mRecurrenceFlags[5],
            mRecurrenceFlags[6],
            mRecurrenceFlags[0],
            editTextDescription.text.toString(),
            mReminder.enabled,
            mReminder.timeInMillis,
            mTask?.listOrder ?: taskViewModel.getMaxSortOrderId() + 1
        )

        val taskId = if (mMode == MODE_NEW_TASK) {
            taskViewModel.addTask(newTask)
        } else {
            taskViewModel.editTask(newTask)
            mTask?._id ?: Task.INVALID_TASKID
        }
        ReminderManager.setAlarm(this, taskId)
        TasksWidgetProvider.notifyDatasetChanged(this)
        finish()
    }

    private fun canSave(): Boolean {
        val editTextTaskName = findViewById<EditText>(R.id.editTextTaskName)
        return editTextTaskName.text.isNotEmpty()
    }

    private fun enableInputEmoji(editText: EditText) {
        val bundle = editText.getInputExtras(true)
        bundle?.putBoolean("allowEmoji", true)
    }

    companion object {
        const val EXTRA_TASK_ID = "TASK-ID"
        private const val MODE_NEW_TASK = 0
        private const val MODE_EDIT_TASK = 1
    }
}