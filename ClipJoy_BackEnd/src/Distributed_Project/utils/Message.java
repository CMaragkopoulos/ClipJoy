package Distributed_Project.utils;

import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {

    public static final long serialVersionUID = 123456789L; //This used by Serializable

    public String channelName;
    public String hashtag;
    public String message;
    public ArrayList<String> videoList;
    public PublisherInfo publisherInfo;
    byte[] videoChunk;
    public Variables.RequestType requestType;
    public BrokerInfo brokerInfo; //new
    public String videoName;

    public String ip;
    public int port;

    public Message(String message){
        this.message = message;
        this.channelName = "";
        this.hashtag = " ";
        this.requestType = Variables.RequestType.CHANNELNAME;
    }

    public Message(String message, String ip,int port)
    {
        this.message = message;
        this.ip = ip;
        this.port = port;
        this.requestType = Variables.RequestType.CHANNELNAME;
    }

    public Message(String channelName, String hashtag) {
        super();
        this.channelName = channelName;
        this.hashtag = hashtag;
        this.message = channelName;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    public void setVideoList(ArrayList<String> videoList) {
        this.videoList = videoList;
    }

    public ArrayList<String> getVideoList() {
        return videoList;
    }

    public void setPublisherInfo(PublisherInfo publisherInfo) {
        this.publisherInfo = publisherInfo;
    }

    public void setVideoChunk(byte[] buffer) {
        this.videoChunk = buffer.clone();
    }

    public byte[] getVideoChunk() {
        return videoChunk;
    }

    public BrokerInfo getBrokerInfo() { return brokerInfo; }

    public void setBrokerInfo(BrokerInfo brokerInfo) { this.brokerInfo = brokerInfo; }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    @Override
    public String toString() {
        return "channelName: " + channelName +
                ", hashtag: " + hashtag;
    }
}
