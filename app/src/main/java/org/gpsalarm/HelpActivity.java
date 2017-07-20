package org.gpsalarm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.gpsalarm.R;


public class HelpActivity extends AppCompatActivity {

    private static final String TAG = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Help) called");
        setContentView(R.layout.activity_help);

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart(Help) called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause(Help) called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume(Help) called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop(Help) called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy(Help) called");
    }

}