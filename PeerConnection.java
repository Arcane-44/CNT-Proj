import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.io.*;

abstract class Connection extends Thread{
    protected Socket connection;
    
    private boolean usable = false;
    public boolean usable() { return usable; }

    protected int peerID;
    protected int myID;

    protected ObjectOutputStream out;
    protected ObjectInputStream in;

    public void shutdown() {
        usable = false;
    }

    public void sendMessage(byte[] message) {
        try {
            out.writeObject(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] readMessage() {
        byte[] ret;
        try {
            ret = (byte[]) (in.readObject() );
        }
        catch (Exception e) {
            ret = null;
        }

        return ret;
    }

    protected void waitForHandshake() {
        byte[] msg;
        int shaken_from;
        while( !usable ) {
            msg = readMessage();
            shaken_from = Message.isHandshake(msg);

            if(shaken_from == peerID) {
                usable = true;
            }
            else if(shaken_from != -1) {
                //ERROR
            }

            //send handshake
            sendMessage(Message.handshake(myID));
        }
    }

}

public class PeerConnection extends Thread{

    private Connection con;

    public boolean usable() { return con.usable(); }

    public void sendMessage(byte[] message) {
        con.sendMessage(message);
    }

    public void shutdown() {
        con.shutdown();
    }

    public PeerConnection( String myAddr, int myPort, String peerAddr, int peerPort, boolean isUp, int peerID, int myID) {

        if(isUp) {
            con = new ConnectUp(myPort, peerAddr, peerPort, peerID, myID);
        }
        else {
            con = new ConnectDown(myAddr, myPort, peerAddr, peerPort, peerID, myID);
        }

        con.start();
    }

    private static class ConnectUp extends Connection {
        private InetAddress goalAddr;
        private int goalPort;

        private ServerSocket server;

        public ConnectUp(int myPort, String peerAddr, int peerPort, int peerID, int myID) {
            try {
                this.peerID = peerID;
                this.myID = myID;
                server = new ServerSocket(myPort);
                goalAddr = InetAddress.getByName(peerAddr);
                goalPort = peerPort;
            }catch (Exception e){
                System.out.println("Connection failed");
            }
        }

        public void shutdown() {
            try {
                super.shutdown();
                connection.close();
                server.close();
            }catch (Exception e){
                System.out.println("Connection wasn't sucessfully closed");
            }
        }

        public void run() {

            do {
                try {
                    connection = server.accept();
                    if ((connection.getInetAddress() != goalAddr) || (connection.getPort() != goalPort)) {
                        connection.close();
                        server.close();
                    }
                }catch (Exception e){
                    System.out.println("Failed");
                }
            }
            while( connection.isClosed() );

            try {
                out = new ObjectOutputStream(connection.getOutputStream());
                in = new ObjectInputStream(connection.getInputStream());
                waitForHandshake();

                System.out.println("Connection made with " + connection.getRemoteSocketAddress() + "!");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static class ConnectDown extends Connection {
        private InetSocketAddress goalSocket;

        public ConnectDown( String myAddr, int myPort, String peerAddr, int peerPort, int peerID, int myID ) {
            try {
                this.peerID = peerID;
                this.myID = myID;
                connection = new Socket();
                connection.bind(new InetSocketAddress(myAddr, myPort));
                goalSocket = new InetSocketAddress(peerAddr, peerPort);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        public void shutdown() {
            try {
                super.shutdown();
                connection.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        public void run() {

            if( !connection.isConnected() ) {
                 try {
                      connection.connect(goalSocket);

                     out = new ObjectOutputStream(connection.getOutputStream());

                     in = new ObjectInputStream(connection.getInputStream());

                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        
            sendMessage(Message.handshake(myID));
            waitForHandshake();

            System.out.println("Connection made with " + connection.getRemoteSocketAddress() + "!");
        }
    }
}