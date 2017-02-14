package a1stgroup.gpsalarm;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * Created by student on 16.9.11.
 */

public class MapBootReceiver extends BroadcastReceiver {
    TrackerAlarmReceiver alarm = new TrackerAlarmReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            alarm.setAlarm(context);
        }
        Intent serviceIntent = new Intent(context, TrackerService.class);
        context.startService(serviceIntent);
    }
}

