package com.hkb48.keepdo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TaskSortingActivity extends Activity {
    private TaskAdapter mAdapter;
    private DatabaseAdapter mDBAdapter = null;
    private List<Task> mDataList = new ArrayList<Task>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_sorting_activity);

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mDBAdapter = DatabaseAdapter.getInstance(this);
        mDataList = mDBAdapter.getTaskList();

        mAdapter = new TaskAdapter();
        ListView taskListView = (ListView) findViewById(R.id.mainListView);
        taskListView.setAdapter(mAdapter);
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
    public void onSaveClicked() {
        for (int index=0; index<mDataList.size(); index++) {
            Task task = mDataList.get(index);
            task.setOrder(index);
            mDBAdapter.editTask(task);
        }
        Intent data = new Intent();
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * Callback method for "Cancel" button
     */
    public void onCancelClicked() {
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
                view = inflater.inflate(R.layout.task_sorting_list_item, null);

                ImageView upArrowView = ((ImageView) view.findViewById(R.id.imageView1));
                upArrowView.setTag(position);
                upArrowView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        int position = (Integer) v.getTag();
                        if (position > 0) {
                            Collections.swap(mDataList, position, position-1);
                            mAdapter.notifyDataSetChanged();
                            enableSaveButton();
                        }
                    }
                });

                ImageView downArrowView = ((ImageView) view.findViewById(R.id.imageView2));
                downArrowView.setTag(position);
                downArrowView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        int position = (Integer) v.getTag();
                        if (position < getCount() - 1) {
                            Collections.swap(mDataList, position, position+1);
                            mAdapter.notifyDataSetChanged();
                            enableSaveButton();
                        }
                    }
                });
            }

            TextView taskName = (TextView) view.findViewById(R.id.textView1);
            Task task = (Task) getItem(position);
            taskName.setText(task.getName());

            return view;
        }

        private void enableSaveButton() {
            Button okButton = (Button) findViewById(R.id.okButton);
            okButton.setEnabled(true);
        }
    }
}
