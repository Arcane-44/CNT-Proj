import java.util.Hashtable;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.*;
import PeerInfo;                                     //IDK how importing local files works


public class PeerProcess {

    /**************************** WORKING DIRECTORY (very important) ****************************/
    private static String workingDir;

    private static String commonInfoFileName;
    private static String peerInfoFileName;


    //Peer ID of machine running this peer process
    private int peerID;
    private setPeerID(int id) {peerID = id;};

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

    //stores all peer info
    private static ArrayList<PeerInfo> peerInfo;

    //Map peer ID to PeerConnection object
    private HashMap<Integer, PeerConnection> connections;



    /**************** Files and initialization ******************/
    private void readPeerInfo() {

        //Get peer info from PeerInfo.cfg file
        //just send line into PeerInfo constructor
        //for/while( ... )


        boolean validID = false;
        //Initialize chokeList, wantMe, peerHas by iterating through peers
        for( PeerInfo peer : peerInfo ) {
            if( peerID != peer.peerID() ) {
                chokedByList.put( peer.peerID(), true );    //initially choked by all peers
                wantMe.put( peer.peerID(), false );    //initially unwanted by all peers
            }
            else
                validID = true;

            if( peer.hasFile() )
                peerHas.put( peer.peerID(), 1);             //TODO: NEED TO CHANGE SHOULD BE ALL ONES
            else
                peerHas.put( peer.peerID(), 0);

        }

        if(!validID) {
            //ERROR
        }
    };

    public void connectToPeers() {

    }

    
    public static void main(String[] args) {

        //Should have exactly one arg
        if(args.length != 1) {
            //Return error
        }

        //Sets peer ID to the arg from command line
        setPeerID( Integer.parseInt(args[0]) );

        //read PeerInfo.cfg using appropriate method


    }

}