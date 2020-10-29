public class CommonInfo {

    //number of preferred neighbors and getter
    private int numPrefNeighbors;
    public int numPrefNeighbors() { return numPrefNeighbors; }


    //unchoke interval and getter
    private int unchokeInterval;
    public int unchokeInterval() {  return unchokeInterval;  }


    //optimistic unchoke interval and getter
    private int optUnchokeInterval;
    public int optUnchokeInterval() {  return optUnchokeInterval;  }


    //file name and getter
    private String fileName;
    public String fileName() { return fileName;  }


    //file size and getter
    private int fileSize;
    public int fileSize() {   return fileSize;   }


    //piece size and getter
    private int pieceSize;
    public int pieceSize() {  return pieceSize;  }


    //number of pieces and getter
    private int numPieces;
    public int numPieces() {  return numPieces;  }


    //Constructor
    public CommonInfo(String filename) {
        //read file here.

    }

}