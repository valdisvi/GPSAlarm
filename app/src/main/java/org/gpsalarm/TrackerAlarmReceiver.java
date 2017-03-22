package org.gpsalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TrackerAlarmReceiver extends BroadcastReceiver {
    final public static String ONE_TIME = "onetime";

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TrackerAlarmReceiver");
        //Acquire the lock
        wl.acquire();

        // Do updates
        Format formatter = new SimpleDateFormat("HH:mm:ss");
        String msg = (formatter.format(new Date()));
        // FIXME this doesn't work
        MapsActivity mapsActivity = MapsActivity.getMapsActivity();
        mapsActivity.startLocationRequest();
        Log.d("TrackerAlarmReceiver", "onReceive: " + msg);

        //Release the lock
        wl.release();

    }

    public void setAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TrackerAlarmReceiver.class);
        intent.putExtra(ONE_TIME, Boolean.FALSE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), MapsActivity.interval, pi);
        Log.d("TrackerAlarmReceiver", "alarm set to:" + MapsActivity.interval);
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, TrackerAlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        Log.d("TrackerAlarmReceiver","alarm canceled");
;    }


}
