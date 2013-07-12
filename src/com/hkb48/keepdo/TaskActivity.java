package com.hkb48.keepdo;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.MenuItem;
import android.widget.TabHost;

public class TaskActivity extends FragmentActivity {

    private String mCurrentSelectedTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        FragmentTabHost mTabHost = new FragmentTabHost(this);
        setContentView(mTabHost);

        final long taskId = getIntent().getLongExtra("TASK-ID", -1);
        final Task task = DatabaseAdapter.getInstance(this).getTask(taskId);
        if (task != null) setTitle(task.getName());

        mTabHost.setup(this, getSupportFragmentManager(), R.layout.activity_task);
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.title_section1)).setIndicator(getResources().getString(R.string.title_section1)), CalendarFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.title_section2)).setIndicator(getResources().getString(R.string.title_section2)), TaskDetailFragment.class, null);
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String tabId) {
                mCurrentSelectedTab = tabId;
            }
        });
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
}