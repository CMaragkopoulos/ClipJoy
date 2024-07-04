package Distributed_Project.utils;

import java.io.Serializable;
import java.util.ArrayList;

public class PublisherInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    public String ip;
    public int port;
    public String channelName;
    public ArrayList<String> hashtags;

    public PublisherInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.channelName = "";
        this.hashtags = new ArrayList<>();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public ArrayList<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(ArrayList<String> hashtags) {
        this.hashtags = hashtags;
    }

    @Override
    public String toString() {
        return "Publisher with " +
                "ip: " + ip +
                " , port: " + port + ", channelname: " + channelName + ", hashtags: " + hashtags;
    }
}
