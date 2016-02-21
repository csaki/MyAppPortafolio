package com.devsaki.myappportafolio.domain;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by DevSaki on 28/01/2016.
 */
public class Video implements Parcelable{

    String name;
    String site;
    String key;
    String type;

    public Video(Parcel in) {
        this(in.readString(), in.readString(), in.readString(), in.readString());
    }

    public Video(String name, String site, String key, String type) {
        this.name = name;
        this.site = site;
        this.key = key;
        this.type = type;
    }

    public Video() {
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(site);
        dest.writeString(key);
        dest.writeString(type);
    }
}
