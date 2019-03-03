package Models;

import com.google.firebase.database.Exclude;

public class DailyShots {

    private String compressedURL;
    private String deviceName;
    private String title;
    private String userName;
    private String userPic;
    private String resolution;
    private String size;
    private String mKey;


    public DailyShots() {
    }

    public DailyShots(String compressedURL, String deviceName, String userName, String userPic) {
        this.compressedURL = compressedURL;
        this.deviceName = deviceName;
        this.userName = userName;
        this.userPic = userPic;
    }


    public DailyShots(String compressedURL, String deviceName, String title, String userName, String userPic, String resolution, String size) {
        this.compressedURL = compressedURL;
        this.deviceName = deviceName;
        this.title = title;
        this.userName = userName;
        this.userPic = userPic;
        this.resolution = resolution;
        this.size = size;
    }

    public String getUserPic() {
        return userPic;
    }

    public void setUserPic(String userPic) {
        this.userPic = userPic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getCompressedURL() {
        return compressedURL;
    }

    public void setCompressedURL(String compressedURL) {
        this.compressedURL = compressedURL;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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


