package com.jedi_supreme.stateportal.models;

public class c_E_contact {

    public static final String TABLE = "EMERGENCY";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_REL = "relation";
    public static final String COLUMN_NUMBER = "mobile_number";

    private String relation;
    private String number;

    public c_E_contact(String relation, String number) {
        this.relation = relation;
        this.number = number;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
