package com.wp.androidftpclient;

import android.graphics.Bitmap;

/**
 * Created by wangpeng on 15-4-3.
 */
public class DIYTestBean {
    String title;
    String text;
    Bitmap image;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Bitmap getImage() {
        return this.image;
    }
}
