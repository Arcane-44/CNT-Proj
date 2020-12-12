import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.io.*;
import java.net.*;
import java.lang.Integer;
import java.lang.Math;


public class PeerProcess {
    public class AverageMaintainer implements Comparable<AverageMaintainer> {
        int peerID;

        private long num_entries = 0;
        private long sum_entries = 0;

        public AverageMaintainer(int peerID) {
            this.peerID = peerID;
        }

        public int get_id() { return peerID; }

        public double get_average() {
            if(num_entries == 0) {
                return Double.MAX_VALUE;
            }
            return ( (double) num_entries ) / sum_entries;
        }

        public void add_entry(long entry) {
            num_entries += 1;
            sum_entries += entry;
        }

        @Override
        public int compareTo(AverageMaintainer other) {
            double me = get_average();
            double other_avg = other.get_average();
            
            if( me > other_avg )
                return 1;
            else if( me < other_avg )
                return -1;
            return 0;
        }
    }

    public static final Boolean FALSE = Boolean.valueOf(false);
    public static final Boolean TRUE = Boolean.valueOf(true);

    /**************************** WORKING DIRECTORY (very important) ****************************/
    private static String workingDir;

    private static String commonInfoFileName = "./Common.cfg";
    private static String peerInfoFileName = "./PeerInfo.cfg";
    private static String fileName = "";
    private static String pieceFileName = "";

    //RNG
    private Random rand = ThreadLocalRandom.current();

    //track if file downloaded by peers
    private boolean file_downloaded;
    public void set_downloaded() { file_downloaded = true; }
    private boolean all_downloaded = false;

    //Peer ID of machine running this peer process
    private int myID;
    private int setID(int id) { return myID = id; };
    public int myID() { return myID; }

    //stores info from common info file
    private static CommonInfo commonInfo = new CommonInfo(commonInfoFileName);                       //I think this is correct
    public static CommonInfo getCommonInfo() { return commonInfo; }

    //Stores messages from different peers in their respective queues.
    private HashMap<Integer, LinkedBlockingQueue<Message>> receivedMessageQueues;
    public LinkedBlockingQueue<Message> getMessageQueue(int i) { return receivedMessageQueues.get(Integer.valueOf(i)); }

    //maps peerIDs to boolean representing whether they are choking this process.
    private HashMap<Integer, Boolean> unchokedByMap = new HashMap<>();
    public boolean isUnchokedBy(int id) { return unchokedByMap.get(Integer.valueOf(id)); }
    public void set_unchoked_by(int id, boolean b) { unchokedByMap.put(Integer.valueOf(id), Boolean.valueOf(b)); }

    //functions/variables for storing preferred/optimistic neighbors
    private HashMap<Integer, Boolean> preferredNeighborMap = new HashMap<>();
    public boolean isPreferred(int id) { return preferredNeighborMap.get(Integer.valueOf(id)); }
    private int optUnchokedNeighborID;
    public int getOptUnchokedID() { return optUnchokedNeighborID; }

    //Keep track of data rates and if I am waiting on neighbor
    private HashMap<Integer, Long> waiting = new HashMap<>();
    public long waitingOn(int i) {
        Integer i_Integer = Integer.valueOf(i);
        if( waiting.get(i_Integer) != null ) {
            return waiting.get(i_Integer);
        }
        else {
            return -1;
        }
    }
    public void startWaiting(int i) { waiting.put(Integer.valueOf(i), System.currentTimeMillis()); }
    public void stopWaiting(int i, boolean from_choke) {
        long wait_start = waitingOn(i);
        if(!from_choke && (wait_start >= 0) ) {
            addTime(i, (System.currentTimeMillis() - wait_start) );
            waiting.remove(Integer.valueOf(i));
        }
    }

    private HashMap<Integer, AverageMaintainer> avgTracker = new HashMap<>();
    private ArrayList<AverageMaintainer> maintainerList = new ArrayList<>();
    public AverageMaintainer getAverageMaintainer(int i) { return avgTracker.get(Integer.valueOf(i)); }
    public double avgDataRate(int i) { return getAverageMaintainer(i).get_average(); }
    public void addTime(int peer, long timeEntry) { getAverageMaintainer(peer).add_entry(timeEntry); }

    //Timers for regularly updating neighbors
    private Timer optUnchokeTimer = new Timer();
    private Timer prefUnchokeTimer = new Timer();

    //maps peerIDs to boolean representing whether the corresponding peer is interested.
    private HashMap<Integer, Boolean> wantMe = new HashMap<>();
    public boolean wantsMe(int i) { return wantMe.get(Integer.valueOf(i)); }
    public void setWantsMe(int i, boolean b) { wantMe.put(Integer.valueOf(i), Boolean.valueOf(b)); } 

    //stores the bitfields of peers (and self)
    private HashMap<Integer, BitSet> peerHas = new HashMap<>();
    public BitSet peerHas(int i) { return peerHas.get(Integer.valueOf(i)); }
    public BitSet amInterested(int i) {
        //Copy peer's bitfield
        BitSet ret = peerHas(i);
        //get rid of any that I already have
        ret.andNot(peerHas(myID));
        return ret;
    }
    public void send_have(int piece_ind) {
        for(int id : peerIDs) {
            getComm(id).send_message(Message.have(piece_ind));
        }
    }

    //stores ports for peers
    private HashMap<Integer, Integer> peerPort = new HashMap<>();
    public int getPort(int i) { return peerPort.get(Integer.valueOf(i)); }

    //stores hostnames for peers
    private HashMap<Integer, String> peerHost = new HashMap<>();
    public String getHost(int i) { return peerHost.get(Integer.valueOf(i)); }

    //stores all peer info
    private static ArrayList<PeerInfo> peerInfo = PeerInfo.readPeerInfo(peerInfoFileName);
    private ArrayList<Integer> peerIDs = new ArrayList<>();

    //Map to hold Communicator objects for each peer
    private HashMap<Integer, Communicator> communicators = new HashMap<>();
    public Communicator getComm(int i) { return communicators.get( Integer.valueOf(i) ); }

    private P2PLogger logger;
    public P2PLogger getLogger() { return logger; }

    public byte[] getPiece(int index) {
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

    public void writePiece(int index, byte[] data) {
        try {
            FileOutputStream out = new FileOutputStream(pieceFileName + index);
            out.write(data);
            out.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**************** Files and initialization ******************/
    private void readPeerInfo() {

        boolean validID = false;
        int curr_id;
        Integer curr_id_Integer;
        AverageMaintainer curr_maintainer;
        //Initialize chokeList, wantMe, peerHas by iterating through peers
        for( PeerInfo peer : peerInfo ) {
            curr_id = peer.peerID();
            curr_id_Integer = Integer.valueOf(curr_id);

            if( myID != curr_id ) {
                curr_maintainer = new AverageMaintainer(curr_id);
                avgTracker.put(curr_id_Integer, curr_maintainer );
                maintainerList.add(curr_maintainer);
                unchokedByMap.put( curr_id_Integer, FALSE );            //initially choked by all peers
                preferredNeighborMap.put( curr_id_Integer, FALSE );     //initial preferred neighbors not set
                wantMe.put( curr_id_Integer , FALSE );                  //initially unwanted by all peers
            }
            else {
                validID = true;
            }

            peerHas.put( curr_id_Integer , new BitSet(commonInfo.numPieces()) );
            if( peer.hasFile() ) {
                //Set all bits in my bitfield and set downloaded flag
                peerHas(curr_id).set(0, commonInfo.numPieces());
                file_downloaded = true;
            }
            else {
                //clear all bits in my bitfield and unset downloaded flag
                peerHas(peer.peerID()).clear();
                file_downloaded = false;
            }

            peerPort.put( curr_id_Integer, Integer.valueOf(peer.port()) );
            peerHost.put( curr_id_Integer, peer.hostName() );
            peerIDs .add( curr_id );

        }

        if(!validID) {
            //ERROR
            System.out.println("Bad ID on command line.");
        }

        if( !peerHas(myID).isEmpty() ) {
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
        //shutdown all peer communicators
        for( Integer id : peerIDs ) {
            if( id.intValue() != myID )
                communicators.get(id).shutdown();
        }

        //cancel timers
        optUnchokeTimer.cancel();
        prefUnchokeTimer.cancel();
    }

    private void connectToPeers() {
        System.out.println("Connecting...");

        for ( int id : peerIDs ) {
            if( id != myID ) {
                communicators.put( Integer.valueOf(id) , 
                    new Communicator( this, myID, id ) );
                getComm( id ).start();
            }
        }
        boolean connected_all = false;

        while(!connected_all) {
            connected_all = true;

            for( int id : peerIDs ) {
                if( (id != myID) && (!getComm(id).usable() ) )
                        connected_all = false;
            }

        }

        System.out.println("Connected to all peers!");
    }


    public void updatePreferredNeighbors() {
        ArrayList<Integer> newPrefNeighbors = new ArrayList<>(commonInfo.numPrefNeighbors());

        //Select preferred neighbors randomly if I have the entire file
        if(file_downloaded) {
            Collections.shuffle(peerIDs);
            for(int i = 0; i < commonInfo.numPrefNeighbors(); ++i) {
                newPrefNeighbors.add(peerIDs.get(i));
            }
        } 
        //Otherwise choose preferred neighbors based on fastest data rates
        else {
            Collections.shuffle(maintainerList);
            Collections.sort(maintainerList);
            for(int i = 0; i < commonInfo.numPrefNeighbors(); ++i) {
                newPrefNeighbors.add(maintainerList.get(i).get_id());
            }
        }

        //After updating new values, log it
        logger.log_preferred_neighbors(myID, newPrefNeighbors);
    }

    public void updateOptNeighbor() {
        ArrayList<Integer> interested_choked_peers = new ArrayList<>();
        int rand_index;

        for(int id : peerIDs) {
            if( wantsMe(id) ) {
                interested_choked_peers.add(id);
            }
        }

        //Choose randomly from interested choked peers
        rand_index = rand.nextInt(interested_choked_peers.size());
        optUnchokedNeighborID = interested_choked_peers.get(rand_index);

        //After updating value, log
        logger.log_opt_unchoked(optUnchokedNeighborID);
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
            //Check messages regardless of if I have the file
            for( HashMap.Entry<Integer, LinkedBlockingQueue<Message> > entry : me.receivedMessageQueues.entrySet() ) {
                curr_peer = entry.getKey();
                curr_peer_messages = entry.getValue();

                //gets all messages from the peer's queue and handles them
                while( !curr_peer_messages.isEmpty() ) {
                    InputHandler.handle_input(me, curr_peer_messages.poll(), curr_peer);
                }
            }

            if( !me.file_downloaded ) {
                BitSet interesting_pieces;
                int rand_index;
                int actual_index;

                //Request pieces from interesting peers that are not choking me
                for(int id : me.peerIDs) {
                    interesting_pieces = me.amInterested(id);

                    //only request if I am unchoked by and interested in that peer; otherwise, move on.
                    if( (me.waitingOn(id) < 0) && me.isUnchokedBy(id) && !interesting_pieces.isEmpty() ) {
                        /*  Choose piece to request
                            get random index and get next set bit in bitfield if there is one; otherwise, get previous set bit in bitfield
                            this randomly chosen index allows us to randomly find an interesting piece to request from peer.
                        */
                        rand_index = me.rand.nextInt(commonInfo.numPieces());
                        actual_index = interesting_pieces.nextSetBit(rand_index) < 0 ? interesting_pieces.previousSetBit(rand_index) : interesting_pieces.nextSetBit(rand_index);

                        //request piece with randomly determined index
                        me.getComm(id).send_message(Message.request(actual_index));
                        me.startWaiting(id);
                    }
                }
            }
            else {
                //What to do when I have full file?
            }
        }

        me.shutdown();

    }

}