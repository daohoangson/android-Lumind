package com.daohoangson.lumind.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

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

    private static final String TAG = "DataStore";

    public static void saveReminder(Context context, Reminder reminder, OnTransactionCompleteListener listener) {
        getInstance(context).executeTransactionAsync(
                realm -> realm.insertOrUpdate(reminder.build()),
                () -> {
                    if (listener != null) {
                        listener.onTransactionSuccess();
                    }

                    Log.d(TAG, String.format("saveReminder OK reminder=%s", reminder));
                }, error -> {
                    if (listener != null) {
                        listener.onTransactionError(error);
                    }

                    Log.e(TAG, String.format("saveReminder failed reminder=%s, error=%s", reminder, error));
                }
        );
    }

    public static void deleteReminder(Context context, final String uuid) {
        getInstance(context).executeTransactionAsync(
                realm -> {
                    boolean deleted = realm.where(ReminderPersist.class)
                            .equalTo("uuid", uuid)
                            .findAll()
                            .deleteAllFromRealm();

                    Log.d(TAG, String.format("deleteReminder OK uuid=%s, deleted=%s", uuid, deleted));
                }
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

                Log.d(TAG, String.format("getReminders OK results.size=%d", results.size()));
            }
        });

        Log.d(TAG, "getReminders...");
    }

    public static List<ReminderPersist> getRemindersReadOnly(Context context) {
        RealmQuery<ReminderPersist> query = getInstance(context).where(ReminderPersist.class);
        return query.findAll();
    }

    public static void closeIfOpened() {
        if (sInstanceRef == null) {
            Log.d(TAG, "closeIfOpened no op");
            return;
        }

        sInstanceRef.get().close();
        sInstanceRef = null;

        Log.d(TAG, "closeIfOpened OK");
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
            Log.d(TAG, "getInstance reused");
            return sInstanceRef.get();
        }

        Realm.init(context.getApplicationContext());

        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm r = Realm.getInstance(realmConfig);
        Log.d(TAG, "getInstance built");

        sInstanceRef = new WeakReference<>(r);

        return r;
    }

}
