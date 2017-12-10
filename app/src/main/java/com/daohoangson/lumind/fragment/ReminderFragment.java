package com.daohoangson.lumind.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.daohoangson.lumind.R;
import com.daohoangson.lumind.databinding.FragmentReminderBinding;
import com.daohoangson.lumind.model.DataStore;
import com.daohoangson.lumind.model.Lumindate;
import com.daohoangson.lumind.model.Reminder;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReminderFragment extends DialogFragment {

    private static final String ARG_DATE = "date";
    private static final String ARG_REMINDER = "reminder";
    private static final String STATE_REMINDER = "reminder";

    private final Reminder mReminder = new Reminder(Lumindate.getInstance());
    private final AtomicBoolean mSaveRequested = new AtomicBoolean(false);
    private Throwable mError = null;

    private final Set<OnDismissListener> mListeners = new HashSet<>();

    public static ReminderFragment newInstance(Lumindate date) {
        ReminderFragment reminderFragment = new ReminderFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_DATE, date);
        reminderFragment.setArguments(args);

        return reminderFragment;
    }

    public static ReminderFragment newInstance(Reminder reminder) {
        ReminderFragment reminderFragment = new ReminderFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_REMINDER, reminder);
        reminderFragment.setArguments(args);

        return reminderFragment;
    }

    public interface CallerActivity {
        void onReminderSaved(Reminder reminder, boolean hasListeners);

        void onReminderError(Reminder reminder, Throwable error, boolean hasListeners);
    }

    public interface OnDismissListener {
        void onReminderFragmentDismiss(@NonNull Reminder reminder, Throwable error);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @SuppressWarnings("ConstantConditions") @NonNull
        Activity activity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> startSavingReminder())
                .setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = LayoutInflater.from(activity);
        FragmentReminderBinding binding = FragmentReminderBinding.inflate(inflater);
        binding.setReminder(mReminder);

        Bundle args = getArguments();
        if (args == null) {
            throw new RuntimeException("Arguments bundle is missing");
        }

        if (args.containsKey(ARG_DATE)) {
            Lumindate date = args.getParcelable(ARG_DATE);
            if (date == null) {
                throw new RuntimeException("Argument date is missing");
            }
            mReminder.date.setTimeInMillis(date.getTimeInMillis());
        }

        if (args.containsKey(ARG_REMINDER)) {
            Reminder reminder = args.getParcelable(ARG_REMINDER);
            if (reminder == null) {
                throw new RuntimeException("Argument reminder is missing");
            }
            mReminder.sync(reminder);
        }

        if (mReminder.isInsert()) {
            builder.setTitle(R.string.title_fragment_reminder_add);
        } else {
            builder.setTitle(R.string.title_fragment_reminder_edit);
        }
        builder.setMessage(mReminder.getDateFormatted(activity));

        builder.setView(binding.getRoot());

        return builder.create();
    }

    @Override
    public void onResume() {
        Window window = getDialog().getWindow();
        if (window != null) {
            ViewGroup.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        }

        super.onResume();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        pingListeners(false);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        pingListeners(false);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            Reminder reminder = savedInstanceState.getParcelable(STATE_REMINDER);
            if (reminder != null) {
                mReminder.sync(reminder);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_REMINDER, mReminder);
    }

    public void addOnDismissListener(OnDismissListener listener) {
        if (listener == null) {
            return;
        }

        mListeners.add(listener);
    }

    @Nullable
    private CallerActivity getCallerActivity() {
        Activity activity = getActivity();
        if (activity == null || !(activity instanceof CallerActivity)) {
            return null;
        }

        return (CallerActivity) activity;
    }

    private void startSavingReminder() {
        mSaveRequested.set(true);

        DataStore.saveReminder(getContext(), mReminder, new DataStore.OnTransactionCompleteListener() {
            @Override
            public void onTransactionSuccess() {
                CallerActivity activity = getCallerActivity();
                if (activity != null) {
                    activity.onReminderSaved(mReminder, mListeners.size() > 0);
                }

                pingListeners(true);
            }

            @Override
            public void onTransactionError(Throwable error) {
                mError = error;

                CallerActivity activity = getCallerActivity();
                if (activity != null) {
                    activity.onReminderError(mReminder, error, mListeners.size() > 0);
                }

                pingListeners(true);
            }
        });
    }

    private void pingListeners(boolean fromSaveListener) {
        if (!fromSaveListener && mSaveRequested.get()) {
            return;
        }

        for (OnDismissListener listener : mListeners) {
            listener.onReminderFragmentDismiss(mReminder, mError);
        }
    }
}
