package com.daohoangson.lumind.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.daohoangson.lumind.R;
import com.daohoangson.lumind.databinding.FragmentReminderEditBinding;
import com.daohoangson.lumind.model.Reminder;

public class ReminderEditFragment extends ReminderBaseFragment {

    private static final String ARG_REMINDER = "reminder";

    public static ReminderEditFragment newInstance(Reminder reminder) {
        ReminderEditFragment reminderEditFragment = new ReminderEditFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_REMINDER, reminder);
        reminderEditFragment.setArguments(args);

        return reminderEditFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @SuppressWarnings("ConstantConditions") @NonNull
        Activity activity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(R.string.title_fragment_reminder_edit)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> startSavingReminder())
                .setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = LayoutInflater.from(activity);
        FragmentReminderEditBinding binding = FragmentReminderEditBinding.inflate(inflater);
        binding.setReminder(mReminder);

        Bundle args = getArguments();
        if (args == null) {
            throw new RuntimeException("Arguments bundle is missing");
        }

        if (args.containsKey(ARG_REMINDER)) {
            Reminder reminder = args.getParcelable(ARG_REMINDER);
            if (reminder == null) {
                throw new RuntimeException("Argument reminder is missing");
            }
            mReminder.sync(reminder);
        }

        builder.setMessage(mReminder.getNameForShow(activity));

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
}
