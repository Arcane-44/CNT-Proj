import java.util.*;
import java.io.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class PeerInfo {
    //Store all peers in an array list
    ArrayList<PeerInfo> all_peers=new ArrayList<PeerInfo>();

    
    //peer ID attribute
    private int peerID;
    public int peerID() {return peerID;};


    //host name attribute
    private String hostName;
    public String hostName() {return hostName;};


    //port attribute
    private int port;
    public int port() {return port;};


    //has file attribute
    private boolean hasFile;
    public boolean hasFile() {return hasFile;};


    public PeerInfo() {
        //parse line here
        try {
            File info = new File("./PeerInfo.cfg");
            Scanner reader = new Scanner(info);
            while(reader.hasNextLine()){
                String line=reader.nextLine();
                String[] contents=line.split("");
                PeerInfo peer = new PeerInfo();
                peer.peerID=Integer.parseInt(contents[0]);
                peer.hostName=contents[1];
                peer.port=Integer.parseInt(contents[2]);
                peer.hasFile=(Integer.parseInt(contents[3])==1)?true:false;
                this.all_peers.add(peer);
            }

        }catch (FileNotFoundException e) {
            System.out.println("Error occured");
        }
    }
}