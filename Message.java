//import org.graalvm.compiler.replacements.arraycopy.ArrayCopy;
import java.lang.*;
import java.util.Arrays;
import java.util.BitSet;

public class Message {
    
    public static final byte CHOKE = 0;
    public static final byte UNCHOKE = 1;
    public static final byte INTERESTED = 2;
    public static final byte NOT_INTERESTED = 3;
    public static final byte HAVE = 4;
    public static final byte BITFIELD = 5;
    public static final byte REQUEST = 6;
    public static final byte PIECE = 7;


    private int handshake;
    private int len;
    private int type;
    private byte[] payload;

    public int getHandshake(){ return handshake; }
    public int getType(){ return type; }
    public void setType(int type){ this.type=type; }
    public byte[] getPayload(){ return this.payload; }
    public void setPayload(byte[] payload){ this.payload=payload; }

    public static int bytesToInt(byte[] bytes) {
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

    public static int isHandshake(byte[] msg) {
        //
        return -1;
    }

    public Message(byte[] msg) {
        handshake = isHandshake(msg);
        if(handshake == -1) {
            byte[] len_array=Arrays.copyOfRange(msg,0,3);
            this.len=bytesToInt(len_array);
            this.type=bytesToInt(Arrays.copyOfRange(msg,3,4));
            this.payload=Arrays.copyOfRange(msg,4,msg.length-1);
        }
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

    public static byte[] choke() {
        byte[] ret = new byte[5];

        //put message length in message: payload is 0 bytes
        System.arraycopy(intToBytes(0), 0, ret, 0, 4);
        //message type
        ret[4] = CHOKE;

        return ret;
    }

    public static byte[] unchoke() {
        byte[] ret = new byte[5];

        //put message length in message: payload is 0 bytes
        System.arraycopy(intToBytes(0), 0, ret, 0, 4);
        //message type
        ret[4] = UNCHOKE;

        return ret;
    }

    public static byte[] interested() {
        byte[] ret = new byte[5];

        //put message length in message: payload is 0 bytes
        System.arraycopy(intToBytes(0), 0, ret, 0, 4);
        //message type
        ret[4] = INTERESTED;

        return ret;
    }

    public static byte[] not_interested() {
        byte[] ret = new byte[5];

        //put message length in message: payload is 0 bytes
        System.arraycopy(intToBytes(0), 0, ret, 0, 4);
        //message type
        ret[4] = NOT_INTERESTED;

        return ret;
    }

    public static byte[] have(int index) {
        byte[] ret = new byte[9];

        //put message length in message: payload is 4 bytes
        System.arraycopy(intToBytes(4), 0, ret, 0, 4);
        //message type
        ret[4] = HAVE;
        //payload is index number
        System.arraycopy(intToBytes(index), 0, ret, 5, 4);

        return ret;
    }

    public static byte[] bitfield(BitSet bits) {
        byte[] bitfield_bytes = bits.toByteArray();
        byte[] ret = new byte[5 + bitfield_bytes.length];

        //put message length in message: payload is 4 bytes
        System.arraycopy(intToBytes(4), 0, ret, 0, 4);
        //message type
        ret[4] = BITFIELD;
        //payload is bitfield number
        System.arraycopy(bitfield_bytes, 0, ret, 5, bitfield_bytes.length);

        return ret;
    }

    public static byte[] request(int index) {
        byte[] ret = new byte[9];

        //put message length in message: payload is 4 bytes
        System.arraycopy(intToBytes(4), 0, ret, 0, 4);
        //message type
        ret[4] = REQUEST;
        //payload is index number
        System.arraycopy(intToBytes(index), 0, ret, 5, 4);

        return ret;
    }

    public static byte[] piece(int index, byte[] piece) {
        byte[] ret = new byte[9 + piece.length];

        //put message length in message: payload is 4 bytes
        System.arraycopy(intToBytes(4 + piece.length), 0, ret, 0, 4);
        //message type
        ret[4] = PIECE;
        //payload has index number first
        System.arraycopy(intToBytes(index), 0, ret, 5, 4);
        //payload has content of piece next
        System.arraycopy(piece, 0, ret, 9, piece.length);

        return ret;
    }

}