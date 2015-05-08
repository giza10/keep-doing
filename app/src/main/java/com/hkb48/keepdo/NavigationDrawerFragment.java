package com.hkb48.keepdo;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class NavigationDrawerFragment extends Fragment {

    private static final int NAVDRAWER_ITEM_SORT = 1;
    private static final int NAVDRAWER_ITEM_BACKUP_RESTORE_DEVICE = 2;
    private static final int NAVDRAWER_ITEM_BACKUP_RESTORE_DRIVE = 3;
    private static final int NAVDRAWER_ITEM_SETTINGS = 4;

    private static final NavDrawerListItem[] NAVDRAWER_LIST_ITEMS = {
            new NavDrawerListItem(NavDrawerListItem.TYPE_HEADER, R.drawable.ic_header, NavDrawerListItem.INVALID_ID),
            new NavDrawerListItem(NavDrawerListItem.TYPE_ITEM, NAVDRAWER_ITEM_SORT, R.drawable.ic_sort, R.string.drawer_item_sort),
            new NavDrawerListItem(NavDrawerListItem.TYPE_SEPARATOR),
            new NavDrawerListItem(NavDrawerListItem.TYPE_SUBHEADER, NAVDRAWER_ITEM_BACKUP_RESTORE_DEVICE, NavDrawerListItem.INVALID_ID, R.string.drawer_category_backup_restore),
            new NavDrawerListItem(NavDrawerListItem.TYPE_ITEM, NAVDRAWER_ITEM_BACKUP_RESTORE_DEVICE, R.drawable.ic_phone_android, R.string.drawer_item_backup_restore_device),
            new NavDrawerListItem(NavDrawerListItem.TYPE_ITEM, NAVDRAWER_ITEM_BACKUP_RESTORE_DRIVE, R.drawable.ic_drive, R.string.drawer_item_backup_restore_drive),
            new NavDrawerListItem(NavDrawerListItem.TYPE_SEPARATOR),
            new NavDrawerListItem(NavDrawerListItem.TYPE_ITEM, NAVDRAWER_ITEM_SETTINGS, R.drawable.ic_settings, R.string.drawer_item_settings)
    };

    // Delay to launch nav drawer item, to allow close animation to play
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private View mContainerView;

    public NavigationDrawerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
    }

    public void setup(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        ListView listView = (ListView) getActivity().findViewById(R.id.RecyclerView);
        final NavDrawerListAdapter adapter = new NavDrawerListAdapter(NAVDRAWER_LIST_ITEMS);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                onNavDrawerItemClicked((int) adapter.getItemId(position));

            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }

    public boolean isDrawerOpen() {
        return (mDrawerLayout != null) && (mContainerView != null) && mDrawerLayout.isDrawerOpen(mContainerView);
    }

    public void closeDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(Gravity.START);
        }
    }

    private void onNavDrawerItemClicked(final int itemId) {
        // launch the target Activity after a short delay, to allow the close animation to play
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goToNavDrawerItem(itemId);
            }
        }, NAVDRAWER_LAUNCH_DELAY);
        closeDrawer();
    }

    private void goToNavDrawerItem(final int itemId) {
        Intent intent;
        switch (itemId) {
            case NAVDRAWER_ITEM_SORT:
                intent = new Intent(getActivity(), TaskSortingActivity.class);
                startActivity(intent);
                break;
            case NAVDRAWER_ITEM_BACKUP_RESTORE_DEVICE:
                // Todo: Tentative implementation
                showBackupRestoreDialog();
                break;
            case NAVDRAWER_ITEM_BACKUP_RESTORE_DRIVE:
                intent = new Intent(getActivity(), GoogleDriveServicesActivity.class);
                startActivity(intent);
                break;
            case NAVDRAWER_ITEM_SETTINGS:
                intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }


    /**
     * Backup & Restore
     */
    private void showBackupRestoreDialog() {
        final DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(getActivity());
        final String fineName = dbAdapter.backupFileName();
        final String dirName = dbAdapter.backupDirName();
        final String dirPath = dbAdapter.backupDirPath();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
                getActivity());
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
                                Toast.makeText(getActivity(),
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
                                backupTaskData();
                                Toast.makeText(getActivity(),
                                        R.string.backup_done, Toast.LENGTH_SHORT)
                                        .show();
                            case 1:
                                // execute restore
                                restoreTaskData();
                                Toast.makeText(getActivity(),
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
        DatabaseAdapter.getInstance(getActivity()).backupDataBase();
    }

    private void restoreTaskData() {
        DatabaseAdapter.getInstance(getActivity()).restoreDatabase();
    }
}

class NavDrawerListItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_SUBHEADER = 2;
    public static final int TYPE_SEPARATOR = 3;
    public static final int INVALID_ID = -1;

    final int type;
    final int itemId;
    final int iconResId;
    final int textResId;

    public NavDrawerListItem(int type) {
        this.type = type;
        this.itemId = INVALID_ID;
        this.iconResId = INVALID_ID;
        this.textResId = INVALID_ID;
    }

    public NavDrawerListItem(int type, int iconResId, int textResId) {
        this.type = type;
        this.itemId = INVALID_ID;
        this.iconResId = iconResId;
        this.textResId = textResId;
    }

    public NavDrawerListItem(int type, int itemId, int iconResId, int textResId) {
        this.type = type;
        this.itemId = itemId;
        this.iconResId = iconResId;
        this.textResId = textResId;
    }
}

class NavDrawerListAdapter extends BaseAdapter {
    private final NavDrawerListItem[] mNavDrawerListItems;

    public static class ViewHolder {
        int viewType;
        TextView textView;
        ImageView imageView;

        public ViewHolder(View itemView, int viewType) {
            switch (viewType) {
                case NavDrawerListItem.TYPE_ITEM:
                    textView = (TextView) itemView.findViewById(R.id.title);
                    imageView = (ImageView) itemView.findViewById(R.id.icon);
                    break;
                case NavDrawerListItem.TYPE_HEADER:
                    imageView = (ImageView) itemView.findViewById(R.id.icon);
                    break;
                case NavDrawerListItem.TYPE_SUBHEADER:
                    textView = (TextView) itemView.findViewById(R.id.title);
                    break;
                default:
                    break;
            }
            this.viewType = viewType;
        }
    }

    NavDrawerListAdapter(NavDrawerListItem[] navDrawerListItems) {
        mNavDrawerListItems = navDrawerListItems;
    }

    @Override
    public int getCount() {
        return mNavDrawerListItems.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return mNavDrawerListItems[position].itemId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;
        final int viewType = getItemViewType(position);
        boolean reuseView = true;

        if (view == null || ((ViewHolder) view.getTag()).viewType != viewType) {
            reuseView = false;
        }

        if (!reuseView) {
            switch (viewType) {
                case NavDrawerListItem.TYPE_ITEM:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.navdrawer_item_row, parent, false);
                    break;
                case NavDrawerListItem.TYPE_HEADER:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.navdrawer_header, parent, false);
                    break;
                case NavDrawerListItem.TYPE_SUBHEADER:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.navdrawer_subheader, parent, false);
                    break;
                case NavDrawerListItem.TYPE_SEPARATOR:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.navdrawer_separator, parent, false);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            viewHolder = new ViewHolder(view, viewType);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        switch (viewType) {
            case NavDrawerListItem.TYPE_ITEM:
                viewHolder.textView.setText(mNavDrawerListItems[position].textResId);
                viewHolder.imageView.setImageResource(mNavDrawerListItems[position].iconResId);
                break;
            case NavDrawerListItem.TYPE_HEADER:
                viewHolder.imageView.setImageResource(mNavDrawerListItems[position].iconResId);
                break;
            case NavDrawerListItem.TYPE_SUBHEADER:
                viewHolder.textView.setText(mNavDrawerListItems[position].textResId);
                break;
            default:
                break;
        }

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return mNavDrawerListItems[position].type;
    }

    @Override
    public boolean isEnabled(int position) {
        return (getItemViewType(position) == NavDrawerListItem.TYPE_ITEM);
    }
}
