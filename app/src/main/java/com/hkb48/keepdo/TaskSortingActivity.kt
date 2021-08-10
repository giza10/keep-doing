package com.hkb48.keepdo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hkb48.keepdo.SortableListView.DragAndDropListener
import com.hkb48.keepdo.databinding.ActivityTaskSortingBinding
import com.hkb48.keepdo.databinding.TaskSortingListItemBinding
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.viewmodel.TaskViewModel
import com.hkb48.keepdo.viewmodel.TaskViewModelFactory
import com.hkb48.keepdo.widget.TasksWidgetProvider
import kotlinx.coroutines.launch


class TaskSortingActivity : AppCompatActivity() {
    private val mAdapter = TaskAdapter()
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(application)
    }
    private lateinit var mDataList: MutableList<Task>
    private lateinit var binding: ActivityTaskSortingBinding
    private val onDrop: DragAndDropListener = object : DragAndDropListener {
        override fun onDrag(from: Int, to: Int) {
            if (from != to) {
                val item = mAdapter.getItem(from) as Task
                mDataList.removeAt(from)
                mDataList.add(to, item)
                mAdapter.notifyDataSetChanged()
            }
        }

        override fun onDrop(from: Int, to: Int) {
            if (from != to) {
                enableSaveButton()
            }
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskSortingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = binding.includedToolbar.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        taskViewModel.getObservableTaskList().observe(this, { taskList ->
            taskViewModel.getObservableTaskList().removeObservers(this@TaskSortingActivity)
            mDataList = taskList.toMutableList()
            binding.mainListView.apply {
                adapter = mAdapter
                setDragAndDropListener(onDrop)
            }
            binding.cancelButton.setOnClickListener { onCancelClicked() }
            mAdapter.notifyDataSetChanged()
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    /**
     * Callback method for "Save" button
     */
    private fun onSaveClicked() = lifecycleScope.launch {
        for (index in mDataList.indices) {
            val task = mDataList[index]
            val newTask = Task(
                task._id,
                task.name,
                task.monFrequency,
                task.tueFrequency,
                task.wedFrequency,
                task.thrFrequency,
                task.friFrequency,
                task.satFrequency,
                task.sunFrequency,
                task.context,
                task.reminderEnabled,
                task.reminderTime,
                index
            )
            taskViewModel.editTask(newTask)
        }
        TasksWidgetProvider.notifyDatasetChanged(applicationContext)
        finish()
    }

    /**
     * Callback method for "Cancel" button
     */
    private fun onCancelClicked() {
        finish()
    }

    private fun enableSaveButton() {
        binding.okButton.apply {
            isEnabled = true
            setOnClickListener { onSaveClicked() }
        }
    }

    private inner class TaskAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return mDataList.size
        }

        override fun getItem(position: Int): Any {
            return mDataList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun isEnabled(position: Int): Boolean {
            return false
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: TaskSortingListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).root
            val itemView = view as SortableListItem
            val task = getItem(position) as Task
            itemView.setText(task.name)
            return view
        }
    }
}