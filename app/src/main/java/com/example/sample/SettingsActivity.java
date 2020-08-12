package com.example.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    DatabaseReference mUserReference;
    FirebaseUser currentUser;
    StorageReference mStorageReference;
    TextView mDisplayName,mStatus;
    CircleImageView mDisplayImage;
    String uId;
    ProgressDialog progressDialog;
    private Toolbar mToolbar;
    StorageReference originalPhotoPath,thumbPath;

    String downloadUrl;
    String thumbDownloadUrl;
    private DatabaseReference userReference;

//    @Override
//    protected void onStart() {
//        super.onStart();
//        if(currentUser!=null){
//            mUserReference.child("online").setValue(true);
//        }
//    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        if(currentUser!=null){
//            mUserReference.child("online").setValue(false);
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar=(Toolbar)findViewById(R.id.userSettingAppBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDisplayName=findViewById(R.id.settingName);
        mStatus=findViewById(R.id.settingStatus);
        mDisplayImage=findViewById(R.id.settingDp);

        mStorageReference= FirebaseStorage.getInstance().getReference();
        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        uId=currentUser.getUid();


        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uId);




        mUserReference.keepSynced(true);
        if(currentUser!=null){
            mUserReference.child("online").setValue(true);
        }
        mUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name =dataSnapshot.child("Name").getValue().toString();
                final String image= dataSnapshot.child("Image").getValue().toString();
                String status= dataSnapshot.child("Status").getValue().toString();
                String ThumbImage= dataSnapshot.child("ThumbImage").getValue().toString();

                mDisplayName.setText(name);
                mStatus.setText(status);
                if(image.equals("default")){
                    Picasso.with(SettingsActivity.this).load(R.drawable.avatar).placeholder(R.drawable.avatar).into(mDisplayImage);
                }
                else if(!image.equals("default")) {
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.avatar).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() { }
                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.avatar).into(mDisplayImage);
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void changeStatusBtn(View view) {
        Intent statusIntent=new Intent(SettingsActivity.this,StatusActivity.class);
        statusIntent.putExtra("statusValue",mStatus.getText().toString());
        startActivity(statusIntent);
    }

    public void changeDp(View view) {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SettingsActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                progressDialog=new ProgressDialog(SettingsActivity.this);
                progressDialog.setTitle("Uploading Image");
                progressDialog.setMessage("Please wait while image is uploading");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();
                File thumbFilePath=new File(resultUri.getPath());
                Bitmap thumbImage= null;
                try {
                    thumbImage = new Compressor(this)
                                .setMaxWidth(200)
                                .setMaxHeight(200)
                                .setQuality(75)
                                .compressToBitmap(thumbFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumbByte = baos.toByteArray();

                originalPhotoPath =mStorageReference.child("profileImages").child(uId+".jpg");
                thumbPath=mStorageReference.child("profileImages").child("thumbs").child(uId+".jpg");

                originalPhotoPath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            originalPhotoPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadUrl=uri.toString();
                                }
                            });

                            UploadTask uploadTask = thumbPath.putBytes(thumbByte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    if(task.isSuccessful()){
                                        thumbPath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Uri> task) {
                                                thumbDownloadUrl=task.getResult().toString();
                                            }
                                        });
//                                        thumbDownloadUrl=thumbPath.getDownloadUrl().toString();
                                        Map updateMap=new HashMap();
                                    updateMap.put("Image",downloadUrl);
//                                    Toast.makeText(getApplicationContext(),thumbDownloadUrl,Toast.LENGTH_LONG).show();
//                                        Log.e("sdsfgfhgdfskddjvbhfb",thumbDownloadUrl);
//                                        updateMap.put("ThumbImage",thumbDownloadUrl);

                                        mUserReference.updateChildren(updateMap).addOnSuccessListener(new OnSuccessListener() {
                                            @Override
                                            public void onSuccess(Object o) {
                                                progressDialog.dismiss();
                                                Toast.makeText(getApplicationContext(),"image uploaded successfully",Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(getApplicationContext(),"image not uploaded",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            });
//                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                @Override
//                                public void onSuccess(UploadTask.TaskSnapshot thumbTask) {
//
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    progressDialog.dismiss();
//                                    Toast.makeText(getApplicationContext(),"error",Toast.LENGTH_SHORT).show();
//                                }
//                            });
                        }else {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"error",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
