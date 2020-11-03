//import org.graalvm.compiler.replacements.arraycopy.ArrayCopy;
import java.lang.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Message {

    private static int bytesToInt(byte[] bytes) {
        if(bytes.length != 4) {
            //ERROR
        }

        return  (bytes[0] & 0xff) << 24 +
                (bytes[1] & 0xff) << 16 +
                (bytes[2] & 0xff) << 8  +
                (bytes[3] & 0xff) << 0;
    }

    private static byte[] intToBytes(int val) {
        byte[] bytes = {    (byte) ( (val >> 24) & 0xff ),
                            (byte) ( (val >> 16) & 0xff ),
                            (byte) ( (val >> 8)  & 0xff ),
                            (byte) ( (val >> 0)  & 0xff ) };

        return bytes;
    }

    public static final byte CHOKE = 0;
    public static final byte UNCHOKE = 1;
    public static final byte INTERESTED = 2;
    public static final byte NOT_INTERESTED = 3;
    public static final byte HAVE = 4;
    public static final byte BITFIELD = 5;
    public static final byte REQUEST = 6;
    public static final byte PIECE = 7;

    private static int isHandshake(byte[] msg) {
        //
        return 0;

    }

    public int handshake;

    private int len;

    private int type;

    private byte[] payload;

    public static int convertByteArrayToInt(byte[] bytes){
        return ByteBuffer.wrap(bytes).getInt();

    }

    public Message(byte[] msg) {
        handshake = isHandshake(msg);
        byte[] len_array=Arrays.copyOfRange(msg,0,3);
        this.len=bytesToInt(len_array);
        this.type=bytesToInt(Arrays.copyOfRange(msg,3,4));
        this.payload=Arrays.copyOfRange(msg,4,msg.length-1);
    }

    public static byte[] handshake(int id) {
        byte[] header = "P2PFILESHARINGPROJ".getBytes();
        byte[] msg = new byte[header.length + 10 + 4];

        System.arraycopy(header, 0, msg, 0, header.length);

        Arrays.fill(msg, header.length, header.length + 10, (byte) 0);

        byte[] idb = intToBytes(id);

        System.arraycopy(idb, 0, msg, header.length + 10, idb.length);

        return msg;
    }
    public int getType(){
        return type;
    }

    public void setType(int type){
        this.type=type;
    }

    public byte[] getPayload(){
        return this.payload;
    }

    public void setPayload(byte[] payload){
        this.payload=payload;
    }

}