package com.matti.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class EditMarkers extends AppCompatActivity {

    private DatabaseHelper DBHelper;
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private double currentLatitude;
    private double currentLongitude;

    private static final String silencerFilter = "com.matti.finalproject.SilenceBroadcastReceiver";
    private SilenceBroadcastReceiver Silencer;

    private static final String unsilencerFilter = "com.matti.finalproject.UnsilenceBroadcastReceiver";
    private UnsilenceBroadcastReceiver Unmute;

    private int HighestID = 0;
    private final String HighestID_KEY = "Highest ID";

    private int DataNumber = 0;
    private final String DATA_KEY = "Data Number";

    private SharedPreferences mPreferences;
    private String sharedPrefFile = "com.matti.finalproject";
    private AudioManager audio;

    private String defaultTitle;
    private String defaultSnippet;
    private String newTitle;
    private String newSnippet;
    private Integer id;
    private Double Latitude;
    private Double Longitude;
    private String defaultMode;
    private String newMode;

    private EditText mTitleInput;
    private EditText mSnippetInput;
    private TextView mLatitudeView;
    private TextView mLongitudeView;
    private RadioButton SilentButton;
    private RadioButton VibrateButton;
    private RadioButton NormalButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_markers);
        DBHelper = new DatabaseHelper(this);
        Silencer = new SilenceBroadcastReceiver();
        Unmute = new UnsilenceBroadcastReceiver();
        registerReceiver(Silencer, new IntentFilter(silencerFilter));
        registerReceiver(Unmute, new IntentFilter(unsilencerFilter));

        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        DataNumber = mPreferences.getInt(DATA_KEY, 0);
        HighestID = mPreferences.getInt(HighestID_KEY, 0);
        audio = (AudioManager) this.getSystemService(this.AUDIO_SERVICE);
        requestNotificationPolicyAccess();

        mTitleInput = findViewById(R.id.editTextTitle);
        mSnippetInput = findViewById(R.id.editTextSnippet);
        mLatitudeView = findViewById(R.id.textViewLatitude);
        mLongitudeView = findViewById(R.id.textViewLongitude);
        SilentButton = findViewById(R.id.radioButtonSilent);
        VibrateButton = findViewById(R.id.radioButtonVibrate);
        NormalButton = findViewById(R.id.radioButtonNormal);

        defaultTitle = getIntent().getStringExtra("title");
        id = DBHelper.getIDbyTitle(defaultTitle);
        defaultSnippet = DBHelper.getSnippet(id);
        mTitleInput.setText(defaultTitle);
        mSnippetInput.setText(defaultSnippet);
        Latitude = DBHelper.getLat(id);
        mLatitudeView.setText("Latitude : " + Latitude);
        Longitude = DBHelper.getLong(id);
        mLongitudeView.setText("Latitude : " + Longitude);
        defaultMode = DBHelper.getMode(id);

        if(defaultMode.equals("Silent")){
            SilentButton.toggle();
        }
        else if (defaultMode.equals("Vibrate")){
            VibrateButton.toggle();
        }
        else if (defaultMode.equals("Normal")){
            NormalButton.toggle();
        }
    }

    public void RadioButton(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.radioButtonSilent:
                if (checked)
                    newMode = "Silent";
                    break;
            case R.id.radioButtonVibrate:
                if (checked)
                    newMode = "Vibrate";
                    break;
            case R.id.radioButtonNormal:
                if (checked)
                    newMode = "Normal";
                break;
        }

    }

    public void Save(View view) {
        newTitle = mTitleInput.getText().toString();
        newSnippet = mSnippetInput.getText().toString();
        Log.d(TAG, "newSnippet = " + newSnippet);
        Log.d(TAG, "defaultSnippet = " + defaultSnippet);
        if (newSnippet.equals(null)){
            newSnippet = "";
        }
        if (defaultSnippet.equals(null)){
            defaultSnippet = "";
        }

        if ((defaultTitle.equals(newTitle))
                && (defaultSnippet.equals(newSnippet))
                && (defaultMode.equals(newMode))){
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
            Toast.makeText(this, getString(R.string.edit_markers_save_error), Toast.LENGTH_LONG).show();
        }
        else {
            if(DBHelper.updateData(id, newTitle, newSnippet, Latitude, Longitude, newMode)){
                this.finish();
            }
            else{Toast.makeText(this, getString(R.string.marker_update_failure), Toast.LENGTH_SHORT).show();}
        }
    }

    public void Reset(View view) {
        mTitleInput.setText(defaultTitle);
        mSnippetInput.setText(defaultSnippet);
    }

    public void Delete(View view) {
        if(DBHelper.deleteData(id)){
            DataNumber -= 1;
            this.finish();
        }
        else {Toast.makeText(this, getString(R.string.marker_rm_failure), Toast.LENGTH_SHORT).show();}
    }

    public void Cancel(View view) {
        this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putInt(DATA_KEY, DataNumber);
        preferencesEditor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataNumber = mPreferences.getInt(DATA_KEY, 0);
        HighestID = mPreferences.getInt(HighestID_KEY, 0);
        getLocationPermission();
        silenceMyPhone();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(Silencer);
        unregisterReceiver(Unmute);
        super.onDestroy();
    }

    private void SilencePhone() {
        boolean ToSilence = false;
        Double closestPlace = 1.0;
        String mode="";
        if (DataNumber != 0) {
            for (int i = 1; i <= HighestID; i++) {
                if (DBHelper.checkID(i)) {
                    Double markerLat = DBHelper.getLat(i);
                    Double markerLong = DBHelper.getLong(i);
                    Double diffLatSqr = currentLatitude - markerLat;
                    diffLatSqr *= diffLatSqr;
                    Double diffLongSqr = currentLongitude - markerLong;
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
                if(!mode.equals(null)) {
                    if (mode.equals("Silent")) {
                        audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    } else if (mode.equals("Vibrate")) {
                        audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    } else if (mode.equals("Normal")) {
                        audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    }
                }

            }
            else {
                audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        }
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            if (ToSilence){
                if(!mode.equals(null)) {
                    if (mode.equals("Silent")) {
                        audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    } else if (mode.equals("Vibrate")) {
                        audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    } else if (mode.equals("Normal")) {
                        audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
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
                EditMarkers.this.getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager.isNotificationPolicyAccessGranted();
    }

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private void silenceMyPhone() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (locationPermissionGranted) {
                final Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                currentLatitude = lastKnownLocation.getLatitude();
                                currentLongitude = lastKnownLocation.getLongitude();
                                SilencePhone();
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            currentLatitude = -33.8523341;
                            currentLongitude = 151.2106085;
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
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