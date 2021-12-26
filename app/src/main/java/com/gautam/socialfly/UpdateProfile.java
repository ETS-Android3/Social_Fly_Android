package com.gautam.socialfly;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

import static android.content.Intent.ACTION_PICK;

public class UpdateProfile extends AppCompatActivity {


    TextView username ;
    EditText name , phonenumber, email;
    ImageView profilePic,saveprofile;
    SharedPreferences sharedPreferences;
    String imgURL,user;
    Uri filepath;
    Bitmap bitmap;
    DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        username= findViewById(R.id.show_username);
        name= findViewById(R.id.name);
        name= findViewById(R.id.name);
        phonenumber= findViewById(R.id.phoneno);
        //phone number cant be edited
        phonenumber.setOnClickListener(v -> Toast.makeText(getApplicationContext(), "Phone Number can't be Updated", Toast.LENGTH_SHORT).show());
        email=findViewById(R.id.mailid);
        profilePic=findViewById(R.id.circleImageView);
        saveprofile = findViewById(R.id.saveprofile);
        saveprofile.setOnClickListener(v-> updateDB());

        sharedPreferences =getSharedPreferences("socialflycredentials",MODE_PRIVATE);
        user = sharedPreferences.getString("user","nouser");
        username.setText(user);
        name.setText(sharedPreferences.getString("name",""));
        phonenumber.setText(sharedPreferences.getString("phonenumber",""));
        email.setText(sharedPreferences.getString("email",""));
        imgURL = sharedPreferences.getString("profilepicURL","");
        Glide.with(getApplicationContext())
                .load(imgURL).centerCrop()
                .into(profilePic);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(UpdateProfile.this)
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                Intent intent=new Intent(ACTION_PICK);
                                intent.setType("image/*");
                                startActivityForResult(Intent.createChooser(intent,"Select Your Social Fly Profile"),1);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });
    }

    private void updateDB() {

        if(!validateEmail()|!validateName()){
            return;
        }

        UsersRef.child(user).child("name").setValue(name.getText().toString());
        UsersRef.child(user).child("phonenumber").setValue(phonenumber.getText().toString());
        UsersRef.child(user).child("email").setValue(email.getText().toString());

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Query checkUser = UsersRef.orderByChild("username").equalTo(user);

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {

                        String name = snapshot.child(user).child("name").getValue(String.class);
                        String email = snapshot.child(user).child("email").getValue(String.class);
                        String phonenumber = snapshot.child(user).child("phonenumber").getValue(String.class);
                        String profilepicURL = snapshot.child(user).child("profilepic").getValue(String.class);

                        sharedPreferences = getSharedPreferences("socialflycredentials",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("user",user);
                        editor.putString("name",name);
                        editor.putString("email",email);
                        editor.putString("phonenumber",phonenumber);
                        editor.putString("profilepicURL",profilepicURL);
                        editor.apply();


                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        System.out.println(deviceToken);
                        UsersRef.child(user)
                                .child("TokenID").setValue(deviceToken);

                        Intent intent = new Intent(getApplicationContext(),ViewProfile.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                    Toast.makeText(UpdateProfile.this, "Profile Updated", Toast.LENGTH_SHORT).show();


                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }


    private boolean validateEmail() {
        String val = email.getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (val.isEmpty()) {
            email.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(emailPattern)) {
            email.setError("Invalid email address");
            return false;
        } else {
            email.setError(null);
            return true;
        }
    }
    private boolean validateName() {
        String val = name.getText().toString();

        if (val.isEmpty()) {
            name.setError("Field cannot be empty");
            return false;
        }
        else {
            name.setError(null);
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {

        if(requestCode==1 && resultCode==RESULT_OK){

            filepath=data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(filepath);
                bitmap=BitmapFactory.decodeStream(inputStream);
                profilePic.setImageBitmap(bitmap);
            }catch(Exception e){}

            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("Wait");
            dialog.show();

            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReference().child("Profile Images").child(user+"profilepic");

            storageReference.putFile(filepath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri)
                                {
                                    String downloadUrl;
                                    DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference("users");
                                    downloadUrl = uri.toString();
                                    UsersRef.child(user).child("profilepic").setValue(downloadUrl);
                                    dialog.dismiss();
                                }
                            });
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull @NotNull UploadTask.TaskSnapshot snapshot) {
                    double p = (100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                    dialog.setMessage("Uploading... "+(int) p +" %");
                }
            });
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}