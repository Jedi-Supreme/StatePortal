package com.jedi_supreme.stateportal;

import android.support.design.widget.Snackbar;
import android.view.View;

public class common {

    //Complaint Keys
    public static final String WATER_COMPLAINT = "water";
    public static final String POWER_COMPLAINT = "power";
    public static final String ROAD_COMPLAINT = "road";

    //Intent Request Codes
    public static final int WATER_PICKER_REQUEST = 155;
    public static final int POWER_PICKER_REQUEST = 157;
    public static final int ROAD_PICKER_REQUEST = 159;

    public static final String POLICE_EMERGENCY = "crime";
    public static final String FIRE_EMERGENCY = "fire";
    public static final String HEALTH_EMERGENCY = "health";

    public static final String COMPLAINTS_REFERENCE = "Complaints";
    public static final String EMERGENCY_REFERENCE = "Emergencies";

    public static final String GHANA_CODE = "+233";
    public static final String PREFERENCE_KEY = "state_portal_pref";

    public static final String MOBILE_NUMBER_KEY = "mobile_number";
    public static final String REGISTRATION_STEP_KEY = "reg_step";

    public static final String USERS_PROFILE_REF = "User_Profiles";

    public static final int STEP_REQUEST_OTP = 10;
    public static final int STEP_FILL_DATA = 12;
    public static final int STEP_REG_COMPLETED = 14;

    public static Snackbar snackbar(View parent, String message){

        final Snackbar snackbar = Snackbar.make(parent,message,Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(parent.getContext().getResources().getColor(R.color.colorPrimary));
        snackbar.setAction("Close", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });

        return snackbar;
    }
}
