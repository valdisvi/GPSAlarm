package org.gpsalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.os.PowerManager.*;

import static android.content.Context.POWER_SERVICE;

public class AlarmReceiver extends WakefulBroadcastReceiver {
    private final String TAG = "AlarmReceiver";
    static MapsActivity mapsActivity;
    static WakeLock wakeLock;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "OnReceive called...");
        PowerManager powerManager;
        powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "test.org.alarmtest");
        //acquireLock();
        // Do Work
        // TODO alarmActivity.doWork();
        //if (mapsActivity != null)
        wakeLock.acquire();
        ((MapsActivity) MapsActivity.context).renewLocationRequest();
        wakeLock.release();
        //releaseWakeLock();
    }

    public void setAlarm(Context context, int interval) {
        Log.d(TAG, "setAlarm called. interval: " + interval);
        mapsActivity = (MapsActivity) context;
        AlarmManager alarmManager;
        PendingIntent alarmIntent;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, alarmIntent);
    }

    void acquireLock() {
        /*
        //Acquire the lock, if not still held
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire();
            saveWakeLock();
            Log.d(TAG, "lock acquired");
        }
        */
    }

    void releaseWakeLock() {
        /*
        Log.d(TAG, "releaseWakeLock");
        loadWakeLock();
        //Release the lock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            Log.d(TAG, "lock released");
        }
        Log.d(TAG, "lock is empty or not held");
        */
    }

    void saveWakeLock() {
        try {
            InternalStorage.writeObject(mapsActivity, "wakeLock", wakeLock);
        } catch (Exception e) {
            Log.e("IOException", e.getMessage());
        }
        Log.d("saveWakeLock", "saveWakeLock" + wakeLock);

    }

    void loadWakeLock() {
        try {
            wakeLock = (WakeLock) InternalStorage.readObject(mapsActivity, "wakeLock");
        } catch (Exception e) {
            Log.e("IOException", e.getMessage());
        }
        Log.d("loadWakeLock", "saveWakeLock" + wakeLock);
    }
}