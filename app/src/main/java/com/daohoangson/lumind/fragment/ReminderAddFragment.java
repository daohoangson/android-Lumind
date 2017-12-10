package com.daohoangson.lumind.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;

import com.daohoangson.lumind.R;
import com.daohoangson.lumind.databinding.FragmentReminderAddBinding;
import com.daohoangson.lumind.model.Lumindate;

public class ReminderAddFragment extends ReminderBaseFragment {

    private static final String ARG_DATE = "date";

    public static ReminderAddFragment newInstance(Lumindate date) {
        ReminderAddFragment f = new ReminderAddFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_DATE, date);
        f.setArguments(args);

        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @SuppressWarnings("ConstantConditions") @NonNull
        Activity activity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(R.string.title_fragment_reminder_add)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> startSavingReminder())
                .setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = LayoutInflater.from(activity);
        FragmentReminderAddBinding binding = FragmentReminderAddBinding.inflate(inflater);
        binding.setReminder(mReminder);

        binding.createMonthly.setOnClickListener(view -> {
            mReminder.monthly.set(true);
            binding.day.requestFocus();
        });

        binding.createAnnually.setOnClickListener(view -> {
            mReminder.monthly.set(false);
            binding.month.requestFocus();
        });

        binding.day.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (mReminder.monthly.get()) {
                    dismiss();
                    startSavingReminder();
                }
            }

            return false;
        });

        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_DATE)) {
            Lumindate date = args.getParcelable(ARG_DATE);
            if (date != null) {
                mReminder.date.setTimeInMillis(date.getTimeInMillis());
                mReminder.monthly.set(false);
            }
        }

        builder.setView(binding.getRoot());

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        CallerActivity activity = getCallerActivity();
        if (activity != null) {
            activity.showKeyboard();
        }
    }
}
