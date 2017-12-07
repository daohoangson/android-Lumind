package com.daohoangson.lumind;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.daohoangson.lumind.model.Lumindate;
import com.daohoangson.lumind.model.Reminder;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ParcelableTest {

    @Test
    public void testLumidate() {
        Lumindate date1 = Lumindate.getInstance();
        Parcel parcel = Parcel.obtain();
        date1.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Lumindate date2 = Lumindate.CREATOR.createFromParcel(parcel);

        assertEquals(date1, date2);
    }


    @Test
    public void testReminder() {
        Reminder reminder1 = new Reminder(Lumindate.getInstance());
        Parcel parcel = Parcel.obtain();
        reminder1.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Reminder reminder2 = Reminder.CREATOR.createFromParcel(parcel);

        assertEquals(reminder1, reminder2);
    }

}
