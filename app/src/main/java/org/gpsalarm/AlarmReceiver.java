package org.gpsalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.os.PowerManager.*;

import static android.content.Context.POWER_SERVICE;
import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;

public class AlarmReceiver extends BroadcastReceiver {

    MapActivity serial = new MapActivity();

    @Override
    public void onReceive(Context context, Intent intent){
        int checkCode = serial.getStatusForAlarm();
        switch(checkCode){
            case 0x0110:{ //sound alarm
                Toast.makeText(context, "ALARM!! ALARM!!", Toast.LENGTH_SHORT).show();
                context.startService(new Intent(context, AlarmSoundService.class));
                ComponentName componentName = new ComponentName(context.getPackageName(),
                        AlarmNotificationService.class.getName());
                startWakefulService(context, (intent.setComponent(componentName)));
            }
            case 0x0001:{
                //do something
            }
            default:{
                //default error message
            }
        }

    }

}