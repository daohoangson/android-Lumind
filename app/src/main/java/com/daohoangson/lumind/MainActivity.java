package com.daohoangson.lumind;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
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
import com.daohoangson.lumind.fragment.ReminderAddFragment;
import com.daohoangson.lumind.fragment.ReminderBaseFragment;
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
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements CalendarFragment.CallerActivity,
        ReminderBaseFragment.CallerActivity,
        RemindersFragment.CallerActivity {

    private static final String ARG_NTF_REMINDER_NTF_ID = "ntfReminderNtfId";
    private static final String ARG_NTF_REMINDER_UUID = "ntfReminderUuid";
    private static final int PAGER_ITEM_CALENDAR = 0;
    private static final int PAGER_ITEM_REMINDERS = 1;
    private static final int PAGER_ITEM_SETTINGS = 2;
    private static final String TAG = "MainActivity";

    private WeakReference<CalendarFragment> mCalendarFragmentRef;
    private WeakReference<RemindersFragment> mRemindersFragmentRef;
    private Reminder mPendingNtfReminder;
    private PagerCapable mPagerCapable;

    public static Intent newNtfIntent(Context context, int ntfId, String uuid) {
        Intent i = new Intent(context, MainActivity.class);

        i.putExtra(ARG_NTF_REMINDER_NTF_ID, ntfId);
        i.putExtra(ARG_NTF_REMINDER_UUID, uuid);

        return i;
    }

    public interface PagerCapable {
        void setPagerItem(int itemId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setLanguage();

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

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

        binding.fab.setOnClickListener(view -> startAddingReminder(binding));

        mPagerCapable = itemId -> binding.viewPager.setCurrentItem(itemId, true);
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
                    if (!reminder.uuid.equals(ntfReminderUuid)) {
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
    public void onReminderSaved(Reminder reminder, boolean hasListeners) {
        hideKeyboard();

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
    public void onReminderInlineSaved(Reminder reminder) {
        onReminderSaved(reminder, true);
    }

    @Override
    public void onReminderInlineError(Reminder reminder, Throwable error) {
        onReminderError(reminder, error, true);
    }

    @Override
    public void setCalendarDate(Lumindate date) {
        if (mCalendarFragmentRef == null) {
            return;
        }
        mCalendarFragmentRef.get().mDate.setTimeInMillis(date.getTimeInMillis());

        if (mPagerCapable != null) {
            mPagerCapable.setPagerItem(PAGER_ITEM_CALENDAR);
        }
    }

    @Override
    public void setRemindersFragment(RemindersFragment f) {
        mRemindersFragmentRef = new WeakReference<>(f);
    }

    @Override
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        imm.hideSoftInputFromWindow(findViewById(R.id.viewPager).getWindowToken(), 0);
    }

    @Override
    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void resetViews(ActivityMainBinding binding) {
        hideKeyboard();

        int tabId = binding.viewPager.getCurrentItem();
        switch (tabId) {
            case PAGER_ITEM_REMINDERS:
                if (mRemindersFragmentRef != null) {
                    RemindersFragment remindersFragment = mRemindersFragmentRef.get();
                    if (remindersFragment != null) {
                        remindersFragment.setActiveTab();
                    }
                }
        }

        binding.fab.setVisibility(tabId == PAGER_ITEM_SETTINGS ? View.GONE : View.VISIBLE);
    }

    private void setLanguage() {
        String language = PrefUtil.getLanguage(this);
        if (SettingsFragment.PREF_LANGUAGE_DEFAULT.equals(language)) {
            Log.v(TAG, "Requested default language, nothing to do");
            return;
        }

        Locale expected = new Locale(language);
        Locale actual = Locale.getDefault();
        if (actual != null && actual.equals(expected)) {
            Log.v(TAG, "Locales matched, move on");
            return;
        }

        Locale.setDefault(expected);
        Configuration config = new Configuration();
        config.locale = expected;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        Log.v(TAG, String.format("Updated locale config from %s to %s",
                actual != null ? actual.getLanguage() : "N/A", expected.getLanguage()));
    }

    private void startAddingReminder(ActivityMainBinding binding) {
        final ReminderAddFragment f;
        int tabId = binding.viewPager.getCurrentItem();
        if (tabId == PAGER_ITEM_CALENDAR && mCalendarFragmentRef != null) {
            f = ReminderAddFragment.newInstance(mCalendarFragmentRef.get().mDate);
        } else {
            f = new ReminderAddFragment();
        }

        FragmentManager fm = getSupportFragmentManager();
        f.addOnDismissListener((reminder, completed, error) -> {
            if (error != null) {
                onReminderError(reminder, error, false);
                return;
            }

            if (completed) {
                onReminderSaved(reminder, false);

                if (mPagerCapable != null) {
                    mPagerCapable.setPagerItem(PAGER_ITEM_REMINDERS);
                }
            }
        });
        f.show(fm, f.toString());
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
