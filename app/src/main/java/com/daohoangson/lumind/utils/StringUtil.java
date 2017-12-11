package com.daohoangson.lumind.utils;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;

import com.daohoangson.lumind.R;
import com.daohoangson.lumind.model.Lumindate;
import com.daohoangson.lumind.model.LunarMonth;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class StringUtil {

    public static String formatDate(Context context, Lumindate date, boolean monthly) {
        if (monthly) {
            return context.getString(R.string.reminder_lunar_day_x, date.lunarDay.get());
        }

        LunarMonth lm = date.getLunarMonth();
        Calendar c = new GregorianCalendar(date.lunarYear.get(), lm.value, date.lunarDay.get());
        String s = DateUtils.formatDateTime(context, c.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
        if (lm.leap > 0) {
            return context.getString(R.string.reminder_lunar_date_x_leap, s);
        } else {
            return context.getString(R.string.reminder_lunar_date_x, s);
        }
    }

    public static int calculateDays(Calendar since, Calendar calendar) {
        long t1 = since.getTimeInMillis();
        long t2 = calendar.getTimeInMillis();

        return (int) Math.ceil((t2 - t1) / 1000 / 86400.0);
    }

    @Nullable
    public static String formatNextOccurrenceWhen(Resources r, int days) {
        if (days < 0) {
            return null;
        }

        int months = (int) Math.ceil(days / 31.0);
        if (months > 1) {
            return r.getQuantityString(R.plurals.x_months, months, months);
        }

        switch (days) {
            case 0:
                return r.getString(R.string.next_occurrence_when_today);
            case 1:
                return r.getString(R.string.next_occurrence_when_tomorrow);
            case 2:
                return r.getString(R.string.next_occurrence_when_day_after_tomorrow);
            case 7:
                return r.getString(R.string.next_occurrence_when_in_a_week);
            default:
                return r.getQuantityString(R.plurals.x_days, days, days);
        }
    }
}
