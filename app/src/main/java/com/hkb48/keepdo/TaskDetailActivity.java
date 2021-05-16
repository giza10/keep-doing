package com.hkb48.keepdo;

import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {
    private long mTaskId;
    private boolean mModelUpdated;
    private ContentObserver mContentObserver;

    public TaskDetailActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTaskId = getIntent().getLongExtra("TASK-ID", -1);

        mContentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                mModelUpdated = true;
            }
        };
        getContentResolver().registerContentObserver(KeepdoProvider.BASE_CONTENT_URI, true, mContentObserver);
        mModelUpdated = true;
    }

    @Override
    public void onResume() {
        if (mModelUpdated) {
            mModelUpdated = false;
            updateTitle();
            updateDetails();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        getContentResolver().unregisterContentObserver(mContentObserver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.menu_edit) {
            Intent intent = new Intent(TaskDetailActivity.this,
                    TaskSettingActivity.class);
            Task task = DatabaseAdapter.getInstance(this).getTask(mTaskId);
            intent.putExtra(TaskSettingActivity.EXTRA_TASK_INFO, task);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void updateTitle() {
        Task task = DatabaseAdapter.getInstance(this).getTask(mTaskId);
        if (task != null) {
            setTitle(task.getName());
        }
    }

    private void updateDetails() {
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(this);
        Task task = dbAdapter.getTask(mTaskId);

        // Recurrence
        RecurrenceView recurrenceView = findViewById(R.id.recurrenceView);
        recurrenceView.update(task.getRecurrence());

        // Reminder
        TextView reminderTextView = findViewById(R.id.taskDetailReminderValue);
        Reminder reminder = task.getReminder();
        if (reminder.getEnabled()) {
            reminderTextView.setText(
                    getString(R.string.remind_at, reminder.getHourOfDay(), reminder.getMinute()));
        } else {
            reminderTextView.setText(R.string.no_reminder);
        }

        // Context
        TextView contextTitleTextView = findViewById(R.id.taskDetailContext);
        TextView contextTextView = findViewById(R.id.taskDetailContextDescription);
        String contextStr = task.getContext();
        if (contextStr == null || contextStr.isEmpty()) {
            View contextLayout = findViewById(R.id.taskDetailContextContainer);
            contextLayout.setVisibility(View.GONE);
            contextTitleTextView.setVisibility(View.INVISIBLE);
            contextTextView.setVisibility(View.INVISIBLE);
        } else {
            contextTitleTextView.setVisibility(View.VISIBLE);
            contextTextView.setVisibility(View.VISIBLE);
            contextTextView.setText(contextStr);
        }

        // Total number of done
        TextView numOfDoneTextView = findViewById(R.id.taskDetailNumOfDoneValue);
        numOfDoneTextView.setText(getString(R.string.number_of_times, dbAdapter.getNumberOfDone(task.getTaskID())));

        // Current combo / Max combo
        TextView comboTextView = findViewById(R.id.taskDetailComboValue);
        ComboCount combo = dbAdapter.getComboCount(task.getTaskID());
        if (combo != null) {
            comboTextView.setText(getString(R.string.current_and_max_combo_num, combo.currentCount, combo.maxCount));
        }

        // First date that done is set
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);
        TextView firstDoneDateTextView = findViewById(R.id.taskDetailFirstDoneDateValue);
        Date firstDoneDate = dbAdapter.getFirstDoneDate(task.getTaskID());
        if (firstDoneDate != null) {
            firstDoneDateTextView.setText(dateFormat.format(firstDoneDate));
        } else {
            View firstDoneDateLayout = findViewById(R.id.taskDetailFirstDoneDateContainer);
            firstDoneDateLayout.setVisibility(View.GONE);
        }

        // Last date that done is set
        TextView lastDoneDateTextView = findViewById(R.id.taskDetailLastDoneDateValue);
        Date lastDoneDate = dbAdapter.getLastDoneDate(task.getTaskID());
        if (lastDoneDate != null) {
            lastDoneDateTextView.setText(dateFormat.format(lastDoneDate));
        } else {
            View lastDoneDateLayout = findViewById(R.id.taskDetailLastDoneDateContainer);
            lastDoneDateLayout.setVisibility(View.GONE);
        }
    }
}
