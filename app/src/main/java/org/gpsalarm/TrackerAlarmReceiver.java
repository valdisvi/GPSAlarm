package org.gpsalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TrackerAlarmReceiver extends WakefulBroadcastReceiver {
    final public static String ONE_TIME = "onetime";
    static MapsActivity mapsActivity;
    static PowerManager powerManager;
    static WakeLock wakeLock;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(), TrackerService.class.getName());
        Intent service = new Intent(context, TrackerService.class);
        startWakefulService(context, intent.setComponent(comp));
    }

    //@Override
    public void onReceivePrev(Context context, Intent intent) {
        Format formatter = new SimpleDateFormat("HH:mm:ss");
        String msg = (formatter.format(new Date()));
        Log.d("onReceive", "time: " + msg);
        if (powerManager == null)
            powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (wakeLock == null)
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TrackerAlarmReceiver");
        //Acquire the lock, if not still/yet held
        acquireLock();
        // Start location requests
        Log.d("onReceive", "context" + context);
        if (mapsActivity != null)
            mapsActivity.renewLocationRequest();
        // Reset alarm time
        setAlarm(MapsActivity.getMapsActivity());
    }

    public void setAlarm(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TrackerService.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        /*-
        ComponentName receiver = new ComponentName(context, MapBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        */
    }


    void setAlarmPrev(Context context) {
        if (context instanceof MapsActivity) {
            mapsActivity = (MapsActivity) context;
            Log.d("setAlarm", "mapsActivity is set");
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TrackerAlarmReceiver.class);
        intent.putExtra(ONE_TIME, Boolean.FALSE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + MapsActivity.interval, MapsActivity.interval, pi);
        Log.d("TrackerAlarmReceiver", "alarm set to:" + MapsActivity.interval);
    }

    void cancelAlarm(Context context) {
        if (context instanceof MapsActivity) {
            mapsActivity = (MapsActivity) context;
            Log.d("cancelAlarm", "mapsActivity is set");
        }
        Intent intent = new Intent(context, TrackerAlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        Log.d("TrackerAlarmReceiver", "alarm canceled");
    }

    void releaseWakeLock() {
        //Release the lock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            Log.d("releaseWakeLock", "lock released");
        }
    }

    void acquireLock() {
        //Acquire the lock, if not still held
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire();
            Log.d("acquireLock", "lock acquired");
        }
    }

}
