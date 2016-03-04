/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.networking.connections.broadcasts;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityProvider;
import org.pidome.pcl.backend.data.interfaces.network.NetworkBroadcastReceiverInterface;
import org.pidome.pcl.utilities.WaitRunnable;

/**
 * The main class used to receive the server broadcast data.
 * @author John
 */
public final class BroadcastReceiver {
    
    static {
        Logger.getLogger(BroadcastReceiver.class.getName()).setLevel(Level.ALL);
    }

    /**
     * The replacement receiver.
     */
    private NetworkBroadcastReceiverInterface broadcastReceiver;
    
    /**
     * Class meant to receive the callback event.
     */
    private BroadcastReceiverListener receiver;
    
    /**
     * Network interface to be used for discovery.
     */
    private final NetworkAvailabilityProvider netProvider;
    
    /**
     * Holds the received broadcast message, if received.
     */
    private String broadcastMessage;
    
    /**
     * Socket used to receive the broadcast on.
     */
    private DatagramSocket socket;
    
    /**
     * Port used to receive broadcasts on.
     */
    private int port = 0;
    
    /**
     * Constructor.
     * @param netProvider The network interface to use for listening.
     */
    public BroadcastReceiver(NetworkAvailabilityProvider netProvider){
        this.netProvider = netProvider;
    }
    
    /**
     * Constructor.
     * @param netProvider The network interface to use for listening.
     * @param replacingReceiver If a different receiver is needed then the default one, supply it here.
     */
    public BroadcastReceiver(NetworkAvailabilityProvider netProvider, NetworkBroadcastReceiverInterface replacingReceiver){
        this.netProvider = netProvider;
        this.broadcastReceiver = replacingReceiver;
    }
    
    /**
     * Starts listening for broadcast packages.
     * @param receiver The listener for the broadcast message.
     * @param port The port to listen to.
     */
    public final void listen(BroadcastReceiverListener receiver, int port){
        this.port = port;
        this.receiver = receiver;
        try {
            this.netProvider.discover();
            
            if(broadcastReceiver==null){
                try {
                    if(System.getProperty("os.name").startsWith("Windows")){
                        socket = new DatagramSocket(port, this.netProvider.getIpAddress());
                    } else {
                        socket = new DatagramSocket(port, this.netProvider.getBroadcastAddress());
                    }
                    socket.setBroadcast(true);
                    byte[] buf = new byte[512];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    Timer timer = new Timer();
                    timer.schedule(new ShowStopper(), 10000);
                    _fireServiceEvent(NetworkBroadcastReceiverInterface.BroadcastStatus.START, false);
                    while (true) {
                        try {
                            socket.receive(packet);
                            broadcastMessage = new String(packet.getData(), 0, packet.getLength());
                            socket.close();
                            socket = null;
                            timer.cancel();
                            timer.purge();
                            timer = null;
                            _fireServiceEvent(NetworkBroadcastReceiverInterface.BroadcastStatus.FOUND);
                            break;
                        } catch (Exception ex) {
                            Logger.getLogger(BroadcastReceiver.class.getName()).log(Level.WARNING, "No broadcast received: " +  ex.getMessage(), ex);
                            _fireServiceEvent(NetworkBroadcastReceiverInterface.BroadcastStatus.NOT_FOUND);
                            break;
                        }
                    }
                } catch ( UnknownHostException | SocketException ex) {
                    Logger.getLogger(BroadcastReceiver.class.getName()).log(Level.SEVERE, null, ex);
                    _fireServiceEvent(NetworkBroadcastReceiverInterface.BroadcastStatus.NOT_FOUND);
                }
            } else {
                _fireServiceEvent(NetworkBroadcastReceiverInterface.BroadcastStatus.START, false);
                WaitRunnable letsWait = new WaitRunnable((Runnable)broadcastReceiver);
                letsWait.run();
                letsWait.waitForComplete();
                broadcastMessage = broadcastReceiver.getMessage();
                _fireServiceEvent(broadcastReceiver.getResult(), false);
            }
        } catch ( UnknownHostException ex) {
            Logger.getLogger(BroadcastReceiver.class.getName()).log(Level.SEVERE, null, ex);
            _fireServiceEvent(NetworkBroadcastReceiverInterface.BroadcastStatus.NOT_FOUND);
        }
    }
    
    /**
     * Returns the port used to listen on.
     * @return The port used.
     */
    public final int getPort(){
        return this.port;
    }
    
    /**
     * Timer used to stop broadcast search.
     */
    class ShowStopper extends TimerTask {
        @Override
        public void run() {
            Logger.getLogger(BroadcastReceiver.class.getName()).log(Level.WARNING, "Broadcast timeout, stopping");
            stop();
        }
    }
    
    /**
     * Closes the socket.
     */
    private void closeSocket(){
        if(socket!=null && !socket.isClosed()){
            socket.close();
        }
        socket = null;
    }
    
    /**
     * Used to close the socket and notification of unavailable broadcast.
     */
    private void stop(){
        _fireServiceEvent(NetworkBroadcastReceiverInterface.BroadcastStatus.NOT_FOUND);
    }
    
    /**
     * Returns the broadcast message.
     * @return The catched broadcast message.
     */
    public final String getBroadcastMessage(){
        return broadcastMessage;
    }
    
    /**
     * Removes listener.
     * @param receiver The broadcast listener.
     */
    public void stopListener(BroadcastReceiverListener receiver){
        this.receiver = null;
    }
    
    /**
     * Fires an event when there is some broadcast news.
     * @param eventType The broadcaststatus event type
     */
    public synchronized void _fireServiceEvent(NetworkBroadcastReceiverInterface.BroadcastStatus eventType) {
        _fireServiceEvent(eventType, true);
    }
    
    /**
     * Fires an event when there is some broadcast news.
     * @param eventType 
     */
    private synchronized void _fireServiceEvent(NetworkBroadcastReceiverInterface.BroadcastStatus eventType, boolean end) {
        if(end) closeSocket();
        BroadcastReceiverEvent serviceEvent = new BroadcastReceiverEvent(this, eventType);
        if(this.receiver!=null) this.receiver.handleBroadcastReceiverEvent(serviceEvent);
    }
    
}
