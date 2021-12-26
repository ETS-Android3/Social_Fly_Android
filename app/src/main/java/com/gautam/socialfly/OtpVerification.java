package com.gautam.socialfly;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class OtpVerification extends AppCompatActivity {

    TextView phoneno;
    EditText Otp;
    Button submit;
    String name,username,email,phonenumber,password,codeByServer,codebyUser;

    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        //Initialization
        phoneno=(TextView) findViewById(R.id.phonenumber);
        phoneno.setText(phoneno.getText()+(getIntent().getStringExtra("phonenumber")));
        submit = (Button) findViewById(R.id.verify);
        Otp = (EditText)findViewById(R.id.otp);

        mAuth = FirebaseAuth.getInstance();

        //Getting Extra Values passed from SignUp Activity
        name=getIntent().getStringExtra("name");
        username=getIntent().getStringExtra("username");
        email=getIntent().getStringExtra("email");
        phonenumber=getIntent().getStringExtra("phonenumber");
        password=getIntent().getStringExtra("password");

        //Calling OTP send
        otpSend();
        submit.setOnClickListener(v-> verify());
    }

    private void otpSend() {

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Toast.makeText(getApplicationContext(), "OTP is successfully send.", Toast.LENGTH_SHORT).show();
                codeByServer=verificationId;
            }
        };

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phonenumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void verify() {

        if(Otp.getText().toString().trim().equals(""))
            Toast.makeText(this, "Enter Code", Toast.LENGTH_SHORT).show();
        else {
            codebyUser = Otp.getText().toString();

            //Checking Verification Code
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeByServer, codebyUser);
            FirebaseAuth
                    .getInstance()
                    .signInWithCredential(credential)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                registerUser();
                                Toast.makeText(getApplicationContext(), "Please Login to Continue !!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {

                                Toast.makeText(getApplicationContext(), "OTP is not Valid!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void registerUser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            password = Base64.getEncoder().encodeToString(password.getBytes());
        }

        Map<String,Object> details = new HashMap<>();
        details.put("name",name);
        details.put("username",username);
        details.put("email",email);
        details.put("phonenumber",phonenumber);
        details.put("password",password);
        details.put("profilepic","https://firebasestorage.googleapis.com/v0/b/social-fly-gamnk.appspot.com/o/avatar.png?alt=media&token=606dcfe0-cfc1-4b6f-bbd3-7c846bac44aa");


        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                usersRef.child(username).setValue(details);
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });
    }
}