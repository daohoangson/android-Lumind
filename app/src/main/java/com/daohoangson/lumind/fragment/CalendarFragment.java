package com.daohoangson.lumind.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.daohoangson.lumind.R;
import com.daohoangson.lumind.databinding.FragmentCalendarBinding;
import com.daohoangson.lumind.model.Lumindate;

/**
 * @author sondh
 */
public class CalendarFragment extends Fragment {
    public static final String STATE_DATE = "date";

    Lumindate mDate = new Lumindate();
    FragmentCalendarBinding mBinding;

    public static CalendarFragment newInstance() {
        return new CalendarFragment();
    }

    public interface CallerActivity {
        void setCalendarFragment(CalendarFragment f);

        FloatingActionButton getFab();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_DATE, mDate);
    }

    public void setActiveTab(CallerActivity callerActivity) {
        updateFab(callerActivity.getFab());
    }

    public void startFabAction() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ReminderFragment reminderFragment = ReminderFragment.newInstance(mDate);
        reminderFragment.show(fm, reminderFragment.toString());
    }

    void updateViews() {
        ImageView arrow = mBinding.arrow;
        int arrowDrawable = 0;
        switch (mDate.getLastChanged()) {
            case SOLAR:
                arrowDrawable = R.drawable.ic_solar2lunar;
                break;
            case LUNAR:
                arrowDrawable = R.drawable.ic_lunar2solar;
                break;
        }

        Context context = getContext();
        arrow.setVisibility(View.VISIBLE);
        arrow.setImageDrawable(ContextCompat.getDrawable(context, arrowDrawable));

        Activity activity = getActivity();
        if (activity instanceof CallerActivity) {
            updateFab(((CallerActivity) activity).getFab());
        }
    }

    void updateFab(FloatingActionButton fab) {
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

        Context context = getContext();
        fab.setVisibility(View.VISIBLE);
        fab.setImageDrawable(ContextCompat.getDrawable(context, fabDrawable));
        fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, fabColor)));
    }
}
