package com.hkb48.keepdo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.hkb48.keepdo.SortableListView.DragAndDropListener


class TaskSortingActivity : AppCompatActivity() {
    private lateinit var mAdapter: TaskAdapter
    private lateinit var mDBAdapter: DatabaseAdapter
    private var mDataList: MutableList<Task> = mutableListOf()
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
        setContentView(R.layout.activity_task_sorting)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        assert(supportActionBar != null)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mDBAdapter = DatabaseAdapter.getInstance(this)
        mDataList = mDBAdapter.taskList
        mAdapter = TaskAdapter()
        val taskListView = findViewById<SortableListView>(R.id.mainListView)
        taskListView.adapter = mAdapter
        taskListView.setDragAndDropListener(onDrop)
        mAdapter.notifyDataSetChanged()
        findViewById<Button>(R.id.cancelButton).setOnClickListener { onCancelClicked() }
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
    private fun onSaveClicked() {
        for (index in mDataList.indices) {
            val task = mDataList[index]
            task.order = index.toLong()
            mDBAdapter.editTask(task)
        }
        finish()
    }

    /**
     * Callback method for "Cancel" button
     */
    private fun onCancelClicked() {
        finish()
    }

    private fun enableSaveButton() {
        val okButton = findViewById<Button>(R.id.okButton)
        okButton.isEnabled = true
        okButton.setOnClickListener { onSaveClicked() }
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
            val inflater = LayoutInflater.from(parent.context)
            var view = convertView
            if (view == null) {
                view = inflater.inflate(R.layout.task_sorting_list_item, parent, false)
            }
            val itemView = view as SortableListItem
            val task = getItem(position) as Task
            itemView.setText(task.name)
            return view
        }
    }
}