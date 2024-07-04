package Distributed_Project.Broker;

import Distributed_Project.utils.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class BrokerManager extends Thread {
    //thread που ενημερώνει τον consumer που έχει ξεκινήσει και έχει κάνει subscribe σε κάποιο topic κάθε φορά που ανεβάινει νέο βίντεο στέλνει notification!
    ObjectOutputStream out = null;
    Message mes = null;

    public BrokerManager(ObjectOutputStream out, String topic) {
        this.out = out;
        this.mes = new Message("New video has been uploaded for topic: " + topic);
    }

    public void run(){
        try {
            out.writeObject(mes); // στέλνει το μήνυμα στον consumer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
