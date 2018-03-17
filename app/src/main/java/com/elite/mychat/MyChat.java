package com.elite.mychat;

import android.app.Application;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by evk29 on 25-01-2018.
 */

public class MyChat extends Application {
    private DatabaseReference users_db_ref;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true); //Firebase Offline Capabilities

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String current_user_id = user.getUid();

            users_db_ref = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);

            users_db_ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        users_db_ref.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);//works when app is closed not minimized
                        //users_db_ref.child("online").setValue(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

}
