package Distributed_Project.Consumer;

import java.util.Scanner;


public class ConsumerMain {

    public static void main(String[] args) throws InterruptedException {
        String folder = args[0]; //διαβάζει ο κονσούμερ μέσα από το argument το path του φακέλου που μέσα του θα αποθηκεύσει τα βίντεο που επιθυμεί
        System.out.println("Hello From Consumer!");
        Scanner sc = new Scanner(System.in); //για να πληκτρολογέι ο χρήστης ότι θέλει
        ConsumerImpl consumer = new ConsumerImpl(folder); //φτίαχνουμε το consumer instance δίνοντας του μέσα το path που μας δώσαν στο argument
        while (true) {
            consumer.showMenu(); //εμφανίζει στον χρήστη το μενού με τις επιλογές που θα έχει ως κονσούμερ!
            int option = 0;
            try {
                option = Integer.parseInt(sc.nextLine()); //μετατρέπει σε integer το string που θα μας δώσει ο χρήστης
            } catch (Exception e) {
                System.out.println("Choose a number from 0 to 4!"); //άμα δώσει αριθμό που δεν εξυπηρετούμε του λέμε να δώσει σωστό
                continue;
            }
            switch (option) {
                case 1:
                    System.out.println("Type the name of the channel: ");
                    String channelName = sc.nextLine(); //διαβάζει το channelname που μας πληκτρολόγισαν
                    consumer.requestVideoByChannelName(channelName); //μέθοδος που γυρνάει όσα βίντεο έχει αυτό το channelname
                    break;
                case 2:
                    System.out.println("Type a hashtag: ");
                    String hashtag = sc.nextLine(); //διαβάζει το hashtag που μας πληκτρολόγισαν
                    consumer.requestVideoByHashtag(hashtag); //μέθοδος που γυρνάει όσα βίντεο έχει αυτό το hashtag
                    break;
                case 3:
                    System.out.println("Type the name of the channel: ");
                    String channelNameSub = sc.nextLine();
                    consumer.subscribeToChannelName(channelNameSub); //μέθοδος θα κρατήσει ένα κοννεκτιον ανοιχτό το οποίο θα ενημερώνει τον κονσουμερ
                                                                     //ότι βγηκε νεο βιντεο με αυτό το channelname οπότε θα μπορεί ο κονσούμερ να
                                                                     //τα ξαναζητησει ολα με το case 1 και να ρθουν και τα καινουρια βιντεο πλεον
                    break;
                case 4:
                    System.out.println("Type a hashtag: ");
                    String hashtagSub = sc.nextLine();
                    consumer.subscribeToHashtag(hashtagSub); //μέθοδος θα κρατήσει ένα κοννεκτιον ανοιχτό το οποίο θα ενημερώνει τον κονσουμερ
                                                             //ότι βγηκε νεο βιντεο με αυτό το hashtag οπότε θα μπορεί ο κονσούμερ να
                                                             //τα ξαναζητησει ολα με το case 1 και να ρθουν και τα καινουρια βιντεο πλεον

                    break;
                case 0:
                    System.out.println("Have a nice day! [CONSUMER]");
                    sc.close(); //κλείνει το σκάννερ αφόυ ο κονσούμερ δεν θέλει να χρησιμοποιήσει άλλο την εφαρμογή μας
                    return;
            }
        }
    }
}
