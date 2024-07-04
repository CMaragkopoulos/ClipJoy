package Distributed_Project.utils;

import java.io.Serializable;

public class Variables implements Serializable {
    public static final long serialVersionUID = -1L;


    public static final String BROKER_IP_11 = "192.168.1.13";
    public static final String BROKER_IP_22 = "192.168.1.13";
    public static final String BROKER_IP_33 = "192.168.1.13";
    public static final String BROKER_IP_MAIN = "10.0.2.2";
    public static final String BROKER_IP_MAIN2 = "192.168.1.13";
    public static final String BROKER_IPS2 =
            BROKER_IP_11 + "-" + BROKER_IP_22 + "-" + BROKER_IP_33 + "-";


    public static final int BROKER_PORT_1 = 1001;
    public static final int BROKER_PORT_2 = 1002;
    public static final int BROKER_PORT_3 = 1003;
    public static final int BROKER_PORT_MAIN = 1004;
    public static final String BROKER_PORTS =
            BROKER_PORT_1 + "-" + BROKER_PORT_2 + "-" + BROKER_PORT_3 + "-";


    public static final String PUBLISHER_IP_1 = "10.0.2.2";
    public static final String PUBLISHER_IP_2 = "192.168.1.12"; //wifi->static->ip address
    public static final String PUBLISHER_IP_3 = "localhost";
    public static final String PUBLISHER_IPS =
            PUBLISHER_IP_1 + "-" + PUBLISHER_IP_2 + "-" + PUBLISHER_IP_3 + "-";

    public static final int PUBLISHER_PORT_1 = 2001 ;
    public static final int PUBLISHER_PORT_2 = 2002;
    public static final int PUBLISHER_PORT_3 = 2003;
    public static final String PUBLISHER_PORTS =
            PUBLISHER_PORT_1 + "-" + PUBLISHER_PORT_2 + "-" + PUBLISHER_PORT_3 + "-";

    public static final String VIDEO_REQUEST = "VIDEO_REQUEST";
    public static enum RequestType {HASHTAG,CHANNELNAME};
    public static final String VIDEO_END = "VIDEO_END";
    public static final String VIDEO_LIST = "LIST_LIST";
    public static final String BROKER_REQUEST = "BROKER_REQUEST";
    public static final String CHANNEL_NOT_FOUND = "CHANNEL_NOT_FOUND";
    public static final String CHANNEL_INFO_REQUEST = "CHANNEL_INFO_REQUEST";
    public static final String COMMUNICATION_OVER = "COMMUNICATION_OVER";
    public static final String SUBSCRIBE = "SUBSCRIBE";
    public static final String VIDEO_PART = "VIDEO_PART";
    public static String VIDEOPLAYER = "";
}
