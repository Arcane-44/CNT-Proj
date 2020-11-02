//import org.graalvm.compiler.replacements.arraycopy.ArrayCopy;
import java.lang.*;

public class Message {
    public static byte[] handshake(int id) {
        byte[] header = "P2PFILESHARINGPROJ".getBytes();
        byte[] msg = new byte[header.length + 10 + 4];

        System.arraycopy(header, 0, msg, 0, header.length);

        for(int i = header.length; i < header.length + 10; i++) {
            msg[i] = 0;
        }

        

        return msg;
    }
}