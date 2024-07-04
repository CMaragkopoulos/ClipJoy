package Distributed_Project;

import java.util.ArrayList;
import java.util.HashMap;

public class ChannelName {

    public String channelName; //Το όνομα του Channel
    public ArrayList<String> hashtagsPublished; //Τα Hashtags του Channel
    public ArrayList<VideoFile> videoList; //Η λίστα με τα ονόματα των video του Channel
    public HashMap<String,VideoFile> userVideoFilesMap; //Η αντιστοίχηση των Hashtags με τα video

    public ChannelName(String channelName, ArrayList<String> hashtagsPublished,String videosDirectory) {
        this.channelName = channelName;
        this.hashtagsPublished = hashtagsPublished;
        userVideoFilesMap = new HashMap<>();
        //videoList.forEach(x -> System.out.println(x.getVideoName()));
        for (int i = 0; i < videoList.size(); i++){
            if (hashtagsPublished.size() == 0) break;
            userVideoFilesMap.put(hashtagsPublished.get(i),videoList.get(i));
        }
        userVideoFilesMap.entrySet().forEach(entry -> {
            //System.out.println(entry.getKey() + " " + entry.getValue().getVideoName()); //Εμφανίζει το HashMap
        });
    }


    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public ArrayList<String> getHashtagsPublished() {
        return hashtagsPublished;
    }

    public void setHashtagsPublished(ArrayList<String> hashtagsPublished) {
        for (int i =0; i < hashtagsPublished.size(); i++) { //checkarei αν υπαρχουν ηδη αυτα που του δωσα ενα ενα και αν οχι τα προσθετει
            if (!this.hashtagsPublished.contains(hashtagsPublished.get(i))) {
                this.hashtagsPublished.add(hashtagsPublished.get(i));
            }
        }
    }
}
