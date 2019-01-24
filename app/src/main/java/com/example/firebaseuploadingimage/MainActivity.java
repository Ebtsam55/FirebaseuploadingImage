package com.example.firebaseuploadingimage;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private Button galleryButton;
    private Button cameraButton;
    private StorageReference storage;
    private static final int GALLERY_INTENT = 22;
    private static final int CAMERA_INTENT = 25;
    private ProgressDialog mProgressDialog;
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storage = FirebaseStorage.getInstance().getReference();

        galleryButton = findViewById(R.id.gallery_button);
        cameraButton = findViewById(R.id.camera_button);
        imageView = findViewById(R.id.image);

        mProgressDialog = new ProgressDialog(this);

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_INTENT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {

            Uri uri = data.getData();

            if (uri != null) {

                mProgressDialog.setMessage("uploading...");
                mProgressDialog.show();


                StorageReference chiledRef = storage.child("Photos").child("Gallery Photos").child(uri.getLastPathSegment());

                chiledRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        mProgressDialog.dismiss();

                        Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.with(MainActivity.this).load(uri).fit().centerCrop().into(imageView);
                            }
                        });
                        Toast.makeText(MainActivity.this, "uploaded Done !", Toast.LENGTH_SHORT).show();

                    }
                });

            }
        }



         //  When we capture the image from Camera in Android then Uri or data.getdata() becomes null
        // to resolve this issue :
        // Retrieve the Uri path from the Bitmap Image


        if (requestCode == CAMERA_INTENT && resultCode == RESULT_OK) {

            Bitmap bitmap=null;

            if(data.getData()==null){
                bitmap = (Bitmap)data.getExtras().get("data");
            }else{
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Uri uri = getImageUri(getApplicationContext(),bitmap);


            mProgressDialog.setMessage("uploading...");
                mProgressDialog.show();



                StorageReference cameraRef = storage.child("Photos").child("Camera Photos").child(uri.getLastPathSegment());

                cameraRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        mProgressDialog.dismiss();

                        Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.with(MainActivity.this).load(uri).fit().centerCrop().into(imageView);
                            }
                        });


                        Toast.makeText(MainActivity.this, "uploaded Done !", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    }

