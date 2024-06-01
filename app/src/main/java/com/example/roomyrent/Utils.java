package com.example.roomyrent;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import android.text.format.DateFormat;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class Utils {

    public static String[] bathroom = {"Yes", "No", "Prefer not to say"};
    public static final String[] categories = {"Family Room", "Hostel", "Student Room", "Hotel"};
    public static String[] kitchen = {"Yes", "No", "Prefer not to say"};
    public static final int[] categoryIcons = {
            R.drawable.ic_category_family,
            R.drawable.ic_category_hostel,
            R.drawable.baseline_school_24,
            R.drawable.ic_category_hotel,
    };
    public static final String AD_STATUS_AVAILABLE = "AVAILABLE";
    public static final String AD_STATUS_SOLD = "SOLD";

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static long getTimestamp() {
        return System.currentTimeMillis();
    }

    public static String formatTimestampDate(Long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy", calendar).toString();
        return date;
    }

    public static void addToFavorite(Context context, String adId) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Utils.toast(context, "You're not logged in!");
        } else {
            long timestamp = Utils.getTimestamp();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("adId", adId);
            hashMap.put("timestamp", timestamp);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Utils.toast(context, "Added to favorite...!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.toast(context, "Failed to add to favorite due to " + e.getMessage());
                        }
                    });
        }
    }

    public static void removeFromFavorite(Context context, String adId) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Utils.toast(context, "You're not logged in!");
        } else {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Utils.toast(context, "Successfully remove from  favorite...!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.toast(context, "Failed to remove from favorite due to " + e.getMessage());
                        }
                    });
        }
    }

    public static void callIntent(Context context, String phone) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + Uri.encode(phone)));
        context.startActivity(intent);
    }

    public static void smsIntent(Context context, String phone) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", Uri.encode(phone), null));
        context.startActivity(intent);

    }
    public static void mapIntent(Context context,double latitude,double longitude){
        Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr="+latitude+""+longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW,gmmIntentUri);

        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) !=null){
            context.startActivity(mapIntent);
        }else {
            Utils.toast(context,"Google MAP Not installed!");
        }
    }
}
