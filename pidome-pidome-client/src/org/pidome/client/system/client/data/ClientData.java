/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.client.data;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.AppProperties;
import org.pidome.client.config.AppPropertiesException;
import org.pidome.client.services.server.ServerStream;
import org.pidome.client.services.server.ServerStreamEvent;
import org.pidome.client.services.server.ServerStreamListener;
import org.pidome.client.system.parsers.Parser;
import org.pidome.client.system.parsers.ServerProtocolParser;
import org.pidome.client.system.parsers.ParseException;

/**
 *
 * @author John Sirach
 */
public class ClientData extends Parser implements ServerStreamListener {

    protected static ServerStream serverStream;
    
    HashMap<String,Object> sessionData = new HashMap<>();
    
    static ClientSession session = new ClientSession();
    
    static Logger LOG = LogManager.getLogger(ClientData.class);
    
    static List _loggedinListeners = new ArrayList();
    static List _dataConnectionListeners = new ArrayList();
    
    Map<String, String> TelnetServer = new HashMap<>();
    Map<String, String> HTTPServer = new HashMap<>();
    
    public final static Map<String,Object> internalServerData = new HashMap<>();
    
    private String mainResourceId;
    
    static ClientData me;
    
    public ClientData(){
        ServerStream.addEventListener(this);
        me = this;
    }
    
    public static ClientData getInstance(){
        return me;
    }
    
    public final void initializeClientDataConnection(Map<String,Object> serverData) throws UnknownHostException, IOException {
        mainResourceId = (String)serverData.get("TELNETADDRESS");
        serverStream = new ServerStream((String)serverData.get("TELNETADDRESS"), (int)serverData.get("TELNETPORT"), (boolean)serverData.get("STREAMSSL"));
        try {
            AppProperties.setProperty("system", "TELNETADDRESS", (String)serverData.get("TELNETADDRESS"));
            AppProperties.setProperty("system", "TELNETPORT", String.valueOf((int)serverData.get("TELNETPORT")));
            AppProperties.setProperty("system", "STREAMSSL", String.valueOf((boolean)serverData.get("STREAMSSL")));
            AppProperties.store("system", "Server data save");
        } catch (Exception ex){
            LOG.error("Could not save server data: {}", ex.getMessage());
        }
        _fireDataLoggedInConnectionEvent(ClientDataConnectionEvent.CONNECTED, null, 0, null);
    }
    
    public static String getLoginName(){
        return ClientSession.getClientName();
    }
    
    public final void startDataConnection(){
        serverStream.start();
        try {
            if(AppProperties.getProperty("system", "client.firstrun").equals("false")){
                serverStream.send(session.getAuthString());
            } else {
                _fireDataLoggedInConnectionEvent(ClientDataConnectionEvent.LOGINFAILURE,session.getSessionName(),0, "firstrun");
            }
        } catch (ClientSessionException ex) {
            LOG.error("Could not send initial login: {}", ex.getMessage());
            _fireDataLoggedInConnectionEvent(ClientDataConnectionEvent.LOGINFAILURE,session.getSessionName(),500, ex.getMessage());
        } catch (AppPropertiesException ex) {
            _fireDataLoggedInConnectionEvent(ClientDataConnectionEvent.LOGINFAILURE,session.getSessionName(),0, "firstrun");
        }
    }
    
    public final void stopDataConnection(){
        LOG.debug("Stopping server stream");
        if(serverStream!=null){
            serverStream.stop();
        }
    }
    
    @Override
    public void handleStreamEvent(ServerStreamEvent event) {
        switch(event.getEventType()){
            case ServerStreamEvent.DATARECEIVED:
                try {
                    ServerProtocolParser client = parseServerStreamData((String)event.getData());
                    try {
                        switch ((String) client.getId()) {
                            case "ClientService.signOn":
                                session.setData((Map<String,Object>)client.getResult().get("data"));
                                if (session.loginError()) {
                                    _fireDataLoggedInConnectionEvent(ClientDataConnectionEvent.LOGINFAILURE, session.getSessionName(), session.getLoginError(), session.getErrorMessage());
                                } else {
                                    _fireDataLoggedInConnectionEvent(ClientDataConnectionEvent.LOGGEDIN, session.getSessionName(), 200, "");
                                }
                            break;
                            case "SystemService.getClientInitPaths":
                                ((Map<String,Object>)client.getResult().get("data")).put("httpaddress", mainResourceId);
                                _fireDataConnectionEvent(ClientDataConnectionEvent.INITRECEIVED, (Map<String,Object>)client.getResult().get("data"));
                            break;
                        }
                    } catch (NullPointerException ex){
                        try {
                            LOG.debug("Going to method handling");
                            Map<String,Object> params = client.getParameters();
                            switch(client.getNameSpace()){
                                case ServerProtocolParser.CLIENT:
                                    switch(client.getMethod()){
                                        case "approveClient":
                                            if((boolean)params.get("aproved")){
                                                _fireDataLoggedInConnectionEvent(ClientDataConnectionEvent.LOGGEDIN, session.getSessionName(), 200, "");
                                            } else {
                                                _fireDataLoggedInConnectionEvent(ClientDataConnectionEvent.LOGINFAILURE, session.getSessionName(), 401, "Approval denied");
                                            }
                                        break;
                                        default:
                                            _fireDataConnectionEvent(ClientDataConnectionEvent.CLIENTRECEIVED, client.getMethod(), params);
                                        break;
                                    }
                                break;
                                case ServerProtocolParser.MACRO:
                                    _fireDataConnectionEvent(ClientDataConnectionEvent.MCRRECEIVED, client.getMethod(), params);
                                break;
                                case ServerProtocolParser.DEVICE:
                                    _fireDataConnectionEvent(ClientDataConnectionEvent.DEVRECEIVED, client.getMethod(), params);
                                break;
                                case ServerProtocolParser.CAT:
                                    _fireDataConnectionEvent(ClientDataConnectionEvent.CATRECEIVED, client.getMethod(), params);
                                break;
                                case ServerProtocolParser.LOC:
                                    _fireDataConnectionEvent(ClientDataConnectionEvent.LOCRECEIVED, client.getMethod(), params);
                                break;
                                case ServerProtocolParser.SYSTEM:
                                    _fireDataConnectionEvent(ClientDataConnectionEvent.SYSRECEIVED, client.getMethod(), params);
                                break;
                                case ServerProtocolParser.MEDIA:
                                    _fireDataConnectionEvent(ClientDataConnectionEvent.MEDIARECEIVED, client.getMethod(), params);
                                break;
                                case ServerProtocolParser.PLUGIN:
                                    _fireDataConnectionEvent(ClientDataConnectionEvent.PLUGINRECEIVED, client.getMethod(), params);
                                break;
                                case ServerProtocolParser.DAYPART:
                                    _fireDataConnectionEvent(ClientDataConnectionEvent.DAYPARTRECEIVED, client.getMethod(), params);
                                break;
                                case ServerProtocolParser.PRESENCE:
                                    _fireDataConnectionEvent(ClientDataConnectionEvent.USERPRESENCERECEIVED, client.getMethod(), params);
                                break;
                                case ServerProtocolParser.USERSTATUS:
                                    _fireDataConnectionEvent(ClientDataConnectionEvent.USERSTATUSRECEIVED, client.getMethod(), params);
                                break;
                                case ServerProtocolParser.UTILITYMEASURE:
                                    _fireDataConnectionEvent(ClientDataConnectionEvent.UTILITYMEASURERECEIVED, client.getMethod(), params);
                                break;
                                case ServerProtocolParser.NOTIFICATION:
                                    _fireDataConnectionEvent(ClientDataConnectionEvent.NOTIFICATIONRECEIVED, client.getMethod(), params);
                                break;
                                case ServerProtocolParser.msg_OK:
                                    switch(client.getNameSpace()){
                                        case ServerProtocolParser.MACRO:
                                            _fireDataConnectionEvent(ClientDataConnectionEvent.MCRRECEIVED, client.getMethod(), params);
                                        break;
                                    }
                                break;
                            }
                        } catch (Exception exception){
                            LOG.error("Error in handling request: {}", exception.getMessage(), exception);
                        }
                    }
                } catch (ParseException ex){
                    LOG.error("Could not parse/handle server data: {}", ex.getMessage());
                    _fireDataLoggedInConnectionEvent(ClientDataConnectionEvent.LOGINFAILURE, session.getSessionName(), 500, ex.getMessage());
                }
            break;
            case ServerStreamEvent.CONNECTIONLOST:
                session.logOut();
                _fireDataConnectionEvent(ClientDataConnectionEvent.DISCONNECTED);
            break;
        }
    }
    
    public static void sendData(String data){
        serverStream.send(data);
    }
    
    public static void login(String clientName, String password) throws ClientSessionException {
        session.setClientName(clientName);
        sendData(session.getAuthString(clientName, password));
    }
    
    public static void goCustomConnect(String serverAddress, int serverPort, boolean isSSL){
        if(!serverAddress.equals("") && serverPort!=0){
            internalServerData.put("TELNETADDRESS", serverAddress);
            internalServerData.put("TELNETPORT", serverPort);
            internalServerData.put("STREAMSSL", isSSL);
            try {
                me.initializeClientDataConnection(internalServerData);
            } catch (IOException ex) {
                LOG.error("Could not connect: {}", ex.getMessage());
                getInstance()._fireDataConnectionEvent(ClientDataConnectionEvent.CONNECTERROR, ex.getMessage());
            }
        } else {
            getInstance()._fireDataConnectionEvent(ClientDataConnectionEvent.CONNECTERROR, "Incorrect server data");
        }
    }
    
    /// Listeners for plain data
    public static synchronized void addClientDataConnectionListener(ClientDataConnectionListener l) {
        LOG.debug("Added data listener: {}", l.getClass().getName());
        _dataConnectionListeners.add(l);
    }

    public static synchronized void removeClientDataConnectionListener(ClientDataConnectionListener l) {
        LOG.debug("Removed data listener: {}", l.getClass().getName());
        _dataConnectionListeners.remove(l);
    }

    synchronized void _fireDataConnectionEvent(String EventType){
        _fireDataConnectionEvent(EventType, null);
    }
    
    synchronized void _fireDataConnectionEvent(String EventType, Object data){
        LOG.debug("New event: {}", EventType);
        ClientDataConnectionEvent event = new ClientDataConnectionEvent(EventType);
        event.setData(data);
        Iterator listeners = _dataConnectionListeners.iterator();
        while (listeners.hasNext()) {
            ((ClientDataConnectionListener) listeners.next()).handleClientDataConnectionEvent(event);
        }
    }
    
    synchronized void _fireDataConnectionEvent(String EventType, String method, Object data){
        LOG.debug("New event: {} with method: {}", EventType, method);
        ClientDataConnectionEvent event = new ClientDataConnectionEvent(EventType);
        event.setData(data);
        event.setMethod(method);
        Iterator listeners = _dataConnectionListeners.iterator();
        while (listeners.hasNext()) {
            ((ClientDataConnectionListener) listeners.next()).handleClientDataConnectionEvent(event);
        }
    }
    
    //// Listeners for connection and login listeners
    public static synchronized void addClientLoggedInConnectionListener(ClientDataConnectionListener l) {
        LOG.debug("Added logged in listener: {}", l.getClass().getName());
        _loggedinListeners.add(l);
    }

    public static synchronized void removeClientLoggedInConnectionListener(ClientDataConnectionListener l) {
        LOG.debug("Removed logged in listener: {}", l.getClass().getName());
        _loggedinListeners.remove(l);
    }
    
    synchronized void _fireDataLoggedInConnectionEvent(String EventType, String clientName, int errorCode, String message){
        LOG.debug("New event: {}", EventType);
        ClientDataConnectionEvent event = new ClientDataConnectionEvent(EventType);
        event.setClientData(clientName, errorCode, message);
        Iterator listeners = _loggedinListeners.iterator();
        while (listeners.hasNext()) {
            ((ClientDataConnectionListener) listeners.next()).handleClientDataConnectionEvent(event);
        }
    }
    
}
