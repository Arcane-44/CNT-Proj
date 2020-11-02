import java.util.Hashtable;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.*;
import java.lang.Integer;
import java.lang.Math;
//import PeerInfo;                                     //IDK how importing local files works


public class PeerProcess {

    /**************************** WORKING DIRECTORY (very important) ****************************/
    private static String workingDir;

    private static String commonInfoFileName = "./Common.cfg";
    private static String peerInfoFileName = "./PeerInfo.cfg";
    private static String fileName = "";

    //Peer ID of machine running this peer process
    private int myID;
    private int setID(int id) { return myID = id; };

    //stores info from common info file
    private static CommonInfo commonInfo = new CommonInfo(commonInfoFileName);                       //I think this is correct
    
    private byte[][] piecedData = new byte[ commonInfo.numPieces() ] [commonInfo.pieceSize()];


    //maps peerIDs to boolean representing whether they are choking this process.
    private HashMap<Integer, Boolean> chokedByList = new HashMap<>();

    private ArrayList<Integer> chokedPeersList;

    //maps peerIDs to boolean representing whether the corresponding peer is interested.
    private HashMap<Integer, Boolean> wantMe = new HashMap<>();

    //stores the bitmaps of peers (and self)
    private HashMap<Integer, Integer> peerHas = new HashMap<>();

    //stores ports for peers
    private HashMap<Integer, Integer> peerPort = new HashMap<>();

    //stores hostnames for peers
    private HashMap<Integer, String> peerHost = new HashMap<>();

    //stores all peer info
    private static ArrayList<PeerInfo> peerInfo = PeerInfo.readPeerInfo(peerInfoFileName);

    private ArrayList<Integer> peerIDs = new ArrayList<>();

    //Map peer ID to PeerConnection object
    private HashMap<Integer, PeerConnection> connections = new HashMap<>();

    private void communicate() {
        //read messages then respond/update info

    }

    /**************** Files and initialization ******************/
    private void readPeerInfo() {

        boolean validID = false;
        //Initialize chokeList, wantMe, peerHas by iterating through peers
        for( PeerInfo peer : peerInfo ) {
            if( myID != peer.peerID() ) {
                chokedByList.put( Integer.valueOf(peer.peerID()), Boolean.valueOf(true) );    //initially choked by all peers
                wantMe.put( Integer.valueOf(peer.peerID()) , Boolean.valueOf(false) );    //initially unwanted by all peers
            }
            else
                validID = true;

            if( peer.hasFile() )
                peerHas.put( Integer.valueOf(peer.peerID()) , Integer.valueOf(0xffffffff) );
            else
                peerHas.put( Integer.valueOf(peer.peerID()), Integer.valueOf(0) );

            peerPort.put(Integer.valueOf(peer.peerID()), Integer.valueOf(peer.port() ) );

            peerHost.put(Integer.valueOf(peer.peerID() ), peer.hostName() );

            peerIDs.add( Integer.valueOf(peer.peerID() ) );

        }

        if(!validID) {
            //ERROR
        }

        if( peerHas.get( Integer.valueOf(myID) ) != 0 ) {
            try {

                FileInputStream file = new FileInputStream(fileName);

                byte[] fileData = new byte[ commonInfo.fileSize() ];

                file.read(fileData, 0, commonInfo.fileSize() );

                file.close();

                //put file into piecedData array as pieces
                for (int i = 0; i < commonInfo.numPieces(); i++) {

                    int offset = i*commonInfo.pieceSize();

                    System.arraycopy(fileData, offset, piecedData[i], 0, Math.min(commonInfo.pieceSize(), commonInfo.fileSize() - offset ) );
                }

            }
            catch(Exception e) {
                e.printStackTrace();
            }

        }
    };

    public void shutdown() {
        for( Integer id : peerIDs ) {
            if( id.intValue() != myID )
                connections.get(id).shutdown();
        }
    }

    public void connectToPeers() {
        System.out.println("Connecting...");

        for ( int id : peerIDs ) {
            if( id != myID ) {
                connections.put( Integer.valueOf(id) , 
                            new PeerConnection( peerHost.get( Integer.valueOf(myID) ), peerPort.get( Integer.valueOf(myID) ), peerHost.get( Integer.valueOf(id) ), peerPort.get( Integer.valueOf(id) ), (id > myID) ) );
            }
        }
        boolean connected = false;

        while(!connected) {
            connected = true;

            for( int id : peerIDs ) {

                if( (id != myID) && (!connections.get( Integer.valueOf(id) ).usable() ) )
                        connected = false;

            }

        }

        System.out.println("Connected to all peers!");

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
        me.readPeerInfo();

        me.connectToPeers();

        me.shutdown();

    }

}