package com.daohoangson.lumind.model;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * @author sondh
 */
public class LunarReminderTest {

    @Test
    public void testGetNextOccurrenceShouldPickTheSameDayNextMonth() {
        Reminder monthly20160101 = create(new int[]{1, 0, 2016, 0}, true);
        test(monthly20160101, new int[]{31, 11, 2015}, new int[]{10, 0, 2016});
        test(monthly20160101, new int[]{31, 0, 2016}, new int[]{8, 1, 2016});
        test(monthly20160101, new int[]{28, 1, 2016}, new int[]{9, 2, 2016});
        test(monthly20160101, new int[]{31, 2, 2016}, new int[]{7, 3, 2016});
        test(monthly20160101, new int[]{30, 3, 2016}, new int[]{7, 4, 2016});
        test(monthly20160101, new int[]{31, 4, 2016}, new int[]{5, 5, 2016});
        test(monthly20160101, new int[]{30, 5, 2016}, new int[]{4, 6, 2016});
        test(monthly20160101, new int[]{31, 6, 2016}, new int[]{3, 7, 2016});
        test(monthly20160101, new int[]{31, 7, 2016}, new int[]{1, 8, 2016});
        test(monthly20160101, new int[]{30, 8, 2016}, new int[]{1, 9, 2016});
        test(monthly20160101, new int[]{30, 10, 2016}, new int[]{29, 11, 2016});
    }

    @Test
    public void testGetNextOccurrenceShouldPickTheNextDayEveryMonth() {
        Reminder monthly20160115 = create(new int[]{15, 0, 2016, 0}, true);
        test(monthly20160115, new int[]{23, 0, 2016}, new int[]{24, 0, 2016});
        test(monthly20160115, new int[]{21, 1, 2016}, new int[]{22, 1, 2016});
        test(monthly20160115, new int[]{22, 2, 2016}, new int[]{23, 2, 2016});
        test(monthly20160115, new int[]{20, 3, 2016}, new int[]{21, 3, 2016});
        test(monthly20160115, new int[]{20, 4, 2016}, new int[]{21, 4, 2016});
        test(monthly20160115, new int[]{18, 5, 2016}, new int[]{19, 5, 2016});
        test(monthly20160115, new int[]{17, 6, 2016}, new int[]{18, 6, 2016});
        test(monthly20160115, new int[]{16, 7, 2016}, new int[]{17, 7, 2016});
        test(monthly20160115, new int[]{14, 8, 2016}, new int[]{15, 8, 2016});
        test(monthly20160115, new int[]{14, 9, 2016}, new int[]{15, 9, 2016});
        test(monthly20160115, new int[]{13, 10, 2016}, new int[]{14, 10, 2016});
        test(monthly20160115, new int[]{12, 11, 2016}, new int[]{13, 11, 2016});
    }

    @Test
    public void testGetNextOccurrenceShouldPickTheLastDayEveryMonth() {
        Reminder monthly20160130 = create(new int[]{30, 0, 2016, 0}, true);
        test(monthly20160130, new int[]{1, 0, 2016}, new int[]{9, 0, 2016});
        test(monthly20160130, new int[]{1, 1, 2016}, new int[]{7, 1, 2016});
        test(monthly20160130, new int[]{1, 2, 2016}, new int[]{8, 2, 2016});
        test(monthly20160130, new int[]{1, 3, 2016}, new int[]{6, 3, 2016});
        test(monthly20160130, new int[]{1, 4, 2016}, new int[]{6, 4, 2016});
        test(monthly20160130, new int[]{1, 5, 2016}, new int[]{4, 5, 2016});
        test(monthly20160130, new int[]{1, 6, 2016}, new int[]{3, 6, 2016});
        test(monthly20160130, new int[]{1, 7, 2016}, new int[]{2, 7, 2016});
        test(monthly20160130, new int[]{3, 7, 2016}, new int[]{31, 7, 2016});
        test(monthly20160130, new int[]{1, 8, 2016}, new int[]{30, 8, 2016});
        test(monthly20160130, new int[]{1, 11, 2016}, new int[]{28, 11, 2016});
    }

    @Test
    public void testGetNextOccurrenceShouldPickTheSameDayNextYear() {
        Reminder yearly20160601 = create(new int[]{1, 5, 2016, 0}, false);
        test(yearly20160601, new int[]{1, 0, 2017}, new int[]{24, 5, 2017});
        test(yearly20160601, new int[]{1, 0, 2018}, new int[]{13, 6, 2018});
        test(yearly20160601, new int[]{1, 0, 2019}, new int[]{3, 6, 2019});
        test(yearly20160601, new int[]{1, 0, 2020}, new int[]{21, 6, 2020});
        test(yearly20160601, new int[]{1, 0, 2021}, new int[]{10, 6, 2021});
        test(yearly20160601, new int[]{1, 0, 2022}, new int[]{29, 5, 2022});
        test(yearly20160601, new int[]{1, 0, 2023}, new int[]{18, 6, 2023});
        test(yearly20160601, new int[]{1, 0, 2024}, new int[]{6, 6, 2024});
        test(yearly20160601, new int[]{1, 0, 2025}, new int[]{25, 5, 2025});
        test(yearly20160601, new int[]{1, 0, 2026}, new int[]{14, 6, 2026});
        test(yearly20160601, new int[]{1, 0, 2027}, new int[]{4, 6, 2027});
        test(yearly20160601, new int[]{1, 0, 2028}, new int[]{22, 6, 2028});
        test(yearly20160601, new int[]{1, 0, 2029}, new int[]{11, 6, 2029});
        test(yearly20160601, new int[]{1, 0, 2030}, new int[]{1, 6, 2030});
    }

    @Test
    public void testGetNextOccurrenceShouldPickTheNextDayEveryYear() {
        Reminder yearly20160601 = create(new int[]{1, 5, 2016, 0}, false);
        test(yearly20160601, new int[]{23, 5, 2017}, new int[]{24, 5, 2017});
        test(yearly20160601, new int[]{12, 6, 2018}, new int[]{13, 6, 2018});
        test(yearly20160601, new int[]{2, 6, 2019}, new int[]{3, 6, 2019});
        test(yearly20160601, new int[]{20, 6, 2020}, new int[]{21, 6, 2020});
        test(yearly20160601, new int[]{9, 6, 2021}, new int[]{10, 6, 2021});
        test(yearly20160601, new int[]{28, 5, 2022}, new int[]{29, 5, 2022});
        test(yearly20160601, new int[]{17, 6, 2023}, new int[]{18, 6, 2023});
        test(yearly20160601, new int[]{5, 6, 2024}, new int[]{6, 6, 2024});
        test(yearly20160601, new int[]{24, 5, 2025}, new int[]{25, 5, 2025});
        test(yearly20160601, new int[]{13, 6, 2026}, new int[]{14, 6, 2026});
        test(yearly20160601, new int[]{3, 6, 2027}, new int[]{4, 6, 2027});
        test(yearly20160601, new int[]{21, 6, 2028}, new int[]{22, 6, 2028});
        test(yearly20160601, new int[]{10, 6, 2029}, new int[]{11, 6, 2029});
        test(yearly20160601, new int[]{30, 5, 2030}, new int[]{1, 6, 2030});
    }

    private Reminder create(int[] values, boolean monthly) {
        Reminder reminder = new Reminder();
        reminder.setSolar(false);
        reminder.setMonthly(monthly);
        reminder.date.setLunarDate(values[0], values[1], values[2]);
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