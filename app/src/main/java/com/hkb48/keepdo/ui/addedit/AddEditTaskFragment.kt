package com.hkb48.keepdo.ui.addedit

import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hkb48.keepdo.R
import com.hkb48.keepdo.Recurrence
import com.hkb48.keepdo.Reminder
import com.hkb48.keepdo.ReminderManager
import com.hkb48.keepdo.databinding.FragmentAddeditTaskBinding
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.widget.TasksWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditTaskFragment : Fragment() {
    private val viewModel: AddEditViewModel by viewModels()
    private var _binding: FragmentAddeditTaskBinding? = null
    private val binding get() = _binding!!
    private val args: AddEditTaskFragmentArgs by navArgs()

    private var mRecurrenceFlags = booleanArrayOf(
        true, true, true, true, true, true,
        true
    )
    private var mTask: Task? = null
    private var mMode = 0
    private var mReminder: Reminder = Reminder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddeditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val editTextTaskName = binding.editTextTaskName
        enableInputEmoji(editTextTaskName)
        val editTextDescription = binding.editTextDescription
        enableInputEmoji(editTextDescription)
        val taskId = args.taskId
        if (taskId == Task.INVALID_TASKID) {
            mMode = MODE_NEW_TASK
            val recurrence = Recurrence(
                monday = true,
                tuesday = true,
                wednesday = true,
                thursday = true,
                friday = true,
                saturday = true,
                sunday = true
            )
            addRecurrence(recurrence)
            addReminder()
        } else {
            viewModel.getObservableTask(taskId).observe(viewLifecycleOwner, { task ->
                viewModel.getObservableTask(taskId).removeObservers(viewLifecycleOwner)
                mMode = MODE_EDIT_TASK
                task.name.let {
                    editTextTaskName.setText(it)
                    editTextTaskName.setSelection(it.length)
                }
                val recurrence = Recurrence.getFromTask(task)
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
                addRecurrence(recurrence)
                addReminder()
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_addedit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                if (canSave()) {
                    hideKeyboard()
                    save()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // Check if no view has focus
        requireActivity().currentFocus?.let { currentFocusedView ->
            inputMethodManager.hideSoftInputFromWindow(
                currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    private fun addRecurrence(recurrence: Recurrence) {
        val weekNames = resources.getStringArray(
            R.array.week_names
        )
        val recurrenceView = binding.recurrenceView
        recurrenceView.update(recurrence)
        recurrenceView.setOnClickListener {
            val tmpRecurrenceFlags = mRecurrenceFlags.copyOf(mRecurrenceFlags.size)
            AlertDialog.Builder(requireContext())
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
        val reminderTime = binding.buttonReminderTime
        val cancelButton = binding.reminderRemove
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
            requireContext(),
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

    private fun save() = lifecycleScope.launch {
        val editTextTaskName = binding.editTextTaskName
        val editTextDescription = binding.editTextDescription
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
            mTask?.listOrder ?: viewModel.getNewSortOrder()
        )

        val taskId = if (mMode == MODE_NEW_TASK) {
            viewModel.addTask(newTask)
        } else {
            viewModel.editTask(newTask)
            mTask?._id ?: Task.INVALID_TASKID
        }
        ReminderManager.setAlarm(requireContext(), taskId)
        TasksWidgetProvider.notifyDatasetChanged(requireContext())
        findNavController().popBackStack()
    }

    private fun canSave(): Boolean {
        return binding.editTextTaskName.text.trim().isNotEmpty()
    }

    private fun enableInputEmoji(editText: EditText) {
        val bundle = editText.getInputExtras(true)
        bundle?.putBoolean("allowEmoji", true)
    }

    companion object {
        private const val MODE_NEW_TASK = 0
        private const val MODE_EDIT_TASK = 1
    }
}