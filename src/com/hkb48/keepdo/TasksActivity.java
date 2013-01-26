package com.hkb48.keepdo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TasksActivity extends Activity {
    // Request code when launching sub-activity
    private final static int REQUEST_ADD_TASK = 0;
    private final static int REQUEST_EDIT_TASK = 1;
    private final static int REQUEST_SHOW_CALENDAR = 2;

    // ID of context menu items
    private final static int CONTEXT_MENU_EDIT = 0;
    private final static int CONTEXT_MENU_DELETE = 1;

    private final static int TASKID_FOR_LISTHEADER = -1;

    private TaskAdapter mAdapter;
    private List<Task> mDataList = new ArrayList<Task>();
    private CheckSoundPlayer mCheckSound = new CheckSoundPlayer(this);
    private DatabaseAdapter mDBAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDBAdapter = DatabaseAdapter.getInstance(this);

        // Cancel notification (if displayed)
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(R.string.app_name);

        ListView listView1 = (ListView)findViewById(R.id.listView1);
        mAdapter = new TaskAdapter();
        listView1.setAdapter(mAdapter);

        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                // Show calendar view
                Long taskId =  mDataList.get(position).getTaskID();
                Intent intent = new Intent(TasksActivity.this, CalendarActivity.class);
                intent.putExtra("TASK-ID", taskId);
                startActivityForResult(intent, REQUEST_SHOW_CALENDAR);
            }
        });

        registerForContextMenu(listView1);

        updateTaskList();
    }

    @Override
    public void onResume() {
    	mCheckSound.load();
        super.onResume();
    }

    @Override
    public void onPause() {
    	mCheckSound.unload();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mDBAdapter.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_add_task:
            Intent intent = new Intent(TasksActivity.this, TaskSettingActivity.class);
            startActivityForResult(intent, REQUEST_ADD_TASK);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Task task;
            switch(requestCode) {
            case REQUEST_ADD_TASK:
                task = (Task) data.getSerializableExtra("TASK-INFO");
                mDBAdapter.addTask(task);
                updateTaskList();
                updateReminder();
                break;
            case REQUEST_EDIT_TASK:
                task = (Task) data.getSerializableExtra("TASK-INFO");
                mDBAdapter.editTask(task);
                updateTaskList();
                updateReminder();
                break;
            case REQUEST_SHOW_CALENDAR:
                updateTaskList();
                break;
            default:
                break;
            }
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        AdapterContextMenuInfo adapterinfo = (AdapterContextMenuInfo)menuInfo;
        ListView listView = (ListView)view;
        Task task = (Task) listView.getItemAtPosition(adapterinfo.position);
        menu.setHeaderTitle(task.getName());
        menu.add(0, CONTEXT_MENU_EDIT, 0, R.string.edit_task);
        menu.add(0, CONTEXT_MENU_DELETE, 1, R.string.delete_task);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
        Task task = (Task) mAdapter.getItem(info.position);
        final long taskId = task.getTaskID();
        switch (item.getItemId()) {
        case CONTEXT_MENU_EDIT:
            Intent intent = new Intent(TasksActivity.this, TaskSettingActivity.class);
            intent.putExtra("TASK-INFO", task);
            startActivityForResult(intent, REQUEST_EDIT_TASK);
            return true;
        case CONTEXT_MENU_DELETE:
            new AlertDialog.Builder(this)
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton(R.string.dialog_ok ,new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mDBAdapter.deleteTask(taskId);
                    updateTaskList();
                    updateReminder();
                }
            })
            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            })
            .setCancelable(true)
            .create()
            .show();
            return true;
        default:
            Toast.makeText(this, "default", Toast.LENGTH_SHORT).show();
            return super.onContextItemSelected(item);
        }
    }

    /**
     * Update the task list view with latest DB information.
     */
    private void updateTaskList() {
        List<Task> taskList = mDBAdapter.getTaskList();
        List<Task> taskListToday =  new ArrayList<Task>();
        List<Task> taskListNotToday = new ArrayList<Task>();
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        mDataList.clear();
        for (Task task : taskList) {
            if (task.getRecurrence().isValidDay(dayOfWeek) ) {
                taskListToday.add(task);
            } else {
                taskListNotToday.add(task);
            }
        }

        if (taskListToday.size() > 0) {
            // Dummy Task for header on the ListView
            Task dummyTask = new Task(getString(R.string.tasklist_header_today_task), null);
            dummyTask.setTaskID(TASKID_FOR_LISTHEADER);
            mDataList.add(dummyTask);

            for (Task task : taskListToday) {
                mDataList.add(task);
            }
        }
        if (taskListNotToday.size() > 0) {
            // Dummy Task for header on the ListView
            Task dummyTask = new Task(getString(R.string.tasklist_header_other_task), null);
            dummyTask.setTaskID(TASKID_FOR_LISTHEADER);
            mDataList.add(dummyTask);

            for (Task task : taskListNotToday) {
                mDataList.add(task);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void updateReminder() {
        ReminderManager.getInstance().setNextAlert(this);
    }

    private class TaskAdapter extends BaseAdapter {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        private ViewHolder viewHolder;

        public int getCount() {
            return mDataList.size();
        }

        public Object getItem(int position) {
            return mDataList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getItemViewType(int position) {
            Task task = (Task) getItem(position);
            if (task.getTaskID() == TASKID_FOR_LISTHEADER) {
                return TYPE_HEADER;
            } else {
                return TYPE_ITEM;
            }
        }

        public boolean isEnabled(int position) {
            return (getItemViewType(position) == TYPE_ITEM);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = convertView;
            Task task = (Task) getItem(position);
            boolean isTask = isEnabled(position);
            boolean createView = false;

            // Check if it's necessary to create view or re-use
            if (view == null ) {
                createView = true;
            } else {
                viewHolder = (ViewHolder) view.getTag();
                if (viewHolder.viewType != getItemViewType(position)) {
                    createView = true;
                }
            }

            if (createView) {
                viewHolder = new ViewHolder();
                if (isTask) {
                    view = inflater.inflate(R.layout.task_list_row, null);
                    viewHolder.viewType = TYPE_ITEM;
                    viewHolder.imageView = (ImageView) view.findViewById(R.id.taskListItemCheck);
                    viewHolder.textView = (TextView) view.findViewById(R.id.taskName);
                    viewHolder.recurrenceView = (RecurrenceView) view.findViewById(R.id.recurrenceView);
                    view.setTag(viewHolder);
                } else {
                    view = inflater.inflate(R.layout.task_list_header, null);
                    viewHolder.viewType = TYPE_HEADER;
                    viewHolder.textView = (TextView) view.findViewById(R.id.listHeader);
                    view.setTag(viewHolder);
                }
            }

            if (isTask) {
                TextView textView = viewHolder.textView;
                String taskName = task.getName();
                textView.setText(taskName);

                RecurrenceView recurrenceView = viewHolder.recurrenceView;
                recurrenceView.setTextSize(12.0f);
                recurrenceView.update(task.getRecurrence());

                ImageView imageView = viewHolder.imageView;
                boolean checked = mDBAdapter.getDoneStatus(task.getTaskID(), new Date());
                if (checked) {
                    imageView.setImageResource(R.drawable.ic_done);
                } else {
                    imageView.setImageResource(R.drawable.ic_not_done);
                }
                imageView.setTag(Integer.valueOf(position));
                imageView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ImageView imageView = (ImageView) v;
                        int position = (Integer) v.getTag();
                        Task task = (Task) getItem(position);
                        long taskId = task.getTaskID();
                        boolean checked = mDBAdapter.getDoneStatus(taskId, new Date());
                        checked = ! checked;
                        mDBAdapter.setDoneStatus(taskId, new Date(), checked);
                        updateReminder();
                        if (checked) {
                            imageView.setImageResource(R.drawable.ic_done);
                            mCheckSound.play();
                        } else {
                            imageView.setImageResource(R.drawable.ic_not_done);
                        }
                    }
                });
            } else {
                TextView listHeader = viewHolder.textView;
                listHeader.setText(task.getName());
            }

            return view;
        }

        private class ViewHolder {
            int viewType;
            TextView textView;
            ImageView imageView;
            RecurrenceView recurrenceView;
        }
    }
}
