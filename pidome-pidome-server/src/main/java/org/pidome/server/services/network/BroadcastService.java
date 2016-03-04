/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pidome.server.services.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCUtils;

import org.pidome.server.services.ServiceInterface;
import org.pidome.server.services.provider.CertGen;
import org.pidome.server.system.config.SystemConfig;
import org.pidome.server.system.config.ConfigException;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.network.Network;

public final class BroadcastService implements ServiceInterface {

    int broadcastPort = 0;
    InetAddress broadcastIp;
    InetAddress serverIp;
    MulticastSocket broadcastSocket;
    DatagramPacket broadcastMessage;
    String broadcastMessageString;
    
    static String threadName = "SERVICE:BroadcastServer";
    static Logger LOG = LogManager.getLogger(BroadcastService.class);

    Map<String,Object>eventDetails = new HashMap<>();
    
    Thread broadcastServiceThread;
    
    /**
     * Constructor
     * @throws java.io.IOException
     */
    public BroadcastService() throws IOException {

    }
    
    final void startBroadcastService(){
        try {
            if (SystemConfig.getProperty("system", "network.enablebroadcast").equals("true")) {
                try {
                    if(broadcastServiceThread==null || !broadcastServiceThread.isAlive()){
                        broadcastIp = Network.getBroadcastAddressProperty().get();
                        serverIp = Network.getIpAddressProperty().get();
                        createMessage();
                        createThread();
                        broadcastServiceThread.start();
                    }
                } catch (ConfigPropertiesException | IOException | PidomeJSONRPCException ex){
                    LOG.error("Can not start broadcast: {}", ex.getMessage());
                }
            } else {
                LOG.info("Broadcast service disabled, clients will not be able to do auto discovery");
            }
        } catch (ConfigPropertiesException ex) {
            LOG.error("Could not get broadcast settings, broadcast service will not start");
        }
    }
    
    final void stopBroadcastService(){
        if(broadcastServiceThread!=null && broadcastServiceThread.isAlive()){
            broadcastServiceThread.interrupt();
        }
    }
    
    /**
     * Creates the message to be broadcasted.
     * @throws ConfigException
     * @throws IOException 
     */
    final void createMessage() throws ConfigPropertiesException, IOException, PidomeJSONRPCException {
        broadcastPort = Integer.parseInt(SystemConfig.getProperty("system", "network.broadcastport"));
        eventDetails.put("serviceIp", broadcastIp.toString());
        eventDetails.put("servicePort", broadcastPort);
        broadcastSocket = new MulticastSocket(broadcastPort);
        byte[] message;

        Map<String,Object> msgSet = new HashMap<>();
        msgSet.put("SSL", CertGen.available);
        msgSet.put("SRV", serverIp.getHostAddress());
        if (SystemConfig.getProperty("system", "displayclients.enabled").equals("true")) {
            msgSet.put("DA", (SystemConfig.getProperty("system", "displayclients.ip").equals("network.autodiscovery") ? "SRV" : SystemConfig.getProperty("system", "displayclients.ip")));
            msgSet.put("DP", Integer.parseInt(SystemConfig.getProperty("system", "displayclients.port")));
        }
        if (SystemConfig.getProperty("system", "userclients.enabled").equals("true")) {
            msgSet.put("UA", (SystemConfig.getProperty("system", "userclients.ip").equals("network.autodiscovery") ? "SRV" : SystemConfig.getProperty("system", "userclients.ip")));
            msgSet.put("UP", Integer.parseInt(SystemConfig.getProperty("system", "userclients.port")));
        }
        
        broadcastMessageString = PidomeJSONRPCUtils.createNonRPCMethods(msgSet);
        
        message = broadcastMessageString.getBytes();
        broadcastMessage = new DatagramPacket(message, message.length, broadcastIp, broadcastPort);
        LOG.info("Broadcast message: '{}'", broadcastMessageString);
    }
    
    /**
     * Creates the broadcast thread.
     * @todo This is sooooo ugly, refactor in main overhaul.
     */
    final void createThread() {
        broadcastServiceThread = new Thread() {
            @Override
            public void run() {
                if (broadcastMessage != null) {
                    LOG.info("Started, sending every 7.5 seconds");
                    try {
                        while (true) {
                            try {
                                broadcastSocket.send(broadcastMessage);
                            } catch (IOException e) {
                                broadcastSocket.close();
                            }
                            Thread.sleep(7500);
                        }
                    } catch (InterruptedException e) {
                        if (broadcastSocket.isBound()) {
                            broadcastSocket.close();
                        }
                        LOG.info("Stopped");
                    }
                } else {
                    LOG.info("Not running, there is no broadcast message (check config)");
                }
            }
        };
        broadcastServiceThread.setName(threadName);
    }
    
    /**
     * Returns the broadcast ip address.
     * @return 
     */
    public final String getIpAddress() {
        if (broadcastIp != null) {
            return broadcastIp.getHostAddress();
        } else {
            return "";
        }
    }

    /**
     * Return a combined address for visuals.
     * @return 
     */
    public final String getCombinedAddress(){
        if(broadcastIp==null){
            return "000.000.000.000:0";
        } else {
            return broadcastIp.getHostAddress() +":"+broadcastPort;
        }
    }
    
    /**
     * Returns the message used for sending.
     * @return 
     */
    public final String getMessage(){
        return broadcastMessageString;
    }
    
    /**
     * Stops the service
     */
    @Override
    public void interrupt() {
        stopBroadcastService();
    }

    /**
     * Starts the service
     */
    @Override
    public void start() {
        startBroadcastService();
    }

    /**
     * Checks of the thread is running or not.
     * @return 
     */
    @Override
    public boolean isAlive() {
        if(broadcastServiceThread!=null){
            return broadcastServiceThread.isAlive();
        } else {
            return false;
        }
    }
    
    @Override
    public String getServiceName() {
        return "Server broadcast service";
    }
    
}
