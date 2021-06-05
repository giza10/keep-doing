package com.hkb48.keepdo

import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.hkb48.keepdo.ReminderManager.Companion.instance
import com.hkb48.keepdo.widget.TasksWidgetProvider.Companion.notifyDatasetChanged
import java.util.*

class TaskSettingActivity : AppCompatActivity() {
    private var mRecurrenceFlags = booleanArrayOf(
        true, true, true, true, true, true,
        true
    )
    private lateinit var mTask: Task
    private var mMode = 0
    private var mTitleText: TextView? = null
    private lateinit var mSaveButton: Button

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        setContentView(R.layout.activity_task_setting)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowCustomEnabled(true)
            toolbar.setNavigationIcon(R.drawable.ic_close)
            val inflater = LayoutInflater.from(this)
            val v = inflater.inflate(R.layout.actionbar_task_setting, null)
            mTitleText = v.findViewById(R.id.title_text)
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
        val task = intent.getSerializableExtra("TASK-INFO") as Task?
        if (task == null) {
            mMode = MODE_NEW_TASK
            mTitleText?.setText(R.string.add_task)
            recurrence = Recurrence(
                monday = true,
                tuesday = true,
                wednesday = true,
                thursday = true,
                friday = true,
                saturday = true,
                sunday = true
            )
            mTask = Task(null, null, recurrence)
        } else {
            mTask = task
            mMode = MODE_EDIT_TASK
            mTitleText?.setText(R.string.edit_task)
            val taskName = mTask.name
            editTextTaskName.setText(taskName)
            editTextTaskName.setSelection(taskName!!.length)
            recurrence = mTask.recurrence
            mRecurrenceFlags[0] = recurrence.sunday
            mRecurrenceFlags[1] = recurrence.monday
            mRecurrenceFlags[2] = recurrence.tuesday
            mRecurrenceFlags[3] = recurrence.wednesday
            mRecurrenceFlags[4] = recurrence.thursday
            mRecurrenceFlags[5] = recurrence.friday
            mRecurrenceFlags[6] = recurrence.saturday
            val description = mTask.context
            if (description != null) {
                editTextDescription.setText(description)
                editTextDescription.setSelection(description.length)
            }
        }
        addTaskName(editTextTaskName)
        addRecurrence(recurrence)
        addReminder()
        if (::mSaveButton.isInitialized) {
            mSaveButton.isEnabled = canSave()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var ret = true
        if (item.itemId == android.R.id.home) {
            finish()
        } else {
            ret = super.onOptionsItemSelected(item)
        }
        return ret
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
                    recurrenceView
                        .update(mRecurrenceFlags)
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
        val reminder = mTask.reminder
        val reminderTime = findViewById<Button>(R.id.buttonReminderTime)
        val cancelButton = findViewById<ImageButton>(R.id.reminder_remove)
        val hourOfDay = reminder.hourOfDay
        val minute = reminder.minute
        val isChecked = reminder.enabled
        if (isChecked) {
            reminderTime.text = getString(R.string.reminder_time, hourOfDay, minute)
            cancelButton.visibility = View.VISIBLE
        } else {
            reminderTime.setText(R.string.no_reminder)
            cancelButton.visibility = View.INVISIBLE
        }
        cancelButton.setOnClickListener {
            cancelButton.visibility = View.INVISIBLE
            reminderTime.setText(R.string.no_reminder)
            reminder.enabled = false
            mTask.reminder = reminder
        }
        val timePickerDialog = TimePickerDialog(
            this,
            { _: TimePicker?, hourOfDay1: Int, minute1: Int ->
                reminderTime.text = getString(R.string.reminder_time, hourOfDay1, minute1)
                cancelButton.visibility = View.VISIBLE
                reminder.enabled = true
                reminder.hourOfDay = hourOfDay1
                reminder.minute = minute1
                mTask.reminder = reminder
            }, hourOfDay, minute, true
        )
        reminderTime.setOnClickListener { timePickerDialog.show() }
    }

    private fun onSaveClicked() {
        val editTextTaskName = findViewById<EditText>(R.id.editTextTaskName)
        val editTextDescription = findViewById<EditText>(R.id.editTextDescription)
        val recurrence = Recurrence(
            mRecurrenceFlags[1],
            mRecurrenceFlags[2], mRecurrenceFlags[3], mRecurrenceFlags[4],
            mRecurrenceFlags[5], mRecurrenceFlags[6], mRecurrenceFlags[0]
        )
        mTask.name = editTextTaskName.text.toString()
        mTask.recurrence = recurrence
        mTask.context = editTextDescription.text.toString()
        val dbAdapter = DatabaseAdapter.getInstance(this)
        val taskId: Long
        if (mMode == MODE_NEW_TASK) {
            mTask.order = (dbAdapter.maxSortOrderId + 1).toLong()
            taskId = dbAdapter.addTask(mTask)
        } else {
            dbAdapter.editTask(mTask)
            taskId = mTask.taskID
        }
        instance.setAlarm(this, taskId)
        notifyDatasetChanged(this)
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
        const val EXTRA_TASK_INFO = "TASK-INFO"
        private const val MODE_NEW_TASK = 0
        private const val MODE_EDIT_TASK = 1
    }
}