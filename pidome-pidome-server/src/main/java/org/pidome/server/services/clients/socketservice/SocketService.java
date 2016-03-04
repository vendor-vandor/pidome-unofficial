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
package org.pidome.server.services.clients.socketservice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.ServiceInterface;
import org.pidome.server.services.provider.CertGen;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.system.config.SystemConfig;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.network.Network;
import org.pidome.server.services.clients.remoteclient.RemoteClientsConnectionPool;

public final class SocketService implements ServiceInterface {

    ServerSocket server;
    ServerSocket serverSSL;
    Integer port = 0;
    Integer portSSL = 0;
    InetAddress ipAddress;
    final String threadName = "Socket";
    final String threadNameSSL = "SocketSSL";

    static Logger LOG = LogManager.getLogger(SocketService.class);
    
    Map<String,Object>eventDetails = new HashMap<>();
    
    Thread clientDisplayServiceThread;
    Thread clientDisplayServiceThreadSSL;
    
    boolean handleConnections = true;
    
    /**
     * Service constructor.
     * @throws SocketServiceException 
     */
    public SocketService() throws SocketServiceException {
        
    }

    public final String getCombinedAddress(){
        return ipAddress.getHostAddress() +":"+port;
    }
    
    public final String getCombinedSSLAddress(){
        return ipAddress.getHostAddress() +":"+portSSL;
    }
    
    /**
     * Creates the configuration for the service.
     * @throws SocketServiceException 
     */
    final void createConfig() throws SocketServiceException {
        eventDetails.put("serviceName", "ClientService");
        try {
            port = Integer.parseInt(SystemConfig.getProperty("system", "displayclients.port"));
            portSSL = Integer.parseInt(SystemConfig.getProperty("system", "displayclients.sslport"));
            if (!SystemConfig.getProperty("system", "displayclients.ip").equals("network.autodiscovery")) {
                ipAddress = InetAddress.getByName(SystemConfig.getProperty("system", "displayclients.ip"));
            }
        } catch (UnknownHostException | NumberFormatException | ConfigPropertiesException e) {
            LOG.error("Message server configuration error: {}", e.getMessage());
            throw new SocketServiceException("Message server configuration error: " + e.getMessage());
        }
    }
    
    /**
     * Creates the server accept thread.
     */
    final void createThread(){
        clientDisplayServiceThread = new Thread(){
            @Override
            public void run(){
                try {
                    server = new ServerSocket(port, 20, ipAddress);
                    handleConnection();
                } catch (IOException e) {
                    LOG.error("Message server could not be started", e);
                }
            }
        };
        clientDisplayServiceThread.setName(threadName);
        try {
            if(SystemConfig.getProperty("system", "server.enablessl").equals("true") && CertGen.available){
                clientDisplayServiceThreadSSL = new Thread(){
                    @Override
                    public void run(){
                        try {
                            ServerSocketFactory ssocketFactory = SSLServerSocketFactory.getDefault();
                            serverSSL = ssocketFactory.createServerSocket(portSSL, 20, ipAddress);
                            handleSSLConnection();
                        } catch (IOException e) {
                            LOG.error("Message server could not be started", e);
                        }
                    }
                };
                clientDisplayServiceThreadSSL.setName(threadNameSSL);
            }
        } catch (ConfigPropertiesException ex) {
            LOG.error("Could not fetch secure enabled option");
        }
    }
    
    /**
     * Returns the server's ip address.
     * @return
     * @throws SocketServiceException 
     */
    public final String getIpAddress() throws SocketServiceException {
        if(ipAddress!=null){
            return ipAddress.getHostAddress();
        } else {
            throw new SocketServiceException("There is no service ip address known");
        }
    }

    /**
     * Returns the server's used port.
     * @return
     * @throws SocketServiceException 
     */
    public final String getPort() throws SocketServiceException {
        if(port!=null){
            return port.toString();
        } else {
            throw new SocketServiceException("There is no service port known");
        }
    }

    /**
     * Stops the service and network listeners
     */
    @Override
    public void interrupt() {
        stopSocketService();
    }

    /**
     * Closes the server port and nicely disconnects all the clients.
     */
    final void close() {
        try {
            for(SocketServiceClient client:RemoteClientsConnectionPool.getConnectedDisplayClients()){
                release(client);
            }
            server.close();
        } catch (IOException e) {
            LOG.error("Error in server shutdown", e);
        }
        LOG.info("Stopped, clients can not be served");
    }

    /**
     * Responsible for handling the connections made to the server.
     */
    final void handleConnection() {
        LOG.info("Active, waiting for connections on ip: " + ipAddress + " and port: " + port);
        handleConnections = true;
        while (handleConnections == true) {
            try {
                Socket socket = server.accept();
                SocketServiceClient client = new SocketServiceClient(socket);
                client.start();
            } catch (SocketException e){
                /// No action needed.
            } catch (IOException e) {
                LOG.error("Server socket problem", e);
                handleConnections = false;
            }
        }
    }
    
    /**
     * Responsible for handling the connections made to the server.
     */
    final void handleSSLConnection() {
        LOG.info("Active, waiting for connections on ip: " + ipAddress + " and port: " + portSSL);
        handleConnections = true;
        while (handleConnections == true) {
            try {
                Socket socket = serverSSL.accept();
                SocketServiceClient client = new SocketServiceClient(socket);
                client.start();
            } catch (SocketException e){
                /// No action needed.
            } catch (IOException e) {
                LOG.error("Server socket problem", e);
                handleConnections = false;
            }
        }
    }
    
    /**
     * Releases a client and nicely finishes it.
     * @param client 
     */
    final void release(SocketServiceClient client){
        RemoteClient releaseClient = null;
        if(RemoteClientsConnectionPool.getConnectedDisplayClients().contains(client)){
            if(client.isConnected()){
                releaseClient = client;
            }
        }
        if(releaseClient!=null){
            LOG.debug("Closing connection with client: {}", client.getRemoteSocketAddress());
            releaseClient.finish();
            RemoteClientsConnectionPool.removeClient(releaseClient);
        }
    }
    
    /**
     * Starts the service
     */
    final void startSocketService(){
        try {
            ipAddress = Network.getIpAddressProperty().get();
            createConfig();
            createThread();
            if(clientDisplayServiceThread!=null && !clientDisplayServiceThread.isAlive()){
                clientDisplayServiceThread.start();
                LOG.info("Client display terminal server has started");
            }
            if(clientDisplayServiceThreadSSL!=null && !clientDisplayServiceThreadSSL.isAlive()){
                clientDisplayServiceThreadSSL.start();
                LOG.info("Client display terminal SSL server has started");
            }
        } catch (SocketServiceException | UnknownHostException ex) {
            LOG.error("Could not start client displays terminal: {}", ex.getMessage());
        }
    }
    
    /**
     * Stops the service
     */
    final void stopSocketService(){
        close();
        if (clientDisplayServiceThread != null && !clientDisplayServiceThread.isAlive()) {
            clientDisplayServiceThread.interrupt();
        }
        if (clientDisplayServiceThreadSSL != null && !clientDisplayServiceThreadSSL.isAlive()) {
            clientDisplayServiceThreadSSL.interrupt();
        }
        handleConnections = false;
        LOG.info("Client display terminal (ssl) server has stopped");
    }
    
    /**
     * Starts the service and network listeners.
     */
    @Override
    public void start() {
        startSocketService();
    }

    /**
     * Returns if the service is alive.
     * @return 
     */
    @Override
    public boolean isAlive() {
        if(clientDisplayServiceThread!=null){
            return clientDisplayServiceThread.isAlive();
        } else {
            return false;
        }
    }

    @Override
    public String getServiceName() {
        return "Socket service";
    }

    
}
