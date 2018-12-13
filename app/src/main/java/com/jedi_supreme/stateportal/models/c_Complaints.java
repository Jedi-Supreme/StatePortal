package com.jedi_supreme.stateportal.models;

public class c_Complaints {

    public static final String TABLE = "COMPLAINTS";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_PLACE_NAME = "place_name";
    public static final String COLUMN_MSG = "complain_msg";

    private int _id;
    private c_User user;
    private String complaint;
    private String Date;
    private String place_name;
    private Double comp_latitude;
    private Double comp_longitude;

    //local constructor
    public c_Complaints(int _id, String complaint, String date, String place_name) {
        this._id = _id;
        this.complaint = complaint;
        Date = date;
        this.place_name = place_name;
    }

    //online constructor
    public c_Complaints(
            c_User user, String place_name,
            String complaint, String date,
            Double comp_latitude, Double comp_longitude) {
        this.user = user;
        this.Date = date;
        this.complaint = complaint;
        this.place_name = place_name;
        this.comp_latitude = comp_latitude;
        this.comp_longitude = comp_longitude;
    }

    public c_User getUser() {
        return user;
    }

    public void setUser(c_User user) {
        this.user = user;
    }

    public String getComplaint() {
        return complaint;
    }

    public void setComplaint(String complaint) {
        this.complaint = complaint;
    }

    public Double getComp_latitude() {
        return comp_latitude;
    }

    public void setComp_latitude(Double comp_latitude) {
        this.comp_latitude = comp_latitude;
    }

    public Double getComp_longitude() {
        return comp_longitude;
    }

    public void setComp_longitude(Double comp_longitude) {
        this.comp_longitude = comp_longitude;
    }

    public String getPlace_name() {
        return place_name;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }
}
