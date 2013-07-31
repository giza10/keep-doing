package com.hkb48.keepdo;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

public class TaskActivity extends FragmentActivity implements
        ActionBar.TabListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        final ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);

        final long taskId = getIntent().getLongExtra("TASK-ID", -1);
        final Task task = DatabaseAdapter.getInstance(this).getTask(taskId);
        if (task != null)
            setTitle(task.getName());

        // For each of the sections in the app, add a tab to the action bar.
        actionBar.addTab(actionBar.newTab().setText(R.string.title_section1)
                .setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.title_section2)
                .setTabListener(this));
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

    public void onTabReselected(Tab arg0, FragmentTransaction ft) {
        // TODO Auto-generated method stub
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        if (tab.getPosition() == 0) {
            CalendarFragment fragment = new CalendarFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment).commit();
        } else {
            TaskDetailFragment fragment = new TaskDetailFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment).commit();
        }
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
    }
}