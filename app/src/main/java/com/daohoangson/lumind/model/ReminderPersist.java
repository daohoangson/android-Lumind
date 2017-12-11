package com.daohoangson.lumind.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class ReminderPersist extends RealmObject {

    private static final String DATA_NAME = "name";
    private static final String DATA_NOTE = "note";
    private static final String DATA_RECURRENCE = "recurrence";
    private static final String DATA_WHEN0 = "when0";
    private static final String DATA_WHEN1 = "when1";
    private static final String DATA_WHEN7 = "when7";

    @SuppressWarnings("CanBeFinal")
    @PrimaryKey
    public String uuid;

    public boolean enabled = true;
    public long timeInMillis;
    private int type = Type.USER_CREATED.ordinal();

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
        return getRecurrence() != Recurrence.ANNUALLY;
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

    Type getType() {
        return Type.values()[type];
    }

    public List<Integer> getWhen() {
        List<Integer> when = new ArrayList<>();

        Object when0 = getData(DATA_WHEN0);
        if (when0 == null || (Boolean) when0) {
            // when0 = true, by default
            when.add(0);
        }

        Object when1 = getData(DATA_WHEN1);
        if (when1 != null && (Boolean) when1) {
            when.add(1);
        }

        Object when7 = getData(DATA_WHEN7);
        if (when7 != null && (Boolean) when7) {
            when.add(7);
        }

        return when;
    }

    public ReminderPersist with(Recurrence recurrence) {
        return withData(DATA_RECURRENCE, recurrence.name());
    }

    public ReminderPersist with(Type sc) {
        switch (sc) {
            case THE_FIRST:
                withDate(new Lumindate(1, 0));
                break;
            case THE_FIFTEENTH:
                withDate(new Lumindate(15, 0));
                break;
            case VESAK:
                withDate(new Lumindate(14, 3));
                with(Recurrence.ANNUALLY);
                withEnabled(false);
                break;
            default:
                throw new IllegalArgumentException();
        }

        type = sc.ordinal();

        return this;
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

    ReminderPersist withWhen0(boolean enabled) {
        return withData(DATA_WHEN0, enabled);
    }

    ReminderPersist withWhen1(boolean enabled) {
        return withData(DATA_WHEN1, enabled);
    }

    ReminderPersist withWhen7(boolean enabled) {
        return withData(DATA_WHEN7, enabled);
    }

    private void ensureDataObj() {
        if (mDataObj == null) {
            if (data != null) {
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

    public enum Recurrence {
        MONTHLY,
        ANNUALLY
    }

    public enum Type implements Serializable {
        USER_CREATED,
        THE_FIRST,
        THE_FIFTEENTH,
        VESAK,
    }
}
