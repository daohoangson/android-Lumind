package com.daohoangson.lumind.utils;

import android.support.annotation.NonNull;

import com.daohoangson.lumind.model.Lumindate;

import java.util.Calendar;
import java.util.Date;

import de.unileipzig.informatik.duc.amlich.VietCalendar;

public class NextOccurrence {

    public static Date calculate(@NonNull Calendar since, Lumindate date, boolean solar, boolean monthly) {
        if (solar) {
            return solar(since, date, monthly);
        } else {
            return lunar(since, date, monthly);
        }
    }

    private static Date solar(@NonNull Calendar since, Lumindate date, boolean monthly) {
        int calendarMonth = since.get(Calendar.MONTH);
        int calendarYear = since.get(Calendar.YEAR);
        Date now = since.getTime();

        Calendar c = (Calendar) since.clone();
        c.set(Calendar.DATE, 1);
        c.set(Calendar.MONTH, date.solarMonth.get());
        c.set(Calendar.YEAR, date.solarYear.get());
        int unit;

        if (monthly) {
            unit = Calendar.MONTH;
            c.set(Calendar.MONTH, calendarMonth);
            c.set(Calendar.YEAR, calendarYear);
        } else {
            unit = Calendar.YEAR;
            c.set(Calendar.YEAR, calendarYear);
        }

        int solarDayInt = date.solarDay.get();
        c.set(Calendar.DATE, Math.min(solarDayInt, c.getActualMaximum(Calendar.DAY_OF_MONTH)));

        c.add(unit, -2);
        while (!c.getTime().after(now)) {
            c.add(unit, 1);
        }

        if (c.get(Calendar.DATE) < solarDayInt) {
            c.set(Calendar.DATE, Math.min(solarDayInt, c.getActualMaximum(Calendar.DAY_OF_MONTH)));
        }

        return c.getTime();
    }

    private static Date lunar(@NonNull Calendar since, Lumindate date, boolean monthly) {
        Calendar c = (Calendar) since.clone();

        while (true) {
            c.add(Calendar.DATE, 1);

            double timeZoneOffset = Lumindate.getTimeZoneOffset();
            int[] tmpLunar = VietCalendar.convertSolar2Lunar(c.get(Calendar.DATE),
                    c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR), timeZoneOffset);
            int daysInMonth = VietCalendar.getLunarDaysInMonth(tmpLunar[1],
                    tmpLunar[2], tmpLunar[3], timeZoneOffset);
            int dayToCompare = Math.min(daysInMonth, date.lunarDay.get());

            if (monthly) {
                if (tmpLunar[0] == dayToCompare) {
                    return c.getTime();
                }
            } else {
                if (tmpLunar[0] == dayToCompare && tmpLunar[1] == date.getLunarMonth().value + 1) {
                    return c.getTime();
                }
            }
        }
    }
}
