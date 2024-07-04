package Distributed_Project.Publisher;


import Distributed_Project.utils.Message;
import Distributed_Project.Node;

import java.util.ArrayList;
import java.util.Set;

public interface Publisher extends Node {

    public void initializationOfChannel();
    public void push();
    public ArrayList<Message> generateChunks(String s);
    public void removeVideo(String s);
    public String read(String rootDir, String type);
    public Set<String> getVideoSet();

}
