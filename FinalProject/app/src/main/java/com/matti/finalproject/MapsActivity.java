package com.matti.finalproject;

// https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial#java
// https://stackoverflow.com/questions/43100365/how-to-refresh-a-google-map-manually

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private final Map<Marker, Map<String, Object>> markers = new HashMap<>();

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap map;

    // The entry point to the Places API.
    private PlacesClient placesClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    // Keys for storing activity state.
    // [START maps_current_place_state_keys]
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    // [END maps_current_place_state_keys]

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] likelyPlaceNames;
    private String[] likelyPlaceAddresses;
    private List[] likelyPlaceAttributions;
    private LatLng[] likelyPlaceLatLngs;

    private DatabaseHelper DBHelper;

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

    private Marker SelectedPlaceMarker;
    private AudioManager audio;


    // [START maps_current_place_on_create]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // [START_EXCLUDE silent]
        // [START maps_current_place_on_create_save_instance_state]
        // Retrieve location and camera position from saved instance state.

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            CameraPosition cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        // [END maps_current_place_on_create_save_instance_state]
        // [END_EXCLUDE]

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // [START_EXCLUDE silent]
        // Construct a PlacesClient
        Places.initialize(getApplicationContext(), "AIzaSyA1hmqXG2KsGqdqrlab2dYKNqqhCyjymqE");
        placesClient = Places.createClient(this);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        // [START maps_current_place_map_fragment]
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // [END maps_current_place_map_fragment]
        // [END_EXCLUDE]

        DBHelper = new DatabaseHelper(this);
        Silencer = new SilenceBroadcastReceiver();
        Unmute = new UnsilenceBroadcastReceiver();
        registerReceiver(Silencer, new IntentFilter(silencerFilter));
        registerReceiver(Unmute, new IntentFilter(unsilencerFilter));

        String sharedPrefFile = "com.matti.finalproject";
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        DataNumber = mPreferences.getInt(DATA_KEY, 0);
        HighestID = mPreferences.getInt(HighestID_KEY, 0);
        audio = (AudioManager) this.getSystemService(this.AUDIO_SERVICE);
        getLocationPermission();
        requestNotificationPolicyAccess();

    }
    // [END maps_current_place_on_create]

    /**
     * Saves the state of the map when the activity is paused.
     */
    // [START maps_current_place_on_save_instance_state]
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }
    // [END maps_current_place_on_save_instance_state]


    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putInt(DATA_KEY, DataNumber);
        preferencesEditor.putInt(HighestID_KEY, HighestID);
        preferencesEditor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataNumber = mPreferences.getInt(DATA_KEY, 0);
        HighestID = mPreferences.getInt(HighestID_KEY, 0);
        addDBMarkers();
        silenceMyPhone();
    }

    private void addDBMarkers() {
        if (map != null) { //prevent crashing if the map doesn't exist yet (eg. on starting activity)
            map.clear();
            if (DataNumber != 0) {
                for (int i = 1; i <= HighestID; i++) {
                    if (DBHelper.checkID(i)) {
                        String title = DBHelper.getTitle(i);
                        String snippet = DBHelper.getSnippet(i);
                        Double markerLat = DBHelper.getLat(i);
                        Double markerLong = DBHelper.getLong(i);
                        map.addMarker(new MarkerOptions()
                                .position(new LatLng(markerLat, markerLong))
                                .title(title)
                                .snippet(snippet));
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(Silencer);
        unregisterReceiver(Unmute);
        super.onDestroy();
    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    // [START maps_current_place_on_map_ready]
    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;

        UiSettings ui = map.getUiSettings();
        ui.setCompassEnabled(true);
        ui.setZoomControlsEnabled(true);

        FloatingActionButton fab = findViewById(R.id.fab_map);
        fab.setContentDescription(getString(R.string.fab_refresh));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetUpdate();
            }
        });

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {

                silenceMyPhone();

                if (SelectedPlaceMarker != null) {
                    SelectedPlaceMarker.remove();
                }
                SelectedPlaceMarker = map.addMarker(new MarkerOptions()
                        .position(point)
                        .title("Selected place")
                        .snippet(""));
            }
        });

        // [START_EXCLUDE]
        // [START map_current_place_set_info_window_adapter]
        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {


            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public View getInfoContents(Marker marker) {
                silenceMyPhone();
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.list_item,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.titleListItem);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippetListItem);
                String MySnippet = marker.getSnippet();
                snippet.setText(marker.getSnippet());

                TextView latitude = infoWindow.findViewById(R.id.latitudeListItem);
                Double MyLat = marker.getPosition().latitude;
                latitude.setText("Latitude: " + marker.getPosition().latitude);

                TextView longitude = infoWindow.findViewById(R.id.longitudeListItem);
                Double MyLong = marker.getPosition().longitude;
                longitude.setText("Longitude: " + marker.getPosition().longitude);

                TextView mode = infoWindow.findViewById(R.id.modeListItem);

                TextView addMarker = infoWindow.findViewById(R.id.addMsg);
                if (Objects.equals(marker.getTitle(), "Selected place")) {
                    mode.setText("");
                    addMarker.setText(getString(R.string.add_marker));
                }
                else{
                    mode.setText(DBHelper.getMode(DBHelper.getIDbyTitle(marker.getTitle())));
                    addMarker.setText(getString(R.string.edit_marker));
                }
                    map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(@NonNull Marker marker) {
                            silenceMyPhone();
                            if (marker.getTitle().equals("Selected place")) {
                                String MyTitle = "My Marker (" + (HighestID + 1) + ")"; //TODO: change DataNumber to another number that only increases
                                if (DBHelper.addData(
                                        MyTitle,
                                        MySnippet,
                                        MyLat,
                                        MyLong,
                                        "Silent")) {
                                    DataNumber += 1;
                                    HighestID += 1;
                                    map.addMarker(new MarkerOptions()
                                            .title(MyTitle)
                                            .snippet(MySnippet)
                                            .position(new LatLng(MyLat, MyLong)));
                                    if (SelectedPlaceMarker != null) {
                                        SelectedPlaceMarker.remove();
                                    }
                                    Toast.makeText(MapsActivity.this, getString(R.string.marker_add_success), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(MapsActivity.this, getString(R.string.marker_add_failure), Toast.LENGTH_LONG).show();
                                }
                            }
                            else{
                                Intent intent = new Intent(MapsActivity.this, EditMarkers.class);
                                intent.putExtra("title", marker.getTitle());
                                MapsActivity.this.startActivity(intent);
                            }
                        }
                    });
                return infoWindow;
            }
        });

        getDeviceLocation();
        onResume();
        // [END map_current_place_set_info_window_adapter]
    }

    //https://github.com/adrcotfas/Goodtime/commit/9fae5701b6e851d6fa5386bcbc71d63fe6e1d87d
    private void requestNotificationPolicyAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(android.provider.Settings.
                    ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isNotificationPolicyAccessGranted() {
        NotificationManager notificationManager = (NotificationManager)
                MapsActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager.isNotificationPolicyAccessGranted();
    }
    // [END maps_current_place_on_map_ready]

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    // [START maps_current_place_get_device_location]
    private void silenceMyPhone() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.getUiSettings().setMyLocationButtonEnabled(true);
                                currentLatitude = lastKnownLocation.getLatitude();
                                currentLongitude = lastKnownLocation.getLongitude();
                                SilencePhone();
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        float zoom = DEFAULT_ZOOM;
                        if (map.getCameraPosition().zoom > DEFAULT_ZOOM) {
                            zoom = map.getCameraPosition().zoom;
                        }
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), zoom));
                                map.getUiSettings().setMyLocationButtonEnabled(true);
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, zoom));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
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
        updateLocationUI();
    }
    // [END maps_current_place_on_request_permissions_result]

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    // [START maps_current_place_show_current_place]
    private void showCurrentPlace() {
        if (map == null) {
            return;
        }

        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final Task<FindCurrentPlaceResponse> placeResult =
                    placesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FindCurrentPlaceResponse likelyPlaces = task.getResult();

                        // Set the count, handling cases where less than 5 entries are returned.
                        int count;
                        if (likelyPlaces.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
                            count = likelyPlaces.getPlaceLikelihoods().size();
                        } else {
                            count = M_MAX_ENTRIES;
                        }

                        int i = 0;
                        likelyPlaceNames = new String[count];
                        likelyPlaceAddresses = new String[count];
                        likelyPlaceAttributions = new List[count];
                        likelyPlaceLatLngs = new LatLng[count];

                        for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
                            // Build a list of likely places to show the user.
                            likelyPlaceNames[i] = placeLikelihood.getPlace().getName();
                            likelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress();
                            likelyPlaceAttributions[i] = placeLikelihood.getPlace()
                                    .getAttributions();
                            likelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                            i++;
                            if (i > (count - 1)) {
                                break;
                            }
                        }

                        // Show a dialog offering the user the list of likely places, and add a
                        // marker at the selected place.
                        MapsActivity.this.openPlacesDialog();
                    } else {
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            map.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(defaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }
    // [END maps_current_place_show_current_place]

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    // [START maps_current_place_open_places_dialog]
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = likelyPlaceLatLngs[which];
                String markerSnippet = likelyPlaceAddresses[which];
                if (likelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + likelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                map.addMarker(new MarkerOptions()
                        .title(likelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                float zoom = DEFAULT_ZOOM;
                if (map.getCameraPosition().zoom > DEFAULT_ZOOM) {
                    zoom = map.getCameraPosition().zoom;
                }

                // Position the map's camera at the location of the marker.
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        zoom));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(likelyPlaceNames, listener)
                .show();
    }
    // [END maps_current_place_open_places_dialog]

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    // [START maps_current_place_update_location_ui]
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    // [END maps_current_place_update_location_ui]

    private void SilencePhone() {
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

    private void resetUpdate(){
        onPause();
        onMapReady(map);
    }

}

