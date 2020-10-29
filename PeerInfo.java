public class PeerInfo {
    
    //peer ID attribute
    private int peerID;
    public int peerID() {return peerID;};


    //host name attribute
    private String hostName;
    public int hostName() {return hostName;};


    //port attribute
    private int port;
    public int port() {return port;};


    //has file attribute
    private boolean hasFile;
    public boolean hasFile() {return hasFile;};


    public PeerInfo(String infoLine) {
        //parse line here
    }
}