/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.plugins.devices.uniPi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import org.pidome.plugins.devices.uniPi.wsConnection.WSocketEvent;
import org.pidome.plugins.devices.uniPi.wsConnection.WSocket;
import org.pidome.plugins.devices.uniPi.wsConnection.WSocketEventListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.logging.log4j.LogManager;
import org.java_websocket.util.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroupException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfiguration;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.interfaces.web.configuration.WebOption;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.emulators.DevicePlugin;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCUtils;

/**
 *
 * @author John
 */
public final class UniPi extends DevicePlugin implements WSocketEventListener {

    private Map<String, String> configuration;
    
    private WSocket socket;
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(UniPi.class);
    
    private boolean intendedDisconnect = false;
    
    Timer reconnectTimer;
    
    public UniPi(){
        WebConfiguration conf = new WebConfiguration();
        
        WebConfigurationOptionSet optionSet = new WebConfigurationOptionSet("UniPi connection options");
        optionSet.setConfigurationSetDescription("This plugin connects to an UniPi device using websockets. This make this plugin very versatile making it possible "
                + "to be added multiple times and connect to both local as wel as remote Raspberry Pi's with the UniPi connected<br/><br/>"
                + "Make sure you have followed the intructions at https://github.com/UniPiTechnology/evok and have a working installation before the plugin is added.");
        
        WebOption ipAddress = new WebOption("IPADDRESS", "Ip address", "The ip address of the websocket interface of the UniPi (evok)", WebOption.WebOptionConfigurationFieldType.IP_ADDRESS);
        optionSet.addOption(ipAddress);
        
        WebOption port = new WebOption("PORT", "Websocket port", "The port of the websocket interface of the UniPi (evok)", WebOption.WebOptionConfigurationFieldType.INT);
        optionSet.addOption(port);

        conf.addOptionSet(optionSet);
        
        
        WebConfigurationOptionSet userSet = new WebConfigurationOptionSet("UniPi connection options");
        userSet.setConfigurationSetDescription("When an username and password is used to connect to the UniPi (evok) enter them below. If they are not needed leave them both empty.");
        WebOption username = new WebOption("USERNAME", "Username/Secret", "The username/secret needed to connect", WebOption.WebOptionConfigurationFieldType.STRING);
        userSet.addOption(username);
        WebOption password = new WebOption("PASSWORD", "Password", "The password needed to connect", WebOption.WebOptionConfigurationFieldType.PASSWORD);
        userSet.addOption(password);
        conf.addOptionSet(userSet);
        
        setConfiguration(conf);
        
    }
    
    @Override
    public void setConfigurationValues(Map<String, String> map) throws WebConfigurationException {
        this.configuration = map;
    }

    @Override
    public void startPlugin() throws PluginException {
        connect();
    }

    /**
     * Preloads known devices data.
     * This is a best effort functionality.
     */
    private void preloadData(){
        if(this.configuration.containsKey("IPADDRESS") && this.configuration.containsKey("PORT")){
            try {
                String location = new StringBuilder("http://").append(this.configuration.get("IPADDRESS")).append(":").append(this.configuration.get("PORT")).append("/rest/all/").toString();
                URL obj = new URL(location);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                if((this.configuration.containsKey("USERNAME") && this.configuration.containsKey("PASSWORD")) && 
                   (!this.configuration.get("USERNAME").isEmpty() && !this.configuration.get("PASSWORD").isEmpty())){
                    String auth = Base64.encodeBytes(new StringBuilder(this.configuration.get("USERNAME")).append(":").append(this.configuration.get("PASSWORD")).toString().getBytes());
                    con.setRequestProperty("Authorization", "Basic " + auth);
                }
                con.setRequestMethod("GET");
                int responseCode = con.getResponseCode();
                if(responseCode==200){
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        processPreloadData(PidomeJSONRPCUtils.jsonParamsToObjectHashMapList((JSONArray)new JSONParser().parse(response.toString())));
                    } catch (ParseException ex) {
                        LOG.warn("Unable to parse preload data: {}", ex.getMessage());
                    }
                }
            } catch (MalformedURLException ex) {
                LOG.error("Invalid url to preload data: {}", ex.getMessage());
            } catch (IOException ex) {
                LOG.error("Could not get preload data: {}", ex.getMessage());
            }
        }
    }
    
    /**
     * Connects to the remote UniPi service.
     * @throws PluginException 
     */
    private void connect() throws PluginException {
        try {
            if(socket==null && this.configuration.containsKey("IPADDRESS") && this.configuration.containsKey("PORT")){
                preloadData();
                WSocket.addSocketListener(this);
                String location = new StringBuilder("ws://").append(this.configuration.get("IPADDRESS")).append(":").append(this.configuration.get("PORT")).append("/ws").toString();
                LOG.info("Connecting to UniPi instance at: {}", location);
                URI connectUri = new URI(location);
                socket = new WSocket(connectUri);
                socket.connect();
            } else {
                if(socket.isConnected() || socket.isConnected() || socket.isOpen()){
                    socket.close();
                }
            }
        } catch (URISyntaxException ex) {
            LOG.error("Could not make a connection to UniPi instance: {}",ex.getMessage(),ex);
            throw new PluginException(ex.getMessage());
        }
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public void stopPlugin() {
        intendedDisconnect = true;
        stopReconnectTimer();
        if(this.socket!=null){
            if(!this.socket.isClosing() || this.socket.isOpen()){
                this.socket.close();
                WSocket.removeSocketListener(this);
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getExpectedDriverId() {
        return "NATIVE_PIDOMEUNIPI_DRIVER";
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getExpectedDriverVersion() {
        return "0.0.1";
    }

    /**
     * @inheritDoc
     */
    @Override
    public void handleDeviceData(Device device, String string, String string1, byte[] bytes, boolean bln) {
        ////
    }

    private void processPreloadData(List<Map<String,Object>> preloadData){
        this.getSoftwareDriverLink().getRunningDevices().stream().forEach((device) -> {
            device.getFullCommandSet().getControlsGroups().values().stream().forEach((group) -> {
                group.getGroupControls().values().stream().forEach((control) -> {
                    for(Map<String,Object> data:preloadData){
                        if(group.getGroupId().equals(data.get("dev")) && control.getControlId().equals(data.get("circuit")) && data.get("value")!=null){
                            switch(group.getGroupId()){
                                case "relay":
                                    device.passToDevice("relay", new UniPiRelayCommand((String)data.get("circuit"), ((Number)data.get("value")).intValue()));
                                break;
                                case "ai":
                                    device.passToDevice("ai", new UniPiAnalogInputCommand((String)data.get("circuit"), ((Number)data.get("value")).floatValue()));
                                break;
                                case "temp":
                                case "1wdevice":
                                    device.passToDevice(group.getGroupId(), new UniPiTemperatureCommand((String)data.get("circuit"), ((Number)data.get("value")).floatValue()));
                                break;
                                case "ao":
                                    device.passToDevice("ao", new UniPiAnalogOutputCommand((String)data.get("circuit"), ((Number)data.get("value")).floatValue()));
                                break;
                                case "input":
                                    device.passToDevice("input", new UniPiDigitalInputCommand((String)data.get("circuit"), ((Number)data.get("value")).intValue()==1));
                                break;
                            }
                        }
                    }
                });
            });
        });
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public void handleDeviceData(Device device, DeviceCommandRequest dcr) {
        if(this.socket != null && this.socket.isConnected()){
            switch(dcr.getGroupId()){
                case "relay":
                    this.socket.send("{\"cmd\":\"set\",\"dev\":\"relay\",\"circuit\":\""+dcr.getControlId()+"\",\"value\":"+dcr.getCommandValue()+"}");
                break;
                case "ao":
                    this.socket.send("{\"cmd\":\"set\",\"dev\":\"ao\",\"circuit\":\""+dcr.getControlId()+"\",\"value\":"+dcr.getCommandValue()+"}");
                break;
            }
        }
    }

    /**
     * Handles the websocket data.
     * Encapsulates the data in the correct data type container for the receiving device.
     * @param data 
     */
    private void handleWSocketData(String data){
        try {
            Map<String,Object> uniData = PidomeJSONRPCUtils.jsonParamsToObjectHashMap((JSONObject)new JSONParser().parse(data));
            if(uniData.containsKey("dev")){
                switch((String)uniData.get("dev")){
                    case "relay":
                        this.getSoftwareDriverLink().getRunningDevices().stream().forEach((device) -> {
                            try {
                                device.getFullCommandSet().getControlsGroup("relay").getDeviceControl((String)uniData.get("circuit"));
                                device.passToDevice("relay", new UniPiRelayCommand((String)uniData.get("circuit"), ((Number)uniData.get("value")).intValue()));
                            } catch (DeviceControlException | DeviceControlsGroupException ex) {
                                /// This device does not have this item
                            }
                        });
                    break;
                    case "ai":
                        this.getSoftwareDriverLink().getRunningDevices().stream().forEach((device) -> {
                            try {
                                device.getFullCommandSet().getControlsGroup("ai").getDeviceControl((String)uniData.get("circuit"));
                                device.passToDevice("ai", new UniPiAnalogInputCommand((String)uniData.get("circuit"), ((Number)uniData.get("value")).floatValue()));
                            } catch (DeviceControlException | DeviceControlsGroupException ex) {
                                /// This device does not have this item
                            }
                        });
                    break;
                    case "temp":
                    case "1wdevice":
                        this.getSoftwareDriverLink().getRunningDevices().stream().forEach((device) -> {
                            try {
                                device.getFullCommandSet().getControlsGroup((String)uniData.get("dev")).getDeviceControl((String)uniData.get("circuit"));
                                device.passToDevice("temp", new UniPiTemperatureCommand((String)uniData.get("circuit"), ((Number)uniData.get("value")).floatValue()));
                            } catch (DeviceControlException | DeviceControlsGroupException ex) {
                                /// This device does not have this item
                            }
                        });
                    break;
                    case "ao":
                        this.getSoftwareDriverLink().getRunningDevices().stream().forEach((device) -> {
                            try {
                                device.getFullCommandSet().getControlsGroup("ao").getDeviceControl((String)uniData.get("circuit"));
                                device.passToDevice("ao", new UniPiAnalogOutputCommand((String)uniData.get("circuit"), ((Number)uniData.get("value")).floatValue()));
                            } catch (DeviceControlException | DeviceControlsGroupException ex) {
                                /// This device does not have this item
                            }
                        });
                    break;
                    case "input":
                        this.getSoftwareDriverLink().getRunningDevices().stream().forEach((device) -> {
                            try {
                                device.getFullCommandSet().getControlsGroup("input").getDeviceControl((String)uniData.get("circuit"));
                                device.passToDevice("input", new UniPiDigitalInputCommand((String)uniData.get("circuit"), ((Number)uniData.get("value")).intValue()==1));
                            } catch (DeviceControlException | DeviceControlsGroupException ex) {
                                /// This device does not have this item
                            }
                        });
                    break;
                    default:
                        throw new Exception("Unhandled dev type: " + (String)uniData.get("dev"));
                }
            }
        } catch (ParseException ex) {
            LOG.error("Received data could not be parsed: {}", data);
        } catch (Exception ex){
            LOG.error("Unhandled exception in driver: {}", ex.getMessage(), ex);
        }
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public void prepareWebPresentation() {
        ////
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean hasGraphData() {
        return false;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void prepareDelete() {
        ///
    }

    /**
     * @inheritDoc
     */
    @Override
    public void deviceRemoved(Device device) {
        ////
    }

    /**
     * @inheritDoc
     */
    @Override
    public void deviceAdded(Device device) {
        ////
    }

    /**
     * Starts a reconnect timer.
     */
    private void startReconnectTimer(){
        stopReconnectTimer();
        if(this.configuration.containsKey("IPADDRESS")){
            TimerTask reconnectTask = new ReconnectTimerTask();
            reconnectTimer = new Timer();
            reconnectTimer.schedule(reconnectTask, 5000, 10000);
        } else {
            LOG.warn("Will not try to reconnect without knowing an ip address to connect to.");
        }
    }
    
    /**
     * Stops a runnign reconnect timer.
     */
    private void stopReconnectTimer(){
        if(reconnectTimer != null){
            reconnectTimer.cancel();
            reconnectTimer.purge();
        }
    }
    
    /**
     * Reconnect timer task.
     */
    public class ReconnectTimerTask extends TimerTask {
        /**
         * Runs the timer task.
         */
        @Override
        public void run() {
            try {
                LOG.info("Trying to reconnect to: {}", configuration.get("IPADDRESS"));
                connect();
            } catch (PluginException ex) {
                LOG.warn("Unable to reconnect: {}", ex.getMessage());
            }
        }
    }
    
    /**
     * Handles a WSocket event.
     * This handles connected, disconnected and data received.
     * @param event 
     */
    @Override
    public void handleWSocketEvent(WSocketEvent event) {
        switch(event.getEventType()){
            case CONNECTIONLOST:
                LOG.info("Connection closed from: {}", socket.getURI());
                this.socket = null;
                this.setRunning(false);
                if(intendedDisconnect == false){
                    startReconnectTimer();
                }
            break;
            case CONNECTIONAVAILABLE:
                stopReconnectTimer();
                LOG.info("Connection opened with: {}", socket.getURI());
                this.setRunning(true);
            break;
            case DATARECEIVED:
                this.handleWSocketData(event.getSource().getData());
            break;
        }
    }
    
}
