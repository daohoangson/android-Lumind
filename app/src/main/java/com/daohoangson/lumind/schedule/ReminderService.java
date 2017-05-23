package com.daohoangson.lumind.schedule;

import android.content.Context;
import android.util.Log;

import com.daohoangson.lumind.BuildConfig;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author sondh
 */
public class ReminderService extends JobService {

    public static final String TAG = "ReminderService";

    public static void scheduleSelf(Context context, boolean enable) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        int result;
        if (enable) {
            int period = (int) TimeUnit.HOURS.toSeconds(6);
            int periodFlex = (int) TimeUnit.HOURS.toSeconds(1);

            Job job = dispatcher.newJobBuilder()
                    .setLifetime(Lifetime.FOREVER)
                    .setRecurring(true)
                    .setReplaceCurrent(true)
                    .setService(ReminderService.class)
                    .setTag(TAG)
                    .setTrigger(Trigger.executionWindow(periodFlex, period))
                    .build();

            result = dispatcher.schedule(job);
        } else {
            result = dispatcher.cancel(TAG);
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format(Locale.US, "%s: enabled=%s -> result=%d",
                    ReminderService.class.getName(), enable, result));
        }
    }

    @Override
    public boolean onStartJob(JobParameters job) {
        ReminderEngine.remind(getApplicationContext());

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}
