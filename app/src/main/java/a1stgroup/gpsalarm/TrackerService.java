package a1stgroup.gpsalarm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by student on 16.9.11.
 */


public class TrackerService extends IntentService {
    MapsActivity mapsActivity;
    NotificationManager mapNotificationManager;



    public TrackerService() {
        super("TrackerService");
    }

    protected void onHandleIntent(Intent intent) {
            mapsActivity.trackLocation();
            sendNotification("Tracking");
    }

    private void sendNotification(String msg) {
        mapNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent mapIntent = new Intent(this.getApplicationContext(), MapsActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this,0,mapIntent,0);

        Notification mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_web)
                .setContentTitle("Alarm manager")
                .setContentIntent(contentIntent)
                .setContentText(msg)
                .build();

        mapNotificationManager.notify(0, mBuilder);
    }

}
