package com.example.roomyrent.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.example.roomyrent.R;
import com.example.roomyrent.Utils;
import com.example.roomyrent.databinding.ActivityProfileEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class ProfileEditActivity extends AppCompatActivity {
    private ActivityProfileEditBinding binding;
    private static final String TAG="PROFILE_EDIT_TAG";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private Uri imageUri = null;
    private String myUserType="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        loadMyInfo();
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.profileImagePickFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickDialog();
            }
        });
        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
        binding.phoneNumberEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.phoneNumberEt.setCursorVisible(true);
            }
        });
    }
    private String name = "";
    private String dob = "";
    private String email = "";
    private String phoneCode = "";
    private String phoneNumber = "";
    private void validateData(){
        name= binding.nameEt.getText().toString().trim();
        dob= binding.dobEt.getText().toString().trim();
        email= binding.emailEt.getText().toString().trim();
        phoneCode= binding.countryCodePicker.getSelectedCountryCodeWithPlus();
        phoneNumber= binding.phoneNumberEt.getText().toString().trim();
        if (imageUri == null){

            updateProfileDb(null);
        }
        else {
            uploadProfileImageStorage();
        }
    }
    private void uploadProfileImageStorage(){
        Log.d(TAG,"uploadProfileImageStorage: ");
        progressDialog.setMessage("Uploading user profile image...");
        progressDialog.show();

        String filePathName = "UserImages/"+"profile_"+firebaseAuth.getUid();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathName);
        ref.putFile(imageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred())/snapshot.getTotalByteCount();

                        Log.d(TAG,"onProgress: Progress : "+progress);

                        progressDialog.setMessage("Uploading profile image. Progress: "+ (int)progress + "%");
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG,"onSuccess: Uploaded");

                        Task<Uri>uriTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!uriTask.isSuccessful());
                        String uploadedImageUrl = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()){
                            updateProfileDb(uploadedImageUrl);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure: ",e);
                        progressDialog.dismiss();
                        Utils.toast(ProfileEditActivity.this,"Failed to update profile info due to "+e.getMessage());

                    }
                });
    }
    private void updateProfileDb(String imageUrl){
                progressDialog.setMessage("Updating user info...");
                progressDialog.show();

        HashMap<String,Object>hashMap = new HashMap<>();
        hashMap.put("name",""+ name);
        hashMap.put("dob", ""+dob);
        if (imageUrl !=null){
            hashMap.put("profileImageUrl","" + imageUrl);
        }
        if (myUserType.equalsIgnoreCase("Phone")){
            hashMap.put("email","" + email);

        } else if (myUserType.equalsIgnoreCase("Email") || myUserType.equalsIgnoreCase("Google"))
         {
             hashMap.put("phoneCode",phoneCode);
             hashMap.put("phoneNumber",phoneNumber);
        }
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG,"onSuccess: Info updated");
                        progressDialog.dismiss();
                        Utils.toast(ProfileEditActivity.this,"Profile Updated...");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure: ",e);
                        progressDialog.dismiss();
                        Utils.toast(ProfileEditActivity.this,"Failed to update info due to "+e.getMessage());
                    }
                });
    }

    private void loadMyInfo() {
        Log.d(TAG,"loadMyInfo: ");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String dob=""+snapshot.child("dob").getValue();
                        String email=""+snapshot.child("email").getValue();

                        String name=""+snapshot.child("name").getValue();

                        String phoneCode=""+snapshot.child("phoneCode").getValue();
                        String phoneNumber=""+snapshot.child("phoneNumber").getValue();
                        String profileImageUrl=""+snapshot.child("profileImageUrl").getValue();
                        String timestamp=""+snapshot.child("timestamp").getValue();
                        myUserType=""+snapshot.child("userType").getValue();

                        String phone = phoneCode+phoneNumber;
                        if (myUserType.equalsIgnoreCase("Email")||myUserType.equalsIgnoreCase("Google")){
                            binding.emailTil.setEnabled(false);
                            binding.emailEt.setEnabled(false);
                        }
                        else {
                            binding.phoneNumberTil.setEnabled(false);
                            binding.phoneNumberEt.setEnabled(false);
                            binding.countryCodePicker.setEnabled(false);
                        }
                        binding.emailEt.setText(email);
                        binding.dobEt.setText(dob);
                        binding.nameEt.setText(name);
                        binding.phoneNumberEt.setText(phoneNumber);
                        try {
                            int phoneCodeInt = Integer.parseInt(phoneCode.replace("+",""));
                            binding.countryCodePicker.setCountryForPhoneCode(phoneCodeInt);
                        }
                        catch (Exception e){
                            Log.e(TAG,"onDataChange",e);
                        }
                        try {
                            Glide.with(ProfileEditActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_person_white)
                                    .into(binding.profileTv);
                        }
                        catch (Exception e){
                            Log.e(TAG,"onDataChange",e);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    private void imagePickDialog(){
        PopupMenu popupMenu = new PopupMenu(this,binding.profileImagePickFab);
        popupMenu.getMenu().add(Menu.NONE,1,1,"Camera");
        popupMenu.getMenu().add(Menu.NONE,2,2,"Gallery");
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == 1){
                    Log.d(TAG,"onMenuItemClick: Camera Clicked, check if camera permission(s) granted or not");
                    resultCameraPermissions.launch(new String[]{Manifest.permission.CAMERA});
                    pickImageCamera();
                } else if (itemId ==2) {
                    Log.d(TAG,"onMenuItemClick: Check if storage permission is granted or not");
                    pickImageGallery();
                }
                return false;
            }
        });
    }
    private ActivityResultLauncher<String[]> resultCameraPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> o) {
                    Log.d(TAG,"onActivityResult: "+o.toString());

                    boolean areAllGranted=true;
                    for (Boolean isGranted: o.values()){
                        areAllGranted = areAllGranted && isGranted;
                    }
                    if (areAllGranted){
                        Log.d(TAG,"onActivityResult: All Granted e.g. Camera, Storage");
                        pickImageCamera();
                    }
                    else {
                        Log.d(TAG,"onActivityResult: All or either one is denied");
                        Utils.toast(ProfileEditActivity.this,"Camera or Storage or both permissions denied...");
                    }
                }
            }
    );
    private ActivityResultLauncher<String>requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG,"onActivityResult: isGranted: "+isGranted);
                    if (isGranted){
                       pickImageGallery();
                    }
                    else {
                        Utils.toast(ProfileEditActivity.this,"Storage permission denied...!");
                    }
                }
            }
    );
    private void pickImageCamera(){
        Log.d(TAG,"pickImageCamera: ");

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"TEMP_TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"TEMP_DESCRIPTION");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        cameraActivityResultLauncher.launch(intent);
    }
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG,"onActivityResult: Image Captured: "+imageUri);

                        try {
                            Glide.with(ProfileEditActivity.this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.ic_person_white)
                                    .into(binding.profileTv);
                        }
                        catch (Exception e){
                            Log.e(TAG,"onActivityResult: ",e);
                        }
                    }
                    else{
                        Utils.toast(ProfileEditActivity.this,"Cancelled...");
                    }
                }
            }
    );
    private void pickImageGallery(){

        Log.d(TAG,"pickImageGallery: ");

        Intent intent = new Intent(Intent.ACTION_PICK);

        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }
    private ActivityResultLauncher<Intent>galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == Activity.RESULT_OK){
                        Intent data = o.getData();
                        imageUri = data.getData();
                        Log.d(TAG,"onActivityResult: Image Picked From Gallery: "+imageUri);
                        try {
                            Glide.with(ProfileEditActivity.this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.ic_person_white)
                                    .into(binding.profileTv);
                        }
                        catch (Exception e){
                            Log.e(TAG,"onActivityResult: ",e);
                        }
                    }
                    else {
                        Utils.toast(ProfileEditActivity.this,"Cancelled...");
                    }
                }
            }
    );
}