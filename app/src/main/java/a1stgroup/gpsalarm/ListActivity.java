package a1stgroup.gpsalarm;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Map;

import static a1stgroup.gpsalarm.R.id.editText;


public class ListActivity extends AppCompatActivity {

    static MarkerData selectedMarkerData = new MarkerData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        final ArrayAdapter myAdapter = new MyCustomizedAdapter(this, MapsActivity.markerDataList);

        final ListView myListView = (ListView) findViewById(R.id.idOfListView);

        myListView.setAdapter(myAdapter);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

              //  String pickedWord = "You touched " + String.valueOf(adapterView.getItemAtPosition(i));
              //  Toast.makeText(ListActivity.this, pickedWord, Toast.LENGTH_LONG).show();

                selectedMarkerData = (MarkerData) myAdapter.getItem(i);

                Toast.makeText(ListActivity.this, "Alarm Set: " + selectedMarkerData.getName(), Toast.LENGTH_SHORT).show();
              //  Toast.makeText(ListActivity.this, "Latitude: " + selectedMarkerData.getLatitude(), Toast.LENGTH_SHORT).show();
              //  Toast.makeText(ListActivity.this, "Longitude: " + selectedMarkerData.getLongitude(), Toast.LENGTH_SHORT).show();

                Intent myIntent = new Intent(ListActivity.this, MapsActivity.class);
                startActivity(myIntent);
            }
        });

        myListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                AlertDialog.Builder alert = new AlertDialog.Builder(ListActivity.this);
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

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(01);
    }

}