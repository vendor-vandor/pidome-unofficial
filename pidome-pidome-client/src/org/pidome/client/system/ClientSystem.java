/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.DomResourceException;
import org.pidome.client.system.domotics.Domotics;
import org.pidome.client.system.domotics.DomoticsException;
import org.pidome.client.system.network.Networking;
import org.pidome.client.system.network.NetworkingEvent;
import org.pidome.client.system.network.NetworkingEventListener;
import org.pidome.client.system.rpc.PidomeJSONRPC;
import org.pidome.client.system.rpc.PidomeJSONRPCException;
import org.pidome.client.system.scenes.dialogs.AwaitServerConnection;
import org.pidome.client.system.scenes.dialogs.ErrorMessage;

/**
 *
 * @author John Sirach
 */
public class ClientSystem extends ClientData implements NetworkingEventListener,ClientDataConnectionListener {

    Networking network = new Networking();
    
    Domotics dom = new Domotics();
    
    static Logger LOG = LogManager.getLogger(ClientSystem.class);
    
    static Map<String,Object> serverSysData = new HashMap<>();
    
    Thread reconnect = new Thread();
    
    boolean shutdown = false;
    
    boolean broadcastEnabled = true;
    
    public ClientSystem(){
        Networking.addEventListener(this);
        addClientLoggedInConnectionListener(this);
        addClientDataConnectionListener(this);
    }
    
    public final void start(){
        LOG.debug("Starting network");
        network.start();
    }

    public static Map<String,Object> getServerData(){
        return serverSysData;
    }
    
    public static boolean isHTTPSSL(){
        return serverSysData.containsKey("HTTPSSL") && (boolean)serverSysData.get("HTTPSSL") == true;
    }
    
    @Override
    public final void handleNetworkingEvent(NetworkingEvent event) {
        switch(event.getEventType()){
            case NetworkingEvent.BROADCASTRECEIVED:
                broadcastEnabled = true;
                initialize(parseInitBroadcast(event.getSource().getBroadcastMessage()));
            break;
            case NetworkingEvent.BROADCASTDISABLED:
                broadcastEnabled = false;
            break;
        }
    }

    void initialize(Map<String,Object> serverData){
        serverSysData = serverData;
        LOG.debug("Got server init data: {}", serverSysData);
        try {
            initializeClientDataConnection(serverSysData);
            startDataConnection();
        } catch (UnknownHostException ex) {
            LOG.error("Could not connect to server: {}", ex.getMessage());
        } catch (IOException ex) {
            LOG.error("Could not connect to server: {}", ex.getMessage());
        }
    }
    
    public void stopClient(boolean byProcess){
        shutdown = byProcess;
        LOG.debug("Stopping data connection");
        stopDataConnection();
    }

    void parseServerInitData(Map<String,Object> data){
        LOG.debug("Handling init data: {}", data);
        serverSysData.put("HTTPADDRESS", (String)data.get("httpaddress"));
        serverSysData.put("INITURL", (String)data.get("initurl"));
        serverSysData.put("JSONENTRY", (String)data.get("jsonurl"));
        if(data.containsKey("httpsport")){
            serverSysData.put("HTTPSSL", true);
            serverSysData.put("HTTPPORT", ((Long)data.get("httpsport")).intValue());
        } else {
            serverSysData.put("HTTPSSL", false);
            serverSysData.put("HTTPPORT", ((Long)data.get("httpport")).intValue());
        }
        LOG.debug("INITPARTS: {}", serverSysData);
    }
    
    @Override
    public void handleClientDataConnectionEvent(ClientDataConnectionEvent event) {
        switch(event.getEventType()){
            case ClientDataConnectionEvent.CONNECTED:
                if(!broadcastEnabled || !ClientData.internalServerData.isEmpty()){
                    startDataConnection();
                }
            break;
            case ClientDataConnectionEvent.LOGGEDIN:
                try {
                    serverStream.send(PidomeJSONRPC.createExecMethod("SystemService.getClientInitPaths", "SystemService.getClientInitPaths"));
                } catch (PidomeJSONRPCException ex) {
                    LOG.error("Could not create/send init");
                }
            break;
            case ClientDataConnectionEvent.INITRECEIVED:
                parseServerInitData((Map<String,Object>)event.getData());
                Thread initThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.currentThread().setName("SERVER:INIT");
                            dom.setHttpConnectorResources((String)serverSysData.get("HTTPADDRESS"), (int)serverSysData.get("HTTPPORT"), (String)serverSysData.get("JSONENTRY"), (int)serverSysData.get("HTTPPORT"), (String)serverSysData.get("INITURL"), (boolean)serverSysData.get("HTTPSSL"));
                            dom.initialize();
                        } catch (DomoticsException ex) {
                            LOG.error("Could not initialize: {}", ex.getMessage());
                            ErrorMessage.display("Server initialisation error", "Problem while initializing server data:\n" + ex.getMessage() +"\nRestart app or file a bug report.");
                        }
                    }
                };
                initThread.start();
                try {
                    initThread.join();
                    LOG.debug("SERVER:INIT Done");
                } catch (InterruptedException ex) {
                    LOG.error("Could not join data initialization thread.");
                }
            break;
            case ClientDataConnectionEvent.DEVRECEIVED:
                switch(event.getMethod()){
                    case "editDevice":
                    case "addDevice":
                        Map<String,Object> params = new HashMap<>();
                        params.put("id", ((Map<String,Object>)event.getData()).get("id"));
                        try {
                            dom.updateDeviceFromJSON(dom.getJSONData("DeviceService.getDevice",params),(event.getMethod().equals("editDevice"))?"UPDATEDEVICE":"ADDEDDEVICE");
                        } catch (DomResourceException | DomComponentsException ex) {
                            LOG.error("Could not update/add device: {}", ex.getMessage());
                        }
                    break;
                    case "deleteDevice":
                        dom.removeDevice(((Long)((Map<String,Object>)event.getData()).get("id")).intValue());
                    break;
                }
            break;
            case ClientDataConnectionEvent.PLUGINRECEIVED:
                switch(event.getMethod()){
                    case "addPlugin":
                    case "updatePlugin":
                        Map<String,String> post = new HashMap<>();
                        post.put("id", String.valueOf((Long)((Map<String,Object>)event.getData()).get("id")));
                        try {
                            dom.updateComponentsFromXml(dom.getRawXml("/screen/api/plugin.xml",post),(event.getMethod().equals("addPlugin"))?"ADDPLUGIN":"UPDATEPLUGIN");
                        } catch (DomResourceException | DomComponentsException ex) {
                            LOG.error("Could not update/add plugin: {}", ex.getMessage());
                        }
                    break;
                    case "deletePlugin":
                        dom.removePlugin(((Long)((Map<String,Object>)event.getData()).get("id")).intValue());
                    break;
                }
            break;
            case ClientDataConnectionEvent.LOCRECEIVED:
                switch(event.getMethod()){
                    case "addLocation":
                    case "editLocation":
                        try {
                            dom.updateComponentsFromXml(dom.getRawXml("/screen/api/locations.xml",null),(event.getMethod().equals("editLocation"))?"UPDATELOCATION":"ADDEDLOCATION");
                        } catch (DomResourceException | DomComponentsException ex) {
                            LOG.error("Could not update/add device: {}", ex.getMessage());
                        }
                    break;
                }
            break;
            case ClientDataConnectionEvent.CATRECEIVED:
                switch(event.getMethod()){
                    case "addSubCategory":
                    case "editSubcategory":
                        try {
                            dom.updateComponentsFromXml(dom.getRawXml("/screen/api/categories.xml",null),(event.getMethod().equals("editSubcategory"))?"UPDATECATEGORY":"ADDEDCATEGORY");
                        } catch (DomResourceException | DomComponentsException ex) {
                            LOG.error("Could not update/add device: {}", ex.getMessage());
                        }
                    break;
                }
            break;
            case ClientDataConnectionEvent.DISCONNECTED:
                if(shutdown==false && broadcastEnabled==true && !reconnect.isAlive()){
                    reconnect = new Thread(){
                        @Override
                        public void run(){
                            LOG.debug("Received disconnect");
                            stopClient(false);
                            awaitServerBrdcst();
                        }
                    };
                    reconnect.start();
                }
            break;
        }
    }
    
    /**
     * This should not be here.
     */
    final void awaitServerBrdcst(){
        Platform.runLater(() -> {
            LOG.debug("Attempting relistening for broadcasts");
            AwaitServerConnection.display("Connection problem", "The server connection seems to be down. Waiting for server to come back");
        });
        network.startBroadcastListener();
    }
    
}
