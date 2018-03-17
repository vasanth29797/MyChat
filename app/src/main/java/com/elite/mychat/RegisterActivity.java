package com.elite.mychat;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tip_reg_name, tip_reg_email, tip_reg_pwd;
    private Button btn_reg;
    private FirebaseAuth auth;
    private String display_name, email, pwd;
    private Toolbar toolbar;
    private ProgressBar pb_reg;
    private DatabaseReference users_db_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth=FirebaseAuth.getInstance();

        toolbar=findViewById(R.id.register_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tip_reg_name=findViewById(R.id.til_reg_name);
        tip_reg_email=findViewById(R.id.til_reg_email);
        tip_reg_pwd=findViewById(R.id.til_reg_pwd);
        btn_reg=findViewById(R.id.btn_reg);
        pb_reg=findViewById(R.id.pb_reg);

        btn_reg.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        display_name=tip_reg_name.getEditText().getText().toString();
                        email=tip_reg_email.getEditText().getText().toString();
                        pwd=tip_reg_pwd.getEditText().getText().toString();

                        if(TextUtils.isEmpty(display_name)||TextUtils.isEmpty(email)
                                ||TextUtils.isEmpty(pwd)) {
                            Toast.makeText(RegisterActivity.this, "Field(s) are empty.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            pb_reg.setVisibility(View.VISIBLE);
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            register_user(display_name, email, pwd);
                        }
                    }
                }
        );

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {//to save values since orientation change recreates the activity
        super.onSaveInstanceState(outState);

        display_name=tip_reg_name.getEditText().getText().toString();
        email=tip_reg_email.getEditText().getText().toString();
        pwd=tip_reg_pwd.getEditText().getText().toString();

        outState.putString("display_name", display_name);
        outState.putString("email", email);
        outState.putString("pwd", pwd);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        tip_reg_name.getEditText().setText(savedInstanceState.getString("display_name"));
        tip_reg_email.getEditText().setText(savedInstanceState.getString("email"));
        tip_reg_pwd.getEditText().setText(savedInstanceState.getString("pwd"));

    }

    private void register_user(final String display_name, String email, String pwd) {
        Log.i("check", "name: "+display_name);
        auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {

                            FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                            String uid=null;
                            if(user!=null) {
                                uid=user.getUid();
                            }
                            users_db_ref= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            String device_token= FirebaseInstanceId.getInstance().getToken();

                            HashMap<String, String> map=new HashMap<>();
                            map.put("name", display_name);
                            map.put("status", "Hi there, I'm using MyChat app.");
                            map.put("image", "default");
                            map.put("thumb_image", "default");
                            map.put("device_token", device_token);
                            users_db_ref.setValue(map);

                            pb_reg.setVisibility(View.GONE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Intent MainIntent=new Intent(RegisterActivity.this, MainActivity.class);
                            MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(MainIntent);
                            finish();
                        }
                        else {
                            pb_reg.setVisibility(View.GONE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                            String error="";
                            try {
                                throw task.getException();
                            }
                            catch (FirebaseAuthWeakPasswordException e) {
                                error="Weak Password!";
                            }
                            catch (FirebaseAuthInvalidCredentialsException e) {
                                error="Invalid Email!";
                            }
                            catch (FirebaseAuthUserCollisionException e) {
                                error="Existing Account!";
                            }
                            catch (Exception e) {
                                error="Unknown error!";
                                e.printStackTrace();
                            }

                            Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }
}
