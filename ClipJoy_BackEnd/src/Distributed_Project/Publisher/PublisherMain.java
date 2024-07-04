package Distributed_Project.Publisher;

import java.util.stream.Stream;

import static Distributed_Project.utils.Variables.*;

public class PublisherMain {
    public static void main(String[] args) {

        String[] brokerIps = BROKER_IPS.split("-"); //Broker's Ips and Ports
        String[] tempBroker = BROKER_PORTS.split("-");
        int[] brokerPorts = Stream.of(tempBroker).mapToInt(Integer::parseInt).toArray();

        String[] publisherIps = PUBLISHER_IPS.split("-"); //Publisher's Ips and Ports
        String[] tempPublisher = PUBLISHER_PORTS.split("-");
        int[] publisherPorts = Stream.of(tempPublisher).mapToInt(Integer::parseInt).toArray();

        String videosDirectory;
        String port = args[0]; //Port argument for each Publisher
        if (port.equals("2001")) videosDirectory = ORIGIN_DIR_1;
        else if (port.equals("2002")) videosDirectory = ORIGIN_DIR_2;
        else videosDirectory = ORIGIN_DIR_3;
        new PublisherImpl(port,brokerIps,brokerPorts, videosDirectory).connect();
    }
}
