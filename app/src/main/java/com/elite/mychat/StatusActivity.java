package com.elite.mychat;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private EditText et_status_status;
    private Button btn_status;
    private ProgressBar pb_status;
    private DatabaseReference users_db_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        String uid=null;
        if(user!=null)
            uid=user.getUid();
        users_db_ref=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        Toolbar toolbar=findViewById(R.id.status_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        et_status_status=findViewById(R.id.et_status_status);
        btn_status=findViewById(R.id.btn_status);
        pb_status=findViewById(R.id.pb_status);

        String status=getIntent().getStringExtra("status");
        et_status_status.setText(status);

        btn_status.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String status=et_status_status.getText().toString();
                        pb_status.setVisibility(View.VISIBLE);
                        users_db_ref.child("status").setValue(status).addOnCompleteListener(
                                new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            pb_status.setVisibility(View.GONE);
                                            Toast.makeText(StatusActivity.this, "Status updated!", Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                        else {
                                            pb_status.setVisibility(View.GONE);
                                            Toast.makeText(StatusActivity.this, "Error!", Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    }
                                }
                        );

                    }
                }
        );

    }
}
