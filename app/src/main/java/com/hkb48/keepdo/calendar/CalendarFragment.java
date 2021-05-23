package com.hkb48.keepdo.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.hkb48.keepdo.R;
import com.hkb48.keepdo.settings.Settings;

public class CalendarFragment extends Fragment {

    /**
     * The maximum number of months to be paging up (Past: 10 years, Future: 1 year)
     */
    private static final int NUM_MAXIMUM_MONTHS_PAST = 10 * 12;  // 10 years
    static final int INDEX_OF_THIS_MONTH = NUM_MAXIMUM_MONTHS_PAST - 1;
    private static final int NUM_MAXIMUM_MONTHS_FUTURE = 12;  // 12 months
    private static final int NUM_MAXIMUM_MONTHS = NUM_MAXIMUM_MONTHS_PAST + NUM_MAXIMUM_MONTHS_FUTURE;

    public CalendarFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager mViewPager = view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(new CalendarPageAdapter(getChildFragmentManager()));
        mViewPager.setCurrentItem(CalendarFragment.INDEX_OF_THIS_MONTH);
    }

    public static class CalendarPageAdapter extends FragmentPagerAdapter {
        CalendarPageAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getCount() {
            return Settings.getEnableFutureDate() ? CalendarFragment.NUM_MAXIMUM_MONTHS : CalendarFragment.NUM_MAXIMUM_MONTHS_PAST;
        }

        @Override
        @NonNull
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putInt(CalendarGrid.POSITION_KEY, position);
            return CalendarGrid.newInstance(args);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CalendarGrid.getPageTitle(position);
        }
    }
}