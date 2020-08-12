package com.example.sample;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder1> {
    public List<Message> messageList;
    FirebaseAuth mAuth;

    public MessageAdapter(List<Message> myListData) {
        this.messageList=myListData;
    }

    @Override
    public ViewHolder1 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.message_single_layout, parent, false);
        return new ViewHolder1(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder1 viewHolder1, int i) {
        mAuth=FirebaseAuth.getInstance();
        String currentUserId=mAuth.getCurrentUser().getUid();
        String fromUser= messageList.get(i).getFrom();

        if(fromUser.equals(currentUserId)){
            viewHolder1.constraintLayout.setBackgroundResource(R.drawable.message_background);
            viewHolder1.message.setTextColor(Color.BLACK);
            viewHolder1.messageLayout.setGravity(Gravity.RIGHT);
        }else {
            viewHolder1.message.setTextColor(Color.BLACK);
            viewHolder1.constraintLayout.setBackgroundResource(R.drawable.message_background_1);
            viewHolder1.messageLayout.setGravity(Gravity.START);
        }
        viewHolder1.message.setText(messageList.get(i).getMessage());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }


    public class ViewHolder1 extends RecyclerView.ViewHolder {
        public TextView message;
        public CircleImageView profileImage;
        LinearLayout messageLayout;
        ConstraintLayout constraintLayout;

        public ViewHolder1(View view) {
            super(view);
            message = view.findViewById(R.id.messageTextLayout);
            profileImage = view.findViewById(R.id.messageProfileLayout);
            messageLayout=view.findViewById(R.id.messageLayout);
            constraintLayout=view.findViewById(R.id.messageArea);
        }
    }
}