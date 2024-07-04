package com.example.clipjoy;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.clipjoy.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;

import Distributed_Project.utils.PublisherImp;
import Distributed_Project.utils.SingletonClass;

public class VideoInfoDelete extends AppCompatActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    Button btnYes;
    Button btnNo;
    String videoName;
    PublisherImp publisher;
    SingletonClass single;
    View v;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_info_delete);

        btnYes = (Button) findViewById(R.id.yesBtn);
        btnNo = (Button) findViewById(R.id.noBtn);

        Intent intent = getIntent();
        videoName = intent.getStringExtra("parameterName"); //παίρνω το όνομα του βίντεου που μας δώσαν στο προηγούμενο activity
        Log.e("Name of video on upload", videoName);

        //παίρνουμε το singleton instance του publisher μας
        single = SingletonClass.getInstance();
        publisher = single.getPublisher();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //αν πατήσει yes διαγράφει το βίντεο αν υπάρχει και δείχνει και ανάλογο μήνυμα
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v = view;

                Log.e("Video's name ", videoName);
                publisher.removeVideo(videoName); //καλουμε την removeVideo του publisher με όρισμα το όνομα του βίντεο που θέλουμε να διαγραφτεί

                //AsyncTask για την push
                DeleteVideoFinally videoDeleted = new DeleteVideoFinally();
                videoDeleted.execute(publisher);
            }
        });

        //αν πατήσει no πηγαίνει στο PublisherActivity
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v = view;
                Intent intent = new Intent(getApplicationContext() , PublisherActivity.class);
                startActivity(intent);
            }
        });

    }

    public class DeleteVideoFinally extends AsyncTask<PublisherImp,Void,Void> {

        @Override
        protected Void doInBackground(PublisherImp... publisherImps) {
            publisherImps[0].push();
            Snackbar mySnackbar = Snackbar.make(v, "You Have successfully deleted the video!", Snackbar.LENGTH_LONG);
            mySnackbar.show();
            return null;
        }
    }
}