package com.daohoangson.lumind.utils;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.daohoangson.lumind.R;

import java.util.Calendar;
import java.util.Date;

public class StringUtil {

    @NonNull
    public static String formatNextOccurrenceInX(Resources r, Calendar calendar, Date nextDate) {
        long today = calendar.getTimeInMillis();
        long next = nextDate.getTime();
        long durationInSec = (next - today) / 1000;
        int days = (int) Math.ceil(durationInSec / 86400.0);
        int months = (int) Math.ceil(days / 31.0);

        if (days <= 0) {
            return "";
        }

        if (months > 1) {
            return r.getString(R.string.next_occurrence_in_x, r.getQuantityString(R.plurals.x_months, months, months));
        }

        switch (days) {
            case 1:
                return r.getString(R.string.next_occurrence_tomorrow);
            case 2:
                return r.getString(R.string.next_occurrence_day_after_tomorrow);
            default:
                return r.getString(R.string.next_occurrence_in_x, r.getQuantityString(R.plurals.x_days, days, days));
        }


    }
}
