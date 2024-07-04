package Distributed_Project.Consumer;

import Distributed_Project.utils.BrokerInfo;
import Distributed_Project.utils.Message;
import Distributed_Project.utils.Variables;


import java.io.*;
import java.net.Socket;

import static Distributed_Project.utils.Variables.*;

public class ConsumerImpl extends Thread implements Consumer {

    public String folder = null; //φάκελος με το path που θα δωθεί στην ConsumerMain

    public Socket sendRequestSocket = null; //υποδοχή για να στείλει δεδομένα στον Broker
    public ObjectOutputStream outFromConsumer = null; //ροή για να στείλει δεδομένα στον Broker
    public ObjectInputStream inToConsumer = null; //ροή για να πάρει δεδομένα απο τον Broker

    public String ip;
    public int port;
    public BrokerInfo broker;

    public ConsumerImpl(String folder) {
        this.folder = folder; //βάζει μέσα στο folder το path που δωθηκε ως argument στην ConsumerMain
        this.ip = BROKER_IP_MAIN;
        this.port = BROKER_PORT_MAIN;
    }

    //Consumer Interface Methods

    public void requestVideoByChannelName(String channelName) { //μέθοδος που γυρνάει όσα βίντεο έχει αυτό το channelname [CASE 1]
        try {
            //Δημιουργία υποδοχής με ip και port
            sendRequestSocket = new Socket(this.ip, this.port); //ανοίγει socket προς τον broker με port 1004, όπου τον ονομάσαμε BrokerMainInfo, και τον αξιοποιούμε για να μας γυρίζει το ip και port
            //του broker που είναι υπευθυνος για αυτό το channelname μέσω της διαδικασίας του consistent hashing
            // Προετοιμασία ObjectOutputStream
            outFromConsumer = new ObjectOutputStream(sendRequestSocket.getOutputStream());
            //Προετοιμασία ObjectInputStream
            inToConsumer = new ObjectInputStream(sendRequestSocket.getInputStream());
            Message brokerRequest = new Message(BROKER_REQUEST); // δημιουργόυμε το μήνυμα με μηνυμα BROKER_REQUEST που θα σταθλεί στον broker με port 1004[BrokerMainInfo]

            brokerRequest.setChannelName(channelName); //βάζουμε στο μήνυμα μέσα να υπάρχει το channelname για να μπορεί να γίνει η διαδικασία με το hashing του στον broker
            brokerRequest.requestType =  RequestType.CHANNELNAME; //ορίζουμε το RequestType του μηνύματος να είναι CHANNELNAME για να γίνει η ανάλογη διαδικασία

            outFromConsumer.writeObject(brokerRequest); //Χρήση της μεθόδου writeObject της ObjectOutputStream για να στείλει ένα Object στον Broker και στέλνει το μήνυμα
            outFromConsumer.flush();

            Message response = (Message) inToConsumer.readObject(); //εδώ μέσω της μεθόδου readObject() λαμβάνουμε ως μήνυμα την απάντηση του broker με port 1004[BrokerMainInfo]
            //όπου μέσα στο μήνυμα έχουν σεταριστεί οι πληροφορίες(ip,port) του broker που θα χρειαστεί να συνδεθουμε για να
            //ξεκινήσει η σύνδεση με τον broker που έχει αναλάβει αυτό το channelname

            //disconnect για να βαλουμε νεο socket με τον σωστο broker
            inToConsumer.close();
            outFromConsumer.close();
            sendRequestSocket.close();

            //Δημιουργία υποδοχής με ip και port με βάση τα ip kai port του broker που γύρισε ο broke με port 1004[BrokerMainInfo]
            sendRequestSocket = new Socket(response.getBrokerInfo().getIp(), response.getBrokerInfo().getPort());
            //Προετοιμασία ObjectOutputStream
            outFromConsumer = new ObjectOutputStream(sendRequestSocket.getOutputStream());
            //Προετοιμασία ObjectInputStream
            inToConsumer = new ObjectInputStream(sendRequestSocket.getInputStream());

            Message videaki = new Message(VIDEO_REQUEST); // δημιουργόυμε το μήνυμα με μηνυμα VIDEO_REQUEST που θα σταθλεί στον broker τον σωστό
            videaki.setChannelName(channelName);
            videaki.requestType =  RequestType.CHANNELNAME;

            outFromConsumer.writeObject(videaki); //Χρήση της μεθόδου writeObject της ObjectOutputStream για να στείλει ένα Object στον Broker και στέλνει το μήνυμα
            outFromConsumer.flush();

            FileOutputStream out = null;
            boolean end = false;
            while (!end) { //μόλις το end γίνει true θα σταματήσει η διαδικασία αποστολής βίντεο
                Message brokerResponse = (Message) inToConsumer.readObject();
                //System.out.println("Eisai mesa ston Consumer" + response.message);
                if (brokerResponse.message.equals(VIDEO_END)) { // μολις του στειλει το μηνυμα VIDEO_END σταματαει να βαζει στο αρχείο του φακέλου μας τα chunks
                    System.out.println("Video with name: " + brokerResponse.getVideoName() + " is ready to be played!"); //εκτυπωνω το ονομα του βιντεο που μετεφερα
                    out.close(); //κλέινει το γέμισμα του αρχείου
                    out = null;
                    System.out.println("Video " + brokerResponse.getVideoName() +" sent successfully! ");
                } else if (brokerResponse.message.equals(COMMUNICATION_OVER)) { // μολις του στειλει ο broker το μηνυμα COMMUNICATION_OVER σημαίνει ότι στάλθηκαν όλα και σταματάει την while
                    System.out.println("All videos are ready! ");
                    end = true;
                } else if(brokerResponse.message.equals(CHANNEL_NOT_FOUND)) {   //κανονικά θα έπρεπε να είναι μήνυμα HASHTAG_NOT_FOUND αλλά δεν έχει σημασία, μόλις
                    // σταλθεί σταματάει η διαδικασια και τελείωνει η while
                    System.out.println("There are no videos with this hashtag! Try another one!");
                    end = true;
                }
                else {
                    if (out == null) { // κάθε φορά που πάει να βάλει το πρώτο chunk θα δημιουργεί και νέο αρχείο με όνομα το όνομα του βίντεο ακολουθούμενο από .mp4 μέσα στον φάκελο μας
                        out = new FileOutputStream(this.folder + brokerResponse.getVideoName() + ".mp4");
                        System.out.println("Consumer output file created");
                    }
                    out.write(brokerResponse.getVideoChunk()); // εδω τα γραφει ενα ενα τα chunks
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Consumer disconnecting");
            disconnect(); //μόλις τελειώσει η διαδικασία κλέινει το κοννεκτιον
        }
    }

    public void requestVideoByHashtag(String hashtag) { //μέθοδος που γυρνάει όσα βίντεο έχει αυτό το hashtag [CASE 2]
        try {
            //Δημιουργία υποδοχής με ip και port
            sendRequestSocket = new Socket(this.ip, this.port); //ανοίγει socket προς τον broker με port 1004, όπου τον ονομάσαμε BrokerMainInfo, και τον αξιοποιούμε για να μας γυρίζει το ip και port
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
            sendRequestSocket = new Socket(response.getBrokerInfo().getIp(), response.getBrokerInfo().getPort());
            //Προετοιμασία ObjectOutputStream
            outFromConsumer = new ObjectOutputStream(sendRequestSocket.getOutputStream());
            //Προετοιμασία ObjectInputStream
            inToConsumer = new ObjectInputStream(sendRequestSocket.getInputStream());

            Message videaki = new Message(VIDEO_REQUEST); // δημιουργόυμε το μήνυμα με μηνυμα VIDEO_REQUEST που θα σταθλεί στον broker τον σωστό
            videaki.setHashtag(hashtag);
            videaki.requestType =  Variables.RequestType.HASHTAG;

            outFromConsumer.writeObject(videaki); //Χρήση της μεθόδου writeObject της ObjectOutputStream για να στείλει ένα Object στον Broker και στέλνουμε το μήνυμα
            outFromConsumer.flush();

            FileOutputStream out = null;
            boolean end = false;
            while (!end) { //μόλις το end γίνει true θα σταματήσει η διαδικασία αποστολής βίντεο
                Message brokerResponse = (Message) inToConsumer.readObject(); //εδώ μέσω της μεθόδου readObject() λαμβάνουμε ως μήνυμα την απάντηση του broker κάθε φορά
                //System.out.println("Eisai mesa ston Consumer" + response.message);
                if (brokerResponse.message.equals(VIDEO_END)) { // μολις του στειλει ο broker το μηνυμα VIDEO_END σταματαει να βαζει στο αρχείο του φακέλου μας τα chunks
                    System.out.println("Video with name: " + brokerResponse.getVideoName() + " is ready to be played!");
                    out.close(); //κλέινει το γέμισμα του αρχείου
                    out = null;
                } else if (brokerResponse.message.equals(CHANNEL_NOT_FOUND)) { //κανονικά θα έπρεπε να είναι μήνυμα HASHTAG_NOT_FOUND αλλά δεν έχει σημασία, μόλις
                                                                               // σταλθεί σταματάει η διαδικασια και τελείωνει η while
                    System.out.println("There are no videos with this hashtag! Try another one!");
                    end = true;
                } else if (brokerResponse.message.equals(COMMUNICATION_OVER)) { // μολις του στειλει ο broker το μηνυμα COMMUNICATION_OVER σημαίνει ότι στάλθηκαν όλα και σταματάει την while
                    System.out.println("All videos associated with this hashtag are ready! ");
                    end = true;
                }
                else {
                    if (out == null) { // κάθε φορά που πάει να βάλει το πρώτο chunk θα δημιουργεί και νέο αρχείο με όνομα το όνομα του βίντεο ακολουθούμενο από .mp4 μέσα στον φάκελο μας
                        out = new FileOutputStream(this.folder + brokerResponse.getVideoName() + ".mp4");
                        System.out.println("Consumer output file created");
                    }
                    out.write(brokerResponse.getVideoChunk()); // εδω τα γραφει ενα ενα τα chunks
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Consumer disconnecting");
            disconnect(); //μόλις τελειώσει η διαδικασία κλέινει το κοννεκτιον
        }
    }



    public void subscribeToChannelName(String channelName) { //μέθοδος θα κρατήσει ένα κοννεκτιον ανοιχτό το οποίο θα ενημερώνει τον κονσουμερ
                                                             //ότι βγηκε νεο βιντεο με αυτό το channelname οπότε θα μπορεί ο κονσούμερ να
                                                             //τα ξαναζητησει ολα με το case 1 και να ρθουν και τα καινουρια βιντεο πλεον[CASE 3]
        try {
            sendRequestSocket = new Socket(this.ip, this.port); //ανοίγει socket προς τον broker με port 1004, όπου τον ονομάσαμε BrokerMainInfo, και τον αξιοποιούμε για να μας γυρίζει το ip και port
                                                                //του broker που είναι υπευθυνος για αυτό το channelname μέσω της διαδικασίας του consistent hashing
            // Προετοιμασία ObjectOutputStream
            outFromConsumer = new ObjectOutputStream(sendRequestSocket.getOutputStream());
            //Προετοιμασία ObjectInputStream
            inToConsumer = new ObjectInputStream(sendRequestSocket.getInputStream());
            Message brokerRequest = new Message(BROKER_REQUEST); // δημιουργόυμε το μήνυμα με μηνυμα BROKER_REQUEST που θα σταθλεί στον broker με port 1004[BrokerMainInfo]

            brokerRequest.setChannelName(channelName); //βάζουμε στο μήνυμα μέσα να υπάρχει το channelname για να μπορεί να γίνει η διαδικασία με το hashing του στον broker
            brokerRequest.requestType =  RequestType.CHANNELNAME; //ορίζουμε το RequestType του μηνύματος να είναι CHANNELNAME για να γίνει η ανάλογη διαδικασία

            outFromConsumer.writeObject(brokerRequest); //Χρήση της μεθόδου writeObject της ObjectOutputStream για να στείλει ένα Object στον Broker και στέλνει το μήνυμα
            outFromConsumer.flush();

            Message response = (Message) inToConsumer.readObject(); //εδώ μέσω της μεθόδου readObject() λαμβάνουμε ως μήνυμα την απάντηση του broker με port 1004[BrokerMainInfo]
                                                                    //όπου μέσα στο μήνυμα έχουν σεταριστεί οι πληροφορίες(ip,port) του broker που θα χρειαστεί να συνδεθουμε για να
                                                                    //ξεκινήσει η σύνδεση με τον broker που έχει αναλάβει αυτό το channelname

            //disconnect για να βαλουμε νεο socket με τον σωστο broker
            inToConsumer.close();
            outFromConsumer.close();
            sendRequestSocket.close();

            //Δημιουργία υποδοχής με ip και port με βάση τα ip kai port του broker που γύρισε ο broke με port 1004[BrokerMainInfo]
            sendRequestSocket = new Socket(response.getBrokerInfo().getIp(), response.getBrokerInfo().getPort());
            //Προετοιμασία ObjectOutputStream
            outFromConsumer = new ObjectOutputStream(sendRequestSocket.getOutputStream());
            //Προετοιμασία ObjectInputStream
            inToConsumer = new ObjectInputStream(sendRequestSocket.getInputStream());

            Message videaki = new Message(SUBSCRIBE); // δημιουργόυμε το μήνυμα με μηνυμα VIDEO_REQUEST που θα σταθλεί στον broker τον σωστό
            videaki.setChannelName(channelName);
            videaki.requestType =  RequestType.CHANNELNAME;

            outFromConsumer.writeObject(videaki); //Χρήση της μεθόδου writeObject της ObjectOutputStream για να στείλει ένα Object στον Broker και στέλνει το μήνυμα
            outFromConsumer.flush();

            ConsumerManager manager = new ConsumerManager(inToConsumer,"channelnames");
            manager.start();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Connection will continue to be open to inform you for new videos posted by channelname: " + channelName);
//            disconnect(); //δεν θέλουμε να κλείνει αυτό το νέο κοννέκτιον για να ενημερώνει τον κονσούμερ όσο είναι ενεργός αν βγήκε νέο βίντεο με το συγκεκριμένο channelname!
        }
    }

    public void subscribeToHashtag(String hashtag) { //μέθοδος θα κρατήσει ένα κοννεκτιον ανοιχτό το οποίο θα ενημερώνει τον κονσουμερ
                                                     //ότι βγηκε νεο βιντεο με αυτό το hashtag οπότε θα μπορεί ο κονσούμερ να
                                                     //τα ξαναζητησει ολα με το case 1 και να ρθουν και τα καινουρια βιντεο πλεον
        try {
            //Δημιουργία υποδοχής με ip και port
            sendRequestSocket = new Socket(this.ip, this.port); //ανοίγει socket προς τον broker με port 1004, όπου τον ονομάσαμε BrokerMainInfo, και τον αξιοποιούμε για να μας γυρίζει το ip και port
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
            sendRequestSocket = new Socket(response.getBrokerInfo().getIp(), response.getBrokerInfo().getPort());
            //Προετοιμασία ObjectOutputStream
            outFromConsumer = new ObjectOutputStream(sendRequestSocket.getOutputStream());
            //Προετοιμασία ObjectInputStream
            inToConsumer = new ObjectInputStream(sendRequestSocket.getInputStream());

            Message videaki = new Message(SUBSCRIBE); // δημιουργόυμε το μήνυμα με μηνυμα VIDEO_REQUEST που θα σταθλεί στον broker τον σωστό
            videaki.setHashtag(hashtag);
            videaki.requestType =  Variables.RequestType.HASHTAG;

            outFromConsumer.writeObject(videaki); //Χρήση της μεθόδου writeObject της ObjectOutputStream για να στείλει ένα Object στον Broker και στέλνουμε το μήνυμα
            outFromConsumer.flush();

            ConsumerManager manager = new ConsumerManager(inToConsumer,"hashtags");
            manager.start();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Connection will continue to be open to inform you for new videos posted by hashtag: " + hashtag);
//            disconnect(); //δεν θέλουμε να κλείνει αυτό το νέο κοννέκτιον για να ενημερώνει τον κονσούμερ όσο είναι ενεργός αν βγήκε νέο βίντεο με το συγκεκριμένο hashtag!
        }
    }
    //Node Interface Methods
    @Override
    public void connect() {  }


        @Override
    public void disconnect() {
        try {
            inToConsumer.close();
            outFromConsumer.close();
            sendRequestSocket.close();
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    public void showMenu() {
        System.out.println("Select option for Consumer: ");
        System.out.println("Press 1 to request all videos based on a channelname.");
        System.out.println("Press 2 to request all videos based on a hashtag. ");
        System.out.println("Press 3 to subscribe to a channelname. ");
        System.out.println("Press 4 to subscribe to a hashtag. ");
        System.out.println("Press 0 to disconnect");
    }

    public void run(){
        connect();
    }

}
