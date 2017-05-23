package com.daohoangson.lumind.fragment;

import android.app.job.JobInfo;
import android.support.v4.app.ListFragment;
import android.util.Log;

import com.daohoangson.lumind.schedule.JobSchedulerService;

/**
 * @author sondh
 */
public class SettingFragment extends ListFragment {

    boolean mReminderJobScheduled = false;

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public void onResume() {
        super.onResume();

        JobInfo reminderJob = JobSchedulerService.getReminderJob(getContext());
        if (reminderJob != null) {
            mReminderJobScheduled = true;
            Log.d("SettingFragment", reminderJob.toString());
        }
    }
}
