package com.daohoangson.lumind.schedule;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.daohoangson.lumind.Constant;
import com.daohoangson.lumind.MainActivity;
import com.daohoangson.lumind.R;
import com.daohoangson.lumind.model.DataStore;
import com.daohoangson.lumind.model.Lumindate;
import com.daohoangson.lumind.model.Reminder;
import com.daohoangson.lumind.model.ReminderPersist;
import com.daohoangson.lumind.utils.NextOccurrence;
import com.daohoangson.lumind.utils.StringUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReminderEngine {

    private static final String TAG = "ReminderEngine";

    public static void remind(Context context) {
        List<ReminderPersist> reminders = DataStore.getRemindersReadOnly(context);
        if (reminders.size() == 0) {
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

        Calendar since = Calendar.getInstance();

        for (ReminderPersist reminder : reminders) {
            if (!reminder.enabled) {
                continue;
            }

            Lumindate date = new Lumindate(reminder.timeInMillis);
            boolean monthly = reminder.getMonthly();
            Calendar no = NextOccurrence.lunar(since, date, monthly);
            int days = StringUtil.calculateDays(since, no);
            List<Integer> when = reminder.getWhen();
            if (!when.contains(days)) {
                continue;
            }

            String contentTitle = reminder.getName();
            if (TextUtils.isEmpty(contentTitle)) {
                contentTitle = new Reminder(reminder).getNameForShow(context);
            }

            String formattedDate = DateUtils.formatDateTime(context, date.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
            String nextOccurrenceWhen = StringUtil.formatNextOccurrenceWhen(context.getResources(), days);
            String contentText = context.getString(R.string.next_occurrence_date_x_when_y, formattedDate, nextOccurrenceWhen);
            int ntfId = (int) (no.getTimeInMillis() / 1000 / 86400);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constant.NOTIFICATION_CHANNEL_REMINDER_ID)
                    .setSmallIcon(R.drawable.ic_moon_white)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(MainActivity.newNtfIntent(context, ntfId, reminder.uuid));
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(ntfId, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);

            manager.notify(ntfId, builder.build());

            Log.d(TAG, String.format(Locale.US, "Built notification #%d %s", ntfId, contentTitle));
        }
    }

    public static void cancelNotification(Context context, int ntfId) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.cancel(ntfId);
    }
}
