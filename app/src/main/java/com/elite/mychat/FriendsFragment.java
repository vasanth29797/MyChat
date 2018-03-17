package com.elite.mychat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private DatabaseReference friends_db_ref, users_db_ref;
    private View view;
    private RecyclerView rv_friends;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String current_user_id = user.getUid();

        friends_db_ref = FirebaseDatabase.getInstance().getReference().child("friends").child(current_user_id);
        friends_db_ref.keepSynced(true);

        users_db_ref = FirebaseDatabase.getInstance().getReference().child("Users");
        users_db_ref.keepSynced(true);

        view = inflater.inflate(R.layout.fragment_friends, container, false);

        rv_friends = view.findViewById(R.id.rv_friends);
        rv_friends.setHasFixedSize(true);
        rv_friends.setLayoutManager(new LinearLayoutManager(getContext()));//notice context

        DividerItemDecoration itemDecoration = new DividerItemDecoration(rv_friends.getContext(),
                new LinearLayoutManager(getContext()).getOrientation());

        rv_friends.addItemDecoration(itemDecoration);
        //rv_friends.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, friendsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Friends, friendsViewHolder>(
                Friends.class,
                R.layout.my_friend,
                friendsViewHolder.class,
                friends_db_ref
        ) {
            @Override
            protected void populateViewHolder(final friendsViewHolder viewHolder, final Friends model, int position) {
                viewHolder.setDate(model.getDate());
                final String uid = getRef(position).getKey();

                users_db_ref.child(uid).addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    String name = dataSnapshot.child("name").getValue().toString();
                                    String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                                    if (dataSnapshot.hasChild("online")) {//since not all users have online field; they acquire it
                                        // by opening the main activity and other activities
                                        String online = dataSnapshot.child("online").getValue().toString();
                                        viewHolder.setOnline(online);
                                    }

                                    viewHolder.setName(name);
                                    viewHolder.setImage(getContext(), thumb_image);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

                viewHolder.view.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (i == 0) {
                                            Intent ProfileIntent = new Intent(getContext(), ProfileActivity.class);
                                            ProfileIntent.putExtra("uid", uid);
                                            startActivity(ProfileIntent);
                                        } else if (i == 1) {
                                            Intent ProfileIntent = new Intent(getContext(), ChatActivity.class);
                                            ProfileIntent.putExtra("uid", uid);
                                            startActivity(ProfileIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        }
                );

            }
        };

        rv_friends.setAdapter(adapter);
    }

    public static class friendsViewHolder extends RecyclerView.ViewHolder {
        View view;

        public friendsViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setDate(String date) {
            TextView tv_friend_date = view.findViewById(R.id.tv_friend_status);
            tv_friend_date.setText(date);
        }

        public void setName(String name) {
            TextView tv_friend_name = view.findViewById(R.id.tv_friend_name);
            tv_friend_name.setText(name);
        }

        public void setImage(Context context, String thumb_image) {
            ImageView iv_friend = view.findViewById(R.id.iv_friend);
            if (context != null)
                Glide.with(context).load(thumb_image)
                        .thumbnail(0.5f)
                        .crossFade()
                        .bitmapTransform(new GlideCircleTransformation(context))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_user)
                        .into(iv_friend);
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
