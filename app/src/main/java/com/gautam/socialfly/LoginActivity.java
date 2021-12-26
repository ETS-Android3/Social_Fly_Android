package com.gautam.socialfly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.TooManyListenersException;

public class LoginActivity extends AppCompatActivity {

    Button signupActivity,forgetPasswordBtn,signinBtn;
    TextInputLayout username, password;
    SharedPreferences sharedPreferences;
    ProgressBar pb;

    DatabaseReference UsersRef ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialization
        signupActivity=(Button) findViewById(R.id.signupActivity);
        forgetPasswordBtn=(Button) findViewById(R.id.forgetPasswordBtn);
        signinBtn=(Button) findViewById(R.id.signinBtn);

        username = (TextInputLayout) findViewById(R.id.username);
        password = (TextInputLayout) findViewById(R.id.password);

        pb = findViewById(R.id.pb);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");

        //Signup Activity
        signupActivity.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),SignupActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        //Sign-in by checking credentials
        signinBtn.setOnClickListener(v-> LoginUser());

        forgetPasswordBtn.setOnClickListener(v->{forgetPass();});

    }



    private void LoginUser() {



        if(!validateUsername() | !validatePassword()){
            //Validate Username and password according to Selection Criteria
            return ;
        }else{
            signinBtn.setVisibility(View.INVISIBLE);
            pb.setVisibility(View.VISIBLE);
            isUser();
        }
    }

    private void isUser() {
        String userEnteredUsername = username.getEditText().getText().toString().trim();
        String userEnteredPassword = password.getEditText().getText().toString().trim();

        //Converting Plain Password to Cipher Password
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            userEnteredPassword = Base64.getEncoder().encodeToString(userEnteredPassword.getBytes());
        }
        String finalUserEnteredPassword = userEnteredPassword;

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        Query checkUser = usersRef.orderByChild("username").equalTo(userEnteredUsername);

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    String passwordFromDB = snapshot.child(userEnteredUsername).child("password").getValue(String.class);

                    if(passwordFromDB.equals(finalUserEnteredPassword))
                    {
                        password.setError(null);
                        password.setErrorEnabled(false);

                        String name = snapshot.child(userEnteredUsername).child("name").getValue(String.class);
                        String email = snapshot.child(userEnteredUsername).child("email").getValue(String.class);
                        String phonenumber = snapshot.child(userEnteredUsername).child("phonenumber").getValue(String.class);
                        String profilepicURL = snapshot.child(userEnteredUsername).child("profilepic").getValue(String.class);

                        sharedPreferences = getSharedPreferences("socialflycredentials",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("user",userEnteredUsername);
                        editor.putString("name",name);
                        editor.putString("email",email);
                        editor.putString("phonenumber",phonenumber);
                        editor.putString("profilepicURL",profilepicURL);
                        editor.apply();


                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        System.out.println(deviceToken);
                        UsersRef.child(userEnteredUsername)
                                .child("tokenid").setValue(deviceToken);

                        Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    else
                    {
                        signinBtn.setVisibility(View.VISIBLE);
                        pb.setVisibility(View.INVISIBLE);
                        //if wrong password entered
                        password.setError("Wrong Password");
                        password.requestFocus();
                    }
                }
                else
                {
                    signinBtn.setVisibility(View.VISIBLE);
                    pb.setVisibility(View.INVISIBLE);
                    //if username is not in database
                    username.setError("Username doesn't Exists.");
                    username.requestFocus();
                    Toast.makeText(getApplicationContext(), "Username doesn't Exists", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }


    private Boolean validateUsername() {
        String val = username.getEditText().getText().toString();
        String noWhiteSpace = "\\A\\w{4,20}\\z";

        if (val.isEmpty()) {
            username.setError("Field cannot be empty");
            return false;
        } else if (val.length() >= 26) {
            username.setError("Username too long");
            return false;
        } else if (!val.matches(noWhiteSpace)) {
            username.setError("White Spaces are not allowed");
            return false;
        } else {
            username.setError(null);
            username.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword() {
        String val = password.getEditText().getText().toString();
        String passwordVal = "^" +
                //"(?=.*[0-9])" +         //at least 1 digit
                //"(?=.*[a-z])" +         //at least 1 lower case letter
                //"(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[@#$%^&+=])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{4,}" +               //at least 4 characters
                "$";

        if (val.isEmpty()) {
            password.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(passwordVal)) {
            password.setError("Password is too weak");
            return false;
        } else {
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }

    private void forgetPass() {

        String checkUsername = username.getEditText().getText().toString().trim();

        if(checkUsername.equals("")){
            Toast.makeText(this, "Enter Username", Toast.LENGTH_SHORT).show();
            return;
        }

        Query checkUser = FirebaseDatabase.getInstance()
                .getReference("users").orderByChild("username")
                .equalTo(checkUsername);

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    String phonenumber = snapshot.child(checkUsername).child("phonenumber").getValue().toString();
                    Intent intent = new Intent(getApplicationContext(),ForgetPassword.class);
                    intent.putExtra("phonenumber",phonenumber);
                    intent.putExtra("username",checkUsername);
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "No user found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}