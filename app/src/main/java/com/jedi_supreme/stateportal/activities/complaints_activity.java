package com.jedi_supreme.stateportal.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jedi_supreme.stateportal.R;
import com.jedi_supreme.stateportal.adapters.complain_recycler_adapter;
import com.jedi_supreme.stateportal.databases.user_db;

public class complaints_activity extends AppCompatActivity {

    RecyclerView recycler_comp_list;
    user_db userDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaints);

        userDb = new user_db(getApplicationContext(),null);
        recycler_comp_list = findViewById(R.id.recycler_complaints);

        try {
            refresh_list();
        }catch (Exception ignored){}
    }

    public void refresh_list(){

        complain_recycler_adapter compRecyclerAdapter = new complain_recycler_adapter(this,userDb.complains_list());
        recycler_comp_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recycler_comp_list.setAdapter(compRecyclerAdapter);

    }

}
