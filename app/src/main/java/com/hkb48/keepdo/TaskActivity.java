package com.hkb48.keepdo;

import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

public class TaskActivity extends AppCompatActivity {
    private long mTaskId;
    private boolean mModelUpdated;
    private ContentObserver mContentObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

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
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
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