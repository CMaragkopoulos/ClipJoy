package com.example.clipjoy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.example.clipjoy.R;

public class ConsumerActivityChannelName extends AppCompatActivity {

    Button btnSend;
    EditText inputParam; //EditText όπου θα μας δώσουν το channelName που θέλουν εκεί

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumer_channel_name);

        btnSend = (Button) findViewById(R.id.sendBtn);
        inputParam = (EditText)findViewById(R.id.inputName);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //όταν πατήσει το κουμπί που λέει send πηγαίνει στο Activity του SearchChannelName
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext() , SearchChannelName.class);
                intent.putExtra("parameterName",inputParam.getText().toString()); //ότι μας πληκτρολογήσουν στο EditText(το channelName δηλαδή) το στέλνουμε στο Activity του SearchChannelName
                startActivity(intent);

            }
        });

    }
}