package com.gautam.socialfly.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.gautam.socialfly.ChatActivity;
import com.gautam.socialfly.HomeActivity;
import com.gautam.socialfly.Model.ChatModel;
import com.gautam.socialfly.Model.UserModel;
import com.gautam.socialfly.R;
import com.gautam.socialfly.ShowProfile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import static android.content.Context.MODE_PRIVATE;

public class ChatsFragment extends Fragment {

    View chatlistFragment;
    RecyclerView chatlist_recview;
    SharedPreferences sharedPreferences;

    String currentuser;

    DatabaseReference UsersRef,ChatlistRef;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        chatlistFragment= inflater.inflate(R.layout.fragment_chats, container, false);

        sharedPreferences = getActivity().getSharedPreferences("socialflycredentials", MODE_PRIVATE);
        currentuser = sharedPreferences.getString("user","nouser");

        ChatlistRef = FirebaseDatabase.getInstance().getReference().child("Chatlist").child(currentuser);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");

        chatlist_recview=chatlistFragment.findViewById(R.id.chatlist_recview);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        chatlist_recview.setLayoutManager(linearLayoutManager);
        return chatlistFragment;
    }
    public void onStart() {
        super.onStart();

        Query query= ChatlistRef.orderByChild("last_chat");
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<UserModel>()
                        .setQuery(query,UserModel.class)
                        .build();

        FirebaseRecyclerAdapter<UserModel, ChatsFragment.chatlistsViewHolder> cAdapter
                = new FirebaseRecyclerAdapter<UserModel, ChatsFragment.chatlistsViewHolder>(options) {


            @Override
            protected void onBindViewHolder(ChatsFragment.chatlistsViewHolder holder, int position, UserModel model)
            {
                String userIDs = getRef(position).getKey();
                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot)
                    {
                        String username = snapshot.child("username").getValue().toString();
                        String name = snapshot.child("name").getValue().toString();
                        String profilepicURL = snapshot.child("profilepic").getValue().toString();

                        holder.name_tv.setText(name);

                        Glide.with(holder.img1.getContext())
                                .load(profilepicURL).centerCrop()
                                .into(holder.img1);

                        holder.itemView.setOnClickListener(v->
                        {
                            Intent intent = new Intent(getContext(), ChatActivity.class);
                            intent.putExtra("receiver_username",username);
                            startActivity(intent);
                        });

                        holder.itemView.setOnLongClickListener(new View.OnLongClickListener()
                        {
                            @Override
                            public boolean onLongClick(View v) {
                                CharSequence options[] = new   CharSequence[]
                                        {
                                                "Delete Chat","Cancel"
                                        };

                                AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                                builder.setTitle("Delete Chat");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int option)
                                    {
                                        if(option==0)
                                        {
                                            DatabaseReference RootRef = FirebaseDatabase.getInstance()
                                                    .getReference();

                                            RootRef.child("Chatlist").child(currentuser).child(username).removeValue();
                                            RootRef.child("Chats").child(currentuser).child(username).removeValue();
                                            Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                });
                                builder.show();
                                return false;
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

                FirebaseDatabase.getInstance().getReference("Chats")
                        .child(currentuser).child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                            {
                                ChatModel chatModel = snapshot.getValue(ChatModel.class);
                                String type=chatModel.getType();
                                if(type.equals("text"))
                                {
                                    holder.username_tv.setText(chatModel.getMessage());
                                }else if(type.equals("image"))
                                {
                                    holder.username_tv.setText("> Photo");
                                }else{
                                    holder.username_tv.setText("> Document");
                                }
                            }
                        }else
                        {
                            holder.username_tv.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }

            public ChatsFragment.chatlistsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext()).
                        inflate(R.layout.singlerow,viewGroup,false);
                ChatsFragment.chatlistsViewHolder cViewHolder = new ChatsFragment.chatlistsViewHolder(view);
                return cViewHolder;
            }
        };
        chatlist_recview.setAdapter(cAdapter);
        cAdapter.startListening();
    }

    public  static  class chatlistsViewHolder extends RecyclerView.ViewHolder
    {
        TextView name_tv,username_tv;
        ImageView img1;
        public chatlistsViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            name_tv= itemView.findViewById(R.id.name_tv);
            username_tv= itemView.findViewById(R.id.username_tv);
            img1= itemView.findViewById(R.id.img1);
        }
    }
}