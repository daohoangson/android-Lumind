package com.daohoangson.lumind;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.daohoangson.lumind.databinding.ActivityMainBinding;
import com.daohoangson.lumind.fragment.CalendarFragment;
import com.daohoangson.lumind.fragment.ReminderFragment;
import com.daohoangson.lumind.fragment.RemindersFragment;
import com.daohoangson.lumind.model.DataStore;
import com.daohoangson.lumind.model.Reminder;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity
        implements CalendarFragment.CallerActivity,
        ReminderFragment.CallerActivity,
        RemindersFragment.CallerActivity {

    private WeakReference<CalendarFragment> mCalendarFragmentRef;
    private WeakReference<RemindersFragment> mRemindersFragmentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        binding.tabs.setupWithViewPager(binding.viewPager, true);

        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // intentionally left blank
            }

            @Override
            public void onPageSelected(int position) {
                resetViews(binding);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // intentionally left blank
            }
        });

        binding.fab.setOnClickListener(view -> startFabAction(binding));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DataStore.closeIfOpened();
    }

    @Override
    public void setCalendarFragment(CalendarFragment f) {
        mCalendarFragmentRef = new WeakReference<>(f);
    }

    @Override
    public FloatingActionButton getFab() {
        return (FloatingActionButton) findViewById(R.id.fab);
    }

    @Override
    public void onReminderSaved(Reminder reminder, boolean hasListeners) {
        hideSoftKeyboard();

        Snackbar.make(findViewById(R.id.viewPager),
                reminder.existingUuid == null
                        ? R.string.form_reminder_added
                        : R.string.form_reminder_updated,
                Snackbar.LENGTH_SHORT).show();

        if (!hasListeners && mRemindersFragmentRef != null) {
            RemindersFragment remindersFragment = mRemindersFragmentRef.get();
            if (remindersFragment != null) {
                remindersFragment.startRefreshing();
            }
        }
    }

    @Override
    public void onReminderError(Reminder reminder, Throwable error, boolean hasListeners) {
        if (BuildConfig.DEBUG) {
            Snackbar.make(findViewById(R.id.viewPager),
                    String.format("%s %s %s", reminder.toString(), error.getMessage(), hasListeners),
                    Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(findViewById(R.id.viewPager),
                    R.string.form_reminder_error,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setRemindersFragment(RemindersFragment f) {
        mRemindersFragmentRef = new WeakReference<>(f);
    }

    private void resetViews(ActivityMainBinding binding) {
        binding.fab.setVisibility(View.GONE);

        hideSoftKeyboard();

        int tabId = binding.viewPager.getCurrentItem();
        switch (tabId) {
            case 0:
                if (mCalendarFragmentRef != null) {
                    CalendarFragment calendarFragment = mCalendarFragmentRef.get();
                    if (calendarFragment != null) {
                        calendarFragment.setActiveTab(this);
                    }
                }
                break;
            case 1:
                if (mRemindersFragmentRef != null) {
                    RemindersFragment remindersFragment = mRemindersFragmentRef.get();
                    if (remindersFragment != null) {
                        remindersFragment.setActiveTab();
                    }
                }
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        imm.hideSoftInputFromWindow(findViewById(R.id.viewPager).getWindowToken(), 0);
    }

    private void startFabAction(ActivityMainBinding binding) {
        switch (binding.viewPager.getCurrentItem()) {
            case 0:
                if (mCalendarFragmentRef != null) {
                    CalendarFragment calendarFragment = mCalendarFragmentRef.get();
                    if (calendarFragment != null) {
                        calendarFragment.startFabAction();
                    }
                }
                break;
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return CalendarFragment.newInstance();
                case 1:
                    return RemindersFragment.newInstance();
            }

            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_fragment_calendar);
                case 1:
                    return getString(R.string.title_fragment_reminders);
            }

            return super.getPageTitle(position);
        }
    }
}
