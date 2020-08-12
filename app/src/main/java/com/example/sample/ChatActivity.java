package com.example.sample;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    Toolbar chatToolBar;
    DatabaseReference rootReference;
    private FirebaseAuth mAuth;
    private DatabaseReference userReference,userMessageReference;
    private String currentUserId;
    private EditText messageField;
    private String chatUserId;
    private RecyclerView messageListView;
    private List<Message> list;
    private LinearLayoutManager layoutManager;
    private MessageAdapter adapter;
    private ImageButton sendButton,addButton;
    private static final int totalItemToLoad=10;
    private int currentPage=1;
    private int itemPos=0;
    private SwipeRefreshLayout refreshLayout;
    private String lastMessage="";
    private String prevKey="";

//    @Override
//    protected void onStart() {
//        super.onStart();
//        FirebaseUser currentUser= mAuth.getCurrentUser();
//
//        if(mAuth.getCurrentUser()!=null){
//            userReference.child("online").setValue(true);
//        }
//    }
//
////    @Override
////    protected void onStop() {
////        super.onSt\\p();
////        if(mAuth.getCurrentUser()!=null){
////            userReference.child("online").setValue(false);
////        }
////    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        list=new ArrayList<>();
        messageField=findViewById(R.id.mess);
        sendButton=findViewById(R.id.send);
        addButton=findViewById(R.id.add);
        messageListView=findViewById(R.id.chatRecyclerView);
        refreshLayout=findViewById(R.id.messageSwipeLayout);
        messageListView.setHasFixedSize(true);
        messageListView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new MessageAdapter(list);
        messageListView.setAdapter(adapter);
        layoutManager=new LinearLayoutManager(this);

        sendButton.setEnabled(true);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();

            }
        });

        chatUserId=getIntent().getStringExtra("userId");
        String name=getIntent().getStringExtra("name");
        String image=getIntent().getStringExtra("image");
        String online=getIntent().getStringExtra("lastSeen");

        chatToolBar=(Toolbar)findViewById(R.id.chatActionBar);
        setSupportActionBar(chatToolBar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mAuth= FirebaseAuth.getInstance();
        rootReference=FirebaseDatabase.getInstance().getReference();
        currentUserId =mAuth.getCurrentUser().getUid();
        userReference=rootReference.child("Users").child(currentUserId);
        userMessageReference=rootReference.child("Messages").child(currentUserId).child(chatUserId);

        loadMessages();

        LayoutInflater inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView=inflater.inflate(R.layout.custom_action_bar,null);
        TextView userName=(TextView)actionBarView.findViewById(R.id.chatUser);
        TextView lastSeen=(TextView)actionBarView.findViewById(R.id.lastSeen);
        CircleImageView imageView=(CircleImageView)actionBarView.findViewById(R.id.dp);
        userName.setText(name);
        Picasso.with(getApplicationContext()).load(image).into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatActivity.this,ProfileActivity.class).putExtra("userId",chatUserId));
            }
        });
        actionBar.setCustomView(actionBarView);

       if(online.equals("true")){
           lastSeen.setText("Online");
       }else {
           GetTimeAgo getTimeAgo=new GetTimeAgo();
           long lastTime=Long.parseLong(online);
           String lastSeenTime=getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
           lastSeen.setText(lastSeenTime);
       }

       rootReference.child("Chat").child(currentUserId).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if(!dataSnapshot.hasChild(chatUserId)){
                   Map chatAddMap=new HashMap();
                   chatAddMap.put("seen",false);
                   chatAddMap.put("timeStamp", ServerValue.TIMESTAMP);

                   Map chatUserMap=new HashMap();
                   chatUserMap.put("Chat/"+ currentUserId +"/"+chatUserId,chatAddMap);
                   chatUserMap.put("Chat/"+ chatUserId +"/"+currentUserId,chatAddMap);
                    rootReference.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

       refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
           @Override
           public void onRefresh() {
                currentPage++;
                itemPos=0;
                loadMoreMessages();
                refreshLayout.setRefreshing(false);
           }
       });
    }

    private void loadMessages() {
        DatabaseReference messageRef=rootReference.child("Messages").child(currentUserId).child(chatUserId);
        Query messageQuery=messageRef.limitToLast(currentPage * totalItemToLoad);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message=dataSnapshot.getValue(Message.class);
                itemPos++;
                if(itemPos==1){
                    String messageKey=dataSnapshot.getKey();
                    lastMessage=messageKey;
                    prevKey=messageKey;
                }
                list.add(message);
                adapter.notifyDataSetChanged();
                messageListView.scrollToPosition(list.size()-1);
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void loadMoreMessages() {
        DatabaseReference messageRef=rootReference.child("Messages").child(currentUserId).child(chatUserId);
        Query messageQuery=messageRef.orderByKey().endAt(lastMessage).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Message message=dataSnapshot.getValue(Message.class);
                String messageKey=dataSnapshot.getKey();
                if(!prevKey.equals(messageKey)){
                    list.add(itemPos++,message);
                }  else {
                    prevKey=lastMessage;
                }
                if(itemPos==1){
                    lastMessage=dataSnapshot.getKey();
                }
                adapter.notifyDataSetChanged();
                layoutManager.scrollToPositionWithOffset(totalItemToLoad,0);
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {
        String message=messageField.getText().toString();
        if(!TextUtils.isEmpty(message)){
            String currentUserReference="Messages/"+currentUserId+"/"+chatUserId;
            String chatUserReference="Messages/"+chatUserId+"/"+currentUserId;
            DatabaseReference userMessagePush=rootReference.child("Messages").child(currentUserId).child(chatUserId).push();
            String pushKey=userMessagePush.getKey();
            Map messageMap=new HashMap<>();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",currentUserId);

            Map messageUserMap=new HashMap<>();
            messageUserMap.put(currentUserReference+"/"+pushKey,messageMap);
            messageUserMap.put(chatUserReference+"/"+pushKey,messageMap);
            rootReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            });
            messageField.setText("");
        }
    }
    public void add(View view) {
    }
}
