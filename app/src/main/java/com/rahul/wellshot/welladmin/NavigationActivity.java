package com.rahul.wellshot.welladmin;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Fragment.CollectionFragment;
import Fragment.WallpapersFragment;
import Fragment.CommunityFragment;

public class NavigationActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST = 1;
    private static final String TAG = NavigationActivity.class.getSimpleName();
    private BottomNavigationView bottomNavigationView;
    public static GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        bottomNavigationView = findViewById(R.id.bottomNavigationID);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.mainFrameID,new CollectionFragment()).commit();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestId()
                .requestProfile()
                .build();


        googleApiClient = new GoogleApiClient.Builder(NavigationActivity.this)
                .enableAutoManage(NavigationActivity.this , new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                } )
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

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


    }


    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment selectedFragment = null;

            switch (menuItem.getItemId()) {

                case R.id.collection_menuID :
                    selectedFragment = new CollectionFragment();
                    break;
                case R.id.wallpapers_menuID:
                    selectedFragment = new WallpapersFragment();
                    break;
                case R.id.community_menuID:
                    selectedFragment = new CommunityFragment();
                    break;

            }
            getSupportFragmentManager().beginTransaction().replace(R.id.mainFrameID,selectedFragment).commit();
            return true;
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        if (!googleApiClient.isConnected()){
            googleApiClient.connect();
        }
    }


    public void closeApplication(){
        this.finish();
    }
}
