package com.example.comp39x;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;

public class groupactivity extends AppCompatActivity {

    private ActionBar actionBar;
    private FirebaseAuth firebaseAuth;
    private ImageView ic_group;
    private EditText groupName, groupdetails;
    private ImageButton imageButton2;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    private String[] cameraPermissions;
    private String[] storagePermissions;
    private ProgressDialog progressDialog;
    private Uri image_uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupactivity);
        ic_group = findViewById(R.id.imageView2);
        groupdetails = findViewById(R.id.groupdetails);
        groupName = findViewById(R.id.groupName);
        imageButton2 = findViewById(R.id.imageButton2);


        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("Create Group");

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();

        ic_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();

            }
        });

        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_create_group();

            }
        });

    }
    private void start_create_group(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Group");

        final String groupTitle = groupName.getText().toString().trim();
        final String groupdetail = groupdetails.getText().toString().trim();

        if(TextUtils.isEmpty(groupTitle)){
            Toast.makeText(this,"Please enter a group title", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();
        final String g_timestamp = ""+System.currentTimeMillis();
        if(image_uri == null){
            createGroup(""+g_timestamp,""+groupTitle,""+groupdetail);
        }
        else{
            String fileNamePath = "Group_IMGS/"+"image"+ g_timestamp;
            StorageReference storageRef = FirebaseStorage.getInstance().getReference(fileNamePath);
            storageRef.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!p_uriTask.isSuccessful());
                    Uri p_downloadUri = p_uriTask.getResult();
                    if(p_uriTask.isSuccessful()){
                        createGroup(""+g_timestamp,""+groupTitle,""+groupdetail);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        }
    }
    private void createGroup(final String g_timestamp, String groupTitle, String groupdetail){
        final GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(groupactivity.this);
        final String id1 = acct.getId();
        final HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("groupID",""+g_timestamp);
        hashMap.put("groupTitle",""+groupTitle);
        hashMap.put("groupdetail",""+groupdetail);
        hashMap.put("timestamp",""+g_timestamp);
        hashMap.put("createdby",""+id1);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(g_timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                HashMap<String , String> hashMap1 = new HashMap<>();
                hashMap1.put("udi", id1);
                hashMap1.put("Role","creator");
                hashMap1.put("timestamp", g_timestamp);

                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
                ref1.child(g_timestamp).child("members").child(id1).setValue(hashMap1).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(groupactivity.this,"Group created successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(groupactivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }



    private void showImagePickDialog() {
        String[] options = {"Camera","Gallery"};

        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Pick an Image").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    if(!checkCameraPermissions()){
                        requestCameraPermissions();
                    }
                    else{
                        pickFromCamera();
                    }
                }
                else{
                    if(!checkStoragePermissions()){
                        requestStoragePermissions();
                    }
                    else{
                        pickFromGallery();
                    }
                }
            }
        }).show();
    }

    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
    }
    private void pickFromCamera(){
        ContentValues ex = new ContentValues();
        ex.put(MediaStore.Images.Media.TITLE,"Group Image Icon Title");
        ex.put(MediaStore.Images.Media.DESCRIPTION,"Group Image Icon Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ex);
        Intent  intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }
    private boolean checkStoragePermissions(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermissions(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);

    }
    private boolean checkCameraPermissions(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result2 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result2;
    }
    private void requestCameraPermissions(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);

    }



    public boolean onSupportNavigateUP(){
        onBackPressed();
        return super.onSupportNavigateUp();
    }
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults){
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }
                    else{
                        Toast.makeText(this,"Camera and storage permission required",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                    pickFromGallery();
                    }
                    }else{
                        Toast.makeText(this,"storage permission required",Toast.LENGTH_SHORT).show();
                    }

                }
            }
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);

    }
    protected void onActivityResult(int requestCode,int resultCode,@NonNull Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();
                ic_group.setImageURI(image_uri);
            }
            else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                ic_group.setImageURI(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


}
