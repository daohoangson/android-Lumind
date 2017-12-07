package com.daohoangson.lumind.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.daohoangson.lumind.R;

import java.util.HashSet;
import java.util.Set;

public class ReminderActionsFragment extends DialogFragment {

    private final Set<OnDismissListener> mListeners = new HashSet<>();

    public enum Action {
        EDIT,
        DELETE
    }

    public interface OnDismissListener {
        void onReminderActionsFragmentDismiss(Action action);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @SuppressWarnings("ConstantConditions") @NonNull
        Activity activity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setCancelable(true)
                .setItems(R.array.reminder_actions, (dialog, which) -> {
                    Activity activityInside = getActivity();
                    if (activityInside == null) {
                        return;
                    }

                    String[] codes = activityInside.getResources().getStringArray(R.array.reminder_action_codes);
                    if (which < 0 || which >= codes.length) {
                        return;
                    }
                    String code = codes[which];

                    switch (code) {
                        case "edit":
                            pingListeners(Action.EDIT);
                            break;
                        case "delete":
                            pingListeners(Action.DELETE);
                            break;
                    }
                });

        return builder.create();
    }

    public void addOnDismissListener(OnDismissListener listener) {
        if (listener == null) {
            return;
        }

        mListeners.add(listener);
    }

    private void pingListeners(Action action) {
        for (OnDismissListener listener : mListeners) {
            listener.onReminderActionsFragmentDismiss(action);
        }
    }
}
