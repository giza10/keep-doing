package com.hkb48.keepdo.ui.addedit

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
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
import javax.inject.Inject

@AndroidEntryPoint
class AddEditTaskFragment : Fragment() {
    private val viewModel: AddEditViewModel by viewModels()
    private var _binding: FragmentAddeditTaskBinding? = null
    private val binding get() = _binding!!
    private val args: AddEditTaskFragmentArgs by navArgs()

    @Inject
    lateinit var reminderManager: ReminderManager

    private var recurrenceFlags = booleanArrayOf(
        true, true, true, true, true, true,
        true
    )
    private var task: Task? = null
    private var mode = 0
    private var reminder: Reminder = Reminder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddeditTaskBinding.inflate(inflater, container, false)

        setFragmentResultListener("confirm") { _, data ->
            val result = data.getBooleanArray("recurrence")
            if (result != null) {
                recurrenceFlags = result
                binding.recurrenceView.update(recurrenceFlags)
            }
        }

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
            mode = MODE_NEW_TASK
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
            viewModel.getTask(taskId).observe(viewLifecycleOwner, { task ->
                // Remove observer to avoid onChange() call while saving the model.
                viewModel.getTask(taskId).removeObservers(viewLifecycleOwner)
                mode = MODE_EDIT_TASK
                task.name.let {
                    editTextTaskName.setText(it)
                    editTextTaskName.setSelection(it.length)
                }
                val recurrence = Recurrence.getFromTask(task)
                recurrenceFlags[0] = task.sunFrequency
                recurrenceFlags[1] = task.monFrequency
                recurrenceFlags[2] = task.tueFrequency
                recurrenceFlags[3] = task.wedFrequency
                recurrenceFlags[4] = task.thrFrequency
                recurrenceFlags[5] = task.friFrequency
                recurrenceFlags[6] = task.satFrequency
                task.description?.let {
                    editTextDescription.setText(it)
                    editTextDescription.setSelection(it.length)
                }
                this.task = task
                addRecurrence(recurrence)
                addReminder()
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
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
        binding.recurrenceView.apply {
            update(recurrence)
            setOnClickListener {
                // TODO
                // Use bundle as argument for now because Navigation component doesn't support
                // booleanArray.
                val bundle = bundleOf("recurrence" to recurrenceFlags.copyOf(recurrenceFlags.size))
                findNavController().navigate(R.id.recurrenceDialogFragment, bundle)
            }
        }
    }

    private fun addReminder() {
        if (task != null) {
            reminder = Reminder(task!!.reminderEnabled, task!!.reminderTime ?: 0)
        }
        val reminderTime = binding.buttonReminderTime
        val cancelButton = binding.reminderRemove
        val hourOfDay = reminder.hourOfDay
        val minute = reminder.minute
        if (reminder.enabled) {
            reminderTime.text = getString(R.string.reminder_time, hourOfDay, minute)
            cancelButton.visibility = View.VISIBLE
        } else {
            reminderTime.setText(R.string.no_reminder)
            cancelButton.visibility = View.INVISIBLE
        }
        cancelButton.setOnClickListener {
            it.visibility = View.INVISIBLE
            reminderTime.setText(R.string.no_reminder)
            reminder.enabled = false
        }
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _: TimePicker?, hourOfDay1: Int, minute1: Int ->
                reminderTime.text = getString(R.string.reminder_time, hourOfDay1, minute1)
                cancelButton.visibility = View.VISIBLE
                reminder.enabled = true
                reminder.hourOfDay = hourOfDay1
                reminder.minute = minute1
            }, hourOfDay, minute, true
        )
        reminderTime.setOnClickListener { timePickerDialog.show() }
    }

    private fun save() = lifecycleScope.launch {
        val editTextTaskName = binding.editTextTaskName
        val editTextDescription = binding.editTextDescription
        val newTask = Task(
            task?._id ?: 0,
            editTextTaskName.text.toString(),
            recurrenceFlags[1],
            recurrenceFlags[2],
            recurrenceFlags[3],
            recurrenceFlags[4],
            recurrenceFlags[5],
            recurrenceFlags[6],
            recurrenceFlags[0],
            editTextDescription.text.toString(),
            reminder.enabled,
            reminder.timeInMillis,
            task?.listOrder ?: viewModel.getNewSortOrder()
        )

        val taskId = if (mode == MODE_NEW_TASK) {
            viewModel.addTask(newTask)
        } else {
            viewModel.editTask(newTask)
            task?._id ?: Task.INVALID_TASKID
        }
        reminderManager.setAlarm(taskId)
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