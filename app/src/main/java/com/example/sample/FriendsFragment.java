package com.example.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment {
    View view;
    RecyclerView friendsView;
    LinearLayoutManager linearLayoutManager;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference userReference, friendsReference;
    FirebaseRecyclerAdapter<Friends, ChatUserHolder> recyclerAdapter;
    FirebaseRecyclerOptions<Friends> options;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
         view=inflater.inflate(R.layout.fragment_friends, container, false);

        friendsView = (RecyclerView)view.findViewById(R.id.friendsRecyclerView);
        linearLayoutManager = new LinearLayoutManager(getContext());
        friendsView.setLayoutManager(linearLayoutManager);

        mAuth=FirebaseAuth.getInstance();
        currentUserId= mAuth.getCurrentUser().getUid();

        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);
        friendsReference.keepSynced(true);
        userReference= FirebaseDatabase.getInstance().getReference().child("Users");
        userReference.keepSynced(true);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(friendsReference, Friends.class).build();
        recyclerAdapter=new FirebaseRecyclerAdapter<Friends, ChatUserHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatUserHolder holder, int position, @NonNull Friends model) {
                holder.statusView.setText(model.getDate());
                final String listUserId=getRef(position).getKey();
                userReference.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String name=dataSnapshot.child("Name").getValue().toString();
                        final String image=dataSnapshot.child("Image").getValue().toString();
                        final String userOnline=dataSnapshot.child("online").getValue().toString();
                        Log.e("onnnnnnnnnnnnn",userOnline);
                        holder.tv1.setText(name);
                        Picasso.with(view.getContext()).load(image).into(holder.img);

                        if(dataSnapshot.hasChild("online")){
                            if(userOnline.equals("true")) {
                                holder.userOnlineImage.setVisibility(View.VISIBLE);
                            }else {
                                holder.userOnlineImage.setVisibility(View.INVISIBLE);
                            }
                        }
                        holder.v.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getContext(),ChatActivity.class)
                                        .putExtra("userId",listUserId)
                                        .putExtra("name",name)
                                        .putExtra("image",image)
                                        .putExtra("lastSeen",userOnline));
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
            @NonNull
            @Override
            public ChatUserHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new ChatUserHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_user_list,viewGroup,false));
            }
        };
        recyclerAdapter.startListening();
        friendsView.setAdapter(recyclerAdapter);

    }

    @Override
    public void onStop() {
        super.onStop();
        recyclerAdapter.stopListening();
    }

    public static class ChatUserHolder extends RecyclerView.ViewHolder{
        TextView statusView,tv1;
        CircleImageView img;
        ImageView userOnlineImage;
        View v;

        public ChatUserHolder(@NonNull View itemView) {
            super(itemView);
            v=itemView;
            statusView =(TextView)itemView.findViewById(R.id.userSingleStatus);
            img=(CircleImageView) itemView.findViewById(R.id.userImage);
            tv1=(TextView)itemView.findViewById(R.id.userSingleName);
            userOnlineImage=(ImageView)itemView.findViewById(R.id.greenCircle);

        }
    }
}
