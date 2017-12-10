package com.daohoangson.lumind.utils;

import android.support.annotation.NonNull;

import com.daohoangson.lumind.model.Lumindate;

import java.util.Calendar;

import de.unileipzig.informatik.duc.amlich.VietCalendar;

public class NextOccurrence {

    public static Calendar lunar(@NonNull Calendar since, Lumindate date, boolean monthly) {
        Calendar c = (Calendar) since.clone();
        double timeZoneOffset = Lumindate.getTimeZoneOffset();

        while (true) {
            c.add(Calendar.DATE, 1);

            int[] tmpLunar = VietCalendar.convertSolar2Lunar(c.get(Calendar.DATE),
                    c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR), timeZoneOffset);
            int daysInMonth = VietCalendar.getLunarDaysInMonth(tmpLunar[1],
                    tmpLunar[2], tmpLunar[3], timeZoneOffset);
            int dayToCompare = Math.min(daysInMonth, date.lunarDay.get());

            if (monthly) {
                if (tmpLunar[0] == dayToCompare) {
                    return c;
                }
            } else {
                if (tmpLunar[0] == dayToCompare && tmpLunar[1] == date.getLunarMonth().value + 1) {
                    return c;
                }
            }
        }
    }
}
