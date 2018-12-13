package com.jedi_supreme.stateportal.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jedi_supreme.stateportal.R;
import com.jedi_supreme.stateportal.activities.UserDetails;
import com.jedi_supreme.stateportal.activities.UserProfile;
import com.jedi_supreme.stateportal.databases.user_db;
import com.jedi_supreme.stateportal.models.c_E_contact;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ice_recycler_Adapter extends RecyclerView.Adapter {

    private ArrayList<c_E_contact> ice_contacts;
    private WeakReference<AppCompatActivity> weak_activity;
    private boolean editable;

    public ice_recycler_Adapter(AppCompatActivity activity, ArrayList<c_E_contact> contacts_list, boolean editable) {
        weak_activity = new WeakReference<>(activity);
        this.ice_contacts = contacts_list;
        this.editable = editable;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ice_row_layout,parent,false);
        return new contacts_list_holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ((contacts_list_holder) holder).bind_views(ice_contacts.get(position));
    }

    @Override
    public int getItemCount() {
        return ice_contacts.size();
    }

    public class contacts_list_holder extends RecyclerView.ViewHolder {

        TextView tv_relation,tv_number;
        Button bt_contact_delete;
        user_db userDb;

        contacts_list_holder(View itemView) {
            super(itemView);

            tv_relation = itemView.findViewById(R.id.tv_ice_relation);
            tv_number = itemView.findViewById(R.id.tv_ice_number);
            bt_contact_delete = itemView.findViewById(R.id.bt_contact_delete);

            WeakReference<Context> weak_mcontext = new WeakReference<>(itemView.getContext());

            userDb = new user_db(weak_mcontext.get(),null);

        }

        void bind_views(final c_E_contact contact){

            tv_relation.setText(contact.getRelation());
            tv_number.setText(contact.getNumber());

            if (editable){
                bt_contact_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (weak_activity.get() instanceof UserDetails){
                            userDb.delete_contact(contact.getRelation());
                            ((UserDetails) weak_activity.get()).refresh_contacts_list();
                        }else {
                            if (ice_recycler_Adapter.this.getItemCount() > 1){
                                userDb.delete_contact(contact.getRelation());
                                ((UserProfile) weak_activity.get()).refresh_contacts_list(true);
                            }else {
                                Toast.makeText(weak_activity.get(), "Emergency Contact Required", Toast.LENGTH_SHORT).show();
                                ((UserProfile) weak_activity.get()).refresh_contacts_list(false);
                            }
                        }
                    }
                });
            }else {
                bt_contact_delete.setVisibility(View.GONE);
            }

        }

    }
}
