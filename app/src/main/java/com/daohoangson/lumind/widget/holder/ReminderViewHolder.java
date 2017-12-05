package com.daohoangson.lumind.widget.holder;

import android.support.v7.widget.RecyclerView;

import com.daohoangson.lumind.databinding.ListItemReminderBinding;
import com.daohoangson.lumind.model.Reminder;

/**
 * @author sondh
 */
public class ReminderViewHolder extends RecyclerView.ViewHolder {

    private final ListItemReminderBinding mBinding;

    public ReminderViewHolder(ListItemReminderBinding binding) {
        super(binding.getRoot());
        mBinding = binding;
    }

    public void bind(Reminder reminder) {
        mBinding.setReminder(reminder);
    }
}
