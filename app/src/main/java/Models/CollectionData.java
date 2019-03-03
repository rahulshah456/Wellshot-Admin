package Models;

import com.google.firebase.database.Exclude;

public class CollectionData {
    private String imageUrl;
    private String collectionTitle;
    private String tags;
    private String totalImages;
    private String mKey;


    public CollectionData() {
    }

    public CollectionData(String imageUrl, String collectionTitle, String tags, String totalImages) {
        this.imageUrl = imageUrl;
        this.collectionTitle = collectionTitle;
        this.tags = tags;
        this.totalImages = totalImages;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCollectionTitle() {
        return collectionTitle;
    }

    public void setCollectionTitle(String collectionTitle) {
        this.collectionTitle = collectionTitle;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTotalImages() {
        return totalImages;
    }

    public void setTotalImages(String totalImages) {
        this.totalImages = totalImages;
    }

    @Exclude
    public String getmKey() {
        return mKey;
    }

    @Exclude
    public void setmKey(String mKey) {
        this.mKey = mKey;
    }
}
