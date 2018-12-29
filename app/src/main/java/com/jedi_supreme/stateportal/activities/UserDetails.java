package com.jedi_supreme.stateportal.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jedi_supreme.stateportal.R;
import com.jedi_supreme.stateportal.adapters.ice_recycler_Adapter;
import com.jedi_supreme.stateportal.common;
import com.jedi_supreme.stateportal.models.c_E_contact;
import com.jedi_supreme.stateportal.models.c_User;
import com.jedi_supreme.stateportal.databases.user_db;

import java.util.ArrayList;

public class UserDetails extends AppCompatActivity {

    TextInputEditText et_firstname, et_lastname,et_home_add;
    RecyclerView recycler_ice_contacts;
    Button bt_add;
    ProgressBar pro_bar_details;
    ConstraintLayout const_userDetails;

    user_db userDb;
    c_User old_user_acc;
    int PLACE_PICKER_REQUEST = 167;
    Place place;

    //check if number exists in records
    //if new add records -> test inputs -> add records online

    //===========================================ON CREATE==========================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        userDb = new user_db(getApplicationContext(),null);

        et_firstname = findViewById(R.id.et_firstname);
        et_lastname = findViewById(R.id.et_lastname);
        et_home_add = findViewById(R.id.et_home_add);
        const_userDetails = findViewById(R.id.user_details_layout);

        recycler_ice_contacts = findViewById(R.id.recycler_ice_contacts);
        bt_add = findViewById(R.id.bt_add);
        pro_bar_details = findViewById(R.id.pro_bar_details);

        //check if user number exists in records
        DatabaseReference records_ref = FirebaseDatabase.getInstance().getReference(common.USER_RECORDS_REF);

        if (FirebaseAuth.getInstance().getCurrentUser() != null){

            String mobile_number = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

            if (mobile_number != null) {

                String target_number = mobile_number.replace("+","");
                records_ref.child(target_number).child("uid").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String old_uid = dataSnapshot.getValue(String.class);

                        if (old_uid != null){
                            fetch_details_online(old_uid);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

        }

        try{
            refresh_contacts_list();
        }catch (Exception ignored){}

    }
    //===========================================ON CREATE==========================================

    //------------------------------------------OVERRIDE METHODS------------------------------------
    @Override
    protected void onPause(){
        super.onPause();
        pro_bar_details.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {

            place = PlacePicker.getPlace(getApplicationContext(),data);

            String home_add = place.getName() + ", " + place.getAddress();
            et_home_add.setText(home_add);
        }

    }
    //------------------------------------------OVERRIDE METHODS------------------------------------

    //------------------------------------------BUTTON CLICK LISTENERS------------------------------
    public void Home_location_picker(View view)
            throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        pro_bar_details.setVisibility(View.VISIBLE);

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        startActivityForResult(builder.build(UserDetails.this), PLACE_PICKER_REQUEST);
    }

    public void add_ice_contact(View view) {
        add_contacts();
    }

    public void submit_details(View view) {
        //test inputs -> save to local DB -> save to online DB -> Dashboard
        test_inputs();
    }
    //------------------------------------------BUTTON CLICK LISTENERS------------------------------

    //==============================================METHODS=========================================

    //fetch details -> bind data -> refresh list
    public void fetch_details_online(String old_uid){

        final DatabaseReference users_profile_ref = FirebaseDatabase.getInstance().getReference(common.USERS_PROFILE_REF);
        users_profile_ref.child(old_uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                old_user_acc = dataSnapshot.getValue(c_User.class);

                bind_user_data(old_user_acc);
                users_profile_ref.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void bind_user_data(c_User old_profile){

        if (old_profile != null){

            //set user values to views
            et_firstname.setText(old_profile.getFn());
            et_lastname.setText(old_profile.getLn());

            et_home_add.setText(old_profile.getHomeAddress());

            build_E_contact_from_list(old_profile.getIce_contacts_list());

        }

    }

    void add_number_toRecords(String new_uid , String number){
        DatabaseReference records_ref = FirebaseDatabase.getInstance().getReference(common.USER_RECORDS_REF);

        records_ref.child(number).child("uid").setValue(new_uid);
    }

    void delete_old_data(String old_uid){

        DatabaseReference old_user_ref = FirebaseDatabase.getInstance().getReference(common.USERS_PROFILE_REF).child(old_uid);
        old_user_ref.removeValue();
    }

    void build_E_contact_from_list(ArrayList<String> old_contacts){

        for (String rel_numb : old_contacts){

            String[]rel_arr = rel_numb.split("-"); //0 = relation, 1 = number

            c_E_contact old_cont = new c_E_contact(rel_arr[0],rel_arr[1]);

            userDb.add_emergency_contact(old_cont);
            refresh_contacts_list();
        }
    }

    //test input -> save details online
    void test_inputs(){

            if (et_firstname.getText().toString().equals("") || et_firstname.getText().toString().isEmpty()){
                et_firstname.setError("Enter Firstname");
                et_firstname.requestFocus();

            }else if (et_lastname.getText().toString().equals("") || et_lastname.getText().toString().isEmpty()){
                et_lastname.setError("Enter Lastname");
                et_lastname.requestFocus();

            }else if (et_home_add.getText().toString().isEmpty() || et_home_add.getText().toString().isEmpty()){
                et_home_add.setError(getResources().getString(R.string.set_home_address));
                et_home_add.requestFocus();

            }else if (recycler_ice_contacts.getAdapter() == null){
                common.snackbar(findViewById(R.id.user_details_layout),"Emergency Contact Required").show();
                recycler_ice_contacts.requestFocus();
            }else {
                pro_bar_details.setVisibility(View.VISIBLE);

                String fn = et_firstname.getText().toString();
                String ln = et_lastname.getText().toString();
                String home_address = et_home_add.getText().toString();

                //after testing inputs, save to local and online database
                if (FirebaseAuth.getInstance().getCurrentUser() != null){

                    Toast.makeText(getApplicationContext(), "Current user not null", Toast.LENGTH_SHORT).show();

                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    String mobile_number = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

                    //local constructor
                    c_User localDB_user;
                    if (mobile_number != null) {

                        Toast.makeText(getApplicationContext(), "Number not null", Toast.LENGTH_SHORT).show();

                        if (place != null){
                            Toast.makeText(getApplicationContext(), "place not null", Toast.LENGTH_SHORT).show();
                            localDB_user = new c_User(
                                    uid,fn,ln,
                                    mobile_number.replace("+",""),
                                    home_address,
                                    place.getLatLng().latitude,
                                    place.getLatLng().longitude);

                        }else {
                            localDB_user = new c_User(
                                    uid,fn,ln,
                                    mobile_number.replace("+",""),
                                    home_address,
                                    old_user_acc.getHome_latitude(),
                                    old_user_acc.getHome_longitude());
                        }

                        if (userDb.user_info().getCount() < 1){
                            Toast.makeText(getApplicationContext(), "saving locally", Toast.LENGTH_SHORT).show();
                            userDb.add_user(localDB_user); //save to local db

                            // if old account is null save new data
                            if (old_user_acc == null ){
                                Toast.makeText(getApplicationContext(), "Clean save", Toast.LENGTH_SHORT).show();
                                save_user_details_online(localDB_user);
                            }else {
                                //else delete old profile from db and save under new id
                                Toast.makeText(getApplicationContext(), "delete save", Toast.LENGTH_SHORT).show();
                                delete_old_data(old_user_acc.getFire_id());
                                save_user_details_online(localDB_user);
                            }
                        }else {
                            Toast.makeText(getApplicationContext(), "saving online", Toast.LENGTH_SHORT).show();
                            // if old account id = new id just save to overwrite old data
                            if (old_user_acc == null ){
                                Toast.makeText(getApplicationContext(), "Clean save", Toast.LENGTH_SHORT).show();
                                save_user_details_online(localDB_user);
                            }else {
                                //else delete old profile from db and save under new id
                                Toast.makeText(getApplicationContext(), "delete save", Toast.LENGTH_SHORT).show();
                                delete_old_data(old_user_acc.getFire_id());
                                save_user_details_online(localDB_user);
                            }
                        }
                    }

                }

            }


    }

    void save_user_details_online(c_User local_user_info){

        Cursor contact_cursor = userDb.all_contacts();

        ArrayList<String> contacts_list = new ArrayList<>();

        String contact;

        while(contact_cursor.moveToNext()){
            contact = contact_cursor.getString(contact_cursor.getColumnIndexOrThrow(c_E_contact.COLUMN_REL))
                    +"-"+contact_cursor.getString(contact_cursor.getColumnIndexOrThrow(c_E_contact.COLUMN_NUMBER));
            contacts_list.add(contact);
        }

        c_User fireDB_user = new c_User(
                local_user_info.getFire_id(),
                local_user_info.getFn(),
                local_user_info.getLn(),
                local_user_info.getMobile(),
                local_user_info.getHomeAddress(),
                local_user_info.getHome_latitude(),
                local_user_info.getHome_longitude(),
                contacts_list
                );

        //save firebase user to path users_profile/user_id/details
        DatabaseReference users_profile_ref = FirebaseDatabase.getInstance().getReference(common.USERS_PROFILE_REF);
        users_profile_ref.child(fireDB_user.getFire_id()).setValue(fireDB_user);// save to users profile
        add_number_toRecords(fireDB_user.getFire_id(),fireDB_user.getMobile()); // save to records
        toDashboard();
    }

    void add_contacts(){
        try {

            if (recycler_ice_contacts.getAdapter().getItemCount() >= 3){
               common.snackbar(const_userDetails,"Maximum of 3 Contacts allowed").show();
            }else {
                emergency_contacts_builder();
            }
        }catch (Exception ignored){
            emergency_contacts_builder();
        }
    }

    void emergency_contacts_builder(){

        AlertDialog.Builder alertbuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        final View dialog_view = inflater.inflate(R.layout.dialog_input,const_userDetails,false);
        alertbuilder.setCancelable(false);
        alertbuilder.setTitle("Emergency contact");
        alertbuilder.setView(dialog_view);

        alertbuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                TextInputEditText et_ice_number = dialog_view.findViewById(R.id.et_list_mobilenumber);
                Spinner sp_relations = dialog_view.findViewById(R.id.sp_relations);

                if (sp_relations.getSelectedItemPosition() <=0){
                    Toast.makeText(getApplicationContext(),"How are you Related to Emergency contact",Toast.LENGTH_LONG).show();
                    sp_relations.requestFocus();
                }else if (et_ice_number.getText().toString().length() != 10){
                    et_ice_number.setError("Enter Valid Mobile Number");
                    et_ice_number.requestFocus();
                }else {
                    String relation = sp_relations.getAdapter().getItem(sp_relations.getSelectedItemPosition()).toString();
                    String number = et_ice_number.getText().toString();

                    try {
                        int numb = Integer.parseInt(number);

                        if (numb>0){
                            c_E_contact ice_contact = new c_E_contact(relation,number);
                            userDb.add_emergency_contact(ice_contact);
                        }
                    }catch (Exception ignored){
                        et_ice_number.setError("Invalid Mobile Number");
                        et_ice_number.requestFocus();
                    }

                    refresh_contacts_list();
                }

            }
        });

        alertbuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                refresh_contacts_list();
            }
        });

        alertbuilder.show();
    }

    public void refresh_contacts_list(){
        ice_recycler_Adapter contactsAdapter = new ice_recycler_Adapter(this,userDb.contacts_list(),true);
        recycler_ice_contacts.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recycler_ice_contacts.setAdapter(contactsAdapter);
    }
    //==============================================METHODS=========================================

    //----------------------------------------------INTENTS-----------------------------------------
    void toDashboard(){
        SharedPreferences state_pref = getSharedPreferences(common.PREFERENCE_KEY,MODE_PRIVATE);
        SharedPreferences.Editor pref_editor = state_pref.edit();
        pref_editor.putInt(common.REGISTRATION_STEP_KEY,common.STEP_REG_COMPLETED).apply();
        Intent dashboard_intent = new Intent(getApplicationContext(),Dashboard.class);
        dashboard_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(dashboard_intent);
        super.finish();
    }
    //----------------------------------------------INTENTS-----------------------------------------
}
