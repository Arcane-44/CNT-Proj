import java.util.Hashtable;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.*;
//import PeerInfo;                                     //IDK how importing local files works


public class PeerProcess {

    /**************************** WORKING DIRECTORY (very important) ****************************/
    private static String workingDir;

    private static String commonInfoFileName = "./CommonInfo.cfg";
    private static String peerInfoFileName = "./PeerInfo.cfg";


    //Peer ID of machine running this peer process
    private int myID;
    private int setID(int id) { return myID = id; };

    //stores info from common info file
    private static CommonInfo commonInfo = new CommonInfo(commonInfoFileName);                       //I think this is correct

    //maps peerIDs to boolean representing whether they are choking this process.
    private HashMap<Integer, Boolean> chokedByList;

    private ArrayList<Integer> chokedPeersList;

    //maps peerIDs to boolean representing whether the corresponding peer is interested.
    private HashMap<Integer, Boolean> wantMe;

    //stores the bitmaps of peers (and self)
    private HashMap<Integer, Integer> peerHas;

    //stores ports for peers
    private HashMap<Integer, Integer> peerPort;

    //stores hostnames for peers
    private HashMap<Integer, String> peerhost;

    //stores all peer info
    private static ArrayList<PeerInfo> peerInfo = PeerInfo.readPeerInfo(peerInfoFileName);

    //Map peer ID to PeerConnection object
    private HashMap<Integer, PeerConnection> connections;

    private void communicate() {
        //read messages then respond/update info

    }

    /**************** Files and initialization ******************/
    private void readPeerInfo() {

        boolean validID = false;
        //Initialize chokeList, wantMe, peerHas by iterating through peers
        for( PeerInfo peer : peerInfo ) {
            if( myID != peer.peerID() ) {
                chokedByList.put( peer.peerID(), true );    //initially choked by all peers
                wantMe.put( peer.peerID(), false );    //initially unwanted by all peers
                chokedPeersList.put( peer.peerID(), true ); //initially chokes all peers
            }
            else
                validID = true;

            if( peer.hasFile() )
                peerHas.put( peer.peerID(), 0xffffffff);
            else
                peerHas.put( peer.peerID(), 0);

            peerPort.put(peer.peerID(), peer.port() );

            peerHost.put(peer.peerID(), peer.hostName() );

        }

        if(!validID) {
            //ERROR
        }
    };

    public void connectToPeers() {

    }

    
    public static void main(String[] args) {

        PeerProcess me = new PeerProcess();

        //Should have exactly one arg
        if(args.length != 1) {
            //Return error
        }

        //Sets my peer ID to the arg from command line
        me.setID( Integer.parseInt(args[0]) );

        //read PeerInfo.cfg using appropriate method


    }

}