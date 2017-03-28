package org.gpsalarm;


import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * This class is used to read/write objects into persistent store
 */
final class InternalStorage extends Application {
    private final String LOCATION_DATA_LIST = "locationDataList";
    private final String INTERVAL = "interval";
    private final String LOCATION_DATA = "locationData";
    private Context context;

    void writeObject(Context context, String key, Object object) {
        try {
            FileOutputStream fos = context.openFileOutput(key, Context.CONTEXT_RESTRICTED);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.flush();
            oos.close();
            fos.close();
        } catch (Exception e) {
            Log.e("writeObject", "Exception:" + e.getMessage() + "\n" + stackTraceToString(e));
        }

    }

    Object readObject(Context context, String key) {
        try {
            FileInputStream fis = context.openFileInput(key);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object object = ois.readObject();
            fis.close();
            ois.close();
            return object;
        } catch (Exception e) {
            Log.e("readObject", "Exception:" + e.getMessage() + "\n" + stackTraceToString(e));
        }
        return null;
    }

    void writeLocationDataList(ArrayList<LocationData> locationDataList) {
        checkContext();
        writeObject(context, LOCATION_DATA_LIST, locationDataList);
        Log.d("writeLocationDataList", "locationDataList:" + locationDataList);
    }

    ArrayList<LocationData> readLocationDataList() {
        checkContext();
        ArrayList<LocationData> list =
                (ArrayList<LocationData>) readObject(context, LOCATION_DATA_LIST);
        if (list == null) {
            Log.w("readLocationDataList", "Empty list was created");
            list = new ArrayList<>();
        }
        Log.d("readLocationDataList", "list:" + list);
        return list;
    }

    void writeInterval(int interval) {
        checkContext();
        writeObject(context, INTERVAL, new Integer(interval));
        Log.d("writeInterval", "interval:" + interval);
    }

    int readInterval() {
        checkContext();
        Integer interval = 0;
        Object object = readObject(context, INTERVAL);
        if (object != null)
            interval = (Integer) object;
        Log.d("readInterval", "interval:" + interval);
        return (int) interval;
    }

    void writeLocationData(LocationData locationData) {
        checkContext();
        writeObject(context, LOCATION_DATA, locationData);
        Log.d("writeLocationData", "writeLocationData:" + locationData);
    }

    LocationData readLocationData() {
        checkContext();
        LocationData locationData = (LocationData) readObject(context, LOCATION_DATA);
        Log.d("readLocationData", "locationData:" + locationData);
        return locationData;
    }

    Location readLocation() {
        LocationData locationData;
        Location location = location = new Location("Location");
        Object object = readLocationData();
        if (object != null) {
            locationData = (LocationData) object;
            location.setProvider(locationData.name);
            location.setLatitude(locationData.latitude);
            location.setLongitude(locationData.longitude);
        } else
            Log.w("readLocation", "Empty location was created");
        Log.d("readLocation", "location:" + location);
        return location;
    }

    void writeLocation(Location location) {
        LocationData data = new LocationData(location.getProvider());
        data.setLatitude(location.getLatitude());
        data.setLongitude(location.getLongitude());
        writeLocationData(data);
        Log.d("writeLocation", "writeLocation:" + location);
    }

    void checkContext() {
        if (context == null) {
            Log.e("InternalStorage", "context is null!" + stackTraceToString(new Exception()));
        }
    }

    static String stackTraceToString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    void setContext(Context context) {
        this.context = context;
    }

    Context getContext() {
        return context;
    }

}