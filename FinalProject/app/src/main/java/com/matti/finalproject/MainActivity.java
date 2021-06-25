package com.matti.finalproject;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;

import android.view.Menu;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private DatabaseHelper DBHelper;
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location lastKnownLocation;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private static final String silencerFilter = "com.matti.finalproject.SilenceBroadcastReceiver";
    private SilenceBroadcastReceiver Silencer;

    private static final String unsilencerFilter = "com.matti.finalproject.UnsilenceBroadcastReceiver";
    private UnsilenceBroadcastReceiver Unmute;

    private int DataNumber = 0;
    private final String DATA_KEY = "Data Number";

    private int HighestID = 0;
    private final String HighestID_KEY = "Highest ID";

    private SharedPreferences mPreferences;

    private RecyclerView mRecyclerView;
    private ArrayList<Marker> mMarkersData;
    private MarkersAdapter mAdapter;
    private AudioManager audio;
    private final int gridColumnCount = 1;

    public MainActivity() {
        // this.fusedLocationProviderClient = fusedLocationProviderClient;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab_main);
        fab.setContentDescription(getString(R.string.fab_refresh));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetUpdate();
            }
        });

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        DBHelper = new DatabaseHelper(this);
        Silencer = new SilenceBroadcastReceiver();
        Unmute = new UnsilenceBroadcastReceiver();
        registerReceiver(Silencer, new IntentFilter(silencerFilter));
        registerReceiver(Unmute, new IntentFilter(unsilencerFilter));

        String sharedPrefFile = "com.matti.finalproject";
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        DataNumber = mPreferences.getInt(DATA_KEY, 0);
        HighestID = mPreferences.getInt(HighestID_KEY, 0);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMarkersData = new ArrayList<>();
        mAdapter = new MarkersAdapter(this, mMarkersData, DataNumber);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridColumnCount));
        initializeData();
        audio = (AudioManager) this.getSystemService(AUDIO_SERVICE);
        requestNotificationPolicyAccess();
    }

    private void initializeData() {
            if (DataNumber == 0) {
                mMarkersData.add(new Marker("No marker has been added!", "Press the button below to go to the map and add markers", "", "", ""));
            }
            else {
                for (int i = 1; i <= HighestID; i++) {
                    if (DBHelper.checkID(i)) {
                        String title = DBHelper.getTitle(i);
                        String snippet = DBHelper.getSnippet(i);
                        String markerLat = "Latitude : " + DBHelper.getLat(i);
                        String markerLong = "Longitude : " + DBHelper.getLong(i);
                        String markerMode = DBHelper.getMode(i);
                        mMarkersData.add(new Marker(title, snippet, markerLat, markerLong, markerMode));
                    }
                }
            }
        mAdapter.notifyDataSetChanged();
        mMarkersData = new ArrayList<>();
    }

    public void launchMap(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataNumber = mPreferences.getInt(DATA_KEY, 0);
        HighestID = mPreferences.getInt(HighestID_KEY, 0);
        getLocationPermission();
        silenceMyPhone();
        mMarkersData = new ArrayList<>();
        mAdapter = new MarkersAdapter(this, mMarkersData, DataNumber);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridColumnCount));
        initializeData();

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(Silencer);
        unregisterReceiver(Unmute);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void resetUpdate(){
        initializeData();
        onResume();
    }

    private void SilencePhone(Double currentLatitude, Double currentLongitude) {
        boolean ToSilence = false;
        double closestPlace = 1.0;
        String mode="a";
        if (DataNumber != 0) {
            for (int i = 1; i <= HighestID; i++) {
                if (DBHelper.checkID(i)) {
                    Double markerLat = DBHelper.getLat(i);
                    Double markerLong = DBHelper.getLong(i);
                    double diffLatSqr = currentLatitude - markerLat;
                    diffLatSqr *= diffLatSqr;
                    double diffLongSqr = currentLongitude - markerLong;
                    diffLongSqr *= diffLongSqr;
                    if ((diffLatSqr < 0.00000036)
                            || (diffLongSqr < 0.00000036)) {
                        ToSilence = true;
                        if (closestPlace > (diffLatSqr+diffLongSqr)){
                            mode = DBHelper.getMode(i);
                        }

                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isNotificationPolicyAccessGranted()){
            if (ToSilence){
                if( mode != null) {
                    switch (mode) {
                        case "Silent":
                            if (audio.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE){
                                audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            }
                            audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                            break;
                        case "Vibrate":
                            audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                            break;
                        case "Normal":
                            audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            break;
                    }
                }

            }
            else {
                audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        }
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            if (ToSilence){
                if(mode != null) {
                    switch (mode) {
                        case "Silent":
                            if (audio.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE){
                                audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            }
                            audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                            break;
                        case "Vibrate":
                            audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                            break;
                        case "Normal":
                            audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            break;
                    }
                }

            }
            else {
                audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        }
    }

    private void requestNotificationPolicyAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(android.provider.Settings.
                    ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isNotificationPolicyAccessGranted()  {
        NotificationManager notificationManager = (NotificationManager)
                MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager.isNotificationPolicyAccessGranted();
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }



    private void silenceMyPhone() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                SilencePhone(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
    // [END maps_current_place_get_device_location]

    /**
     * Prompts the user for permission to use the device location.
     */
    // [START maps_current_place_location_permission]
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    // [END maps_current_place_location_permission]

    /**
     * Handles the result of the request for location permissions.
     */
    // [START maps_current_place_on_request_permissions_result]
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
    }
    // [END maps_current_place_on_request_permissions_result]


}