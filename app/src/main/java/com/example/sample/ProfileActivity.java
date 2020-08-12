package com.example.sample;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private TextView mDisplayName, mUserStatus, mTotalFriends;
    private ImageView mDisplayImage;
    private DatabaseReference userDatabase,mFriendRequestDatabase,friendsDatabase,notificationDatabase,rootRef;
    private ProgressDialog mProgressDialog;
    private FirebaseUser mCurrentUser;
    private String currentState;
    private String userId;
    private Button sendRequest,declineRequest;
    private DatabaseReference userReference;

    @Override
    protected void onStart() {
        super.onStart();
        if(mCurrentUser!=null){
            userReference.child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mCurrentUser!=null){
            userReference.child("online").setValue(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mDisplayName =findViewById(R.id.displayName);
        mUserStatus =findViewById(R.id.currentUserStatus);
        mTotalFriends =findViewById(R.id.totalFriends);
        mDisplayImage=findViewById(R.id.userImage);
        sendRequest=findViewById(R.id.sendRequestButton);
        declineRequest=findViewById(R.id.declineButton);


        mProgressDialog=new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load your data !");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        currentState="notFriend";
        declineRequest.setVisibility(View.INVISIBLE);
        declineRequest.setEnabled(false);
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();

        userId=getIntent().getStringExtra("userId");
        userReference= FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        notificationDatabase=FirebaseDatabase.getInstance().getReference().child("Notifications");
        mFriendRequestDatabase=FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        rootRef=FirebaseDatabase.getInstance().getReference();
        friendsDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        userDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String displayName=dataSnapshot.child("Name").getValue().toString();
                String status=dataSnapshot.child("Status").getValue().toString();
                String image=dataSnapshot.child("Image").getValue().toString();
//                String thumbImage=dataSnapshot.child("ThumbImage").getValue().toString();

                mDisplayName.setText(displayName);
                mUserStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.avatar).into(mDisplayImage);

                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(userId)){
                            String requestType=dataSnapshot.child(userId).child("requestType").getValue().toString();
                            if(requestType.equals("received")){
                                currentState="requestReceived";
                                sendRequest.setText("Accept Friend Request");

                                declineRequest.setVisibility(View.VISIBLE);
                                declineRequest.setEnabled(true);

                            }else if(requestType.equals("sent")){
                                currentState="requestSent";
                                sendRequest.setText("Cancle Friend Request");

                                declineRequest.setVisibility(View.INVISIBLE);
                                declineRequest.setEnabled(false);
                            }
                            mProgressDialog.dismiss();
                        }else {
                            friendsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(userId)){
                                        currentState="friends";
                                        sendRequest.setText("Unfriend "+mDisplayName.getText().toString());
                                        mProgressDialog.dismiss();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }

                        mProgressDialog.dismiss();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        mProgressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mProgressDialog.dismiss();
            }
        });
    }

    public void sendRequest(View view) {
        sendRequest.setEnabled(false);
//        ------------------------------------------------------------------------------------------------------------

        if(currentState.equals("notFriend")){
            String  newNotiId=rootRef.child("Notifications").child(userId).push().getKey();
            HashMap<String,String> map=new HashMap<>();
            map.put("from",mCurrentUser.getUid());
            map.put("type","request");

            Map requestMap=new HashMap<>();
            requestMap.put("Friend Requests/"+mCurrentUser.getUid()+"/"+userId+"/requestType/","sent");
            requestMap.put("Friend Requests/"+userId+"/"+mCurrentUser.getUid()+"/requestType/","received");
            requestMap.put("Notifications/"+userId+"/"+newNotiId,map);
            rootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError!=null){
                            Toast.makeText(getApplicationContext(),"Request not sent",Toast.LENGTH_SHORT).show();
                        }else {
                            currentState="requestSent";
                            sendRequest.setText("Cancle Friend Request");
                            sendRequest.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));

                            declineRequest.setVisibility(View.INVISIBLE);
                            declineRequest.setEnabled(false);
                            Toast.makeText(getApplicationContext(),"Request sent Successfully.",Toast.LENGTH_SHORT).show();
                        }
                    sendRequest.setEnabled(true);
                }
            });
        }
//        ------------------------------------------------------------------------------------------------------------

        if(currentState.equals("requestSent")){
            Map map=new HashMap();
            map.put("Friend Requests/"+mCurrentUser.getUid()+"/"+userId , null);
            map.put("Friend Requests/"+userId+"/"+mCurrentUser.getUid() , null);
            rootRef.updateChildren(map, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                    }else {
                        sendRequest.setEnabled(true);
                        currentState="notFriend";
                        sendRequest.setText("Send Friend Request");
                        sendRequest.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));

                        declineRequest.setVisibility(View.INVISIBLE);
                        declineRequest.setEnabled(false);
                        Toast.makeText(getApplicationContext(),"Request cancled Successfully.",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


//        --------------------------------------------------------------------------------------------------------------------
        if(currentState.equals("requestReceived")){
            final String currentDate= DateFormat.getDateTimeInstance().format(new Date());
            Map map=new HashMap();
            map.put("Friends/"+mCurrentUser.getUid()+"/"+userId+"/date" , currentDate);
            map.put("Friends/"+userId+"/"+mCurrentUser.getUid()+"/date" , currentDate);
            map.put("Friend Requests/"+mCurrentUser.getUid()+"/"+userId , null);
            map.put("Friend Requests/"+userId+"/"+mCurrentUser.getUid() , null);
            rootRef.updateChildren(map, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                    }else {
                        sendRequest.setEnabled(true);
                        currentState="friends";
                        sendRequest.setText("Unfriend "+mDisplayName.getText().toString());
                        sendRequest.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));

                        declineRequest.setVisibility(View.INVISIBLE);
                        declineRequest.setEnabled(false);
                        Toast.makeText(getApplicationContext(),"Request accepted Successfully.",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
//        --------------------------------------------------------------------------------------------------------------------

        if(currentState.equals("friends")){
            Map map=new HashMap();
            map.put("Friends/"+mCurrentUser.getUid()+"/"+userId , null);
            map.put("Friends/"+userId+"/"+mCurrentUser.getUid() , null);
            rootRef.updateChildren(map, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                    }else {
                        sendRequest.setEnabled(true);
                        currentState="notFriend";
                        sendRequest.setText("Send Friend Request");
                        sendRequest.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));

                        declineRequest.setVisibility(View.INVISIBLE);
                        declineRequest.setEnabled(false);
                        Toast.makeText(getApplicationContext(),"Unfriended Successfully.",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void declineRequest(View view) {
        Map map=new HashMap();
        map.put("Friend Request/"+mCurrentUser.getUid()+"/"+userId , null);
        map.put("Friend Request/"+userId+"/"+mCurrentUser.getUid() , null);
        rootRef.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError!=null){
                    Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                }else {
                    sendRequest.setEnabled(true);
                    currentState="notFriend";
                    sendRequest.setText("Send Friend Request");
                    sendRequest.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));

                    declineRequest.setVisibility(View.INVISIBLE);
                    declineRequest.setEnabled(false);
                    Toast.makeText(getApplicationContext(),"Unfriended Successfully.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
