package com.hkb48.keepdo.ui.tasklist

import android.content.DialogInterface
import android.os.Bundle
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
import com.hkb48.keepdo.*
import com.hkb48.keepdo.databinding.FragmentTaskListBinding
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.ui.TasksActivity
import com.hkb48.keepdo.util.CompatUtil
import com.hkb48.keepdo.widget.TasksWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


@AndroidEntryPoint
class TaskListFragment : Fragment() {
    private val viewModel: TaskListViewModel by activityViewModels()
    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val adapter = TaskAdapter(object : TaskAdapter.ClickListener {
        override fun onItemClick(task: Task) {
            val taskId = task._id
            val action =
                TaskListFragmentDirections.actionTaskListFragmentToCalendarFragment(
                    taskId,
                    task.name
                )
            findNavController().navigate(action)
        }

        override fun onDoneClick(task: Task) {
            lifecycleScope.launch {
                val taskId = task._id
                val daysSinceLastDone = viewModel.getElapsedDaysSinceLastDoneDate(taskId)
                val doneTodayBefore = (daysSinceLastDone == 0)
                val doneTodayAfter = doneTodayBefore.not()
                if (doneTodayAfter) {
                    (requireActivity() as TasksActivity).playCheckSound()
                }
                viewModel.setDoneStatus(taskId, doneTodayAfter)

                updateTaskListItem(task)
                ReminderManager.setAlarm(requireContext(), taskId)
                TasksWidgetProvider.notifyDatasetChanged(requireContext())
            }
        }
    })

    private val mOnDateChangedListener: DateChangeTimeManager.OnDateChangedListener =
        object : DateChangeTimeManager.OnDateChangedListener {
            override fun onDateChanged() {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.date_changed)
                    .setPositiveButton(
                        R.string.dialog_ok
                    ) { _: DialogInterface?, _: Int ->
                        adapter.notifyDataSetChanged()
                    }.setCancelable(false)
                    .create().show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.recycler
        val dividerItemDecoration = DividerItemDecoration(
            requireContext(),
            LinearLayoutManager(requireContext()).orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
        binding.recycler.adapter = adapter

        binding.fab.setOnClickListener {
            val action = TaskListFragmentDirections.actionTaskListFragmentToAddEditTaskFragment(
                Task.INVALID_TASKID,
                getString(R.string.add_task)
            )
            findNavController().navigate(action)
        }

        // Cancel notification (if displayed)
        NotificationController.cancelReminder(requireContext())

        subscribeToModel()

        if (CompatUtil.isNotificationChannelSupported) {
            NotificationController.createNotificationChannel(requireContext())
        }

        (requireActivity().application as KeepdoApplication).getDateChangeTimeManager()
            .registerOnDateChangedListener(mOnDateChangedListener)
    }

    override fun onDestroyView() {
        (requireActivity().application as KeepdoApplication).getDateChangeTimeManager()
            .unregisterOnDateChangedListener(mOnDateChangedListener)
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
                        ReminderManager.cancelAlarm(requireContext(), taskId)
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
        viewModel.getObservableTaskList().observe(viewLifecycleOwner, { taskList ->
            updateTaskList(taskList)
        })
    }

    private val mutex = Mutex()

    private fun updateTaskList(taskList: List<Task>) = lifecycleScope.launch {
        mutex.withLock {
            val taskListItems: MutableList<TaskListItem> = ArrayList()
            for (task in taskList) {
                val daysSinceLastDone = viewModel.getElapsedDaysSinceLastDoneDate(task._id)
                val comboCount = viewModel.getComboCount(task)
                val item = TaskListItem(task, daysSinceLastDone, comboCount)
                taskListItems.add(item)
            }
            adapter.addHeaderAndSubmitList(requireContext(), taskListItems)
            if (taskList.isEmpty()) {
                binding.empty.visibility = View.VISIBLE
            } else {
                binding.empty.visibility = View.INVISIBLE
            }
        }
    }

    private suspend fun updateTaskListItem(task: Task) {
        val daysSinceLastDone = viewModel.getElapsedDaysSinceLastDoneDate(task._id)
        val comboCount = viewModel.getComboCount(task)
        val newItem = TaskListItem(task, daysSinceLastDone, comboCount)
        adapter.updateTask(newItem)
    }
}