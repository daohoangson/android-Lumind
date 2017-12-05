package com.daohoangson.lumind.widget;

import android.databinding.BindingAdapter;
import android.databinding.Observable;
import android.widget.TextView;

import com.daohoangson.lumind.utils.StringUtil;
import com.daohoangson.lumind.model.Lumindate;
import com.daohoangson.lumind.model.Reminder;

import java.util.Calendar;
import java.util.Date;

/**
 * @author sondh
 */
public class BindingAdapters {
    @BindingAdapter("date_observer")
    public static void bindDateToLumindPicker(LumindPicker picker, Lumindate date) {
        if (date == null) {
            return;
        }

        Observable.OnPropertyChangedCallback callback = ((LumindPicker.DateObserver) picker).getOnDateChangedCallback();
        if (callback == null) {
            return;
        }

        date.removeOnPropertyChangedCallback(callback);
        date.addOnPropertyChangedCallback(callback);
    }

    @BindingAdapter("next_occurrence_text")
    public static void bindNextOccurrenceToTextView(TextView textView, Reminder reminder) {
        Calendar calendar = Calendar.getInstance();
        Date nextDate = reminder.getNextOccurrence(calendar);
        String txt = StringUtil.formatNextOccurrenceInX(textView.getResources(), calendar, nextDate);

        textView.setText(txt);
    }
}
