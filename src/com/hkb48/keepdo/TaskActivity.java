package com.hkb48.keepdo;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

public class TaskActivity extends FragmentActivity implements
        ActionBar.TabListener {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current tab position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        // Set up the action bar to show tabs.
        final ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);

        final long taskId = getIntent().getLongExtra("TASK-ID", -1);
        final Task task = DatabaseAdapter.getInstance(this).getTask(taskId);
        if (task != null) {
            setTitle(task.getName());
        }

        // For each of the sections in the app, add a tab to the action bar.
        actionBar.addTab(actionBar.newTab().setText(R.string.title_section1)
                .setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.title_section2)
                .setTabListener(this));
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current tab position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current tab position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
                .getSelectedNavigationIndex());
    }

    public void onTabSelected(ActionBar.Tab tab,
            FragmentTransaction fragmentTransaction) {
        if (tab.getPosition() == 0) {
            Fragment fragment = new CalendarFragment();
            getSupportFragmentManager().beginTransaction()
                     .replace(R.id.container, fragment).commit();
        } else {
            Fragment fragment = new TaskDetailFragment();
            getSupportFragmentManager().beginTransaction()
                     .replace(R.id.container, fragment).commit();
        }
    }

    public void onTabUnselected(ActionBar.Tab tab,
        FragmentTransaction fragmentTransaction) {
    }

    public void onTabReselected(ActionBar.Tab tab,
        FragmentTransaction fragmentTransaction) {
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
