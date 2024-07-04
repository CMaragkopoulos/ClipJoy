package com.example.clipjoy;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clipjoy.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import Distributed_Project.utils.Message;
import Distributed_Project.utils.Variables;

import static Distributed_Project.utils.Variables.BROKER_PORT_MAIN;
import static Distributed_Project.utils.Variables.BROKER_REQUEST;
import static Distributed_Project.utils.Variables.CHANNEL_NOT_FOUND;
import static Distributed_Project.utils.Variables.COMMUNICATION_OVER;
import static Distributed_Project.utils.Variables.VIDEO_END;
import static Distributed_Project.utils.Variables.VIDEO_REQUEST;
import static Distributed_Project.utils.Variables.VIDEOPLAYER;

public class SearchChannelName extends AppCompatActivity {

    public TextView txtParam;
    String channelName;
    ArrayList<String> videoNames; //ArrayList με τα names των βίντεο όπου θα τα βάζουμε με το που θα μας έρθουν απ τον Broker
    ArrayList<String> paths; //ArrayList με τα paths των βίντεο όπου θα τα βάζουμε με το που θα μας έρθουν απ τον Broker
    int noVideos; //το χρειαζόμαστε ώστε άμα πάρουμε μήνυμα ότι δεν υπάρχουν βίντεο του αλλάζουμε τιμή και το εκμεταλλευόμαστε

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_channel_name);

        txtParam = (TextView) findViewById(R.id.txtParam3);

        Intent intent = getIntent();
        channelName = intent.getStringExtra("parameterName"); //λαμβάνουμε το channelName που μας δώσαν στο προηγούμενο Activity

        String helloMsg = txtParam.getText().toString();
        txtParam.setText(helloMsg + " " +channelName); //βάζουμε στο TextView του Activity μας να δείχνει το channelName δίπλα

        videoNames = new ArrayList<String>();
        paths = new ArrayList<String>();

        noVideos = 0;

        MessageSender messageSender = new MessageSender(); //φτίαχνουμε το instance του AsyncTask μας που είναι πιο κάτω
        messageSender.execute(channelName); //το ξεκινάμε με όρισμα το channelName
    }

    public class MessageSender  extends AsyncTask<String,ArrayList<String>,ArrayList<String>> {

        public Socket sendRequestSocket = null; //υποδοχή για να στείλει δεδομένα στον Broker
        public ObjectOutputStream outFromConsumer = null; //ροή για να στείλει δεδομένα στον Broker
        public ObjectInputStream inToConsumer = null; //ροή για να πάρει δεδομένα απο τον Broker

        @Override
        protected ArrayList<String> doInBackground(String... voids) {
            Log.e("On back:",voids[0]);
            requestVideoByChannelName(voids[0]); //καλούμε την μέθοδο requestVideoByChannelName() με όρισμα το channelName
            if (noVideos == 100) { //σε περιπτωση που δεν υπαρχουν βιντεο απο το καναλι στέλνουμε null στην onPostExecute για να πράξει ανάλογα
                return null;
            }
            return videoNames;
        }

        @Override
        protected void onPostExecute(ArrayList<String> videoNamesNow) {
            //άμα δεν υπάρχουν βίντεο ή κανάλι ξεκινάμε το Activity του ConsumerActivityChannelName για να ξαναζητήσουν channelName
            if (videoNamesNow == null) {
                Toast.makeText(getApplicationContext(),"There are no videos by this channelName! Try another one!",Toast.LENGTH_SHORT);
                noVideos = 0;
                Intent intent = new Intent(getApplicationContext() , ConsumerActivityChannelName.class);
                startActivity(intent);
            } else { //άμα υπάρχουν βίντεο(αφού το videoNamesNow != null)
                for (String path: videoNamesNow) { //περνάμε από όλο το ArrayList με τα βίντεο που μας γύρισαν
                    paths.add(getFilesDir() + "/" + path); //βάζουμε στην λίστα με τα paths τα paths των φακέλων που φτίαξαμε στην requestVideoByChannelName
                    Log.e("Path now", path);
                }
                //μόλις έχουμε έτοιμα τα ArrayList μας τα στέλνουμε στο Activity του VideosMenu
                Intent intent = new Intent(getApplicationContext() , VideosMenu.class);
                intent.putExtra("channelName", channelName); //βάζουμε μέσα στο Intent το channelName
                intent.putStringArrayListExtra("paths", paths); //βάζουμε μέσα στο Intent το ArrayList με τα paths
                intent.putStringArrayListExtra("videoNames", videoNames); //βάζουμε μέσα στο Intent το ArrayList με τα ονόματα των βίντεο
                startActivity(intent);
            }
        }

        public void requestVideoByChannelName(String channelName) { //μέθοδος που γυρνάει όσα βίντεο έχει αυτό το channelname [CASE 1]
            try {
                //Δημιουργία υποδοχής με ip και port
                sendRequestSocket = new Socket("10.0.2.2", BROKER_PORT_MAIN); //ανοίγει socket προς τον broker με port 1004, όπου τον ονομάσαμε BrokerMainInfo, και τον αξιοποιούμε για να μας γυρίζει το ip και port
                //του broker που είναι υπευθυνος για αυτό το channelName μέσω της διαδικασίας του consistent hashing
                // Προετοιμασία ObjectOutputStream
                outFromConsumer = new ObjectOutputStream(sendRequestSocket.getOutputStream());
                //Προετοιμασία ObjectInputStream
                inToConsumer = new ObjectInputStream(sendRequestSocket.getInputStream());
                Message brokerRequest = new Message(BROKER_REQUEST); // δημιουργόυμε το μήνυμα με μηνυμα BROKER_REQUEST που θα σταθλεί στον broker με port 1004[BrokerMainInfo]

                brokerRequest.setChannelName(channelName); //βάζουμε στο μήνυμα μέσα να υπάρχει το channelName για να μπορεί να γίνει η διαδικασία με το hashing του στον broker
                brokerRequest.requestType = Variables.RequestType.CHANNELNAME; //ορίζουμε το RequestType του μηνύματος να είναι CHANNELNAME για να γίνει η ανάλογη διαδικασία

                Log.e("SOCKET",brokerRequest.toString());

                outFromConsumer.writeObject(brokerRequest); //Χρήση της μεθόδου writeObject της ObjectOutputStream για να στείλει ένα Object στον Broker και στέλνει το μήνυμα
                outFromConsumer.flush();


                Message response = (Message) inToConsumer.readObject(); //εδώ μέσω της μεθόδου readObject() λαμβάνουμε ως μήνυμα την απάντηση του broker με port 1004[BrokerMainInfo]
                //όπου μέσα στο μήνυμα έχουν σεταριστεί οι πληροφορίες(ip,port) του broker που θα χρειαστεί να συνδεθουμε για να
                //ξεκινήσει η σύνδεση με τον broker που έχει αναλάβει αυτό το channelName

                Log.e("SECOND SOCKET", String.valueOf(response.getBrokerInfo().getPort()));

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

                Message videaki = new Message(VIDEO_REQUEST); // δημιουργόυμε το μήνυμα με μηνυμα VIDEO_REQUEST που θα σταθλεί στον broker τον σωστό
                videaki.setChannelName(channelName);
                videaki.requestType = Variables.RequestType.CHANNELNAME;

                outFromConsumer.writeObject(videaki); //Χρήση της μεθόδου writeObject της ObjectOutputStream για να στείλει ένα Object στον Broker και στέλνει το μήνυμα
                outFromConsumer.flush();

                FileOutputStream out = null;
                boolean end = false;
                while (!end) { //μόλις το end γίνει true θα σταματήσει η διαδικασία αποστολής βίντεο
                    Message brokerResponse = (Message) inToConsumer.readObject();
                    if (brokerResponse.message.equals(VIDEO_END)) { //μολις του στειλει το μηνυμα VIDEO_END σταματαει να βαζει στο αρχείο του φακέλου μας τα chunks
                        Log.e("Video: ",brokerResponse.getVideoName() + " is ready"); //εκτυπωνω το ονομα του βιντεο που μετεφερα
                        out.close(); //κλέινει το γέμισμα του αρχείου
                        out = null; //το κάνουμε null ώστε άμα υπάρχουν έξτρα βίντεο να μπει στην σωστή if στην γραμμή 166
                    } else if (brokerResponse.message.equals(COMMUNICATION_OVER)) { // μολις του στειλει ο broker το μηνυμα COMMUNICATION_OVER σημαίνει ότι στάλθηκαν όλα και σταματάει την while
                        Log.e("All videos are ready ","!");
                        end = true;
                    } else if(brokerResponse.message.equals(CHANNEL_NOT_FOUND)) {   //οταν δεν βρει publisher με αυτο το channelName ή οταν βρει αλλα δεν έχει βίντεο
                                                                                    // μόλις σταλθεί σταματάει η διαδικασια και τελείωνει η while
                        noVideos = 100; //βάζουμε αυτή την τιμή για να την αξιοποιήσουμε στην doInBackground() ώστε να ξέρει άν δεν υπάρχουν βίντεο και να πράξει ανάλογα
                        Log.e("No videos from channel",channelName);
                        end = true;
                    }
                    else {
                        if (out == null) { // κάθε φορά που πάει να βάλει το πρώτο chunk θα δημιουργεί και νέο αρχείο με όνομα το όνομα του βίντεο ακολουθούμενο από .mp4 μέσα σε φάκελο του android
                            Log.e("Video now:",getFilesDir() + "/" + brokerResponse.getVideoName() + ".mp4");
                            out = new FileOutputStream(getFilesDir() + "/" + brokerResponse.getVideoName() + ".mp4"); //φτιάχνει τον φάκελο
                            Variables.VIDEOPLAYER = brokerResponse.getVideoName() + ".mp4"; //βάζουμε στο όνομα του βίντεο που μας στείλαν το .mp4
                            videoNames.add(VIDEOPLAYER); //βάζει στο ArrayList μας το όνομα του βίντεο
                            Log.e("VIDEO PLAYER",VIDEOPLAYER);
                            Log.e("Android file created", String.valueOf(getFilesDir()));
                        }
                        out.write(brokerResponse.getVideoChunk()); //βάζει στον φάκελο μας ένα ένα τα chunks
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}