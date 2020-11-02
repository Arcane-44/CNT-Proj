//import org.graalvm.compiler.replacements.arraycopy.ArrayCopy;
import java.lang.*;

public class Message {

    public static final byte CHOKE = 0;
    public static final byte UNCHOKE = 1;
    public static final byte INTERESTED = 2;
    public static final byte NOT_INTERESTED = 3;
    public static final byte HAVE = 4;
    public static final byte BITFIELD = 5;
    public static final byte REQUEST = 6;
    public static final byte PIECE = 7;

    private static int isHandshake(byte[] msg) {

    }

    public int handshake = null;

    private byte type;

    private byte[] payload;

    public Message(byte[] msg) {
        handshake = isHandshake(msg);
    }

    public static byte[] handshake(int id) {
        byte[] header = "P2PFILESHARINGPROJ".getBytes();
        byte[] msg = new byte[header.length + 10 + 4];

        System.arraycopy(header, 0, msg, 0, header.length);

        Arrays.fill(msg, header.length, header.length + 10, 0);

        byte[] idb = {  (byte) ( (id >> 24) & 0xff ),
                        (byte) ( (id >> 16) & 0xff ),
                        (byte) ( (id >> 8)  & 0xff ),
                        (byte) ( (id >> 0)  & 0xff ) };

        Systems.arraycopy(idb, 0, msg, header.length + 10, idb.length);

        return msg;
    }
}