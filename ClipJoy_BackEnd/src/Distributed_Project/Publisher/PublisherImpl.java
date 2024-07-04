package Distributed_Project.Publisher;

import Distributed_Project.utils.BrokerInfo;
import Distributed_Project.utils.Message;
import Distributed_Project.ChannelName;
import Distributed_Project.utils.PublisherInfo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static Distributed_Project.utils.Variables.VIDEO_LIST;
import static Distributed_Project.utils.Variables.VIDEO_PART;

public class PublisherImpl implements Publisher {

    ServerSocket serverSocket;
    Socket connection = null;
    ObjectInputStream in;
    ObjectOutputStream out;

    //Publisher Field
    public ChannelName channelName = null;
    public ArrayList<String> hashtagsPublished;

    public int currentPort;
    public String videosDirectory;
    public ArrayList<BrokerInfo> brokerInfoArrayList;
    private HashMap<String,ArrayList<String>> hashtagPathMap;



    //Constructor
    public PublisherImpl(String port, String[] brokerIps, int[] brokerPorts, String videosDirectory) {
        brokerInfoArrayList = new ArrayList<>();
        currentPort = Integer.parseInt(port);
        this.videosDirectory = videosDirectory;
        for (int i = 0; i < brokerIps.length; i++){
            brokerInfoArrayList.add(new BrokerInfo(brokerIps[i],brokerPorts[i]));
        }
        this.hashtagPathMap = new HashMap<String,ArrayList<String>>();
        initializationOfChannel();
        new PublisherMenuThread(this).start(); //thread που έχει το μενού μας για να βάζουμε channelname και έξτρα βίντεο και hashtags, είτε να τα διαγράφουμε
    }

    //μολις ξεκιναει ο παμπλισερ να του δινουμε ονομα case 1 και μετα να μπορουμε να ανταρουμε βιντεο με το case 2
    public void initializationOfChannel() { //Δίνουμε όνομα στο κανάλι και προσθέτουμε hashtags
        Scanner sc = new Scanner(System.in);
        String name = "temp"; //θα αλλάξει στο PublisherMenuThread
        hashtagsPublished = new ArrayList<>(); //Όλα τα hashtags του Channel
        channelName = new ChannelName(name,hashtagsPublished,videosDirectory); //Αρχικοποιούμε το Channel
        ArrayList<String> hashtags = new ArrayList<String>();
        //διαδικασία για να προσθέσουμε τα βίντεο που έχει ο φάκελος(videosDirectory) μας στο κανάλι μας
        //και να προσθέσουμε και hashtags στο κάθε βίντεο εκέι μέσα
        try (Stream<Path> paths = Files.walk(Paths.get(videosDirectory))) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                if (!path.getFileName().toString().startsWith(".") &&
                        path.getFileName().toString().endsWith("mp4")){ //mp4
                    System.out.println("Found video file: " + path.toString());
                    System.out.println("Type hashtags for the already existing video and type done when you are ready:");
                    String newHashtag = sc.nextLine();
                    while(!newHashtag.equals("done")) {
                        hashtags.add(newHashtag);
                        if (this.getHashtagPathMap().containsKey(newHashtag)) { //αν έχει ξαναγραφτεί αυτό το hashtag
                            ArrayList<String> existingVideoPathList = this.getHashtagPathMap().get(newHashtag); // αντέγραψε την λίστα με τα paths του

                            existingVideoPathList.add(path.toString()); // πρόσθεσε μέσα στην λίστα το καινούριο path
                            this.getHashtagPathMap().put(newHashtag,existingVideoPathList); //ενημέρωσε το hashmap ότι πλέον το hashtag αυτό έχει αυτά τα paths
                        }
                        else { //αν δεν έχει ξαναγραφτεί αυτό το hashtag
                            ArrayList<String> videoPathList = new ArrayList<>();
                            videoPathList.add(path.toString());
                            this.getHashtagPathMap().put(newHashtag,videoPathList); //ενημέρωσε το hashmap ότι πλέον έχει αυτό το hashtag και με αυτό το path
                        }
                        newHashtag = sc.nextLine();
                    }
                    this.channelName.setHashtagsPublished(hashtags);
                    //videoNameList.add(new VideoFile(videoName,channelName)); //Για κάθε video δημιουργεί instance VideoFile
                }
            });
        }
        catch (IOException e){
            e.printStackTrace();
        } finally {
            this.push();
        }
    }

    public void push() {
        Socket socketToBroker = null;
        //σπρωχνεις την πληροφορια του παμπλισερ σε ολους τους μπροκερς
        //Message m = new Message(VIDEO_LIST,"localhost",currentPort); //δημιουργεί το Message με μήνυμα VIDEO_LIST
        //PublisherInfo pubInfo = new PublisherInfo("localhost",currentPort);
        Message m = new Message(VIDEO_LIST,"192.168.2.3",currentPort); //δημιουργεί το Message με μήνυμα VIDEO_LIST
        PublisherInfo pubInfo = new PublisherInfo("192.168.2.3",currentPort);
        pubInfo.setChannelName(channelName.getChannelName());
        pubInfo.setHashtags(channelName.getHashtagsPublished());
        m.setPublisherInfo(pubInfo);
        for (BrokerInfo broker : brokerInfoArrayList) { //designatedbrokerinfoarraylist θα μπορούσε να υπήρχε εδώ για να ενημερώνει μόνο τους brokers που χρείαζονται
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
                System.out.println("Couldn't connect with broker cause it has not started yet! ");
                //e.printStackTrace();
            }
        }
    }

    public ArrayList<Message> generateChunks(String path) { //γυρνάει μια λίστα από Messages που το κάθε ένα έχει κρατήσει chunk του video
        File videoFile = new File(path); //διαβάζει απο σκληρό δίσκο το file
        ArrayList<Message> list = new ArrayList<>(); // αρχικοποιεί κενή λίστα η οποία θα επιστραφεί στο τέλος
        try {
            FileInputStream videoFileStream = new FileInputStream(videoFile);
            int len = videoFileStream.available(); //όσα bytes μπορούν να γίνουν read τα δείχνει στο available()
            int i = 0;
            while (len > 0) {
                if (len > 512000) len = 512000; // MAx δεκτά bytes = 512 kb
                byte[] buffer = new byte[len]; // byte array με χώρο όσο το len
                videoFileStream.read(buffer);  //εδώ τα κάνουμε read
                Message m = new Message(VIDEO_PART + " " + i); //να μας δείχνει ποίο τσανκ γύρισε
                i++;
                m.setVideoChunk(buffer); //βάζουμε το τσανκ στο μήνυμα m ώστε μέσω αυτού να τα βάλουμε στην λίστα
                list.add(m); // βάζουμε τα bytes στην λίστα μας
                len = videoFileStream.available(); //τα μειομένα πλέον bytes που μπορούν να γίνουν read τα βάζουμε στο len
            }
            System.out.println("Τελείωσαν τα chunks");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("THIS_IS_AN_ERROR " + ex);
        }
        return list;
    }

    public HashMap<String, ArrayList<String>> getHashtagPathMap() {
        return hashtagPathMap;
    }

    public void removeVideo(String path) { //μέθοδος για να διαγράφουμε το path σε όποιο hashtag υπάρχει
        Iterator it = this.hashtagPathMap.entrySet().iterator(); //δεικτης που δειχνει με την σειρα στα στοιχεια του hashmap
        System.out.println("Before remove: " + this.hashtagPathMap);
        while (it.hasNext()) {
            HashMap.Entry<String, ArrayList<String>> pair = (HashMap.Entry<String, ArrayList<String>>)it.next();
            ArrayList<String> existingEntryPathList = pair.getValue();
            System.out.println("existingEntryPathList: " + existingEntryPathList);
            if (existingEntryPathList.contains(path)) {
                existingEntryPathList.remove(path);
                if (existingEntryPathList.size() == 0) { //άμα αδείασουν τα path του χασταγκ, διεγραψε το χασταγκ από το κανάλι
                    it.remove(); // avoids a ConcurrentModificationException και τα βγάζει απ το hashtagpathmap
                    this.channelName.hashtagsPublished.remove(pair.getKey());
                    System.out.println("existingEntryPathList(when size=0): " + existingEntryPathList);
                } else {
                    this.hashtagPathMap.put(pair.getKey(), existingEntryPathList);
                    System.out.println("existingEntryPathList(when size!=0: " + existingEntryPathList);
                }
            }
        }
        System.out.println("After remove: " + this.hashtagPathMap);
    }


   public String read(String rootDir, String type) { //Διαβάζει το path με το βίντεο και κραταει μονο το ονομα
       Path p = Paths.get(rootDir);
       String videoName = p.getFileName().toString();
       //System.out.println("Name of the video is: " + videoName);
       if (!p.getFileName().toString().startsWith(".") && p.getFileName().toString().endsWith(type)) { //mp4
           videoName = p.getFileName().toString().replace("." + type, "");
       }
       //System.out.println("Name of the video now is: " + videoName);
       return videoName;
   }

    public Set<String> getVideoSet() { //απαραίτητη μέθοδος για να γυρνάει τα path μόνο μια φορά, οπότε όποια hashtag και να έχουν το ίδιο path αυτό θα γυρνάει μια φορά
        Set<String> videoSet = new HashSet<String>();
        for (String hashtag: this.hashtagPathMap.keySet()) { //για κάθε key(hashtag) του hashtagPathMap
            for (String path: this.hashtagPathMap.get(hashtag)) { //για κάθε path του key(hashtag) του hashtagPathMap
                videoSet.add(path); //addare το στο σετ(τα κρατάει όλα μόνο μια φορά)
            }
        }
        return videoSet;
    }

    public static void printMenu() {
        System.out.println("---MENU---");
        System.out.println("Type 1 to set a name to your channel. ");
        System.out.println("Type 2 to upload a new video to your channel. ");
        System.out.println("Type 3 to delete a video from your channel. ");
        System.out.println("Type 0 to disconnect. ");
    }


    //Node Interface Methods

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
