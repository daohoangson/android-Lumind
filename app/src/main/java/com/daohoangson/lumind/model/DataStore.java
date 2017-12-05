package com.daohoangson.lumind.model;

import android.content.Context;
import android.support.annotation.NonNull;

import com.daohoangson.lumind.model.Reminder;
import com.daohoangson.lumind.model.ReminderPersist;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * @author sondh
 */
public class DataStore {

    public static void saveEditingReminder(final Context context, final Reminder reminder, final OnTransactionCompleteListener listener) {
        getInstance(context).executeTransactionAsync(
                realm -> realm.insertOrUpdate(reminder.build()),
                () -> {
                    if (listener != null) {
                        listener.onTransactionSuccess();
                    }
                }, error -> {
                    if (listener != null) {
                        listener.onTransactionError(error);
                    }
                }
        );
    }

    public static void deleteReminder(Context context, final String uuid) {
        getInstance(context).executeTransactionAsync(
                realm -> realm.where(ReminderPersist.class)
                        .equalTo("uuid", uuid)
                        .findAll()
                        .deleteAllFromRealm()
        );
    }

    public static void getReminders(Context context, final OnGetRemindersResults listener) {
        RealmQuery<ReminderPersist> query = getInstance(context).where(ReminderPersist.class);

        final RealmResults<ReminderPersist> realmResults = query.findAllAsync();
        realmResults.addChangeListener(new RealmChangeListener<RealmResults<ReminderPersist>>() {
            @Override
            public void onChange(@NonNull RealmResults<ReminderPersist> elements) {
                List<Reminder> results = new ArrayList<>(elements.size());
                for (ReminderPersist element : elements) {
                    results.add(new Reminder(element));
                }
                listener.onResults(results);

                realmResults.removeChangeListener(this);
            }
        });
    }

    public static void closeIfOpened() {
        if (sInstanceRef == null) {
            return;
        }

        sInstanceRef.get().close();
        sInstanceRef = null;
    }

    public interface OnTransactionCompleteListener {
        void onTransactionSuccess();

        void onTransactionError(Throwable error);
    }

    public interface OnGetRemindersResults {
        void onResults(List<Reminder> results);
    }

    private static WeakReference<Realm> sInstanceRef;

    private static Realm getInstance(Context context) {
        if (sInstanceRef != null) {
            return sInstanceRef.get();
        }

        Realm.init(context.getApplicationContext());

        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm r = Realm.getInstance(realmConfig);

        sInstanceRef = new WeakReference<>(r);

        return r;
    }

}
