package com.daohoangson.lumind.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.daohoangson.lumind.fragment.SettingsFragment;

public class PrefUtil {
    public static String getLanguage(Context context) {
        return getShared(context).getString(SettingsFragment.PREF_LANGUAGE, SettingsFragment.PREF_LANGUAGE_DEFAULT);
    }

    public static boolean getRemind(Context context) {
        return getShared(context).getBoolean(SettingsFragment.PREF_REMIND, false);
    }

    private static SharedPreferences getShared(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
