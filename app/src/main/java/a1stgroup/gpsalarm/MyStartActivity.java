package a1stgroup.gpsalarm;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;


import static a1stgroup.gpsalarm.MapsActivity.markerDataList;

/**
 * Prilozhenie startuet s etoj stranici, esli estj soxranennie tochki, inache perexodit v MapsActivity.
 */

public class MyStartActivity extends AppCompatActivity {

    static MarkerData selectedMarkerData = new MarkerData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            markerDataList = (ArrayList<MarkerData>) InternalStorage.readObject(this, "myFile");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (markerDataList.size() == 0) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        } else {
            setContentView(R.layout.activity_start);
            final ArrayAdapter myAdapter = new MyCustomizedAdapter(this, MapsActivity.markerDataList);
            ListView listView = (ListView) findViewById(R.id.listView);

            Collections.sort(markerDataList, new Comparator<MarkerData>() {

            /* This comparator will sort MarkerData objects alphabetically. */

                @Override
                public int compare(MarkerData a1, MarkerData a2) {
                    // String implements Comparable
                    return (a1.getName()).compareTo(a2.getName());
                }
            });

            listView.setAdapter(myAdapter);

            final ListView myListView2 = listView;
            myListView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {        // Dobavlenij kod 21.02,2017
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedMarkerData = (MarkerData) myAdapter.getItem(i);
                    Toast.makeText(MyStartActivity.this, "Alarm '" + selectedMarkerData.getName() + "' is set", Toast.LENGTH_LONG).show();
                    Intent myIntent = new Intent(MyStartActivity.this, MapsActivity.class);
                    startActivity(myIntent);
                }
            });

            myListView2.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(MyStartActivity.this);
                    alert.setMessage("Are you sure you want to delete this?");
                    alert.setCancelable(false);
                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MapsActivity.markerDataList.remove(i);
                            myAdapter.notifyDataSetChanged();
                            saveMarkerDataList();
                        }
                    });
                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alert.show();
                    return true;
                }
            });
        }

    }


    private boolean saveMarkerDataList() {
        try {
            InternalStorage.writeObject(this, "myFile", MapsActivity.markerDataList);
            return true;
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save alarm", Toast.LENGTH_SHORT).show();
            Log.e("IOException", e.getMessage());
        }
        return false;
    }

    public void toMap(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

}
