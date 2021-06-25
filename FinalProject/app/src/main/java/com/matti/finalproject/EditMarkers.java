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
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location lastKnownLocation;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private static final String silencerFilter = "com.matti.finalproject.SilenceBroadcastReceiver";
    private SilenceBroadcastReceiver Silencer;

    private static final String unsilencerFilter = "com.matti.finalproject.UnsilenceBroadcastReceiver";
    private UnsilenceBroadcastReceiver Unmute;

    private int HighestID = 0;
    private final String HighestID_KEY = "Highest ID";

    private int DataNumber = 0;
    private final String DATA_KEY = "Data Number";

    private SharedPreferences mPreferences;
    private AudioManager audio;

    private String defaultTitle;
    private String defaultSnippet;
    private Integer id;
    private Double Latitude;
    private Double Longitude;
    private String defaultMode;
    private String newMode;

    private EditText mTitleInput;
    private EditText mSnippetInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_markers);
        DBHelper = new DatabaseHelper(this);
        Silencer = new SilenceBroadcastReceiver();
        Unmute = new UnsilenceBroadcastReceiver();
        registerReceiver(Silencer, new IntentFilter(silencerFilter));
        registerReceiver(Unmute, new IntentFilter(unsilencerFilter));

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        String sharedPrefFile = "com.matti.finalproject";
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        DataNumber = mPreferences.getInt(DATA_KEY, 0);
        HighestID = mPreferences.getInt(HighestID_KEY, 0);
        audio = (AudioManager) this.getSystemService(AUDIO_SERVICE);
        requestNotificationPolicyAccess();

        mTitleInput = findViewById(R.id.editTextTitle);
        mSnippetInput = findViewById(R.id.editTextSnippet);
        TextView mLatitudeView = findViewById(R.id.textViewLatitude);
        TextView mLongitudeView = findViewById(R.id.textViewLongitude);
        RadioButton silentButton = findViewById(R.id.radioButtonSilent);
        RadioButton vibrateButton = findViewById(R.id.radioButtonVibrate);
        RadioButton normalButton = findViewById(R.id.radioButtonNormal);

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

        switch (defaultMode) {
            case "Silent":
                silentButton.toggle();
                break;
            case "Vibrate":
                vibrateButton.toggle();
                break;
            case "Normal":
                normalButton.toggle();
                break;
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
        String newTitle = mTitleInput.getText().toString();
        String newSnippet = mSnippetInput.getText().toString();

        if (defaultSnippet == null){
            defaultSnippet = "";
        }

        if (newMode == null){
            newMode = defaultMode;
        }

        if ((defaultTitle.equals(newTitle))
                && (defaultSnippet.equals(newSnippet))
                && (defaultMode.equals(newMode))){
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
            Toast.makeText(this, getString(R.string.edit_markers_save_error), Toast.LENGTH_LONG).show();
        }
        else if (newTitle.equals("")){
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
            Toast.makeText(this, getString(R.string.invalid_title_input), Toast.LENGTH_LONG).show();
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
                if(mode != null) {
                    switch (mode) {
                        case "Silent":
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
                EditMarkers.this.getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager.isNotificationPolicyAccessGranted();
    }



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
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
    }

    // [END maps_current_place_on_request_permissions_result]
}