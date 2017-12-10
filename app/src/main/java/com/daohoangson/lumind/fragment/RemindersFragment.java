package com.daohoangson.lumind.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
        void onReminderInlineSaved(Reminder reminder);

        void onReminderInlineError(Reminder reminder, Throwable error);

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
            Calendar since = Calendar.getInstance();
            Collections.sort(results, (r0, r1) -> {
                Calendar no0 = r0.getNextOccurrence(since);
                Calendar no1 = r1.getNextOccurrence(since);
                return no0.compareTo(no1);
            });

            mAdapter.data.addAll(results);
            mAdapter.notifyItemRangeInserted(0, results.size());

            if (mRefreshingCapable != null) {
                mRefreshingCapable.setRefreshing(false);
            }
            mRefreshing.set(false);
        });
    }

    @Nullable
    private CallerActivity getCallerActivity() {
        Activity activity = getActivity();
        if (activity == null || !(activity instanceof CallerActivity)) {
            return null;
        }

        return (CallerActivity) activity;
    }

    private void onReminder(ReminderViewHolder vh, Reminder reminder, boolean saved) {
        if (vh == null) {
            if (!saved) {
                return;
            }

            // new reminder
            mAdapter.data.add(reminder);
            mAdapter.notifyItemInserted(mAdapter.data.size() - 1);
            return;
        }

        int position = vh.getAdapterPosition();
        if (position < 0 || position >= mAdapter.data.size()) {
            startRefreshing();
            return;
        }

        Reminder vhReminder = mAdapter.data.get(position);
        if (!vhReminder.uuid.equals(reminder.uuid)) {
            startRefreshing();
            return;
        }

        if (!saved) {
            // revert reminder view with data previously in adapter
            vh.bind(vhReminder);
            return;
        }

        // update reminder in-place
        mAdapter.data.remove(position);
        mAdapter.data.add(position, reminder);
        vh.bind(reminder);
    }

    private void startViewingReminder(ReminderViewHolder vh) {
        CallerActivity activity = getCallerActivity();
        if (activity == null) {
            return;
        }

        int position = vh.getAdapterPosition();
        Reminder reminder = mAdapter.data.get(position);

        Calendar c = Calendar.getInstance();
        Calendar no = reminder.getNextOccurrence(c);
        Lumindate date = new Lumindate(no.getTimeInMillis());
        activity.setCalendarDate(date);
    }

    private void startEditingReminder(ReminderViewHolder vh) {
        int position = vh.getAdapterPosition();
        Reminder reminder = mAdapter.data.get(position);

        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }

        FragmentManager fm = activity.getSupportFragmentManager();
        ReminderEditFragment reminderEditFragment = ReminderEditFragment.newInstance(reminder);
        reminderEditFragment.addOnDismissListener((edited, completed, error) -> onReminder(vh, edited, completed && error != null));

        reminderEditFragment.show(fm, reminderEditFragment.toString());
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

    private void startTogglingReminder(ReminderViewHolder vh, boolean enabled) {
        int position = vh.getAdapterPosition();
        Reminder editing = mAdapter.data.get(position);

        Reminder toggled = new Reminder(Lumindate.getInstance());
        toggled.sync(editing);
        toggled.enabled.set(enabled);

        DataStore.saveReminder(getContext(), toggled, new DataStore.OnTransactionCompleteListener() {
            @Override
            public void onTransactionSuccess() {
                onReminder(vh, toggled, true);

                CallerActivity activity = getCallerActivity();
                if (activity != null) {
                    activity.onReminderInlineSaved(toggled);
                }
            }

            @Override
            public void onTransactionError(Throwable error) {
                onReminder(vh, toggled, false);

                CallerActivity activity = getCallerActivity();
                if (activity != null) {
                    activity.onReminderInlineError(toggled, error);
                }
            }
        });
    }

    class RecycleViewAdapter extends RecyclerView.Adapter<ReminderViewHolder> {

        private final List<Reminder> data = new ArrayList<>();

        @Override
        public ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            ListItemReminderBinding binding = ListItemReminderBinding.inflate(layoutInflater, parent, false);
            ReminderViewHolder vh = new ReminderViewHolder(binding);
            View root = binding.getRoot();

            root.setOnClickListener(view -> startEditingReminder(vh));

            root.setOnLongClickListener(view -> {
                FragmentActivity activity = getActivity();
                if (activity == null) {
                    return false;
                }

                FragmentManager fm = activity.getSupportFragmentManager();
                ReminderActionsFragment f = new ReminderActionsFragment();
                f.show(fm, f.toString());

                f.addOnDismissListener(action -> {
                    switch (action) {
                        case VIEW:
                            startViewingReminder(vh);
                            break;
                        case EDIT:
                            startEditingReminder(vh);
                            break;
                        case DELETE:
                            startDeletingReminder(vh);
                            break;
                    }
                });

                return true;
            });

            binding.enabled.setOnClickListener(view -> startTogglingReminder(vh, binding.enabled.isChecked()));

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
