package Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.rahul.wellshot.welladmin.R;
import java.util.List;
import Models.DailyShots;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.ImageViewHolder> {


    public Context mContext;
    public static List<DailyShots> mWallpapers;
    public OnItemClickListener onItemClickListener;
    public DatabaseReference mPhoneName = FirebaseDatabase.getInstance().getReference().child("DailyShots");


    public CommunityAdapter(Context mContext, List<DailyShots> mWallpapers) {
        this.mContext = mContext;
        this.mWallpapers = mWallpapers;
    }




    public interface OnItemClickListener {
        void OnItemClick(int position);
        void OnItemLongClick(int position);
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_details,parent,false);
        return new CommunityAdapter.ImageViewHolder(view);
    }



    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView cardImage;
        public ProgressBar progressBar;
        public TextView userName;
        private CircularImageView accountPic;
        public ImageButton optionsButton;
        public RelativeLayout deviceName;
        public TextView deviceDetails;

        public ImageViewHolder(View itemView) {
            super(itemView);

            cardImage = (ImageView) itemView.findViewById(R.id.image_thumbnail);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
            userName = (TextView) itemView.findViewById(R.id.userNameID);
            accountPic = (CircularImageView) itemView.findViewById(R.id.accountPicID);
            optionsButton = (ImageButton) itemView.findViewById(R.id.optionsID);
            deviceName = (RelativeLayout) itemView.findViewById(R.id.phoneNameID);
            deviceDetails = (TextView) itemView.findViewById(R.id.deviceTextID);



            optionsButton.setOnClickListener(this);



        }

        @Override
        public void onClick(View v) {
            onItemClickListener.OnItemClick(this.getLayoutPosition());
        }

//        @Override
//        public boolean onLongClick(View v) {
//            onItemClickListener.OnItemLongClick(this.getLayoutPosition());
//            return true;
//        }
    }





    @Override
    public void onBindViewHolder(final CommunityAdapter.ImageViewHolder holder, int position)  {
        final DailyShots firebaseDailyShots = mWallpapers.get(position);
        String imageUrl = "";
        imageUrl = firebaseDailyShots.getCompressedURL();
        final String postKey = firebaseDailyShots.getmKey();

        if (firebaseDailyShots.getUserName()!=null){
            holder.userName.setText(firebaseDailyShots.getUserName().trim());
        }
        //holder.likes.setText("10 Likes");

        mPhoneName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postKey).hasChild("deviceName")){
                    holder.deviceName.setVisibility(View.VISIBLE);
                    holder.deviceDetails.setText("Shot on " + firebaseDailyShots.getDeviceName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (firebaseDailyShots.getUserPic()!=null){

            Glide.with(mContext)
                    .load(firebaseDailyShots.getUserPic().trim())
                    .into(holder.accountPic);
        }


        holder.progressBar.setVisibility(View.VISIBLE);
        Glide.with(mContext).load(imageUrl)
                .thumbnail(0.5f)
                .transition(withCrossFade())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.cardImage.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(holder.cardImage);

    }


    @Override
    public int getItemCount() {
        return mWallpapers.size();
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public List<DailyShots> getItemList(){
        return mWallpapers;
    }



}
