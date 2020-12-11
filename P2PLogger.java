import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.*;
import java.io.*;

public class P2PLogger {

    private FileHandler log_file_Handler;
    private SimpleFormatter formatter;
    private Logger logger;
    private int myID;

    public P2PLogger(int peerID) throws IOException {
      myID = peerID;
      logger = Logger.getLogger("Peer" + myID);
      logger.setLevel(Level.INFO);
      log_file_Handler = new FileHandler("log_peer_" + peerID + ".log");
      formatter = new SimpleFormatter();
      log_file_Handler.setFormatter(formatter);
      logger.addHandler(log_file_Handler);
    }

    private static String get_time() {
        Date date = new Date();
        SimpleDateFormat date_format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        return date_format.format(date);
    }

    public void log_tcp_down(int peerID) {
      logger.info(":Peer " + this.myID + " makes a connection to Peer " + peerID + "\n");
    }

    public void log_tcp_up(int peerID) {
      logger.info(":Peer " + this.myID + " is connected to by Peer " + peerID + "\n");
    }

    public void log_preferred_neighbors(int myID, ArrayList<Integer> prefNeighbors) {
      int size=prefNeighbors.size();
      for (int i=0;i<size;i++){
        logger.info(":Peer " + this.myID + " contains neighbor " + prefNeighbors.get(i) + "\n");
      }

    }

    public void log_opt_unchoked(int optUnchokedID) {
      logger.info(": Peer " + this.myID + " has the optimistically unchoked neighbor " + optUnchokedID + "\n");

    }

    public void log_unchoke(int peerID) {
      logger.info(": Peer " + this.myID + " is unchoked by " + peerID + "\n");

    }

    public void log_choke(int myID, int peerID) {
      logger.info(": Peer " + this.myID + " is choked by " + peerID + "\n");

    }

    public void log_have(int peerID, int pieceIndex) {
      logger.info(": Peer " + this.myID + " receives a 'have' message from " + peerID + " for the piece " + pieceIndex + "\n");

    }

    public void log_interested(int peerID) {
      logger.info(": Peer " + this.myID + " receives the 'interested' message from " + peerID + "\n");

    }

    public void log_uninterested(int peerID) {
      logger.info(": Peer " + this.myID + " receives the 'not interested' message from " + peerID + "\n");
    }

    public void log_download_piece(int peerID, int pieceIndex) {
      logger.info(": Peer " + this.myID + " has downloaded the piece " + pieceIndex + " from " + peerID + "\n");
    }

    public void log_complete_download(int myID, int completeDownloadPeerID) {
      logger.info(": Peer " + this.myID + " has downloaded the complete file with id" + completeDownloadPeerID + "\n");

    }

    public void log_handshake(int peerID) {
      logger.info(": Peer " + this.myID + " is handshaked by " + peerID + "\n");

    }

}
