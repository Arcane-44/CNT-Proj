import java.net.*;
import java.io.*;

public class Connector extends Thread {
    private ObjectInputStream in;
    protected ObjectInputStream get_in() { return in; }
    private ObjectOutputStream out;
    protected ObjectOutputStream get_out() { return out; }

    private InetSocketAddress goal_address;
    private Socket connection_socket;
    private ServerSocket server;

    private boolean connected = false;
    protected boolean is_connected() { return connected; }

    private int myID;
    private int targetID;
    private boolean is_up;

    public void shutdown() {
        connected = false;
        try{
            if(is_up) {
                server.close();
            }
            connection_socket.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public Connector(int myID, String myAddr, int myPort, int target_peer, String peerAddr, int peerPort) {
        this.myID = myID;
        targetID = target_peer;
        goal_address = new InetSocketAddress(peerAddr, peerPort);

        is_up = myID < target_peer;

        try{
            if(is_up) {
                server = new ServerSocket(myPort);
            } else {
                connection_socket = new Socket();
                connection_socket.bind( new InetSocketAddress(myAddr, myPort) );
            }
        }
        catch(Exception e) {
            System.out.println("Connection failed");
            e.printStackTrace();
        }
    }

    public ObjectInputStream read_connector() { return in; }
    public ObjectOutputStream write_connector() { return out; }

    public void connect_up() {
        do {
            try {
                connection_socket = server.accept();
                if (connection_socket.getRemoteSocketAddress() != goal_address) {
                    connection_socket.close();
                    server.close();
                }
            }catch (Exception e){
                System.out.println("Failed");
            }
        }
        while( connection_socket.isClosed() );

        try {
            out = new ObjectOutputStream(connection_socket.getOutputStream());
            in = new ObjectInputStream(connection_socket.getInputStream());

            System.out.println("TCP connection made with " + connection_socket.getRemoteSocketAddress() + "!");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void connect_down() {
       try {
        while( !connection_socket.isConnected() )
           connection_socket.connect(goal_address);

        out = new ObjectOutputStream(connection_socket.getOutputStream());
        in = new ObjectInputStream(connection_socket.getInputStream());
        }
        catch (IOException e) {
           e.printStackTrace();
        }
    }

    //The thread is for connecting between peers via TCP
    @Override
    public void run() {
        //Makes TCP connection either up or down.
        //At the end of running, should be connected and should log that TCP connection has been made
        if(is_up) {
            connect_up();
            P2PLogger.log_tcp_up(myID, targetID);
        }
        else {
            connect_down();
            P2PLogger.log_tcp_down(myID, targetID);
        }

        connected = true;
    }
}