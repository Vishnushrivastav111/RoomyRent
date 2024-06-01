package com.example.roomyrent.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import com.example.roomyrent.Utils;
import com.example.roomyrent.databinding.ActivityChangePasswordBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class ChangePasswordActivity extends AppCompatActivity {
    private ActivityChangePasswordBinding binding;
    private static final String TAG="CHANGE_PASS_TAG";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.toolbarBackBtn.setOnClickListener(v -> onBackPressed());
        binding.submitBtn.setOnClickListener(v -> validateData());
    }
    private String currentPassword="";
    private String newPassword="";
    private String confirmNewPassword="";

    private void validateData(){
        Log.d(TAG,"validateData: ");

        currentPassword = binding.currentPasswordEt.getText().toString().trim();
        newPassword = binding.newPasswordEt.getText().toString().trim();
        confirmNewPassword = binding.confirmNewPasswordEt.getText().toString().trim();

        Log.d(TAG,"validateData: currentPassword: "+currentPassword);
        Log.d(TAG,"validateData: newPassword: "+newPassword);
        Log.d(TAG,"validateData: confirmNewPassword: "+ confirmNewPassword);

        if (currentPassword.isEmpty()){
            binding.currentPasswordEt.setError("Enter current Password!");
            binding.currentPasswordEt.requestFocus();
        } else if (newPassword.isEmpty()) {
            binding.newPasswordEt.setError("Enter new Password!");
            binding.newPasswordEt.requestFocus();
        } else if (confirmNewPassword.isEmpty()) {
            binding.confirmNewPasswordEt.setError("Enter confirm Password!");
            binding.confirmNewPasswordEt.requestFocus();
        } else if (!newPassword.equals(confirmNewPassword)) {
            binding.confirmNewPasswordEt.setError("Password doesn't match!");
            binding.currentPasswordEt.requestFocus();
        }
        else {
            authenticationForUpdatePassword();
        }
    }
    private void authenticationForUpdatePassword(){
        Log.d(TAG,"authenticationForUpdatePassword: ");

        progressDialog.setMessage("Authenticating User");
        progressDialog.show();

        AuthCredential authCredential = EmailAuthProvider.getCredential(Objects.requireNonNull(firebaseUser.getEmail()),currentPassword);
        firebaseUser.reauthenticate(authCredential)
                .addOnSuccessListener(unused -> updatePassword())
                .addOnFailureListener(e -> {
                    Log.e(TAG,"onFailure: ",e);
                    progressDialog.dismiss();
                    Utils.toast(ChangePasswordActivity.this,"Failed to authenticate due to "+e.getMessage());
                });
    }
    private void updatePassword(){
        Log.d(TAG,"updatePassword: ");

        progressDialog.setMessage("Updating Password");
        progressDialog.show();

        firebaseUser.updatePassword(newPassword)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Utils.toast(ChangePasswordActivity.this,"Password Updated!");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG,"onFailure: ",e);
                    progressDialog.dismiss();
                    Utils.toast(ChangePasswordActivity.this,"Failed to update password due to " + e.getMessage());
                });
    }
}