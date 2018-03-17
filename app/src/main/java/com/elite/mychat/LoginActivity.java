package com.elite.mychat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout til_login_email, til_login_pwd;
    private Button btn_login;
    private ProgressBar pb_login;
    private FirebaseAuth auth;
    private DatabaseReference users_db_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth=FirebaseAuth.getInstance();

        users_db_ref=FirebaseDatabase.getInstance().getReference().child("Users");
        users_db_ref.keepSynced(true);

        til_login_email=findViewById(R.id.til_login_email);
        til_login_pwd=findViewById(R.id.til_login_pwd);
        btn_login=findViewById(R.id.btn_login);
        pb_login=findViewById(R.id.pb_login);

        toolbar=findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btn_login.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String email=til_login_email.getEditText().getText().toString();
                        String pwd=til_login_pwd.getEditText().getText().toString();

                        if(TextUtils.isEmpty(email) && TextUtils.isEmpty(pwd)) {
                            Toast.makeText(LoginActivity.this, "Field(s) are empty.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            pb_login.setVisibility(View.VISIBLE);
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            login_user(email, pwd);
                        }

                    }
                }
        );

    }

    private void login_user(String email, String pwd) {

        auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            pb_login.setVisibility(View.GONE);

                            String current_user_id=auth.getCurrentUser().getUid();
                            String device_token= FirebaseInstanceId.getInstance().getToken();

                            users_db_ref.child(current_user_id).child("device_token").setValue(device_token)
                                    .addOnSuccessListener(
                                            new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Intent MainIntent=new Intent(LoginActivity.this, MainActivity.class);
                                                    MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(MainIntent);
                                                    finish();
                                                }
                                            }
                                    );
                        }
                        else {
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            pb_login.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Cannot sign in. Please try again.", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                }
        );

    }
}
