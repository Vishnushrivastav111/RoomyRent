package com.example.roomyrent.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.roomyrent.FilterAd;
import com.example.roomyrent.R;
import com.example.roomyrent.Utils;
import com.example.roomyrent.activities.AdDetailsActivity;
import com.example.roomyrent.databinding.RowAdBinding;
import com.example.roomyrent.models.ModelAd;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterAd extends RecyclerView.Adapter<AdapterAd.HolderAd> implements Filterable {
    private RowAdBinding binding;
    private static final String TAG = "ADAPTER_AD_TAG";
    private FirebaseAuth firebaseAuth;
    private Context context;
    public ArrayList<ModelAd> adArrayList;
    private ArrayList<ModelAd> filterList;
    private FilterAd filter;


    public AdapterAd(Context context, ArrayList<ModelAd> adArrayList) {
        this.context = context;
        this.adArrayList = adArrayList != null ? adArrayList : new ArrayList<>();
        this.filterList = this.adArrayList;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderAd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowAdBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderAd(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAd holder, int position) {
        ModelAd modelAd = adArrayList.get(position);

        // Check if modelAd is null to avoid NullPointerException
        if (modelAd == null) {
            Log.e(TAG, "onBindViewHolder: modelAd is null at position " + position);
            holder.titleTv.setText("N/A");
            holder.descriptionTv.setText("");
            holder.addressTv.setText("");
            holder.conditionTv.setText("");
            holder.priceTv.setText("");
            holder.dateTv.setText("");
            holder.imageIv.setImageResource(R.drawable.ic_images_gray);
            return;
        }
            String title = modelAd.getTitle();
            String description = modelAd.getDescription();
            String address = modelAd.getAddress();
            String condition = modelAd.getCondition();
            String rent = modelAd.getRent();
            String timestamp = modelAd.getTimestamp();

            String formattedDate = "N/A";
            try {
                formattedDate = Utils.formatTimestampDate(Long.parseLong(timestamp));
            } catch (Exception e) {
                Log.e(TAG, "Invalid timestamp: " + timestamp, e);
            }

            loadAdFirstImage(modelAd, holder);

            if (firebaseAuth.getCurrentUser() != null) {
                checkIsFavorite(modelAd, holder);
            }

            holder.titleTv.setText(title != null ? title : "N/A");
            holder.descriptionTv.setText(description != null ? description : "");
            holder.addressTv.setText(address != null ? address : "");
            holder.conditionTv.setText(condition != null ? condition : "");
            holder.priceTv.setText(rent != null ? rent : "");
            holder.dateTv.setText(formattedDate);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, AdDetailsActivity.class);
                intent.putExtra("adId", modelAd.getId());
                context.startActivity(intent);
            });

            holder.favBtn.setOnClickListener(v -> {
                boolean favorite = modelAd.isFavorite();
                if (favorite) {
                    Utils.removeFromFavorite(context, modelAd.getId());
                } else {
                    Utils.addToFavorite(context, modelAd.getId());
                }
            });
        }


    private void checkIsFavorite(ModelAd modelAd, HolderAd holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(modelAd.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean favorite = snapshot.exists();
                        modelAd.setFavorite(favorite);
                        if (favorite){
                            holder.favBtn.setImageResource(R.drawable.ic_fav_yes);
                        }else {
                            holder.favBtn.setImageResource(R.drawable.ic_fav_no);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAdFirstImage(ModelAd modelAd, HolderAd holder) {
        Log.d(TAG,"loadAdFirstImage: ");
        String adId = modelAd.getId();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Ads");
        reference.child(adId).child("Images").limitToFirst(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String imageUrl = ""+ ds.child("imageUrl").getValue();
                            Log.d(TAG,"onDataChange: imageUrl: "+imageUrl);
                            try {
                                Glide.with(context)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_images_gray)
                                        .into(holder.imageIv);
                            }catch (Exception e){
                                Log.e(TAG,"onDataChange: ",e);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return adArrayList != null ? adArrayList.size() : 0;
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterAd(this,filterList);
        }
        return filter;
    }

    class HolderAd extends RecyclerView.ViewHolder{

        ShapeableImageView imageIv;
        TextView titleTv,descriptionTv,addressTv,conditionTv,priceTv,dateTv;
        ImageButton favBtn;

        public HolderAd(@NonNull View itemView) {

            super(itemView);
            imageIv = binding.imageIv;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            favBtn = binding.favBtn;
            addressTv = binding.addressTv;
            conditionTv = binding.conditionTv;
            priceTv = binding.priceTv;
            dateTv = binding.dateTv;
        }
    }
}
