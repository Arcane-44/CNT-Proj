import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.*;
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
    private static String pieceFileName = "";

    //track if file downloaded by peers
    private boolean file_downloaded;
    private boolean all_downloaded = false;

    //Peer ID of machine running this peer process
    private int myID;
    private int setID(int id) { return myID = id; };
    public int myID() { return myID; }

    //stores info from common info file
    private static CommonInfo commonInfo = new CommonInfo(commonInfoFileName);                       //I think this is correct

    //Stores messages from different peers in their respective queues.
    private HashMap<Integer, LinkedBlockingQueue<Message>> receivedMessageQueues;

    //maps peerIDs to boolean representing whether they are choking this process.
    private HashMap<Integer, Boolean> unchokedByMap = new HashMap<>();
    private HashMap<Integer, Boolean> preferredNeighborMap = new HashMap<>();
    private int optUnchokedNeighborID;
    private Timer optUnchokeTimer = new Timer();
    private Timer prefUnchokeTimer = new Timer();

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

    //Map to hold Communicator objects for each peer
    private HashMap<Integer, Communicator> communicators = new HashMap<>();

    P2PLogger logger;

    private byte[] getPiece(int index) {
        byte[] ret = null;
        try{
            ret = new byte[commonInfo.pieceSize()];
            FileInputStream pieceFile = new FileInputStream(pieceFileName + index);
            pieceFile.read(ret);
            pieceFile.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**************** Files and initialization ******************/
    private void readPeerInfo() {

        boolean validID = false;
        //Initialize chokeList, wantMe, peerHas by iterating through peers
        for( PeerInfo peer : peerInfo ) {
            if( myID != peer.peerID() ) {
                unchokedByMap.put( Integer.valueOf(peer.peerID()), Boolean.valueOf(false) );    //initially choked by all peers
                preferredNeighborMap.put( Integer.valueOf(peer.peerID()), Boolean.valueOf(false) );    //initial preferred neighbors not set
                wantMe.put( Integer.valueOf(peer.peerID()) , Boolean.valueOf(false) );    //initially unwanted by all peers
            }
            else {
                validID = true;
            }

            if( peer.hasFile() ) {
                peerHas.put( Integer.valueOf(peer.peerID()) , Integer.valueOf(0xffffffff) );
                file_downloaded = true;
            }
            else {
                peerHas.put( Integer.valueOf(peer.peerID()), Integer.valueOf(0) );
                file_downloaded = false;
            }

            peerPort.put(Integer.valueOf(peer.peerID()), Integer.valueOf(peer.port() ) );

            peerHost.put(Integer.valueOf(peer.peerID() ), peer.hostName() );

            peerIDs.add( Integer.valueOf(peer.peerID() ) );

        }

        if(!validID) {
            //ERROR
            System.out.println("Bad ID on command line.");
        }

        if( peerHas.get( Integer.valueOf(myID) ) != 0 ) {
            try {
                FileInputStream fin =  new FileInputStream(fileName);
                BufferedInputStream bin = new BufferedInputStream( fin );
                byte[] buffer = new byte[commonInfo.pieceSize()];
                FileOutputStream pieceFile;

                //break file into piece files
                for (int i = 0; i < commonInfo.numPieces(); i++) {

                    pieceFile = new FileOutputStream(pieceFileName + i);

                    int offset = i*commonInfo.pieceSize();

                    bin.read( buffer, offset, commonInfo.pieceSize() );

                    pieceFile.write( buffer );
                    pieceFile.close();
                }

                fin.close();
                bin.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void shutdown() {
        for( Integer id : peerIDs ) {
            if( id.intValue() != myID )
                communicators.get(id).shutdown();
        }

        optUnchokeTimer.cancel();
        prefUnchokeTimer.cancel();
    }

    private void connectToPeers() {
        System.out.println("Connecting...");

        for ( int id : peerIDs ) {
            if( id != myID ) {
                communicators.put( Integer.valueOf(id) , 
                    new Communicator(   myID,   peerHost.get( Integer.valueOf(myID)),   peerPort.get( Integer.valueOf(myID) ),
                                        id,     peerHost.get( Integer.valueOf(id) ),    peerPort.get( Integer.valueOf(id) ), 
                                        receivedMessageQueues.get(Integer.valueOf(id)), logger ) );
                communicators.get( Integer.valueOf(id) ).start();
            }
        }
        boolean connected_all = false;

        while(!connected_all) {
            connected_all = true;

            for( int id : peerIDs ) {
                if( (id != myID) && (!communicators.get( Integer.valueOf(id) ).usable() ) )
                        connected_all = false;
            }

        }

        System.out.println("Connected to all peers!");
    }


    public void updatePreferredNeighbors() {

    }

    public void updateOptNeighbor() {
        
    }

    
    public static void main(String[] args) {

        PeerProcess me = new PeerProcess();

        //Should have exactly one arg
        if(args.length != 1) {
            //Return error
            System.out.println("Need exactly 1 command line argument.");
            return;
        }

        //Sets my peer ID to the arg from command line
        me.setID( Integer.parseInt(args[0]) );

        //read PeerInfo.cfg using appropriate method
        me.readPeerInfo();

        try {
            me.logger = new P2PLogger(me.myID);
        }
        catch(Exception e) {
            System.out.println("Failed to make Logger.");
            e.printStackTrace();
        }

        //connect to all peers
        me.connectToPeers();

        me.optUnchokeTimer.schedule(new ChokeTimerTask(me, ChokeTimerTask.OPTIMISTICALLY_UNCHOKED), 0, commonInfo.optUnchokeInterval() );
        me.prefUnchokeTimer.schedule(new ChokeTimerTask(me, ChokeTimerTask.PREFERRED), 0, commonInfo.optUnchokeInterval() );

        LinkedBlockingQueue<Message> curr_peer_messages;
        int curr_peer;
        while(!me.all_downloaded) {

            if( !me.file_downloaded ) {
                //Go through all peers' queues
                for( HashMap.Entry<Integer, LinkedBlockingQueue<Message> > entry : me.receivedMessageQueues.entrySet() ) {
                    curr_peer = entry.getKey();
                    curr_peer_messages = entry.getValue();

                    //gets all messages from the peer's queue and handles them
                    while( !curr_peer_messages.isEmpty() ) {
                        InputHandler.handle_input(me, curr_peer_messages.poll(), curr_peer);
                    }
                }

                //Take initiative for sending messages needed

            }
            else {
                //What to do when I have full file?

            }
        }

        me.shutdown();

    }

}