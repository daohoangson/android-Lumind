package com.daohoangson.lumind.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daohoangson.lumind.R;
import com.daohoangson.lumind.databinding.FragmentRemindersBinding;
import com.daohoangson.lumind.databinding.ListItemReminderBinding;
import com.daohoangson.lumind.model.DataStore;
import com.daohoangson.lumind.model.Reminder;
import com.daohoangson.lumind.widget.holder.ReminderViewHolder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * @author sondh
 */
public class RemindersFragment extends Fragment {

    private FragmentRemindersBinding mBinding;

    public static RemindersFragment newInstance() {
        return new RemindersFragment();
    }

    public interface CallerActivity {
        void setRemindersFragment(RemindersFragment f);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentRemindersBinding.inflate(inflater, container, false);
        mBinding.list.setHasFixedSize(true);

        mBinding.list.setLayoutManager(new LinearLayoutManager(getContext()));

        final RecycleViewAdapter adapter = new RecycleViewAdapter();
        mBinding.list.setAdapter(adapter);

        mBinding.swipeRefresh.setOnRefreshListener(this::startRefreshing);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                startDeletingReminder(viewHolder.getAdapterPosition());
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }
        };
        ItemTouchHelper touchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        touchHelper.attachToRecyclerView(mBinding.list);

        if (savedInstanceState == null) {
            startRefreshing();
        }

        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = getActivity();
        if (activity instanceof CallerActivity) {
            ((CallerActivity) activity).setRemindersFragment(this);
        }
    }

    public void setActiveTab() {
        if (mBinding.list.getAdapter().getItemCount() == 0) {
            startRefreshing();
        }
    }

    public void startRefreshing() {
        mBinding.swipeRefresh.setRefreshing(true);

        final RecycleViewAdapter adapter = (RecycleViewAdapter) mBinding.list.getAdapter();
        int oldItemCount = adapter.mData.size();
        adapter.mData.clear();
        adapter.notifyItemRangeRemoved(0, oldItemCount);

        DataStore.getReminders(getContext(), results -> {
            final Calendar calendar = Calendar.getInstance();
            Collections.sort(results, (reminder, t1) -> reminder.getNextOccurrence(calendar).compareTo(t1.getNextOccurrence(calendar)));

            adapter.mData.addAll(results);
            adapter.notifyItemRangeInserted(0, results.size());

            mBinding.swipeRefresh.setRefreshing(false);
        });

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            // make sure user can request another refresh if this one hangs
            mBinding.swipeRefresh.setRefreshing(false);
        }, 500);
    }

    private void startEditingReminder(Reminder reminder, ReminderFragment.OnDismissListener listener) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }

        FragmentManager fm = activity.getSupportFragmentManager();
        ReminderFragment reminderFragment = ReminderFragment.newInstance(reminder);
        reminderFragment.addOnResultListener(listener);

        reminderFragment.show(fm, reminderFragment.toString());
    }

    private void startDeletingReminder(final int position) {
        final RecycleViewAdapter adapter = (RecycleViewAdapter) mBinding.list.getAdapter();
        final Reminder reminder = adapter.mData.remove(position);
        adapter.notifyItemRemoved(position);

        Snackbar.make(mBinding.list, R.string.reminder_has_been_deleted, Snackbar.LENGTH_LONG)
                .setAction(R.string.reminder_undelete, view -> {
                    adapter.mData.add(position, reminder);
                    adapter.notifyItemInserted(position);
                })
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);

                        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                            DataStore.deleteReminder(getContext(), reminder.existingUuid);
                        }
                    }
                })
                .show();
    }

    class RecycleViewAdapter extends RecyclerView.Adapter<ReminderViewHolder> {

        final List<Reminder> mData = new ArrayList<>();

        @Override
        public ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            final ListItemReminderBinding binding = ListItemReminderBinding.inflate(layoutInflater, parent, false);
            final ReminderViewHolder vh = new ReminderViewHolder(binding);

            binding.getRoot().setOnClickListener(view -> {
                final Reminder editingReminder = new Reminder();
                editingReminder.sync(mData.get(vh.getAdapterPosition()));
                startEditingReminder(mData.get(vh.getAdapterPosition()), (reminder, success, error) -> {
                    if (!success) {
                        return;
                    }

                    int position = vh.getAdapterPosition();
                    if (position < 0 || position >= mData.size()) {
                        return;
                    }
                    Reminder vhReminder = mData.get(position);

                    if (!reminder.existingUuid.equals(vhReminder.existingUuid)) {
                        startRefreshing();
                        return;
                    }

                    mData.remove(position);
                    mData.add(position, reminder);
                    notifyItemChanged(position);
                });
            });

            binding.enabled.setOnClickListener(view -> {
                final Reminder enabledChangedReminder = new Reminder();
                enabledChangedReminder.sync(mData.get(vh.getAdapterPosition()));
                enabledChangedReminder.enabled.set(binding.enabled.isChecked());
                startEditingReminder(enabledChangedReminder, (reminder, success, error) -> {
                    if (success) {
                        return;
                    }

                    int position = vh.getAdapterPosition();
                    if (position < 0 || position >= mData.size()) {
                        return;
                    }
                    Reminder vhReminder = mData.get(position);

                    if (!reminder.existingUuid.equals(vhReminder.existingUuid)) {
                        startRefreshing();
                        return;
                    }

                    vhReminder.enabled.set(!enabledChangedReminder.enabled.get());
                });
            });

            return vh;
        }

        @Override
        public void onBindViewHolder(ReminderViewHolder holder, int position) {
            if (mData.size() > position) {
                holder.bind(mData.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }
}
