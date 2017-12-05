package com.daohoangson.lumind.utils;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.daohoangson.lumind.R;
import com.daohoangson.lumind.model.Lumindate;
import com.daohoangson.lumind.model.LunarMonth;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class StringUtil {

    public static String formatDate(Context context, Lumindate date, boolean solar, boolean monthly) {
        if (solar) {
            if (monthly) {
                return context.getString(R.string.reminder_solar_day_x, date.solarDay.get());
            }

            Calendar c = new GregorianCalendar(date.solarYear.get(), date.solarMonth.get(), date.solarDay.get());
            return DateUtils.formatDateTime(context, c.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
        }

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
