package com.daohoangson.lumind.schedule;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.daohoangson.lumind.Constant;
import com.daohoangson.lumind.MainActivity;
import com.daohoangson.lumind.R;
import com.daohoangson.lumind.model.DataStore;
import com.daohoangson.lumind.model.Reminder;
import com.daohoangson.lumind.utils.PrefUtil;
import com.daohoangson.lumind.utils.StringUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author sondh
 */
public class ReminderEngine {

    private static final String TAG = "ReminderEngine";

    public static void remind(Context context) {
        DataStore.getReminders(context, results -> onReminders(context, results));
    }

    public static void cancelNotification(Context context, int ntfId) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.cancel(ntfId);
    }

    private static void onReminders(Context context, List<Reminder> reminders) {
        if (reminders.size() == 0) {
            return;
        }

        Calendar at = Calendar.getInstance();
        Calendar cutOff = (Calendar) at.clone();
        cutOff.add(Calendar.DATE, PrefUtil.getRemindHowFar(context));
        Date cutOffDate = cutOff.getTime();

        List<Reminder> comingSoon = new ArrayList<>(reminders.size());
        for (Reminder reminder : reminders) {
            Date no = reminder.getNextOccurrence(at);
            if (no.before(cutOffDate)) {
                comingSoon.add(reminder);
            }
        }
        if (comingSoon.size() == 0) {
            return;
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Constant.NOTIFICATION_CHANNEL_REMINDER_ID,
                    context.getString(R.string.reminder_ntf_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);

            manager.createNotificationChannel(channel);

            Log.d(TAG, "Created notification channel");
        }

        for (Reminder reminder : comingSoon) {
            String contentTitle = reminder.name.get();
            String nextOccurrenceInX = StringUtil.formatNextOccurrenceInX(context.getResources(), at, reminder.getNextOccurrence(at));
            String contentText = reminder.getDayMonthYear(context) + nextOccurrenceInX;
            int ntfId = (int) (reminder.getNextOccurrence(at).getTime() / 1000 / 86400);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constant.NOTIFICATION_CHANNEL_REMINDER_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(MainActivity.newNtfIntent(context, ntfId, reminder));
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(ntfId, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);

            manager.notify(ntfId, builder.build());

            Log.d(TAG, String.format(Locale.US, "Built notification #%d %s", ntfId, contentTitle));
        }
    }
}
