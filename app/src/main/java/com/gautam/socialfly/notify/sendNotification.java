package com.gautam.socialfly.notify;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.gautam.socialfly.ChatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class sendNotification
{
    String receiver;
    String userToken;
    Context mContext;
    Activity mActivity;
    String message ;
    String type ;
    String sender ;

    public sendNotification(String receiver, Context mContext, Activity mActivity, String message,String type,String sender) {
        this.receiver = receiver;
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.message = message;
        this.type=type;
        this.sender=sender;
    }

    public void noti_prep()
    {
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(receiver)
                .child("tokenid")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                        {
                            userToken = snapshot.getValue(String.class);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            FCMNotificationsSender fcmNotificationsSender =
                    new FCMNotificationsSender(userToken,
                            receiver,
                            message,
                            mContext,
                            mActivity,
                            type,
                            sender);

            fcmNotificationsSender.SendNotifications();
        },1000);
    }

}
