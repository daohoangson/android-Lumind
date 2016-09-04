package com.daohoangson.lumind.model;

import android.content.Context;

import com.daohoangson.lumind.model.Reminder;
import com.daohoangson.lumind.model.ReminderPersist;

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
        getInstance(context).executeTransactionAsync(new io.realm.Realm.Transaction() {
            @Override
            public void execute(io.realm.Realm realm) {
                realm.insertOrUpdate(reminder.build());
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                if (listener != null) {
                    listener.onTransactionSuccess();
                }
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                if (listener != null) {
                    listener.onTransactionError(error);
                }
            }
        });
    }

    public static void deleteReminder(Context context, final String uuid) {
        getInstance(context).executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(ReminderPersist.class)
                        .equalTo("uuid", uuid)
                        .findAll()
                        .deleteAllFromRealm();
            }
        });
    }

    public static List<Reminder> getReminders(Context context, final OnGetRemindersResults listener) {
        RealmQuery<ReminderPersist> query = getInstance(context).where(ReminderPersist.class);

        if (listener != null) {
            final RealmResults<ReminderPersist> realmResults = query.findAllAsync();
            realmResults.addChangeListener(new RealmChangeListener<RealmResults<ReminderPersist>>() {
                @Override
                public void onChange(RealmResults<ReminderPersist> elements) {
                    List<Reminder> results = new ArrayList<>(elements.size());
                    for (ReminderPersist element : elements) {
                        results.add(new Reminder(element));
                    }
                    listener.onResults(results);

                    realmResults.removeChangeListener(this);
                }
            });
            return null;
        } else {
            RealmResults<ReminderPersist> realmResults = query.findAll();
            List<Reminder> results = new ArrayList<>(realmResults.size());
            for (ReminderPersist element : realmResults) {
                results.add(new Reminder(element));
            }

            return results;
        }
    }

    public interface OnTransactionCompleteListener {
        void onTransactionSuccess();

        void onTransactionError(Throwable error);
    }

    public interface OnGetRemindersResults {
        void onResults(List<Reminder> results);
    }

    static Realm getInstance(Context context) {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context)
                .deleteRealmIfMigrationNeeded()
                .build();

        return Realm.getInstance(realmConfig);
    }

}
