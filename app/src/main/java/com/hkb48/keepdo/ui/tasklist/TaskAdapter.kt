package com.hkb48.keepdo.ui.tasklist

import android.content.Context
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hkb48.keepdo.R
import com.hkb48.keepdo.Recurrence
import com.hkb48.keepdo.databinding.TaskListItemBinding
import com.hkb48.keepdo.databinding.TaskListSectionBinding
import com.hkb48.keepdo.db.entity.TaskWithDoneHistory
import com.hkb48.keepdo.util.DateChangeTimeUtil
import java.util.*
import kotlin.collections.ArrayList


private const val ITEM_VIEW_TYPE_SECTION = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class TaskAdapter(
    private val clickListener: ClickListener
) : ListAdapter<DataItem, RecyclerView.ViewHolder>(
    callbacks
) {
    private var listItems: MutableList<DataItem> = ArrayList()
    private var longPressedPosition = 0

    fun getLongPressedPosition(): Int {
        return longPressedPosition
    }

    private fun setLongPressedPosition(position: Int) {
        this.longPressedPosition = position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_SECTION -> SectionViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SectionViewHolder -> {
                val section = getItem(position) as DataItem.Section
                holder.bind(section.text)
            }
            is ViewHolder -> {
                val item = getItem(position) as DataItem.TaskDataItem
                holder.bind(item.taskWithDoneHistory, clickListener)
                holder.itemView.setOnLongClickListener {
                    setLongPressedPosition(holder.bindingAdapterPosition)
                    false
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Section -> ITEM_VIEW_TYPE_SECTION
            is DataItem.TaskDataItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        holder.itemView.setOnLongClickListener(null)
        super.onViewRecycled(holder)
    }

    fun addHeaderAndSubmitList(context: Context, taskList: List<TaskWithDoneHistory>) {
        val newListItems: MutableList<DataItem> = ArrayList()
        val taskListToday: MutableList<TaskWithDoneHistory> = ArrayList()
        val taskListNotToday: MutableList<TaskWithDoneHistory> = ArrayList()
        val dayOfWeek = DateChangeTimeUtil.dateTimeCalendar[Calendar.DAY_OF_WEEK]
        for (item in taskList) {
            if (Recurrence.getFromTask(item.task).isValidDay(dayOfWeek)) {
                taskListToday.add(item)
            } else {
                taskListNotToday.add(item)
            }
        }
        if (taskListToday.size > 0) {
            val section = DataItem.Section(context.getString(R.string.tasklist_header_today_task))
            newListItems += listOf(section) + taskListToday.map { DataItem.TaskDataItem(it) }
        }
        if (taskListNotToday.size > 0) {
            val section = DataItem.Section(context.getString(R.string.tasklist_header_other_task))
            newListItems += listOf(section) + taskListNotToday.map { DataItem.TaskDataItem(it) }
        }
        submitList(newListItems)
        listItems = newListItems
    }

    fun getItemAt(position: Int): TaskWithDoneHistory {
        val item = getItem(position) as DataItem.TaskDataItem
        return item.taskWithDoneHistory
    }

    class SectionViewHolder(
        private val binding: TaskListSectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.title = title
        }

        companion object {
            fun from(parent: ViewGroup): SectionViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TaskListSectionBinding.inflate(layoutInflater, parent, false)
                return SectionViewHolder(binding)
            }
        }
    }

    class ViewHolder(
        private val binding: TaskListItemBinding
    ) : RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {
        fun bind(taskWithDoneHistory: TaskWithDoneHistory, clickListener: ClickListener) {
            binding.taskWithDoneHistory = taskWithDoneHistory
            binding.clickListener = clickListener
            binding.root.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu,
            view: View,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu.add(0, ContextMenuConstants.CONTEXT_MENU_EDIT, 0, R.string.edit_task)
            menu.add(0, ContextMenuConstants.CONTEXT_MENU_DELETE, 1, R.string.delete_task)
        }


        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TaskListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    interface ClickListener {
        fun onItemClick(taskWithDoneHistory: TaskWithDoneHistory)
        fun onDoneClick(taskWithDoneHistory: TaskWithDoneHistory)
    }

    companion object {
        private val callbacks = object : DiffUtil.ItemCallback<DataItem>() {
            override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}

sealed class DataItem {
    data class TaskDataItem(val taskWithDoneHistory: TaskWithDoneHistory) : DataItem() {
        override val id: Long = taskWithDoneHistory.task._id.toLong()
    }

    data class Section(val text: String) : DataItem() {
        override val id = Long.MIN_VALUE
    }

    abstract val id: Long
}
