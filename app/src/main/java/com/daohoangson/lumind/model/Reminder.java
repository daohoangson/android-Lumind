package com.daohoangson.lumind.model;

import android.content.Context;
import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.daohoangson.lumind.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.unileipzig.informatik.duc.amlich.VietCalendar;

public class Reminder implements Parcelable {

    public String existingUuid;

    public final Lumindate date = new Lumindate();
    public final ObservableInt solarOrLunar = new ObservableInt(R.id.solar);
    public final ObservableInt monthlyOrAnnually = new ObservableInt(R.id.monthly);
    public final ObservableField<String> name = new ObservableField<>("");
    public final ObservableField<String> note = new ObservableField<>("");
    public final ObservableBoolean enabled = new ObservableBoolean(true);

    private Calendar mNextOccurrenceSince = null;
    private Date mNextOccurrence = null;

    public Reminder() {
        setupCallbacks();
    }

    public Reminder(ReminderPersist persist) {
        sync(persist);
        setupCallbacks();
    }

    private Reminder(Parcel in) {
        date.sync((Lumindate) in.readParcelable(Lumindate.class.getClassLoader()));
        setSolar(in.readInt() > 0);
        setMonthly(in.readInt() > 0);
        name.set(in.readString());
        note.set(in.readString());
        enabled.set(in.readInt() > 0);

        setupCallbacks();
    }

    @SuppressWarnings("unused")
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
        parcel.writeParcelable(date, i);
        parcel.writeInt(getSolar() ? 1 : 0);
        parcel.writeInt(getMonthly() ? 1 : 0);
        parcel.writeString(name.get());
        parcel.writeString(note.get());
        parcel.writeInt(enabled.get() ? 1 : 0);
    }

    private void setupCallbacks() {
        Observable.OnPropertyChangedCallback resetNextOccurrenceCallback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                mNextOccurrence = null;
            }
        };

        date.addOnPropertyChangedCallback(resetNextOccurrenceCallback);
        solarOrLunar.addOnPropertyChangedCallback(resetNextOccurrenceCallback);
        monthlyOrAnnually.addOnPropertyChangedCallback(resetNextOccurrenceCallback);
    }

    public void sync(Reminder other) {
        existingUuid = other.existingUuid;

        date.sync(other.date);
        setSolar(other.getSolar());
        setMonthly(other.getMonthly());
        name.set(other.name.get());
        note.set(other.note.get());
        enabled.set(other.enabled.get());
    }

    private void sync(ReminderPersist persist) {
        existingUuid = persist.uuid;

        date.sync(persist);
        setSolar(persist.solar);
        setMonthly(persist.getMonthly());
        name.set(persist.name);
        note.set(persist.getNote());
        enabled.set(persist.enabled);
    }

    public void setSolar(boolean solar) {
        if (solar) {
            solarOrLunar.set(R.id.solar);
        } else {
            solarOrLunar.set(R.id.lunar);
        }
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
        if (existingUuid != null) {
            persist = new ReminderPersist(existingUuid);
        } else {
            persist = new ReminderPersist();
        }

        ReminderPersist.Recurrence recurrence = monthlyOrAnnually.get() == R.id.monthly ?
                ReminderPersist.Recurrence.MONTHLY :
                ReminderPersist.Recurrence.ANNUALLY;

        return persist.withDate(date, getSolar())
                .withEnabled(enabled.get())
                .withName(name.get())
                .withNote(note.get())
                .withRecurrence(recurrence);
    }

    public boolean getSolar() {
        return solarOrLunar.get() == R.id.solar;
    }

    public boolean getMonthly() {
        return monthlyOrAnnually.get() == R.id.monthly;
    }

    public String getDayMonthYear(Context context) {
        if (getSolar()) {
            if (getMonthly()) {
                return context.getString(R.string.reminder_solar_day_x, date.solarDay.get());
            } else {
                Calendar c = new GregorianCalendar(date.solarYear.get(), date.solarMonth.get(), date.solarDay.get());
                return DateUtils.formatDateTime(context, c.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
            }
        } else {
            if (getMonthly()) {
                return context.getString(R.string.reminder_lunar_day_x, date.lunarDay.get());
            } else {
                LunarMonth lm = date.getLunarMonth();
                Calendar c = new GregorianCalendar(date.lunarDay.get(), lm.value, date.lunarYear.get());
                String s = DateUtils.formatDateTime(context, c.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
                if (lm.leap > 0) {
                    return context.getString(R.string.reminder_lunar_date_x_leap, s);
                } else {
                    return context.getString(R.string.reminder_lunar_date_x, s);
                }
            }
        }
    }

    public Date getNextOccurrence(@NonNull Calendar since) {
        if (mNextOccurrence != null) {
            if (mNextOccurrenceSince == null) {
                mNextOccurrence = null;
            } else {
                if (mNextOccurrenceSince.equals(since)) {
                    mNextOccurrence = null;
                }
            }
        }

        if (mNextOccurrence == null) {
            if (getSolar()) {
                mNextOccurrence = getNextOccurrenceSolar(since);
            } else {
                mNextOccurrence = getNextOccurrenceLunar(since);
            }

            mNextOccurrenceSince = (Calendar) since.clone();
        }

        return mNextOccurrence;
    }

    private Date getNextOccurrenceSolar(@NonNull Calendar since) {
        int calendarMonth = since.get(Calendar.MONTH);
        int calendarYear = since.get(Calendar.YEAR);
        Date now = since.getTime();

        Calendar c = (Calendar) since.clone();
        c.set(Calendar.DATE, 1);
        c.set(Calendar.MONTH, date.solarMonth.get());
        c.set(Calendar.YEAR, date.solarYear.get());
        int unit;

        if (monthlyOrAnnually.get() == R.id.monthly) {
            unit = Calendar.MONTH;
            c.set(Calendar.MONTH, calendarMonth - 2);
            c.set(Calendar.YEAR, calendarYear);
        } else {
            unit = Calendar.YEAR;
            c.set(Calendar.YEAR, calendarYear - 2);
        }

        int solarDayInt = date.solarDay.get();
        c.set(Calendar.DATE, Math.min(solarDayInt, c.getActualMaximum(Calendar.DAY_OF_MONTH)));

        while (!c.getTime().after(now)) {
            c.add(unit, 1);
        }

        if (c.get(Calendar.DATE) < solarDayInt) {
            c.set(Calendar.DATE, Math.min(solarDayInt, c.getActualMaximum(Calendar.DAY_OF_MONTH)));
        }

        return c.getTime();
    }

    private Date getNextOccurrenceLunar(@NonNull Calendar since) {
        Calendar c = (Calendar) since.clone();

        while (true) {
            c.add(Calendar.DATE, 1);

            double timeZoneOffset = Lumindate.getTimeZoneOffset();
            int[] tmpLunar = VietCalendar.convertSolar2Lunar(c.get(Calendar.DATE),
                    c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR), timeZoneOffset);
            int daysInMonth = VietCalendar.getLunarDaysInMonth(tmpLunar[1],
                    tmpLunar[2], tmpLunar[3], timeZoneOffset);
            int dayToCompare = Math.min(daysInMonth, date.lunarDay.get());

            if (monthlyOrAnnually.get() == R.id.monthly) {
                if (tmpLunar[0] == dayToCompare) {
                    return c.getTime();
                }
            } else {
                if (tmpLunar[0] == dayToCompare && tmpLunar[1] == date.getLunarMonth().value + 1) {
                    return c.getTime();
                }
            }
        }
    }
}
