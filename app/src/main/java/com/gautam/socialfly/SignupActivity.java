package com.gautam.socialfly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import org.jetbrains.annotations.NotNull;

public class SignupActivity extends AppCompatActivity{

    String name, email,username, phonenumber,password;
    TextInputLayout user_fullname,user_username,user_email, user_phonenumber,user_password,user_confirmpassword;
    Button signupBtn;
    TextView signinActivity;
    CountryCodePicker ccp;
    DatabaseReference RootRef;

    String usernameCheck;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Initialization
        signupBtn =findViewById(R.id.signupBtn);
        signinActivity=findViewById(R.id.signinActivity);
        ccp=(CountryCodePicker) findViewById(R.id.ccp) ;

        user_fullname=(TextInputLayout)findViewById(R.id.user_fullname);
        user_username=(TextInputLayout)findViewById(R.id.user_username);
        user_email=(TextInputLayout)findViewById(R.id.user_email);
        user_phonenumber=(TextInputLayout)findViewById(R.id.user_phone);
        user_password=(TextInputLayout)findViewById(R.id.user_password);
        user_confirmpassword=(TextInputLayout)findViewById(R.id.user_confirmpassword);
        signupBtn.setOnClickListener(v-> RegisterUser());

        RootRef = FirebaseDatabase.getInstance().getReference();

        //Toggle to Login Activity
        signinActivity.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
        });
    }

    private void RegisterUser() {

        signupBtn.setVisibility(View.GONE);

        username=user_username.getEditText().getText().toString().trim().toLowerCase();
        Log.d("username :",username);
        String countrycode = ccp.getSelectedCountryCode();
        name=user_fullname.getEditText().getText().toString();
        username=user_username.getEditText().getText().toString();
        email=user_email.getEditText().getText().toString();
        phonenumber= "+"+countrycode+user_phonenumber.getEditText().getText().toString();
        password=user_password.getEditText().getText().toString();


        if(!validateName() |!validatePassword() |!validateEmail()|!validatePhoneNo()|!validateUsername())
        {
            signupBtn.setVisibility(View.VISIBLE);
            //Validation according to Selection Criteria
            return;
        }

        Query checkUsername = RootRef.child("users").orderByChild("username").equalTo(username);
        Query checkPhoneno = RootRef.child("users").orderByChild("phonenumber").equalTo(phonenumber);

        checkUsername.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    signupBtn.setVisibility(View.VISIBLE);
                    user_username.setError("Username Already in use");
                    Toast.makeText(SignupActivity.this, "Username already in use", Toast.LENGTH_SHORT).show();
                }
                else
                    {
                        checkPhoneno.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {
                                    signupBtn.setVisibility(View.VISIBLE);
                                    user_phonenumber.setError("Phone number already in use");
                                    Toast.makeText(SignupActivity.this, "Phone number already in use", Toast.LENGTH_SHORT).show();

                                }else{

                                    //Go-TO Otp Verification.Passing all Values

                                    Intent intent = new Intent(getApplicationContext(), OtpVerification.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("name", name);
                                    intent.putExtra("username", username);
                                    intent.putExtra("email", email);
                                    intent.putExtra("phonenumber",phonenumber );
                                    intent.putExtra("password", password);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                    }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


    }

    public void LoginActivity(View view) {
        Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    private Boolean validateName() {
        String val = user_fullname.getEditText().getText().toString();

        if (val.isEmpty()) {
            user_fullname.setError("Field cannot be empty");
            return false;
        }
        else {
            user_fullname.setError(null);
            user_fullname.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateUsername() {
        String val = user_username.getEditText().getText().toString();
        String noWhiteSpace = "\\A\\w{4,20}\\z";

        if (val.isEmpty()) {
            user_username.setError("Field cannot be empty");
            return false;
        } else if (val.length() >= 25) {
            user_username.setError("Username too long");
            return false;
        } else if (!val.matches(noWhiteSpace)) {
            user_username.setError("White Spaces are not allowed");
            return false;
        }else{
            user_username.setError(null);
            user_username.setErrorEnabled(false);
            return true;
        }


    }

    private Boolean validateEmail() {
        String val = user_email.getEditText().getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (val.isEmpty()) {
            user_email.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(emailPattern)) {
            user_email.setError("Invalid email address");
            return false;
        } else {
            user_email.setError(null);
            user_email.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePhoneNo() {
        String val = user_phonenumber.getEditText().getText().toString();

        if (val.isEmpty()) {
            user_phonenumber.setError("Field cannot be empty");
            return false;
        } else {
            user_phonenumber.setError(null);
            user_phonenumber.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword() {
        String val = user_password.getEditText().getText().toString();
        String confirmpassword = user_confirmpassword.getEditText().getText().toString();
        String passwordVal = "^" +
                //"(?=.*[0-9])" +         //at least 1 digit
                //"(?=.*[a-z])" +         //at least 1 lower case letter
                //"(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[@#$%^&+=])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{8,}" +               //at least 8 characters
                "$";

        if (val.isEmpty()) {
            user_password.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(passwordVal)) {
            user_password.setError("Password should be minimum 8 characters with combination of atleast a Uppercase, Lowercase , Symbols and numbers");
            return false;
        } else {
            user_password.setError(null);
            user_password.setErrorEnabled(false);
            if(val.equals(confirmpassword)){
                user_confirmpassword.setError(null);
                user_confirmpassword.setErrorEnabled(false);
                return true;
            }
            else {
                user_confirmpassword.setError("Please make sure your passwords match");
                return false;
            }

        }
    }
}