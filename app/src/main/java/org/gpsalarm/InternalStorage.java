package org.gpsalarm;


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
final class InternalStorage {
    static final String LOCATION_DATA_LIST = "locationDataList";
    static final String INTERVAL = "interval";
    static final String LOCATION_DATA = "locationData";
    static Context context;

    static void setContext(Context context) {
        InternalStorage.context = context;
    }

    static void writeObject(Context context, String key, Object object) {
        try {
            FileOutputStream fos = context.openFileOutput(key, Context.CONTEXT_IGNORE_SECURITY);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
            fos.close();
        } catch (Exception e) {
            Log.e("writeObject", "Exception:" + e.getStackTrace() + "\n" + e.getStackTrace());
        }

    }

    static Object readObject(Context context, String key) {
        try {
            FileInputStream fis = context.openFileInput(key);
            ObjectInputStream ois = new ObjectInputStream(fis);
            ois.close();
            fis.close();
            return ois.readObject();
        } catch (Exception e) {
            Log.e("readObject", "Exception:" + e.getStackTrace() + "\n" + e.getStackTrace());
        }
        return null;
    }

    static void writeLocationDataList(ArrayList<LocationData> locationDataList) {
        checkContext();
        InternalStorage.writeObject(context, LOCATION_DATA_LIST, locationDataList);
        Log.d("writeLocationDataList", "locationDataList:" + locationDataList);
    }

    static ArrayList<LocationData> readLocationDataList() {
        checkContext();
        ArrayList<LocationData> list =
                (ArrayList<LocationData>) InternalStorage.readObject(context, LOCATION_DATA_LIST);
        if (list==null)
            list = new ArrayList<>();
        Log.d("readLocationDataList", "list:" + list);
        return list;
    }

    static void writeInterval(int interval) {
        checkContext();
        InternalStorage.writeObject(context, INTERVAL, new Integer(interval));
        Log.d("writeInterval", "interval:" + interval);
    }

    static int readInterval() {
        checkContext();
        Integer interval = 0;
        Object object = InternalStorage.readObject(context, INTERVAL);
        if (object != null)
            interval = (Integer) object;
        Log.d("readInterval", "interval:" + interval);
        return (int) interval;
    }

    static void writeLocationData(LocationData locationData) {
        checkContext();
        InternalStorage.writeObject(context, LOCATION_DATA, locationData);
        Log.d("writeLocationData", "writeLocationData:" + locationData);
    }

    static LocationData readLocationData() {
        checkContext();
        LocationData locationData = (LocationData) InternalStorage.readObject(context, LOCATION_DATA);
        Log.d("readLocationData", "locationData:" + locationData);
        return locationData;
    }

    static Location readLocation() {
        LocationData locationData;
        Location location = location = new Location("Location");
        Object object = readLocationData();
        if (object != null) {
            locationData = (LocationData) object;
            location.setProvider(locationData.name);
            location.setLatitude(locationData.latitude);
            location.setLongitude(locationData.longitude);
        }
        Log.d("readLocation","location:" + location);
        return location;
    }

    static void writeLocation(Location location) {
        LocationData data = new LocationData(location.getProvider());
        data.setLatitude(location.getLatitude());
        data.setLongitude(location.getLongitude());
        writeLocationData(data);
        Log.d("writeLocation", "writeLocation:" + location);
    }

    static void checkContext() {
        if (context == null)
            Log.e("InternalStorage", "context is null" + Thread.currentThread().getStackTrace());
    }

}