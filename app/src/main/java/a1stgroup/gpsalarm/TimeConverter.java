package a1stgroup.gpsalarm;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by student on 17.21.2.
 */

public class TimeConverter {

    Calendar calendarForConvert;

    TimeConverter(long millis) {
        calendarForConvert = Calendar.getInstance();
        calendarForConvert.setTimeInMillis(millis);
    }

    public static Date setDate(Date date) {
        int year = date.getYear() - 1900;
        int month = date.getMonth();
        int hour = date.getHours();
        int minute = date.getMinutes();
        int second = date.getSeconds();
        Date newDate = new Date(year, month, hour, minute, second);
        return newDate;
    }


//    public String toString() {
//        return "Your alarm will be enabled after: " + calendarForConvert.get(Calendar.YEAR) + " years " + calendarForConvert.get(Calendar.MONTH) + " months " +
//                calendarForConvert.get(Calendar.DAY_OF_MONTH) + " days " + calendarForConvert.get(Calendar.HOUR) + " hours "
//                + calendarForConvert.get(Calendar.MINUTE) + " minutes ";
//    }
}