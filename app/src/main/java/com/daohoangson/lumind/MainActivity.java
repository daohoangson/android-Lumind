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
import com.daohoangson.lumind.fragment.SettingFragment;
import com.daohoangson.lumind.model.Reminder;
import com.daohoangson.lumind.schedule.JobSchedulerService;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity
        implements CalendarFragment.CallerActivity,
        ReminderFragment.CallerActivity,
        RemindersFragment.CallerActivity {

    WeakReference<CalendarFragment> mCalendarFragmentRef;
    WeakReference<RemindersFragment> mRemindersFragmentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.toolbar);

        binding.viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        binding.viewPager.setCurrentItem(1);
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

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startFabAction(binding);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        JobSchedulerService.scheduleReminderJob(this, true, 6);
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
                    error.getMessage(),
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

    void resetViews(ActivityMainBinding binding) {
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

    void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.viewPager).getWindowToken(), 0);
    }

    void startFabAction(ActivityMainBinding binding) {
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

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return SettingFragment.newInstance();
                case 1:
                    return CalendarFragment.newInstance();
                case 2:
                    return RemindersFragment.newInstance();
            }

            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_fragment_setting);
                case 1:
                    return getString(R.string.title_fragment_calendar);
                case 2:
                    return getString(R.string.title_fragment_reminders);
            }

            return super.getPageTitle(position);
        }
    }
}
