package com.example.clipjoy;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.clipjoy.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;



import Distributed_Project.utils.PublisherImp;
import Distributed_Project.utils.SingletonClass;


@RequiresApi(api = Build.VERSION_CODES.N)
public class SetNameToChannel extends AppCompatActivity implements Serializable {

    public static final long serialVersionUID = 123456789L; //This used by Serializable

    Button btnSendSetChannelName;
    EditText inputParamSetChannelName;
    PublisherImp publisher;
    private SingletonClass single;
    View v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_name_to_channel);

        btnSendSetChannelName = (Button) findViewById(R.id.sendBtnSetChannelName);
        inputParamSetChannelName = (EditText)findViewById(R.id.inputNameSetChannelName);

        //παίρνουμε το singleton instance του publisher μας
        single = SingletonClass.getInstance();
        publisher = single.getPublisher();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //όταν πατήσει το send ξεκινάει την διαδικασία αλλαγής ονόματος καναλιού
        btnSendSetChannelName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v = view;

                Log.e("Channel's old name: ", publisher.channelName.getChannelName());

                //βάζεις ως όνομα του καναλιού αυτό που πληκτρολογεί ο χρήστης στο EditText
                publisher.channelName.setChannelName(inputParamSetChannelName.getText().toString());

                //AsyncTask για να κάνει push στο background και να μην επηρρεάσει το main ui μας
                ChangeChannelName newName = new ChangeChannelName();
                //δίνουμε ως όρισμα το instance του publisher μας
                newName.execute(publisher);

                Log.e("Channel's new name1: ", inputParamSetChannelName.getText().toString());
                Log.e("Channel's new name2: ", publisher.channelName.getChannelName());
            }
        });

    }

    public class ChangeChannelName  extends AsyncTask<PublisherImp,Void,Void> {

        @Override
        protected Void doInBackground(PublisherImp... publisherImps) {
            //push στους brokers για να ξέρουν το καινούριο channelName
            publisherImps[0].push();
            //μηνυματάκι που εμφανίζεται στην εφαρμογή και σου λέει ότι άλλαξες το όνομα του καναλιού
            Snackbar mySnackbar = Snackbar.make(v, "You Have changed your channelName!", Snackbar.LENGTH_LONG);
            mySnackbar.show();
            return null;
        }
    }

 }
