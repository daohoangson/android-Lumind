package com.daohoangson.lumind.widget;

import android.content.Context;
import android.databinding.Observable;
import android.util.AttributeSet;

import com.daohoangson.lumind.model.Lumindate;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class SolarDayPicker extends LumindPicker implements LumindPicker.DateObserver {

    Observable.OnPropertyChangedCallback onDateChangedCallback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable observable, int i) {
            Lumindate date = (Lumindate) observable;
            Calendar calendar = new GregorianCalendar(date.solarYear.get(), date.solarMonth.get(), 1);
            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            setMaxValue(daysInMonth);
        }
    };

    public SolarDayPicker(Context context) {
        super(context);
    }

    public SolarDayPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SolarDayPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    public SolarDayPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void init() {
        setMinValue(1);
        setMaxValue(31);
    }

    @Override
    public Observable.OnPropertyChangedCallback getOnDateChangedCallback() {
        return onDateChangedCallback;
    }
}
