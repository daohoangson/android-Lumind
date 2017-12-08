package com.daohoangson.lumind;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.daohoangson.lumind.databinding.ActivityMainBinding;
import com.daohoangson.lumind.fragment.CalendarFragment;
import com.daohoangson.lumind.fragment.ReminderFragment;
import com.daohoangson.lumind.fragment.RemindersFragment;
import com.daohoangson.lumind.fragment.SettingsFragment;
import com.daohoangson.lumind.model.DataStore;
import com.daohoangson.lumind.model.Lumindate;
import com.daohoangson.lumind.model.Reminder;
import com.daohoangson.lumind.schedule.AlarmReceiver;
import com.daohoangson.lumind.schedule.ReminderEngine;
import com.daohoangson.lumind.utils.PrefUtil;

import java.lang.ref.WeakReference;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements CalendarFragment.CallerActivity,
        ReminderFragment.CallerActivity,
        RemindersFragment.CallerActivity {

    private static final String ARG_NTF_REMINDER_NTF_ID = "ntfReminderNtfId";
    private static final String ARG_NTF_REMINDER_UUID = "ntfReminderUuid";
    private static final int PAGER_ITEM_CALENDAR = 0;
    private static final int PAGER_ITEM_REMINDERS = 1;
    private static final int PAGER_ITEM_SETTINGS = 2;
    private static final String TAG = "MainActivity";

    private ActivityMainBinding mBinding;
    private WeakReference<CalendarFragment> mCalendarFragmentRef;
    private WeakReference<RemindersFragment> mRemindersFragmentRef;
    private Reminder mPendingNtfReminder;

    public static Intent newNtfIntent(Context context, int ntfId, String uuid) {
        Intent i = new Intent(context, MainActivity.class);

        i.putExtra(ARG_NTF_REMINDER_NTF_ID, ntfId);
        i.putExtra(ARG_NTF_REMINDER_UUID, uuid);

        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mBinding.viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        mBinding.tabs.setupWithViewPager(mBinding.viewPager, true);

        mBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // intentionally left blank
            }

            @Override
            public void onPageSelected(int position) {
                resetViews();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // intentionally left blank
            }
        });

        mBinding.fab.setOnClickListener(view -> startFabAction());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DataStore.closeIfOpened();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (PrefUtil.getRemind(this)) {
            AlarmReceiver.setup(this);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(ARG_NTF_REMINDER_UUID)) {
            int ntfId = intent.getIntExtra(ARG_NTF_REMINDER_NTF_ID, 0);
            ReminderEngine.cancelNotification(this, ntfId);

            String ntfReminderUuid = intent.getStringExtra(ARG_NTF_REMINDER_UUID);
            DataStore.getReminders(this, results -> {
                for (Reminder reminder : results) {
                    if (!reminder.isSameUuid(ntfReminderUuid)) {
                        continue;
                    }

                    mPendingNtfReminder = reminder;
                }
            });
        }
    }

    @Override
    public void setCalendarFragment(CalendarFragment f) {
        mCalendarFragmentRef = new WeakReference<>(f);

        if (mPendingNtfReminder != null) {
            Calendar since = Calendar.getInstance();
            Calendar no = mPendingNtfReminder.getNextOccurrence(since);
            mCalendarFragmentRef.get().mDate.setTimeInMillis(no.getTimeInMillis());
        }
    }

    @Override
    public FloatingActionButton getFab() {
        return (FloatingActionButton) findViewById(R.id.fab);
    }

    @Override
    public void onReminderSaved(Reminder reminder, boolean hasListeners) {
        hideSoftKeyboard();

        Snackbar.make(findViewById(R.id.viewPager),
                reminder.isInsert()
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
        Log.d(TAG, String.format("%s %s %s", reminder.toString(), error.getMessage(), hasListeners));

        Snackbar.make(findViewById(R.id.viewPager),
                R.string.form_reminder_error,
                Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void setCalendarDate(Lumindate date) {
        if (mCalendarFragmentRef == null) {
            return;
        }

        mBinding.viewPager.setCurrentItem(PAGER_ITEM_CALENDAR);
        mCalendarFragmentRef.get().mDate.setTimeInMillis(date.getTimeInMillis());
    }

    @Override
    public void setRemindersFragment(RemindersFragment f) {
        mRemindersFragmentRef = new WeakReference<>(f);
    }

    private void resetViews() {
        mBinding.fab.setVisibility(View.GONE);

        hideSoftKeyboard();

        int tabId = mBinding.viewPager.getCurrentItem();
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

    private void startFabAction() {
        if (mCalendarFragmentRef != null) {
            CalendarFragment calendarFragment = mCalendarFragmentRef.get();
            if (calendarFragment != null) {
                calendarFragment.startFabAction();
            }
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case PAGER_ITEM_CALENDAR:
                    return CalendarFragment.newInstance();
                case PAGER_ITEM_REMINDERS:
                    return RemindersFragment.newInstance();
                case PAGER_ITEM_SETTINGS:
                    return new SettingsFragment();
            }

            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case PAGER_ITEM_CALENDAR:
                    return getString(R.string.title_fragment_calendar);
                case PAGER_ITEM_REMINDERS:
                    return getString(R.string.title_fragment_reminders);
                case PAGER_ITEM_SETTINGS:
                    return getString(R.string.title_fragment_settings);
            }

            return super.getPageTitle(position);
        }
    }
}
