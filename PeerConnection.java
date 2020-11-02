import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.*;
import java.io.*;

abstract class Connection extends Thread{
    protected Socket connection;
    
    protected boolean usable = false;
    public boolean usable() { return usable; }

    protected ObjectOutputStream out;
    protected ObjectInputStream in;

    abstract public void shutdown();

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
        catch (EOFException e) {
            ret = null;
        }

        return ret;
    }

}

public class PeerConnection {

    private Connection con;

    public boolean usable() { return con.usable(); }

    public void sendMessage(byte[] message) {
        con.sendMessage(message);
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
            try {
                server = new ServerSocket(myPort);
                goalAddr = InetAddress.getByName(peerAddr);
                goalPort = peerPort;
            }catch (IOException e){
                System.out.println("Connection failed");
            }
        }

        public void shutdown() {
            try {
                connection.close();
                server.close();
                usable = false;
            }catch (IOException e){
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
                }catch (IOException e){
                    System.out.println("Failed");
                }
            }
            while( connection.isClosed() );

            try {

                out = new ObjectOutputStream(connection.getOutputStream());

                in = new ObjectInputStream(connection.getInputStream());

                usable = true;

                System.out.println("Connection made with " + connection.getRemoteSocketAddress() + "!");
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private static class ConnectDown extends Connection {
        private InetSocketAddress goalSocket;

        public ConnectDown( String myAddr, int myPort, String peerAddr, int peerPort ) {
            try {
                connection = new Socket();
                connection.bind(new InetSocketAddress(myAddr, myPort));
                goalSocket = new InetSocketAddress(peerAddr, peerPort);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        public void shutdown() {
            try {
                connection.close();
                usable = false;
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        public void run() {
            do {

                if( !connection.isConnected() ) {
                    try {
                        connection.connect(goalSocket);

                        out = new ObjectOutputStream(connection.getOutputStream());

                        in = new ObjectInputStream(connection.getInputStream());

                        usable = true;
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }

            }
            while( !usable );

            System.out.println("Connection made with " + connection.getRemoteSocketAddress() + "!");
        }
    }
}