import java.text.SimpleDateFormat;
import java.util.*;

public class Logger {
    private static String get_time() {
        Date date = new Date();
        SimpleDateFormat date_format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        return date_format.format(date);
    }

    public static void log_tcp_up(int myID, int peerID) {

    }

    public static void log_tcp_down(int myID, int peerID) {

    }

    public static void log_preferred_neighbors(int myID, ArrayList<Integer> prefNeighbors) {

    }
    public static void log_opt_unchoked(int myID, int optUnchokedID) {

    }

    public static void log_unchoke(int myID, int peerID) {

    }

    public static void log_choke(int myID, int peerID) {

    }

    public static void log_have(int myID, int peerID, int pieceIndex) {

    }

    public static void log_interested(int myID, int peerID) {

    }

    public static void log_uninterested(int myID, int peerID) {

    }

    public static void log_download_piece(int myID, int peerID, int pieceIndex) {

    }

    public static void log_complete_download(int myID, int completeDownloadPeerID) {

    }

    public static void log_handshake(int myID, int peerID) {

    }

    
}