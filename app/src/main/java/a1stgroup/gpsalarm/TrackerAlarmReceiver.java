package a1stgroup.gpsalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by student on 16.9.11.
 */

public class TrackerAlarmReceiver extends WakefulBroadcastReceiver {
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(),TrackerService.class.getName());
        Intent service =  new Intent(context,TrackerService.class);
        startWakefulService(context, intent.setComponent(comp));
    }

    public void setAlarm(Context context) {
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context,TrackerService.class);
        alarmIntent = PendingIntent.getBroadcast(context,0,intent,0);

        ComponentName receiver = new ComponentName(context, MapBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

}
