package com.elite.mychat;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private DatabaseReference db_ref, users_db_ref, chats_db_ref, message_db_ref;
    private FirebaseAuth auth;
    private FirebaseUser current_user;
    private String current_user_id;
    private RecyclerView rv_chats;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        auth = FirebaseAuth.getInstance();
        current_user = auth.getCurrentUser();
        current_user_id = current_user.getUid();

        db_ref = FirebaseDatabase.getInstance().getReference();

        users_db_ref = db_ref.child("Users");
        chats_db_ref = db_ref.child("Chats").child(current_user_id);
        chats_db_ref.keepSynced(true);
        message_db_ref = db_ref.child("messages").child(current_user_id);
        message_db_ref.keepSynced(true);

        Log.i("check", "a");

        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        rv_chats = view.findViewById(R.id.rv_chats);
        rv_chats.setHasFixedSize(true);

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setStackFromEnd(true);
        manager.setReverseLayout(true);

        rv_chats.setLayoutManager(manager);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(rv_chats.getContext(),
                new LinearLayoutManager(getContext()).getOrientation());

        rv_chats.addItemDecoration(itemDecoration);

        Log.i("check", "b");

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Query old_to_new_query = chats_db_ref.orderByChild("timestamp");

        Log.i("check", "1");

        FirebaseRecyclerAdapter<Conversation, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Conversation, ChatsViewHolder>(
                        Conversation.class,
                        R.layout.my_friend,
                        ChatsViewHolder.class,
                        old_to_new_query
                ) {
                    @Override
                    protected void populateViewHolder(final ChatsViewHolder viewHolder, final Conversation model, int position) {
                        Log.i("check", "2");

                        final String uid = getRef(position).getKey();

                        viewHolder.view.setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Log.i("check", "3");
                                        Intent ChatIntent = new Intent(getContext(), ChatActivity.class);
                                        ChatIntent.putExtra("uid", uid);
                                        startActivity(ChatIntent);
                                    }
                                }
                        );

                        Query last_message_query = message_db_ref.child(uid).limitToLast(1);
                        last_message_query.addChildEventListener(
                                new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        Log.i("check", "4");
                                        String message = dataSnapshot.child("message").getValue().toString();
                                        viewHolder.setMessage(message, model.isSeen());
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

                        users_db_ref.child(uid).addValueEventListener(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Log.i("check", "5");
                                        String name = dataSnapshot.child("name").getValue().toString();
                                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                                        viewHolder.setName(name);
                                        viewHolder.setImage(getContext(), thumb_image);

                                        if (dataSnapshot.hasChild("online")) {
                                            String online = dataSnapshot.child("online").getValue().toString();
                                            viewHolder.setOnline(online);
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                }
                        );

                    }
                };
        Log.i("check", "6");
        rv_chats.setAdapter(adapter);
        Log.i("check", "7");
    }


    public static class ChatsViewHolder extends RecyclerView.ViewHolder {//use static or else u will get nosuchmethod exception
        View view;

        public ChatsViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            Log.i("check", "8");
        }

        public void setName(String name) {
            TextView tv_friend_name = view.findViewById(R.id.tv_friend_name);
            tv_friend_name.setText(name);
        }

        public void setImage(Context context, String thumb_image) {
            ImageView iv_friend = view.findViewById(R.id.iv_friend);//used different id :/
            if (context != null) {
                Glide.with(context).load(thumb_image)
                        .thumbnail(0.5f)
                        .crossFade()
                        .bitmapTransform(new GlideCircleTransformation(context))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_user)
                        .into(iv_friend);
            }
        }

        public void setMessage(String message, Boolean isSeen) {
            TextView tv_friend_message = view.findViewById(R.id.tv_friend_status);

            if (message.equals(null)) {
                tv_friend_message.setVisibility(View.GONE);
            } else if (message.startsWith("http"))
                tv_friend_message.setText("***Image***");
            else
                tv_friend_message.setText(message);

            if (message.equals(null)) {
                tv_friend_message.setVisibility(View.GONE);
            }

            if (!isSeen)
                tv_friend_message.setTypeface(tv_friend_message.getTypeface(), Typeface.BOLD);
            else
                tv_friend_message.setTypeface(tv_friend_message.getTypeface(), Typeface.NORMAL);

        }

        public void setOnline(String online) {
            ImageView iv_friend_online = view.findViewById(R.id.iv_friend_online);
            if (online.equals("true")) {
                iv_friend_online.setVisibility(View.VISIBLE);
            } else {
                iv_friend_online.setVisibility(View.INVISIBLE);
            }
        }

    }
}
