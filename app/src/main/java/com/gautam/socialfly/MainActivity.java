
package com.gautam.socialfly;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    Animation topAnim,bottomAnim;
    ImageView img;
    TextView applogotext,tagline;

    DatabaseReference UsersRef;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialization
        topAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.bottom_animation);

        img=findViewById(R.id.logo);
        applogotext=findViewById(R.id.applogotext);
        tagline=findViewById(R.id.tagline);

        //Setting Animation
        img.setAnimation(topAnim);
        applogotext.setAnimation(bottomAnim);
        tagline.setAnimation(bottomAnim);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");

        //Handler to move-in next activity in 5 seconds
        new Handler().postDelayed(() -> {

            //Checking if user is already logged in locally
            sharedPreferences =getSharedPreferences("socialflycredentials",MODE_PRIVATE);
            String user = sharedPreferences.getString("user","nouser");

            if(user.equals("nouser")){
                //if not logged-in. Go to loginActivity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
            else{

                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                System.out.println(deviceToken);
                UsersRef.child(user)
                        .child("TokenID").setValue(deviceToken);
                //if logged in go to Home activity
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                finish();
            }
        },3000);
    }
}
