package com.daohoangson.lumind.widget;

import android.content.Context;
import android.databinding.Observable;
import android.util.AttributeSet;

import com.daohoangson.lumind.model.Lumindate;
import com.daohoangson.lumind.model.LunarMonth;

import java.util.Calendar;
import java.util.List;

public class LunarMonthPicker extends SolarMonthPicker implements LumindPicker.DateObserver {

    private final Observable.OnPropertyChangedCallback onDateChangedCallback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable observable, int i) {
            Lumindate date = (Lumindate) observable;
            List<LunarMonth> months = LunarMonth.getLunarMonths(date.lunarYear.get(), getContext(), Calendar.SHORT);
            String[] monthNames = new String[months.size()];
            for (int monthId = 0; monthId < months.size(); monthId++) {
                monthNames[monthId] = months.get(monthId).label;
            }

            setMinValue(0);

            if (months.size() - 1 < getMaxValue()) {
                setMaxValue(months.size() - 1);
            }

            setDisplayedValues(monthNames);
            setMaxValue(months.size() - 1);
        }
    };

    public LunarMonthPicker(Context context) {
        super(context);
    }

    public LunarMonthPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LunarMonthPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    public LunarMonthPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public Observable.OnPropertyChangedCallback getOnDateChangedCallback() {
        return onDateChangedCallback;
    }
}
