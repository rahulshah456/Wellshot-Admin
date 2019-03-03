package Fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import Fragment.FileUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rahul.wellshot.welladmin.MainActivity;
import com.rahul.wellshot.welladmin.R;
import com.rahul.wellshot.welladmin.SettingsActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import Models.CollectionData;
import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import mabbas007.tagsedittext.TagsEditText;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.rahul.wellshot.welladmin.NavigationActivity.googleApiClient;

public class CollectionFragment extends Fragment {

    private static final String TAG = CollectionFragment.class.getSimpleName();
    private ImageView optionsButton;
    private FirebaseAuth mAuth;
    private Context mContext;
    private EditText title,size;
    private TagsEditText tags;
    private ImageView imageView;
    private Button uploadCollection;
    private NumberProgressBar progressBar;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseDatabase database;
    private File actualImage,compressedImage;
    private int SELECT_PICTURES = 1;
    private Uri imageUri,compressedImageUri;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collection,container,false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = getActivity();
        mAuth = FirebaseAuth.getInstance();
        optionsButton = (ImageView) view.findViewById(R.id.optionsButtonID);
        imageView = (ImageView) view.findViewById(R.id.pickImageID);
        progressBar = (NumberProgressBar) view.findViewById(R.id.progressBarID);
        title = (EditText) view.findViewById(R.id.titleEditTextID);
        size = (EditText) view.findViewById(R.id.totalEditTextID);
        tags = (TagsEditText) view.findViewById(R.id.tagsEditText);
        uploadCollection = (Button) view.findViewById(R.id.uploadCollectionID);

        //Firebase
        storageReference = FirebaseStorage.getInstance().getReference("Collections");
        databaseReference = FirebaseDatabase.getInstance().getReference("Collections");
        database = FirebaseDatabase.getInstance();

        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext,optionsButton);
                popupMenu.getMenuInflater().inflate(R.menu.menu_main,popupMenu.getMenu());



                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if (item.getItemId() == R.id.signout_menuID){

                            if (!googleApiClient.isConnected()){
                                googleApiClient.connect();
                                mAuth.signOut();
                                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(@NonNull Status status) {
                                        Log.d(TAG,"Sign out successfully");
                                    }
                                });
                            }else {
                                mAuth.signOut();
                                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(@NonNull Status status) {
                                        Log.d(TAG,"Sign out successfully");
                                    }
                                });
                            }



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



        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURES);
            }
        });

        title.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                if (b){
                    title.setBackground(getResources().getDrawable(R.drawable.edittext_back2));
                    title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    title.setHint("");
                }else {
                    title.setBackground(getResources().getDrawable(R.drawable.edittext_back1));
                    if (title.getText()!=null){
                        title.setHint("collection title");
                        title.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                    }
                }


            }
        });


        size.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                if (b){
                    size.setBackground(getResources().getDrawable(R.drawable.edittext_back2));
                    size.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    size.setHint("");
                }else {
                    size.setBackground(getResources().getDrawable(R.drawable.edittext_back1));
                    if (size.getText()!=null){
                        size.setHint("collection size");
                        size.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                    }
                }


            }
        });


        tags.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                if (b){
                    tags.setBackground(getResources().getDrawable(R.drawable.edittext_back2));
                    tags.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    tags.setHint("");
                }else {
                    tags.setBackground(getResources().getDrawable(R.drawable.edittext_back1));
                    if (tags.getText()!=null){
                        tags.setHint("collection tags");
                        title.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                    }
                }


            }
        });


        uploadCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + "JPEG");
                final String uploadId = databaseReference.push().getKey();
                final Uri[] thumbUri = {compressImage(imageUri)};

                if (thumbUri[0] !=null){
                    fileReference.putFile(thumbUri[0])
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            String collectionTitle = title.getText().toString();
                            final String collectionSize = size.getText().toString();
                            String collectionTags = tags.getText().toString();
                            CollectionData collectionData = new CollectionData(taskSnapshot.getDownloadUrl().toString(),collectionTitle,collectionTags,collectionSize);
                            databaseReference.child(uploadId).setValue(collectionData);
                            Toast.makeText(mContext, "Creates Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });


                }

//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        Toast.makeText(mContext, "Collection Created", Toast.LENGTH_SHORT).show();
//                        imageView.setImageDrawable(null);
//                        title.getText().clear();
//                        size.getText().clear();
//                        tags.getText().clear();
//
//
//                    }
//                }, 2500);

            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PICTURES){
            if (resultCode == getActivity().RESULT_OK){
                if (data.getData()!=null){
                    Glide.with(mContext)
                            .load(data.getData())
                            .transition(withCrossFade())
                            .apply(new RequestOptions()
                            .centerCrop())
                            .into(imageView);

                    imageUri = data.getData();
                    uploadCollection.setEnabled(true);
                    uploadCollection.setBackground(getResources().getDrawable(R.drawable.round_button));

                }
            }
        }
    }



    public Uri compressImage(Uri originalUri) {

        try {
            actualImage = FileUtil.from(mContext, originalUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (actualImage == null) {
//            Toast.makeText(UploadActivity.this,"Please choose an image!",Toast.LENGTH_LONG).show();
        } else {
            // Compress image in main thread using custom Compressor
            new Compressor(mContext)
                    .compressToFileAsFlowable(actualImage)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<File>() {
                        @Override
                        public void accept(File file) {
                            compressedImage = file;
//                            setCompressedImage();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            throwable.printStackTrace();
                            Toast.makeText(mContext,throwable.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
            try {
                compressedImage = new Compressor(mContext)
                        .setMaxWidth(480)
                        .setMaxHeight(320)
                        .setQuality(50)
                        .setCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath())
                        .compressToFile(actualImage);

                compressedImageUri = getImageContentUri(mContext,compressedImage);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(mContext,e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
        return compressedImageUri;
    }


    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }


}
