package Distributed_Project.utils;

public interface Consumer extends Node {
    public void requestVideoByChannelName(String channelName);
    public void requestVideoByHashtag(String hashtag);
    public void subscribeToChannelName(String channelName);
    public void subscribeToHashtag(String hashtag);
}