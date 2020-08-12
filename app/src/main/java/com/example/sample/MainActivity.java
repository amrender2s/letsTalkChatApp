package com.example.sample;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    TabFragmentAdapter messangerAdapter;
    DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth =FirebaseAuth.getInstance();

        mToolbar=(Toolbar)findViewById(R.id.mainPageToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Lets Talk");

        if(mAuth.getCurrentUser()!=null) {
            userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }
        tabLayout=(TabLayout)findViewById(R.id.mainTab);
        viewPager=(ViewPager)findViewById(R.id.tabPager);


//        tabLayout.addTab(tabLayout.newTab().setText("Requests"));
        tabLayout.addTab(tabLayout.newTab().setText("Chat"));
        tabLayout.addTab(tabLayout.newTab().setText("Friends"));

        messangerAdapter=new TabFragmentAdapter(this,getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(messangerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                Toast.makeText(getApplicationContext(),"current",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser= mAuth.getCurrentUser();

        if(currentUser==null){
            sendToStart();
        }else {
            userReference.child("online").setValue(true);
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if(mAuth.getCurrentUser()!=null){
//            userReference.child("online").setValue(true);
//        }
//    }

//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        if(mAuth.getCurrentUser()!=null){
//            userReference.child("online").setValue(true);
//        }
//    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuth.getCurrentUser()!=null){
            userReference.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void sendToStart() {
        Intent startIntent=new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id){
            case R.id.main_account_setting:
                Intent settingIntent=new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(settingIntent);
                break;

            case R.id.main_all_users:Toast.makeText(getApplicationContext(),"All users",Toast.LENGTH_SHORT).show();
                Intent userIntent=new Intent(MainActivity.this,UserActivity.class);
                startActivity(userIntent);
                break;

            case R.id.main_logout_btn:Toast.makeText(getApplicationContext(),"Logout Button",Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                sendToStart();
                break;

        }
        return true;
    }
}
