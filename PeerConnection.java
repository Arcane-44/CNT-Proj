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
        while( !usable ) {
            msg = readMessage();

            if(Message.isHandshake(msg) == peerID) {
                usable = true;
            }
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

    public PeerConnection( String myAddr, int myPort, String peerAddr, int peerPort, boolean isUp, int peerID) {

        if(isUp) {
            con = new ConnectUp(myPort, peerAddr, peerPort, peerID);
        }
        else {
            con = new ConnectDown(myAddr, myPort, peerAddr, peerPort, peerID);
        }

        con.start();
    }

    private static class ConnectUp extends Connection {
        private InetAddress goalAddr;
        private int goalPort;

        private ServerSocket server;

        public ConnectUp(int myPort, String peerAddr, int peerPort, int peerID) {
            try {
                this.peerID = peerID;
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

        public ConnectDown( String myAddr, int myPort, String peerAddr, int peerPort, int peerID ) {
            try {
                this.peerID = peerID;
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
        
            waitForHandshake();

            System.out.println("Connection made with " + connection.getRemoteSocketAddress() + "!");
        }
    }
}