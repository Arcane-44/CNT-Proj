import java.util.*;

public class ChokeTimerTask extends TimerTask {
    public static final boolean OPTIMISTICALLY_UNCHOKED  = false;
    public static final boolean PREFERRED = true;

    private PeerProcess proc;
    private boolean neighbor_type;

    public ChokeTimerTask(PeerProcess proc, boolean neighbor_type) {
        this.proc = proc;
        this.neighbor_type = neighbor_type;
    }

    @Override
    public void run() {
    
        if( neighbor_type == OPTIMISTICALLY_UNCHOKED) {
            proc.updateOptNeighbor();
        } else {
            proc.updatePreferredNeighbors();
        }

    }

}