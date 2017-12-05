package com.daohoangson.lumind.model;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * @author sondh
 */
public class SolarReminderTest {

    @Test
    public void testGetNextOccurrenceShouldPickTheSameDayNextMonth() {
        Reminder monthly20160101 = create(new int[]{1, 0, 2016}, true);
        for (int i = 0; i < 12; i++) {
            test(monthly20160101, new int[]{2, i, 2017},
                    new int[]{1, (i + 1) % 12, i == 11 ? 2018 : 2017});
        }
    }

    @Test
    public void testGetNextOccurrenceShouldPickTheNextDayEveryMonth() {
        Reminder monthly20160115 = create(new int[]{15, 0, 2016}, true);
        for (int i = 0; i < 12; i++) {
            test(monthly20160115, new int[]{14, i, 2017}, new int[]{15, i, 2017});
        }
    }

    @Test
    public void testGetNextOccurrenceShouldPickTheLastDayEveryMonth() {
        Reminder monthly20160130 = create(new int[]{31, 0, 2016}, true);
        test(monthly20160130, new int[]{1, 0, 2017}, new int[]{31, 0, 2017});
        test(monthly20160130, new int[]{1, 1, 2017}, new int[]{28, 1, 2017});
        test(monthly20160130, new int[]{1, 2, 2017}, new int[]{31, 2, 2017});
        test(monthly20160130, new int[]{1, 3, 2017}, new int[]{30, 3, 2017});
        test(monthly20160130, new int[]{1, 4, 2017}, new int[]{31, 4, 2017});
        test(monthly20160130, new int[]{1, 5, 2017}, new int[]{30, 5, 2017});
        test(monthly20160130, new int[]{1, 6, 2017}, new int[]{31, 6, 2017});
        test(monthly20160130, new int[]{1, 7, 2017}, new int[]{31, 7, 2017});
        test(monthly20160130, new int[]{1, 8, 2017}, new int[]{30, 8, 2017});
        test(monthly20160130, new int[]{1, 9, 2017}, new int[]{31, 9, 2017});
        test(monthly20160130, new int[]{1, 10, 2017}, new int[]{30, 10, 2017});
        test(monthly20160130, new int[]{1, 11, 2017}, new int[]{31, 11, 2017});
    }

    @Test
    public void testGetNextOccurrenceShouldPickTheSameDayNextYear() {
        Reminder yearly20160601 = create(new int[]{1, 5, 2016}, false);
        for (int i = 0; i < 12; i++) {
            test(yearly20160601, new int[]{31, 12, 2016 + i}, new int[]{1, 5, 2017 + i});
        }
    }

    @Test
    public void testGetNextOccurrenceShouldPickTheNextDayEveryYear() {
        Reminder yearly20160615 = create(new int[]{15, 6, 2016}, false);
        for (int i = 0; i < 12; i++) {
            test(yearly20160615, new int[]{14, 6, 2017 + i}, new int[]{15, 6, 2017 + i});
        }
    }

    @Test
    public void testGetNextOccurrenceShouldPickTheLastDayEveryFeb() {
        Reminder yearly20160229 = create(new int[]{29, 1, 2016}, false);
        test(yearly20160229, new int[]{1, 0, 2017}, new int[]{28, 1, 2017});
        test(yearly20160229, new int[]{1, 0, 2018}, new int[]{28, 1, 2018});
        test(yearly20160229, new int[]{1, 0, 2019}, new int[]{28, 1, 2019});
        test(yearly20160229, new int[]{1, 0, 2020}, new int[]{29, 1, 2020});
        test(yearly20160229, new int[]{1, 0, 2021}, new int[]{28, 1, 2021});
        test(yearly20160229, new int[]{1, 0, 2022}, new int[]{28, 1, 2022});
        test(yearly20160229, new int[]{1, 0, 2023}, new int[]{28, 1, 2023});
        test(yearly20160229, new int[]{1, 0, 2024}, new int[]{29, 1, 2024});
        test(yearly20160229, new int[]{1, 0, 2025}, new int[]{28, 1, 2025});
        test(yearly20160229, new int[]{1, 0, 2026}, new int[]{28, 1, 2026});
        test(yearly20160229, new int[]{1, 0, 2027}, new int[]{28, 1, 2027});
        test(yearly20160229, new int[]{1, 0, 2028}, new int[]{29, 1, 2028});
        test(yearly20160229, new int[]{1, 0, 2029}, new int[]{28, 1, 2029});
        test(yearly20160229, new int[]{1, 0, 2030}, new int[]{28, 1, 2030});
    }

    private Reminder create(int[] values, boolean monthly) {
        Reminder reminder = new Reminder();
        reminder.setSolar(true);
        reminder.setMonthly(monthly);
        reminder.date.setSolarDate(values[0], values[1], values[2]);
        return reminder;
    }

    private void test(Reminder r, int[] values, int[] expected) {
        Calendar cTest = Calendar.getInstance();
        cTest.set(Calendar.DATE, values[0]);
        cTest.set(Calendar.MONTH, values[1]);
        cTest.set(Calendar.YEAR, values[2]);
        Date next = r.getNextOccurrence(cTest);

        Calendar cNext = Calendar.getInstance();
        cNext.setTime(next);
        int[] actual = new int[]{cNext.get(Calendar.DATE), cNext.get(Calendar.MONTH), cNext.get(Calendar.YEAR)};
        String message = String.format(Locale.US, "[%d, %d, %d] expects [%d, %d, %d]",
                values[0], values[1], values[2],
                expected[0], expected[1], expected[2]);
        assertEquals(message,
                String.format(Locale.US, "[%d, %d, %d]", expected[0], expected[1], expected[2]),
                String.format(Locale.US, "[%d, %d, %d]", actual[0], actual[1], actual[2]));
    }
}