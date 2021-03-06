package com.daohoangson.lumind.model;

import android.databinding.BaseObservable;
import android.databinding.Observable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableInt;
import android.databinding.ObservableList;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import de.unileipzig.informatik.duc.amlich.VietCalendar;

public class Lumindate extends BaseObservable implements Parcelable {

    @SuppressWarnings("WeakerAccess")
    public final ObservableInt solarDay = new ObservableInt(0);
    public final ObservableInt solarMonth = new ObservableInt(0);
    public final ObservableInt solarYear = new ObservableInt(0);
    public final ObservableInt lunarDay = new ObservableInt(0);
    @SuppressWarnings("WeakerAccess")
    public final ObservableInt lunarMonthRaw = new ObservableInt(0);
    @SuppressWarnings("WeakerAccess")
    public final ObservableList<String> lunarMonths = new ObservableArrayList<>();
    public final ObservableInt lunarYear = new ObservableInt(0);

    private final AtomicBoolean mCorrectnessGuarantee = new AtomicBoolean(true);
    private FieldGroup mLastChanged = FieldGroup.SOLAR;

    public static Lumindate getInstance() {
        return new Lumindate(Calendar.getInstance().getTimeInMillis());
    }

    public Lumindate(long timeInMillis) {
        setTimeInMillis(timeInMillis);
        calculateLunar();
        setupCallbacks();
    }

    public Lumindate(@NonNull Lumindate other) {
        this(other.getTimeInMillis());
    }

    Lumindate(int lunarDay, int lunarMonth) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -1);

        setLunarDate(lunarDay, lunarMonth, c.get(Calendar.YEAR));
        validateLunarValues();
        calculateSolar();
        setupCallbacks();
    }

    private Lumindate(Parcel in) {
        this(in.readLong());
    }

    public static final Creator<Lumindate> CREATOR = new Creator<Lumindate>() {
        @Override
        public Lumindate createFromParcel(Parcel in) {
            return new Lumindate(in);
        }

        @Override
        public Lumindate[] newArray(int size) {
            return new Lumindate[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(getTimeInMillis());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Lumindate && getTimeInMillis() == ((Lumindate) obj).getTimeInMillis();
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "[Solar(%d-%d-%d),Lunar(%d-%d-%d)]",
                solarYear.get(), solarMonth.get(), solarDay.get(),
                lunarYear.get(), lunarMonthRaw.get(), lunarDay.get());
    }

    public LunarMonth getLunarMonth() {
        List<LunarMonth> months = LunarMonth.getLunarMonths(lunarYear.get());
        return months.get(lunarMonthRaw.get());
    }

    public FieldGroup getLastChanged() {
        return mLastChanged;
    }

    public long getTimeInMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, solarDay.get());
        calendar.set(Calendar.MONTH, solarMonth.get());
        calendar.set(Calendar.YEAR, solarYear.get());
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    void setLunarDate(int day, int month, int year) {
        boolean flag = mCorrectnessGuarantee.getAndSet(false);

        lunarDay.set(day);
        lunarYear.set(year);
        calculateLunarMonth(month, 0);

        validateLunarValues();
        calculateSolar();

        mCorrectnessGuarantee.set(flag);
    }

    public void setTimeInMillis(long timeInMillis) {
        boolean flag = mCorrectnessGuarantee.getAndSet(false);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timeInMillis);

        solarDay.set(c.get(Calendar.DATE));
        solarMonth.set(c.get(Calendar.MONTH));
        solarYear.set(c.get(Calendar.YEAR));

        validateSolarValues();
        calculateLunar();

        mCorrectnessGuarantee.set(flag);
    }

    private void calculateLunar() {
        int solarDayInt = solarDay.get();
        int solarMonthInt = solarMonth.get();
        int solarYearInt = solarYear.get();

        int[] lunarValues = VietCalendar.convertSolar2Lunar(solarDayInt,
                solarMonthInt + 1, solarYearInt, getTimeZoneOffset());

        lunarDay.set(lunarValues[0]);
        lunarYear.set(lunarValues[2]);
        calculateLunarMonth(lunarValues[1] - 1, lunarValues[3]);
    }

    private void calculateLunarMonth(int value, int leap) {
        List<LunarMonth> months = LunarMonth.getLunarMonths(lunarYear.get());
        String[] monthLabels = new String[months.size()];
        for (int monthId = 0; monthId < months.size(); monthId++) {
            LunarMonth m = months.get(monthId);
            if (m.value == value && m.leap == leap) {
                lunarMonthRaw.set(monthId);
            }
            monthLabels[monthId] = m.label;
        }

        lunarMonths.clear();
        lunarMonths.addAll(Arrays.asList(monthLabels));
    }

    private void calculateSolar() {
        int lunarDayInt = lunarDay.get();
        LunarMonth m = getLunarMonth();
        int lunarYearInt = lunarYear.get();

        int[] solarValues = VietCalendar.convertLunar2Solar(lunarDayInt,
                m.value + 1, lunarYearInt, m.leap, getTimeZoneOffset());

        solarDay.set(solarValues[0]);
        solarMonth.set(solarValues[1] - 1);
        solarYear.set(solarValues[2]);
    }

    private void setupCallbacks() {
        Observable.OnPropertyChangedCallback solarCallback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (mCorrectnessGuarantee.compareAndSet(true, false)) {
                    validateSolarValues();
                    calculateLunar();

                    mLastChanged = FieldGroup.SOLAR;
                    notifyChange();

                    mCorrectnessGuarantee.set(true);
                }
            }
        };
        solarDay.addOnPropertyChangedCallback(solarCallback);
        solarMonth.addOnPropertyChangedCallback(solarCallback);
        solarYear.addOnPropertyChangedCallback(solarCallback);

        Observable.OnPropertyChangedCallback lunarCallback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (mCorrectnessGuarantee.compareAndSet(true, false)) {
                    validateLunarValues();
                    calculateSolar();

                    mLastChanged = FieldGroup.LUNAR;
                    notifyChange();

                    mCorrectnessGuarantee.set(true);
                }
            }
        };
        lunarDay.addOnPropertyChangedCallback(lunarCallback);
        lunarMonthRaw.addOnPropertyChangedCallback(lunarCallback);
        lunarYear.addOnPropertyChangedCallback(lunarCallback);
    }

    private void validateLunarValues() {
        int lunarDayInt = lunarDay.get();
        if (lunarDayInt < 1) {
            lunarDay.set(1);
        }

        int lunarMonthInt = lunarMonthRaw.get();
        if (lunarMonthInt < 0) {
            lunarMonthRaw.set(0);
        }

        int lunarYearInt = lunarYear.get();

        List<LunarMonth> months = LunarMonth.getLunarMonths(lunarYearInt);
        if (lunarMonthInt >= months.size()) {
            lunarMonthRaw.set(months.size() - 1);
        }

        LunarMonth m = getLunarMonth();
        int daysInMonth = VietCalendar.getLunarDaysInMonth(m.value + 1,
                lunarYearInt, m.leap, getTimeZoneOffset());
        if (lunarDayInt > daysInMonth) {
            lunarDay.set(daysInMonth);
        }
    }

    private void validateSolarValues() {
        int solarDayInt = solarDay.get();
        if (solarDayInt < 1) {
            solarDay.set(1);
        }

        int solarMonthInt = solarMonth.get();
        if (solarMonthInt < 0) {
            solarMonth.set(0);
        } else if (solarMonthInt > 11) {
            solarMonth.set(11);
        }

        int solarYearInt = solarYear.get();

        Calendar calendar = new GregorianCalendar(solarYearInt, solarMonthInt, 1);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (solarDayInt > daysInMonth) {
            solarDay.set(daysInMonth);
        }
    }

    public static double getTimeZoneOffset() {
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = calendar.getTimeZone();
        int rawOffset = timeZone.getRawOffset();
        return TimeUnit.HOURS.convert(rawOffset, TimeUnit.MILLISECONDS);
    }

    public enum FieldGroup {
        SOLAR,
        LUNAR
    }
}
