package Fragment;

import android.content.ContentResolver;
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
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.rahul.wellshot.welladmin.MainActivity;
import com.rahul.wellshot.welladmin.R;
import com.rahul.wellshot.welladmin.SettingsActivity;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import static com.rahul.wellshot.welladmin.NavigationActivity.googleApiClient;

public class WallpapersFragment extends Fragment {

    private static final String TAG = CollectionFragment.class.getSimpleName();
    private ImageView optionsButton;
    private FirebaseAuth mAuth;
    private Context mContext;
    private Button selectImages,compressImages,createThumbnails,uploadImages;
    private TextView imagesCount,compressedCount,thumbnailsCount;
    private NumberProgressBar progressBar;
    private int SELECT_PICTURES = 1;
    private ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
    private ArrayList<Uri> mThumbArrayUri = new ArrayList<Uri>();
    private ArrayList<Uri> mCompressedUri  = new ArrayList<Uri>();
    private ArrayList<String> mResolution = new ArrayList<>();
    private ArrayList<String> mKey = new ArrayList<String>();
    private Uri imageUri,compressedImageUri,thumbImageUri;
    private File actualImage,compressedImage;
    private int compressed = 0,position = 0,uploaded = 0,index=0 ;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseDatabase database;
    private EditText category;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallpapers,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mContext = getActivity();
        //Buttons
        optionsButton = (ImageView) view.findViewById(R.id.optionsButtonID);
        selectImages = (Button) view.findViewById(R.id.selectImagesID);
        compressImages = (Button) view.findViewById(R.id.compressImagesID);
        createThumbnails = (Button) view.findViewById(R.id.createThumbnailsID);
        uploadImages = (Button) view.findViewById(R.id.uploadImagesID);
        progressBar = (NumberProgressBar) view.findViewById(R.id.progressBarID);
        //TextViews
        imagesCount = (TextView) view.findViewById(R.id.orignalCountID);
        compressedCount = (TextView) view.findViewById(R.id.compressCountID);
        thumbnailsCount = (TextView) view.findViewById(R.id.thumbnailCountID);
        category = (EditText) view.findViewById(R.id.titleEditTextID);
        //Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Wallpapers");
        databaseReference = FirebaseDatabase.getInstance().getReference("Wallpapers");


        category.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                if (b){
                    category.setBackground(getResources().getDrawable(R.drawable.edittext_back2));
                    category.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    category.setHint("");
                }else {
                    category.setBackground(getResources().getDrawable(R.drawable.edittext_back1));
                    if (category.getText()!=null){
                        category.setHint("collection title");
                        category.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                    }
                }

            }
        });


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





        selectImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mArrayUri.clear();
                mThumbArrayUri.clear();
                mCompressedUri.clear();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURES);
            }
        });


        compressImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mArrayUri!=null){
                    while (compressed < mArrayUri.size()){
                        Uri thumbUri  = compressedFullImage(mArrayUri.get(position));
                        mCompressedUri.add(thumbUri);

                        compressed++;
                        position++;

                    }
                    Toast.makeText(mContext,String.valueOf(mCompressedUri.size())+
                            " Images Compressed Successfully",Toast.LENGTH_SHORT).show();
                    compressedCount.setText(String.valueOf(mCompressedUri.size()));
                }
                compressed = 0;
                position = 0;
            }
        });


        createThumbnails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mArrayUri!=null){
                    while (compressed < mArrayUri.size()){
                        Uri thumbUri  = thumbCompressImage(mArrayUri.get(position));
                        mThumbArrayUri.add(thumbUri);

                        compressed++;
                        position++;

                    }
                    Toast.makeText(getContext(),String.valueOf(mThumbArrayUri.size())+
                            " Thumbnails Created Successfully",Toast.LENGTH_SHORT).show();
                    thumbnailsCount.setText(String.valueOf(mThumbArrayUri.size()));
                }
                compressed = 0;
                position = 0;
            }
        });




        uploadImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                while (uploaded < mArrayUri.size()){

                    StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + "JPEG");
                    final String uploadId = databaseReference.push().getKey();
                    final String imageCategory = category.getText().toString();
                    mKey.add(uploadId);

                    fileReference.putFile(mArrayUri.get(index)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //date_Time = dateFormat.format(Calendar.getInstance().getTime());
                            //Toast.makeText(mContext, "Upload Successful" + " For Image " + (++index), Toast.LENGTH_SHORT).show();
                            databaseReference.child(uploadId).child("orignalURL").setValue(taskSnapshot.getDownloadUrl().toString());
                            databaseReference.child(uploadId).child("imageCategory").setValue(imageCategory);
                            databaseReference.child(uploadId).child("size").setValue(taskSnapshot.getTotalByteCount());
                            databaseReference.child(uploadId).child("resolution").setValue(mResolution.get(index));

                        }

                    });

                    StorageReference referenceFile = storageReference.child(System.currentTimeMillis() + "." + "JPEG");
                    referenceFile.putFile(mCompressedUri.get(index)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            databaseReference.child(uploadId).child("compressedURL").setValue(taskSnapshot.getDownloadUrl().toString());
                        }
                    });

                    StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + "JPEG");
                    reference.putFile(mThumbArrayUri.get(index)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            databaseReference.child(uploadId).child("thumbURL").setValue(taskSnapshot.getDownloadUrl().toString());
                        }
                    });

                    uploaded++;
                    index++;

                    progressBar.setProgress(uploaded);
                    Log.d("Progress",String.valueOf(progressBar.getProgress()));


                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(mContext, "Upload Success", Toast.LENGTH_SHORT).show();
//                        mThumbArrayUri.clear();
//                        mArrayUri.clear();
//                        mCompressedUri.clear();
//                        mResolution.clear();
                        imagesCount.setText("0");
                        thumbnailsCount.setText("0");
                        compressedCount.setText("0");



                    }
                }, 500);

                uploaded=0;
                index = 0;
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SELECT_PICTURES) {
            if (resultCode == getActivity().RESULT_OK) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    Log.i("count", String.valueOf(count));
                    int currentItem = 0;
                    while (currentItem < count) {
                        imageUri = data.getClipData().getItemAt(currentItem).getUri();



                        Log.i("uri", imageUri.toString());
                        mArrayUri.add(imageUri);
                        currentItem = currentItem + 1;
                        imagesCount.setText(String.valueOf(mArrayUri.size()));

                        mResolution.add(getImageResolution(imageUri));

                    }
                    createThumbnails.setEnabled(true);
                    createThumbnails.setBackground(getResources().getDrawable(R.drawable.round_button));
                    compressImages.setEnabled(true);
                    compressImages.setBackground(getResources().getDrawable(R.drawable.round_button));
                    uploadImages.setEnabled(true);
                    uploadImages.setBackground(getResources().getDrawable(R.drawable.round_button));
                    progressBar.setMax(mArrayUri.size());
                    Log.i("listsize", String.valueOf(mArrayUri.size()));
                } else if (data.getData() != null) {
                    String imagePath = data.getData().getPath();
                    Toast.makeText(mContext,"Only One Image Selected",Toast.LENGTH_SHORT).show();
                    imagesCount.setText(String.valueOf(mArrayUri.size()));

                }
            }
        }
    }

    public String getImageResolution(Uri uri ) {

        String resolution = "0";

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
            resolution = bitmap.getWidth() + " X " + bitmap.getHeight();

        } catch (IOException e) {
            Log.i("TAG", "Some exception " + e);
        }

        return resolution;
    }



    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = mContext.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    public Uri thumbCompressImage(Uri orignalImageUri) {

        try {
            actualImage = FileUtil.from(getActivity(), orignalImageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (actualImage == null) {
//            Toast.makeText(UploadActivity.this,"Please choose an image!",Toast.LENGTH_LONG).show();
        } else {
            // Compress image in main thread using custom Compressor
            new Compressor(getActivity())
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
                compressedImage = new Compressor(getActivity())
                        .setMaxWidth(400)
                        .setMaxHeight(400)
                        .setQuality(25)
                        .setCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DCIM).getAbsolutePath())
                        .compressToFile(actualImage);

                compressedImageUri = getImageContentUri(getActivity(),compressedImage);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(mContext,e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
        return compressedImageUri;
    }



    public Uri compressedFullImage(Uri orignalImageUri) {

        try {
            actualImage = FileUtil.from(mContext, orignalImageUri);
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
                        .setMaxWidth(1024)
                        .setMaxHeight(768)
                        .setQuality(80)
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
