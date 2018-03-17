package com.elite.mychat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.FirebaseDatabase;

public class StartActivity extends AppCompatActivity {

    private Button btn_start_new, btn_start_got;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btn_start_new=findViewById(R.id.btn_start_new);
        btn_start_got=findViewById(R.id.btn_start_got);

        btn_start_new.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent RegisterIntent=new Intent(StartActivity.this, RegisterActivity.class);
                        startActivity(RegisterIntent);
                    }
                }
        );

        btn_start_got.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent LoginIntent=new Intent(StartActivity.this, LoginActivity.class);
                        startActivity(LoginIntent);
                    }
                }
        );

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("check", "StartActivity: onStart");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.i("check", "StartActivity: onResume");
    }
}
