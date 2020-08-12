package com.example.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    Toolbar statusToolbar;
    TextInputLayout mStatus;
    ProgressDialog saveStatusProgress;
    DatabaseReference statusReference;
    private DatabaseReference userReference;

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            userReference.child("online").setValue(true);
        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
//            userReference.child("online").setValue(false);
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        statusToolbar=(Toolbar)findViewById(R.id.statusToolbar);
        setSupportActionBar(statusToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        userReference= FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mStatus=findViewById(R.id.statusFeild);
        mStatus.getEditText().setText(getIntent().getStringExtra("statusValue"));

        FirebaseUser currentUser= FirebaseAuth.getInstance().getCurrentUser();
        String uId=currentUser.getUid();
        statusReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uId);
    }

    public void saveStatusBtn(View view) {
        saveStatusProgress=new ProgressDialog(StatusActivity.this);
        saveStatusProgress.setTitle("Saving Changes");
        saveStatusProgress.setMessage("Please wait while we save your changes !");
        saveStatusProgress.setCanceledOnTouchOutside(false);
        saveStatusProgress.show();

        String status=mStatus.getEditText().getText().toString();
        statusReference.child("Status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    saveStatusProgress.dismiss();
                    startActivity(new Intent(StatusActivity.this,SettingsActivity.class));
                }else {
                    Toast.makeText(getApplicationContext(),"There was some error on saving Changes",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
