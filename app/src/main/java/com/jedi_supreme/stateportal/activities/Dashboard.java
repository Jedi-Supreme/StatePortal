package com.jedi_supreme.stateportal.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jedi_supreme.stateportal.R;
import com.jedi_supreme.stateportal.common;
import com.jedi_supreme.stateportal.databases.user_db;
import com.jedi_supreme.stateportal.models.c_Complaints;
import com.jedi_supreme.stateportal.models.c_User;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.jedi_supreme.stateportal.common.POWER_COMPLAINT;
import static com.jedi_supreme.stateportal.common.POWER_PICKER_REQUEST;

import static com.jedi_supreme.stateportal.common.ROAD_COMPLAINT;
import static com.jedi_supreme.stateportal.common.ROAD_PICKER_REQUEST;

import static com.jedi_supreme.stateportal.common.WATER_COMPLAINT;
import static com.jedi_supreme.stateportal.common.WATER_PICKER_REQUEST;

public class Dashboard extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnLongClickListener {

    static final String LOCATION_PERMISSION_REQ = Manifest.permission.ACCESS_FINE_LOCATION;
    static final int LOCATION_REQ_CODE = 123;
    private static final int REQUEST_CHECK_SETTINGS = 1000;

    ProgressBar pro_bar_dash;
    ConstraintLayout const_dashboard;
    Button bt_police, bt_fire,bt_health;
    WeakReference<Dashboard> weak_dashboard;

    private GoogleApiClient googleApiClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location user_curr_location;

    private static final long
            UPDATE_INTERVAL = 15*1000,
            FASTEST_INTERVAL = 7*1000;

    private FusedLocationProviderClient fusedLocationProviderClient;

    //============================================ON CREATE=========================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        weak_dashboard = new WeakReference<>(this);
        const_dashboard = findViewById(R.id.dashboard_layout);


        //build google api client
        googleApiClient = new GoogleApiClient.Builder(weak_dashboard.get()).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        //location request builder
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        //location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        // Update UI with location data
                        user_curr_location = location;
                        //sos_user = create_sos_user(location);
                    }
                }
            }
        };

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(weak_dashboard.get());

        bt_police = findViewById(R.id.bt_police);
        bt_fire = findViewById(R.id.bt_fire);
        bt_health = findViewById(R.id.bt_health);

        pro_bar_dash = findViewById(R.id.probar_dashboard);

        //check user login and registration progress
        if (firebaseAuth.getCurrentUser() == null){
            mobile_verification();
        }else {
            SharedPreferences state_portal_pref = getSharedPreferences(common.PREFERENCE_KEY,MODE_PRIVATE);
            int reg_State = state_portal_pref.getInt(common.REGISTRATION_STEP_KEY,common.STEP_REQUEST_OTP);

            switch (reg_State){

                case common.STEP_REQUEST_OTP:
                    mobile_verification();
                    break;

                case common.STEP_FILL_DATA:
                    second_Step(firebaseAuth.getCurrentUser().getPhoneNumber());
                    break;

                case common.STEP_REG_COMPLETED:
                    //init -> check location permission -> request location
                    init();
                    break;
            }

        }
        //check user login and registration progress

        //set long click listeners
        bt_police.setOnLongClickListener(this);
        bt_fire.setOnLongClickListener(this);
        bt_health.setOnLongClickListener(this);

    }
    //============================================ON CREATE=========================================


    //--------------------------------------------OVERRIDE METHODS----------------------------------
    //============================================ MENU ============================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.menu_comp_list:
                lists_intent();
                break;
            case R.id.menu_profile:
                profiles_intent();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    //============================================ MENU ============================================

    //====================================ACTIVITY RESULTS==========================================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        pro_bar_dash.setVisibility(View.GONE);

        if (resultCode == RESULT_OK) {

             Place place = PlacePicker.getPlace(getApplicationContext(),data);
             Location place_location = new Location("Place_Picker");

             place_location.setLatitude(place.getLatLng().latitude);
             place_location.setLongitude(place.getLatLng().longitude);

             //distance in metres and as bird flies
            if (user_curr_location.distanceTo(place_location) <= 1500){
                switch (requestCode){

                    case WATER_PICKER_REQUEST:
                        complaint_dialog(WATER_COMPLAINT,place);
                        break;

                    case POWER_PICKER_REQUEST:
                        complaint_dialog(POWER_COMPLAINT,place);
                        break;

                    case ROAD_PICKER_REQUEST:
                        complaint_dialog(ROAD_COMPLAINT,place);
                        break;

                    case REQUEST_CHECK_SETTINGS:
                        // All required changes were successfully made
                        start_locationupdates();
                        break;
                }
            }else {
                String msg = "Complaints must be within a 1.5km radius of your current location.";
                common.snackbar(const_dashboard,msg).show();
            }



        }


    }

    @Override
    public void onRequestPermissionsResult
            (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, yay! Do the task you need to do.
            check_locations_settings();

        } else {
            // permission denied, boo! Disable the functionality that depends on this permission.
            String msg = "Emergency Services Unavailable";
            common.snackbar(const_dashboard,msg).show();

            bt_health.setLongClickable(false);
            bt_fire.setLongClickable(false);
            bt_police.setLongClickable(false);

        }
    }
    //====================================ACTIVITY RESULTS==========================================

    @Override
    public boolean onLongClick(final View v) {

        //permission granted
        if (ActivityCompat.checkSelfPermission(weak_dashboard.get(), LOCATION_PERMISSION_REQ) ==
                PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(weak_dashboard.get(),
                    new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {

                        switch (v.getId()){

                            case R.id.bt_police:
                                send_emergency_alert(common.POLICE_EMERGENCY,create_sos_user(location));
                                common.snackbar(const_dashboard,common.POLICE_EMERGENCY +" report sent").show();
                                break;
                            case R.id.bt_fire:
                                send_emergency_alert(common.FIRE_EMERGENCY,create_sos_user(location));
                                common.snackbar(const_dashboard,common.FIRE_EMERGENCY +" report sent").show();
                                break;
                            case R.id.bt_health:
                                send_emergency_alert(common.HEALTH_EMERGENCY,create_sos_user(location));
                                common.snackbar(const_dashboard,common.HEALTH_EMERGENCY +" report sent").show();
                                break;
                        }
                    } else {
                        //request location
                        start_locationupdates();
                    }

                }
            });

        } else {
            //request permission
            request_permission();
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop location updates
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.getFusedLocationProviderClient(weak_dashboard.get()).removeLocationUpdates(locationCallback);
            googleApiClient.disconnect();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        // stop location updates
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.getFusedLocationProviderClient(weak_dashboard.get()).removeLocationUpdates(locationCallback);
            googleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        check_permissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // stop location updates
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.getFusedLocationProviderClient(weak_dashboard.get()).removeLocationUpdates(locationCallback);
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    //--------------------------------------------OVERRIDE METHODS----------------------------------


    //======================================METHODS=================================================
    //init -> check location permission -> request permission -> check location settings -> request location updates

    void init(){
        check_permissions();
    }

    void check_permissions(){
        if (ContextCompat.checkSelfPermission(weak_dashboard.get(), LOCATION_PERMISSION_REQ)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, Request permission
            request_permission();
        }else {
            //Request location updates
           check_locations_settings();
        }
    }

    void request_permission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(weak_dashboard.get(),LOCATION_PERMISSION_REQ)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(weak_dashboard.get(),
                   LOCATION_PERMISSION_REQ)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                String explanation = "Location permissions are needed to share emergency alerts with the appropriate first responder";
                common.snackbar(const_dashboard,explanation).show();

                ActivityCompat.requestPermissions(weak_dashboard.get(), new String[]{LOCATION_PERMISSION_REQ},LOCATION_REQ_CODE);

            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(weak_dashboard.get(), new String[]{LOCATION_PERMISSION_REQ},LOCATION_REQ_CODE);

                // LOCATION_REQ_CODE is an app-defined int constant.
                // The callback method gets the result of the request.
            }
        } else {
            // Permission has already been granted
            //Request location update
            check_locations_settings();
        }
    }

    void check_locations_settings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        //**************************
        builder.setAlwaysShow(false); //this is the key ingredient
        //**************************

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(weak_dashboard.get()).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    start_locationupdates();

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult( weak_dashboard.get(), REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException | ClassCastException e) {
                                // Ignore the error.
                            }
                            break;

                    }
                }
            }
        });

    }

    void start_locationupdates() {
        if (ActivityCompat.checkSelfPermission(weak_dashboard.get(), LOCATION_PERMISSION_REQ)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(weak_dashboard.get()).requestLocationUpdates(
                    locationRequest, locationCallback, null);
        }else {
            request_permission();
        }
    }

    void complaint_dialog(final String complaint_type, final Place issue_place){

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date_format = new SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault());
        final String date = date_format.format(calendar.getTime());

        String[] road_arr = getResources().getStringArray(R.array.road_issues);
        String[] power_arr = getResources().getStringArray(R.array.power_issues);
        String[] water_arr = getResources().getStringArray(R.array.water_issues);

        ArrayAdapter<String> spinner_adapter;

        AlertDialog.Builder complaint_bulider = new AlertDialog.Builder(this);
        complaint_bulider.setCancelable(true);
        LayoutInflater inflater = LayoutInflater.from(this);

        View complain_view = inflater.inflate(R.layout.complaints_dialog_layout,const_dashboard,false);
        complaint_bulider.setView(complain_view);
        final Spinner sp_issues = complain_view.findViewById(R.id.sp_issues);

        final TextInputEditText et_issue_latitude = complain_view.findViewById(R.id.et_issue_latitude);
        final TextInputEditText et_issue_longitude = complain_view.findViewById(R.id.et_issue_longitude);
        final TextInputEditText et_issue_body = complain_view.findViewById(R.id.et_issue_body);

        final String name_address = issue_place.getName() + " " + issue_place.getAddress();
        complaint_bulider.setTitle(name_address);

        et_issue_latitude.setText(String.valueOf(issue_place.getLatLng().latitude));
        et_issue_longitude.setText(String.valueOf(issue_place.getLatLng().longitude));

        complaint_bulider.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        switch (complaint_type){

            case WATER_COMPLAINT:
                spinner_adapter = new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_spinner_dropdown_item,water_arr);
                sp_issues.setAdapter(spinner_adapter);
                break;

            case POWER_COMPLAINT:
                spinner_adapter = new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_spinner_dropdown_item,power_arr);
                sp_issues.setAdapter(spinner_adapter);
                break;

            case ROAD_COMPLAINT:
                spinner_adapter = new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_spinner_dropdown_item,road_arr);
                sp_issues.setAdapter(spinner_adapter);
                break;
        }


        complaint_bulider.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                user_db userDb = new user_db(getApplicationContext(),null);

                if (sp_issues.getSelectedItemPosition() > 0){
                    String issue_body = sp_issues.getAdapter().getItem(sp_issues.getSelectedItemPosition()).toString();
                    if (!et_issue_body.getText().toString().isEmpty()){
                        issue_body +=":- "+et_issue_body.getText().toString();
                    }
                    c_User complain_user = userDb.complain_user();
                    c_Complaints complain = new c_Complaints(complain_user,name_address,
                            issue_body,date,issue_place.getLatLng().latitude,issue_place.getLatLng().longitude);

                    userDb.add_complaint(complain);
                    send_complaint(complaint_type,complain);
                }
            }
        });
        complaint_bulider.show();
    }

    void send_complaint(String type, c_Complaints complain){

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd",Locale.getDefault());

        String date = dateFormat.format(calendar.getTime());

        DatabaseReference comp_ref = FirebaseDatabase.getInstance().getReference(common.COMPLAINTS_REFERENCE);
        comp_ref.child(type).child(date).push().setValue(complain);

    }

    void send_emergency_alert(String type, c_User sos_user){

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd",Locale.getDefault());

        String date = dateFormat.format(calendar.getTime());

        DatabaseReference emergency_ref = FirebaseDatabase.getInstance().getReference(common.EMERGENCY_REFERENCE);
        emergency_ref.child(type).child(date).push().setValue(sos_user);

    }

    c_User create_sos_user(Location distress_loc){

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date_timeformat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        user_db userDb = new user_db(getApplicationContext(),null);
        Cursor user_cursor = userDb.user_info();
        user_cursor.moveToFirst();

        return new c_User(
                user_cursor.getString(user_cursor.getColumnIndexOrThrow(c_User.COLUMN_FIRE_ID)),
                user_cursor.getString(user_cursor.getColumnIndexOrThrow(c_User.COLUMN_FN)),
                user_cursor.getString(user_cursor.getColumnIndexOrThrow(c_User.COLUMN_LN)),
                date_timeformat.format(calendar.getTime()),
                distress_loc.getLatitude(),
                distress_loc.getLongitude()
        );

    }

    public void complaint_builder_request(int COMPLAINT_CODE)
            throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        startActivityForResult(builder.build(weak_dashboard.get()), COMPLAINT_CODE);
    }
    //======================================METHODS=================================================

    //------------------------------------------BUTTON CLICK LISTENERS------------------------------
    //maintenance buttons
    public void complaint_report(View view) {

        pro_bar_dash.setVisibility(View.VISIBLE);

        switch (view.getId()){

            case R.id.bt_water:
                try {
                    complaint_builder_request(WATER_PICKER_REQUEST);
                } catch (GooglePlayServicesNotAvailableException
                        | GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.bt_power:
                try {
                    complaint_builder_request(POWER_PICKER_REQUEST);
                } catch (GooglePlayServicesNotAvailableException
                        | GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.bt_road:
                try {
                    complaint_builder_request(ROAD_PICKER_REQUEST);
                } catch (GooglePlayServicesNotAvailableException
                        | GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    //emergency buttons
    public void emergency_guide(View view) {
        String msg = "Long Click to Send Emergency Alert";
        common.snackbar(const_dashboard,msg).show();
        check_locations_settings();
    }
    //------------------------------------------BUTTON CLICK LISTENERS------------------------------

    //=================================================INTENTS======================================
    void mobile_verification(){
        Intent mobile_verificaation_intent = new Intent(getApplicationContext(),Mobile_Verification.class);
        startActivity(mobile_verificaation_intent);
        super.finish();
    }

    void second_Step(String mobilenumber){
        SharedPreferences state_pref = getSharedPreferences(common.PREFERENCE_KEY,MODE_PRIVATE);
        SharedPreferences.Editor pref_editor = state_pref.edit();
        pref_editor.putInt(common.REGISTRATION_STEP_KEY,common.STEP_FILL_DATA).apply();
        Intent userDetails_intent = new Intent(weak_dashboard.get(),UserDetails.class);
        userDetails_intent.putExtra(common.MOBILE_NUMBER_KEY,mobilenumber);
        startActivity(userDetails_intent);
        super.finish();
    }

    void lists_intent(){
        Intent comp_list = new Intent(getApplicationContext(),complaints_activity.class);
        startActivity(comp_list);
    }

    void profiles_intent(){
        Intent profile_view = new Intent(getApplicationContext(),UserProfile.class);
        startActivity(profile_view);
    }
    //=================================================INTENTS======================================

}
