package com.daohoangson.lumind.model;

import android.content.Context;
import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.daohoangson.lumind.R;
import com.daohoangson.lumind.utils.NextOccurrence;
import com.daohoangson.lumind.utils.StringUtil;

import java.util.Calendar;
import java.util.List;

public class Reminder implements Parcelable {

    @NonNull
    public String uuid;
    public final Lumindate date;
    public ReminderPersist.Type type;

    @SuppressWarnings("WeakerAccess")
    public final ObservableBoolean monthly = new ObservableBoolean(true);
    @SuppressWarnings("WeakerAccess")
    public final ObservableField<String> name = new ObservableField<>("");
    @SuppressWarnings("WeakerAccess")
    public final ObservableField<String> note = new ObservableField<>("");
    public final ObservableBoolean enabled = new ObservableBoolean(true);

    public final ObservableBoolean when0 = new ObservableBoolean(true);
    public final ObservableBoolean when1 = new ObservableBoolean(false);
    public final ObservableBoolean when7 = new ObservableBoolean(false);

    private Calendar mNextOccurrenceSince = null;
    private Calendar mNextOccurrence = null;

    public Reminder(Lumindate date) {
        uuid = "";
        this.date = new Lumindate(date);
        type = ReminderPersist.Type.USER_CREATED;

        setupCallbacks();
    }

    public Reminder(ReminderPersist persist) {
        uuid = persist.uuid;
        date = new Lumindate(persist.timeInMillis);
        type = persist.getType();

        monthly.set(persist.getMonthly());
        name.set(persist.getName());
        note.set(persist.getNote());
        enabled.set(persist.enabled);

        when0.set(false);
        when1.set(false);
        when7.set(false);
        List<Integer> when = persist.getWhen();
        for (Integer whenX : when) {
            switch (whenX) {
                case 0:
                    when0.set(true);
                    break;
                case 1:
                    when1.set(true);
                    break;
                case 7:
                    when7.set(true);
                    break;
            }
        }

        setupCallbacks();
    }

    private Reminder(Parcel in) {
        uuid = in.readString();
        date = in.readParcelable(Lumindate.class.getClassLoader());
        type = (ReminderPersist.Type) in.readSerializable();

        monthly.set(in.readInt() > 0);
        name.set(in.readString());
        note.set(in.readString());
        enabled.set(in.readInt() > 0);

        when0.set(in.readInt() > 0);
        when1.set(in.readInt() > 0);
        when7.set(in.readInt() > 0);

        setupCallbacks();
    }

    public static final Creator<Reminder> CREATOR = new Creator<Reminder>() {
        @Override
        public Reminder createFromParcel(Parcel in) {
            return new Reminder(in);
        }

        @Override
        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uuid);
        parcel.writeParcelable(date, i);
        parcel.writeSerializable(type);

        parcel.writeInt(monthly.get() ? 1 : 0);
        parcel.writeString(name.get());
        parcel.writeString(note.get());
        parcel.writeInt(enabled.get() ? 1 : 0);

        parcel.writeInt(when0.get() ? 1 : 0);
        parcel.writeInt(when1.get() ? 1 : 0);
        parcel.writeInt(when7.get() ? 1 : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Reminder)) {
            return false;
        }

        Reminder other = (Reminder) obj;

        return !(!TextUtils.equals(uuid, other.uuid) ||
                !date.equals(other.date) ||
                !type.equals(other.type) ||
                monthly.get() != other.monthly.get() ||
                !TextUtils.equals(name.get(), other.name.get()) ||
                !TextUtils.equals(note.get(), other.note.get()) ||
                enabled.get() != other.enabled.get()) ||
                when0.get() != other.when0.get() ||
                when1.get() != other.when1.get() ||
                when7.get() != other.when7.get();
    }

    @Override
    public String toString() {
        return String.format("uuid=%s, date=%s, monthly=%s", uuid, date, monthly.get());
    }

    private void setupCallbacks() {
        Observable.OnPropertyChangedCallback resetNextOccurrenceCallback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                mNextOccurrence = null;
            }
        };

        date.addOnPropertyChangedCallback(resetNextOccurrenceCallback);
        monthly.addOnPropertyChangedCallback(resetNextOccurrenceCallback);
    }

    ReminderPersist build() {
        ReminderPersist persist;
        if (isInsert()) {
            persist = new ReminderPersist();
        } else {
            persist = new ReminderPersist(uuid);
        }

        ReminderPersist.Recurrence recurrence = monthly.get() ?
                ReminderPersist.Recurrence.MONTHLY :
                ReminderPersist.Recurrence.ANNUALLY;

        return persist.withDate(date)
                .with(type)
                .with(recurrence)
                .withName(name.get())
                .withNote(note.get())
                .withEnabled(enabled.get())
                .withWhen0(when0.get())
                .withWhen1(when1.get())
                .withWhen7(when7.get());
    }

    public String getNameForShow(Context context) {
        String name = this.name.get();
        if (!TextUtils.isEmpty(name)) {
            return name;
        }

        switch (this.type) {
            case THE_FIRST:
                return context.getString(R.string.reminder_default_daily_first);
            case THE_FIFTEENTH:
                return context.getString(R.string.reminder_default_daily_fifteenth);
            case VESAK:
                return context.getString(R.string.reminder_default_vesak);
        }

        return StringUtil.formatDate(context, this.date, monthly.get());
    }

    public Calendar getNextOccurrence(@NonNull Calendar since) {
        if (mNextOccurrence != null) {
            if (mNextOccurrenceSince == null) {
                mNextOccurrence = null;
            } else {
                if (!mNextOccurrenceSince.equals(since)) {
                    mNextOccurrence = null;
                }
            }
        }

        if (mNextOccurrence == null) {
            mNextOccurrence = NextOccurrence.lunar(since, date, monthly.get());
            mNextOccurrenceSince = (Calendar) since.clone();
        }

        return mNextOccurrence;
    }

    public boolean isInsert() {
        return TextUtils.isEmpty(uuid);
    }

    public boolean isSystem() {
        return type != ReminderPersist.Type.USER_CREATED;
    }

    public void sync(Reminder other) {
        uuid = other.uuid;
        date.setTimeInMillis(other.date.getTimeInMillis());
        type = other.type;

        monthly.set(other.monthly.get());
        name.set(other.name.get());
        note.set(other.note.get());
        enabled.set(other.enabled.get());

        when0.set(other.when0.get());
        when1.set(other.when1.get());
        when7.set(other.when7.get());
    }
}
