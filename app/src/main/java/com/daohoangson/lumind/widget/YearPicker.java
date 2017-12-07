package com.daohoangson.lumind.widget;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Calendar;

public class YearPicker extends LumindPicker {
    public YearPicker(Context context) {
        super(context);
    }

    public YearPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public YearPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public YearPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    void init() {
        Calendar calendar = Calendar.getInstance();
        setMinValue(calendar.get(Calendar.YEAR) - 100);
        setMaxValue(calendar.get(Calendar.YEAR) + 100);
    }
}
