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
    static MapActivity mapActivity;
    static WakeLock wakeLock; //NOTE: Controls whether screen must kept on or off.
    //NOTE: PowerManager controls CPU usage. PARTIAL_WAKE_LOCK = cpu(on), screen(off), keyboard(off)

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w(TAG, "started");
        if (mapActivity != null && mapActivity.getEstimate() != MapActivity.Estimate.DISABLED) {
            PowerManager powerManager;
            powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "org.gpsalarm");
            acquireLock();
            mapActivity.renewLocationRequest();
            // This loop is because recent Android versions limit alarms to >=60 seconds
            /*-
            do {
                mapActivity.renewLocationRequest();
                try {
                    Log.v(TAG, "sleeping for: " + interval);
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Errror: " + e + "\n" + InternalStorage.stackTraceToString(e));
                }
            }
            while (mapActivity.getEstimate() == MapActivity.Estimate.NEAR);
            */

            // Wake lock is released only if FAR away from MapActivity
        }
    }

    public void setAlarm(Context context, int interval) {
        Log.d(TAG, "setAlarm called. interval: " + interval);
        mapActivity = (MapActivity) context;
        AlarmManager alarmManager;
        PendingIntent alarmIntent;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, alarmIntent);
    }

    void acquireLock() {
        Log.d(TAG, "acquireLock");
        //Acquire the lock, if not still hold
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire();
            Log.v(TAG, "lock acquired");
        }
    }

    void releaseWakeLock() {
        // Release lock if is hold
        Log.d(TAG, "releaseWakeLock");
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            Log.v(TAG, "lock released");
        } else
            Log.v(TAG, "lock is empty or not held");
    }
}