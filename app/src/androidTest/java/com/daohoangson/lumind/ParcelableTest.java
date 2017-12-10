package com.daohoangson.lumind;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.daohoangson.lumind.model.Lumindate;
import com.daohoangson.lumind.model.Reminder;
import com.daohoangson.lumind.model.ReminderPersist;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ParcelableTest {

    @Test
    public void testLumidateDefault() {
        Lumindate date1 = Lumindate.getInstance();
        Lumindate date2 = doIt(date1, Lumindate.CREATOR);

        assertEquals(date1, date2);
    }

    @Test
    public void testLumidateYesterday() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1);

        Lumindate date1 = new Lumindate(c.getTimeInMillis());
        Lumindate date2 = doIt(date1, Lumindate.CREATOR);

        assertEquals(date1, date2);
    }

    @Test
    public void testReminderDefault() {
        Reminder reminder1 = new Reminder(Lumindate.getInstance());
        Reminder reminder2 = doIt(reminder1, Reminder.CREATOR);

        assertEquals(reminder1, reminder2);
    }

    @Test
    public void testReminderExistingUuid() {
        final String uuid = UUID.randomUUID().toString();
        Reminder reminder1 = new Reminder(Lumindate.getInstance());
        reminder1.uuid = uuid;
        Reminder reminder2 = doIt(reminder1, Reminder.CREATOR);

        assertEquals(uuid, reminder2.uuid);
    }

    @Test
    public void testReminderDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1);
        Lumindate date = new Lumindate(c.getTimeInMillis());
        Reminder reminder1 = new Reminder(date);
        Reminder reminder2 = doIt(reminder1, Reminder.CREATOR);

        assertEquals(date.getTimeInMillis(), reminder2.date.getTimeInMillis());
    }

    @Test
    public void testReminderType() {
        final ReminderPersist.Type typeVesak = ReminderPersist.Type.VESAK;
        Reminder reminder1 = new Reminder(Lumindate.getInstance());
        reminder1.type = typeVesak;
        Reminder reminder2 = doIt(reminder1, Reminder.CREATOR);

        assertEquals(typeVesak, reminder2.type);
    }

    @Test
    public void testReminderMonthly() {
        Reminder reminder1 = new Reminder(Lumindate.getInstance());
        reminder1.monthly.set(true);
        Reminder reminder2 = doIt(reminder1, Reminder.CREATOR);

        assertEquals(true, reminder2.monthly.get());
    }

    @Test
    public void testReminderAnnually() {
        Reminder reminder1 = new Reminder(Lumindate.getInstance());
        reminder1.monthly.set(false);
        Reminder reminder2 = doIt(reminder1, Reminder.CREATOR);

        assertEquals(false, reminder2.monthly.get());
    }

    @Test
    public void testReminderName() {
        final String name = UUID.randomUUID().toString();
        Reminder reminder1 = new Reminder(Lumindate.getInstance());
        reminder1.name.set(name);
        Reminder reminder2 = doIt(reminder1, Reminder.CREATOR);

        assertEquals(name, reminder2.name.get());
    }

    @Test
    public void testReminderNote() {
        final String note = UUID.randomUUID().toString();
        Reminder reminder1 = new Reminder(Lumindate.getInstance());
        reminder1.note.set(note);
        Reminder reminder2 = doIt(reminder1, Reminder.CREATOR);

        assertEquals(note, reminder2.note.get());
    }

    @Test
    public void testReminderEnabled() {
        Reminder reminder1 = new Reminder(Lumindate.getInstance());
        reminder1.enabled.set(true);
        Reminder reminder2 = doIt(reminder1, Reminder.CREATOR);

        assertEquals(true, reminder2.enabled.get());
    }

    @Test
    public void testReminderDisabled() {
        Reminder reminder1 = new Reminder(Lumindate.getInstance());
        reminder1.enabled.set(false);
        Reminder reminder2 = doIt(reminder1, Reminder.CREATOR);

        assertEquals(false, reminder2.enabled.get());
    }

    private <T extends Parcelable> T doIt(T original, Parcelable.Creator<T> creator) {
        Parcel parcel = Parcel.obtain();
        original.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        return creator.createFromParcel(parcel);
    }
}
