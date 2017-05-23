package com.daohoangson.lumind.schedule;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author sondh
 */
public class JobSchedulerService extends JobService {

    public static final String TAG = "JobSchedulerService";
    public static final int REMINDER_JOB_ID = 1;

    public static boolean scheduleReminderJob(Context context, boolean isPersisted, long periodHours) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        long intervalMillis = TimeUnit.MINUTES.toMillis(periodHours);
        List<JobInfo> jobs = jobScheduler.getAllPendingJobs();
        for (JobInfo job : jobs) {
            if (job.getId() != REMINDER_JOB_ID) {
                // different job id, ignore
                continue;
            }

            if (job.isPersisted() != isPersisted) {
                // persisted flag mismatched, ignore
                continue;
            }

            if (!job.isPeriodic() || job.getIntervalMillis() != intervalMillis) {
                // period config mismatched, ignore
                continue;
            }

            Log.i(TAG, "scheduleReminderJob found existing pending job");
            return false;
        }

        JobInfo.Builder builder = new JobInfo.Builder(REMINDER_JOB_ID,
                new ComponentName(context.getPackageName(), JobSchedulerService.class.getName()))
                .setPersisted(isPersisted)
                .setPeriodic(intervalMillis);

        int scheduled = jobScheduler.schedule(builder.build());
        if (scheduled <= 0) {
            Log.e(TAG, String.format("scheduleReminderJob encountered an error %d", scheduled));
            return false;
        }

        Log.i(TAG, "scheduleReminderJob ok");
        return true;
    }

    public static JobInfo getReminderJob(Context context) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        List<JobInfo> jobs = jobScheduler.getAllPendingJobs();
        for (JobInfo job : jobs) {
            if (job.getId() != REMINDER_JOB_ID) {
                continue;
            }

            return job;
        }

        return null;
    }

    Handler mReminderJobHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            Log.i(TAG, String.format("mReminderJobHandler handleMessage %s", msg.toString()));
            ReminderEngine.remind(getApplicationContext(), Calendar.getInstance());
            Log.i(TAG, "mReminderJobHandler ok");

            jobFinished((JobParameters) msg.obj, false);
            return true;
        }

    });

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        mReminderJobHandler.sendMessage(Message.obtain(mReminderJobHandler, REMINDER_JOB_ID, jobParameters));
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        mReminderJobHandler.removeMessages(REMINDER_JOB_ID);
        return false;
    }
}
