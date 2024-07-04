package Distributed_Project.Consumer;

import Distributed_Project.utils.Message;

import java.io.IOException;
import java.io.ObjectInputStream;

public class ConsumerManager extends Thread {
    //thread που ενημερώνει τον consumer που έχει ξεκινήσει και έχει κάνει subscribe σε κάποιο topic κάθε φορά που σκάει ενημέρωση για νεο βίντεο από τον BrokerManager
    ObjectInputStream in = null;
    String type = null;


    public ConsumerManager(ObjectInputStream in, String type) {
        this.type = type;
        this.in = in;
    }

    public void run(){
        try {
            while (true) { //receive subscription messages(μένει πάντα ανοιχτό)
                Message mes = null;
                mes = (Message) in.readObject();
                System.out.println("For " + this.type +": " + mes.message);
                if (type == "channelnames") {
                    System.out.println("You can press 1 to request all videos updated on this channelname! ");
                } else {
                    System.out.println("You can press 2 to request all videos updated on this hashtag! ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
