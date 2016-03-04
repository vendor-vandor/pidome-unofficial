/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.network.connectivity;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.backend.data.interfaces.network.NetworkBroadcastReceiverInterface;

/**
 *
 * @author John
 */
public class AndroidBroadcastReceiver implements Runnable,NetworkBroadcastReceiverInterface {

    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    
    private NetworkBroadcastReceiverInterface.BroadcastStatus endStatus = NetworkBroadcastReceiverInterface.BroadcastStatus.NOT_FOUND;
    
    private final Context instanceContext;
    
    private String message = "";
    
    public AndroidBroadcastReceiver(Context context){
        instanceContext = context;
    }
    
    @Override
    public void run() {
        try {
            if (Build.PRODUCT.matches(".*_?sdk_?.*")) {
                endStatus = NetworkBroadcastReceiverInterface.BroadcastStatus.NOT_FOUND;
            } else {

                final DatagramSocket socket = new DatagramSocket(10000);
                socket.setBroadcast(true);
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                WifiManager wifi = (WifiManager) instanceContext.getSystemService(Context.WIFI_SERVICE);
                final WifiManager.MulticastLock lock = wifi.createMulticastLock("Log_Tag");

                Runnable run = new Runnable(){
                    @Override
                    public final void run(){
                        try {
                            Thread.sleep(9000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(AndroidBroadcastReceiver.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if(socket!=null && (socket.isBound() || socket.isConnected())){
                            socket.close();
                        }
                        if(lock!=null && lock.isHeld()){
                            lock.release();
                        }
                    }
                };

                exec.submit(run);

                lock.acquire();
                socket.receive(packet);
                exec.shutdownNow();
                socket.close();
                lock.release();
                message = new String(packet.getData(), 0, packet.getLength());
                endStatus = NetworkBroadcastReceiverInterface.BroadcastStatus.FOUND;
                
            }

        } catch (IOException ex) {
            endStatus = NetworkBroadcastReceiverInterface.BroadcastStatus.NOT_FOUND;
        }
    }

    @Override
    public BroadcastStatus getResult() {
        return endStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
    
}
