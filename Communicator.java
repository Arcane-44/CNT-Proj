import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.*;

public class Communicator extends Connector{
    
    public class Message_Reader extends Thread {
        private ObjectInputStream in;
        private LinkedBlockingQueue<Message> received_queue;
        private volatile boolean active;
        private byte[] msg_bytes;

        public Message_Reader(ObjectInputStream i, LinkedBlockingQueue<Message> rcv) {
            in = i;
            received_queue = rcv;
            active = true;
        }

        public void shutdown() {
            active = false;
        }

        @Override
        public void run() {
            while(active) {
                try {
                    msg_bytes = (byte[]) in.readObject();
                    received_queue.add( new Message(msg_bytes) );
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class Message_Sender extends Thread {
        private ObjectOutputStream out;
        private LinkedBlockingQueue<byte[]> sending_queue = new LinkedBlockingQueue<byte[]>();
        private volatile boolean active;

        public Message_Sender(ObjectOutputStream o) {
            out = o;
            active = true;
        }

        public void shutdown() {
            active = false;
        }

        public void add_message(byte[] msg) {
            sending_queue.add(msg);
        }

        @Override
        public void run() {
            while(active) {
                if( !sending_queue.isEmpty() ) {
                    try {
                        out.writeObject( sending_queue.peek() );
                        sending_queue.poll();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private Message_Reader reader;
    private Message_Sender sender;
    private LinkedBlockingQueue<Message> received_message_queue;

    private boolean usable = false;
    public boolean usable() { return usable; }

    public Communicator(int myID, String myAddr, int myPort, int target_peer, String peerAddr, int peerPort, LinkedBlockingQueue<Message> received_message_queue, P2PLogger logger) {
        super(myID, myAddr, myPort, target_peer, peerAddr, peerPort);
        this.received_message_queue = received_message_queue;
    }

    public void send_message(byte[] msg) { if(usable) { sender.add_message(msg); } }

    @Override
    public void shutdown() {
        reader.shutdown();
        sender.shutdown();
        
        super.shutdown();
    }

    @Override
    public void run() {
        //Do task for Connector
        super.run();

        //Make reader and sender objects
        reader = new Message_Reader(get_in(), received_message_queue);
        sender = new Message_Sender(get_out());

        //Complete handshake


        //send bitfield message directly after handshake protocol


        //make communicator usable after handshake protocol complete (maybe)
        usable = true;
    }
}