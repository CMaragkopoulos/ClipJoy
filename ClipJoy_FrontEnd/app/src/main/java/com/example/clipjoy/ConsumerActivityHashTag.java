package com.example.clipjoy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.clipjoy.R;

public class ConsumerActivityHashTag extends AppCompatActivity {

    Button btnSendHashTag;
    EditText inputParamHashTag; //EditText όπου θα μας δώσουν το hashtag που θέλουν εκεί

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumer_hash_tag);

        btnSendHashTag = (Button) findViewById(R.id.sendBtnHashTag);
        inputParamHashTag = (EditText)findViewById(R.id.inputNameHashTag);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //όταν πατήσει το κουμπί που λέει send πηγαίνει στο Activity του SearchHashTag
        btnSendHashTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext() , SearchHashTag.class);
                intent.putExtra("parameterName",inputParamHashTag.getText().toString()); //ότι μας πληκτρολογήσουν στο EditText(το hashtag δηλαδή) το στέλνουμε στο Activity του SearchHashTag
                startActivity(intent);
            }
        });

    }
}