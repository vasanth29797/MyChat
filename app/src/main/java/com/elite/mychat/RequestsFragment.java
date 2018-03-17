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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private DatabaseReference db_ref, users_db_ref, frndreq_db_ref;
    private FirebaseAuth auth;
    private FirebaseUser current_user;
    private String current_user_id;
    private RecyclerView rv_requests;


    public RequestsFragment() {
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
        users_db_ref.keepSynced(true);

        frndreq_db_ref = db_ref.child("frndreq").child(current_user_id);
        frndreq_db_ref.keepSynced(true);

        Log.i("check", "a");

        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        rv_requests = view.findViewById(R.id.rv_requests);
        rv_requests.setHasFixedSize(true);

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setStackFromEnd(true);
        manager.setReverseLayout(true);

        rv_requests.setLayoutManager(manager);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(rv_requests.getContext(),
                new LinearLayoutManager(getContext()).getOrientation());

        rv_requests.addItemDecoration(itemDecoration);

        Log.i("check", "b");

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Query old_to_new_query = frndreq_db_ref.orderByChild("timestamp");

        Log.i("check", "1");

        FirebaseRecyclerAdapter<Requests, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>(
                        Requests.class,
                        R.layout.my_friend_request,
                        RequestsViewHolder.class,
                        old_to_new_query
                ) {
                    @Override
                    protected void populateViewHolder(final RequestsViewHolder viewHolder, final Requests model, int position) {
                        Log.i("check", "2");

                        if (model.getRequest_type().equals("received")) {

                            final String uid = getRef(position).getKey();

                            users_db_ref.child(uid).addValueEventListener(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Log.i("check", "5");
                                            String name = dataSnapshot.child("name").getValue().toString();
                                            String status = dataSnapshot.child("status").getValue().toString();
                                            String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                                            viewHolder.setName(name);
                                            viewHolder.setStatus(status);
                                            viewHolder.setImage(getContext(), thumb_image);

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    }
                            );

                            viewHolder.btn_my_frndreq_accept.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String frnd_since = DateFormat.getDateTimeInstance().format(new Date());

                                            Map map = new HashMap();
                                            map.put("frndreq/" + current_user_id + "/" + uid + "/request_type", null);
                                            map.put("frndreq/" + current_user_id + "/" + uid + "/timestamp", null);
                                            map.put("frndreq/" + uid + "/" + current_user_id + "/request_type", null);
                                            map.put("frndreq/" + uid + "/" + current_user_id + "/timestamp", null);

                                            map.put("friends/" + current_user_id + "/" + uid + "/date", frnd_since);
                                            map.put("friends/" + uid + "/" + current_user_id + "/date", frnd_since);
                                            db_ref.updateChildren(map, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    Toast.makeText(getContext(), "You're friends now :)", Toast.LENGTH_SHORT)
                                                    .show();
                                                }
                                            });
                                        }
                                    }
                            );

                            viewHolder.btn_my_frndreq_decline.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            Map map = new HashMap();
                                            map.put("frndreq/" + current_user_id + "/" + uid + "/request_type", null);
                                            map.put("frndreq/" + current_user_id + "/" + uid + "/timestamp", null);
                                            map.put("frndreq/" + uid + "/" + current_user_id + "/request_type", null);
                                            map.put("frndreq/" + uid + "/" + current_user_id + "/timestamp", null);
                                            db_ref.updateChildren(map, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    Toast.makeText(getContext(), "Request denied successfully :)", Toast.LENGTH_SHORT)
                                                            .show();
                                                }
                                            });

                                        }
                                    }
                            );


                        } else {
                            viewHolder.view.setVisibility(View.GONE);
                            viewHolder.view.setLayoutParams(new RecyclerView.LayoutParams(0, 0));//tells the parent view that
                            //how its width and height gonna be
                        }

                    }
                };
        Log.i("check", "6");
        rv_requests.setAdapter(adapter);
        Log.i("check", "7");
    }


    public static class RequestsViewHolder extends RecyclerView.ViewHolder {//use static or else u will get nosuchmethod exception
        View view;
        Button btn_my_frndreq_accept, btn_my_frndreq_decline;

        public RequestsViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            Log.i("check", "8");
            btn_my_frndreq_accept = view.findViewById(R.id.btn_my_frndreq_accept);
            btn_my_frndreq_decline = view.findViewById(R.id.btn_my_frndreq_decline);
        }

        public void setName(String name) {
            TextView tv_friend_name = view.findViewById(R.id.tv_my_frndreq_name);
            tv_friend_name.setText(name);
        }

        public void setImage(Context context, String thumb_image) {
            ImageView iv_friend = view.findViewById(R.id.iv_my_frndreq);//used different id :/
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

        public void setStatus(String status) {
            TextView tv_friend_status = view.findViewById(R.id.tv_my_frndreq_status);
            tv_friend_status.setText(status);
        }

    }
}
