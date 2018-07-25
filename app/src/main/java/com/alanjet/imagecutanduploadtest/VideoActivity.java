package com.alanjet.imagecutanduploadtest;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import static android.view.View.getDefaultSize;

/**
 * Created by hongjian.chen on 2018/7/25.
 */

public class VideoActivity extends AppCompatActivity {

    String path = Environment.getExternalStorageDirectory() + "/test.mp4";
    private VideoView mVideoView;
    private TextView textView;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.video_activity);
        Uri uri = Uri.parse("http://www.uichange.com/ums3-client2/files/9071a524c1c64cfeaef7a0ec5c5247f5/jiangxiaoyu4.MP4");
        mVideoView = (VideoView) findViewById(R.id.mVideoView);
        mVideoView.setMediaController(new MediaController(this));
        Log.e("path", path);
//        mVideoView.setVideoPath(path);
        mVideoView.setVideoURI(uri);
        mVideoView.start();
//        if (!mVideoView.isPlaying ()) {
//            mVideoView.start ();
//        }
//
//        if(mVideoView.isPlaying ()){
//            mVideoView.pause ();
//        }
//        if(mVideoView!=null && mVideoView.isPlaying ()){
//            mVideoView.stopPlayback ();
//        }
//        if (mVideoView != null) {
//            mVideoView.suspend ();
//        }

    }
}
