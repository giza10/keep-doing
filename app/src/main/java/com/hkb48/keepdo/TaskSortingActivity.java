package com.hkb48.keepdo;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class TaskSortingActivity extends AppCompatActivity {
    private TaskAdapter mAdapter;
    private DatabaseAdapter mDBAdapter = null;
    private List<Task> mDataList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_sorting);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDBAdapter = DatabaseAdapter.getInstance(this);
        mDataList = mDBAdapter.getTaskList();

        mAdapter = new TaskAdapter();
        SortableListView taskListView = findViewById(R.id.mainListView);
        taskListView.setAdapter(mAdapter);
        taskListView.setDragAndDropListener(onDrop);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Callback method for "Save" button
     */
    public void onSaveClicked(View view) {
        for (int index = 0; index < mDataList.size(); index++) {
            Task task = mDataList.get(index);
            task.setOrder(index);
            mDBAdapter.editTask(task);
        }
        finish();
    }

    /**
     * Callback method for "Cancel" button
     */
    public void onCancelClicked(View view) {
        finish();
    }

    private class TaskAdapter extends BaseAdapter {

        public int getCount() {
            return mDataList.size();
        }

        public Object getItem(int position) {
            return mDataList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.task_sorting_list_item, parent, false);
            }

            SortableListItem itemView = (SortableListItem) view;
            Task task = (Task) getItem(position);
            itemView.setText(task.getName());

            return view;
        }
    }

    private final SortableListView.DragAndDropListener onDrop = new SortableListView.DragAndDropListener() {
        public void onDrag(int from, int to) {
            if (from != to) {
                Task item = (Task) mAdapter.getItem(from);
                mDataList.remove(from);
                mDataList.add(to, item);
                mAdapter.notifyDataSetChanged();
            }
        }

        public void onDrop(int from, int to) {
            if (from != to) {
                enableSaveButton();
            }
        }
    };

    private void enableSaveButton() {
        Button okButton = findViewById(R.id.okButton);
        okButton.setEnabled(true);
    }
}
