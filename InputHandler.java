import java.util.*;

//Static class for functionality.
public final class InputHandler {
    //private constructor: DO NOT INSTANTIATE
    private InputHandler() {}

    private static void handle_choke(PeerProcess proc, Message msg, int peerID) {
        proc.set_unchoked_by(peerID, false);

        //now that I am choked, proc should not be waiting for piece from peer w/ peerID
        proc.stopWaiting(peerID, true);

        proc.getLogger().log_choke(peerID);
    }

    private static void handle_unchoke(PeerProcess proc, Message msg, int peerID) {
        proc.set_unchoked_by(peerID, true);

        proc.getLogger().log_unchoke(peerID);
    }

    private static void handle_interested(PeerProcess proc, Message msg, int peerID) {
        proc.setWantsMe(peerID, true);

        proc.getLogger().log_interested(peerID);
    }

    private static void handle_not_interested(PeerProcess proc, Message msg, int peerID) {
        proc.setWantsMe(peerID, false);

        proc.getLogger().log_uninterested(peerID);
    }

    private static void handle_have(PeerProcess proc, Message msg, int peerID) {
        int index = Message.bytesToInt(msg.getPayload());
        proc.peerHas(peerID).set( index );

        proc.getLogger().log_have(peerID, index);
    }

    private static void handle_bitfield(PeerProcess proc, Message msg, int peerID) {
        BitSet bits = BitSet.valueOf(msg.getPayload());

        proc.peerHas(peerID).clear();
        proc.peerHas(peerID).or(bits);
    }

    private static void handle_request(PeerProcess proc, Message msg, int peerID) {
        int piece_ind = Message.bytesToInt(msg.getPayload());
        //Check if peer is a preferred neighbor or opt unchoked
        if(proc.isPreferred(peerID) || (peerID == proc.getOptUnchokedID()) ) {
            //if so, send piece
            proc.getComm(peerID).send_message( Message.piece(piece_ind, proc.getPiece(piece_ind)) );
        }
        //Otherwise, ignore.
    }

    private static void handle_piece(PeerProcess proc, Message msg, int peerID) {
        byte[] index_bytes = new byte[4];
        int index;
        byte[] piece_content = new byte[PeerProcess.getCommonInfo().pieceSize()];
        
        System.arraycopy(msg.getPayload(), 0, index_bytes, 0, 4);
        index = Message.bytesToInt(index_bytes);
        System.arraycopy(msg.getPayload(), 4, piece_content, 0, PeerProcess.getCommonInfo().pieceSize());

        proc.writePiece(index, piece_content);

        BitSet bits = proc.peerHas(proc.myID());

        bits.set(index);
        proc.getLogger().log_download_piece(peerID, index);

        proc.stopWaiting(peerID, false);
        proc.send_have(index);

        if(bits.cardinality() == bits.size() ) {
            proc.set_downloaded();
            proc.getLogger().log_complete_download();
        }
    }

    //Only public method.
    public static void handle_input(PeerProcess proc, Message msg, int peerID) {
        if(msg.getHandshake() != -1) {
            //ERROR
        }
        switch( msg.getType() ) {
            case Message.CHOKE:
                handle_choke(proc, msg, peerID);
                break;
            case Message.UNCHOKE:
                handle_unchoke(proc, msg, peerID);
                break;
            case Message.INTERESTED:
                handle_interested(proc, msg, peerID);
                break;
            case Message.NOT_INTERESTED:
                handle_not_interested(proc, msg, peerID);
                break;
            case Message.HAVE:
                handle_have(proc, msg, peerID);
                break;
            case Message.BITFIELD:
                handle_bitfield(proc, msg, peerID);
                break;
            case Message.REQUEST:
                handle_request(proc, msg, peerID);
                break;
            case Message.PIECE:
                handle_piece(proc, msg, peerID);
                break;
        }
    }
}