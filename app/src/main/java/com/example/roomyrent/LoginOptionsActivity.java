package com.example.roomyrent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.example.roomyrent.databinding.ActivityLoginOptionsBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class LoginOptionsActivity extends AppCompatActivity {
    private static final String TAG = "LOGIN_OPTIONS_TAG";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private GoogleSignInClient mgoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.roomyrent.databinding.ActivityLoginOptionsBinding binding = ActivityLoginOptionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mgoogleSignInClient = GoogleSignIn.getClient(this, gso);
        binding.closeBtn.setOnClickListener(v -> onBackPressed());
        binding.loginEmailBtn.setOnClickListener(v -> startActivity(new Intent(LoginOptionsActivity.this, LoginEmailActivity.class)));
        binding.loginPhoneBtn.setOnClickListener(v -> startActivity(new Intent(LoginOptionsActivity.this,LoginPhoneActivity.class)));
        binding.loginGoogleBtn.setOnClickListener(v -> beginGoogleLogin());
    }
    private void beginGoogleLogin(){
        Log.d(TAG,"beginGoogleLogin: ");
        Intent googleSignInIntent = mgoogleSignInClient.getSignInIntent();
        //googleSignInnARL.launch(googleSignInIntent);
        startActivityForResult(googleSignInIntent,101);
    }
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == 101){
            if (resultCode == RESULT_OK){
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d(TAG,"onActivityResult: Account ID: "+account.getId());
                    firebaseAuthWithGoogleAccount(account.getIdToken());
                }catch (ApiException e){
                    Log.e(TAG,"onActivityResult: ",e);
                }
            }
            else {
                Log.d(TAG,"onActivityResult: Cancelled");
                //startActivity(new Intent(LoginOptionsActivity.this,MainActivity.class));
                finish();
            }
        }


    }
   // private final ActivityResultLauncher<Intent>googleSignInnARL = registerForActivityResult(
           // new ActivityResultContracts.StartActivityForResult(),
           // result -> {
             //   Log.d(TAG,"onActivityResult: ");

         //   }
   // );
    private void firebaseAuthWithGoogleAccount(String idToken){
        Log.d(TAG,"firebaseAuthWithGoogleAccount: idToken: "+idToken);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    if (Objects.requireNonNull(authResult.getAdditionalUserInfo()).isNewUser()){
                        Log.d(TAG,"onSuccess: New User,Account created...");
                        updateUserInfoDb();
                    }
                    else {
                        Log.d(TAG,"onSuccess: Existing User, Logged In...");
                        startActivity(new Intent(LoginOptionsActivity.this,MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "onFailure: ",e));
    }
    private void updateUserInfoDb(){
        Log.d(TAG,"updateUserInfoDb");
        progressDialog.setMessage("Saving User Info");
        progressDialog.show();
        long timestamp=Utils.getTimestamp();
        String registerUserEmail = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail();
        String registerUserUId = firebaseAuth.getUid();
        String name = firebaseAuth.getCurrentUser().getDisplayName();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("name", ""+name);
        hashMap.put("phoneCode", "");
        hashMap.put("phoneNumber", "");
        hashMap.put("profileImageUrl", "");
        hashMap.put("dob", "");
        hashMap.put("UserType", "Google");
        hashMap.put("typingTo", "");
        hashMap.put("timestamp", timestamp);
        hashMap.put("onlineStatus", true);
        hashMap.put("email", registerUserEmail);
        hashMap.put("uid", registerUserUId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
        assert registerUserUId != null;
        ref.child(registerUserUId)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG,"onSuccess: User info saved...");
                    progressDialog.dismiss();

                    startActivity(new Intent(LoginOptionsActivity.this,MainActivity.class));
                    finishAffinity();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG,"onFailure",e);
                   progressDialog.dismiss();
                    Utils.toast(LoginOptionsActivity.this,"Failed to save user info due to"+e.getMessage());
                });
    }
}