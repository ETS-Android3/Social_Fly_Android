package com.gautam.socialfly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.gautam.socialfly.notify.FCMNotificationsSender;
import com.gautam.socialfly.notify.sendNotification;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowProfile extends AppCompatActivity {

    TextView t1,t2;
    String img_url,currentuser,current_state,receiver,userToken;
    CircleImageView show_dp;
    Button sendmsg_req,cancelmsg_req;

    private DatabaseReference UserRef, ChatRequestRef, ContactsRef, NotificationsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);



        t1=findViewById(R.id.show_username);
        t2=findViewById(R.id.show_name);
        show_dp=findViewById(R.id.show_dp);
        sendmsg_req=findViewById(R.id.sendmsg_req);
        cancelmsg_req=findViewById(R.id.cancelmsg_req);
        cancelmsg_req.setVisibility(View.GONE);

        t1.setText(getIntent().getExtras().get("show_username").toString());

        UserRef = FirebaseDatabase.getInstance().getReference("users").child(t1.getText().toString());
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    t2.setText(snapshot.child("name").getValue().toString());
                    img_url=snapshot.child("profilepic").getValue().toString();
                    Glide.with(getApplicationContext()).load(img_url).into(show_dp);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        SharedPreferences sharedPreferences =getSharedPreferences("socialflycredentials",MODE_PRIVATE);
        currentuser = sharedPreferences.getString("user","nouser");
        receiver =getIntent().getExtras().get("show_username").toString();

        current_state="new";

        UserRef = FirebaseDatabase.getInstance().getReference().child("users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        ManageChatRequests();
        sendmsg_req.setOnClickListener(v->
        {
            if (current_state.equals("new"))
            {
                sendChatRequest();
            }
            if (current_state.equals("request_sent"))
            {
                cancelChatRequest();
            }
            if (current_state.equals("request_received"))
            {
                acceptChatRequest();
            }
            if (current_state.equals("friends"))
            {
                Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
                intent.putExtra("receiver_username",receiver);
                startActivity(intent);
            }
        });

        cancelmsg_req.setOnClickListener(v -> {
            if(current_state.equals("request_received"))
            {
                cancelChatRequest();
            }
            if(current_state.equals("friends"))
            {
                deleteRequest();
            }
        });
    }

    private void ManageChatRequests() {
        String senderUserID = currentuser;
        String receiverUserID=receiver;
        ChatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild(receiverUserID))
                        {
                            String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if (request_type.equals("sent"))
                            {
                                current_state = "request_sent";
                                sendmsg_req.setText("Cancel Request");
                            }
                            else if (request_type.equals("received"))
                            {
                                current_state = "request_received";
                                sendmsg_req.setText("Accept Request");

                                cancelmsg_req.setVisibility(View.GONE);
                                cancelmsg_req.setEnabled(true);

                            }
                        }
                        else
                        {
                            ContactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot)
                                        {
                                            if (dataSnapshot.hasChild(receiverUserID))
                                            {
                                                current_state = "friends";
                                                sendmsg_req.setText("send message");
                                                cancelmsg_req.setText("remove friend");
                                                cancelmsg_req.setVisibility(View.VISIBLE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void sendChatRequest()
    {
        ChatRequestRef.child(currentuser).child(receiver)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ChatRequestRef.child(receiver).child(currentuser)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                sendNotification sendNoti =new  sendNotification(receiver,getApplicationContext(),
                                                        ShowProfile.this,currentuser+" sent you request.","request",currentuser);
                                                sendNoti.noti_prep();
                                                sendmsg_req.setEnabled(true);
                                                current_state="request_sent";
                                                sendmsg_req.setText("cancel request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptChatRequest()
    {
        String senderUserID = currentuser;
        String receiverUserID=receiver;
        ContactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                ChatRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    ChatRequestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    sendNotification sendNoti =new  sendNotification(receiverUserID,getApplicationContext(),
                                                                                            ShowProfile.this,currentuser+" accepted your request.","request",currentuser);
                                                                                    sendNoti.noti_prep();
                                                                                    sendmsg_req.setEnabled(true);
                                                                                    current_state = "friends";
                                                                                    sendmsg_req.setText("send message");

                                                                                    cancelmsg_req.setVisibility(View.VISIBLE);
                                                                                    cancelmsg_req.setText("remove friend");
                                                                                    cancelmsg_req.setEnabled(true);
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

    private void deleteRequest() {

        ContactsRef.child(currentuser).child(receiver)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ContactsRef.child(receiver).child(currentuser)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                sendmsg_req.setEnabled(true);
                                                current_state="new";
                                                sendmsg_req.setText("Send request");
                                                cancelmsg_req.setVisibility(View.GONE);
                                                Toast.makeText(ShowProfile.this, "Successfull", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
    private void cancelChatRequest() {
        ChatRequestRef.child(currentuser).child(receiver)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ChatRequestRef.child(receiver).child(currentuser)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                sendmsg_req.setEnabled(true);
                                                current_state="new";
                                                sendmsg_req.setText("Send request");
                                                cancelmsg_req.setEnabled(false);
                                                cancelmsg_req.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

}