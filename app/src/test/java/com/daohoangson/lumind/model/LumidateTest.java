package com.daohoangson.lumind.model;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class LumidateTest {

    @Test
    public void testConstructorLumidate() {
        Lumindate date1 = Lumindate.getInstance();
        Lumindate date2 = new Lumindate(date1);
        Lumindate date3 = new Lumindate(date2);
        Lumindate date4 = new Lumindate(date3);

        assertEquals(date1.getTimeInMillis(), date4.getTimeInMillis());
    }
}