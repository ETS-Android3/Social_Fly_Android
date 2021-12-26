package com.gautam.socialfly.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.gautam.socialfly.Model.UserModel;
import com.gautam.socialfly.R;
import com.gautam.socialfly.ShowProfile;
import com.gautam.socialfly.notify.sendNotification;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.jetbrains.annotations.NotNull;

import static android.content.Context.MODE_PRIVATE;

public class RequestsFragment extends Fragment
{
    private View RequestsFragmentView;
    private RecyclerView myRequestsList;

    private DatabaseReference ChatRequestsRef, UsersRef, ContactsRef;
    private String currentUserID;



    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("socialflycredentials", MODE_PRIVATE);
        currentUserID = sharedPreferences.getString("user","nouser");

        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");
        ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");


        myRequestsList = (RecyclerView) RequestsFragmentView.findViewById(R.id.requests_recview);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));


        return RequestsFragmentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<UserModel> options =
                new FirebaseRecyclerOptions.Builder<UserModel>()
                        .setQuery(ChatRequestsRef.child(currentUserID), UserModel.class)
                        .build();


        FirebaseRecyclerAdapter<UserModel, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<UserModel, RequestsViewHolder>(options) {

                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull UserModel model)
                    {

                        final String list_user_id = getRef(position).getKey();

                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    String type = dataSnapshot.getValue().toString();

                                    if (type.equals("received"))
                                    {
                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot snapshot)
                                            {
                                                String request_name = snapshot.child("name").getValue().toString();
                                                String request_profilepicURL = snapshot.child("profilepic").getValue().toString();
                                                String request_username = snapshot.child("username").getValue().toString();

                                                holder.name_tv.setText(request_name);
                                                holder.username_tv.setText(request_username);
                                                Glide.with(holder.img1.getContext())
                                                        .load(request_profilepicURL).centerCrop()
                                                        .into(holder.img1);

                                                holder.accept.setOnClickListener(v->{
                                                    acceptance(list_user_id);
                                                });
                                                holder.reject.setOnClickListener(v->
                                                {
                                                    rejectance(list_user_id);
                                                });

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                    else if (type.equals("sent"))
                                    {
                                        Button request_sent_btn = holder.itemView.findViewById(R.id.accept_request);
                                        request_sent_btn.setText("Withdraw request");

                                        holder.itemView.findViewById(R.id.reject_request).setVisibility(View.INVISIBLE);


                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot snapshot)
                                            {
                                                String request_name = snapshot.child("name").getValue().toString();
                                                String request_profilepicURL = snapshot.child("profilepic").getValue().toString();
                                                String request_username = snapshot.child("username").getValue().toString();

                                                holder.name_tv.setText(request_name);
                                                holder.username_tv.setText(request_username);
                                                Glide.with(holder.img1.getContext())
                                                        .load(request_profilepicURL).centerCrop()
                                                        .into(holder.img1);

                                                holder.accept.setOnClickListener(v->{
                                                    rejectance(list_user_id);
                                                });


                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.requests_singlerow, viewGroup, false);
                        RequestsViewHolder holder = new RequestsViewHolder(view);
                        return holder;
                    }
                };

        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {
        TextView name_tv,username_tv;
        ImageView img1;
        Button accept,reject;
        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            name_tv= itemView.findViewById(R.id.name_tv);
            username_tv= itemView.findViewById(R.id.username_tv);
            img1= itemView.findViewById(R.id.img1);
            accept= itemView.findViewById(R.id.accept_request);
            reject= itemView.findViewById(R.id.reject_request);
        }
    }

    public void acceptance(String list_user_id)
    {
        ContactsRef.child(currentUserID).child(list_user_id).child("Contact")
                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    ContactsRef.child(list_user_id).child(currentUserID).child("Contact")
                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                ChatRequestsRef.child(currentUserID).child(list_user_id)
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    ChatRequestsRef.child(list_user_id).child(currentUserID)
                                                            .removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    sendNotification sendNoti =new  sendNotification(list_user_id,getContext(),
                                                                            getActivity(),currentUserID+" accepted your request.","request",currentUserID);
                                                                    sendNoti.noti_prep();
                                                                    if (task.isSuccessful())
                                                                    {Toast.makeText(getContext(), "New Contact Saved", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        });
                            }
                        }
                    });
                }
            }
        });
    }

    private void rejectance(String list_user_id) {
        ChatRequestsRef.child(currentUserID).child(list_user_id)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            ChatRequestsRef.child(list_user_id).child(currentUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(getContext(), "Successfull", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

}
