import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.*;

abstract class Connection extends Thread{
    protected Socket connection;
    
    public boolean usable = false;

    protected ObjectOutputStream out;
    protected ObjectInputStream in;

    abstract public void shutdown();

    public void sendMessage(String message) {

    }
}

public class PeerConnection {
    private int myID;

    private Connection con;

    public void sendMessage(String message) {
        
    }

    public void shutdown() {
        con.shutdown();
    }

    public PeerConnection( int myID, String myAddr, int myPort, String peerAddr, int peerPort, boolean isUp) {
        this.myID = myID;

        if(isUp) {
            con = new ConnectUp(myPort, peerAddr, peerPort);
        }
        else {
            con = new ConnectDown(myAddr, myPort, peerAddr, peerPort);
        }

        con.start();
    }

    private static class ConnectUp extends Connection {
        private InetAddress goalAddr;
        private int goalPort;

        private ServerSocket server;

        public ConnectUp(int myPort, String peerAddr, int peerPort) {
            server = new ServerSocket(myPort);
            goalAddr = InetAddress.getByName(peerAddr);
            goalPort = peerPort;
        }

        public void shutdown() {
            connection.close();
            server.close();
            usable = false;
        }

        public void run() {

            do {
                connection = server.accept();
                if( (connection.getInetAddress() != goalAddr) || (connection.getPort() != goalPort ) ) {
                    connection.close();
                    server.close();
                }
            }
            while( connection.isClosed() );

            out = new ObjectOutputStream( connection.getOutputStream() );

            in = new ObjectInputStream( connection.getInputStream() );

            usable = true;

            System.out.println("Connection made with " + connection.getRemoteSocketAddress() + "!");
        }
    }

    private static class ConnectDown extends Connection {
        private InetSocketAddress goalSocket;

        public ConnectDown( String myAddr, int myPort, String peerAddr, int peerPort ) {
            connection = new Socket();
            connection.bind( new InetSocketAddress(myAddr, myPort) );
            goalSocket = new InetSocketAddress(peerAddr, peerPort);
        }

        public void shutdown() {
            connection.close();
            usable = false;
        }

        public void run() {
            do {

                if( !connection.isConnected() ) {
                    connection.connect(goalSocket);

                    out = new ObjectOutputStream( connection.getOutputStream() );

                    in = new ObjectInputStream( connection.getInputStream() );

                    usable = true;
                }

            }
            while( !usable );

            System.out.println("Connection made with " + connection.getRemoteSocketAddress() + "!");
        }
    }
}