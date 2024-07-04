package Distributed_Project.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;


import static Distributed_Project.utils.Variables.*;

public class PublisherThread extends Thread implements Serializable {

    private static final long serialVersionUID = 1L;

    ObjectInputStream inFromBroker;
    ObjectOutputStream outToBroker;
    private PublisherImp publisher;
    public ArrayList<Message> chunksList;

    //Constructor
    @RequiresApi(api = Build.VERSION_CODES.N)
    public PublisherThread(Socket connection, PublisherImp publisher) {
        this.publisher = publisher;
        try {
            //για γράψιμο στον Broker
            outToBroker = new ObjectOutputStream(connection.getOutputStream());

            //για διάβασμα απο τον Broker
            inFromBroker = new ObjectInputStream(connection.getInputStream());
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void run() {
        try {
            Message request = (Message) inFromBroker.readObject();// Διαβάζει το message από τον Broker
            if (request.message.equals(VIDEO_REQUEST)) {
                if (request.requestType == RequestType.CHANNELNAME) { //αν ο τύπος του Message είναι CHANNELNAME
                    if (this.publisher.channelName.hashtagsPublished.size() == 0) { //άμα δεν έχει hashtags τότε δεν έχει βίντεο
                        Message fail = new Message(CHANNEL_NOT_FOUND); //αν το κανάλι δεν έχει hashtags(δηλαδή ούτε βίντεο), θα επρέπε να είναι μήνυμα της μορφής NO_VIDEOS_FOUND κανονικά(αλλά κάνει ίδια διαδικασία)
                        outToBroker.writeObject(fail); // στείλτο στον broker
                        return; //return; εδώ για να μην συνεχίσει παρακάτω
                    }
                    for (String videoName :this.publisher.getVideoNameChunks().keySet()) { //η for περνάει από όλα τα ονόματα των βίντεο(key) απ' το hashmap όπου μέσα έχει τα chunks του(value)
                        this.chunksList = this.publisher.getVideoNameChunks().get(videoName); //βάζει στο chunksList τα Message Objects που περιέχουν τα chunks του βίντεο
                        for (int m = 0; m < chunksList.size(); m++) {  //η for περνάει από κάθε chunk του βίντεο
                            this.chunksList.get(m).setVideoName(videoName); //βάζει το όνομα του βίντεο στο Message για να το πάρει ο Broker και αν το στείλει στον Consumer για να ονομάσει και ανάλογα το βίντεο
                            outToBroker.writeObject(this.chunksList.get(m)); // στελνω ενα ενα τα τσανκς στον μπροκερ
                            outToBroker.flush();
                        }
                        Message mEnd = new Message(VIDEO_END); //όταν τελείωσει με το να στέλνει τα chunks κάποιου βίντεου στέλνει νέο Message που λέγεται VIDEO_END
                        mEnd.setVideoName(videoName); //βάζει το όνομα του βίντεο στο Message για να το πάρει ο Broker και αν το στείλει στον Consumer για να ονομάσει και ανάλογα το βίντεο
                        outToBroker.writeObject(mEnd); // μολις τελειωσουν τα τσανκς στειλ' του το μηνυμα αυτο για να δειξεις οτι τελειωσε
                        outToBroker.flush();
                    }
                    Message commOver = new Message(COMMUNICATION_OVER); //όταν τελείωσει με το να στέλνει όλα τα βίντεο, στέλνει νέο Message που λέγεται COMMUNICATION_OVER
                    outToBroker.writeObject(commOver);
                    outToBroker.flush();
                } else {  //αν ο τύπος του Message είναι HASHTAG
                    //για καθε key(hashtag) του publisher δες αν ειναι ιδιο με το hashtag του message
                    for (String key : this.publisher.hashtagChunks.keySet()) { //η for περνάει από όλα τα hashtag(key) του καναλιού απ' το hashmap όπου μέσα έχει τα ονόματα των βίντεο(value)
                        if (request.getHashtag().equals(key)) { //αν το getHashtag() του Message είναι ίδιο με το key
                            for (String video: this.publisher.hashtagChunks.get(key)) { //η for περνάει από όλα τα ονόματα των βίντεο(value) απ' το hashmap των hashtag
                                this.chunksList = this.publisher.getVideoNameChunks().get(video); //βάζει στο chunksList τα Message Objects που περιέχουν τα chunks του βίντεο
                                //στελνω ενα ενα καθε chunk στον broker
                                for (int m = 0; m < chunksList.size(); m++) {
                                    this.chunksList.get(m).setVideoName(video); //βάζει το όνομα του βίντεο στο Message για να το πάρει ο Broker και αν το στείλει στον Consumer για να ονομάσει και ανάλογα το βίντεο
                                    outToBroker.writeObject(this.chunksList.get(m)); // στελνω ενα ε να τα chunks στον μπροκερ
                                    outToBroker.flush();
                                    //System.out.println("You are inside Publisher and chunk's number is:"+ this.chunksList.get(m).message);
                                }
                                Message mEnd = new Message(VIDEO_END); //όταν τελείωσει με το να στέλνει τα chunks κάποιου βίντεου στέλνει νέο Message που λέγεται VIDEO_END
                                mEnd.setVideoName(video); //βάζει το όνομα του βίντεο στο Message για να το πάρει ο Broker και αν το στείλει στον Consumer για να ονομάσει και ανάλογα το βίντεο
                                outToBroker.writeObject(mEnd); // μολις τελειωσουν τα τσανκς στειλτου το μηνυμα αυτο για να δειξεις οτι τελειωσε
                                outToBroker.flush();
                            }
                        }
                    }
                    Message commOver = new Message(COMMUNICATION_OVER); //όταν τελείωσει με το να στέλνει όλα τα βίντεο, στέλνει νέο Message που λέγεται COMMUNICATION_OVER
                    outToBroker.writeObject(commOver);
                    outToBroker.flush();
                }
            } else if (request.message.equals(CHANNEL_INFO_REQUEST)) {
                // απανταμε στο τραβηγμα της πληροφοριας απο εναν μπροκερ(pull)
                Message m = new Message(CHANNEL_INFO_REQUEST);
                PublisherInfo pubInfo = new PublisherInfo(PUBLISHER_IP_2,this.publisher.currentPort);
                pubInfo.setChannelName(this.publisher.channelName.getChannelName());
                pubInfo.setHashtags(this.publisher.channelName.getHashtagsPublished());
                m.setPublisherInfo(pubInfo); //σεταρουμε τον Publisher, οπου πανω δωσαμε τα στοχιεια του, μέσα στο Message για να πάρει τα στοιχεία του ο Broker
                outToBroker.writeObject(m); //το στέλνουμε το Message αυτο στον Broker
                outToBroker.flush();
            }
        }
        catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }
}
