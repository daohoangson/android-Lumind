package com.daohoangson.lumind.widget;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class SolarMonthPicker extends LumindPicker {
    public SolarMonthPicker(Context context) {
        super(context);
    }

    public SolarMonthPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SolarMonthPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    public SolarMonthPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void init() {
        setMinValue(0);
        setMaxValue(11);

        Calendar calendar = Calendar.getInstance();
        Map<String, Integer> displayNames = calendar.getDisplayNames(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
        String[] monthNames = new String[displayNames.size()];
        for (Map.Entry<String, Integer> displayName : displayNames.entrySet()) {
            monthNames[displayName.getValue()] = displayName.getKey();
        }
        setDisplayedValues(monthNames);
    }
}
