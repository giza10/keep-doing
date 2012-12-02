package com.hkb48.keepdo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class TasksActivity extends MainActivity {
    // Request code when launching sub-activity
    private final static int REQUEST_ADD = 0;
    private final static int REQUEST_EDIT = 1;

    // ID of context menu items
    private final static int CONTEXT_MENU_EDIT = 0;
    private final static int CONTEXT_MENU_DELETE = 1;

    private TaskAdapter adapter;
    private List<Task> dataList = new ArrayList<Task>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView1 = (ListView)findViewById(R.id.listView1);
        adapter = new TaskAdapter();
        listView1.setAdapter(adapter);

        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                // Show calendar view
                Long taskId =  getTaskList().get(position).getTaskID();
                Intent intent = new Intent(TasksActivity.this, CalendarActivity.class);
                intent.putExtra("TASK-ID", taskId);
                startActivity(intent);
            }
        });

        listView1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getSelectedItem();
                Log.v("tag", String.format("onItemSelected: %s", item));
            }

            public void onNothingSelected(AdapterView<?> parent) {
                Log.v("tag", "onNothingSelected");
            }
        });

        registerForContextMenu(listView1);

        updateTaskList();
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
            startActivityForResult(intent, REQUEST_ADD);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Task task = (Task) data.getSerializableExtra("TASK-INFO");
            if (requestCode == 0) {
                addTask(task.getName(), task.getRecurrence());
                updateTaskList();
            } else {
                editTask(task.getTaskID(), task.getName(), task.getRecurrence());
                updateTaskList();
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
        Task task = (Task) adapter.getItem(info.position);
        switch (item.getItemId()) {
        case CONTEXT_MENU_EDIT:
            Intent intent = new Intent(TasksActivity.this, TaskSettingActivity.class);
            intent.putExtra("TASK-INFO", task);
            startActivityForResult(intent, REQUEST_EDIT);
            return true;
        case CONTEXT_MENU_DELETE:
            final Long taskId = task.getTaskID();
            new AlertDialog.Builder(this)
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton(R.string.dialog_ok ,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                    deleteTask(taskId);
                    adapter.notifyDataSetChanged();
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
        dataList = getTaskList();
        adapter.notifyDataSetChanged();
    }

    private class TaskAdapter extends BaseAdapter {

		public int getCount() {
			return dataList.size();
		}

		public Object getItem(int position) {
			return dataList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.task_list_row, null);
            }

            Task task = (Task) getItem(position);
            if (task != null) {
                TextView textView1 = (TextView) view.findViewById(R.id.taskName);
                textView1.setText(task.getName());
            }

            ImageView imageView = (ImageView) view.findViewById(R.id.taskListItemCheck);
            boolean checked = task.ifChecked();
            if (checked) {
                imageView.setImageResource(R.drawable.done_mark);
            } else {
                imageView.setImageResource(R.drawable.not_done);
            }
            imageView.setTag(Integer.valueOf(position));
            imageView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ImageView imageView = (ImageView) v;
                    int position = (Integer) v.getTag();
                    Task task = (Task) getItem(position);
                    boolean checked = task.ifChecked();
                    checked = ! checked;
                    task.setChecked(checked);
                    setDoneStatus(task.getTaskID(), new Date(), checked);
                    if (checked) {
                        imageView.setImageResource(R.drawable.done_mark);
                    } else {
                        imageView.setImageResource(R.drawable.not_done);
                    }
                }
            });

            return view;
		}
    }
}
