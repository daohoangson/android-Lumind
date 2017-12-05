package com.daohoangson.lumind.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.daohoangson.lumind.utils.PrefUtil;

import java.util.Calendar;

public class AlarmReceiver extends Service {

    private static final String TAG = "AlarmReceiver";

    public static void setup(Context context) {
        setBootCompletedReceiverEnabled(context, true);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        PendingIntent pendingIntent = createPendingIntent(context);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        Log.d(TAG, String.format("Setup OK, scheduled for %d", calendar.getTimeInMillis()));
    }

    public static void cancel(Context context) {
        setBootCompletedReceiverEnabled(context, false);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        alarmManager.cancel(createPendingIntent(context));

        Log.d(TAG, "Cancel OK");
    }

    private static PendingIntent createPendingIntent(Context context) {
        Context ac = context.getApplicationContext();
        Intent intent = new Intent(ac, AlarmReceiver.class);
        return PendingIntent.getService(ac, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static void setBootCompletedReceiverEnabled(Context context, boolean enabled) {
        ComponentName receiver = new ComponentName(context, BootCompletedReceiver.class);
        PackageManager pm = context.getPackageManager();
        int flag = enabled ?
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        pm.setComponentEnabledSetting(receiver, flag, PackageManager.DONT_KILL_APP);

        Log.d(TAG, "setBootCompletedReceiverEnabled OK");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand...");

        if (PrefUtil.getRemind(this)) {
            ReminderEngine.remind(this);
        } else {
            cancel(this);
        }

        Log.i(TAG, "onStartCommand OK");

        return START_NOT_STICKY;
    }
}
