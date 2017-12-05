package com.daohoangson.lumind.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.daohoangson.lumind.R;
import com.daohoangson.lumind.databinding.FragmentCalendarBinding;
import com.daohoangson.lumind.model.Lumindate;
import com.daohoangson.lumind.model.Reminder;

/**
 * @author sondh
 */
public class CalendarFragment extends Fragment {
    private static final String STATE_DATE = "date";

    private final Lumindate mDate = new Lumindate();
    private FragmentCalendarBinding mBinding;

    public static CalendarFragment newInstance() {
        return new CalendarFragment();
    }

    public interface CallerActivity {
        void setCalendarFragment(CalendarFragment f);

        FloatingActionButton getFab();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentCalendarBinding.inflate(inflater, container, false);
        mBinding.setDate(mDate);

        mDate.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                updateViews();
            }
        });

        updateViews();

        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = getActivity();
        if (activity instanceof CallerActivity) {
            ((CallerActivity) activity).setCalendarFragment(this);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            Lumindate date = savedInstanceState.getParcelable(STATE_DATE);
            if (date != null) {
                mDate.sync(date);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_DATE, mDate);
    }

    public void setActiveTab(CallerActivity callerActivity) {
        updateFab(callerActivity.getFab());
    }

    public void startFabAction() {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }

        FragmentManager fm = activity.getSupportFragmentManager();
        ReminderFragment reminderFragment = ReminderFragment.newInstance(mDate);
        reminderFragment.show(fm, reminderFragment.toString());
    }

    public void setDateFromReminder(Reminder reminder) {
        mDate.sync(reminder.date);
    }

    private void updateViews() {
        ImageView arrow = mBinding.arrow;
        int resDrawable = 0;
        int resDescription = 0;
        switch (mDate.getLastChanged()) {
            case SOLAR:
                resDrawable = R.drawable.ic_solar2lunar;
                resDescription = R.string.solar_to_lunar;
                break;
            case LUNAR:
                resDrawable = R.drawable.ic_lunar2solar;
                resDescription = R.string.lunar_to_solar;
                break;
        }

        arrow.setVisibility(View.VISIBLE);
        arrow.setContentDescription(getString(resDescription));

        Context context = getContext();
        if (context != null) {
            arrow.setImageDrawable(ContextCompat.getDrawable(context, resDrawable));
        }

        Activity activity = getActivity();
        if (activity instanceof CallerActivity) {
            updateFab(((CallerActivity) activity).getFab());
        }
    }

    private void updateFab(FloatingActionButton fab) {
        if (fab == null) {
            return;
        }

        int fabDrawable = 0;
        int fabColor = 0;
        switch (mDate.getLastChanged()) {
            case SOLAR:
                fabDrawable = R.drawable.ic_sun_white;
                fabColor = R.color.sunBackground;
                break;
            case LUNAR:
                fabDrawable = R.drawable.ic_moon_white;
                fabColor = R.color.moonBackground;
                break;
        }

        fab.setVisibility(View.VISIBLE);

        Context context = getContext();
        if (context != null) {
            fab.setImageDrawable(ContextCompat.getDrawable(context, fabDrawable));
            fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, fabColor)));
        }
    }
}
