package com.gautam.socialfly;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ViewImage extends AppCompatActivity {

    ImageView view_image;
    String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        view_image = findViewById(R.id.view_image);

        url = getIntent().getExtras().get("message_image_url").toString();

        Glide.with(getApplicationContext())
                .load(url)
                .into(view_image);
    }
}