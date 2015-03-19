package com.hkb48.keepdo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.hkb48.keepdo.widget.TasksWidgetProvider;

public class TasksActivity extends ActionBarActivity implements
        DateChangeTimeManager.OnDateChangedListener {
    // Request code when launching sub-activity
    private static final int REQUEST_ADD_TASK = 0;
    private static final int REQUEST_EDIT_TASK = 1;
    private static final int REQUEST_SHOW_CALENDAR = 2;
    private static final int REQUEST_SORT_TASK = 3;

    // ID of context menu items
    private static final int CONTEXT_MENU_EDIT = 0;
    private static final int CONTEXT_MENU_DELETE = 1;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private TaskAdapter mAdapter;
    private final List<TaskListItem> mDataList = new ArrayList<TaskListItem>();
    private final CheckSoundPlayer mCheckSound = new CheckSoundPlayer(this);
    private DatabaseAdapter mDBAdapter = null;

    private final Settings.OnChangedListener mSettingsChangedListener = new Settings.OnChangedListener() {
        public void onDoneIconSettingChanged() {
            updateTaskList();
        }

        public void onDateChangeTimeSettingChanged() {
            updateTaskList();
        }

        public void onWeekStartDaySettingChanged() {
            updateTaskList();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setLogo(R.drawable.ic_launcher);

        mDBAdapter = DatabaseAdapter.getInstance(this);

        Settings.initialize(getApplicationContext());
        Settings.registerOnChangedListener(mSettingsChangedListener);

        DateChangeTimeManager.getInstance(this).registerOnDateChangedListener(
                this);

        // Cancel notification (if displayed)
        NotificationController.cancelReminder(this);

        ListView taskListView = (ListView) findViewById(R.id.mainListView);
        mAdapter = new TaskAdapter();
        taskListView.setAdapter(mAdapter);

        taskListView.setEmptyView(findViewById(R.id.empty));

        taskListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        // Show calendar view
                        TaskListItem item = mDataList
                                .get(position);
                        if (item.type == TYPE_ITEM) {
                            Task task = (Task) item.data;
                            Long taskId = task.getTaskID();
                            Intent intent = new Intent(TasksActivity.this,
                                    TaskActivity.class);
                            intent.putExtra("TASK-ID", taskId);
                            startActivityForResult(intent,
                                    REQUEST_SHOW_CALENDAR);
                        }
                    }
                });

        registerForContextMenu(taskListView);

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
        DateChangeTimeManager.getInstance(this)
                .unregisterOnDateChangedListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
        case R.id.menu_add_task:
            intent = new Intent(TasksActivity.this, TaskSettingActivity.class);
            startActivityForResult(intent, REQUEST_ADD_TASK);
            return true;
        case R.id.menu_settings:
            intent = new Intent(TasksActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        case R.id.menu_sort_task:
            intent = new Intent(TasksActivity.this, TaskSortingActivity.class);
            startActivityForResult(intent, REQUEST_SORT_TASK);
            return true;
        case R.id.menu_backup_restore:
            showBackupRestoreDialog();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Context context = getApplicationContext();
        if (resultCode == RESULT_OK) {
            Task task;
            switch (requestCode) {
            case REQUEST_ADD_TASK:
                task = (Task) data.getSerializableExtra("TASK-INFO");
                assert task != null;
                task.setOrder(mDBAdapter.getMaxSortOrderId() + 1);
                mDBAdapter.addTask(task);
                updateTaskList();
                updateReminder();
                TasksWidgetProvider.notifyDatasetChanged(context);
                break;
            case REQUEST_EDIT_TASK:
                task = (Task) data.getSerializableExtra("TASK-INFO");
                mDBAdapter.editTask(task);
                updateTaskList();
                updateReminder();
                TasksWidgetProvider.notifyDatasetChanged(context);
                break;
            case REQUEST_SHOW_CALENDAR:
            case REQUEST_SORT_TASK:
                updateTaskList();
                break;
            default:
                break;
            }
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View view,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        AdapterContextMenuInfo adapterinfo = (AdapterContextMenuInfo) menuInfo;
        ListView listView = (ListView) view;
        TaskListItem taskListItem = (TaskListItem) listView
                .getItemAtPosition(adapterinfo.position);
        Task task = (Task) taskListItem.data;
        menu.setHeaderTitle(task.getName());
        menu.add(0, CONTEXT_MENU_EDIT, 0, R.string.edit_task);
        menu.add(0, CONTEXT_MENU_DELETE, 1, R.string.delete_task);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();
        assert info != null;
        TaskListItem taskListItem = (TaskListItem) mAdapter
                .getItem(info.position);
        Task task = (Task) taskListItem.data;
        final long taskId = task.getTaskID();
        switch (item.getItemId()) {
        case CONTEXT_MENU_EDIT:
            Intent intent = new Intent(TasksActivity.this,
                    TaskSettingActivity.class);
            intent.putExtra("TASK-INFO", task);
            startActivityForResult(intent, REQUEST_EDIT_TASK);
            return true;
        case CONTEXT_MENU_DELETE:
            new AlertDialog.Builder(this)
                    .setMessage(R.string.delete_confirmation)
                    .setPositiveButton(R.string.dialog_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    mDBAdapter.deleteTask(taskId);
                                    updateTaskList();
                                    updateReminder();
                                    TasksWidgetProvider.notifyDatasetChanged(getApplicationContext());
                                }
                            })
                    .setNegativeButton(R.string.dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                }
                            }).setCancelable(true).create().show();
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

        List<Task> taskListToday = new ArrayList<Task>();
        List<Task> taskListNotToday = new ArrayList<Task>();
        int dayOfWeek = DateChangeTimeUtil.getDateTimeCalendar().get(
                Calendar.DAY_OF_WEEK);

        mDataList.clear();
        for (Task task : taskList) {
            if (task.getRecurrence().isValidDay(dayOfWeek)) {
                taskListToday.add(task);
            } else {
                taskListNotToday.add(task);
            }
        }

        if (taskListToday.size() > 0) {
            // Dummy Task for header on the ListView
            TaskListHeader header = new TaskListHeader();
            header.title = getString(R.string.tasklist_header_today_task);
            TaskListItem taskListItem = new TaskListItem(TYPE_HEADER, header);
            mDataList.add(taskListItem);

            for (Task task : taskListToday) {
                taskListItem = new TaskListItem(TYPE_ITEM, task);
                mDataList.add(taskListItem);
            }
        }
        if (taskListNotToday.size() > 0) {
            // Dummy Task for header on the ListView
            TaskListHeader header = new TaskListHeader();
            header.title = getString(R.string.tasklist_header_other_task);
            TaskListItem taskListItem = new TaskListItem(TYPE_HEADER, header);
            mDataList.add(taskListItem);

            for (Task task : taskListNotToday) {
                taskListItem = new TaskListItem(TYPE_ITEM, task);
                mDataList.add(taskListItem);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void updateReminder() {
        ReminderManager.getInstance().setNextAlert(this);
    }

    /**
     * Backup & Restore
     */
    private void showBackupRestoreDialog() {
        final String fineName = mDBAdapter.backupFileName();
        final String dirName = mDBAdapter.backupDirName();
        final String dirPath = mDBAdapter.backupDirPath();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
                TasksActivity.this);
        String title = getString(R.string.backup_restore) + "\n" + dirName
                + fineName;
        dialogBuilder.setTitle(title);
        dialogBuilder.setSingleChoiceItems(
                R.array.dialog_choice_backup_restore, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        boolean enabled = true;
                        if (which == 1) {
                            // Restore
                            File backupFile = new File(dirPath + fineName);
                            if (!backupFile.exists()) {
                                enabled = false;
                                Toast.makeText(TasksActivity.this,
                                        R.string.no_backup_file,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        ((AlertDialog) dialog).getButton(
                                AlertDialog.BUTTON_POSITIVE)
                                .setEnabled(enabled);
                    }
                });
        dialogBuilder.setNegativeButton(R.string.dialog_cancel, null);
        dialogBuilder.setPositiveButton(R.string.dialog_start,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (((AlertDialog) dialog).getListView()
                                .getCheckedItemPosition()) {
                        case 0:
                            // execute backup
//                            backupTaskData();
//                            Toast.makeText(TasksActivity.this,
//                                    R.string.backup_done, Toast.LENGTH_SHORT)
//                                    .show();
                            // backup to Google drive
                            Intent intent = new Intent(TasksActivity.this, BackupFileInGoogleDrive.class);
//                            Intent intent = new Intent(TasksActivity.this, GooglePlayServicesActivity.class);
                            startActivity(intent);
                            break;
                        case 1:
                            // execute restore
                            restoreTaskData();
                            updateTaskList();
                            Toast.makeText(TasksActivity.this,
                                    R.string.restore_done, Toast.LENGTH_SHORT)
                                    .show();
                            break;
                        default:
                            break;
                        }
                    }
                });
        dialogBuilder.setCancelable(true);
        final AlertDialog alertDialog = dialogBuilder.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            public void onShow(DialogInterface dialog) {
                File backupFile = new File(dirPath + fineName);
                boolean existBackupFile = backupFile.exists();
                ((AlertDialog) dialog).getListView().getChildAt(1)
                        .setEnabled(existBackupFile);
            }
        });
    }

    private void backupTaskData() {
        mDBAdapter.backupDataBase();
    }

    private void restoreTaskData() {
        mDBAdapter.restoreDataBase();
    }

    private static class TaskListItem {
        final int type;
        final Object data;

        public TaskListItem(int type, Object data) {
            this.type = type;
            this.data = data;
        }
    }

    private static class TaskListHeader {
        String title;
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

        public int getItemViewType(int position) {
            TaskListItem item = (TaskListItem) getItem(position);
            return item.type;
        }

        public boolean isEnabled(int position) {
            return (getItemViewType(position) == TYPE_ITEM);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = convertView;
            TaskListItem taskListItem = (TaskListItem) getItem(position);
            boolean isTask = isEnabled(position);
            boolean createView = false;
            HeaderViewHolder headerViewHolder = null;
            ItemViewHolder itemViewHolder = null;

            // Check if it's necessary to create view or re-use
            if (view == null) {
                createView = true;
            } else {
                ViewHolder viewHolder = (ViewHolder) view.getTag();
                if (viewHolder.viewType != getItemViewType(position)) {
                    createView = true;
                } else {
                    if (isTask) {
                        itemViewHolder = (ItemViewHolder) viewHolder;
                    } else {
                        headerViewHolder = (HeaderViewHolder) viewHolder;
                    }
                }
            }

            if (createView) {
                if (isTask) {
                    itemViewHolder = new ItemViewHolder();
                    view = inflater.inflate(R.layout.task_list_row, parent, false);
                    itemViewHolder.viewType = TYPE_ITEM;
                    itemViewHolder.imageView = (ImageView) view
                            .findViewById(R.id.taskListItemCheck);
                    itemViewHolder.textView1 = (TextView) view
                            .findViewById(R.id.taskName);
                    itemViewHolder.textView2 = (TextView) view
                            .findViewById(R.id.taskContext);
                    itemViewHolder.recurrenceView = (RecurrenceView) view
                            .findViewById(R.id.recurrenceView);
                    itemViewHolder.lastDoneDateTextView = (TextView) view
                            .findViewById(R.id.taskLastDoneDate);
                    view.setTag(itemViewHolder);
                } else {
                    headerViewHolder = new HeaderViewHolder();
                    view = inflater.inflate(R.layout.task_list_header, parent, false);
                    headerViewHolder.viewType = TYPE_HEADER;
                    headerViewHolder.textView1 = (TextView) view
                            .findViewById(R.id.textView1);
                    view.setTag(headerViewHolder);
                }
            }

            if (isTask) {
                Task task = (Task) taskListItem.data;
                long taskId = task.getTaskID();

                TextView textView1 = itemViewHolder.textView1;
                String taskName = task.getName();
                textView1.setText(taskName);

                TextView textView2 = itemViewHolder.textView2;
                String taskContext = task.getContext();
                if ((taskContext != null) && (!taskContext.isEmpty())) {
                    textView2.setText(taskContext);
                    textView2.setVisibility(View.VISIBLE);
                } else {
                    textView2.setVisibility(View.GONE);
                }

                RecurrenceView recurrenceView = itemViewHolder.recurrenceView;
                recurrenceView.update(task.getRecurrence());

                ImageView imageView = itemViewHolder.imageView;
                Date today = DateChangeTimeUtil.getDateTime();
                boolean checked = mDBAdapter.getDoneStatus(taskId, today);

                updateView(taskId, checked, itemViewHolder);

                imageView.setTag(position);
                imageView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        View parent = (View) v.getParent();
                        ItemViewHolder itemViewHolder = new ItemViewHolder();
                        itemViewHolder.imageView = (ImageView) v;
                        itemViewHolder.lastDoneDateTextView = (TextView) parent
                                .findViewById(R.id.taskLastDoneDate);
                        int position = (Integer) v.getTag();
                        TaskListItem taskListItem = (TaskListItem) getItem(position);
                        Task task = (Task) taskListItem.data;
                        long taskId = task.getTaskID();
                        Date today = DateChangeTimeUtil.getDateTime();
                        boolean checked = mDBAdapter.getDoneStatus(taskId,
                                today);
                        checked = !checked;
                        mDBAdapter.setDoneStatus(taskId, today, checked);
                        updateView(taskId, checked, itemViewHolder);
                        updateReminder();
                        TasksWidgetProvider.notifyDatasetChanged(getApplicationContext());

                        if (checked) {
                            mCheckSound.play();
                        }
                    }
                });
            } else {
                TaskListHeader taskListHeader = (TaskListHeader) taskListItem.data;
                headerViewHolder.textView1.setText(taskListHeader.title);
            }

            return view;
        }

        private void updateView(long taskId, boolean checked,
                ItemViewHolder holder) {
            Date today = DateChangeTimeUtil.getDateTime();
            ImageView imageView = holder.imageView;
            TextView lastDoneDateTextView = holder.lastDoneDateTextView;
            final int colorLastDoneDate = getResources().getColor(
                    R.color.tasklist_last_donedate);
            lastDoneDateTextView.setTextColor(colorLastDoneDate);

            if (checked) {
                imageView.setImageResource(Settings.getDoneIconId());
                ComboCount comboCount = mDBAdapter.getComboCount(taskId);
                if (comboCount.currentCount > 1) {
                    lastDoneDateTextView.setTextColor(getResources().getColor(
                            R.color.tasklist_combo));
                    lastDoneDateTextView.setText(getString(
                            R.string.tasklist_combo, comboCount.currentCount));
                } else {
                    lastDoneDateTextView
                            .setText(R.string.tasklist_lastdonedate_today);
                }
            } else {
                imageView.setImageResource(Settings.getNotDoneIconId());
                Date lastDoneDate = mDBAdapter.getLastDoneDate(taskId);
                if (lastDoneDate != null) {
                    ComboCount comboCount = mDBAdapter.getComboCount(taskId);
                    if (comboCount.currentCount > 1) {
                        lastDoneDateTextView.setTextColor(getResources()
                                .getColor(R.color.tasklist_combo));
                        lastDoneDateTextView.setText(getString(
                                R.string.tasklist_combo,
                                comboCount.currentCount));
                    } else {
                        int diffDays = (int) ((today.getTime() - lastDoneDate
                                .getTime()) / (long) (1000 * 60 * 60 * 24));
                        if (diffDays == 1) {
                            lastDoneDateTextView
                                    .setText(getString(R.string.tasklist_lastdonedate_yesterday));
                        } else {
                            lastDoneDateTextView.setText(getString(
                                    R.string.tasklist_lastdonedate_diffdays,
                                    diffDays));
                        }
                    }
                } else {
                    lastDoneDateTextView
                            .setText(R.string.tasklist_lastdonedate_notyet);
                }
            }
        }

        private class ViewHolder {
            int viewType;
        }

        private class HeaderViewHolder extends ViewHolder {
            TextView textView1;
        }

        private class ItemViewHolder extends ViewHolder {
            TextView textView1;
            TextView textView2;
            ImageView imageView;
            RecurrenceView recurrenceView;
            TextView lastDoneDateTextView;
        }
    }

    public void onDateChanged() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.date_changed)
                .setPositiveButton(R.string.dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                updateTaskList();
                            }
                        }).setCancelable(false).create().show();
    }
}