package com.example.clipjoy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.clipjoy.R;

import java.io.Serializable;

public class DeleteVideo extends AppCompatActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    Button btnDeleteVideo;
    EditText inputParamDeleteVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_video);
        btnDeleteVideo = (Button) findViewById(R.id.chooseVideo);
        inputParamDeleteVideo = (EditText) findViewById(R.id.inputNameDeleteVideo);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //όταν πατήσει το delete πηγαίνει στο Activity tou VideoInfoDelete
        btnDeleteVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext() , VideoInfoDelete.class);
                intent.putExtra("parameterName",inputParamDeleteVideo.getText().toString()); //βάζουμε ως παράμετρο το όνομα του βίντεο που μας πληκτρολόγησαν στο EditText
                startActivity(intent);
            }
        });

    }
}