package com.elite.mychat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;
    private StorageReference profile_images_sto_ref, thumb_images_sto_ref;
    private String uid;
    private ProgressBar pb_settings;
    private DatabaseReference users_db_ref;
    private int flag = 0;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = null;
        if (user != null) {
            uid = user.getUid();
        }
        users_db_ref = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(uid);
        users_db_ref.keepSynced(true);

        profile_images_sto_ref = FirebaseStorage.getInstance().getReference().child("profile_images");
        thumb_images_sto_ref = profile_images_sto_ref.child("thumb_images");//thumb_images in profile_images(Folder)

        final TextView tv_settings_name, tv_settings_status;
        final ImageView iv_settings;
        Button btn_settings_image, btn_settings_status;

        btn_settings_image = findViewById(R.id.btn_settings_image);
        btn_settings_status = findViewById(R.id.btn_settings_status);

        tv_settings_name = findViewById(R.id.tv_settings_name);
        tv_settings_status = findViewById(R.id.tv_settings_status);
        iv_settings = findViewById(R.id.iv_settings);
        pb_settings = findViewById(R.id.pb_settings);

        btn_settings_status.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String status = tv_settings_status.getText().toString();
                        Intent StatusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                        StatusIntent.putExtra("status", status);
                        startActivity(StatusIntent);
                    }
                }
        );

        btn_settings_image.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent GalleryIntent = new Intent();
                        GalleryIntent.setType("image/*");
                        GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);//shows all options
                        startActivityForResult(Intent.createChooser(GalleryIntent, "SELECT AN IMAGE"), GALLERY_REQUEST);
                    }
                }
        );


        users_db_ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String name = dataSnapshot.child("name").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();
                        String image = dataSnapshot.child("image").getValue().toString();
                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                        if (!image.equals("default")) {
                            if (!SettingsActivity.this.isFinishing() && !SettingsActivity.this.isDestroyed())
                                Glide.with(SettingsActivity.this)
                                        .load(image)
                                        .thumbnail(0.5f)
                                        .crossFade()
                                        .bitmapTransform(new GlideCircleTransformation(SettingsActivity.this))
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .placeholder(R.drawable.ic_user)//displays a dummy image before loading finishes
                                        .into(iv_settings);

                            /*if(flag==1) {
                                pb_settings.setVisibility(View.GONE);
                                Toast.makeText(SettingsActivity.this, "Images upload successful!", Toast.LENGTH_SHORT).show();
                                flag=0;
                            }*/
                        }

                        tv_settings_name.setText(name);
                        tv_settings_status.setText(status);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            Uri gallery_uri = data.getData();
            CropImage.activity(gallery_uri)
                    .setAspectRatio(1, 1)
                    .start(SettingsActivity.this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                pb_settings.setVisibility(View.VISIBLE);

                Uri crop_uri = result.getUri();

                File file_thumb = new File(crop_uri.getPath());//Uri to File

                Bitmap bitmap_thumb = new Compressor(this)//File to Bitmap
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(file_thumb);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap_thumb.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] byteArray_thumb = baos.toByteArray();//Bitmap to byte array

                thumb_images_sto_ref = thumb_images_sto_ref.child(uid + ".jpg");
                profile_images_sto_ref = profile_images_sto_ref.child(uid + ".jpg");

                profile_images_sto_ref.putFile(crop_uri).addOnCompleteListener(//Profile image upload
                        new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> profile_image_upload_task) {
                                if (profile_image_upload_task.isSuccessful()) {
                                    final String profile_image_dl_url = profile_image_upload_task.getResult().getDownloadUrl().toString();//getting profile image dl url
                                    UploadTask uploadTask = thumb_images_sto_ref.putBytes(byteArray_thumb);//Thumb image upload
                                    uploadTask.addOnCompleteListener(
                                            new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_image_upload_task) {
                                                    if (thumb_image_upload_task.isSuccessful()) {
                                                        String thumb_image_dl_url = thumb_image_upload_task.getResult().getDownloadUrl().toString();//getting thumb image dl url
                                                        Map images_map = new HashMap();
                                                        images_map.put("image", profile_image_dl_url);
                                                        images_map.put("thumb_image", thumb_image_dl_url);
                                                        users_db_ref.updateChildren(images_map).addOnCompleteListener(//update n not set
                                                                new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            pb_settings.setVisibility(View.GONE);
                                                                            Toast.makeText(SettingsActivity.this, "Images upload successful!", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                }
                                                        );
                                                    } else {
                                                        pb_settings.setVisibility(View.GONE);
                                                        Toast.makeText(SettingsActivity.this, "Thumb image upload error :/", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                    );
                                } else {
                                    pb_settings.setVisibility(View.GONE);
                                    Toast.makeText(SettingsActivity.this, "Profile image upload error :/", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                );

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception exception = result.getError();
                Toast.makeText(SettingsActivity.this, exception.toString(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    public static String random() {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        int randomLength = random.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (random.nextInt(96) + 32);
            stringBuilder.append(tempChar);
        }
        return stringBuilder.toString();
    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("online")
                .setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("online")
                .setValue("true");
    }

}
