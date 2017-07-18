package org.gpsalarm;


import android.app.Service;
import android.media.MediaPlayer;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;

public class AlarmSoundService extends Service{
    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        //Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        mediaPlayer = MediaPlayer.create(this, R.raw.annoying);
        mediaPlayer.start();
        mediaPlayer.setLooping(true);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }
}
