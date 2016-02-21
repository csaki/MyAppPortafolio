package com.devsaki.myappportafolio.domain;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by DevSaki on 28/01/2016.
 */
public class Review implements Parcelable {

    String author;
    String content;

    public Review() {
    }

    public Review(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public Review(Parcel in) {
        this(in.readString(), in.readString());
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.author);
        dest.writeString(this.content);
    }
}
