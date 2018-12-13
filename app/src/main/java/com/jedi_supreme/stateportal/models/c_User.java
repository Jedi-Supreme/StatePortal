package com.jedi_supreme.stateportal.models;

import java.util.ArrayList;

public class c_User {

    public static final String TABLE = "USER";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FIRE_ID = "fire_id";
    public static final String COLUMN_FN = "firstname";
    public static final String COLUMN_LN = "lastname";
    public static final String COLUMN_HOME_ADD = "home_address";
    public static final String COLUMN_MOBILE = "mobile_number";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";

    private String fn;
    private String ln;
    private String mobile;
    private String fire_id;
    private String date_time;
    private String state;
    private ArrayList<String>ice_contacts_list;
    private String homeAddress;
    private double home_latitude;
    private double home_longitude;
    private double e_latitude;
    private double e_longitude;

    //empty constructor
    public c_User() {
    }

    //complaints_constructor
    public c_User(String fn, String ln, String mobile) {
        this.fn = fn;
        this.ln = ln;
        this.mobile = mobile;
    }

    //emergency constructor
    public c_User(
            String fire_id, String fn, String ln,
            String date_time, double e_latitude, double e_longitude) {
        this.fire_id = fire_id;
        this.date_time = date_time;
        this.state = "N";
        this.fn = fn;
        this.ln = ln;
        this.e_latitude = e_latitude;
        this.e_longitude = e_longitude;
    }

    //firebase constructor
    public c_User(
            String fire_id, String fn, String ln,
            String mobile,String homeAddress,
            double home_latitude, double home_longitude,
            ArrayList<String> ice_contacts_list) {
        this.fire_id = fire_id;
        this.fn = fn;
        this.ln = ln;
        this.mobile = mobile;
        this.homeAddress = homeAddress;
        this.ice_contacts_list = ice_contacts_list;
        this.home_latitude = home_latitude;
        this.home_longitude = home_longitude;
    }

    //local db constructor
    public c_User(
            String fire_id,String fn, String ln,
            String mobile, String homeAddress,
            double home_latitude, double home_longitude) {
        this.fire_id = fire_id;
        this.fn = fn;
        this.ln = ln;
        this.mobile = mobile;
        this.homeAddress = homeAddress;
        this.home_latitude = home_latitude;
        this.home_longitude = home_longitude;
    }

    public String getFn() {
        return fn;
    }

    public void setFn(String fn) {
        this.fn = fn;
    }

    public String getLn() {
        return ln;
    }

    public void setLn(String ln) {
        this.ln = ln;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public double getHome_latitude() {
        return home_latitude;
    }

    public void setHome_latitude(double home_latitude) {
        this.home_latitude = home_latitude;
    }

    public double getHome_longitude() {
        return home_longitude;
    }

    public void setHome_longitude(double home_longitude) {
        this.home_longitude = home_longitude;
    }

    public ArrayList<String> getIce_contacts_list() {
        return ice_contacts_list;
    }

    public void setIce_contacts_list(ArrayList<String> ice_contacts_list) {
        this.ice_contacts_list = ice_contacts_list;
    }

    public double getE_longitude() {
        return e_longitude;
    }

    public void setE_longitude(double e_longitude) {
        this.e_longitude = e_longitude;
    }

    public double getE_latitude() {
        return e_latitude;
    }

    public void setE_latitude(double e_latitude) {
        this.e_latitude = e_latitude;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getFire_id() {
        return fire_id;
    }

    public void setFire_id(String fire_id) {
        this.fire_id = fire_id;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
