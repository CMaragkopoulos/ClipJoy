package Distributed_Project;

import java.util.ArrayList;

public class VideoFile {

    private String videoName;
    private String channelName;
    private String dateCreated;
    private String length;
    private String frameRate;
    private String frameWidth;
    private String frameHeight;
    private ArrayList<String> associatedHashtags;
    private byte[] videoFileChunk;

    public VideoFile(String videoName, String channelName, String dateCreated, String length,
                     String frameRate, String frameWidth, String frameHeight) {

        this.videoName = videoName;
        this.channelName = channelName;
        this.dateCreated = dateCreated;
        this.length = length;
        this.frameRate = frameRate;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;

    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(String frameRate) {
        this.frameRate = frameRate;
    }

    public String getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(String frameWidth) {
        this.frameWidth = frameWidth;
    }

    public String getFrameHeight() {
        return frameHeight;
    }

    public void setFrameHeight(String frameHeight) {
        this.frameHeight = frameHeight;
    }

    public ArrayList<String> getAssociatedHashtags() {
        return associatedHashtags;
    }

    public void setAssociatedHashtags(ArrayList<String> associatedHashtags) {
        this.associatedHashtags = associatedHashtags;
    }

    public byte[] getVideoFileChunk() {
        return videoFileChunk;
    }

    public void setVideoFileChunk(byte[] videoFileChunk) {
        this.videoFileChunk = videoFileChunk;
    }
}
