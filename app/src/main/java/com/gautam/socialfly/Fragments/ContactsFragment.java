package com.gautam.socialfly.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.gautam.socialfly.Model.UserModel;
import com.gautam.socialfly.R;
import com.gautam.socialfly.ShowProfile;
import com.gautam.socialfly.ViewProfile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.jetbrains.annotations.NotNull;

import static android.content.Context.MODE_PRIVATE;

public class ContactsFragment extends Fragment {

    private View contactsView;
    private RecyclerView  contacts_recview;
    private DatabaseReference contactsRef,usersRef;
    private String currentuser;

    SharedPreferences sharedPreferences;

    public ContactsFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        contacts_recview = (RecyclerView)contactsView.findViewById(R.id.contacts_recview);
        contacts_recview.setLayoutManager(new LinearLayoutManager(getContext()));

        sharedPreferences = getActivity().getSharedPreferences("socialflycredentials", MODE_PRIVATE);
        currentuser = sharedPreferences.getString("user","nouser");

        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentuser);
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        updateToken();
        return contactsView;
    }

    private  void updateToken()
    {

        String refreshtoken = FirebaseInstanceId.getInstance().getToken();

        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference("users");
        UsersRef.child(currentuser)
                .child("tokenid").setValue(refreshtoken);
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=
                new FirebaseRecyclerOptions.Builder<UserModel>()
                .setQuery(contactsRef,UserModel.class)
                .build();

        FirebaseRecyclerAdapter<UserModel,contactsViewHolder> cAdapter
                = new FirebaseRecyclerAdapter<UserModel, contactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(contactsViewHolder holder, int position, UserModel model)
            {
                String userIDs = getRef(position).getKey();
                usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot)
                    {
                        String username = snapshot.child("username").getValue().toString();
                        String name = snapshot.child("name").getValue().toString();
                        String profilepicURL = snapshot.child("profilepic").getValue().toString();

                        holder.name_tv.setText(name);
                        holder.username_tv.setText(username);
                        Glide.with(holder.img1.getContext())
                                .load(profilepicURL).centerCrop()
                                .into(holder.img1);

                        holder.itemView.setOnClickListener(v->{
                            Intent intent = new Intent(holder.name_tv.getContext(), ShowProfile.class);
                            intent.putExtra("show_username",holder.username_tv.getText().toString());
                            intent.putExtra("show_name",holder.name_tv.getText().toString());
                            intent.putExtra("show_dp",profilepicURL);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            holder.username_tv.getContext().startActivity(intent);
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }

            public contactsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext()).
                        inflate(R.layout.singlerow,viewGroup,false);
                contactsViewHolder cViewHolder = new contactsViewHolder(view);
                return cViewHolder;
            }
        };
        contacts_recview.setAdapter(cAdapter);
        cAdapter.startListening();
    }

    public  static  class contactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView name_tv,username_tv;
        ImageView img1;
        public contactsViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            name_tv= itemView.findViewById(R.id.name_tv);
            username_tv= itemView.findViewById(R.id.username_tv);
            img1= itemView.findViewById(R.id.img1);
        }
    }
}