package Distributed_Project.Broker;

import Distributed_Project.utils.PublisherInfo;
import Distributed_Project.utils.BrokerInfo;
import Distributed_Project.utils.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


import static Distributed_Project.utils.Variables.*;

public class BrokerThread extends Thread{

    public Socket receiveRequest; //Το connection με τον Consumer
    ObjectInputStream inToBrokerFromConsumer = null; //Ροή με τον Consumer η τον  Publisher
    ObjectOutputStream outFromBrokerToConsumer = null;
    BrokerImpl brokerImpl; //BrokerImpl instance with all the information

    public BrokerThread(Socket receiveRequest, BrokerImpl brokerImpl){
        this.receiveRequest = receiveRequest; //connection
        this.brokerImpl = brokerImpl; //Το instance του BrokerImpl
    }

    @Override
    public void run(){
        try {
            System.out.println("Broker initialize connection " + Thread.currentThread().getId()); // μήνυμα για να μας δείχνει ποιο thread το ανέλαβε
            inToBrokerFromConsumer = new ObjectInputStream(receiveRequest.getInputStream()); //Δημιουργία ροών με Consumer ή τον publisher
            outFromBrokerToConsumer = new ObjectOutputStream(receiveRequest.getOutputStream());
            System.out.println("Broker waiting to read message");
            Message request = (Message) inToBrokerFromConsumer.readObject(); //Διαβάζει το Message απο τον Consumer ή τον publisher
            System.out.println("Broker received message" + request.message);

            if (request.message.equals(VIDEO_LIST)) { // απάντηση στο push() tou Publisher
                int i = 0;
                for (PublisherInfo p: brokerImpl.publisherInfoArrayList) {
                    if (p.getPort() == request.port) { //αν το port  που είναι στο Message είναι ίδιο με το port ενός Publisher απ το publisherInfoArrayList
                        //brokerImpl.updatePublisherInfoList(i,request.publisherInfo.getChannelName(),request.publisherInfo.getHashtags());
                        p.setChannelName(request.publisherInfo.getChannelName()); //σεταρε του το νεο τσανελνειμ
                        p.setHashtags(request.publisherInfo.getHashtags()); //σεταρε του το να χασταγκς
                        System.out.print("Updated publisher " + p);
                    }
                    i++;
                }
                //για subscribe
                this.brokerImpl.forwardToSubscribers(request,request.publisherInfo.getChannelName()); // ενημέρωσε τον subscriber για νεο βίντεο με αυτό το channelname
                for (String hashtag : request.publisherInfo.getHashtags()) {
                    this.brokerImpl.forwardToSubscribers(request,hashtag); // ενημέρωσε τον subscriber για νεο βίντεο με αυτό το hashtag
                }
            } else if (request.message.equals(VIDEO_REQUEST)) { //όταν στειλει ο κονσουμερ το μηνυμα για video_request ξεκιναμε εδω
                ArrayList<PublisherInfo> publishers = new ArrayList<PublisherInfo>();
                if (request.requestType == RequestType.CHANNELNAME) {
                    System.out.println("Broker received video request from consumer based on channelName: " + request.getChannelName());
                    String channelName = request.getChannelName();
                    PublisherInfo channelNamePublisher = this.brokerImpl.getPublisherByChannelName(channelName); //γυρνάει τον σωστό publisher με αυτό το όνομα καναλιού
                    if (channelNamePublisher == null) { //θα στείλει message ότι δεν υπάρχει το συγκεκριμένο channelname αν δεν το βρεί
                        Message fail = new Message(CHANNEL_NOT_FOUND);
                        fail.setChannelName(channelName);
                        System.out.println("Publisher for channelName " + channelName + "not found ");
                        outFromBrokerToConsumer.writeObject(fail); //στειλτο στον κονσουμερ
                        return;
                    }
                    publishers.add(channelNamePublisher); //τον publisher που θα βρεις βάλτον μέσα στην ArrayList publishers
                } else { // ΑΝ ΕΙΝΑΙ ΤΟ REQUESTTYPE == HASHTAG
                    System.out.println("Broker received video request from consumer based on hashtag " + request.getHashtag());
                    String hashtag = request.getHashtag();
                    ArrayList<PublisherInfo> hashtagPublishers = this.brokerImpl.getPublisherByHashtag(hashtag);
                    if (hashtagPublishers.size() == 0) { // αν δεν βρέι κανέναν publisher με αυτό το hashtag στέλνει ανάλογο μήνυμα
                        Message fail = new Message(CHANNEL_NOT_FOUND); //θα επρέπε να λέγεται NO_HASHTAG_FOUND αλλά η διαδικασία είναι ίδια
                        fail.setHashtag(hashtag);
                        System.out.println("Publisher for hashtag " + hashtag + "not found ");
                        outFromBrokerToConsumer.writeObject(fail); // στείλτο στον consumer
                        return;
                    }

                    publishers.addAll(hashtagPublishers); //τους publisher που θα βρεις βάλτους μέσα στην ArrayList publishers

                }
                for (PublisherInfo pub : publishers) {
                    System.out.println("Broker started connection with " + pub);
                    //Δημιουργία υποδοχής με ip και port με τον κατάλληλο publisher
                    Socket sendRequestSocket = new Socket(pub.getIp(), pub.getPort());
                    ObjectOutputStream outFromBrokerToPublisher = new ObjectOutputStream(sendRequestSocket.getOutputStream());
                    ObjectInputStream inToBrokerFromPublisher = new ObjectInputStream(sendRequestSocket.getInputStream());
                    outFromBrokerToPublisher.writeObject(request); //το στελνουμε στον publisher
                    outFromBrokerToPublisher.flush();
                    boolean end = false;
                    while (!end) {
                        Message response = (Message) inToBrokerFromPublisher.readObject(); //διαβαζουμε τα τσανκς απο τον παμπλισερ
                        System.out.println("Broker responding with chunk from publisher: "+ response.message);

                        if (response.message.equals(VIDEO_END)) { // αν στειλει publisher το μηνυμα για video end σταματαει η διαδικασια
                            System.out.println("Video with name (" + response.getVideoName() +") sent successfully to broker");
                            outFromBrokerToConsumer.writeObject(response); //στέλνει και στον consumer to VIDEO_END κατευθείαν με ότι set έχουν γίνει στον publisher
                            outFromBrokerToConsumer.flush();
                        } else if(response.message.equals(CHANNEL_NOT_FOUND)) { //αν του στείλει ο publisher ότι δεν βρήκε βίντεο,θα επρέπε να λέγεται NO_VIDEO_FOUND αλλά η διαδικασία είναι ίδια
                            end = true;
                            outFromBrokerToConsumer.writeObject(response); //ενημέρωσε και τον consumer κατευθείαν
                            outFromBrokerToConsumer.flush();
                        } else if(response.message.equals(COMMUNICATION_OVER)) { //όταν στείλει όλα τα βίντεο ο πάμπλισερ βγες απ την while
                            end = true;
                        }
                        else { //διαβάζει ένα ένα τα chunks
                            outFromBrokerToConsumer.writeObject(response); //στελνουμε τα chunks κατευθειαν στον κονσουμερ
                            outFromBrokerToConsumer.flush();
                        }
                    }

                    //κλείνει το socket
                    inToBrokerFromPublisher.close();
                    outFromBrokerToPublisher.close();
                    sendRequestSocket.close();
                    System.out.println("Broker finished forwarding video");
                }
                outFromBrokerToConsumer.writeObject(new Message(COMMUNICATION_OVER)); //τώρα που έχεις στείλει όλα τα βίντεο ενημέρωσε τον consumer ότι τελείωσες
                outFromBrokerToConsumer.flush();
            } else if (request.message.equals(SUBSCRIBE)) { //όταν δέχεται Message με μήνυμα subscribe
                String subTopic = null;
                if (request.requestType == RequestType.CHANNELNAME) {
                    subTopic = request.getChannelName(); //αν είναι channelname βάλτο subTopic μέσα
                } else {
                    subTopic = request.getHashtag(); //αν είναι hashtag βάλτο subTopic μέσα
                }
                if (this.brokerImpl.subscribersList.containsKey(subTopic)) { //αν υπάρχει ήδη αυτό το topic(channelname or hashtag)
                    ArrayList<ObjectOutputStream> existingSubscribers = this.brokerImpl.subscribersList.get(subTopic); //πάρε όλα τα ObjectOutputStream που έχει αυτό το topic
                    existingSubscribers.add(outFromBrokerToConsumer); //πρόσθεσε και το τωρινό ObjectOutputStream
                    this.brokerImpl.subscribersList.put(subTopic,existingSubscribers); //και βάλε το ανανεωμένο ArrayList μέσα στο topic σου
                } else { //αν δεν υπάρχει ήδη αυτό το topic(channelname or hashtag) το φτιάχνουμε
                    ArrayList<ObjectOutputStream> newSubscriber = new ArrayList<ObjectOutputStream>();
                    newSubscriber.add(outFromBrokerToConsumer);
                    this.brokerImpl.subscribersList.put(subTopic,newSubscriber);
                }
            }
            else if (request.message.equals(BROKER_REQUEST)) { //μέσω του brokermaininfo θα hasharei channelname ή hashtag(τα βλέπουμε ως ξεχωριστά) και θα στελνει ip, port του σωστου broker που θα το αναλαβει!
                if (request.requestType == RequestType.CHANNELNAME) {
                    String channelName = request.getChannelName(); //κράτα το channelname του Message που το χουν setarei στον consumer
                    BrokerInfo brokerReturned = this.brokerImpl.getDesignatedBroker(channelName); //βάλε σε ενα BrokerInfo αντικείμενο τον broker που γύρισε η διαδικασία με το hasharisma
                    Message brokerAnswer = new Message(BROKER_REQUEST); //δημιουργεια Message με μήνυμα BROKER_REQUEST
                    brokerAnswer.setBrokerInfo(brokerReturned); //σεταρε μέσα στο Message να έχει ως Broker τον broker που θα αναλάβει το αίτημα
                    System.out.println("Sending broker info with port " + brokerReturned.getPort() + " for channelname " + channelName);
                    outFromBrokerToConsumer.writeObject(brokerAnswer); //στέιλτο στον consumer
                    outFromBrokerToConsumer.flush();
                } else { //άμα το request type == hashtag κάνε τα ίδια όπως πάνω
                    String hashtag = request.getHashtag(); //το μόνο διαφορετικό με πάνω
                    BrokerInfo brokerReturned = this.brokerImpl.getDesignatedBroker(hashtag);
                    Message brokerAnswer = new Message(BROKER_REQUEST);
                    brokerAnswer.setBrokerInfo(brokerReturned);
                    System.out.println("Sending broker info! " + brokerReturned.getPort() + " for hashtag " + hashtag);
                    outFromBrokerToConsumer.writeObject(brokerAnswer);
                    outFromBrokerToConsumer.flush();
                }
            }
        }
        catch (UnknownHostException unknownHostException){
            System.err.println("Broker: You are trying to connect to an unknown host!");
            unknownHostException.printStackTrace();
        }
        catch (IOException | ClassNotFoundException ioe){
            System.err.println("BrokerThread: This is an Error");
            ioe.printStackTrace();
        }
        finally {
            System.out.println("Broker finished " + Thread.currentThread().getId()); //μήνυμα για να μας δείχνει ποιο thread το ανέλαβε
        }
    }
}