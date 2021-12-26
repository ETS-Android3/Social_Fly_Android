package com.gautam.socialfly;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class ViewProfile extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    TextView name , username, phonenumber, email, address, marsts;
    String imgURL;
    ImageView profilePic,back,editprofile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);


        username= findViewById(R.id.show_username);name= findViewById(R.id.name);
        name= findViewById(R.id.name);
        phonenumber= findViewById(R.id.phoneno);
        email=findViewById(R.id.mailid);
        profilePic=findViewById(R.id.circleImageView);

        back = findViewById(R.id.back);
        back.setOnClickListener(v->finish());

        sharedPreferences =getSharedPreferences("socialflycredentials",MODE_PRIVATE);
        String user = sharedPreferences.getString("user","nouser");
        username.setText(user);
        name.setText(sharedPreferences.getString("name",""));
        phonenumber.setText(sharedPreferences.getString("phonenumber",""));
        email.setText(sharedPreferences.getString("email",""));
        imgURL = sharedPreferences.getString("profilepicURL","");

        Glide.with(getApplicationContext())
                .load(imgURL).centerCrop()
                .into(profilePic);



        editprofile=findViewById(R.id.editprofile);
        editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),UpdateProfile.class);
                startActivity(intent);
            }
        });
    }

    public void logout(View view) {
        Toast.makeText(getApplicationContext(), "Logging Out....", Toast.LENGTH_SHORT).show();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}