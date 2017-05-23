package com.daohoangson.lumind.schedule;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.daohoangson.lumind.Constant;
import com.daohoangson.lumind.MainActivity;
import com.daohoangson.lumind.R;
import com.daohoangson.lumind.model.DataStore;
import com.daohoangson.lumind.model.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author sondh
 */
public class ReminderEngine {



    public static void remind(Context context, Calendar at) {

        Calendar cutOff = (Calendar) at.clone();
        cutOff.add(Calendar.DATE, 1);
        Date cutOffDate = cutOff.getTime();

        List<Reminder> reminders = DataStore.getReminders(context, null);
        if (reminders == null) {
            return;
        }

        List<Reminder> comingSoon = new ArrayList<>(reminders.size());
        for(Reminder reminder: reminders) {
            if (reminder.getNextOccurrence(at).before(cutOffDate)) {
                comingSoon.add(reminder);
            }
        }

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Reminder")
                .setContentText("Blah blah blah");

        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notifBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constant.NOTIFICATION_ID_REMINDER, notifBuilder.build());
    }

}
