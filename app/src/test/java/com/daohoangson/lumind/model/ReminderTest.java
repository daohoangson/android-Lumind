package com.daohoangson.lumind.model;

import org.junit.Test;

import java.util.UUID;

import static junit.framework.Assert.assertEquals;

public class ReminderTest {

    @Test
    public void testSyncUuid() {
        Reminder reminder1 = new Reminder(Lumindate.getInstance());
        Reminder reminder2 = new Reminder(Lumindate.getInstance());
        reminder2.uuid = UUID.randomUUID().toString();
        reminder1.sync(reminder2);

        assertEquals(reminder2.uuid, reminder1.uuid);
    }

    @Test
    public void testSyncDate() {
        Reminder reminder1 = new Reminder(Lumindate.getInstance());
        Reminder reminder2 = new Reminder(Lumindate.getInstance());
        reminder1.sync(reminder2);

        assertEquals(reminder2.date.getTimeInMillis(), reminder1.date.getTimeInMillis());
    }

    @Test
    public void testSyncType() {
        Reminder reminder1 = new Reminder(Lumindate.getInstance());
        Reminder reminder2 = new Reminder(Lumindate.getInstance());
        ReminderPersist.Type vesak = ReminderPersist.Type.VESAK;
        reminder2.type = vesak;
        reminder1.sync(reminder2);

        assertEquals(vesak, reminder1.type);
    }

    @Test
    public void testSyncMonthly() {
        for (int i = 0; i < 2; i++) {
            Reminder reminder1 = new Reminder(Lumindate.getInstance());
            Reminder reminder2 = new Reminder(Lumindate.getInstance());
            boolean monthly = i > 0;
            reminder2.monthly.set(monthly);
            reminder1.sync(reminder2);

            assertEquals(monthly, reminder1.monthly.get());
        }
    }

    @Test
    public void testSyncName() {
        Reminder reminder1 = new Reminder(Lumindate.getInstance());
        Reminder reminder2 = new Reminder(Lumindate.getInstance());
        String name = UUID.randomUUID().toString();
        reminder2.name.set(name);
        reminder1.sync(reminder2);

        assertEquals(name, reminder1.name.get());
    }

    @Test
    public void testSyncNote() {
        Reminder reminder1 = new Reminder(Lumindate.getInstance());
        Reminder reminder2 = new Reminder(Lumindate.getInstance());
        String note = UUID.randomUUID().toString();
        reminder2.note.set(note);
        reminder1.sync(reminder2);

        assertEquals(note, reminder1.note.get());
    }

    @Test
    public void testSyncEnabled() {
        for (int i = 0; i < 2; i++) {
            Reminder reminder1 = new Reminder(Lumindate.getInstance());
            Reminder reminder2 = new Reminder(Lumindate.getInstance());
            boolean enabled = i > 0;
            reminder2.enabled.set(enabled);
            reminder1.sync(reminder2);

            assertEquals(enabled, reminder1.enabled.get());
        }
    }

    @Test
    public void testSyncWhen0() {
        for (int i = 0; i < 2; i++) {
            Reminder reminder1 = new Reminder(Lumindate.getInstance());
            Reminder reminder2 = new Reminder(Lumindate.getInstance());
            boolean when0 = i > 0;
            reminder2.when0.set(when0);
            reminder1.sync(reminder2);

            assertEquals(when0, reminder1.when0.get());
        }
    }

    @Test
    public void testSyncWhen1() {
        for (int i = 0; i < 2; i++) {
            Reminder reminder1 = new Reminder(Lumindate.getInstance());
            Reminder reminder2 = new Reminder(Lumindate.getInstance());
            boolean when1 = i > 0;
            reminder2.when1.set(when1);
            reminder1.sync(reminder2);

            assertEquals(when1, reminder1.when1.get());
        }
    }

    @Test
    public void testSyncWhen7() {
        for (int i = 0; i < 2; i++) {
            Reminder reminder1 = new Reminder(Lumindate.getInstance());
            Reminder reminder2 = new Reminder(Lumindate.getInstance());
            boolean when7 = i > 0;
            reminder2.when7.set(when7);
            reminder1.sync(reminder2);

            assertEquals(when7, reminder1.when7.get());
        }
    }
}