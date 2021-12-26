package com.gautam.socialfly.notify;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FCMNotificationsSender
{
    String userFCMToken;
    String title;
    String body;
    Context mContext;
    Activity mActivity;
    String type ;
    String sender ;

    private RequestQueue requestQueue;
    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private final String fcmServerKey="AAAAjf3a4Jo:APA91bFVZYxg9b62NqqoZ_KdDQh68hbYO6l9813n4jX4_L3t0g9Jn515qaEZ7TwifT10OPkBwXsEYJ5lnSAroG1UbxOki0Q8xc7KKjJHDvOODK1HxuhzIpUcEkU8aj_HjLWeAkiuFKiA";

    public FCMNotificationsSender(String userFCMToken, String title, String body, Context mContext, Activity mActivity,String type,String sender)
    {
        this.userFCMToken = userFCMToken;
        this.title = title;
        this.body = body;
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.type=type;
        this.sender=sender;
    }


    public void SendNotifications()
    {
        requestQueue = Volley.newRequestQueue(mActivity);
        JSONObject mainObj = new JSONObject();

        try
        {
            mainObj.put("to",userFCMToken);

            JSONObject notiObject = new JSONObject();
            notiObject.put("title",sender);
            notiObject.put("body",body);
            notiObject.put("icon","notification_icon");
            notiObject.put("type",type);
            notiObject.put("sender",sender);


            mainObj.put("notification",notiObject);
        }
        catch (Exception e)
        {

        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(postUrl, mainObj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map <String,String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization","key="+fcmServerKey);
                return headers;
            }
        };

        Volley.newRequestQueue(mContext).add(jsonObjectRequest);
    }
}
