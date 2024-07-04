package Distributed_Project.utils;

import java.io.Serializable;
import java.math.BigInteger;

public class BrokerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    public String ip;
    public int port;
    public BigInteger hashCode;

    public BrokerInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.hashCode = Hash.getHashBigInt(getID());
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return this.port;
    }

    public String getID(){
        return this.ip + this.port;
    }

    public BigInteger getHashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return "Broker with " +
                "ip: " + ip +
                " , port: " + port;
    }
}
