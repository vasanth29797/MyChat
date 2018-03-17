package com.elite.mychat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser current_user;
    private String current_user_id;
    private Toolbar toolbar;
    private ViewPager main_page_vp;
    private ViewPagerAdapter adapter;
    private TabLayout main_page_tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("MyChat");

        //tabs
        main_page_vp = findViewById(R.id.main_page_vp);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        main_page_vp.setAdapter(adapter);

        main_page_tabs = findViewById(R.id.main_page_tl);
        main_page_tabs.setupWithViewPager(main_page_vp);

    }

    @Override
    protected void onStart() {
        super.onStart();

        current_user = auth.getCurrentUser();//returns null if user is  not signed in

        if (current_user == null) {
            sendToStart();
        } else {
            current_user_id = current_user.getUid();

            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(current_user_id)
                    .child("online")
                    .setValue("true");
        }

    }

    private void sendToStart() {
        Intent StartIntent = new Intent(MainActivity.this, StartActivity.class);
        StartIntent.putExtra("uid", current_user_id);
        startActivity(StartIntent);
        finish();//once user is directed to StartActivity he/she should not return to MainActivity
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.action_logout:
                FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .child(current_user_id)
                        .child("online")
                        .setValue(ServerValue.TIMESTAMP);
                auth.signOut();
                sendToStart();
                break;
            case R.id.action_settings:
                Intent SettingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(SettingsIntent);
                break;
            case R.id.action_allusers:
                Intent UsersIntent = new Intent(MainActivity.this, UsersActivity.class);
                startActivity(UsersIntent);
                break;
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(current_user_id)
                .child("online")
                .setValue(ServerValue.TIMESTAMP);

    }

}
