package com.hkb48.keepdo.calendar;

import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.hkb48.keepdo.DatabaseAdapter;
import com.hkb48.keepdo.KeepdoProvider;
import com.hkb48.keepdo.R;
import com.hkb48.keepdo.Task;

public class TaskCalendarActivity extends AppCompatActivity {
    private long mTaskId;
    private boolean mModelUpdated;
    private ContentObserver mContentObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_calendar);

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

        CalendarFragment fragment = new CalendarFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment).commit();
    }

    @Override
    public void onResume() {
        if (mModelUpdated) {
            mModelUpdated = false;
            updateTitle();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        getContentResolver().unregisterContentObserver(mContentObserver);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
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
}