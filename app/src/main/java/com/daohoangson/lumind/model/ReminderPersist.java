package com.daohoangson.lumind.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class ReminderPersist extends RealmObject {

    private static final String DATA_NAME = "name";
    private static final String DATA_NOTE = "note";
    private static final String DATA_RECURRENCE = "recurrence";

    @SuppressWarnings("CanBeFinal")
    @PrimaryKey
    public String uuid;

    public boolean enabled = true;
    public long timeInMillis;

    private String data;

    @Ignore
    private JSONObject mDataObj;

    public ReminderPersist() {
        uuid = UUID.randomUUID().toString();
    }

    ReminderPersist(String uuid) {
        this.uuid = uuid;
    }

    public boolean getMonthly() {
        return getRecurrence() == Recurrence.MONTHLY;
    }

    @NonNull
    public String getName() {
        Object name = getData(DATA_NAME);
        if (name == null) {
            return "";
        }

        return (String) name;
    }

    @NonNull
    String getNote() {
        Object note = getData(DATA_NOTE);
        if (note == null) {
            return "";
        }

        return (String) note;
    }

    ReminderPersist withDate(@NonNull Lumindate date) {
        timeInMillis = date.getTimeInMillis();

        return this;
    }

    ReminderPersist withEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    ReminderPersist withName(String name) {
        return withData(DATA_NAME, name);
    }

    ReminderPersist withNote(String note) {
        return withData(DATA_NOTE, note);
    }

    ReminderPersist withRecurrence(Recurrence recurrence) {
        return withData(DATA_RECURRENCE, recurrence.name());
    }

    enum Recurrence {
        MONTHLY,
        ANNUALLY
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

    @Nullable
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

    @Nullable
    private Recurrence getRecurrence() {
        Object data = getData(DATA_RECURRENCE);
        if (Recurrence.MONTHLY.name().equals(data)) {
            return Recurrence.MONTHLY;
        }
        if (Recurrence.ANNUALLY.name().equals(data)) {
            return Recurrence.ANNUALLY;
        }

        return null;
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
}
