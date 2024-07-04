package com.example.clipjoy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.clipjoy.R;

public class VideoPlayer extends AppCompatActivity {

    VideoView videoView;
    MediaController mediaController;
    public TextView txtParam;
    String videoName;
    String videoPath;
    String videoNameWithoutMp4;
    //MediaMetadataRetriever m; //to be used to get info of video

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        txtParam = (TextView) findViewById(R.id.txtParamVideo);

        Intent intent = getIntent();
        videoName = intent.getStringExtra("videoName"); //λαμβάνουμε το String με το όνομα του βίντεο που μας δώσαν στο προηγούμενο Activity
        videoPath = intent.getStringExtra("path"); //λαμβάνουμε το String με το path του βίντεο που μας δώσαν στο προηγούμενο Activity

        String message = txtParam.getText().toString();

        videoNameWithoutMp4 = videoName.replace(".mp4", ""); //βγάζουμε το .mp4 απ το όνομα του βίντεο να μην φάινεται στην οθόνη μας πάνω απ' το βίντεο

        txtParam.setText(message + " " + videoNameWithoutMp4);

        videoView = findViewById(R.id.videoViewMenu); //βάζουμε στο TextView μας δίπλα το όνομα του βίντεο

        //διαδικασία για να παίξει το βίντεο
        videoView.setVideoPath(videoPath);
        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        //για τα metadata του βίντεο, δεν προλάβαμε να το κάνουμε

//        m = new MediaMetadataRetriever();
//        m.setDataSource(videoPath);
//
//        if (Build.VERSION.SDK_INT >= 17) {
//            String s = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
//        }
//      video_width = Integer.valueOf(m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
//      video_height = Integer.valueOf(m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
//
//      close object
//      retriever.release();
    }
}