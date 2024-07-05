package com.example.clipjoy;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.clipjoy.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Distributed_Project.utils.Message;
import Distributed_Project.utils.Variables;

import static Distributed_Project.utils.Variables.BROKER_PORT_MAIN;
import static Distributed_Project.utils.Variables.BROKER_REQUEST;
import static Distributed_Project.utils.Variables.SUBSCRIBE;

//ΣΚΟΠΟΣ ΤΗΣ ΚΛΑΣΗΣ ΑΥΤΗΣ ΕΙΝΑΙ ΝΑ ΣΤΕΛΝΕΙ ΜΗΝΥΜΑ ΑΝ ΒΓΕΙ ΝΕΟ ΒΙΝΤΕΟ ΜΕ ΚΑΠΟΙΟ HASHTAG ΠΟΥ ΕΧΕΙ ΚΑΝΕΙ SUBSCRIBE O ΧΡΗΣΤΗΣ
//ΑΛΛΑ ΔΕΝ ΔΟΥΛΕΥΕΙ ΟΠΩΣ ΘΑ ΘΕΛΑΜΕ
public class SubscribeHasTag extends AppCompatActivity {

    Button btnSendSubscribeHashTag;
    EditText inputParamSubscribeHashTag;
    View v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe_has_tag);

        btnSendSubscribeHashTag = (Button) findViewById(R.id.sendBtnSubscribeHashTag);
        inputParamSubscribeHashTag = (EditText)findViewById(R.id.inputNameSubscribeHashTag);
    }

    @Override
    protected void onStart() {
        super.onStart();

        btnSendSubscribeHashTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v = view;
                String hashTag = inputParamSubscribeHashTag.getText().toString();
                SubscribeToaHashTag subscribeToaHashTag = new SubscribeToaHashTag();
                subscribeToaHashTag.execute(hashTag);

            }
        });

    }

    public class SubscribeToaHashTag extends AsyncTask<String,View,Void> {

        public Socket sendRequestSocket = null; //υποδοχή για να στείλει δεδομένα στον Broker
        public ObjectOutputStream outFromConsumer = null; //ροή για να στείλει δεδομένα στον Broker
        public ObjectInputStream inToConsumer = null; //ροή για να πάρει δεδομένα απο τον Broker

        @SuppressLint("WrongThread")
        @Override
        protected Void doInBackground(String... voids) {
            Log.e("DEBUG SUBSCRIBE",voids[0]);
            subscribeToHashtag(voids[0]);
            ConsumerManager manager = new ConsumerManager();
            manager.execute(inToConsumer); //Method execute must be called from the main thread, currently inferred thread is worker thread
            return null;
        }

        public void subscribeToHashtag(String hashtag) { //μέθοδος θα κρατήσει ένα κοννεκτιον ανοιχτό το οποίο θα ενημερώνει τον κονσουμερ
            //ότι βγηκε νεο βιντεο με αυτό το hashtag οπότε θα μπορεί ο κονσούμερ να
            //τα ξαναζητησει ολα με το case 1 και να ρθουν και τα καινουρια βιντεο πλεον
            try {
                //Δημιουργία υποδοχής με ip και port
                sendRequestSocket = new Socket("10.0.2.2", BROKER_PORT_MAIN); //ανοίγει socket προς τον broker με port 1004, όπου τον ονομάσαμε BrokerMainInfo, και τον αξιοποιούμε για να μας γυρίζει το ip και port
                //του broker που είναι υπευθυνος για αυτό το hashtag μέσω της διαδικασίας του consistent hashing
                // Προετοιμασία ObjectOutputStream
                outFromConsumer = new ObjectOutputStream(sendRequestSocket.getOutputStream());
                //Προετοιμασία ObjectInputStream
                inToConsumer = new ObjectInputStream(sendRequestSocket.getInputStream());
                Message brokerRequest = new Message(BROKER_REQUEST); // δημιουργόυμε το μήνυμα με μηνυμα BROKER_REQUEST που θα σταθλεί στον broker με port 1004[BrokerMainInfo]

                brokerRequest.setHashtag(hashtag); //βάζουμε στο μήνυμα μέσα να υπάρχει το hashtag για να μπορεί να γίνει η διαδικασία με το hashing του στον broker
                brokerRequest.requestType =  Variables.RequestType.HASHTAG; //ορίζουμε το RequestType του μηνύματος να είναι HASHTAG για να γίνει η ανάλογη διαδικασία

                outFromConsumer.writeObject(brokerRequest); //Χρήση της μεθόδου writeObject της ObjectOutputStream για να στείλει ένα Object στον Broker και στέλνουμε το μήνυμα
                outFromConsumer.flush();

                Message response = (Message) inToConsumer.readObject(); //εδώ μέσω της μεθόδου readObject() λαμβάνουμε ως μήνυμα την απάντηση του broker με port 1004[BrokerMainInfo]
                //όπου μέσα στο μήνυμα έχουν σεταριστεί οι πληροφορίες(ip,port) του broker που θα χρειαστεί να συνδεθουμε για να
                //ξεκινήσει η σύνδεση με τον broker που έχει αναλάβει αυτό το hashtag

                //disconnect για να βαλουμε νεο socket με τον σωστο broker
                inToConsumer.close();
                outFromConsumer.close();
                sendRequestSocket.close();

                //Δημιουργία υποδοχής με ip και port με βάση τα ip kai port του broker που γύρισε ο broke με port 1004[BrokerMainInfo]
                sendRequestSocket = new Socket("10.0.2.2", response.getBrokerInfo().getPort());
                //Προετοιμασία ObjectOutputStream
                outFromConsumer = new ObjectOutputStream(sendRequestSocket.getOutputStream());
                //Προετοιμασία ObjectInputStream
                inToConsumer = new ObjectInputStream(sendRequestSocket.getInputStream());

                Message videaki = new Message(SUBSCRIBE); // δημιουργόυμε το μήνυμα με μηνυμα VIDEO_REQUEST που θα σταθλεί στον broker τον σωστό
                videaki.setHashtag(hashtag);
                videaki.requestType =  Variables.RequestType.HASHTAG;

                outFromConsumer.writeObject(videaki); //Χρήση της μεθόδου writeObject της ObjectOutputStream για να στείλει ένα Object στον Broker και στέλνουμε το μήνυμα
                outFromConsumer.flush();

                Snackbar mySnackbar = Snackbar.make(v, "You Have Been Subscribed", Snackbar.LENGTH_LONG);
                mySnackbar.show();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                Log.e("Connection will","continue to be open to inform you for new videos posted by hashtag" + hashtag);
            }
        }


        class ConsumerManager extends AsyncTask<ObjectInputStream,Void,Void>{
            @Override
            protected Void doInBackground(ObjectInputStream... voids) {
                try {
                    while (true) { //receive subscription messages(μένει πάντα ανοιχτό)
                        Message mes = null;
                        mes = (Message) voids[0].readObject();

                        Snackbar mySnackbar = Snackbar.make(v, "A new video is available!!!", Snackbar.LENGTH_LONG);
                        mySnackbar.show();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
    }
}