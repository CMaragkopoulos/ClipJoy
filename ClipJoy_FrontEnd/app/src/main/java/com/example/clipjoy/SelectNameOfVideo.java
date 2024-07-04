package com.example.clipjoy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.clipjoy.R;

public class SelectNameOfVideo extends AppCompatActivity {

    Button btnContinue;
    EditText inputParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_name_of_video);
        btnContinue = (Button) findViewById(R.id.continueBtn2);
        inputParam = (EditText)findViewById(R.id.inputVideoName);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //μόλις πατηθεί το κουμπί continue  πηγαίνει στο Activity του UploadVideo
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext() , UploadVideo.class);
                intent.putExtra("parameterName",inputParam.getText().toString()); //στέλνω το όνομα του καναλιού που μας πληκτρολόγησαν
                startActivity(intent);
            }
        });

    }
}