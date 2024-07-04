package Distributed_Project.Broker;
import java.util.stream.Stream;

import static Distributed_Project.utils.Variables.*;

public class BrokerMain {

    public static void main(String[] args) {

        String[] brokerIps = BROKER_IPS.split("-"); //Broker's Ips and Ports
        String[] tempBroker = BROKER_PORTS.split("-");
        int[] brokerPorts = Stream.of(tempBroker).mapToInt(Integer::parseInt).toArray();

        String[] publisherIps = PUBLISHER_IPS.split("-"); //Publisher's Ips and Ports
        String[] tempPublisher = PUBLISHER_PORTS.split("-");
        int[] publisherPorts = Stream.of(tempPublisher).mapToInt(Integer::parseInt).toArray();

        String port = args[0]; //First Argument of the program is the port
        System.out.println("Hello From Broker");
        new BrokerImpl(port,brokerIps,brokerPorts,publisherIps,publisherPorts).connect();
    }

}
