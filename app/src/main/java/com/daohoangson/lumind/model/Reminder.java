package com.daohoangson.lumind.model;

import android.content.Context;
import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.daohoangson.lumind.R;
import com.daohoangson.lumind.utils.NextOccurrence;
import com.daohoangson.lumind.utils.StringUtil;

import java.util.Calendar;

public class Reminder implements Parcelable {

    @NonNull
    final String existingUuid;
    private final Lumindate date;
    private final ReminderPersist.Type type;

    @SuppressWarnings("WeakerAccess")
    public final ObservableInt monthlyOrAnnually = new ObservableInt(R.id.monthly);
    @SuppressWarnings("WeakerAccess")
    public final ObservableField<String> name = new ObservableField<>("");
    @SuppressWarnings("WeakerAccess")
    public final ObservableField<String> note = new ObservableField<>("");
    public final ObservableBoolean enabled = new ObservableBoolean(true);

    private Calendar mNextOccurrenceSince = null;
    private Calendar mNextOccurrence = null;

    public Reminder(Lumindate date) {
        existingUuid = "";
        this.date = new Lumindate(date);
        type = ReminderPersist.Type.USER_CREATED;

        setupCallbacks();
    }

    public Reminder(Reminder other) {
        existingUuid = other.existingUuid;
        date = new Lumindate(other.date);
        type = other.type;

        setMonthly(other.getMonthly());
        name.set(other.name.get());
        note.set(other.note.get());
        enabled.set(other.enabled.get());

        setupCallbacks();
    }

    public Reminder(ReminderPersist persist) {
        existingUuid = persist.uuid;
        date = new Lumindate(persist.timeInMillis);
        type = persist.getType();

        setMonthly(persist.getMonthly());
        name.set(persist.getName());
        note.set(persist.getNote());
        enabled.set(persist.enabled);

        setupCallbacks();
    }

    private Reminder(Parcel in) {
        existingUuid = in.readString();
        date = in.readParcelable(Lumindate.class.getClassLoader());
        type = (ReminderPersist.Type) in.readSerializable();

        setMonthly(in.readInt() > 0);
        name.set(in.readString());
        note.set(in.readString());
        enabled.set(in.readInt() > 0);

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
        parcel.writeString(existingUuid);
        parcel.writeParcelable(date, i);
        parcel.writeSerializable(type);

        parcel.writeInt(getMonthly() ? 1 : 0);
        parcel.writeString(name.get());
        parcel.writeString(note.get());
        parcel.writeInt(enabled.get() ? 1 : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Reminder)) {
            return false;
        }

        Reminder other = (Reminder) obj;

        return !(!TextUtils.equals(existingUuid, other.existingUuid) ||
                !date.equals(other.date) ||
                !type.equals(other.type) ||
                getMonthly() != other.getMonthly() ||
                !TextUtils.equals(name.get(), other.name.get()) ||
                !TextUtils.equals(note.get(), other.note.get()) ||
                enabled.get() != other.enabled.get());
    }

    @Override
    public String toString() {
        return String.format("uuid=%s, date=%s, monthly=%s", existingUuid, date, getMonthly());
    }

    private void setupCallbacks() {
        Observable.OnPropertyChangedCallback resetNextOccurrenceCallback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                mNextOccurrence = null;
            }
        };

        date.addOnPropertyChangedCallback(resetNextOccurrenceCallback);
        monthlyOrAnnually.addOnPropertyChangedCallback(resetNextOccurrenceCallback);
    }

    public void setMonthly(boolean monthly) {
        if (monthly) {
            monthlyOrAnnually.set(R.id.monthly);
        } else {
            monthlyOrAnnually.set(R.id.annually);
        }
    }

    ReminderPersist build() {
        ReminderPersist persist;
        if (isInsert()) {
            persist = new ReminderPersist();
        } else {
            persist = new ReminderPersist(existingUuid);
        }

        ReminderPersist.Recurrence recurrence = monthlyOrAnnually.get() == R.id.monthly ?
                ReminderPersist.Recurrence.MONTHLY :
                ReminderPersist.Recurrence.ANNUALLY;

        return persist.withDate(date)
                .withEnabled(enabled.get())
                .withName(name.get())
                .withNote(note.get())
                .with(recurrence);
    }

    public String getDateFormatted(Context context) {
        return StringUtil.formatDate(context, date, false);
    }

    public boolean getMonthly() {
        return monthlyOrAnnually.get() == R.id.monthly;
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

        return StringUtil.formatDate(context, this.date, getMonthly());
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
            mNextOccurrence = NextOccurrence.lunar(since, date, getMonthly());
            mNextOccurrenceSince = (Calendar) since.clone();
        }

        return mNextOccurrence;
    }

    public long getTimeInMillis() {
        return date.getTimeInMillis();
    }

    public ReminderPersist.Type getType() {
        return type;
    }

    public boolean isInsert() {
        return TextUtils.isEmpty(existingUuid);
    }

    public boolean isSameUuid(Reminder other) {
        return existingUuid.equals(other.existingUuid);
    }

    public boolean isSameUuid(String uuid) {
        return existingUuid.equals(uuid);
    }

    public boolean isSystem() {
        return type != ReminderPersist.Type.USER_CREATED;
    }
}
