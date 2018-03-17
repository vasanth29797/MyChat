package com.elite.mychat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private DatabaseReference db_ref;
    private TextView tv_chat_custom_name, tv_chat_custom_lastseen;
    private ImageView iv_chat_custom;
    private String current_user_id, uid;
    private EditText et_chat_typing;
    private ImageButton ib_chat_add, ib_chat_send;
    private RecyclerView rv_chat;
    private final List<Messages> message_list = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter adapter;
    public static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int curent_page = 1;
    private SwipeRefreshLayout swipe;
    private int item_pos = 0, count = 0;
    private String last_key = "", prev_key = "";
    public static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db_ref = FirebaseDatabase.getInstance().getReference();
        db_ref.keepSynced(true);

        toolbar = findViewById(R.id.chat_page_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        uid = getIntent().getStringExtra("uid");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_page_custom_toolbar, null);

        actionBar.setCustomView(action_bar_view);
        //actionBar.setCustomView(action_bar_view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        //ViewGroup.LayoutParams.MATCH_PARENT));
        //actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        tv_chat_custom_name = findViewById(R.id.tv_chat_custom_name);
        tv_chat_custom_lastseen = findViewById(R.id.tv_chat_custom_lastseen);
        iv_chat_custom = findViewById(R.id.iv_chat_custom);

        swipe = findViewById(R.id.swipe);

        rv_chat = findViewById(R.id.rv_chat);
        linearLayoutManager = new LinearLayoutManager(this);
        adapter = new MessageAdapter(message_list, uid);
        rv_chat.setHasFixedSize(true);
        linearLayoutManager.setStackFromEnd(true);
        rv_chat.setLayoutManager(linearLayoutManager);
        rv_chat.setAdapter(adapter);

        db_ref.child("Chats").child(current_user_id).child(uid).child("seen").setValue(true);

        loadMessages();

        db_ref.child("Users").child(uid).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("name").getValue().toString();

                        tv_chat_custom_name.setText(name);

                        String lastseen_string = dataSnapshot.child("online").getValue().toString();
                        if (lastseen_string.equals("true"))
                            tv_chat_custom_lastseen.setText("online");
                        else {
                            long lastseen_long = Long.parseLong(lastseen_string);
                            GetTimeAgo getTimeAgo = new GetTimeAgo();
                            String lastseen_ago = getTimeAgo.getTimeAgo(lastseen_long, getApplicationContext());

                            tv_chat_custom_lastseen.setText(lastseen_ago);
                        }

                        String image = dataSnapshot.child("image").getValue().toString();

                        if (!ChatActivity.this.isDestroyed() && !ChatActivity.this.isFinishing())
                            Glide.with(getApplicationContext()).load(image)
                                    .thumbnail(0.5f)
                                    .crossFade()
                                    .bitmapTransform(new GlideCircleTransformation(getApplicationContext()))
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(R.drawable.ic_user)
                                    .into(iv_chat_custom);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

        et_chat_typing = findViewById(R.id.et_chat_typing);
        ib_chat_add = findViewById(R.id.ib_chat_add);
        ib_chat_send = findViewById(R.id.ib_chat_send);

        db_ref.child("Chats").child(current_user_id).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChild(uid)) {
                            Map chat_add_map = new HashMap();
                            chat_add_map.put("seen", false);
                            chat_add_map.put("timestamp", ServerValue.TIMESTAMP);

                            Map chat_user_map = new HashMap();
                            chat_user_map.put("Chats/" + current_user_id + "/" + uid, chat_add_map);
                            chat_user_map.put("Chats/" + uid + "/" + current_user_id, chat_add_map);

                            db_ref.updateChildren(chat_user_map, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError != null)
                                        Log.d("check", "chats: " + databaseError.getMessage().toString());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

        ib_chat_send.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String message = et_chat_typing.getText().toString();
                        if (!TextUtils.isEmpty(message)) {
                            String sender_side = "messages/" + current_user_id + "/" + uid;
                            String receiver_side = "messages/" + uid + "/" + current_user_id;
                            String message_id = db_ref.child("messages").child(current_user_id).child(uid)
                                    .push().getKey();
                            Map message_map = new HashMap();
                            message_map.put("message", message);
                            message_map.put("type", "text");
                            message_map.put("seen", false);
                            message_map.put("timestamp", ServerValue.TIMESTAMP);
                            message_map.put("from", current_user_id);

                            Map user_message_map = new HashMap();
                            user_message_map.put(sender_side + "/" + message_id, message_map);
                            user_message_map.put(receiver_side + "/" + message_id, message_map);

                            //**********************************************************************//

                            String sender_side_chats = "Chats/"+current_user_id+"/"+uid;
                            String receiver_side_chats = "Chats/"+uid+"/"+current_user_id;

                            Map sender_chat_map = new HashMap();
                            sender_chat_map.put("seen", true);
                            sender_chat_map.put("timestamp", ServerValue.TIMESTAMP);

                            Map receiver_chat_map = new HashMap();
                            receiver_chat_map.put("seen", false);
                            receiver_chat_map.put("timestamp", ServerValue.TIMESTAMP);

                            Map duplex_chat_map = new HashMap();
                            duplex_chat_map.put(sender_side_chats+"/", sender_chat_map);
                            duplex_chat_map.put(receiver_side_chats+"/", receiver_chat_map);

                            db_ref.updateChildren(duplex_chat_map);

                            //**********************************************************************//

                            db_ref.updateChildren(user_message_map, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError != null)
                                        Log.d("check", "messages: " + databaseError.getMessage());
                                    et_chat_typing.setText("");
                                }
                            });
                        }
                    }
                }
        );

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                curent_page++;
                item_pos = 0;
                loadMoreMessages();
            }
        });

        ib_chat_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent GalleryIntent = new Intent();
                GalleryIntent.setType("image/*");
                GalleryIntent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(GalleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri gallery_image_uri = data.getData();

            final String sender_ref = "messages/" + current_user_id + "/" + uid;
            final String receiver_ref = "messages/" + uid + "/" + current_user_id;

            DatabaseReference push_ref = db_ref.child("messages").child(current_user_id).child(uid)
                    .push();
            final String push_id = push_ref.getKey();

            StorageReference filepath = FirebaseStorage.getInstance().getReference()
                    .child("message_images").child(push_id + ".jpg");

            if (gallery_image_uri != null)
                filepath.putFile(gallery_image_uri).addOnCompleteListener(
                        new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    String dl_url = task.getResult().getDownloadUrl().toString();
                                    Map message_map = new HashMap();
                                    message_map.put("message", dl_url);
                                    message_map.put("type", "image");
                                    message_map.put("seen", false);
                                    message_map.put("timestamp", ServerValue.TIMESTAMP);
                                    message_map.put("from", current_user_id);

                                    Map user_message_map = new HashMap();
                                    user_message_map.put(sender_ref+"/"+push_id, message_map);
                                    user_message_map.put(receiver_ref+"/"+push_id, message_map);

                                    db_ref.updateChildren(user_message_map, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        }
                                    });

                                }
                            }
                        }
                );

        }

    }

    private void loadMoreMessages() {
        DatabaseReference messages_ref = db_ref.child("messages").child(current_user_id).child(uid);
        Query messages_query = messages_ref.orderByKey().endAt(last_key).limitToLast(10);

        messages_query.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        count = (int) dataSnapshot.getChildrenCount();
                        if (count != 10 || count == 1) {
                            swipe.setEnabled(false);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

        messages_query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                String message_key = dataSnapshot.getKey();

                if (!prev_key.equals(message_key)) {//if the first element is not the first element
                    message_list.add(item_pos++, message);
                } else {//if the first element is again the first element
                    prev_key = last_key;
                }

                if (item_pos == 1) {
                    last_key = message_key;
                }

                adapter.notifyDataSetChanged();
                swipe.setRefreshing(false);
                linearLayoutManager.scrollToPosition(0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages() {

        DatabaseReference messages_ref = db_ref.child("messages").child(current_user_id).child(uid);
        Query messages_query = messages_ref.limitToLast(TOTAL_ITEMS_TO_LOAD);//the last ten

        messages_query.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Messages message = dataSnapshot.getValue(Messages.class);

                        item_pos++;

                        if (item_pos == 1) {
                            last_key = dataSnapshot.getKey();
                            prev_key = last_key;
                        }

                        message_list.add(message);
                        adapter.notifyDataSetChanged();
                        linearLayoutManager.scrollToPosition(message_list.size() - 1);
                        //linearLayoutManager.smoothScrollToPosition(rv_chat, null, adapter.getItemCount());//$//
                        swipe.setRefreshing(false);//recycler view starts at position 1
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        item_pos = 0;
        count = 0;
        db_ref.child("Users").child(current_user_id).child("online").setValue("true");
    }

    @Override
    protected void onPause() {
        super.onPause();
        db_ref.child("Users").child(current_user_id).child("online").setValue(ServerValue.TIMESTAMP);
    }
}
