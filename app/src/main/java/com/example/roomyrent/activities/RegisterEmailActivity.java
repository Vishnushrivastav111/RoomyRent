package com.example.roomyrent.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.roomyrent.Utils;
import com.example.roomyrent.databinding.ActivityRegisterEmailBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterEmailActivity extends AppCompatActivity {
    private ActivityRegisterEmailBinding binding;
    private static final String TAG="REGISTER_TAG";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.haveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }
    private String email,password,cPassword,username,phone;
    private void validateData(){
        username = binding.usernameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        phone = binding.phoneEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString();
        cPassword = binding.cPasswordEt.getText().toString();
        Log.d(TAG,"validateData: username: "+username);
        Log.d(TAG,"validateData: email: "+email);
        Log.d(TAG,"validateData: phone: "+phone);
        Log.d(TAG,"validateData: password: "+password);
        Log.d(TAG,"validateData: cPassword: "+cPassword);
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.setError("Invalid Email Pattern");
            binding.emailEt.requestFocus();
        } else if (!password.equals(cPassword)) {
            binding.cPasswordEt.setError("Password doesn't match");
            binding.cPasswordEt.requestFocus();
        }else {
            registerUser();
        }
    }
    private void registerUser(){
        progressDialog.setMessage("Creating Account");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            updateUserInfo();
                            sendVerificationEmail();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthUserCollisionException){
                            binding.emailEt.setError("Email Already Registered");
                            binding.emailEt.requestFocus();
                        }
                        else {
                            Toast.makeText(RegisterEmailActivity.this,"Oops: Something went wrong",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
       /* firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG,"onSuccess: Register Success");
                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure",e);
                        Utils.toast(RegisterEmailActivity.this,"Failed due to "+e.getMessage());
                        progressDialog.dismiss();
                    }
                });*/
    }
    private void sendVerificationEmail(){
        if (firebaseAuth.getCurrentUser()!=null){
            firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(RegisterEmailActivity.this,"Email has been sent to your email address",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        firebaseAuth.signOut(); // User ko logout kar dein jab tak wo verify na kare
                        startActivity(new Intent(RegisterEmailActivity.this, LoginEmailActivity.class)); // Ya kisi dusre screen par bhej dein
                        finish();
                    }
                    else {
                        Toast.makeText(RegisterEmailActivity.this,"Oops! failed to send verification email",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void updateUserInfo(){
        progressDialog.setMessage("Saving User Info");

        long timestamp=Utils.getTimestamp();
        String registerUserEmail = firebaseAuth.getCurrentUser().getEmail();
        String registerUserUId = firebaseAuth.getUid();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("name",""+username);
        hashMap.put("phoneCode", "");
        hashMap.put("phoneNumber", ""+phone);
        hashMap.put("profileImageUrl", "");
        hashMap.put("dob", "");
        hashMap.put("UserType", "Email");
        hashMap.put("typingTo", "");
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("onlineStatus", true);
        hashMap.put("email",""+registerUserEmail);
        hashMap.put("uid",""+registerUserUId);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(registerUserUId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG,"onSuccess: Info saved...");
                        progressDialog.dismiss();
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure: ",e);
                        progressDialog.dismiss();
                        Utils.toast(RegisterEmailActivity.this,"Failed to save info due to "+e.getMessage());
                    }
                });
    }
}