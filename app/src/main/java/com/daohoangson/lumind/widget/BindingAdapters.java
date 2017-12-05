package com.daohoangson.lumind.widget;

import android.content.res.Resources;
import android.databinding.BindingAdapter;
import android.databinding.Observable;
import android.widget.TextView;

import com.daohoangson.lumind.R;
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

        long today = calendar.getTimeInMillis();
        long next = nextDate.getTime();
        long durationInSec = (next - today) / 1000;
        int days = (int) Math.ceil(durationInSec / 86400.0);
        int months = (int) Math.ceil(days / 30.0);

        Resources r = textView.getResources();
        String txt = null;
        if (months > 1) {
            txt = r.getString(R.string.next_occurrence_in_x, r.getQuantityString(R.plurals.x_months, months, months));
        } else if (days > 0) {
            txt = r.getString(R.string.next_occurrence_in_x, r.getQuantityString(R.plurals.x_days, days, days));
        }
        if (txt != null) {
            textView.setText(txt);
        } else {
            textView.setText("");
        }
    }
}
