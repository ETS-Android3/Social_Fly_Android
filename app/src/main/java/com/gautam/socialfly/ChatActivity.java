package com.gautam.socialfly;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.gautam.socialfly.Fragments.ContactsFragment;
import com.gautam.socialfly.Model.ChatModel;
import com.gautam.socialfly.Model.UserModel;
import com.gautam.socialfly.adapter.ChatAdapter;
import com.gautam.socialfly.notify.sendNotification;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class ChatActivity extends AppCompatActivity
{
    TextView chatusername;
    CircleImageView chatdp;
    String img_url,sender,receiver;
    ImageView back,vc_btn,ac_btn;
    SharedPreferences sharedPreferences;
    EditText chat_text;
    ImageButton send_btn,attach_file;
    RecyclerView chat_recview;

    DatabaseReference RootRef,Usersref,Chatsref,ContactsRef,ChatlistRef,UserRef;
    ChatAdapter chatAdapter;
    List<ChatModel> chats;

    RelativeLayout chatsendlayout;
    LinearLayout call_linear;

    ProgressDialog loadingbar;
    String fileUrl,file_type="";
    StorageTask uploadTask;
    Uri fileUri;
    ValueEventListener seen_reference;
    StorageReference storageReference;
    ItemTouchHelper.SimpleCallback  simpleCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatsendlayout= (RelativeLayout)findViewById(R.id.chatsendlayout);
        call_linear= (LinearLayout)findViewById(R.id.call_linear);
        chatusername=findViewById(R.id.chatusername);
        chatdp=findViewById(R.id.chatdp);
        chat_text=findViewById(R.id.chat_text);
        send_btn=findViewById(R.id.chatsend_btn);
        attach_file=findViewById(R.id.attach_file);
        chat_recview=findViewById(R.id.chat_recview);
        chat_recview.setHasFixedSize(true);

        loadingbar = new ProgressDialog(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);

        chat_recview.setLayoutManager(linearLayoutManager);

        chatusername.setText(getIntent().getExtras().get("receiver_username").toString());
        receiver = chatusername.getText().toString();

        UserRef = FirebaseDatabase.getInstance().getReference("users").child(receiver);
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {

                    img_url=snapshot.child("profilepic").getValue().toString();
                    Glide.with(getApplicationContext())
                            .load(img_url).centerCrop()
                            .into(chatdp);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        sharedPreferences =getSharedPreferences("socialflycredentials",MODE_PRIVATE);
        sender = sharedPreferences.getString("user","nouser");

        back=findViewById(R.id.backbtn);
        vc_btn=findViewById(R.id.vc_btn);
        ac_btn=findViewById(R.id.ac_btn);

        vc_btn.setOnClickListener(v->
        {
            Intent intent = new Intent(ChatActivity.this,OutgoingCall.class);
            intent.putExtra("receiver_username",receiver);
            intent.putExtra("caller_username",sender);
            intent.putExtra("call_type","video");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        ac_btn.setOnClickListener(v->
        {
            Intent intent = new Intent(ChatActivity.this,OutgoingCall.class);
            intent.putExtra("receiver_username",receiver);
            intent.putExtra("caller_username",sender);
            intent.putExtra("call_type","audio");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        back.setOnClickListener(v->{finish();});

        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        ChatlistRef=FirebaseDatabase.getInstance().getReference().child("Chatlist");
        Chatsref=FirebaseDatabase.getInstance().getReference().child("Chats");
        RootRef = FirebaseDatabase.getInstance().getReference();
        Usersref = FirebaseDatabase.getInstance().getReference().child("users");
        checkIfFriend();

        send_btn.setOnClickListener(v->{
            String message= chat_text.getText().toString().trim();
            if(!(message.trim().equals(""))){
                sendMessage(sender,receiver,message);
            }else
            {
                Toast.makeText(this, "You can't send Empty Message", Toast.LENGTH_SHORT).show();
            }
            chat_text.setText("");
        });

        readMessages();

        attach_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new   CharSequence[]
                        {
                                "Images","PDF Files","Other"
                        };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select File Type");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int option)
                    {
                        if(option==0)
                        {
                            file_type="image";

                            Intent intent = new Intent();
                            intent.setAction(intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Image"),1);

                        }
                        if(option==1)
                        {
                            file_type="pdf";
                            Intent intent = new Intent();
                            intent.setAction(intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Select File"),1);
                        }
                        if(option==2)
                        {
                            file_type="docx";
                            Intent intent = new Intent();
                            intent.setAction(intent.ACTION_GET_CONTENT);
                            intent.setType("application/*");
                            startActivityForResult(intent.createChooser(intent,"Select File"),1);
                        }

                    }

                });
                builder.show();
            }
        });

        chat_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(chat_text.getText().toString().equals(""))
                    attach_file.setVisibility(View.VISIBLE);
                else
                    attach_file.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, @NonNull @NotNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();

                if(chats.get(position).getSender().equals(sender))
                {
                    String MessageID = chats.get(position).getMessageID();

                    FirebaseDatabase.getInstance().getReference("Chats")
                            .child(sender).child(receiver).child(MessageID).removeValue();

                    FirebaseDatabase.getInstance().getReference("Chats")
                            .child(receiver).child(sender).child(MessageID).removeValue();
                }else if(chats.get(position).getSender().equals(receiver))
                {
                    String MessageID = chats.get(position).getMessageID();

                    FirebaseDatabase.getInstance().getReference("Chats")
                            .child(sender).child(receiver).child(MessageID).removeValue();
                    Toast.makeText(ChatActivity.this, "Message deleted for you", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public int getSwipeDirs(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder) {


                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onChildDraw(@NonNull @NotNull Canvas c, @NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(ChatActivity.this, R.color.red))
                        .addSwipeLeftActionIcon(R.drawable.delete)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void checkIfFriend() {

        ContactsRef.child(sender)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild(receiver))
                        {
                            chatsendlayout.setVisibility(View.VISIBLE);
                            call_linear.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            chatsendlayout.setVisibility(View.GONE);
                            call_linear.setVisibility(View.GONE);
                            send_btn.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessage(String sender,String receiver,String message){

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String strDate= formatter.format(date);

        long time = Long.parseLong(strDate);
        String messageSenderRef = "Chats/" + sender + "/" + receiver;
        String messageReceiverRef = "Chats/" + receiver + "/" + sender;

        DatabaseReference userMessageKeyRef = RootRef.child("Chats").child(sender).child(receiver).push();

        String messagePushID = userMessageKeyRef.getKey();

        Map messageTextBody = new HashMap();
        messageTextBody.put("message", message);
        messageTextBody.put("type", "text");
        messageTextBody.put("sender", sender);
        messageTextBody.put("receiver", receiver);
        messageTextBody.put("messageID", messagePushID);
        messageTextBody.put("seen", false);


        Map messageBodyDetails = new HashMap();
        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
        messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

        RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task)
            {
                if (task.isSuccessful())
                {
                    sendNotification sendNoti =new  sendNotification(receiver,
                            getApplicationContext(),
                            ChatActivity.this,
                            sender +" : "+message,
                            "message",
                            sender);
                    sendNoti.noti_prep();
                }
                else
                {
                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }

            }
        });

        HashMap<String,Object> timeMap = new HashMap<>();
        timeMap.put("last_chat",time);
        ChatlistRef.child(sender).child(receiver).setValue(timeMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            ChatlistRef.child(receiver).child(sender).setValue(timeMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            { }
                                        }
                                    });
                        }
                    }
                });
    }

    private void readMessages() {
        chats = new ArrayList<>();

        RootRef.child("Chats").child(sender).child(receiver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot datasnapshot) {
                chats.clear();
                for(DataSnapshot snapshot: datasnapshot.getChildren())
                {
                    ChatModel chat = snapshot.getValue(ChatModel.class);

                    chats.add(chat);
                    chatAdapter = new ChatAdapter(getApplicationContext(),chats,img_url,sender,receiver);
                    chatAdapter.notifyDataSetChanged();
                    chat_recview.setAdapter(chatAdapter);
                    chat_recview.smoothScrollToPosition(chat_recview.getAdapter().getItemCount());
                    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                    itemTouchHelper.attachToRecyclerView(chat_recview);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            loadingbar.setTitle("Wait..");
            loadingbar.setMessage("Sending");
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.show();

            fileUri = data.getData();
            if(!file_type.equals("image"))
            {
                file_type= MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
                String messageSenderRef = "Chats/" + sender + "/" + receiver;
                String messageReceiverRef = "Chats/" + receiver + "/" + sender;

                DatabaseReference userMessageKeyRef = RootRef.child("Chats").child(sender).child(receiver).push();

                final String messagePushID = userMessageKeyRef.getKey();

                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                storageReference = firebaseStorage.getReference().child("Profile Images").child(messagePushID+"."+file_type);

                StorageReference finalStorageReference = storageReference;
                storageReference.putFile(fileUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                            {
                                finalStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri)
                                    {
                                        Map messageTextBody = new HashMap();
                                        messageTextBody.put("message", uri.toString());
                                        messageTextBody.put("name", fileUri.getLastPathSegment());
                                        messageTextBody.put("type", file_type);
                                        messageTextBody.put("sender", sender);
                                        messageTextBody.put("receiver", receiver);
                                        messageTextBody.put("messageID", messagePushID);
                                        messageTextBody.put("seen", false);

                                        Map messageBodyDetails = new HashMap();
                                        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                                        messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                                        RootRef.updateChildren(messageBodyDetails);
                                        loadingbar.dismiss();
                                    }
                                });
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull @NotNull UploadTask.TaskSnapshot snapshot) {
                        double p = (100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                        loadingbar.setMessage("Uploading... "+(int) p +" %");
                    }
                });



            }else if(file_type.equals("image"))
            {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                String messageSenderRef = "Chats/" + sender + "/" + receiver;
                String messageReceiverRef = "Chats/" + receiver + "/" + sender;

                DatabaseReference userMessageKeyRef = RootRef.child("Chats").child(sender).child(receiver).push();

                final String messagePushID = userMessageKeyRef.getKey();

                StorageReference filePath = storageReference.child(messagePushID+".jpg");

                uploadTask=filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull @NotNull Task task) throws Exception
                    {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Uri> task)
                    {
                      if(task.isSuccessful())
                      {
                          Uri downloadUrl = task.getResult();
                          fileUrl = downloadUrl.toString();
                          Map messageTextBody = new HashMap();
                          messageTextBody.put("message", fileUrl);
                          messageTextBody.put("name", fileUri.getLastPathSegment());
                          messageTextBody.put("type", file_type);
                          messageTextBody.put("sender", sender);
                          messageTextBody.put("receiver", receiver);
                          messageTextBody.put("messageID", messagePushID);
                          messageTextBody.put("seen", false);

                          Map messageBodyDetails = new HashMap();
                          messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                          messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                          RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                              @Override
                              public void onComplete(@NonNull Task task)
                              {
                                  if (task.isSuccessful())
                                  {
                                      loadingbar.dismiss();
                                  }
                                  else
                                  {
                                      loadingbar.dismiss();
                                      Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                  }

                              }
                          });
                      }
                    }
                });

            }
            else{
                loadingbar.dismiss();
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

}