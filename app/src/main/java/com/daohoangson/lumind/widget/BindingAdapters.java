package com.daohoangson.lumind.widget;

import android.content.res.Resources;
import android.databinding.BindingAdapter;
import android.databinding.Observable;
import android.text.format.DateUtils;
import android.widget.TextView;

import com.daohoangson.lumind.R;
import com.daohoangson.lumind.model.Lumindate;
import com.daohoangson.lumind.model.Reminder;
import com.daohoangson.lumind.utils.StringUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@SuppressWarnings("WeakerAccess")
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
        Calendar since = Calendar.getInstance();
        Calendar next = reminder.getNextOccurrence(since);
        int days = StringUtil.calculateDays(since, next);
        Resources r = textView.getResources();

        String date = DateUtils.formatDateTime(textView.getContext(), next.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
        String when = StringUtil.formatNextOccurrenceWhen(r, days);
        if (when == null) {
            textView.setText(date);
            return;
        }

        textView.setText(r.getString(R.string.next_occurrence_date_x_when_y, date, when));
    }

    @BindingAdapter("remind_when_text")
    public static void bindRemindWhenToTextView(TextView textView, Reminder reminder) {
        if (!reminder.enabled.get()) {
            textView.setText("");
            return;
        }

        List<String> when = new ArrayList<>();
        Resources r = textView.getResources();

        if (reminder.when0.get()) {
            when.add(r.getString(R.string.remind_when0));
        }
        if (reminder.when1.get()) {
            when.add(r.getString(R.string.remind_when1));
        }
        if (reminder.when7.get()) {
            when.add(r.getString(R.string.remind_when7));
        }

        final String whenText;
        switch (when.size()) {
            case 1:
                whenText = when.get(0);
                break;
            case 2:
                whenText = r.getString(R.string.remind_when_x_y, when.get(0), when.get(1));
                break;
            case 3:
                whenText = r.getString(R.string.remind_when_x_y_z, when.get(0), when.get(1), when.get(2));
                break;
            default:
                textView.setText("");
                return;
        }

        if (reminder.monthly.get()) {
            textView.setText(r.getString(R.string.remind_monthly_when_x, whenText));
            return;
        }

        textView.setText(r.getString(R.string.remind_annually_when_x, whenText));
    }
}
