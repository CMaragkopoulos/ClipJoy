package Distributed_Project.Publisher;

import Distributed_Project.utils.Message;
import Distributed_Project.utils.PublisherInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;

import static Distributed_Project.utils.Variables.*;

public class PublisherThread extends Thread {
    ObjectInputStream inFromBroker;
    ObjectOutputStream outToBroker;
    private PublisherImpl publisher;
    public ArrayList<Message> chunksList;

    public PublisherThread(Socket connection, PublisherImpl publisher) {
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

    public void run() {
        try {
            Message request = (Message) inFromBroker.readObject();// Διαβάζει το message από τον Broker
            //System.out.println("Publisher incoming connection established! " + request.message);
            if (request.message.equals(VIDEO_REQUEST)) {
                if (request.requestType == RequestType.CHANNELNAME) {
                    System.out.println("Publisher received request for channel with name: " + request.getChannelName() );
                    System.out.println("Publisher started sending video based on this channelname!");
                        if (this.publisher.channelName.hashtagsPublished.size() == 0) { //άμα δεν έχει hashtags τότε δεν έχει βίντεο
                            Message fail = new Message(CHANNEL_NOT_FOUND); //θα επρέπε να είναι μήνυμα της μορφής NO_VIDEOS_FOUND κανονικά
                            outToBroker.writeObject(fail); // στείλτο στον broker
                            return;
                        }
                        Set<String> videosOfChannel = this.publisher.getVideoSet(); //τα κρατάει σε σετακι για να παίρνει το κάθε path μία φορά
                        for (String path: videosOfChannel) {
                            this.chunksList = this.publisher.generateChunks(path); //γυρνάει μια λίστα από Messages που το κάθε ένα έχει κρατήσει chunk του video
                            for (int m = 0; m < chunksList.size(); m++) {
                                this.chunksList.get(m).setVideoName(this.publisher.read(path,"mp4"));
                                outToBroker.writeObject(this.chunksList.get(m)); // στελνω ενα ενα τα τσανκς στον μπροκερ
                                outToBroker.flush();
                                //System.out.println("You are inside Publisher and chunk's number is: "+ this.chunksList.get(m).message);
                            }
                            System.out.println("Video ended! ");
                            Message mEnd = new Message(VIDEO_END);
                            mEnd.setVideoName(this.publisher.read(path,"mp4")); //μεταφερω το ονομα του βιντεο
                            outToBroker.writeObject(mEnd); // μολις τελειωσουν τα τσανκς στειλτου το μηνυμα αυτο για να δειξεις οτι τελειωσε
                            outToBroker.flush();
                        }
                        System.out.println("All videos sent from publisher to consumer!");
                        Message commOver = new Message(COMMUNICATION_OVER);
                        outToBroker.writeObject(commOver);
                        outToBroker.flush();
                } else {
                    //για καθε key(hashtag) του publisher δες αν ειναι ιδιο με το hashtag του message
                    for (String key : this.publisher.getHashtagPathMap().keySet()) {
                        if (request.getHashtag().equals(key)) {
                            System.out.println("Publisher received request for hashtag: " + request.getHashtag());
                            for (String path: this.publisher.getHashtagPathMap().get(key)) {
                                System.out.println("Publisher started sending video based on hashtag!");
                                // μολις βρεις το hashtag που βρισκεται στο value του hashtag αυτου, χωριζε τα σε chunks
                                this.chunksList = this.publisher.generateChunks(path);
                                //System.out.println("You are inside Publisher and chunk's number is:" + chunksList.size());
                                //ωραια τωρα εχω την λιστα με τα chunks του 1ou path του #
                                //στελνω ενα ενα καθε chunk στον broker
                                for (int m = 0; m < chunksList.size(); m++) {
                                    this.chunksList.get(m).setVideoName(this.publisher.read(path,"mp4"));
                                    outToBroker.writeObject(this.chunksList.get(m)); // στελνω ενα ενα τα chunks στον μπροκερ
                                    outToBroker.flush();
                                    System.out.println("You are inside Publisher and chunk's number is:"+ this.chunksList.get(m).message);
                                }
                                System.out.println("Video ended");
                                Message mEnd = new Message(VIDEO_END);
                                mEnd.setVideoName(this.publisher.read(path,"mp4")); //μεταφερω το ονομα του βιντεο
                                outToBroker.writeObject(mEnd); // μολις τελειωσουν τα τσανκς στειλτου το μηνυμα αυτο για να δειξεις οτι τελειωσε
                                outToBroker.flush();
                            }
                        }
                    }
                    System.out.println("All videos sent from publisher " + this.publisher.currentPort +" to consumer!");
                    Message commOver = new Message(COMMUNICATION_OVER);
                    outToBroker.writeObject(commOver);
                    outToBroker.flush();
                }
            } else if (request.message.equals(CHANNEL_INFO_REQUEST)) { //κανονικά είναι σαν να λέμε request.topic
                // απανταμε στο τραβηγμα της πληροφοριας απο εναν μπροκερ
                Message m = new Message(CHANNEL_INFO_REQUEST);
                PublisherInfo pubInfo = new PublisherInfo("localhost",this.publisher.currentPort);
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
        //finally {
            //System.out.println("Publisher closing connection"); //δεν θέλουμε να κλείνει
        //}
    }
}
