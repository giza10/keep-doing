package com.hkb48.keepdo.ui.sort

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hkb48.keepdo.databinding.FragmentTaskSortBinding
import com.hkb48.keepdo.databinding.TaskSortingListItemBinding
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.ui.sort.SortableListView.DragAndDropListener
import com.hkb48.keepdo.widget.TasksWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskSortFragment : Fragment() {
    private val viewModel: TaskSortViewModel by viewModels()
    private var _binding: FragmentTaskSortBinding? = null
    private val binding get() = _binding!!

    private val mAdapter = TaskAdapter()
    private lateinit var mDataList: MutableList<Task>
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskSortBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getObservableTaskList().observe(viewLifecycleOwner, { taskList ->
            viewModel.getObservableTaskList().removeObservers(viewLifecycleOwner)
            mDataList = taskList.toMutableList()
            binding.mainListView.apply {
                adapter = mAdapter
                setDragAndDropListener(onDrop)
            }
            binding.cancelButton.setOnClickListener { onCancelClicked() }
            mAdapter.notifyDataSetChanged()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            viewModel.editTask(newTask)
        }
        TasksWidgetProvider.notifyDatasetChanged(requireContext())
        findNavController().popBackStack()
    }

    /**
     * Callback method for "Cancel" button
     */
    private fun onCancelClicked() {
        findNavController().popBackStack()
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