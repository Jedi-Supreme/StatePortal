package com.jedi_supreme.stateportal.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jedi_supreme.stateportal.models.c_Complaints;
import com.jedi_supreme.stateportal.models.c_E_contact;
import com.jedi_supreme.stateportal.models.c_User;

import java.util.ArrayList;

public class user_db extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "user.db";
    private static final int DATABASE_VERSION = 1;

    public user_db(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String e_contact_query = "CREATE TABLE IF NOT EXISTS " + c_E_contact.TABLE + " ("
                + c_E_contact.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + c_E_contact.COLUMN_REL + " TEXT UNIQUE, "
                + c_E_contact.COLUMN_NUMBER + " TEXT );";
        db.execSQL(e_contact_query);

        String complain_query = "CREATE TABLE IF NOT EXISTS " + c_Complaints.TABLE + " ( "
                + c_Complaints.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + c_Complaints.COLUMN_DATE + " TEXT, "
                + c_Complaints.COLUMN_PLACE_NAME + " TEXT, "
                + c_Complaints.COLUMN_MSG + " TEXT);";
        db.execSQL(complain_query);

        String user_query = "CREATE TABLE IF NOT EXISTS " + c_User.TABLE + " ( "
                + c_User.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + c_User.COLUMN_FIRE_ID + " TEXT UNIQUE NOT NULL, "
                + c_User.COLUMN_FN + " TEXT, "
                + c_User.COLUMN_LN + " TEXT, "
                + c_User.COLUMN_MOBILE + " TEXT, "
                + c_User.COLUMN_HOME_ADD + " TEXT, "
                + c_User.COLUMN_LATITUDE + " INTEGER, "
                + c_User.COLUMN_LONGITUDE + " INTEGER); ";
        db.execSQL(user_query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + c_User.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + c_E_contact.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + c_Complaints.TABLE);
        onCreate(db);
    }

    //===========================================RETURNED OBJECTS===================================
    public Cursor all_contacts(){
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery("SELECT * FROM " + c_E_contact.TABLE,null);
    }

    public Cursor user_info(){

        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery("SELECT * FROM " + c_User.TABLE,null);
    }

    public c_User complain_user(){

        SQLiteDatabase db = getReadableDatabase();

        Cursor c =  db.rawQuery("SELECT * FROM " + c_User.TABLE,null);
        c.moveToFirst();

        c_User comp_user = new c_User(
                c.getString(c.getColumnIndexOrThrow(c_User.COLUMN_FN)),
                c.getString(c.getColumnIndexOrThrow(c_User.COLUMN_LN)),
                c.getString(c.getColumnIndexOrThrow(c_User.COLUMN_MOBILE)));

        c.close();

        return comp_user;
    }

    public c_User app_user(){

        SQLiteDatabase db = getReadableDatabase();

        Cursor c =  db.rawQuery("SELECT * FROM " + c_User.TABLE,null);
        c.moveToFirst();

        c_User app_user = new c_User(
                c.getString(c.getColumnIndexOrThrow(c_User.COLUMN_FIRE_ID)),
                c.getString(c.getColumnIndexOrThrow(c_User.COLUMN_FN)),
                c.getString(c.getColumnIndexOrThrow(c_User.COLUMN_LN)),
                c.getString(c.getColumnIndexOrThrow(c_User.COLUMN_MOBILE)),
                c.getString(c.getColumnIndexOrThrow(c_User.COLUMN_HOME_ADD)),
                c.getDouble(c.getColumnIndexOrThrow(c_User.COLUMN_LATITUDE)),
                c.getDouble(c.getColumnIndexOrThrow(c_User.COLUMN_LONGITUDE)));

        c.close();

        return app_user;
    }

    public ArrayList<c_Complaints> complains_list(){

        ArrayList<c_Complaints> comp_list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c =  db.rawQuery("SELECT * FROM " + c_Complaints.TABLE,null);

        while (c.moveToNext()){

            c_Complaints dbcomplains = new c_Complaints(
                    c.getInt(c.getColumnIndexOrThrow(c_Complaints.COLUMN_ID)),
                    c.getString(c.getColumnIndexOrThrow(c_Complaints.COLUMN_MSG)),
                    c.getString(c.getColumnIndexOrThrow(c_Complaints.COLUMN_DATE)),
                    c.getString(c.getColumnIndexOrThrow(c_Complaints.COLUMN_PLACE_NAME))
            );

            comp_list.add(dbcomplains);
        }

        c.close();
        return comp_list;
    }

    public ArrayList<c_E_contact> contacts_list(){

        ArrayList<c_E_contact> cont_list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c =  db.rawQuery("SELECT * FROM " + c_E_contact.TABLE,null);

        while (c.moveToNext()){

            c_E_contact contact = new c_E_contact(
                    c.getString(c.getColumnIndexOrThrow(c_E_contact.COLUMN_REL)),
                    c.getString(c.getColumnIndexOrThrow(c_E_contact.COLUMN_NUMBER)));

            cont_list.add(contact);
        }

        c.close();
        return cont_list;
    }

    //===========================================RETURNED OBJECTS===================================

    //============================================METHODS===========================================
    public void add_user(c_User user){

        SQLiteDatabase sqDB = getWritableDatabase();

        ContentValues user_values = new ContentValues();

        user_values.put(c_User.COLUMN_FIRE_ID,user.getFire_id());
        user_values.put(c_User.COLUMN_FN,user.getFn());
        user_values.put(c_User.COLUMN_LN,user.getLn());
        user_values.put(c_User.COLUMN_MOBILE,user.getMobile());
        user_values.put(c_User.COLUMN_HOME_ADD,user.getHomeAddress());
        user_values.put(c_User.COLUMN_LATITUDE,user.getHome_latitude());
        user_values.put(c_User.COLUMN_LONGITUDE,user.getHome_longitude());

        sqDB.insert(c_User.TABLE,null,user_values);
    }

    public void update_user(c_User user){

        SQLiteDatabase sqDB = getWritableDatabase();

        ContentValues user_values = new ContentValues();

        user_values.put(c_User.COLUMN_FIRE_ID,user.getFire_id());
        user_values.put(c_User.COLUMN_FN,user.getFn());
        user_values.put(c_User.COLUMN_LN,user.getLn());
        user_values.put(c_User.COLUMN_MOBILE,user.getMobile());
        user_values.put(c_User.COLUMN_HOME_ADD,user.getHomeAddress());
        user_values.put(c_User.COLUMN_LATITUDE,user.getHome_latitude());
        user_values.put(c_User.COLUMN_LONGITUDE,user.getHome_longitude());

        sqDB.update(c_User.TABLE,user_values,c_User.COLUMN_FIRE_ID + " =? ",new String[]{user.getFire_id()});
    }

    public void add_emergency_contact(c_E_contact e_contact){

        SQLiteDatabase sqDB = getWritableDatabase();

        ContentValues e_values = new ContentValues();
        e_values.put(c_E_contact.COLUMN_REL,e_contact.getRelation());
        e_values.put(c_E_contact.COLUMN_NUMBER,e_contact.getNumber());

        sqDB.insert(c_E_contact.TABLE,null,e_values);
    }

    public void add_complaint(c_Complaints compalaint){

        SQLiteDatabase sqDB = getWritableDatabase();
        ContentValues complain_values = new ContentValues();

        complain_values.put(c_Complaints.COLUMN_DATE,compalaint.getDate());
        complain_values.put(c_Complaints.COLUMN_MSG,compalaint.getComplaint());
        complain_values.put(c_Complaints.COLUMN_PLACE_NAME,compalaint.getPlace_name());

        sqDB.insert(c_Complaints.TABLE,null,complain_values);
    }

    public void delete_contact(String relation){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM " + c_E_contact.TABLE + " WHERE "
                + c_E_contact.COLUMN_REL + " = \"" + relation + "\"");
    }

    public void delete_complain(c_Complaints compalaint){

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM " + c_Complaints.TABLE + " WHERE "
                + c_Complaints.COLUMN_ID + " = " + compalaint.get_id());
    }
    //===========================================METHODS============================================

}
