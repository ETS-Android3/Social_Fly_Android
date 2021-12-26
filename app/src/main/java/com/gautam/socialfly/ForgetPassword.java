package com.gautam.socialfly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class ForgetPassword extends AppCompatActivity {

    private TextView phone_text;
    private String phonenumber,username,codeByServer,codebyUser,newpassword,confirmpassword,finalpassword;

    private Button fp_verifycode,fp_resetpassword;
    private EditText fp_verificationcode;

    private LinearLayout codelayout,passwordlayout;

    private TextInputLayout fp_newpassword,fp_confirmpassword;

    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private DatabaseReference UsersRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        mAuth = FirebaseAuth.getInstance();

        phonenumber = getIntent().getExtras().getString("phonenumber");
        username = getIntent().getExtras().getString("username");

        phone_text = findViewById(R.id.phone_text);
        phone_text.setText(phone_text.getText()+phonenumber);

        codelayout = findViewById(R.id.code_layout);
        passwordlayout = findViewById(R.id.password_layout);

        fp_verifycode = findViewById(R.id.fp_verifycodebtn);
        fp_resetpassword = findViewById(R.id.fp_resetpass);

        fp_verificationcode = findViewById(R.id.fp_verifycodetext);

        fp_newpassword = findViewById(R.id.fp_newpassword);
        fp_confirmpassword = findViewById(R.id.fp_confirmpassword);

        UsersRef = FirebaseDatabase.getInstance().getReference("users");

        otpSend();
        fp_verifycode.setOnClickListener(v->{checkOtp();});
        fp_resetpassword.setOnClickListener(v->{resetPassword();});
    }

    private void resetPassword() {

        newpassword = fp_newpassword.getEditText().getText().toString();
        confirmpassword = fp_confirmpassword.getEditText().getText().toString();
        if(!validatePassword())
        {
            return;
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            finalpassword = Base64.getEncoder().encodeToString(newpassword.getBytes());
        }

        UsersRef.child(username).child("password").setValue(finalpassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(), "Password Updated !!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else{
                    Toast.makeText(ForgetPassword.this, "Error Occured", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void checkOtp() {
        if(fp_verificationcode.getText().toString().trim().equals(""))
            Toast.makeText(this, "Enter Code", Toast.LENGTH_SHORT).show();
        else {
            codebyUser = fp_verificationcode.getText().toString();

            //Checking Verification Code
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeByServer, codebyUser);
            FirebaseAuth
                    .getInstance()
                    .signInWithCredential(credential)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @com.google.firebase.database.annotations.NotNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                codelayout.setVisibility(View.GONE);
                                passwordlayout.setVisibility(View.VISIBLE);
                            } else {

                                Toast.makeText(getApplicationContext(), "Code is not Valid!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
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
                Toast.makeText(getApplicationContext(), "Code is successfully send.", Toast.LENGTH_SHORT).show();
                codeByServer=verificationId;
            }
        };

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phonenumber)
                        .setTimeout(120L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private Boolean validatePassword() {
        String val = newpassword;
        String val_ = confirmpassword;
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
            fp_newpassword.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(passwordVal)) {
            fp_newpassword.setError("Password is too weak");
            return false;
        } else {
            fp_newpassword.setError(null);
            fp_newpassword.setErrorEnabled(false);
            if(val.equals(val_)){
                fp_confirmpassword.setError(null);
                fp_confirmpassword.setErrorEnabled(false);
                return true;
            }
            else {
                fp_confirmpassword.setError("Please make sure your passwords match");
                return false;
            }

        }
    }
}