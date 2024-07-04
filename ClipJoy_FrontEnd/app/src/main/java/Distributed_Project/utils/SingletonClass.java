package Distributed_Project.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.stream.Stream;

import static Distributed_Project.utils.Variables.BROKER_IPS2;
import static Distributed_Project.utils.Variables.BROKER_PORTS;

//SINGLETON CLASS ΓΙΑ ΝΑ ΕΧΟΥΜΕ ΕΝΑ INSTANCE ΤΟΥ PUBLISHER ΚΑΙ ΝΑ ΤΟ ΜΕΤΑΦΕΡΟΥΜΕ ΜΕΤΑΞΥ ACTIVITIES
@RequiresApi(api = Build.VERSION_CODES.N)
public class SingletonClass {

    private PublisherImp publisher;
    String[] brokerIps2 = BROKER_IPS2.split("-"); //Broker's Ips and Ports

    String[] tempBroker = BROKER_PORTS.split("-");
    int[] brokerPorts = Stream.of(tempBroker).mapToInt(Integer::parseInt).toArray();


    //ΤΟ ΑΡΧΙΚΟΙΠΟΙΟΥΜΕ ΜΕ ΤΟ PORT, IPS OF BROKERS FROM VARIABLES, PORTS OF BROKERS FROM VARIABLES ΚΑΙ ΆΔΕΙΟ DIRECTORY
    private SingletonClass() {
        publisher = new PublisherImp("2001" , brokerIps2, brokerPorts, "");
    }

    public PublisherImp getPublisher() {
        return publisher;
    }

    public static SingletonClass mSingleton;

    //ΑΠΑΡΑΙΤΗΤΟ ΓΙΑ ΝΑ ΑΡΧΙΚΟΠΟΙΕΙΤΑΙ ΜΙΑ ΦΟΡΑ
    public static SingletonClass getInstance() {
        if (mSingleton == null) {
            mSingleton = new SingletonClass();
        }
        return mSingleton;
    }

}
