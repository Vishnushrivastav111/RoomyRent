package com.example.roomyrent.activities;


import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;

import com.example.roomyrent.Utils;
import com.example.roomyrent.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private static final String TAG = "FORGOT_PASS_TAG";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        binding.toolbarBackBtn.setOnClickListener(v -> onBackPressed());

        binding.submitBtn.setOnClickListener(v -> validateData());
    }
    private String email= "";
    private void validateData(){
        Log.d(TAG,"validateData: ");
        email = binding.emailEt.getText().toString().trim();

        Log.d(TAG,"validateData: email"+email);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.setError("Invalid Email Pattern!");
            binding.emailEt.requestFocus();
        }
        else {
            sendPasswordRecoveryInstructions();
        }
    }
    private void sendPasswordRecoveryInstructions(){
        Log.d(TAG,"sendPasswordRecoveryInstructions: ");

        progressDialog.setMessage("Sending password recovery instructions to "+email);
        progressDialog.show();

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Utils.toast(ForgotPasswordActivity.this,"Instruction to reset password is sent to "+email);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG,"onFailure",e);
                    progressDialog.dismiss();
                    Utils.toast(ForgotPasswordActivity.this,"Failed to send due to "+e.getMessage());
                });
    }
}