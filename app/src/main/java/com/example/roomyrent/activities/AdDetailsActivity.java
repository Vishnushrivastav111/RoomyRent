package com.example.roomyrent.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.roomyrent.R;
import com.example.roomyrent.Utils;
import com.example.roomyrent.adapters.AdapterImageSlider;
import com.example.roomyrent.databinding.ActivityAdDetailsBinding;
import com.example.roomyrent.models.ModelAd;
import com.example.roomyrent.models.ModelImageSlider;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class AdDetailsActivity extends AppCompatActivity {
         private ActivityAdDetailsBinding binding;
         private static final String TAG = "AD_DETAILS_TAG";
         private FirebaseAuth firebaseAuth;
         private String adId = "";
         private double adLatitude = 0;
         private double adLongitude =0;
         private String sellerUid = null;
         private String sellerPhone = "";
         private boolean favorite = false;
         private ArrayList<ModelImageSlider> imageSliderArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarEditBtn.setVisibility(View.GONE);
        binding.toolbarDeleteBtn.setVisibility(View.GONE);
        binding.chatBtn.setVisibility(View.GONE);
        binding.callBtn.setVisibility(View.GONE);
        binding.smsBtn.setVisibility(View.GONE);

        adId = getIntent().getStringExtra("adId");
        Log.d(TAG,"onCreate: adId: "+adId);


        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }
        loadAdDetails();
        loadAdImages();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.toolbarDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(AdDetailsActivity.this);
                materialAlertDialogBuilder.setTitle("Delete Ad")
                        .setMessage("Are you sure you want to delete this Ad?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteAd();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
        binding.toolbarEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        binding.toolbarFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favorite){
                    Utils.removeFromFavorite(AdDetailsActivity.this,adId);
                }else {
                    Utils.addToFavorite(AdDetailsActivity.this,adId);
                }
            }
        });

        binding.sellerProfileCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        binding.chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        binding.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.callIntent(AdDetailsActivity.this,sellerPhone);
            }
        });
        binding.smsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.smsIntent(AdDetailsActivity.this,sellerPhone);
            }
        });
        binding.mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.mapIntent(AdDetailsActivity.this,adLatitude,adLongitude);
            }
        });

    }
    private void showMarkAsSoldDialog(){
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        alertDialogBuilder.setTitle("Mark as Sold")
                .setMessage("Are you sure you want to mark this Ad as sold?")
                .setPositiveButton("SOLD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG,"onClick: Sold Clicked...");
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("status",""+ Utils.AD_STATUS_SOLD);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
                        ref.child(adId)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG,"onSuccess: Marked as sold");
                                        Utils.toast(AdDetailsActivity.this,"Marked as sold");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG,"onFailure: ",e);
                                        Utils.toast(AdDetailsActivity.this,"Failed to mark as sold due to "+e.getMessage());
                                    }
                                });

                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG,"onClick: Cancel Clicked...");
                        dialog.dismiss();
                    }
                })
                .show();
    }
    private void loadAdDetails(){
        Log.d(TAG,"loadAdDetails: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            ModelAd modelAd = snapshot.getValue(ModelAd.class);

                            sellerUid = modelAd.getUid();
                            String title = modelAd.getTitle();
                            String description = modelAd.getDescription();
                            String address = modelAd.getAddress();
                            String category = modelAd.getCategory();
                            String rent = modelAd.getRent();
                            String house = modelAd.getHouse();
                            String floor = modelAd.getFloor();
                            String kitchen = modelAd.getKitchen();
                            String size = modelAd.getSize();
                            String bathroom = modelAd.getBathroom();
                            String wifi = modelAd.getWifi();
                            String furniture = modelAd.getFurniture();
                            String lightBill = modelAd.getLight();
                            adLatitude = Double.parseDouble(modelAd.getLatitude());
                            adLongitude = Double.parseDouble(modelAd.getLongitude());
                            long timestamp = Long.parseLong(modelAd.getTimestamp());

                           String formattedDate = Utils.formatTimestampDate(timestamp);

                            if (sellerUid.equals(firebaseAuth.getUid())){

                                binding.toolbarEditBtn.setVisibility(View.VISIBLE);
                                binding.toolbarDeleteBtn.setVisibility(View.VISIBLE);

                                binding.chatBtn.setVisibility(View.GONE);
                                binding.callBtn.setVisibility(View.GONE);
                                binding.smsBtn.setVisibility(View.GONE);

                            }else {
                                binding.toolbarEditBtn.setVisibility(View.GONE);
                                binding.toolbarDeleteBtn.setVisibility(View.GONE);

                                binding.chatBtn.setVisibility(View.VISIBLE);
                                binding.callBtn.setVisibility(View.VISIBLE);
                                binding.smsBtn.setVisibility(View.VISIBLE);
                            }
                            binding.titleTv.setText(title);
                            binding.descriptionTv.setText(description);
                            binding.addressTv.setText(address);
                            binding.categoryTv.setText(category);
                            binding.priceTv.setText(rent);
                            binding.dateTv.setText(formattedDate);
                            binding.houseTv.setText(house);
                            binding.floorTv.setText(floor);
                            binding.kitchenTv.setText(kitchen);
                            binding.sizeTv.setText(size);
                            binding.bathroomTv.setText(bathroom);
                            binding.wifiTv.setText(wifi);
                            binding.furnitureTv.setText(furniture);
                            binding.lightBillTv.setText(lightBill);

                            loadSellerDetails();
                        }catch (Exception e){
                            Log.e(TAG,"onDataChange: ",e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadSellerDetails(){
        Log.d(TAG,"loadSellerDetails: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String phoneCode = ""+ snapshot.child("phoneCode").getValue();
                        String phoneNumber = ""+ snapshot.child("phoneNumber").getValue();
                        String name = ""+ snapshot.child("name").getValue();
                        String profileImageUrl = ""+ snapshot.child("profileImageUrl").getValue();
                        String timestamp=""+ snapshot.child("timestamp").getValue();
                        if (timestamp.equals("null")){
                            timestamp="0";
                        }
                        String formattedDate = Utils.formatTimestampDate(Long.parseLong(timestamp));
                        sellerPhone = phoneCode+phoneNumber;

                        binding.sellerNameTv.setText(name);
                        binding.memberSinceTv.setText(formattedDate);
                        try {
                            Glide.with(AdDetailsActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_person_white)
                                    .into(binding.sellerProfileIv);
                        }catch (Exception e){
                            Log.e(TAG,"onDataChange: ",e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void checkIsFavorite(){
        Log.d(TAG,"checkIsFavorite: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        favorite = snapshot.exists();

                        Log.d(TAG,"onDataChange: favorite: "+favorite);

                        if (favorite){
                            binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_yes);
                        }else {
                            binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_no);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadAdImages(){
        Log.d(TAG,"loadAdImages: ");
        imageSliderArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId)
                .child("Images")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                         imageSliderArrayList.clear();

                         for (DataSnapshot ds: snapshot.getChildren()){
                             ModelImageSlider modelImageSlider = ds.getValue(ModelImageSlider.class);
                             imageSliderArrayList.add(modelImageSlider);

                         }
                        AdapterImageSlider adapterImageSlider = new AdapterImageSlider(AdDetailsActivity.this,imageSliderArrayList);
                         binding.imageSliderVp.setAdapter(adapterImageSlider);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void deleteAd(){
        Log.d(TAG,"deleteAd: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG,"onSuccess: Deleted: ");
                        Utils.toast(AdDetailsActivity.this,"Deleted");
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure: ",e);
                        Utils.toast(AdDetailsActivity.this,"Failed to delete due to "+e.getMessage());
                    }
                });
    }
}