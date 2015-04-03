package com.wp.androidftpclient;

/**
 * Created by wangpeng on 15-4-3.
 */
public interface IRetrieveListener {
    public void onStart();
    public void onTrack(long nowOffset);
    public void onError(Object obj, int type);
    public void onCancel(Object obj);
    public void onDone();
}