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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author sondh
 */
public class RemindersFragment extends Fragment {

    private FragmentRemindersBinding mBinding;
    private AtomicBoolean mRefreshing = new AtomicBoolean(false);

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
        if (!mRefreshing.compareAndSet(false, true)) {
            return;
        }

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
            mRefreshing.set(false);
        });
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
            ListItemReminderBinding binding = ListItemReminderBinding.inflate(layoutInflater, parent, false);
            ReminderViewHolder vh = new ReminderViewHolder(binding);

            binding.getRoot().setOnClickListener(view -> {
                int position = vh.getAdapterPosition();
                Reminder editing = mData.get(position);
                onItemClick(vh, editing);
            });

            binding.enabled.setOnClickListener(view -> {
                int position = vh.getAdapterPosition();
                Reminder editing = new Reminder();
                editing.sync(mData.get(position));

                editing.enabled.set(binding.enabled.isChecked());
                onItemClick(vh, editing);
            });

            return vh;
        }

        @Override
        public void onBindViewHolder(ReminderViewHolder holder, int position) {
            if (mData.size() > position) {
                Reminder reminder = mData.get(position);

                holder.bind(reminder);
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        private void onItemClick(ReminderViewHolder vh, Reminder editing) {
            startEditingReminder(editing, (edited, success, error) -> {
                int position = vh.getAdapterPosition();
                if (position < 0 || position >= mData.size()) {
                    startRefreshing();
                    return;
                }
                Reminder vhReminder = mData.get(position);

                if (!edited.existingUuid.equals(vhReminder.existingUuid)) {
                    startRefreshing();
                    return;
                }

                if (!success) {
                    vh.bind(vhReminder);
                    return;
                }

                mData.remove(position);
                mData.add(position, edited);
                vh.bind(edited);
            });
        }
    }
}
