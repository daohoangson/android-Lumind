package com.daohoangson.lumind.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.daohoangson.lumind.R;
import com.daohoangson.lumind.databinding.FragmentReminderBinding;
import com.daohoangson.lumind.model.Reminder;
import com.daohoangson.lumind.model.Lumindate;
import com.daohoangson.lumind.model.DataStore;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author sondh
 */
public class ReminderFragment extends DialogFragment {
    public static final String ARG_DATE = "date";
    public static final String ARG_REMINDER = "reminder";
    public static final String STATE_REMINDER = "reminder";

    Reminder mReminder = new Reminder();
    AtomicBoolean mSaveRequested = new AtomicBoolean(false);
    boolean mSuccess = false;
    Throwable mError = null;

    WeakReference<CallerActivity> mCallerActivityRef;
    Set<OnDismissListener> mListeners = new HashSet<>();

    public static ReminderFragment newInstance(Lumindate date) {
        ReminderFragment reminderFragment = new ReminderFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_DATE, date);
        reminderFragment.setArguments(args);

        return reminderFragment;
    }

    public interface CallerActivity {
        void onReminderSaved(Reminder reminder, boolean hasListeners);

        void onReminderError(Reminder reminder, Throwable error, boolean hasListeners);
    }

    public interface OnDismissListener {
        void onReminderFragmentDismiss(Reminder reminder, boolean success, Throwable error);
    }

    public static ReminderFragment newInstance(Reminder reminder) {
        ReminderFragment reminderFragment = new ReminderFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_REMINDER, reminder);
        reminderFragment.setArguments(args);

        return reminderFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startSavingReminder();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        FragmentReminderBinding binding = FragmentReminderBinding.inflate(inflater);
        binding.setReminder(mReminder);

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_DATE)) {
                builder.setTitle(R.string.title_fragment_reminder_add);
                Lumindate date = args.getParcelable(ARG_DATE);
                if (date != null) {
                    mReminder.date.sync(date);

                    if (date.getLastChanged() == Lumindate.FieldGroup.LUNAR) {
                        mReminder.solarOrLunar.set(R.id.lunar);
                    }
                }
            } else if (args.containsKey(ARG_REMINDER)) {
                builder.setTitle(R.string.title_fragment_reminder_edit);
                Reminder reminder = args.getParcelable(ARG_REMINDER);
                if (reminder != null) {
                    mReminder.sync(reminder);
                }
            }
        }

        builder.setView(binding.getRoot());

        return builder.create();
    }

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = getActivity();
        if (activity instanceof CallerActivity) {
            mCallerActivityRef = new WeakReference<>((CallerActivity) activity);
        }
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

    public void addOnResultListener(OnDismissListener listener) {
        if (listener == null) {
            return;
        }

        mListeners.add(listener);
    }

    void startSavingReminder() {
        mSaveRequested.set(true);
        DataStore.saveEditingReminder(getContext(), mReminder, new DataStore.OnTransactionCompleteListener() {
            @Override
            public void onTransactionSuccess() {
                mSuccess = true;

                if (mCallerActivityRef != null && mCallerActivityRef.get() != null) {
                    mCallerActivityRef.get().onReminderSaved(mReminder, mListeners.size() > 0);
                }

                pingListeners(true);
            }

            @Override
            public void onTransactionError(Throwable error) {
                mError = error;

                if (mCallerActivityRef != null && mCallerActivityRef.get() != null) {
                    mCallerActivityRef.get().onReminderError(mReminder, error, mListeners.size() > 0);
                }

                pingListeners(true);
            }
        });
    }

    void pingListeners(boolean fromSaveListener) {
        if (!fromSaveListener && mSaveRequested.get()) {
            return;
        }

        for (OnDismissListener listener : mListeners) {
            listener.onReminderFragmentDismiss(mReminder, mSuccess, mError);
        }
    }
}
