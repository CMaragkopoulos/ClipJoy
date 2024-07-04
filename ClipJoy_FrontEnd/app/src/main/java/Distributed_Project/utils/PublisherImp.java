package Distributed_Project.utils;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static Distributed_Project.utils.Variables.PUBLISHER_IP_2;
import static Distributed_Project.utils.Variables.VIDEO_LIST;

public class PublisherImp implements Publisher, Serializable {

    private static final long serialVersionUID = 1L;

    ServerSocket serverSocket;
    Socket connection = null;
    ObjectInputStream in;
    ObjectOutputStream out;

    //Publisher Field
    public ChannelName channelName;
    public ArrayList<String> hashtagsPublished;

    public int currentPort;
    public String videosDirectory;
    public ArrayList<BrokerInfo> brokerInfoArrayList;
    private HashMap<String,ArrayList<String>> hashtagPathMap; //hashmap που τελικα δεν χρειαστηκε

    public HashMap<String,ArrayList<Message>> videoNameChunks; //hashmap με keys τα ονοματα των video και values ArrayList με Message που εχουν τα chunks
    public HashMap<String,ArrayList<String>> hashtagChunks; //hashmap me keys τα hashtag και values ArrayList με String τα ονοματα των video


    //Constructor
    public PublisherImp(String port, String[] brokerIps, int[] brokerPorts, String videosDirectory) {
        //ΤΑ ΑΡΧΙΚΟΠΟΙΟΥΜΕ ΟΛΑ
        brokerInfoArrayList = new ArrayList<>();
        currentPort = Integer.parseInt(port);
        this.videosDirectory = videosDirectory;
        for (int i = 0; i < brokerIps.length; i++){
            brokerInfoArrayList.add(new BrokerInfo(brokerIps[i],brokerPorts[i]));
        }
        this.hashtagPathMap = new HashMap<String,ArrayList<String>>();
        hashtagsPublished = new ArrayList<>(); //Όλα τα hashtags του Channel
        videoNameChunks = new HashMap<>();
        hashtagChunks = new HashMap<>();
        channelName = new ChannelName("temp",hashtagsPublished,videosDirectory);
    }

    public void push() {
        Socket socketToBroker = null;
        //σπρωχνεις την πληροφορια του παμπλισερ σε ολους τους μπροκερς
        Message m = new Message(VIDEO_LIST,PUBLISHER_IP_2,currentPort); //δημιουργεί το Message με μήνυμα VIDEO_LIST
        PublisherInfo pubInfo = new PublisherInfo(PUBLISHER_IP_2,currentPort); // ΠΡΟΣ ΤΟ ΠΑΡΟΝ 2001
        pubInfo.setChannelName(channelName.getChannelName());
        pubInfo.setHashtags(channelName.getHashtagsPublished());
        m.setPublisherInfo(pubInfo);
        for (BrokerInfo broker : brokerInfoArrayList) { //designatedBrokerInfoArrayList θα μπορούσε να υπήρχε εδώ για να ενημερώνει μόνο τους brokers που χρείαζονται αλλα δνεν προλαβαιναμε
            try {
                socketToBroker = new Socket(broker.getIp(), broker.getPort());
                out = new ObjectOutputStream(socketToBroker.getOutputStream());
                in = new ObjectInputStream(socketToBroker.getInputStream());
                out.writeObject(m); //στέλνει το μήνυμα στον broker(θα περάσει από όλους)
                out.flush();
                in.close();
                out.close();
                socketToBroker.close();
            } catch (IOException e) {
                Log.e("Broker hasn't started: ",broker.getIp());
            }
        }
    }

    public HashMap<String, ArrayList<String>> getHashtagPathMap() {
        return hashtagPathMap;
    }

    public void removeVideo(String videoName) { //μέθοδος για να διαγράφουμε το path σε όποιο hashtag υπάρχει
        Iterator it = this.hashtagChunks.entrySet().iterator(); //δεικτης που δειχνει με την σειρα στα στοιχεια του hashmap
        while (it.hasNext()) {
            HashMap.Entry<String, ArrayList<String>> pair = (HashMap.Entry<String, ArrayList<String>>)it.next();
            ArrayList<String> existingVideoNames = pair.getValue();
            if (existingVideoNames.contains(videoName)) {
                existingVideoNames.remove(videoName);
                if (existingVideoNames.size() == 0) { //άμα αδείασουν τα videoNames του hashtag, διεγραψε το hashtag από το channel
                    it.remove(); // avoids a ConcurrentModificationException και τα βγάζει απ το hashmap hashtagChunks κατευθείαν
                    this.channelName.hashtagsPublished.remove(pair.getKey());
                } else {
                    this.hashtagChunks.put(pair.getKey(), existingVideoNames);
                }
                this.videoNameChunks.remove(videoName); //διαγράφει το βιντεο απ το hashmap με τα chunks
            }
        }
    }

    public HashMap<String, ArrayList<Message>> getVideoNameChunks() {
        return videoNameChunks;
    }

    //        sortedBrokers = brokerInfoArrayList.stream().
    //                sorted(Comparator.comparing(BrokerInfo::getHashCode)). //Ταξινομεί σε αύξουσα τους Brokers βάσει το Hash τους
    //                collect(Collectors.toList());
    //
    //        minBroker = sortedBrokers.get(0); //κρατάει τον broker με το μικρότερο hashing του ip+port του
    //        maxBroker = sortedBrokers.get(brokerInfoArrayList.size()-1); //κρατάει τον broker με το μεγαλύερο hashing του ip+port του
    //    public BrokerInfo getDesignatedBroker(String data){
    //        //Κάνει Hashing το channelName ή το hashTag και επιστρέφει τον κατάλληλο Broker
    //        BigInteger dataHash = Hash.getHashBigInt(data); // hasharoume το topic μας
    //        BrokerInfo currentBroker = maxBroker; //θέτουμε ως ο broker που θα επιστραφεί αρχικά να είναι ο broker με το μεγαλύτερο hash
    //        if (dataHash.compareTo(maxBroker.getHashCode()) > 0){ //θα χρησιμοποιύσαμε mod() με τον μεγαλύτερο broker
    //                                                              //αλλά στην συζήτηση στο eclass αναφέρθηκε ότι όποιο hash βγέι μεγαλύτερο
    //                                                              //απ' το hash του μεγαλύτερου broker στείλτο στον μικρότερο broker
    //            currentBroker = minBroker; //θετουμε ο broker που θα επιστρεφεί να είναι αυτός με το μικρότερο hash του ip+port του
    //        }
    //        else {
    //            for(BrokerInfo b : sortedBrokers){
    //                if (dataHash.compareTo(b.getHashCode()) < 0) { //άμα το hasharisma είναι μικρότερο απ το hasharisma του broker
    //                    currentBroker = b; //θετουμε ο broker που θα επιστρεφεί να είναι αυτός ο broker
    //                    break; //break για να μην πάει να δει και τον επόμενο broker και αλλάξει τιμή
    //                }
    //            }
    //        }
    //        return currentBroker;
    //    }

    //Node Interface Methods

    //ΔΕΝ ΚΑΛΕΙΤΕ ΠΟΤΕ ΓΙΑΤΙ ΑΝΟΙΓΩ ΤΟ SERVERSOCKET ΣΤΟ MAINACTIVITY
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void connect() {
        try {
            serverSocket = new ServerSocket(this.currentPort); //Δημιουργία serverSocket για τον Broker
            while(true) {
                System.out.println("Publisher waiting for incoming connection!" + currentPort);
                connection = serverSocket.accept(); //Σύνδεση με Broker


                //Νέο νήμα για κάθε πελάτη και συνέχισε να δέχεσαι αιτήσεις
                //Περνάει το connection για την υποδοχή με τον Broker
                new PublisherThread(connection, this).start();
            }
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        }
        finally {
            disconnect();
        }
    }

    @Override
    public void disconnect() {
        try {
            serverSocket.close();
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        }
    }
}
