package com.jedi_supreme.stateportal.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jedi_supreme.stateportal.R;
import com.jedi_supreme.stateportal.adapters.ice_recycler_Adapter;
import com.jedi_supreme.stateportal.common;
import com.jedi_supreme.stateportal.databases.user_db;
import com.jedi_supreme.stateportal.models.c_E_contact;
import com.jedi_supreme.stateportal.models.c_User;

import java.util.ArrayList;

public class UserProfile extends AppCompatActivity {

    TextView tv_profile_number;
    TextInputEditText et_profile_fn, et_profile_ln, et_profile_homeAddress;
    Button bt_profile_map, bt_profile_save, bt_profile_edit, bt_profile_add;
    ConstraintLayout const_profile_layout;

    c_User app_user;
    boolean editable = false;

    int PLACE_PICKER_REQUEST = 167;
    Place place;

    RecyclerView recycler_profile_contacts;
    user_db userDb;

    //==============================================ON CREATE=======================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userDb = new user_db(getApplicationContext(),null);

        app_user = userDb.app_user();

        tv_profile_number = findViewById(R.id.tv_profile_number);
        et_profile_fn = findViewById(R.id.et_profile_fn);
        et_profile_ln = findViewById(R.id.et_profile_ln);
        et_profile_homeAddress = findViewById(R.id.et_profile_homeadd);

        recycler_profile_contacts = findViewById(R.id.recycler_profile_contacts);
        const_profile_layout = findViewById(R.id.user_profile_layout);

        bt_profile_edit = findViewById(R.id.bt_profile_edit);
        bt_profile_map = findViewById(R.id.bt_profile_homeselector);
        bt_profile_save = findViewById(R.id.bt_profile_save);
        bt_profile_add = findViewById(R.id.bt_profile_add);

        //set user values to views
        tv_profile_number.setText(app_user.getMobile());
        et_profile_fn.setText(app_user.getFn());
        et_profile_ln.setText(app_user.getLn());
        et_profile_homeAddress.setText(app_user.getHomeAddress());

        refresh_contacts_list(editable);
    }
    //==============================================ON CREATE=======================================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {

            place = PlacePicker.getPlace(getApplicationContext(),data);

            String home_add = place.getName() + ", " + place.getAddress();
            et_profile_homeAddress.setText(home_add);
        }
    }

    //------------------------------------------------METHODS---------------------------------------
    public void refresh_contacts_list(boolean edit_mode){
        ice_recycler_Adapter iceRecyclerAdapter = new ice_recycler_Adapter(this,userDb.contacts_list(),edit_mode);
        recycler_profile_contacts.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recycler_profile_contacts.setAdapter(iceRecyclerAdapter);
    }

    //enable editing
    void edit_mode(boolean iseditable){

        if (iseditable){

            bt_profile_edit.setVisibility(View.INVISIBLE);
            enable_buttons(new Button[]{bt_profile_map,bt_profile_save,bt_profile_add});

            enable_text_input(new TextInputEditText[]{et_profile_fn,et_profile_ln});

            refresh_contacts_list(true);
        }else {

            bt_profile_edit.setVisibility(View.VISIBLE);
            disable_buttons(new Button[]{bt_profile_map,bt_profile_save,bt_profile_add});

            disable_text_input(new TextInputEditText[]{et_profile_fn,et_profile_ln});

            refresh_contacts_list(false);
        }

    }

    //============================================VIEW MANIPULATIONS================================
    void enable_text_input(TextInputEditText[] text_input_fields){

        for (TextInputEditText edittext : text_input_fields){
            edittext.setFocusableInTouchMode(true);
            edittext.setFocusable(true);
            edittext.setClickable(true);
            edittext.setLongClickable(true);
        }
    }
    void disable_text_input(TextInputEditText[] text_input_fields){

        for (TextInputEditText edittext : text_input_fields){
            edittext.setFocusableInTouchMode(false);
            edittext.setFocusable(false);
            edittext.setClickable(false);
            edittext.setLongClickable(false);
        }
    }

    void enable_buttons(Button[] buttons){
        for (Button button : buttons){
            button.setVisibility(View.VISIBLE);
        }
    }
    void disable_buttons(Button[] buttons){
        for (Button button : buttons){
            button.setVisibility(View.GONE);
        }
    }
    //============================================VIEW MANIPULATIONS================================

    void add_contacts(){
        try {

            if (recycler_profile_contacts.getAdapter().getItemCount() >= 3){
                common.snackbar(const_profile_layout,"Maximum of 3 Contacts allowed").show();
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
        final View dialog_view = inflater.inflate(R.layout.dialog_input,const_profile_layout,false);
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

                    refresh_contacts_list(true);
                }

            }
        });

        alertbuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                refresh_contacts_list(true);
            }
        });

        alertbuilder.show();
    }

    void test_inputs(){

        String fn = app_user.getFn();
        String ln = app_user.getLn();
        String home_address = app_user.getHomeAddress();

        if (!et_profile_fn.getText().toString().equals(app_user.getFn()) && !et_profile_fn.getText().toString().isEmpty()){
            fn = et_profile_fn.getText().toString();
        }

        if (!et_profile_ln.getText().toString().equals(app_user.getLn()) && !et_profile_ln.getText().toString().isEmpty()){
            ln = et_profile_ln.getText().toString();
        }

        if(!et_profile_homeAddress.getText().toString().equals(app_user.getHomeAddress()) && place != null){
            home_address = et_profile_homeAddress.getText().toString();

            //local constructor
            c_User localDB_user = new c_User(
                    app_user.getFire_id(),fn,ln,
                    app_user.getMobile(),
                    home_address,
                    place.getLatLng().latitude,
                    place.getLatLng().longitude);

            userDb.update_user(localDB_user);
            save_user_details_online(localDB_user);

        }else {

            //local constructor
            c_User localDB_user = new c_User(
                    app_user.getFire_id(),fn,ln,
                    app_user.getMobile(),
                    home_address,
                    app_user.getHome_latitude(),
                    app_user.getHome_longitude());

                userDb.update_user(localDB_user);
                save_user_details_online(localDB_user);

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
        users_profile_ref.child(fireDB_user.getFire_id()).setValue(fireDB_user);
        edit_mode(false);
        common.snackbar(const_profile_layout,"Profile Updated").show();
    }
    //------------------------------------------------METHODS---------------------------------------

    //--------------------------------------------BUTTON CLICK LISTENERS----------------------------
    public void Details_update_save(View view) {
        test_inputs();
        edit_mode(false);
    }

    public void Address_picker_update(View view) throws GooglePlayServicesNotAvailableException,
            GooglePlayServicesRepairableException {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        startActivityForResult(builder.build(UserProfile.this), PLACE_PICKER_REQUEST);
    }

    public void enter_edit_mode(View view) {
        edit_mode(true);
    }

    public void add_ice_contact(View view) {
        add_contacts();
    }
    //--------------------------------------------BUTTON CLICK LISTENERS----------------------------
}
