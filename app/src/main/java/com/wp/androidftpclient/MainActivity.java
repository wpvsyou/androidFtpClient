package com.wp.androidftpclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;


public class MainActivity extends Activity {

    final static String TAG = "ftp_test";
    final static String ACTION_STOP_FTP_CONNECT = "stop.ftp.android.client";
    final static String ACTION_GET_FILE_FROM_FTP = "get.file.from.ftp";
    final static String ACTION_UPDATE_UI = "up.data.ui";

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_STOP_FTP_CONNECT)) {
                Log.d(TAG, "Get a action to send a stop connect message!");
                mHandler.sendEmptyMessage(MSG_STOP_CONNECT);
            } else if (intent.getAction().equals(ACTION_GET_FILE_FROM_FTP)) {
                Log.d(TAG, "Get a action to send a get file message!");
                mHandler.sendEmptyMessage(MSG_GET_FILE);
            } else if (intent.getAction().equals(ACTION_UPDATE_UI)) {
                Log.d(TAG, "Get a action to send a update ui message!");
                mHandler.sendEmptyMessage(MSG_UPDATE_UI);
            }
        }
    };

    final static int MSG_BASE = 1;
    final static int MSG_STOP_CONNECT = MSG_BASE << 1;
    final static int MSG_GET_FILE = MSG_BASE << 2;
    final static int MSG_UPDATE_UI = MSG_BASE << 3;

    TextView mTitle, mText;
    ImageView mImage;
    FTPClient mFtpClient;
    boolean mUpLoadRun = true;
    boolean mDownLoadRun = true;
    DIYTestBean mBean = new DIYTestBean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTitle = (TextView) findViewById(R.id.title);
        mText = (TextView) findViewById(R.id.text);
        mImage = (ImageView) findViewById(R.id.image);
        mFtpClient = new FTPClient();
        mFtpClient.setConnectTimeout(30 * 1000);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STOP_FTP_CONNECT);
        filter.addAction(ACTION_GET_FILE_FROM_FTP);
        filter.addAction(ACTION_UPDATE_UI);
        registerReceiver(mReceiver, filter);
        mHandler.sendEmptyMessage(MSG_BASE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    Thread mUpLoadThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                mFtpClient.connect("192.168.10.210");
                while (mUpLoadRun) {
                    Log.d(TAG, "Connected to " + "192.168.10.210" + ".");
                    Log.d(TAG, mFtpClient.getReplyString());
                    Log.d(TAG, "User login --> ");
                    mUpLoadRun = false;
                    mFtpClient.login("pekall", "pekall");
                    FTPFile[] files = mFtpClient.listFiles("/storage/psbc_demo");
                    if (null != files && files.length > 0) {
                        for (FTPFile f : files) {
                            Log.d(TAG, "Get file --> " + f.toString());
                            Log.d(TAG, "Test to get file name --> " + f.getName());
                        }
                    }
                    String json = createTestJsonData();
                    InputStream in = new ByteArrayInputStream(json.getBytes());
                    Log.d(TAG, "Check the input stream " + in.toString());
                    Log.d(TAG, "Check the up load file states ["
                            + mFtpClient.storeFile("/storage/psbc_demo/demo1", in) + "]");
                }
            } catch (SocketException e) {
                e.printStackTrace();
                Log.d(TAG, "Error : " + e);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Error : " + e);
            }
        }
    });

    Thread mDownLoadThreat = new Thread(new Runnable() {
        @Override
        public void run() {
            while (mDownLoadRun) {
                String json = null;
                try {
                    json = inputStream2String(mFtpClient.retrieveFileStream("/storage/psbc_demo/demo1"));
                    Log.d(TAG, "get input stream from ftp was done!");
                } catch (IOException e) {
                    Log.d(TAG, "get input stream from ftp was failed : " + e);
                    e.printStackTrace();
                }
                mBean = new Gson().fromJson(json, DIYTestBean.class);
                mDownLoadRun = false;
            }
        }
    });

    final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "In the mHandler the msg is [" + msg.what + "]");
            if (msg.what == MSG_STOP_CONNECT) {
                Log.d(TAG, "Get a msg to stop this fip connect!");
                mUpLoadRun = false;
                mDownLoadRun = false;
            } else if (msg.what == MSG_BASE) {
                mUpLoadThread.start();
            } else if (msg.what == MSG_GET_FILE) {
                mDownLoadThreat.start();
            } else if (msg.what == MSG_UPDATE_UI) {
                mTitle.setText(mBean.getTitle());
                mText.setText(mBean.getText());
                mImage.setImageBitmap(mBean.getImage());
            }
        }
    };

    protected String createTestJsonData() {
        DIYTestBean bean = new DIYTestBean();
        bean.text = "This is Android FTP Client demo bean text!";
        bean.title = "This is Android FTP Client demo bean title!";
        bean.image = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.ic_launcher);
        return new Gson().toJson(bean, DIYTestBean.class);
    }

    public static String inputStream2String(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = -1;
        while ((i = is.read()) != -1) {
            baos.write(i);
        }
        return baos.toString();
    }
}
