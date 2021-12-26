package com.gautam.socialfly.notify;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.gautam.socialfly.ChatActivity;
import com.gautam.socialfly.HomeActivity;
import com.gautam.socialfly.IncomingCall;
import com.gautam.socialfly.R;
import com.gautam.socialfly.ShowProfile;
import com.gautam.socialfly.ViewProfile;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService
{
    NotificationManager mNotificationManager;
    String title,body;

    //FCMNotificationsSender get = new FCMNotificationsSender();

    @Override
    public void onMessageReceived(@NonNull @NotNull RemoteMessage remoteMessage)
    {
        super.onMessageReceived(remoteMessage);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone= RingtoneManager.getRingtone(getApplicationContext(),notification);
        ringtone.play();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        {
            ringtone.setLooping(false);
        }

        //vibrate
        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100,300,300,300};
        //vibrator.vibrate(pattern,-1);

        int resourceImage = getResources().getIdentifier(remoteMessage.getNotification().getIcon(),"drawable",getPackageName());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"CHANNEL_ID");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            builder.setSmallIcon(resourceImage);
        }
        else
        {
            builder.setSmallIcon(resourceImage);
        }

        body=remoteMessage.getNotification().getBody();
        title=remoteMessage.getNotification().getTitle();
        if(body.equals("audio call") || body.equals("video call"))
        {
            startCall();
        }else if(body.equals("end_call"))
        {
            endCall();
        }
        else
        {
            Intent resultIntent;
            resultIntent = new Intent(this, ViewProfile.class);
            resultIntent.putExtra("receiver_username",title);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentTitle(remoteMessage.getNotification().getTitle());
            builder.setContentText(remoteMessage.getNotification().getBody());
            builder.setContentIntent(pendingIntent);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()));
            builder.setAutoCancel(true);
            builder.setSmallIcon(R.drawable.notification_icon);
            builder.setSound(notification);
            builder.setVibrate(pattern);
            builder.setPriority(Notification.PRIORITY_MAX);



            mNotificationManager =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                String channelId = "MY_NOTIFICATION_CHANNEL_ID";
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_HIGH
                );

                mNotificationManager.createNotificationChannel(channel);
                builder.setChannelId(channelId);
            }

            mNotificationManager.notify(new Random().nextInt(), builder.build());
        }


    }

    private void endCall()
    {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void startCall()
    {
        Intent intent = new Intent(getApplicationContext(), IncomingCall.class);
        intent.putExtra("caller_username",title);
        intent.putExtra("call_type",body);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
