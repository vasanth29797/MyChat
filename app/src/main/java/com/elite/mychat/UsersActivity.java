package com.elite.mychat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class UsersActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private DatabaseReference users_db_ref;
    private FirebaseUser current_user;
    private String current_user_id;
    private String current_user_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        current_user_id = current_user.getUid();

        users_db_ref = FirebaseDatabase.getInstance().getReference().child("Users");
        users_db_ref.keepSynced(true);

        users_db_ref.child(current_user_id).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("name"))
                            current_user_name = dataSnapshot.child("name").getValue().toString();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

        toolbar = findViewById(R.id.users_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.rv_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(current_user_id)
                .child("online")
                .setValue("true");

        FirebaseRecyclerAdapter<Users, usersViewHolder> adapter
                = new FirebaseRecyclerAdapter<Users, usersViewHolder>(
                Users.class,
                R.layout.my_user,
                usersViewHolder.class,
                users_db_ref
        ) {
            @Override
            protected void populateViewHolder(usersViewHolder viewHolder, Users model, int position) {

                if (!model.getName().equals(current_user_name)) {

                    viewHolder.setName(model.getName());
                    viewHolder.setStatus(model.getStatus());
                    viewHolder.setImage(getApplicationContext(), model.getThumb_image());

                    final String uid = getRef(position).getKey();

                    viewHolder.view.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent ProfileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                                    ProfileIntent.putExtra("uid", uid);
                                    startActivity(ProfileIntent);
                                }
                            }
                    );

                } else {//here view is my_user.xml
                    viewHolder.view.setVisibility(View.GONE);
                    viewHolder.view.setLayoutParams(new RecyclerView.LayoutParams(0, 0));//changing the height of the parent view
                }


            }
        };

        recyclerView.setAdapter(adapter);

    }

    public static class usersViewHolder extends RecyclerView.ViewHolder {

        View view;

        public usersViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setName(String name) {
            TextView tv_user_name = view.findViewById(R.id.tv_user_name);
            tv_user_name.setText(name);
        }

        public void setStatus(String status) {
            TextView tv_user_status = view.findViewById(R.id.tv_user_status);
            tv_user_status.setText(status);
        }

        public void setImage(Context context, String image) {
            ImageView iv_user = view.findViewById(R.id.iv_user);
            Glide.with(context).load(image)
                    .thumbnail(0.5f)
                    .crossFade()
                    .bitmapTransform(new GlideCircleTransformation(context))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_user)
                    .into(iv_user);
        }

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
