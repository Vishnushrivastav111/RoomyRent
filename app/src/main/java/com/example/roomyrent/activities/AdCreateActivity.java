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
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;

import com.example.roomyrent.R;
import com.example.roomyrent.Utils;
import com.example.roomyrent.adapters.AdapterImagesPicked;
import com.example.roomyrent.databinding.ActivityAdCreateBinding;
import com.example.roomyrent.models.ModelImagePicked;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdCreateActivity extends AppCompatActivity {
    private ActivityAdCreateBinding binding;
    private static final String TAG = "AD_CREATE_TAG";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private Uri imageUri = null;
    private ArrayList<ModelImagePicked>imagePickedArrayList;
    private AdapterImagesPicked adapterImagesPicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
        ArrayAdapter<String> adapterCategories = new ArrayAdapter<>(this, R.layout.row_category_act, Utils.categories);
        binding.categoriesEt.setAdapter(adapterCategories);
        ArrayAdapter<String> kitchen = new ArrayAdapter<>(this,R.layout.row_kitchen_act,Utils.kitchen);
        binding.kitchenEt.setAdapter(kitchen);
        ArrayAdapter<String> bathroom = new ArrayAdapter<>(this,R.layout.row_bathroom_act,Utils.bathroom);
        binding.conditionEt.setAdapter(bathroom);
        ArrayAdapter<String> Wifi = new ArrayAdapter<>(this,R.layout.row_bathroom_act,Utils.bathroom);
        binding.wifiEt.setAdapter(Wifi);
        ArrayAdapter<String> furniture = new ArrayAdapter<>(this,R.layout.row_bathroom_act,Utils.bathroom);
        binding.furnitureEt.setAdapter(furniture);
        ArrayAdapter<String> lightBill = new ArrayAdapter<>(this,R.layout.row_bathroom_act,Utils.bathroom);
        binding.lightBillEt.setAdapter(lightBill);
        imagePickedArrayList = new ArrayList<>();
        loadImages();

        binding.toolbarBackBtn.setOnClickListener(v -> onBackPressed());
        binding.postAdBtn.setOnClickListener(v -> validateData());
        binding.toolbarAddImageBtn.setOnClickListener(v -> showImagePickOption());
        binding.locationAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdCreateActivity.this, LocationPickerActivity.class);
                locationPickerActivityResultLauncher.launch(intent);
            }
        });

    }
    private ActivityResultLauncher<Intent>locationPickerActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    Log.d(TAG,"onActivityResult: ");
                    if (o.getResultCode() == Activity.RESULT_OK){
                        Intent data = o.getData();
                        if (data!=null){
                            latitude = data.getDoubleExtra("latitude",0.0);
                            longitude = data.getDoubleExtra("longitude",0.0);
                            address = data.getStringExtra("address");

                            Log.d(TAG,"onActivityResult: latitude: "+latitude);
                            Log.d(TAG,"onActivityResult: longitude: "+longitude);
                            Log.d(TAG,"onActivityResult: address: "+address);
                            binding.locationAct.setText(address);

                        }
                    }else {
                        Log.d(TAG,"onActivityResult: cancelled");
                        Utils.toast(AdCreateActivity.this,"Cancelled");
                    }
                }
            }
    );
    private void loadImages(){
        Log.d(TAG,"loadImages: ");
        adapterImagesPicked = new AdapterImagesPicked(this,imagePickedArrayList);
        binding.imagesRv.setAdapter(adapterImagesPicked);
    }
    private void showImagePickOption(){
        Log.d(TAG,"showImagePickOption: ");

        PopupMenu popupMenu = new PopupMenu(this,binding.toolbarAddImageBtn);
        popupMenu.getMenu().add(Menu.NONE,1,1,"Camera");
        popupMenu.getMenu().add(Menu.NONE,2,2,"Gallery");
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == 1){
                    String[] cameraPermissions;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        cameraPermissions = new String[]{Manifest.permission.CAMERA};

                    }else {
                           pickImageCamera();
                       /* cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestCameraPermissions.launch(cameraPermissions);*/
                    }
                } else if (itemId == 2) {
                    pickImageGallery();

                   /* if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        pickImageGallery();
                    }else {
                        String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                        requestStoragePermission.launch(storagePermission);
                    }*/
                }
                return true;
            }
        });
    }
    private ActivityResultLauncher<String>requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                public void onActivityResult(Boolean isGranted){
                    Log.d(TAG,"onActivityResult: isGranted: "+isGranted);

                    if (isGranted){
                        pickImageGallery();
                    }else {
                        Utils.toast(AdCreateActivity.this,"Storage Permission denied...");
                    }
                }
            }
    );
    private ActivityResultLauncher<String[]>requestCameraPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> o) {
                    Log.d(TAG,"onActivityResult: ");
                    Log.d(TAG,"onActivityResult: "+o.toString());

                    boolean areAllGranted = true;
                    for(Boolean isGranted : o.values()){
                        areAllGranted = areAllGranted && isGranted;
                    }
                    if (areAllGranted){
                        pickImageCamera();
                    }else {
                        Utils.toast(AdCreateActivity.this,"Camera or Storage or both permissions denied...");
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
    private void pickImageCamera(){
        Log.d(TAG,"pickImageCamera");

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"TEMPORARY_IMAGE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"TEMPORARY_IMAGE_DESCRIPTION");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        cameraActivityResultLauncher.launch(intent);

    }
    private final ActivityResultLauncher<Intent>galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    Log.d(TAG,"onActivityResult: ");
                    if (o.getResultCode()== Activity.RESULT_OK){
                        Intent data = o.getData();

                        Log.d(TAG,"onActivityResult: imageUri: "+imageUri);

                        String timestamp = ""+Utils.getTimestamp();

                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp,imageUri,null,false);
                        imagePickedArrayList.add(modelImagePicked);

                        loadImages();
                    }else {
                        Utils.toast(AdCreateActivity.this,"Cancelled");
                    }
                }
            }
    );
    private final ActivityResultLauncher<Intent>cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    Log.d(TAG,"onActivityResult: ");
                    if (o.getResultCode()== Activity.RESULT_OK){

                        Log.d(TAG,"onActivityResult: imageUri: "+imageUri);

                        String timestamp = ""+Utils.getTimestamp();

                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp,imageUri,null,false);
                        imagePickedArrayList.add(modelImagePicked);

                        loadImages();
                    }else {
                        Utils.toast(AdCreateActivity.this,"Cancelled");
                    }
                }
            }
    );
    private String size="";
    private String floor="";
    private String house="";
    private String bathroom="";
    private String kitchen="";
    private String category="";
    private String wifi="";
    private String furniture="";
    private String light="";
    private String rent="";
    private String title="";
    private String description="";
    private double latitude = 0.0;
    private double longitude = 0.0;
    private String address ="";

    private void validateData(){
        Log.d(TAG,"validateData: ");

        size = binding.brandEt.getText().toString().trim();
        floor = binding.categoryEt.getText().toString().trim();
        house = binding.houseEt.getText().toString().trim();
        bathroom = binding.conditionEt.getText().toString().trim();
        kitchen = binding.kitchenEt.getText().toString().trim();
        address = binding.locationAct.getText().toString().trim();
        category = binding.categoriesEt.getText().toString().trim();
        wifi = binding.wifiEt.getText().toString().trim();
        furniture = binding.furnitureEt.getText().toString().trim();
        light = binding.lightBillEt.getText().toString().trim();
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        rent = binding.priceEt.getText().toString().trim();

        if (size.isEmpty()){
            binding.brandEt.setError("Enter Size");
            binding.brandEt.requestFocus();
        }else if (floor.isEmpty()){
            binding.categoryEt.setError("Enter Floor No.");
            binding.categoryEt.requestFocus();
        }else if (house.isEmpty()){
            binding.categoryEt.setError("Enter House No.");
            binding.categoryEt.requestFocus();
        }else if (bathroom.isEmpty()){
            binding.categoryEt.setError("Enter Yes/No");
            binding.categoryEt.requestFocus();
        }else if (kitchen.isEmpty()){
            binding.categoryEt.setError("Enter Yes/No");
            binding.categoryEt.requestFocus();
        } else if (category.isEmpty()) {
            binding.categoriesEt.setError("Choose any one");
            binding.categoriesEt.requestFocus();
        } else if (wifi.isEmpty()){
            binding.categoryEt.setError("Enter Yes/No");
            binding.categoryEt.requestFocus();
        }else if (furniture.isEmpty()){
            binding.categoryEt.setError("Enter Yes/No");
            binding.categoryEt.requestFocus();
        }else if (light.isEmpty()){
            binding.categoryEt.setError("Enter Yes/No");
            binding.categoryEt.requestFocus();
        }else if (address.isEmpty()){
            binding.categoryEt.setError("Choose location");
            binding.categoryEt.requestFocus();
        }else if (rent.isEmpty()){
            binding.categoryEt.setError("Enter Rent of Room/Flat");
            binding.categoryEt.requestFocus();
        }else if (title.isEmpty()){
            binding.categoryEt.setError("Enter Title");
            binding.categoryEt.requestFocus();
        }else if (description.isEmpty()){
            binding.categoryEt.setError("Enter description");
            binding.categoryEt.requestFocus();
        }else if (imagePickedArrayList.isEmpty()){
            Utils.toast(this,"Pick at-least one image");
        }else{
            postAd();
        }
    }
private void postAd(){
        Log.d(TAG,"postAd: ");
        progressDialog.setMessage("Publishing Ad");
        progressDialog.show();

        long timestamp = Utils.getTimestamp();

    DatabaseReference refAds = FirebaseDatabase.getInstance().getReference("Ads");

    String keyId = refAds.push().getKey();

    HashMap<String, Object> hashMap = new HashMap<>();
    hashMap.put("id",""+ keyId);
    hashMap.put("uid",""+ firebaseAuth.getUid());
    hashMap.put("size",""+ size);
    hashMap.put("floor",""+ floor);
    hashMap.put("house",""+ house);
    hashMap.put("bathroom",""+ bathroom);
    hashMap.put("kitchen",""+ kitchen);
    hashMap.put("wifi",""+ wifi);
    hashMap.put("category",""+category);
    hashMap.put("address",""+address);
    hashMap.put("furniture",""+ furniture);
    hashMap.put("light",""+ light);
    hashMap.put("rent",""+ rent);
    hashMap.put("title",""+ title);
    hashMap.put("description",""+ description);
    hashMap.put("status",""+ Utils.AD_STATUS_AVAILABLE);
    hashMap.put("timestamp",""+ timestamp);
    hashMap.put("latitude",""+latitude);
    hashMap.put("longitude",""+ longitude);
    refAds.child(keyId)
            .setValue(hashMap)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(TAG,"onSuccess: Ad Published");
                    uploadImagesStorage(keyId);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                     Log.e(TAG,"onFailure: ",e);
                     progressDialog.dismiss();
                     Utils.toast(AdCreateActivity.this,"Failed to publish Ad due to "+e.getMessage());
                }
            });

}
private void uploadImagesStorage(String keyId){
        Log.d(TAG,"uploadImagesStorage: ");
        for (int i=0;i<imagePickedArrayList.size();i++){
            ModelImagePicked modelImagePicked = imagePickedArrayList.get(i);
            String imageName = modelImagePicked.getId();
            String filePathName = "Ads/"+imageName;
            String adId = keyId;
            int imageIndexForProgress = i+1;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathName);
            storageReference.putFile(modelImagePicked.getImageUri())
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                            String message = "Uploading "+imageIndexForProgress + " of " +imagePickedArrayList.size() + "images...\nProgress "+ (int)progress + "%";
                            Log.d(TAG,"onProgress: message: "+message);

                            progressDialog.setMessage(message);
                            progressDialog.show();
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG,"onSuccess: ");
                            Task<Uri>uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri uploadImageUrl=uriTask.getResult();

                            if (uriTask.isSuccessful()){
                                HashMap<String,Object>hashMap=new HashMap<>();
                                hashMap.put("id",""+modelImagePicked.getId());
                                hashMap.put("imageUrl",""+uploadImageUrl);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
                                ref.child(adId).child("Images")
                                        .child(imageName)
                                        .updateChildren(hashMap);
                            }
                            progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG,"onFailure: ",e);
                            progressDialog.dismiss();
                        }
                    });
        }
}
}