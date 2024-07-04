package com.example.clipjoy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.clipjoy.R;

public class ConsumerActivity extends AppCompatActivity {

    Button consumerBtn1;
    Button consumerBtn2;
    Button consumerBtn3;
    Button consumerBtn4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumer);

        consumerBtn1 = (Button) findViewById(R.id.consumerBtn1);
        consumerBtn2 = (Button) findViewById(R.id.consumerBtn2);
        consumerBtn3 = (Button) findViewById(R.id.consumerBtn3);
        consumerBtn4 = (Button) findViewById(R.id.consumerBtn4);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //όταν πατήσει το κουμπί που λέει VIDEOS BY CHANNELNAME πηγαίνει στο Activity του ConsumerActivityChannelName όπου εκεί γυρνάει όλα τα βίντεο του channelName που θα ζητήσουμε
        consumerBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext() , ConsumerActivityChannelName.class);
                startActivity(intent);
            }
        });

        //όταν πατήσει το κουμπί που λέει VIDEOS BY HASHTAG πηγαίνει στο Activity του ConsumerActivityHashtag όπου εκεί γυρνάει όλα τα βίντεο με το hashtag που θα ζητήσουμε
         consumerBtn2.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(getApplicationContext(), ConsumerActivityHashTag.class);
                 startActivity(intent);
             }
         });

        //όταν πατήσει το κουμπί που λέει SUBSCRIBE TO A CHANNELNAME πηγαίνει στο Activity του SubscribeChannelName όπου εκεί κάνουμε subscribe σε κάποιο channelName
        //ώστε να στείλει μήνυμα αν βγέι νέο βίντεο απ' το κανάλι αυτό, αλλά δεν δουλεύει καλά
        consumerBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SubscribeChannelName.class);
                startActivity(intent);
            }
        });

        //όταν πατήσει το κουμπί που λέει SUBSCRIBE TO A HASHTAG πηγαίνει στο Activity του SubscribeHasTag όπου εκεί κάνουμε subscribe σε κάποιο hashtag
        //ώστε να στείλει μήνυμα αν βγέι νέο βίντεο με αυτό το hashtag, αλλά δεν δουλεύει καλά
        consumerBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SubscribeHasTag.class);
                startActivity(intent);
            }
        });

    }
}
