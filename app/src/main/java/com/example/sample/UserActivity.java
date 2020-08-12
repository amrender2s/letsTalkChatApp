package com.example.sample;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    RecyclerView userView;
    DatabaseReference reference;
    LinearLayoutManager linearLayoutManager;
    private DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mToolbar=(Toolbar)findViewById(R.id.userAppBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userView = (RecyclerView) findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        userView.setLayoutManager(linearLayoutManager);

        reference= FirebaseDatabase.getInstance().getReference().child("Users");
        userReference= reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userReference.keepSynced(true);
        reference.keepSynced(true);
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
//            userReference.child("online").setValue(false);
//        }
//    }

    @Override
    protected void onStart() {
        super.onStart();
//        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
//            userReference.child("online").setValue(true);
//        }
        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(reference, Users.class).build();

        FirebaseRecyclerAdapter<Users,UserViewHolder> recyclerAdapter=new FirebaseRecyclerAdapter<Users, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull Users model) {
                holder.tv1.setText(model.getName());
                holder.tv2.setText(model.getStatus());
                Picasso.with(UserActivity.this).load(model.getImage()).into(holder.img);
                final String userId=getRef(position).getKey();
                holder.v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent=new Intent(UserActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("userId",userId);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new UserViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_user_list,viewGroup,false));
            }
        };
        recyclerAdapter.startListening();
        userView.setAdapter(recyclerAdapter);


    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        CircleImageView img;
        TextView tv1,tv2;
        View v;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            v=itemView;
            img=(CircleImageView) itemView.findViewById(R.id.userImage);
            tv1=(TextView)itemView.findViewById(R.id.userSingleName);
            tv2=(TextView)itemView.findViewById(R.id.userSingleStatus);
        }

//        public void setName(String name) {
//            tv1.setText(name);
//        }
    }
}
