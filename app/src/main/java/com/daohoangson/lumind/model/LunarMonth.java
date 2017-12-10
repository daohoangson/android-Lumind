package com.daohoangson.lumind.model;

import android.content.Context;

import com.daohoangson.lumind.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.unileipzig.informatik.duc.amlich.VietCalendar;

public class LunarMonth {
    public static List<LunarMonth> getLunarMonths(int lunarYear, Context context, int labelStyle) {
        ArrayList<LunarMonth> months = new ArrayList<>();

        final Map<String, Integer> displayNames;
        if (context != null && labelStyle > 0) {
            Calendar calendar = Calendar.getInstance();
            displayNames = calendar.getDisplayNames(Calendar.MONTH, labelStyle, Locale.getDefault());
        } else {
            int numOfMonths = 12;
            displayNames = new HashMap<>(numOfMonths);
            for (int i = 0; i < numOfMonths; i++) {
                displayNames.put(String.valueOf(i + 1), i);
            }
        }
        Integer lunarLeapMonth = VietCalendar.getLunarLeapMonthOrNull(
                lunarYear, Lumindate.getTimeZoneOffset());

        if (lunarLeapMonth == null) {
            for (int i = 0; i < 12; i++) {
                addMonth(months, i, 0, displayNames, context);
            }
        } else {
            for (int i = 0; i < lunarLeapMonth; i++) {
                addMonth(months, i, 0, displayNames, context);
            }

            addMonth(months, lunarLeapMonth - 1, 1, displayNames, context);

            for (int i = lunarLeapMonth; i < 12; i++) {
                addMonth(months, i, 0, displayNames, context);
            }
        }

        return months;
    }

    public static List<LunarMonth> getLunarMonths(int lunarYear) {
        return getLunarMonths(lunarYear, null, 0);
    }

    private static void addMonth(ArrayList<LunarMonth> months, int value, int leap, Map<String, Integer> displayNames, Context context) {
        LunarMonth m = new LunarMonth();
        m.value = value;
        m.leap = leap;

        if (displayNames != null) {
            for (Map.Entry<String, Integer> displayName : displayNames.entrySet()) {
                if (displayName.getValue() == m.value) {
                    String label = displayName.getKey();
                    if (m.leap > 0) {
                        if (context != null) {
                            label = context.getString(R.string.month_short_x_leap, label);
                        } else {
                            label += "*";
                        }
                    }

                    m.label = label;
                }
            }
        }

        months.add(m);
    }

    public int value = -1;
    public int leap = 0;
    public String label = null;
}
