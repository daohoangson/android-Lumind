package com.daohoangson.lumind.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.daohoangson.lumind.R;
import com.daohoangson.lumind.schedule.AlarmReceiver;
import com.daohoangson.lumind.schedule.ReminderEngine;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String PREF_REMIND = "pref_remind";
    public static final String PREF_REMIND_HOW_FAR = "pref_remind_how_far";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PREF_REMIND.equals(key)) {
            boolean prefRemind = sharedPreferences.getBoolean(key, false);
            Context context = getContext();

            if (prefRemind) {
                AlarmReceiver.setup(context);
                ReminderEngine.remind(context);
            } else {
                AlarmReceiver.cancel(context);
            }
        }
    }
}
