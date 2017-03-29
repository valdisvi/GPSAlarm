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
        if (mapsActivity != null && mapsActivity.getEstimate() != MapsActivity.Estimate.DISABLED) {
            Log.d(TAG, "OnReceive called...");
            PowerManager powerManager;
            powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "org.gpsalarm");
            acquireLock();
            mapsActivity.renewLocationRequest();
            releaseWakeLock();
        }
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
        //Acquire the lock, if not still hold
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire();
            Log.v(TAG, "lock acquired");
        }
    }

    void releaseWakeLock() {
        // Release lock if is hold
        Log.v(TAG, "releaseWakeLock");
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            Log.v(TAG, "lock released");
        }
        Log.v(TAG, "lock is empty or not held");
    }
}