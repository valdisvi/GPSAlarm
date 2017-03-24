package org.gpsalarm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.gpsalarm.R;

/**
 * Created by student on 16.9.11.
 */
class TrackerService extends IntentService {
    NotificationManager mapNotificationManager;


    public TrackerService(String name) {
        super(name);
    }

    protected void onHandleIntent(Intent intent) {

        // TODO implement coordinate update here
        if (TrackerAlarmReceiver.mapsActivity != null) {
            TrackerAlarmReceiver.mapsActivity.renewLocationRequest();
            Log.d("TrackerService", "onHandleIntent: renewLocationRequest() called");
        } else
            Log.e("TrackerService", "onHandleIntent: mapsActivity == null");
    }


}
