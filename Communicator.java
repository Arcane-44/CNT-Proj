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
    private BitSet myBitfield;
    private P2PLogger logger;

    private boolean usable = false;
    public boolean usable() { return usable; }

    public Communicator(PeerProcess proc, int myID, int target_peer) {
        super(myID, proc.getHost( myID ), proc.getPort(myID), target_peer, proc.getHost(target_peer), proc.getPort(target_peer) );
        this.received_message_queue = proc.getMessageQueue(target_peer);
        myBitfield = proc.peerHas( myID() );
        logger = proc.getLogger();
    }

    public void send_message(byte[] msg) {
        if(usable) {
            sender.add_message(msg);
        } else {
            //CANNOT SEND MESSAGE BECAUSE COMMUNICATOR IS NOT YET USABLE
        } 
    }

    private void handshake_and_bitfield() {
        Message msg;
        boolean shaken = false;
        int shaker;

        //send handshake first if peer is down
        if( !is_up() ) {
            sender.add_message(Message.handshake( myID() ));
        }

        while( !shaken ) {
            msg = received_message_queue.poll();
            shaker = msg.getHandshake();

            if( shaker == targetID() ) {
                shaken = true;
            }
            else if(shaker != -1) {
                //NOT A HANDSHAKE
            }
            else {
                //SOMETHING WENT VERY WRONG
            }
        }

        //send handshake after receiving if peer is up
        if(is_up()) {
            sender.add_message(Message.handshake( myID() ));
        }

        //After all handshaking sent the bitfield
        sender.add_message( Message.bitfield(myBitfield) );
    }

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

        //After running super, TCP connection has been made
        if(is_up()) {
            logger.log_tcp_up( targetID() );
        }
        else {
            logger.log_tcp_up( targetID() );
        }

        //Make reader and sender objects
        reader = new Message_Reader(get_in(), received_message_queue);
        sender = new Message_Sender(get_out());

        //Start the messenger threads so reading and sending of messages can be accomplished
        reader.start();
        sender.start();

        //Complete handshake and bitfield protocol
        handshake_and_bitfield();

        //make communicator usable after handshake protocol complete (maybe)
        usable = true;
    }
}