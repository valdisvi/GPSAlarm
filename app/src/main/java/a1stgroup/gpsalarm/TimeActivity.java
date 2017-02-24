package a1stgroup.gpsalarm;

/**
 * Created by student on 17.22.2.
 */

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;


public class TimeActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        View myView = (LayoutInflater.from(this)).inflate(R.layout.input_name, null);
        final TimePicker timePicker = (TimePicker) myView.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                timePicker.setCurrentHour(hourOfDay);
                timePicker.setCurrentMinute(minute);
            }
        });

        Calendar calendar = Calendar.getInstance();

        Date currentDate = calendar.getTime();
        int hour = timePicker.getCurrentHour();
        int minutes = timePicker.getCurrentMinute();
        int year = calendar.YEAR;
        int month = currentDate.getMonth();
        int day = 23;
        Date enablingDate = new Date(117, month, day, hour, minutes);
        Calendar calendarForSettingEnablingDate = Calendar.getInstance();
        calendarForSettingEnablingDate.setTime(enablingDate);

        }
}