package com.example.clipjoy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.clipjoy.R;

public class SelectNameOfVideoRecord extends AppCompatActivity {

    Button btnContinue;
    EditText inputParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_name_of_video_record);

        btnContinue = (Button) findViewById(R.id.continueBtn3);
        inputParam = (EditText)findViewById(R.id.inputVideoName2);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //μόλις πατήσει το continue πηγαίνουμε στο Activity tou RecordVideo
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext() , RecordVideo.class);
                intent.putExtra("parameterName",inputParam.getText().toString()); //βάοζυμε ως παράμετρο το όνομα του βίντεο που μας δώσαν στο EditText
                startActivity(intent);

            }
        });

    }
}