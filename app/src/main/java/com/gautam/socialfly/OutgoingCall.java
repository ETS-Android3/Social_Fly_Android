package com.gautam.socialfly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gautam.socialfly.notify.sendNotification;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class OutgoingCall extends AppCompatActivity {

    CircleImageView receiver_pic;
    TextView receiver_tv,og_text;
    ImageView decline_btn;
    String receiver_username, receiver_imgurl,receiver_token,caller_username,call_type;
    DatabaseReference UsersRef;
    DatabaseReference reference_response;
    DatabaseReference reference_call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_call);

        receiver_pic = findViewById(R.id.og_img);
        receiver_tv = findViewById(R.id.og_username);
        decline_btn=findViewById(R.id.og_decline);

        receiver_username=getIntent().getExtras().getString("receiver_username");
        caller_username=getIntent().getExtras().getString("caller_username");
        call_type=getIntent().getExtras().getString("call_type");

        og_text=findViewById(R.id.og_text);
        if(call_type.equals("audio"))
        {
            og_text.setText("Outgoing Audio Call");
        }else{
            og_text.setText("Outgoing Video Call");
        }


        receiver_tv.setText(receiver_username);

        decline_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNotification noti = new sendNotification(receiver_username,
                        getApplicationContext(),
                        OutgoingCall.this,
                        "end_call",
                        "call",
                        ""+caller_username);
                noti.noti_prep();

                Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
                intent.putExtra("receiver_username",receiver_username);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        });

        UsersRef = FirebaseDatabase.getInstance().getReference("users");
        UsersRef.child(receiver_username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {

                    receiver_imgurl = snapshot.child("profilepic").getValue().toString();
                    Glide.with(getApplicationContext())
                            .load(receiver_imgurl)
                            .into(receiver_pic);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    UsersRef.child(receiver_username).child("tokenid").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if(snapshot.exists())
                    {
                        receiver_token= snapshot.getValue().toString();
                    }else{
                        Toast.makeText(OutgoingCall.this, "Couldn't place call.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

        sendCallInvitation();
        checkResponse();
    }

    private void checkResponse()
    {
        reference_call=FirebaseDatabase.getInstance().getReference("vcref")
                .child(caller_username).child(receiver_username);

        Handler hander = new Handler();
        hander.postDelayed(new Runnable() {
            @Override
            public void run() {
                reference_call.child("res").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            String key = snapshot.child("key").getValue().toString();
                            String response=snapshot.child("response").getValue().toString();

                            if(response.equals("yes"))
                            {
                                joinMeeting(key);
                                Toast.makeText(OutgoingCall.this, "Call Accepted", Toast.LENGTH_SHORT).show();
                                finish();

                            }else
                            {
                                Toast.makeText(OutgoingCall.this, "Call Denied", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
                                intent.putExtra("receiver_username",receiver_username);
                                finish();
                            }
                        }else
                        {
                            //Toast.makeText(OutgoingCall.this, "Not Responding", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        },7000);
    }


    private void joinMeeting(String key)
    {
        try {
            JitsiMeetConferenceOptions options;
            if(call_type.equals("audio"))
            {
                options = new JitsiMeetConferenceOptions.Builder()
                        .setServerURL(new URL("https://meet.jit.si"))
                        .setRoom(caller_username+receiver_username)
                        .setWelcomePageEnabled(false)
                        .setAudioOnly(true)
                        .setVideoMuted(true)
                        .build();
            }else
            {
                options = new JitsiMeetConferenceOptions.Builder()
                        .setServerURL(new URL("https://meet.jit.si"))
                        .setRoom(caller_username+receiver_username)
                        .setWelcomePageEnabled(false)
                        .build();
            }

            JitsiMeetActivity.launch(OutgoingCall.this,options);


        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendCallInvitation()
    {
      // FirebaseDatabase.getInstance().getReference("vcref")
        //        .child(caller_username).child(receiver_username).child("res").child("status").setValue("calling");
        sendNotification noti = new sendNotification(receiver_username,
                getApplicationContext(),
                OutgoingCall.this,
                ""+call_type+" call",
                "call",
                ""+caller_username);
        noti.noti_prep();
    }
}