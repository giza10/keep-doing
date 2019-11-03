package com.hkb48.keepdo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
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

import com.hkb48.keepdo.com.hkb48.keepdo.util.CompatUtil;
import com.hkb48.keepdo.com.hkb48.keepdo.util.DateComparator;
import com.hkb48.keepdo.widget.TasksWidgetProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TasksActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, DateChangeTimeManager.OnDateChangedListener {

    // ID of context menu items
    private static final int CONTEXT_MENU_EDIT = 0;
    private static final int CONTEXT_MENU_DELETE = 1;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private static final int BACKUP_PERMISSION_REQUEST_CODE = 100;
    private static final int RESTORE_PERMISSION_REQUEST_CODE = 101;

    // Delay to launch nav drawer item, to allow close animation to play
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;
    private final List<TaskListItem> mDataList = new ArrayList<>();
    private final CheckSoundPlayer mCheckSound = new CheckSoundPlayer(this);
    private TaskAdapter mAdapter;
    private ContentObserver mContentObserver;
    private boolean mContentsUpdated;
    private DatabaseAdapter mDBAdapter = null;
    private final Settings.OnChangedListener mSettingsChangedListener = new Settings.OnChangedListener() {
        public void onDoneIconSettingChanged() {
            mContentsUpdated = true;
        }

        public void onDateChangeTimeSettingChanged() {
            mContentsUpdated = true;
        }

        public void onWeekStartDaySettingChanged() {
            mContentsUpdated = true;
        }
    };
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        mDrawerLayout = findViewById(R.id.main_drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        NavigationView navigationView = findViewById(R.id.main_drawer_view);
        navigationView.setNavigationItemSelectedListener(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TasksActivity.this, TaskSettingActivity.class));
            }
        });

        mDBAdapter = DatabaseAdapter.getInstance(this);

        Settings.initialize(getApplicationContext());
        Settings.registerOnChangedListener(mSettingsChangedListener);

        DateChangeTimeManager.getInstance(this).registerOnDateChangedListener(
                this);

        // Cancel notification (if displayed)
        NotificationController.cancelReminder(this);

        ListView taskListView = findViewById(R.id.mainListView);
        mAdapter = new TaskAdapter();
        taskListView.setAdapter(mAdapter);

        taskListView.setEmptyView(findViewById(R.id.empty));

        taskListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        // Show calendar view
                        TaskListItem item = mDataList.get(position);
                        if (item.type == TYPE_ITEM) {
                            Task task = (Task) item.data;
                            Long taskId = task.getTaskID();
                            Intent intent = new Intent(TasksActivity.this,
                                    TaskActivity.class);
                            intent.putExtra("TASK-ID", taskId);
                            startActivity(intent);
                        }
                    }
                });

        mContentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                mContentsUpdated = true;
            }
        };
        getContentResolver().registerContentObserver(KeepdoProvider.BASE_CONTENT_URI, true, mContentObserver);

        NotificationController.initNotificationChannel(getApplicationContext());

        registerForContextMenu(taskListView);
        updateTaskList();
    }

    @Override
    public void onResume() {
        if (mContentsUpdated) {
            updateTaskList();
        }
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
        getContentResolver().unregisterContentObserver(mContentObserver);
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
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

    @Override
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
                intent.putExtra(TaskSettingActivity.EXTRA_TASK_INFO, task);
                startActivity(intent);
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

        List<Task> taskListToday = new ArrayList<>();
        List<Task> taskListNotToday = new ArrayList<>();
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
            TaskListItem taskListItem = new TaskListItem(TYPE_HEADER, header, null, null);
            mDataList.add(taskListItem);

            for (Task task : taskListToday) {
                Date lastDoneDate = mDBAdapter.getLastDoneDate(task.getTaskID());
                ComboCount comboCount = mDBAdapter.getComboCount(task.getTaskID());
                taskListItem = new TaskListItem(TYPE_ITEM, task, lastDoneDate, comboCount);
                mDataList.add(taskListItem);
            }
        }
        if (taskListNotToday.size() > 0) {
            // Dummy Task for header on the ListView
            TaskListHeader header = new TaskListHeader();
            header.title = getString(R.string.tasklist_header_other_task);
            TaskListItem taskListItem = new TaskListItem(TYPE_HEADER, header, null, null);
            mDataList.add(taskListItem);

            for (Task task : taskListNotToday) {
                Date lastDoneDate = mDBAdapter.getLastDoneDate(task.getTaskID());
                ComboCount comboCount = mDBAdapter.getComboCount(task.getTaskID());
                taskListItem = new TaskListItem(TYPE_ITEM, task, lastDoneDate, comboCount);
                mDataList.add(taskListItem);
            }
        }
        mAdapter.notifyDataSetChanged();
        mContentsUpdated = false;
    }

    private void updateReminder() {
        ReminderManager.getInstance().setNextAlert(this);
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goToNavDrawerItem(itemId);
            }
        }, NAVDRAWER_LAUNCH_DELAY);
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        return false;
    }

    private void goToNavDrawerItem(final int itemId) {
        Intent intent;

        switch (itemId) {
            case R.id.drawer_item_1:
                intent = new Intent(this, TaskSortingActivity.class);
                startActivity(intent);
                break;
            case R.id.drawer_item_2:
                // Todo: Tentative implementation
                showBackupRestoreDeviceDialog();
                break;
            case R.id.drawer_item_3:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * Backup & Restore
     */
    private void showBackupRestoreDeviceDialog() {
        final Context context = this;
        final DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(context);
        final String fineName = dbAdapter.backupFileName();
        final String dirName = dbAdapter.backupDirName();
        final String dirPath = dbAdapter.backupDirPath();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
                context);
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
                                Toast.makeText(context,
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
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                    backupTaskData(context);
                                } else {
                                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, BACKUP_PERMISSION_REQUEST_CODE);
                                }

                                break;
                            case 1:
                                // execute restore
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                    restoreTaskData(context);
                                } else {
                                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RESTORE_PERMISSION_REQUEST_CODE);
                                }
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

    private void backupTaskData(final Context context) {
        DatabaseAdapter.getInstance(this).backupDataBase();
        Toast.makeText(context,
                R.string.backup_done, Toast.LENGTH_SHORT)
                .show();
    }

    private void restoreTaskData(final Context context) {
        DatabaseAdapter.getInstance(this).restoreDatabase();
        Toast.makeText(context,
                R.string.restore_done, Toast.LENGTH_SHORT)
                .show();
        updateTaskList();
        updateReminder();
        TasksWidgetProvider.notifyDatasetChanged(getApplicationContext());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == BACKUP_PERMISSION_REQUEST_CODE) {
                backupTaskData(getApplicationContext());
            } else if (requestCode == RESTORE_PERMISSION_REQUEST_CODE) {
                restoreTaskData(getApplicationContext());
            }
        } else {
            // Permission request was denied.
            Toast.makeText(this, "Permission request was denied.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private static class TaskListItem {
        final int type;
        final Object data;
        final Date lastDoneDate;
        final ComboCount comboCount;

        TaskListItem(int type, Object data, Date lastDoneDate, ComboCount comboCount) {
            this.type = type;
            this.data = data;
            this.lastDoneDate = lastDoneDate;
            this.comboCount = comboCount;
        }
    }

    private static class TaskListHeader {
        String title;
    }

    private class TaskAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            TaskListItem item = (TaskListItem) getItem(position);
            return item.type;
        }

        @Override
        public boolean isEnabled(int position) {
            return (getItemViewType(position) == TYPE_ITEM);
        }

        @Override
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
                    itemViewHolder.imageView = view
                            .findViewById(R.id.taskListItemCheck);
                    itemViewHolder.textView1 = view
                            .findViewById(R.id.taskName);
                    itemViewHolder.textView2 = view
                            .findViewById(R.id.taskContext);
                    itemViewHolder.recurrenceView = view
                            .findViewById(R.id.recurrenceView);
                    itemViewHolder.lastDoneDateTextView = view
                            .findViewById(R.id.taskLastDoneDate);
                    itemViewHolder.imageAlarm = view
                            .findViewById(R.id.AlarmIcon);
                    itemViewHolder.textAlarm = view
                            .findViewById(R.id.AlarmText);
                    view.setTag(itemViewHolder);
                } else {
                    headerViewHolder = new HeaderViewHolder();
                    view = inflater.inflate(R.layout.task_list_header, parent, false);
                    headerViewHolder.viewType = TYPE_HEADER;
                    headerViewHolder.textView1 = view
                            .findViewById(R.id.textView1);
                    view.setTag(headerViewHolder);
                }
            }

            if (isTask) {
                Task task = (Task) taskListItem.data;

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

                ImageView imageAlarm = itemViewHolder.imageAlarm;
                TextView textAlarm = itemViewHolder.textAlarm;
                Reminder reminder = task.getReminder();
                if (reminder.getEnabled()) {
                    String alarmStr = String.format("%1$02d", reminder.getHourOfDay()) + ":" + String.format("%1$02d", reminder.getMinute());
                    textAlarm.setText(alarmStr);
                    imageAlarm.setVisibility(View.VISIBLE);
                    textAlarm.setVisibility(View.VISIBLE);
                } else {
                    imageAlarm.setVisibility(View.GONE);
                    textAlarm.setVisibility(View.GONE);
                }

                ImageView imageView = itemViewHolder.imageView;
                final Date today = DateChangeTimeUtil.getDateTime();
                boolean checked = DateComparator.equals(taskListItem.lastDoneDate, today);

                updateView(taskListItem, checked, itemViewHolder);

                imageView.setTag(position);
                imageView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        View parent = (View) v.getParent();
                        ItemViewHolder itemViewHolder = new ItemViewHolder();
                        itemViewHolder.imageView = (ImageView) v;
                        itemViewHolder.lastDoneDateTextView =
                                parent.findViewById(R.id.taskLastDoneDate);
                        int position = (Integer) v.getTag();
                        TaskListItem taskListItem = (TaskListItem) getItem(position);
                        Task task = (Task) taskListItem.data;
                        long taskId = task.getTaskID();
                        boolean checked = DateComparator.equals(taskListItem.lastDoneDate, today);
                        checked = !checked;
                        mDBAdapter.setDoneStatus(taskId, today, checked);
                        updateModel(task, position);
                        taskListItem = (TaskListItem) getItem(position);
                        updateView(taskListItem, checked, itemViewHolder);
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

        private void updateModel(Task task, int position){
            Date lastDoneDate = mDBAdapter.getLastDoneDate(task.getTaskID());
            ComboCount comboCount = mDBAdapter.getComboCount(task.getTaskID());
            TaskListItem newTaskListItem =
                    new TaskListItem(TYPE_ITEM, task, lastDoneDate, comboCount);
            mDataList.set(position, newTaskListItem);

        }
        private void updateView(TaskListItem taskListItem, boolean checked,
                                ItemViewHolder holder) {
            Date today = DateChangeTimeUtil.getDateTime();
            ImageView imageView = holder.imageView;
            TextView lastDoneDateTextView = holder.lastDoneDateTextView;
            final int colorLastDoneDate = CompatUtil.getColor(getApplicationContext(),
                    R.color.tasklist_last_donedate);
            lastDoneDateTextView.setTextColor(colorLastDoneDate);

            if (checked) {
                imageView.setImageResource(Settings.getDoneIconId());
                ComboCount comboCount = taskListItem.comboCount;
                if (comboCount.currentCount > 1) {
                    lastDoneDateTextView.setTextColor(CompatUtil.getColor(getApplicationContext(),
                            R.color.tasklist_combo));
                    lastDoneDateTextView.setText(getString(
                            R.string.tasklist_combo, comboCount.currentCount));
                } else {
                    lastDoneDateTextView
                            .setText(R.string.tasklist_lastdonedate_today);
                }
            } else {
                imageView.setImageResource(Settings.getNotDoneIconId());
                Date lastDoneDate = taskListItem.lastDoneDate;
                if (lastDoneDate != null) {
                    ComboCount comboCount = taskListItem.comboCount;
                    if (comboCount.currentCount > 1) {
                        lastDoneDateTextView.setTextColor(CompatUtil
                                .getColor(getApplicationContext(), R.color.tasklist_combo));
                        lastDoneDateTextView.setText(getString(
                                R.string.tasklist_combo,
                                comboCount.currentCount));
                    } else {
                        int diffDays = (int) ((today.getTime() - lastDoneDate.getTime())
                                / (long) (1000 * 60 * 60 * 24));
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
            ImageView imageAlarm;
            TextView textAlarm;
        }
    }
}
