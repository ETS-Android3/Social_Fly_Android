package com.gautam.socialfly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.gautam.socialfly.Model.UserModel;
import com.gautam.socialfly.adapter.MyAdapter;
import com.google.firebase.database.FirebaseDatabase;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class SearchUser extends AppCompatActivity {

    RecyclerView recview;
    MyAdapter adapter;
    Toolbar mToolbar;
    String currentuser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        SharedPreferences sharedPreferences = getSharedPreferences("socialflycredentials",MODE_PRIVATE);
        currentuser = sharedPreferences.getString("user","nouser");

        mToolbar = (Toolbar)findViewById(R.id.search_page_toolbar);
        setSupportActionBar(mToolbar);

        recview=findViewById(R.id.search_recview);
        recview.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<UserModel> options=
                new FirebaseRecyclerOptions.Builder<UserModel>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("users"), UserModel.class)
                .build();

        recview.setVisibility(View.INVISIBLE);
         adapter= new MyAdapter(options,currentuser);
         recview.setAdapter(adapter);

    }

    private class UserViewHolder extends RecyclerView.ViewHolder{
        TextView name_tv,username_tv;
        ImageView img1;
        public UserViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            name_tv= itemView.findViewById(R.id.name_tv);
            username_tv= itemView.findViewById(R.id.username_tv);
            img1= itemView.findViewById(R.id.img1);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.searchmenu,menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchview = (SearchView) item.getActionView();

        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                processSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                processSearch(newText);
                return false;
            }
        });

        return true;
    }

    private void processSearch(String keyword)
    {
        if(keyword.trim().equals(""))
        {
            recview.setVisibility(View.GONE);
        }
        else
        {
            recview.setVisibility(View.VISIBLE);
            FirebaseRecyclerOptions<UserModel> options=
                    new FirebaseRecyclerOptions.Builder<UserModel>()
                            .setQuery(FirebaseDatabase.getInstance().getReference().child("users")
                                    .orderByChild("username").startAt(keyword).endAt(keyword+"\uf8ff"), UserModel.class)
                            .build();

            adapter=new MyAdapter(options,currentuser);
            adapter.startListening();
            recview.setAdapter(adapter);
        }
    }
}