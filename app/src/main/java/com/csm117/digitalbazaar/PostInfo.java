package com.csm117.digitalbazaar;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by kirkzhang on 3/1/17.
 */
@IgnoreExtraProperties
public class PostInfo {
    public String title;
    public String price;
    public String description;
    public String image;
    public String posterid;
    public String latitude;
    public String longitude;
    public PostInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(PaymentInformation.class)
    }

    public PostInfo(String title, String price,String description,String image, String posterid, String latitude, String longitude) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.image = image;
        this.posterid = posterid;
        this.latitude = latitude;
        this.longitude = longitude;

    }

}
