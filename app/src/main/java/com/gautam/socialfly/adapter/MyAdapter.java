package com.gautam.socialfly.adapter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.gautam.socialfly.Model.UserModel;
import com.gautam.socialfly.R;
import com.gautam.socialfly.ShowProfile;
import com.gautam.socialfly.ViewProfile;

import org.jetbrains.annotations.NotNull;

import static android.content.Context.MODE_PRIVATE;

public class MyAdapter extends FirebaseRecyclerAdapter <UserModel,MyAdapter.MyViewHolder>
{
    String currentuser;

    public MyAdapter(FirebaseRecyclerOptions<UserModel> options,String currentuser) {
        super(options);
        this.currentuser=currentuser;
    }

    protected void onBindViewHolder(MyViewHolder holder, int position, UserModel model)
    {
        holder.name_tv.setText(model.getName());
        holder.username_tv.setText(model.getUsername());
        Glide.with(holder.img1.getContext())
                .load(model.getProfilepic()).centerCrop()
                .into(holder.img1);

        holder.itemView.setOnClickListener(v->{

            if(currentuser.equals(holder.username_tv.getText()))
            {
                Intent intent = new Intent(holder.name_tv.getContext(), ViewProfile.class);
                holder.username_tv.getContext().startActivity(intent);
            }
            else
            {
                Intent intent = new Intent(holder.name_tv.getContext(), ShowProfile.class);
                intent.putExtra("show_username",holder.username_tv.getText().toString());
                intent.putExtra("show_name",holder.name_tv.getText().toString());
                intent.putExtra("show_dp",model.profilepic.toString());

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                holder.username_tv.getContext().startActivity(intent);
            }
        });
    }


    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.singlerow,parent,false);
        return new MyViewHolder(v);
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView name_tv,username_tv;
        ImageView img1;
        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            name_tv= itemView.findViewById(R.id.name_tv);
            username_tv= itemView.findViewById(R.id.username_tv);
            img1= itemView.findViewById(R.id.img1);
        }
    }
}