package com.jedi_supreme.stateportal.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jedi_supreme.stateportal.R;
import com.jedi_supreme.stateportal.activities.complaints_activity;
import com.jedi_supreme.stateportal.databases.user_db;
import com.jedi_supreme.stateportal.models.c_Complaints;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class complain_recycler_adapter extends RecyclerView.Adapter {

    private ArrayList<c_Complaints> complaints;
    private WeakReference<complaints_activity> weak_complain;

    public complain_recycler_adapter(complaints_activity comp_activity, ArrayList<c_Complaints> complains_list) {
        weak_complain = new WeakReference<>(comp_activity);
        this.complaints = complains_list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.complain_list_row,parent,false);
        return new complaints_list_holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ((complaints_list_holder) holder).bind_views(complaints.get(position));
    }

    @Override
    public int getItemCount() {
        return complaints.size();
    }

    public class complaints_list_holder extends RecyclerView.ViewHolder {

        TextView tv_address,tv_date,tv_body;
        Button bt_delete;
        user_db userDb;

                complaints_list_holder(View itemView) {
            super(itemView);

            tv_address = itemView.findViewById(R.id.tv_list_comp_address);
            tv_body = itemView.findViewById(R.id.tv_list_body);
            tv_date = itemView.findViewById(R.id.tv_list_comp_date);

            bt_delete = itemView.findViewById(R.id.bt_list_delete);
            WeakReference<Context> weak_mcontext = new WeakReference<>(itemView.getContext());

            userDb = new user_db(weak_mcontext.get(),null);

        }

        void bind_views(final c_Complaints complaints){

            tv_address.setText(complaints.getPlace_name());
            tv_body.setText(complaints.getComplaint());
            tv_date.setText(complaints.getDate());

            bt_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userDb.delete_complain(complaints);
                    weak_complain.get().refresh_list();
                }
            });

        }

    }
}
