package com.example.roomyrent.activities;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.example.roomyrent.Utils;
import com.example.roomyrent.databinding.ActivityLoginEmailBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LoginEmailActivity extends AppCompatActivity {
    private ActivityLoginEmailBinding binding;
    private static final String TAG="LOGIN_TAG";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        binding.toolbarBackBtn.setOnClickListener(v -> onBackPressed());
        binding.noAccountTv.setOnClickListener(v -> startActivity(new Intent(LoginEmailActivity.this, RegisterEmailActivity.class)));
        binding.forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginEmailActivity.this, ForgotPasswordActivity.class));
            }
        });
        binding.loginBtn.setOnClickListener(v -> validateData());
        firebaseAuth = FirebaseAuth.getInstance();
    }
    private String email,password;
    @SuppressLint("SetTextI18n")
    private void validateData(){
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString();
        Log.d(TAG,"validateData: email: "+email);
        Log.d(TAG,"validateData: password: "+password);
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.setText("Invalid Email");
            binding.emailEt.requestFocus();
        } else if (password.isEmpty()) {
            binding.passwordEt.setError("Enter Password");
            binding.passwordEt.requestFocus();
        }else {
            loginUser();
        }
    }
    private void loginUser(){
        progressDialog.setMessage("Logging In");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(authResult -> {
                    Log.d(TAG,"onSuccess: Logged In...");
                    progressDialog.dismiss();

                    startActivity(new Intent(LoginEmailActivity.this, MainActivity.class));
                    finishAffinity();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG,"onFailure:",e);
                    Utils.toast(LoginEmailActivity.this,"Failed due to "+e.getMessage());
                    progressDialog.dismiss();
                });
    }
}