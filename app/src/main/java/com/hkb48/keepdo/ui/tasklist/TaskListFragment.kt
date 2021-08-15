package com.hkb48.keepdo.ui.tasklist

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.hkb48.keepdo.*
import com.hkb48.keepdo.databinding.FragmentTaskListBinding
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.db.entity.TaskWithDoneHistory
import com.hkb48.keepdo.ui.TasksActivity
import com.hkb48.keepdo.util.CompatUtil
import com.hkb48.keepdo.util.DoneHistoryUtil
import com.hkb48.keepdo.widget.TasksWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject


@AndroidEntryPoint
class TaskListFragment : Fragment() {
    private val viewModel: TaskListViewModel by activityViewModels()
    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var dateChangeTimeManager: DateChangeTimeManager

    @Inject
    lateinit var notificationController: NotificationController

    @Inject
    lateinit var reminderManager: ReminderManager

    private val adapter = TaskAdapter(object : TaskAdapter.ClickListener {
        override fun onItemClick(taskWithDoneHistory: TaskWithDoneHistory) {
            val task = taskWithDoneHistory.task
            val action =
                TaskListFragmentDirections.actionTaskListFragmentToCalendarFragment(
                    task._id,
                    task.name
                )
            findNavController().navigate(action)
        }

        override fun onDoneClick(taskWithDoneHistory: TaskWithDoneHistory) {
            lifecycleScope.launch {
                val taskId = taskWithDoneHistory.task._id
                val daysSinceLastDone =
                    DoneHistoryUtil(taskWithDoneHistory).getElapsedDaysSinceLastDone()
                val doneTodayBefore = (daysSinceLastDone == 0)
                val doneTodayAfter = doneTodayBefore.not()
                if (doneTodayAfter) {
                    (requireActivity() as TasksActivity).playCheckSound()
                }
                viewModel.setDoneStatus(taskId, doneTodayAfter)

                reminderManager.setAlarm(taskId)
                TasksWidgetProvider.notifyDatasetChanged(requireContext())
            }
        }
    })

    private val dateChangedListener: DateChangeTimeManager.OnDateChangedListener =
        object : DateChangeTimeManager.OnDateChangedListener {
            override fun onDateChanged() {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.date_changed)
                    .setPositiveButton(
                        R.string.dialog_ok
                    ) { _: DialogInterface?, _: Int ->
                        viewModel.refresh()
                    }.setCancelable(false)
                    .create().show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)

        subscribeToModel()

        dateChangeTimeManager.registerOnDateChangedListener(dateChangedListener)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.recycler

        // Add dividers.
        val dividerItemDecoration = DividerItemDecoration(
            requireContext(),
            LinearLayoutManager(requireContext()).orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)

        // Disable animation when the list item is updated.
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        binding.recycler.adapter = adapter

        binding.fab.setOnClickListener {
            val action = TaskListFragmentDirections.actionTaskListFragmentToAddEditTaskFragment(
                Task.INVALID_TASKID,
                getString(R.string.add_task)
            )
            findNavController().navigate(action)
        }

        // Cancel notification (if displayed)
        notificationController.cancelReminder()

        if (CompatUtil.isNotificationChannelSupported) {
            notificationController.createNotificationChannel()
        }
    }

    override fun onDestroyView() {
        dateChangeTimeManager.unregisterOnDateChangedListener(dateChangedListener)
        super.onDestroyView()
        _binding = null
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val position = adapter.getLongPressedPosition()
        val taskListItem = adapter.getItemAt(position)
        val taskId = taskListItem.task._id
        return when (item.itemId) {
            ContextMenuConstants.CONTEXT_MENU_EDIT -> {
                val action =
                    TaskListFragmentDirections.actionTaskListFragmentToAddEditTaskFragment(
                        taskId,
                        getString(R.string.edit_task)
                    )
                findNavController().navigate(action)
                true
            }
            ContextMenuConstants.CONTEXT_MENU_DELETE -> {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.delete_confirmation)
                    .setPositiveButton(
                        R.string.dialog_ok
                    ) { _: DialogInterface?, _: Int ->
                        // Cancel the alarm for Reminder before deleting the task.
                        reminderManager.cancelAlarm(taskId)
                        lifecycleScope.launch {
                            viewModel.deleteTask(taskId)
                            TasksWidgetProvider.notifyDatasetChanged(requireContext())
                        }
                    }
                    .setNegativeButton(
                        R.string.dialog_cancel
                    ) { _: DialogInterface?, _: Int -> }.setCancelable(true).create()
                    .show()
                true
            }
            else -> {
                Toast.makeText(requireContext(), "default", Toast.LENGTH_SHORT).show()
                super.onContextItemSelected(item)
            }
        }
    }

    private fun subscribeToModel() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG_KEEPDO, "TaskListFragment#subscribeToModel() called.")
        }
        viewModel.getTaskListWithDoneHistory().observe(viewLifecycleOwner, { taskList ->
            if (BuildConfig.DEBUG) {
                Log.d(TAG_KEEPDO, "TaskListFragment#subscribeToModel() - viewModel onChanged().")
            }
            updateTaskList(taskList)
        })
    }

    private val mutex = Mutex()

    private fun updateTaskList(taskList: List<TaskWithDoneHistory>) = lifecycleScope.launch {
        mutex.withLock {
            adapter.addHeaderAndSubmitList(requireContext(), taskList)
            if (taskList.isEmpty()) {
                binding.empty.visibility = View.VISIBLE
            } else {
                binding.empty.visibility = View.INVISIBLE
            }
        }
    }

    companion object {
        private const val TAG_KEEPDO = "#LOG_KEEPDO: "
    }
}