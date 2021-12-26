package com.gautam.socialfly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gautam.socialfly.Model.VCModel;
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

public class IncomingCall extends AppCompatActivity {
    String caller_img,caller_username,receiver_username,call_type;
    CircleImageView caller_dp;
    TextView caller_tv,ic_text;
    SharedPreferences sharedPreferences;
    ImageView accept_btn,decline_btn;
    VCModel model;
    Ringtone ringtone;
    DatabaseReference reference_response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone= RingtoneManager.getRingtone(getApplicationContext(),notification);
        ringtone.play();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        {
            ringtone.setLooping(false);
        }

        caller_tv=findViewById(R.id.ic_username);
        ic_text=findViewById(R.id.ic_text);
        caller_dp =findViewById(R.id.ic_img);
        accept_btn=findViewById(R.id.ic_accept);
        decline_btn=findViewById(R.id.ic_decline);

        call_type=getIntent().getExtras().getString("call_type");

        if(call_type.equals("audio call"))
        {
            ic_text.setText("Incoming Audio Call");
        }else{
            ic_text.setText("Incoming Video Call");
        }

        model = new VCModel();
        sharedPreferences =getSharedPreferences("socialflycredentials",MODE_PRIVATE);
        receiver_username = sharedPreferences.getString("user","nouser");
        caller_username=getIntent().getExtras().get("caller_username").toString();
        caller_tv.setText(caller_username);
        DatabaseReference usersRef= FirebaseDatabase.getInstance().getReference("users").child(caller_username);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                caller_img = snapshot.child("profilepic").getValue().toString();
                Glide.with(getApplicationContext())
                        .load(caller_img)
                        .into(caller_dp);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        accept_btn.setOnClickListener(v->{
            String response="yes";
            ringtone.stop();
            sendResponse(response);

        });
        decline_btn.setOnClickListener(v->{

            sendNotification noti = new sendNotification(caller_username,
                    getApplicationContext(),
                    IncomingCall.this,
                    "end_call",
                    "call",
                    ""+receiver_username);
            noti.noti_prep();
            String response="no";
            ringtone.stop();
            sendResponse(response);
            Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
            intent.putExtra("receiver_username",caller_username);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        });

    }

    private void sendResponse(String response)
    {
        if(response.equals("yes"))
        {
            model.setKey(caller_username+receiver_username);
            model.setResponse(response);

            reference_response = FirebaseDatabase.getInstance().getReference("vcref")
                    .child(caller_username).child(receiver_username);
            reference_response.child("res").setValue(model);
            joinmeeting();
            finish();
            Handler handler = new Handler();
            handler.postDelayed(() -> reference_response.child("res").removeValue(),3000);
        }
        else if(response.equals("no"))
        {
            reference_response = FirebaseDatabase.getInstance().getReference("vcref")
                    .child(caller_username).child(receiver_username);
            model.setKey(caller_username+receiver_username);
            model.setResponse(response);
            reference_response.child("res").setValue(model);

            Handler handler = new Handler();
            handler.postDelayed(() -> reference_response.child("res").removeValue(),4000);
        }
    }

    private void joinmeeting() {
        try {
            JitsiMeetConferenceOptions options;
            if(call_type.equals("audio call"))
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

            JitsiMeetActivity.launch(IncomingCall.this,options);


        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        ringtone.stop();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

}