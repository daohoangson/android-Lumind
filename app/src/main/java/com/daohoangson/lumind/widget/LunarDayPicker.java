package com.daohoangson.lumind.widget;

import android.content.Context;
import android.databinding.Observable;
import android.util.AttributeSet;

import com.daohoangson.lumind.model.Lumindate;
import com.daohoangson.lumind.model.LunarMonth;

import de.unileipzig.informatik.duc.amlich.VietCalendar;

public class LunarDayPicker extends LumindPicker implements LumindPicker.DateObserver {

    private final Observable.OnPropertyChangedCallback onDateChangedCallback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable observable, int i) {
            Lumindate date = (Lumindate) observable;
            int lunarYearInt = date.lunarYear.get();

            LunarMonth m = date.getLunarMonth();
            int daysInMonth = VietCalendar.getLunarDaysInMonth(m.value + 1,
                    lunarYearInt, m.leap, Lumindate.getTimeZoneOffset());
            setMaxValue(daysInMonth);
        }
    };

    public LunarDayPicker(Context context) {
        super(context);
    }

    public LunarDayPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LunarDayPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    public LunarDayPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void init() {
        setMinValue(1);
        setMaxValue(30);
    }

    @Override
    public Observable.OnPropertyChangedCallback getOnDateChangedCallback() {
        return onDateChangedCallback;
    }
}
