package com.daohoangson.lumind.fragment;

import android.app.Activity;
import android.content.Context;
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

public class CalendarFragment extends Fragment {
    private static final String STATE_DATE = "date";

    public final Lumindate mDate = Lumindate.getInstance();

    public static CalendarFragment newInstance() {
        return new CalendarFragment();
    }

    public interface CallerActivity {
        void setCalendarFragment(CalendarFragment f);

        FloatingActionButton getFab();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentCalendarBinding binding = FragmentCalendarBinding.inflate(inflater, container, false);
        binding.setDate(mDate);

        mDate.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                updateViews(binding);
            }
        });

        updateViews(binding);

        return binding.getRoot();
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
                mDate.setTimeInMillis(date.getTimeInMillis());
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

    private void updateViews(FragmentCalendarBinding binding) {
        if (binding == null) {
            return;
        }

        ImageView arrow = binding.arrow;
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

        fab.setVisibility(View.VISIBLE);

        Context context = getContext();
        if (context != null) {
            fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_add_white));
        }
    }
}
