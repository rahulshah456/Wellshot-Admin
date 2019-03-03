package com.rahul.wellshot.welladmin;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.ebanx.swipebtn.OnActiveListener;
import com.ebanx.swipebtn.SwipeButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private static final int RequestSignInCode = 7;
    public static GoogleApiClient googleApiClient;
    private SwipeButton googleSignIn,facebookSignIn;
    private ProgressDialog progressDialog;
    private ConstraintLayout constraintLayout;
    AnimationDrawable animationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        databaseReference = database.getReference().child("Google Users");
        googleSignIn = (SwipeButton) findViewById(R.id.signInGooogleID);
        facebookSignIn = (SwipeButton) findViewById(R.id.signInFacebookID);
        progressDialog = new ProgressDialog(this);
        constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayoutID);


        animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();



        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,Manifest.permission.ACCESS_NETWORK_STATE};
        String rationale = "Please provide storage permissions for app services";
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle("Permissions")
                .setSettingsDialogTitle("Important");

        Permissions.check(this/*context*/, permissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                // do your task.
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                // permission denied, block the feature.
                closeApplication();
            }
        });





        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestId()
                .requestProfile()
                .build();


        googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .enableAutoManage(MainActivity.this , new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                } )
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        googleSignIn.setOnActiveListener(new OnActiveListener() {
            @Override
            public void onActive() {
                UserSignInMethod();
            }
        });

        facebookSignIn.setOnActiveListener(new OnActiveListener() {
            @Override
            public void onActive() {
                Toast.makeText(MainActivity.this,"Not Available",Toast.LENGTH_LONG).show();
            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();
        if (!googleApiClient.isConnected()){
            googleApiClient.connect();
        }
        if (mAuth.getCurrentUser()!=null){
            Intent intent = new Intent(MainActivity.this,NavigationActivity.class);
            Log.d(TAG,"User Already Signed in");
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }

    public void UserSignInMethod(){
        Intent AuthIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(AuthIntent, RequestSignInCode);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestSignInCode){
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (googleSignInResult.isSuccess()){
                GoogleSignInAccount googleSignInAccount = googleSignInResult.getSignInAccount();
                if(googleSignInAccount.getEmail().toString().trim().equals("rahulkumarshah5000@gmail.com")){
                    FirebaseUserAuth(googleSignInAccount);
                }else {
                    Toast.makeText(this,"Only Admin Can Login !!!",Toast.LENGTH_SHORT).show();
                    data = null;
                    Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            Log.d(TAG,"User Succesfully Signed Out From Google Account");
                        }
                    });
                }
            }

        }
    }

    public void FirebaseUserAuth(GoogleSignInAccount account) {

        final String emailString = account.getEmail().toString().trim();
        final String firstNameString = account.getGivenName().toString().trim();
        final String lastNameString = account.getFamilyName().toString().trim();

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        Log.d(TAG, "Signing in with Google:" + account.getId());

        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> AuthResultTask) {

                        if (AuthResultTask.isSuccessful()){

                            // Getting Current Login user details.
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userId = mAuth.getCurrentUser().getUid();
                            DatabaseReference currentUser = databaseReference.child(userId);
                            currentUser.child("First Name").setValue(firstNameString);
                            currentUser.child("Last Name").setValue(lastNameString);
                            currentUser.child("Email").setValue(emailString);
                            progressDialog.dismiss();
                            Intent intent = new Intent(MainActivity.this,NavigationActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                        }else {
                            Toast.makeText(MainActivity.this,"Something Went Wrong",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    public void closeApplication(){
        this.finish();
    }

}
