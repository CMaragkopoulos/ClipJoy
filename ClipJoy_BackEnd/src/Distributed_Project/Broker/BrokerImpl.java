package Distributed_Project.Broker;

import Distributed_Project.utils.PublisherInfo;
import Distributed_Project.utils.BrokerInfo;
import Distributed_Project.utils.Hash;
import Distributed_Project.utils.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static Distributed_Project.utils.Variables.*;

public class BrokerImpl implements Broker {

    public ArrayList<BrokerInfo> brokerInfoArrayList; //ArrayList for All Brokers
    public ArrayList<PublisherInfo> publisherInfoArrayList; //ArrayList for All Publishers
    List<BrokerInfo> sortedBrokers; //Ταξινομημένη λίστα των Brokers
    ServerSocket receiveRequest; //Reference to ServerSocket Class
    public int currentPort; //Current Broker's port
    public BrokerInfo minBroker;
    public BrokerInfo maxBroker;
    public HashMap<String, ArrayList<ObjectOutputStream>> subscribersList; //Λίστα που κρατάει για κάθε topic(channelname or hashtag) ποια ObjectOutputStream έχει

    public BrokerImpl(String port, String[] brokerIps, int[] brokerPorts,
                      String[] publisherIps, int[] publisherPorts){

        this.currentPort = Integer.parseInt(port); //String to Integer
        this.brokerInfoArrayList = new ArrayList<BrokerInfo>();
        this.publisherInfoArrayList = new ArrayList<PublisherInfo>();
        this.subscribersList = new HashMap<>();


        for (int i = 0; i < brokerIps.length; i++) {
            //Add Broker instances(ip+port) into an Arraylist<BrokerInfo>
            this.brokerInfoArrayList.add(new BrokerInfo(brokerIps[i], brokerPorts[i]));
        }

        for (int i = 0; i <  publisherIps.length; i++) {
            //Add Publisher instances(ip+port) into an Arraylist<PublisherInfo>
            this.publisherInfoArrayList.add(new PublisherInfo( publisherIps[i], publisherPorts[i]));
        }


        sortedBrokers = brokerInfoArrayList.stream().
                sorted(Comparator.comparing(BrokerInfo::getHashCode)). //Ταξινομεί σε αύξουσα τους Brokers βάσει το Hash τους
                collect(Collectors.toList());

        minBroker = sortedBrokers.get(0); //κρατάει τον broker με το μικρότερο hashing του ip+port του
        maxBroker = sortedBrokers.get(brokerInfoArrayList.size()-1); //κρατάει τον broker με το μεγαλύερο hashing του ip+port του

        pull(); //κάθε φορά που κλέινει ένας broker και ξαναρχίσει μέσω της pull θα ενημερωθεί για ότι πληροφορία έχουν οι publishers εκείνη την στιγμή

    }

    public void pull() { //κάθε φορά που κλέινει ένας broker και ξαναρχίσει μέσω της pull θα ενημερωθεί για ότι πληροφορία έχουν οι publishers εκείνη την στιγμή
        //σύνδεση με όλους τους publisher και ρώτα τους ποιο channelname εξυπηρετούν
        for (int i = 0; i < publisherInfoArrayList.size(); i++) {
            Socket s = null;
            try {
                s = new Socket(publisherInfoArrayList.get(i).getIp(), publisherInfoArrayList.get(i).getPort()); //ξεκινάει επικοινωνία με τον publisher τάδε(θα περάσει από όλους)
                ObjectOutputStream outFromBrokerToPub = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream inToBrokerFromPub = new ObjectInputStream(s.getInputStream());
                Message requestPub = new Message(CHANNEL_INFO_REQUEST); //δημιουργεία Message με μήνυμα CHANNEL_INFO_REQUEST
                outFromBrokerToPub.writeObject(requestPub); //στέλνουμε το Message στον publisher
                outFromBrokerToPub.flush();

                Message responsePub = (Message) inToBrokerFromPub.readObject(); //διαβάζουμε την απάντηση από τον publisher και την κρατάμε σε ένα Message
                //updatePublisherInfoList(i,responsePub.publisherInfo.getChannelName(),responsePub.publisherInfo.getHashtags());
                publisherInfoArrayList.get(i).setChannelName(responsePub.publisherInfo.getChannelName()); //διαβάζει το channelname που είχε σετάρει στο Message αυτό ο publisher και το βάζει να υπάρχει στην δομή μας με τους publisher ως channelname του
                publisherInfoArrayList.get(i).setHashtags(responsePub.publisherInfo.getHashtags()); //διαβάζει τα hashtag που είχε σετάρει στο Message αυτό ο publisher και τα βάζει να υπάρχουν στην δομή μας με τους publisher ως hashtags του
                //διαδικασία για το subscribe
                forwardToSubscribers(responsePub,responsePub.publisherInfo.getChannelName()); //στείλε στον subscriber για channelname
                for (String hashtag : responsePub.publisherInfo.getHashtags()) { //για κάθε hashtag του publisher
                    forwardToSubscribers(responsePub,hashtag); //στείλε στον subscriber για hashtags
                }
                //κλείνει το κοννέκτιον
                inToBrokerFromPub.close();
                outFromBrokerToPub.close();
                s.close();
            } catch (IOException e) {
                System.out.println("Couldn't connect with publisher: " + publisherInfoArrayList.get(i) + " yet, cause it has not started yet!");
               // e.printStackTrace();
            } catch (ClassNotFoundException e) {
                //e.printStackTrace();
            }
            i++;
        }
    }



    public BrokerInfo getDesignatedBroker(String data){
        //Κάνει Hashing το channelName ή το hashTag και επιστρέφει τον κατάλληλο Broker
        BigInteger dataHash = Hash.getHashBigInt(data); // hasharoume το topic μας
        BrokerInfo currentBroker = maxBroker; //θέτουμε ως ο broker που θα επιστραφεί αρχικά να είναι ο broker με το μεγαλύτερο hash
        if (dataHash.compareTo(maxBroker.getHashCode()) > 0){ //θα χρησιμοποιύσαμε mod() με τον μεγαλύτερο broker
                                                              //αλλά στην συζήτηση στο eclass αναφέρθηκε ότι όποιο hash βγέι μεγαλύτερο
                                                              //απ' το hash του μεγαλύτερου broker στείλτο στον μικρότερο broker
            currentBroker = minBroker; //θετουμε ο broker που θα επιστρεφεί να είναι αυτός με το μικρότερο hash του ip+port του
        }
        else {
            for(BrokerInfo b : sortedBrokers){
                if (dataHash.compareTo(b.getHashCode()) < 0) { //άμα το hasharisma είναι μικρότερο απ το hasharisma του broker
                    currentBroker = b; //θετουμε ο broker που θα επιστρεφεί να είναι αυτός ο broker
                    break; //break για να μην πάει να δει και τον επόμενο broker και αλλάξει τιμή
                }
            }
        }
        return currentBroker;
    }

    public PublisherInfo getPublisherByChannelName(String channelname) { //μέθοδος που γυρνάει τον PublisherInfo που έχει το channelname που μας δίνουν
        System.out.println("Looking for publisher with channelname: " + channelname);
        PublisherInfo publisherChosen = null;
        for (int i = 0; i < publisherInfoArrayList.size(); i++) {
            if ( channelname.equals(publisherInfoArrayList.get(i).getChannelName())) {
                publisherChosen = publisherInfoArrayList.get(i);
                System.out.println("Publisher for channelname " + channelname + " is the one with this port: " + publisherChosen.getPort());
            }
        }
        return publisherChosen;
    }

    public ArrayList<PublisherInfo> getPublisherByHashtag(String hashtag) { //μέθοδος που γυρνάει τους PublisherInfo που έχουν το hashtag που μας δίνουν
        System.out.println("Looking for publisher with hashtag: " + hashtag);
        ArrayList<PublisherInfo> publishersChosen = new ArrayList<PublisherInfo>();
        for (int i = 0; i < publisherInfoArrayList.size(); i++) {
            System.out.println("Checking " + publisherInfoArrayList.get(i));
            if (publisherInfoArrayList.get(i).getHashtags().contains(hashtag)) {
                publishersChosen.add(publisherInfoArrayList.get(i));
                System.out.println("Publisher for hashtag " + hashtag + " is " + publisherInfoArrayList.get(i).getPort());
            }
        }
        return publishersChosen;
    }

    public void forwardToSubscribers(Message mes, String topic) {
        //ελέγχει αν το port του broker που είμαστε τώρα είναι ίδιο με το port του broker που επιστρέφεται ανάλογα το hasharisma του topic(channelname or hashtag) για να
        //τσεκάρει αν είμαστε στον σωστό broker και τσεκάρει αν η subscribersList μας έχει αυτό το topic ήδη
        if (this.getDesignatedBroker(topic).getPort() == this.currentPort && subscribersList.containsKey(topic)) {
            for (ObjectOutputStream subscriber : subscribersList.get(topic)) { //για κάθε κοννέκτιον του subscribersList με κλειδί το topic
                new BrokerManager(subscriber,topic).start(); //ξεκίνα το thread που ενημερώνει τον consumer αν ανέβηκε νέο βίντεο
            }
        }
    }
//synchronized
    public synchronized void updatePublisherInfoList(int i, String channelname, ArrayList<String> hashtag){
        System.out.println("I am inside publisher.");
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        publisherInfoArrayList.get(i).setChannelName(channelname); //σεταρε του το νεο τσανελνειμ
        publisherInfoArrayList.get(i).setHashtags(hashtag); //σεταρε του το να χασταγκς
        System.out.println("I am out now.");
    }

    @Override
    public void connect() {
        try {
            System.out.println("Current Broker's Port: " + currentPort);
            receiveRequest = new ServerSocket(currentPort); //Δημιουργία της υποδοχής ServerSocket για τον Consumer
            while(true) {
                System.out.println("Broker: Waiting for a Client...");
                new BrokerThread(receiveRequest.accept(), this).start(); //Εκκίνηση νήματος με το connection
                System.out.println("Broker: Client Accepted! ");
            }

        }
        catch (IOException ioException){
            System.err.println("Broker: We have an Error");
            ioException.printStackTrace();
        }
        finally {
            disconnect();
        }
    }



    @Override
    public void disconnect() {
        try {
            receiveRequest.close();
        }
        catch (IOException ioException){
            System.err.println("Broker: Error closing sever.");
            ioException.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return " brokerInfoArrayList: " + brokerInfoArrayList +
                ", currentPort: " + currentPort;
    }

}
