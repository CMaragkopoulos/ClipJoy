package com.example.clipjoy;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.clipjoy.R;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

import Distributed_Project.utils.PublisherImp;
import Distributed_Project.utils.PublisherThread;
import Distributed_Project.utils.SingletonClass;

//το πρώτο μας Activity, μόλις ανοίγει η εφαρμογή το δείχνει
public class MainActivity extends AppCompatActivity implements Serializable {

    public static final long serialVersionUID = 123456789L; //This used by Serializable

    Button consumerBtn;
    Button publisherBtn;
    PublisherImp publisher;
    private SingletonClass single; //αξιοποιούμε την singleton για να πάρουμε το instance του Publisher μας

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ορίζουμε τα κουμπιά μας
        consumerBtn = (Button) findViewById(R.id.consumerBtn);
        publisherBtn = (Button) findViewById(R.id.publisherBtn);

        single = SingletonClass.getInstance(); //καλούμε την getInstance() της Singleton κλάσης μας
        publisher = single.getPublisher(); //παίρνουμε το instance του Publisher μας

        //ξεκινάμε το thread που θα ανοίγει το ServerSocket του publisher μας για να μπορεί να επικοινωνεί με τους Brokers
        Thread myThread = new Thread(new MyServer());
        myThread.start();
    }


    @Override
    protected void onStart() {
        super.onStart();

        //όταν πατήσει το κουμπί που λέει consumer πηγαίνει στο Activity του ConsumerActivity
        consumerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext() , ConsumerActivity.class);
                startActivity(intent);
            }
        });

        //όταν πατήσει το κουμπί που λέει publisher πηγαίνει στο Activity του PublisherActivity
        publisherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext() , PublisherActivity.class);
                startActivity(intent);
            }
        });

    }

    class MyServer implements Runnable {
        ServerSocket serverSocket;
        Socket connection;

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void run() {
            try {
                serverSocket = new ServerSocket(publisher.currentPort); //Δημιουργία serverSocket για τον Broker
                while (true) {
                    Log.e("Publisher waiting","for incoming connection with port" + publisher.currentPort);
                    connection = serverSocket.accept(); //Σύνδεση με Broker

                    //Περνάει το connection για την υποδοχή με τον Broker και καλεί την run του
                    new PublisherThread(connection, publisher).start();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}