package com.daohoangson.lumind.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * @author sondh
 */
@SuppressWarnings({"WeakerAccess", "CanBeFinal"})
public class ReminderPersist extends RealmObject {
    private static final String DATA_NOTE = "note";
    private static final String DATA_RECURRENCE = "recurrence";

    @PrimaryKey
    public String uuid;

    public int day;
    public int month;
    public int year;
    public boolean solar;

    public String name;
    public boolean enabled = true;
    String data;

    @Ignore
    private JSONObject mDataObj;

    public ReminderPersist() {
        uuid = UUID.randomUUID().toString();
    }

    ReminderPersist(String uuid) {
        this.uuid = uuid;
    }

    ReminderPersist withDate(@NonNull Lumindate date, boolean solar) {
        if (solar) {
            day = date.solarDay.get();
            month = date.solarMonth.get();
            year = date.solarYear.get();
            this.solar = true;
        } else {
            day = date.lunarDay.get();
            month = date.getLunarMonth().value;
            year = date.lunarYear.get();
            this.solar = false;
        }

        return this;
    }

    ReminderPersist withName(String name) {
        this.name = name;
        return this;
    }

    ReminderPersist withNote(String note) {
        return withData(DATA_NOTE, note);
    }

    ReminderPersist withRecurrence(Recurrence recurrence) {
        return withData(DATA_RECURRENCE, recurrence.name());
    }

    ReminderPersist withEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    String getNote() {
        Object note = getData(DATA_NOTE);
        if (note == null) {
            return "";
        }

        return (String) note;
    }

    private Recurrence getRecurrence() {
        Object recurrenceData = getData(DATA_RECURRENCE);
        if (Recurrence.MONTHLY.name().equals(recurrenceData)) {
            return Recurrence.MONTHLY;
        }
        if (Recurrence.ANNUALLY.name().equals(recurrenceData)) {
            return Recurrence.ANNUALLY;
        }
        return null;
    }

    public boolean getMonthly() {
        return getRecurrence() == Recurrence.MONTHLY;
    }

    enum Recurrence {
        MONTHLY,
        ANNUALLY
    }

    private ReminderPersist withData(String name, Object value) {
        ensureDataObj();

        try {
            mDataObj.put(name, value);
        } catch (JSONException e) {
            // TODO
        }

        data = mDataObj.toString();

        return this;
    }

    private Object getData(String name) {
        ensureDataObj();

        if (mDataObj == null) {
            return null;
        }

        if (!mDataObj.has(name)) {
            return null;
        }

        try {
            return mDataObj.get(name);
        } catch (JSONException e) {
            return null;
        }
    }

    private void ensureDataObj() {
        if (mDataObj == null) {
            if (!TextUtils.isEmpty(data)) {
                try {
                    mDataObj = new JSONObject(data);
                } catch (JSONException e) {
                    // TODO
                }
            }

            if (mDataObj == null) {
                mDataObj = new JSONObject();
            }
        }
    }
}
