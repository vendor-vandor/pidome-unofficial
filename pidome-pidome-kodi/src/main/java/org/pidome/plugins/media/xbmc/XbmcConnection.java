/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.media.xbmc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.pidome.plugins.media.xbmc.XbmcConnection.LOG;

/**
 * used for connecting to an xbmc instance.
 * @author John Sirach
 */
public final class XbmcConnection {

    static Logger LOG = LogManager.getLogger(XbmcConnection.class);
    
    public enum XbmcVersion {
        VERSION_12;
    }
    
    Long lastMessageTime;
    
    Socket socket;
    InetAddress ipAddress;
    int port;
    int httpPort;
    
    int reconnect = 30;
    
    PrintWriter writer;
    BufferedInputStream reader;

    XbmcVersion version; 
    
    Thread connectionListener;
    Thread reconnectThread;
    Thread monitorThread;
    
    String setCheckedMessage;
    
    List<XbmcConnectionListener> _listeners = new ArrayList<>();
    
    boolean closeInitialized = false;
    
    int openJsonChars = 0;
    int closedJsonChars = 0;
    
    String base64AuthString = "";
    
    /**
     * Constructor sets ip, port and xbmc version.
     */
    public XbmcConnection() {}
    
    public final void setConfiguration(String ipAddress, int port, int httpPort, int reconnect, String userName, String password, XbmcVersion version) {
        try {
            this.ipAddress = InetAddress.getByName(ipAddress);
        } catch (Exception ex){
            LOG.error("Problem interpreting ip addres {}", ipAddress, ex.getMessage());
        }
        this.port = port;
        this.httpPort = httpPort;
        this.version = version;
        this.reconnect = reconnect;
        if(userName!=null && !userName.isEmpty() && password!=null && !password.isEmpty()){
            this.base64AuthString = "Basic " +  new String(Base64.getEncoder().encode((userName + ":" + password).getBytes()));
        }
    }
    
    /**
     * Opens the connection and sets port listener and sender.
     * @throws IOException 
     */
    public final void open() throws IOException {
        reconnect();
    }
    
    /**
     * Returns true if the service is trying to reconnect.
     * @return 
     */
    public final boolean isInReconnect(){
        return (reconnectThread!=null && reconnectThread.isAlive());
    }
    
    /**
     * Initializes and starts checking when the last message is received from XBMC.
     * @param checkMessage
     */
    public final void lastMessageThread(final String checkMessage){
        if (checkMessage != null) {
            setCheckedMessage = checkMessage;
        }
        final String pingMessage = setCheckedMessage;
        if(setCheckedMessage!=null && (monitorThread==null || !monitorThread.isAlive())){
            monitorThread = new Thread(){
                @Override
                public final void run(){
                    while(true){
                        try {
                            if(socket != null && writer!=null && reader!=null){
                                send(pingMessage);
                                try {
                                    /// Wait 3 seconds for a last received message check.
                                    Thread.sleep(3000);
                                    /// A second latency is much, but now sure it is really missed when failed.
                                    if((lastMessageTime + 4000) < System.currentTimeMillis()){
                                        LOG.warn("Looks like last message is longer ago then set, xbmc gone? Closing connection.");
                                        internalClose();
                                    }
                                    /// Wait timeout/connection check (via settings) seconds for connection check.
                                    Thread.sleep(reconnect * 1000L);
                                    
                                } catch (InterruptedException ex) {
                                    LOG.error("Can not wait for next connection check event, plugin should be restarted manually");
                                }
                            }
                        } catch (IOException ex){
                            LOG.error("Could not send ping: {}", ex.getMessage());
                            internalClose();
                        }
                    }
                }
            };
            monitorThread.setName("XBMC if present thread");
            monitorThread.start();
        }
    }
    
    /**
     * Initializes a reconnect thread and starts it.
     */
    final void reconnect(){
        if(reconnectThread==null || !reconnectThread.isAlive()){
            XbmcConnection myself = this;
            reconnectThread = new Thread(){
                @Override
                public final void run(){
                    boolean keepRunning = true;
                    while(keepRunning){
                        try {
                            socket = new Socket(ipAddress, port);
                            socket.setKeepAlive(true);
                            writer = new PrintWriter(socket.getOutputStream(),true);
                            reader = new BufferedInputStream(socket.getInputStream());
                            lastMessageTime = System.currentTimeMillis();
                            startReaderListener();
                            LOG.info("Connected");
                            _fireConnectionEvent("CONNECTED");
                            break;
                        } catch (Exception ex){
                            synchronized (myself) {
                                myself.notifyAll();
                            }
                            LOG.error("Could not (re)connect: {}", ex.getMessage());
                            try {
                                LOG.info("Waiting {} seconds", reconnect);
                                Thread.sleep(reconnect * 1000L);
                            } catch (InterruptedException e) {
                                keepRunning = false;
                            }
                        }
                    }
                }
            };
            reconnectThread.setName("XBMC reconnect thread");
            reconnectThread.start();
        }
    }
    
    /**
     * Closes the port.
     */
    final void portCloser(){
        try {
            this.socket.close();
        } catch (Exception ex) {
            LOG.error("Socket close error: {}", ex.getMessage());
        }
        socket = null;
        try {
            writer.close();
        } catch (Exception ex){
            LOG.error("Socket writer close error: {}", ex.getMessage());
        }
        writer = null;
        try {
            reader.close();
        } catch (Exception ex) {
            LOG.error("Socket reader close error: {}", ex.getMessage());
        }
        reader = null;
        LOG.info("Connection closed");
        _fireConnectionEvent("DISCONNECTED");
    }
    
    /**
     * Close the connection without removing list.
     * The main difference is that internal close starts the (re)connect thread and is thus only used when there is an need for continuing.
     */
    public final void internalClose() {
        closeInitialized = true;
        portCloser();
        Thread waitforIt = new Thread() {
            @Override
            public final void run() {
                try {
                    Thread.sleep(10000L);
                    reconnect();
                } catch (InterruptedException ex) {
                    LOG.error("Automatic reconnection routine failed ({}), restart the plugin when applyable.", ex.getMessage());
                }
            }
        };
        waitforIt.start();
    }
    
    /**
     * Closes the connection and removes listeners.
     * Removes listeners if not done earlier. 
     */
    public final void close() {
        closeInitialized = true;
        if(reconnectThread!=null && reconnectThread.isAlive()){
            reconnectThread.interrupt();
        }
        if(monitorThread!=null && monitorThread.isAlive()){
            monitorThread.interrupt();
        }
        if(connectionListener!=null){
            if(connectionListener.isAlive()){
                connectionListener.interrupt();
            }
            connectionListener = null;
        }
        portCloser();
        _listeners.clear();
    }
    
    /**
     * Sends a command.
     * @param command
     * @throws IOException 
     */
    public final void send(String command) throws IOException {
        if(writer!=null){
            LOG.trace("Sending: {}", command);
            writer.println(command);
            writer.flush();
        } else {
            throw new IOException("There is no connection");
        }
    }
    
    /**
     * Starts the internal data listener.
     * Because of xbmc does not do any newline characters at the end of a received broadcasted json broadcast we just 
     * count the amount of opening "{" and closing "}" characters. At the moment the amount of closing "}" characters 
     * matches the amount of open "{" characters i assume to have a legit json string. Allthough this does not guarantee 
     * 100% accuracy i do not have an other option as of this moment, and i do not want to initialize an json object on
     * any received byte.
     * @throws IOException 
     */
    public final synchronized void startReaderListener() throws IOException{
        if(this.socket!=null && reader!=null){
            if(connectionListener==null || !connectionListener.isAlive()){
                connectionListener = new Thread(){
                    @Override
                    public final void run(){
                        byte[] readBytes = new byte[16384];
                        int lengthRead = 0;
                        int character;
                        try {
                            while ((character = reader.read()) != -1) {
                                try {
                                    readBytes[lengthRead] = (byte) character;
                                    lengthRead++;
                                    switch (character) {
                                        case 123: //{
                                            openJsonChars++;
                                            break;
                                        case 125: /// }
                                            closedJsonChars++;
                                            break;
                                    }
                                    if (openJsonChars != 0 && closedJsonChars == openJsonChars) {
                                        byte[] resultBytes = new byte[lengthRead];
                                        System.arraycopy(readBytes, 0, resultBytes, 0,lengthRead);
                                        String jsonString = new String(resultBytes, "UTF-8");
                                        LOG.trace("Got json string: {}", jsonString);
                                        _fireDataEvent(jsonString);
                                        openJsonChars = 0;
                                        closedJsonChars = 0;
                                        readBytes = new byte[16384];
                                        lengthRead=0;
                                    } else if (closedJsonChars > openJsonChars){
                                        closedJsonChars = 0;
                                        readBytes = new byte[16384];
                                        lengthRead=0;
                                    }
                                } catch (Exception ex){
                                    LOG.error("Could not correctly read last message: {}", ex.getMessage(), ex);
                                    openJsonChars = 0;
                                    closedJsonChars = 0;
                                    lengthRead=0;
                                }
                                lastMessageTime = System.currentTimeMillis();
                            }
                        } catch (IOException | NullPointerException e) {
                            LOG.warn("Read failed: {}", e.getMessage());
                            _fireConnectionEvent("DISCONNECTED");
                            if(closeInitialized == false) {
                                internalClose();
                                closeInitialized = false;
                            }
                        }
                    }
                };
                connectionListener.setName("XBMC connection listener");
                connectionListener.start();
            }
            LOG.debug("Connection listener thread started: {}", connectionListener.isAlive());
        } else {
            throw new IOException("Connection not initialized");
        }
    }
    
    /**
     * Returns xbmc RPC data by doing a post request to xbmc web interface.
     * @param postString
     * @return
     * @throws IOException 
     */
    public final String getDataViaHttp(String postString) throws IOException {
        try {
            LOG.trace("Http POST request: {}", postString);
            URL url = new URL("http://"+this.ipAddress.getHostAddress()+":"+this.httpPort+"/jsonrpc");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", this.base64AuthString);
            connection.setRequestMethod("POST");
            connection.connect();

            byte[] outputBytes = postString.getBytes("UTF-8");
            try (OutputStream os = connection.getOutputStream()) {
                os.write(outputBytes);
            }
            String returnData = "";
            try (InputStream stream = connection.getInputStream()) {
                int data = stream.read();
                while(data != -1) {
                    returnData += (char) data;
                    data = stream.read();
                }
            }
            LOG.trace("Data from http request: {}", returnData);
            return returnData;
        } catch (MalformedURLException ex) {
            throw new IOException("Could not retreive by malformed url: " + ex.getMessage());
        }
    }
    
    /**
     * Stops the internal listener.
     */
    public final void stop(){
        if(connectionListener!=null){
            connectionListener.interrupt();
        }
    }
    
    /**
     * Adds a listener.
     * @param l 
     */
    public final void addListener(XbmcConnectionListener l){
        if(!_listeners.contains(l)) _listeners.add(l);
    }
    
    /**
     * Removes a listener.
     * @param l 
     */
    public final void removeListener(XbmcConnectionListener l){
        if(_listeners.contains(l)) _listeners.remove(l);
    }
    
    final void _fireDataEvent(String data){
        final XbmcEvent event = new XbmcEvent(new XbmcRPC6ConnectionData(this,data));
        final Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            Runnable handles = new Runnable(){
                @Override
                public final void run(){
                    ( (XbmcConnectionListener) listeners.next() ).handleXbmcEvent( event );
                }
            };
            handles.run();
        }
    }

    /**
     * Fires when a connection change is detected.
     * @param connectionData 
     */
    final void _fireConnectionEvent(final String connectionData){
        if(connectionData.equals("CONNECTED")){
            synchronized (this) {
                this.notifyAll();
            }
        }
        final Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            Runnable handles = () -> {
                ( (XbmcConnectionListener) listeners.next() ).handleConnectionEvent( connectionData );
            };
            handles.run();
        }
    }
    
}
