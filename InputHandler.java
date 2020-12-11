//Static class for functionality.
public final class InputHandler {
    //private constructor: DO NOT INSTANTIATE
    private InputHandler() {}

    private static void handle_choke(PeerProcess proc, Message msg, int peerID) {

    }

    private static void handle_unchoke(PeerProcess proc, Message msg, int peerID) {
        
    }

    private static void handle_interested(PeerProcess proc, Message msg, int peerID) {
        
    }

    private static void handle_not_interested(PeerProcess proc, Message msg, int peerID) {
        
    }

    private static void handle_have(PeerProcess proc, Message msg, int peerID) {
        
    }

    private static void handle_bitfield(PeerProcess proc, Message msg, int peerID) {
        
    }

    private static void handle_request(PeerProcess proc, Message msg, int peerID) {
        
    }

    private static void handle_piece(PeerProcess proc, Message msg, int peerID) {
        
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