package com.daohoangson.lumind.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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
import com.daohoangson.lumind.model.Lumindate;
import com.daohoangson.lumind.model.Reminder;
import com.daohoangson.lumind.widget.holder.ReminderViewHolder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RemindersFragment extends Fragment {

    private final RecycleViewAdapter mAdapter = new RecycleViewAdapter();
    private final AtomicBoolean mRefreshing = new AtomicBoolean(false);
    private RefreshingCapable mRefreshingCapable;

    public static RemindersFragment newInstance() {
        return new RemindersFragment();
    }

    public interface CallerActivity {
        void setCalendarDate(Lumindate date);

        void setRemindersFragment(RemindersFragment f);
    }

    public interface RefreshingCapable {
        void setRefreshing(boolean refreshing);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentRemindersBinding binding = FragmentRemindersBinding.inflate(inflater, container, false);
        binding.list.setHasFixedSize(true);
        binding.list.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.list.setAdapter(mAdapter);

        binding.swipeRefresh.setOnRefreshListener(this::startRefreshing);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                startDeletingReminder((ReminderViewHolder) viewHolder);
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }
        };
        ItemTouchHelper touchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        touchHelper.attachToRecyclerView(binding.list);

        mRefreshingCapable = binding.swipeRefresh::setRefreshing;

        return binding.getRoot();
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
        if (mAdapter.getItemCount() == 0) {
            startRefreshing();
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void startRefreshing() {
        if (!mRefreshing.compareAndSet(false, true)) {
            return;
        }

        if (mRefreshingCapable != null) {
            mRefreshingCapable.setRefreshing(true);
        }

        int oldItemCount = mAdapter.data.size();
        mAdapter.data.clear();
        mAdapter.notifyItemRangeRemoved(0, oldItemCount);

        DataStore.getReminders(getContext(), results -> {
            Calendar calendar = Calendar.getInstance();
            Collections.sort(results, (reminder, t1) -> reminder.getNextOccurrence(calendar).compareTo(t1.getNextOccurrence(calendar)));

            mAdapter.data.addAll(results);
            mAdapter.notifyItemRangeInserted(0, results.size());

            if (mRefreshingCapable != null) {
                mRefreshingCapable.setRefreshing(false);
            }
            mRefreshing.set(false);
        });
    }

    private void startViewingReminder(ReminderViewHolder vh) {
        int position = vh.getAdapterPosition();
        Reminder reminder = mAdapter.data.get(position);

        Activity activity = getActivity();
        if (activity instanceof CallerActivity) {
            Calendar c = Calendar.getInstance();
            Calendar no = reminder.getNextOccurrence(c);
            Lumindate date = new Lumindate(no.getTimeInMillis());
            ((CallerActivity) activity).setCalendarDate(date);
        }
    }

    private void startEditingReminder(ReminderViewHolder vh, Reminder reminder) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }

        FragmentManager fm = activity.getSupportFragmentManager();
        ReminderFragment reminderFragment = ReminderFragment.newInstance(reminder);
        reminderFragment.addOnDismissListener((edited, success, error) -> {
            int position = vh.getAdapterPosition();
            if (position < 0 || position >= mAdapter.data.size()) {
                startRefreshing();
                return;
            }

            Reminder vhReminder = mAdapter.data.get(position);
            if (!vhReminder.uuid.equals(edited.uuid)) {
                startRefreshing();
                return;
            }

            if (!success) {
                vh.bind(vhReminder);
                return;
            }

            mAdapter.data.remove(position);
            mAdapter.data.add(position, edited);
            vh.bind(edited);
        });

        reminderFragment.show(fm, reminderFragment.toString());
    }

    private void startDeletingReminder(ReminderViewHolder vh) {
        int position = vh.getAdapterPosition();
        if (position < 0 || position >= mAdapter.data.size()) {
            startRefreshing();
            return;
        }

        Reminder reminder = mAdapter.data.remove(position);
        mAdapter.notifyItemRemoved(position);

        View viewForSnackbar = getView();
        if (viewForSnackbar == null) {
            return;
        }

        Snackbar.make(viewForSnackbar, R.string.reminder_has_been_deleted, Snackbar.LENGTH_LONG)
                .setAction(R.string.reminder_undelete, view -> {
                    mAdapter.data.add(position, reminder);
                    mAdapter.notifyItemInserted(position);
                })
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);

                        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                            DataStore.deleteReminder(getContext(), reminder);
                        }
                    }
                })
                .show();
    }

    class RecycleViewAdapter extends RecyclerView.Adapter<ReminderViewHolder> {

        private final List<Reminder> data = new ArrayList<>();

        @Override
        public ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            ListItemReminderBinding binding = ListItemReminderBinding.inflate(layoutInflater, parent, false);
            ReminderViewHolder vh = new ReminderViewHolder(binding);
            View root = binding.getRoot();

            root.setOnClickListener(view -> startViewingReminder(vh));

            root.setOnLongClickListener(view -> {
                FragmentActivity activity = getActivity();
                if (activity == null) {
                    return false;
                }

                int position = vh.getAdapterPosition();
                Reminder focusing = data.get(position);
                FragmentManager fm = activity.getSupportFragmentManager();
                ReminderActionsFragment f = new ReminderActionsFragment();
                f.show(fm, f.toString());

                f.addOnDismissListener(action -> {
                    switch (action) {
                        case VIEW:
                            startViewingReminder(vh);
                            break;
                        case EDIT:
                            startEditingReminder(vh, focusing);
                            break;
                        case DELETE:
                            startDeletingReminder(vh);
                            break;
                    }
                });

                return true;
            });

            binding.enabled.setOnClickListener(view -> {
                int position = vh.getAdapterPosition();
                Reminder editing = data.get(position);

                Reminder toggled = new Reminder(Lumindate.getInstance());
                toggled.sync(editing);
                toggled.enabled.set(binding.enabled.isChecked());

                startEditingReminder(vh, toggled);
            });

            return vh;
        }

        @Override
        public void onBindViewHolder(ReminderViewHolder holder, int position) {
            if (data.size() > position) {
                Reminder reminder = data.get(position);

                holder.bind(reminder);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}
