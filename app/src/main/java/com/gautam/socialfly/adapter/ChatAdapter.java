package com.gautam.socialfly.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gautam.socialfly.ChatActivity;
import com.gautam.socialfly.Model.ChatModel;
import com.gautam.socialfly.R;
import com.gautam.socialfly.ViewImage;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public static  final int MSG_TYPE_LEFT  = 0;
    public static  final int MSG_TYPE_RIGHT = 1;

    private Context context;
    List<ChatModel> chats;
    private String img_url;
    String sender,receiver;

    DatabaseReference RootRef ;

    public ChatAdapter(Context context, List<ChatModel> chats, String img_url, String sender, String receiver) {
        this.context = context;
        this.chats = chats;
        this.img_url = img_url;
        this.sender = sender;
        this.receiver = receiver;

        RootRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_right_item,parent,false);
            return new ChatAdapter.ViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_left_item,parent,false);
            return new ChatAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ChatAdapter.ViewHolder holder, int position) {

        ChatModel chatModel = chats.get(position);

        String type = chatModel.getType();

        if(type.equals("text"))
        {
            holder.message_image.setVisibility(View.GONE);
            holder.message_file.setVisibility(View.GONE);

            holder.show_message.setText(chatModel.getMessage());
            Glide.with(context)
                    .load(img_url).centerCrop()
                    .into(holder.chatimg);

        }
        else if(type.equals("image"))
        {
            holder.message_image.setVisibility(View.VISIBLE);
            holder.show_message.setVisibility(View.GONE);
            holder.message_file.setVisibility(View.GONE);

            Glide.with(context)
                    .load(chatModel.getMessage()).centerCrop()
                    .into(holder.message_image);

            Glide.with(context)
                    .load(img_url)
                    .into(holder.chatimg);

            holder.message_image.setOnClickListener(v->
            {
                Intent intent = new Intent(holder.itemView.getContext(), ViewImage.class);
                intent.putExtra("message_image_url",chatModel.getMessage());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                holder.itemView.getContext().startActivity(intent);

            });
        }else
        {
            holder.message_image.setVisibility(View.GONE);
            holder.show_message.setVisibility(View.GONE);

            holder.message_file.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(img_url)
                    .into(holder.chatimg);

            Glide.with(context)
                    .load(R.drawable.docs).centerCrop()
                    .into(holder.message_file);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(chats.get(position).getMessage()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    holder.itemView.getContext().startActivity(intent);

                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView show_message,show_seen;
        CircleImageView chatimg;
        ShapeableImageView message_image;
        ImageView message_file;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            show_message=itemView.findViewById(R.id.show_message);
            chatimg=itemView.findViewById(R.id.chatimg);
            message_image=itemView.findViewById(R.id.message_img);
            message_file=itemView.findViewById(R.id.message_file);
            show_seen=itemView.findViewById(R.id.show_seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(chats.get(position).getSender().equals(sender))
            return MSG_TYPE_RIGHT;
        else
            return MSG_TYPE_LEFT;
    }

}
