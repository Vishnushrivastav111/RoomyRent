package com.example.roomyrent.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.roomyrent.R;
import com.example.roomyrent.Utils;
import com.example.roomyrent.activities.ChangePasswordActivity;
import com.example.roomyrent.activities.DeleteAccountActivity;
import com.example.roomyrent.activities.HelpActivity;
import com.example.roomyrent.activities.MainActivity;
import com.example.roomyrent.activities.ProfileEditActivity;
import com.example.roomyrent.databinding.FragmentAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;
    private static final String TAG="Account_TAG";
    private FirebaseAuth firebaseAuth;
    private Context mContext;
    private ProgressDialog progressDialog;

    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(LayoutInflater.from(mContext), container, false);
        return binding.getRoot();
    }
    public void onViewCreated(@NonNull View view,@NonNull Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();

        binding.logoutCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();

                startActivity(new Intent(mContext, MainActivity.class));
                getActivity().finishAffinity();
            }
        });
        binding.editProfileCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, ProfileEditActivity.class));
            }
        });
        binding.changePasswordCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, ChangePasswordActivity.class));

            }
        });
        binding.helpCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpAndSupport();
            }
        });
        binding.verifyAccountCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyAccount();
            }
        });
        binding.deleteAccountCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, DeleteAccountActivity.class));
                getActivity().finishAffinity();
            }
        });
    }
    private void loadMyInfo(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String dob=""+ snapshot.child("dob").getValue();
                        String email=""+ snapshot.child("email").getValue();
                        String name=""+ snapshot.child("name").getValue();
                        String phoneCode=""+ snapshot.child("phoneCode").getValue();
                        String phoneNumber=""+ snapshot.child("phoneNumber").getValue();
                        String profileImageUrl=""+ snapshot.child("profileImageUrl").getValue();
                        String timestamp=""+ snapshot.child("timestamp").getValue();
                        String userType =""+ snapshot.child("userType").getValue();

                        String phone=phoneCode + phoneNumber;
                        if (timestamp.equals("null")){
                            timestamp="0";
                        }
                        String formattedDate = Utils.formatTimestampDate(Long.parseLong(timestamp));
                        binding.emailTv.setText(email);
                        binding.nameTv.setText(name);
                        binding.dobTv.setText(dob);
                        binding.phoneTv.setText(phone);
                        binding.memberSinceTv.setText(formattedDate);
                        if (userType.equals("Email")){
                            boolean isVerified = firebaseAuth.getCurrentUser().isEmailVerified();
                            if (isVerified){
                                binding.verifyAccountCv.setVisibility(View.GONE);
                                binding.verificationTv.setText("Verified");
                            }
                            else {
                                binding.verifyAccountCv.setVisibility(View.VISIBLE);
                                binding.verificationTv.setText("Not Verified");
                            }
                        }
                        else {
                            binding.verifyAccountCv.setVisibility(View.GONE);
                            binding.verificationTv.setText("Verified");
                        }
                        try {
                            Glide.with(mContext)
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
    public void helpAndSupport(){
        Log.d(TAG,"help And Support");
        Intent intent = new Intent(mContext, HelpActivity.class);
        startActivity(intent);
    }
    public void verifyAccount(){
        Log.d(TAG,"verifyAccount");

        progressDialog.setMessage("Sending Account verification instruction to your email");
        progressDialog.show();

        firebaseAuth.getCurrentUser().sendEmailVerification()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG,"onSuccess: Sent");
                    progressDialog.dismiss();
                    Utils.toast(mContext,"Account verification instruction sent to your email");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG,"onFailure: ",e);
                    progressDialog.dismiss();
                    Utils.toast(mContext,"Failed due to "+e.getMessage());
                });
    }
}