package a1stgroup.gpsalarm;



import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MarkerData implements Serializable {

    private String name = "Location";
    private double latitude = 100.1;
    private double longitude;
//    private Calendar timeToEnable = Calendar.getInstance();
//    Date date;
    private long enablingTime;

    public MarkerData() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getEnablingTime() {
        return enablingTime;
    }

    public void setEnablingTime(long enablingTime) {
        this.enablingTime = enablingTime;
    }

    public boolean isReal() {
        return latitude < 100.0;
    }
}
