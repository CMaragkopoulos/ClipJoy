package com.example.clipjoy;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.Serializable;

import Distributed_Project.utils.PublisherImp;
import Distributed_Project.utils.SingletonClass;


public class PublisherActivity extends AppCompatActivity implements Serializable {

    public static final long serialVersionUID = 123456789L; //This used by Serializable

    Button publisherBtn1;
    Button publisherBtn2;
    Button publisherBtn3;
    Button publisherBtn4;
    PublisherImp publisher;
    SingletonClass single;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publisher);

        publisherBtn1 = (Button) findViewById(R.id.publisherBtn1);
        publisherBtn2 = (Button) findViewById(R.id.publisherBtn2);
        publisherBtn3 = (Button) findViewById(R.id.publisherBtn3);
        publisherBtn4 = (Button) findViewById(R.id.publisherBtn4);

        single = SingletonClass.getInstance(); //καλούμε την getInstance() της Singleton κλάσης μας
        publisher = single.getPublisher(); //παίρνουμε το instance του Publisher μας
    }

    @Override
    protected void onStart() {
        super.onStart();

        //όταν πατήσει το κουμπί που λέει SET A NAME TO YOUR CHANNEL πηγαίνει στο Activity του SetNameToChannel όπου εκεί ορίζει το όνομα του channelName
        publisherBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SetNameToChannel.class);
                startActivity(intent);
            }
        });

        //όταν πατήσει το κουμπί που λέει UPLOAD A NEW VIDEO TO YOUR CHANNEL πηγαίνει στο Activity του SelectNameOfVideo όπου εκεί ορίζει το όνομα του βίντεο που θα ανεβάσουμε απ το gallery
        publisherBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SelectNameOfVideo.class);
                startActivity(intent);
            }
        });

        //όταν πατήσει το κουμπί που λέει DELETE A VIDEO FROM YOUR CHANNEL πηγαίνει στο Activity του DeleteVideo όπου εκεί διαγράφουμε ένα βίντεο απ' το κανάλι δίνοντας το όνομα του βίντεο
        publisherBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DeleteVideo.class);
                startActivity(intent);
            }
        });

        //όταν πατήσει το κουμπί που λέει RECORD πηγαίνει στο Activity του SelectNameOfVideoRecord όπου εκεί ορίζει το όνομα του βίντεο που θα ανεβάσουμε ενώ το έχουμε κάνει record
        publisherBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SelectNameOfVideoRecord.class);
                startActivity(intent);
            }
        });
    }
}