package com.elite.mychat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView iv_profile;
    private TextView tv_profile_name, tv_profile_status, tv_profile_count;
    private Button btn_profile_send, btn_profile_decline;
    private ProgressBar pb_profile;
    private String frndreq_current_state;
    private DatabaseReference frndreq_db_ref, friends_db_ref, notify_db_ref, db_ref, users_db_ref;
    private FirebaseUser current_user;
    private String frnd_since;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        current_user = FirebaseAuth.getInstance().getCurrentUser();

        Toolbar toolbar = findViewById(R.id.profile_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        final String uid = getIntent().getStringExtra("uid");

        db_ref = FirebaseDatabase.getInstance().getReference();
        db_ref.keepSynced(true);

        users_db_ref = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        users_db_ref.keepSynced(true);

        frndreq_db_ref = FirebaseDatabase.getInstance().getReference().child("frndreq");
        frndreq_db_ref.keepSynced(true);

        friends_db_ref = FirebaseDatabase.getInstance().getReference().child("friends");
        friends_db_ref.keepSynced(true);

        notify_db_ref = FirebaseDatabase.getInstance().getReference().child("notifications");
        notify_db_ref.keepSynced(true);

        iv_profile = findViewById(R.id.iv_profile);
        tv_profile_name = findViewById(R.id.tv_profile_name);
        tv_profile_status = findViewById(R.id.tv_profile_status);
        tv_profile_count = findViewById(R.id.tv_profile_count);
        btn_profile_send = findViewById(R.id.btn_profile_send);
        btn_profile_decline = findViewById(R.id.btn_profile_decline);
        pb_profile = findViewById(R.id.pb_profile);

        frndreq_current_state = "not friends";

        pb_profile.setVisibility(View.VISIBLE);

        users_db_ref.addValueEventListener(//WHILE LOADING
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();
                        String image = dataSnapshot.child("image").getValue().toString();

                        tv_profile_name.setText(name);
                        tv_profile_status.setText(status);

                        frndreq_db_ref.child(current_user.getUid()).addValueEventListener(//other end purpose
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild(uid)) {
                                            String frndreq_type = dataSnapshot.child(uid).child("request_type").getValue().toString();
                                            if (frndreq_type.equals("sent")) {
                                                frndreq_current_state = "frndreq sent";
                                                btn_profile_send.setText("Cancel Friend Request");
                                            } else if (frndreq_type.equals("received")) {
                                                frndreq_current_state = "frndreq received";
                                                btn_profile_send.setText("Accept Friend Request");
                                                btn_profile_decline.setVisibility(View.VISIBLE);
                                            }
                                        } else {
                                            friends_db_ref.child(current_user.getUid()).addValueEventListener(
                                                    new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.hasChild(uid)) {
                                                                frndreq_current_state = "friends";
                                                                btn_profile_send.setText("Unfriend");
                                                                btn_profile_decline.setVisibility(View.GONE);
                                                            } else {
                                                                frndreq_current_state = "not friends";
                                                                btn_profile_send.setText("Send Friend Request");
                                                                btn_profile_decline.setVisibility(View.GONE);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    }
                                            );
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                }
                        );

                        if (!image.equals("default"))
                            if (!ProfileActivity.this.isDestroyed() && !ProfileActivity.this.isFinishing())
                                Glide.with(ProfileActivity.this).load(image)
                                        .thumbnail(0.5f)
                                        .crossFade()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .placeholder(R.drawable.ic_user)
                                        .into(iv_profile);

                        pb_profile.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

        btn_profile_send.setOnClickListener(//ONCLICKING SEND BUTTON
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (frndreq_current_state.equals("not friends")) {//SEND TO CANCEL
                            notify_db_ref = notify_db_ref.child(uid).push();
                            String notification_id = notify_db_ref.getKey();

                            HashMap<String, String> notifications_map = new HashMap<>();
                            notifications_map.put("from", current_user.getUid());
                            notifications_map.put("type", "request");

                            Map map = new HashMap();
                            map.put("frndreq/" + current_user.getUid() + "/" + uid + "/request_type", "sent");
                            map.put("frndreq/" + current_user.getUid() + "/" + uid + "/timestamp", ServerValue.TIMESTAMP);
                            map.put("frndreq/" + uid + "/" + current_user.getUid() + "/request_type", "received");
                            map.put("frndreq/" + uid + "/" + current_user.getUid() + "/timestamp", ServerValue.TIMESTAMP);
                            map.put("notifications/" + uid + "/" + notification_id, notifications_map);

                            db_ref.updateChildren(map, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    Toast.makeText(ProfileActivity.this, "Request sent :)",
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
                        }

                        if (frndreq_current_state.equals("frndreq sent")) {//CANCEL TO SEND
                            Map map = new HashMap();
                            map.put("frndreq/" + current_user.getUid() + "/" + uid + "/request_type", null);
                            map.put("frndreq/" + current_user.getUid() + "/" + uid + "/timestamp", null);
                            map.put("frndreq/" + uid + "/" + current_user.getUid() + "/request_type", null);
                            map.put("frndreq/" + uid + "/" + current_user.getUid() + "/timestamp", null);
                            db_ref.updateChildren(map, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    Toast.makeText(ProfileActivity.this, "Request cancelled :)",
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
                        }

                        if (frndreq_current_state.equals("frndreq received")) {
                            frnd_since = DateFormat.getDateTimeInstance().format(new Date());

                            Map map = new HashMap();
                            map.put("frndreq/" + current_user.getUid() + "/" + uid + "/request_type", null);
                            map.put("frndreq/" + current_user.getUid() + "/" + uid + "/timestamp", null);
                            map.put("frndreq/" + uid + "/" + current_user.getUid() + "/request_type", null);
                            map.put("frndreq/" + uid + "/" + current_user.getUid() + "/timestamp", null);
                            map.put("friends/" + current_user.getUid() + "/" + uid + "/date", frnd_since);
                            map.put("friends/" + uid + "/" + current_user.getUid() + "/date", frnd_since);
                            db_ref.updateChildren(map, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    Toast.makeText(ProfileActivity.this, "You're friends now :)",
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
                        }

                        if (frndreq_current_state.equals("friends")) {
                            Map map = new HashMap();
                            map.put("friends/" + current_user.getUid() + "/" + uid, null);
                            map.put("friends/" + uid + "/" + current_user.getUid(), null);
                            db_ref.updateChildren(map, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    Toast.makeText(ProfileActivity.this, "Unfriended :)",
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
                        }

                    }
                }
        );

        btn_profile_decline.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Map map = new HashMap();
                        map.put("frndreq/" + current_user.getUid() + "/" + uid + "/request_type", null);
                        map.put("frndreq/" + uid + "/" + current_user.getUid() + "/request_type", null);
                        db_ref.updateChildren(map, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                Toast.makeText(ProfileActivity.this, "Request denied :)",
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
                    }
                }
        );

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(current_user.getUid())
                .child("online")
                .setValue("true");
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(current_user.getUid())
                .child("online")
                .setValue(ServerValue.TIMESTAMP);
    }
}
