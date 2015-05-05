package com.hkb48.keepdo;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;

public class NavigationDrawerFragment extends Fragment {

    private static final int NAVDRAWER_ITEM_SORT = 1;
    private static final int NAVDRAWER_ITEM_IMPORT = 2;
    private static final int NAVDRAWER_ITEM_EXPORT = 3;
    private static final int NAVDRAWER_ITEM_BACKUP_RESTORE = 4;
    private static final int NAVDRAWER_ITEM_SETTINGS = 5;

    private static final String[] NAVDRAWER_TITLE_RES_ID = {
            "Sort",
            "Export",
            "Import",
            "Backup&Restore(GoogleDrive)",
            "Settings"};

    private static final int[] NAVDRAWER_ICON_RES_ID = new int[]{
            R.drawable.ic_sort,
            R.drawable.ic_file_upload,
            R.drawable.ic_file_download,
            R.drawable.ic_drive,
            R.drawable.ic_settings
    };

    // delay to launch nav drawer item, to allow close animation to play
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
        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.RecyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.Adapter adapter = new DrawerAdapter(NAVDRAWER_TITLE_RES_ID, NAVDRAWER_ICON_RES_ID);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        onNavDrawerItemClicked(position);
                    }
                })
        );

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
        if ((mDrawerLayout != null) && (mContainerView != null)) {
            return mDrawerLayout.isDrawerOpen(mContainerView);
        }
        return false;
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
            case NAVDRAWER_ITEM_IMPORT:
                break;
            case NAVDRAWER_ITEM_EXPORT:
                break;
            case NAVDRAWER_ITEM_BACKUP_RESTORE:
                break;
            case NAVDRAWER_ITEM_SETTINGS:
                intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {

        private static final int TYPE_HEADER = 0;  // Declaring Variable to Understand which View is being worked on
        // IF the view under inflation and population is header or Item
        private static final int TYPE_ITEM = 1;

        private String mNavTitles[]; // String Array to store the passed titles Value from MainActivity.java
        private int mIcons[];       // Int Array to store the passed icons resource value from MainActivity.java

        // Creating a ViewHolder which extends the RecyclerView View Holder
        // ViewHolder are used to to store the inflated views in order to recycle them

        public class ViewHolder extends RecyclerView.ViewHolder {
            int Holderid;

            TextView textView;
            ImageView imageView;

            public ViewHolder(View itemView, int ViewType) {                 // Creating ViewHolder Constructor with View and viewType As a parameter
                super(itemView);

                // Here we set the appropriate view in accordance with the the view type as passed when the holder object is created
                if (ViewType == TYPE_ITEM) {
                    textView = (TextView) itemView.findViewById(R.id.title); // Creating TextView object with the id of textView from item_row.xml
                    imageView = (ImageView) itemView.findViewById(R.id.icon);// Creating ImageView object with the id of ImageView from item_row.xml
                    Holderid = 1;                                               // setting holder id as 1 as the object being populated are of type item row
                } else {
                    Holderid = 0;                                                // Setting holder id = 0 as the object being populated are of type header view
                }
            }
        }

        DrawerAdapter(String Titles[], int Icons[]) { // MyAdapter Constructor with titles and icons parameter
            // titles, icons, name, email, profile pic are passed from the main activity as we
            mNavTitles = Titles;                //have seen earlier
            mIcons = Icons;
            //in adapter
        }

        //Below first we ovverride the method onCreateViewHolder which is called when the ViewHolder is
        //Created, In this method we inflate the item_row.xml layout if the viewType is Type_ITEM or else we inflate header.xml
        // if the viewType is TYPE_HEADER
        // and pass it to the view holder

        @Override
        public DrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_ITEM) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_item_row, parent, false); //Inflating the layout
                ViewHolder vhItem = new ViewHolder(v, viewType); //Creating ViewHolder and passing the object of type view
                return vhItem; // Returning the created object
            } else if (viewType == TYPE_HEADER) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_header, parent, false); //Inflating the layout
                ViewHolder vhHeader = new ViewHolder(v, viewType); //Creating ViewHolder and passing the object of type view
                return vhHeader; //returning the object created
            }
            return null;
        }

        //Next we override a method which is called when the item in a row is needed to be displayed, here the int position
        // Tells us item at which position is being constructed to be displayed and the holder id of the holder object tell us
        // which view type is being created 1 for item row
        @Override
        public void onBindViewHolder(DrawerAdapter.ViewHolder holder, int position) {
            if (holder.Holderid == 1) {                              // as the list view is going to be called after the header view so we decrement the
                // position by 1 and pass it to the holder while setting the text and image
                holder.textView.setText(mNavTitles[position - 1]); // Setting the Text with the array of our Titles
                holder.imageView.setImageResource(mIcons[position - 1]);// Settimg the image with array of our icons
            }
        }

        // This method returns the number of items present in the list
        @Override
        public int getItemCount() {
            return mNavTitles.length + 1; // the number of items in the list will be +1 the titles including the header view.
        }


        // Witht the following method we check what type of view is being passed
        @Override
        public int getItemViewType(int position) {
            if (isPositionHeader(position))
                return TYPE_HEADER;

            return TYPE_ITEM;
        }

        private boolean isPositionHeader(int position) {
            return position == 0;
        }
    }
}
