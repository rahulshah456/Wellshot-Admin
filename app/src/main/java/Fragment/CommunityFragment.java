package Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rahul.wellshot.welladmin.MainActivity;
import com.rahul.wellshot.welladmin.R;
import com.rahul.wellshot.welladmin.SettingsActivity;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.rahul.wellshot.welladmin.NavigationActivity.googleApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import Adapters.CommunityAdapter;
import Adapters.ConnectionDetector;
import Models.DailyShots;

public class CommunityFragment extends Fragment {

    private static final String TAG = CollectionFragment.class.getSimpleName();
    public static final String EVENTBUSKEY_REFRESH_PAGER_ADAPTER = "Refresh Adapter";
    private static final String ADDMOB_APP_ID = "ca-app-pub-8215970961458765~4792719695";
    public static final String BUNDLE_LIST_STATE = "list_state";
    private static final String PREF_NAME = "myPreference";
    public static final String CONTENT = "contentType";
    //private AdView mAdView;
    private DatabaseReference recyclerDatabaseReference,likeDatabase;
    private CommunityAdapter imageListAdaper;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private ValueEventListener eventListener;
    private RecyclerView imageRecyclerView;
    private ImageView noConnectionGif;
    private RelativeLayout loadingLayout,connectionLost;
    public static List<DailyShots> wallpaperList;
    private SharedPreferences preferences;
    private DailyShots wallpapers;
    private String mLastKey = null;
    private RecyclerView.LayoutManager mLayoutManager;
    private Parcelable mListState;
    private Animation slide_down,slide_up;
    private ConnectionDetector networkState;
    private SwipeRefreshLayout swipeLayout;
    private ImageView optionsButton;
    private Context mContext;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mContext = getActivity();
        mAuth = FirebaseAuth.getInstance();
        optionsButton = (ImageView) view.findViewById(R.id.optionsButtonID);

        imageRecyclerView = (RecyclerView) view.findViewById(R.id.images_recycler_view);
        connectionLost = (RelativeLayout) view.findViewById(R.id.noConnectionLayoutID);
        loadingLayout = (RelativeLayout) view.findViewById(R.id.loadingRecyclerID);
        noConnectionGif = (ImageView) view.findViewById(R.id.no_connectionImageID);
        networkState = new ConnectionDetector(mContext);
        swipeLayout = view.findViewById(R.id.swipeRefreshID);
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        recyclerDatabaseReference = FirebaseDatabase.getInstance().getReference("DailyShots");
        preferences = mContext.getSharedPreferences(PREF_NAME,0);

        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext,optionsButton);
                popupMenu.getMenuInflater().inflate(R.menu.menu_main,popupMenu.getMenu());



                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if (item.getItemId() == R.id.signout_menuID){

                            mAuth.signOut();
                            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                                @Override
                                public void onResult(@NonNull Status status) {
                                    Log.d(TAG,"Sign out successfully");
                                }
                            });

                            Intent intent = new Intent(mContext,MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);


                        } else if (item.getItemId() == R.id.exit_menuID){

                            Objects.requireNonNull(getActivity()).finish();

                        }else if (item.getItemId() == R.id.settings_menuID){

                            Intent intent = new Intent(mContext,SettingsActivity.class);
                            startActivity(intent);

                        }

                        return true;
                    }
                });

                popupMenu.show();
            }
        });

        CheckNetworkConnection();
        downloadImages();



        // Adding Listener
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // refresh layouts
                //Toast.makeText(mContext, "Works!", Toast.LENGTH_LONG).show();
                CheckNetworkConnection();
                connectionLost.setVisibility(View.INVISIBLE);
                imageRecyclerView.setVisibility(View.VISIBLE);
                downloadImages();
                // To keep animation for 4 seconds
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        // Stop animation (This will be after 3 seconds)
                        swipeLayout.setRefreshing(false);
                    }
                }, 4000); // Delay in millis
            }
        });

        // Scheme colors for animation
        swipeLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );

        //Load animation
        slide_down = AnimationUtils.loadAnimation(mContext, R.anim.slide_down);
        slide_up = AnimationUtils.loadAnimation(mContext, R.anim.slide_up);





        // Load more images onScroll end
        imageRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);


//                // Check if end of page has been reached
//                if( !isLoading && ((LinearLayoutManager)mLayoutManager).findLastVisibleItemPosition() == imageListAdaper.getItemCount()-1 ){
//                    isLoading = true;
//                    Log.d(TAG , "End has reached, loading more images!");
//                    loadingLayout.startAnimation(slide_up);
//                    loadingLayout.setVisibility(View.VISIBLE);
//                    page++;
//                    downloadMoreImages();
//                }
            }
        });


    }





    public void CheckNetworkConnection(){
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    imageRecyclerView.setVisibility(View.VISIBLE);
                    noConnectionGif.setVisibility(View.INVISIBLE);
                    connectionLost.setVisibility(View.INVISIBLE);
                } else {
                    imageRecyclerView.setVisibility(View.GONE);
                    Glide.with(mContext)
                            .asGif()
                            .load(R.drawable.no_connection)
                            .transition(withCrossFade())
                            .apply(new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                            .into(noConnectionGif);
                    connectionLost.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });
    }




    //Fetch and load images via FirebaseDatabase
    public void downloadImages(){

        wallpaperList = new ArrayList<>();
        @SuppressLint("CommitPrefEdits") final SharedPreferences.Editor editor = preferences.edit();

        eventListener = recyclerDatabaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                wallpaperList.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    wallpapers = postSnapshot.getValue(DailyShots.class);

                    wallpapers.setmKey(postSnapshot.getKey());
                    wallpaperList.add(wallpapers);
                    Collections.reverse(wallpaperList);

                }
                editor.putInt("uploadCount",wallpaperList.size());
                editor.commit();
                // Set up RecyclerView
                mLayoutManager = new GridLayoutManager(mContext, 1);
                imageListAdaper = new CommunityAdapter(mContext,wallpaperList);
                imageRecyclerView.setLayoutManager(mLayoutManager);
                imageRecyclerView.setItemAnimator(new DefaultItemAnimator());
                imageRecyclerView.setAdapter(imageListAdaper);
                mLastKey = dataSnapshot.getRef().push().getKey();
                imageListAdaper.setOnItemClickListener(new CommunityAdapter.OnItemClickListener() {
                    @Override
                    public void OnItemClick(final int position) {
                        //Click Event Here
                        PopupMenu popupMenu = new PopupMenu(mContext,imageRecyclerView.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.optionsID));
                        popupMenu.getMenuInflater().inflate(R.menu.item_menu,popupMenu.getMenu());


                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                if (item.getItemId() == R.id.delete_menuID){

                                    DailyShots selectedItem = wallpaperList.get(position);
                                    final String selectedKey = selectedItem.getmKey();

                                    final StorageReference compressedImage = storage.getReferenceFromUrl(selectedItem.getCompressedURL());

                                    compressedImage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            recyclerDatabaseReference.child(selectedKey).removeValue();
                                            Toast.makeText(mContext,"Image Successfully Deleted",Toast.LENGTH_LONG).show();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(mContext,e.getMessage(),Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                                return true;
                            }
                        });

                        popupMenu.show();
                    }

                    @Override
                    public void OnItemLongClick(int position) {

                    }
                });
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }





    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mListState!=null){
            mListState = mLayoutManager.onSaveInstanceState();
            outState.putParcelable(BUNDLE_LIST_STATE, mListState);
        }
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onResume() {
        if(mListState != null && imageRecyclerView !=null) {
            if (imageRecyclerView.getLayoutManager()!=null){
                Log.d(TAG, "onResume: Restoring list state ...");
                mLayoutManager.onRestoreInstanceState(mListState);
            }
        }else {
            Log.d(TAG, "onResume: ListState empty!");
        }
        super.onResume();
    }

}
