package Distributed_Project.Broker;

import Distributed_Project.Node;
import Distributed_Project.utils.PublisherInfo;
import Distributed_Project.utils.BrokerInfo;
import Distributed_Project.utils.Message;

import java.util.ArrayList;

public interface Broker extends Node {
        public void pull();
        public BrokerInfo getDesignatedBroker(String data);
        public PublisherInfo getPublisherByChannelName(String channelname);
        public ArrayList<PublisherInfo> getPublisherByHashtag(String hashtag);
        public void forwardToSubscribers(Message mes, String topic);
}

