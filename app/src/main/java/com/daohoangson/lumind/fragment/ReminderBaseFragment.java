package com.daohoangson.lumind.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.daohoangson.lumind.model.DataStore;
import com.daohoangson.lumind.model.Lumindate;
import com.daohoangson.lumind.model.Reminder;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ReminderBaseFragment extends DialogFragment {

    private static final String STATE_REMINDER = "reminder";

    final Reminder mReminder = new Reminder(Lumindate.getInstance());
    private final AtomicBoolean mSaveRequested = new AtomicBoolean(false);
    private final AtomicBoolean mSaveCompleted = new AtomicBoolean(false);
    private Throwable mError = null;

    private final Set<OnDismissListener> mListeners = new HashSet<>();

    public interface CallerActivity {
        void onReminderSaved(Reminder reminder, boolean hasListeners);

        void onReminderError(Reminder reminder, Throwable error, boolean hasListeners);

        void hideKeyboard();

        void showKeyboard();
    }

    public interface OnDismissListener {
        void onReminder(@NonNull Reminder reminder, boolean completed, Throwable error);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        pingListeners(false);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        CallerActivity activity = getCallerActivity();
        if (activity != null) {
            activity.hideKeyboard();
        }
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
    CallerActivity getCallerActivity() {
        Activity activity = getActivity();
        if (activity == null || !(activity instanceof CallerActivity)) {
            return null;
        }

        return (CallerActivity) activity;
    }

    void startSavingReminder() {
        mSaveRequested.set(true);

        DataStore.saveReminder(getContext(), mReminder, new DataStore.OnTransactionCompleteListener() {
            @Override
            public void onTransactionSuccess() {
                CallerActivity activity = getCallerActivity();
                if (activity != null) {
                    activity.onReminderSaved(mReminder, mListeners.size() > 0);
                }

                mSaveCompleted.set(true);

                pingListeners(true);
            }

            @Override
            public void onTransactionError(Throwable error) {
                mError = error;

                CallerActivity activity = getCallerActivity();
                if (activity != null) {
                    activity.onReminderError(mReminder, error, mListeners.size() > 0);
                }

                mSaveCompleted.set(true);

                pingListeners(true);
            }
        });
    }

    private void pingListeners(boolean fromSaveListener) {
        if (!fromSaveListener && mSaveRequested.get()) {
            return;
        }

        for (OnDismissListener listener : mListeners) {
            listener.onReminder(mReminder, mSaveCompleted.get(), mError);
        }
    }
}
