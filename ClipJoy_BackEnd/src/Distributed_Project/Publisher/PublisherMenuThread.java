package Distributed_Project.Publisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class PublisherMenuThread extends Thread {
    //απαραίτητη class επειδη το να πληκτρολογεις εντολες μπλοκάρει το thread, οποτε πρέπει να
    //τρέχει σε άλλο thread το μενού μας για να μην μπλοκάρουμε το main thread που τρέχει την while
    //που δεχεται τα connections
    PublisherImpl publisher = null;

    public PublisherMenuThread(PublisherImpl publisherImp) {
        this.publisher = publisherImp;
    }

    @Override
    public void run() {
        System.out.println("Hello From Publisher!");
        Scanner sc = new Scanner(System.in);
        while (true) {
            this.publisher.printMenu();
            int option = 0;
            try {
                option = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Choose a number from 0 to 3!");
                System.out.println(e);
                continue;
            }
            switch (option) {
                case 1:
                    System.out.println("Type the name you wish: ");
                    String newName = sc.nextLine();
                    this.publisher.channelName.setChannelName(newName);
                    this.publisher.push();
                    break;
                case 2:
                    ArrayList<String> hashtags = new ArrayList<String>();
                    System.out.println("Type a path for your new video: ");
                    String newVideoPath = sc.nextLine();
                    System.out.println("Type hashtags for your new video and type 'done' when you wish to stop! ");
                    String newHashtag = sc.nextLine();
                    while (!newHashtag.equals("done")) {
                        hashtags.add(newHashtag);
                        if (this.publisher.getHashtagPathMap().containsKey(newHashtag)) {
                            ArrayList<String> existingVideoPathList = this.publisher.getHashtagPathMap().get(newHashtag);
                            existingVideoPathList.add(newVideoPath);
                            this.publisher.getHashtagPathMap().put(newHashtag,existingVideoPathList);
                        }
                        else {
                            ArrayList<String> videoPathList = new ArrayList<>();
                            videoPathList.add(newVideoPath);
                            this.publisher.getHashtagPathMap().put(newHashtag,videoPathList);
                        }
                        newHashtag = sc.nextLine();
                    }
                    this.publisher.channelName.setHashtagsPublished(hashtags);
                    this.publisher.read(newVideoPath,"mp4");
                    //System.out.println("Hashtags for push: " + this.publisher.channelName.getHashtagsPublished());
                    //System.out.println("Hashtags for gethashtagpathmap: " + this.publisher.getHashtagPathMap());
                    this.publisher.push();
                    System.out.println("File is ready! ");
                    break;
                case 3:
                    System.out.println("Type a path for the video you wish to delete! ");
                    String videoDeleted = sc.nextLine();
                    this.publisher.removeVideo(videoDeleted);
                    this.publisher.push();
                    break;
                case 0:
                    System.out.println("Have a nice day! [PUBLISHER]");
                    sc.close();
                    return;
            }
        }
    }
}
