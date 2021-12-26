package com.gautam.socialfly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.gautam.socialfly.adapter.TabAccessorAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

public class HomeActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabAccessorAdapter mTabAccessorAdapter;
    String currentuser;
    DatabaseReference UsersRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sharedPreferences = getSharedPreferences("socialflycredentials",MODE_PRIVATE);
        currentuser = sharedPreferences.getString("user","nouser");

        mToolbar = findViewById(R.id.home_page_toolbar);
        setSupportActionBar(mToolbar);

        mViewPager = findViewById(R.id.homeViewPager);
        mTabAccessorAdapter= new TabAccessorAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabAccessorAdapter);

        mTabLayout = findViewById(R.id.homeTabLayout);
        mTabLayout.setupWithViewPager(mViewPager);

        UsersRef = FirebaseDatabase.getInstance().getReference("users");

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<String> task) {
                        if(!task.isSuccessful())
                        {
                            return;
                        }
                        String token = task.getResult();
                        UsersRef.child(currentuser).child("tokenid").setValue(token);
                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.home_options,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.find_friends)
        {
            Intent intent = new Intent(getApplicationContext(),SearchUser.class);
            startActivity(intent);
        }
        if(item.getItemId()==R.id.settings)
        {
            Intent intent = new Intent(getApplicationContext(),ViewProfile.class);
            startActivity(intent);
        }
        if(item.getItemId()==R.id.log_out)
        {
            Toast.makeText(getApplicationContext(), "Logging Out....", Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        return true;
    }

}