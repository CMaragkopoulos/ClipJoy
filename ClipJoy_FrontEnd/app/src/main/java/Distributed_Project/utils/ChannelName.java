package Distributed_Project.utils;

import java.io.Serializable;
import java.util.ArrayList;

public class ChannelName implements Serializable {

    private static final long serialVersionUID = 1L;


    public String channelName; //Το όνομα του Channel
    public ArrayList<String> hashtagsPublished = new ArrayList<String>(); //Τα Hashtags του Channel

    public ChannelName(String channelName, ArrayList<String> hashtagsPublished,String videosDirectory) {
        this.channelName = channelName;
        setHashtagsPublished(hashtagsPublished);
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
