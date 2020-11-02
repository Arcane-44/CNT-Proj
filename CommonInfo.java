import java.util.*;
import java.io.File;
import java.util.Scanner;
import java.io.FileNotFoundException;

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
        try {
            File info = new File(filename);
            Scanner reader = new Scanner(info);
            while(reader.hasNextLine()){
                String line = reader.nextLine();
                String[] contents=line.split(" ");
                String sample=contents[0];
                if(sample.equals("NumberOfPreferredNeighbors")){
                    //System.out.println("YESSS");
                    //System.out.println(contents[1]);
                    this.numPrefNeighbors=Integer.parseInt(contents[1]);
                }else if(sample.equals("UnchokingInterval")){
                    this.unchokeInterval=Integer.parseInt(contents[1]);
                }else if(sample.equals("OptimisticUnchokingInterval")){
                    this.optUnchokeInterval=Integer.parseInt(contents[1]);
                }else if(sample.equals("FileName")){
                    this.fileName=contents[1];
                }else if(sample.equals("FileSize")){
                    this.fileSize=Integer.parseInt(contents[1]);
                }else if(sample.equals("PieceSize")){
                    this.pieceSize=Integer.parseInt(contents[1]);
                }

            }
            this.numPieces=Math.round(this.fileSize/this.pieceSize);
            reader.close();

        }catch(FileNotFoundException e) {
            System.out.println("Error occured");

        }

    }

}