/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.plugins.devices.philipsHue;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.PluginDeviceMutationException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceColorPickerControlColorData;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroupException;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryScanInterface;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryServiceException;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDevice;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredItemsCollection;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralDriverDeviceMutationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfiguration;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.interfaces.web.configuration.WebOption;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentAddExistingDeviceRequest;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.emulators.DevicePlugin;
import org.pidome.server.connector.tools.MathImpl;
import org.pidome.server.connector.tools.http.JSONConnector;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;

/**
 *
 * @author John
 */
public class PhilipsHue extends DevicePlugin implements DeviceDiscoveryScanInterface {

    Map<String, String> configuration;
    static Logger LOG = LogManager.getLogger(PhilipsHue.class);

    private final ScheduledExecutorService lightActionExecutor = Executors.newSingleThreadScheduledExecutor();
    
    boolean runningInternal = false;

    String username = "";
    int port = 80;
    String ip = "";

    String workUrlNonUser = "";
    String workUrl = "";

    boolean registered        = false;
    boolean connected         = false;
    boolean deviceSyncRunning = false;

    boolean driverIsManager   = false;
    
    int lightsCheckTimeout = 10;

    PHHueSDK phHueSDK;
    
    Map<String,Integer> deviceDelays = new HashMap<>();
    
    /**
     * Constructor.
     * Sets the configuration.
     */
    public PhilipsHue() {
        WebConfiguration conf = new WebConfiguration();
        WebConfigurationOptionSet optionSet = new WebConfigurationOptionSet("Philips Hue connect options");
        optionSet.setConfigurationSetDescription("Currently only one bridge is supported. When adding the plugin for the first time you need to press "
                + "the button on the bridge and within 30 seconds press save. The plugin will try to register itself "
                + "on the bridge. If it fails try this procedure again. You need a minimum of 10 characters to succeed without spaces and must be something "
                + "not easy to guess.");
        WebOption ipAddress = new WebOption("IPADDRESS", "Ip address", "Philips Hue bridge ip address", WebOption.WebOptionConfigurationFieldType.IP_ADDRESS);
        if (runningInternal == false) {
            try {
                JSONConnector getDefault = new JSONConnector("https://www.meethue.com/api/nupnp", true);
                List<Map<String, String>> jsonData = (List<Map<String, String>>) getDefault.getJSON(null, null, null).getArrayData();
                if (!jsonData.isEmpty() && jsonData.get(0).get("internalipaddress") != null && !jsonData.get(0).get("internalipaddress").equals("")) {
                    ipAddress.setDefaultValue(jsonData.get(0).get("internalipaddress"));
                }
            } catch (Exception ex) {
                LOG.warn("Could not discover: {}", ex.getMessage(), ex);
            }
        }
        optionSet.addOption(ipAddress);
        WebOption usernameOption = new WebOption("USERNAME", "Username", "Username used (Minimum of 10 characters, otherwise it will fail)", WebOption.WebOptionConfigurationFieldType.STRING);
        usernameOption.setDefaultValue(UUID.randomUUID().toString().substring(0, 16));
        optionSet.addOption(usernameOption);
        conf.addOptionSet(optionSet);

        WebConfigurationOptionSet pluginoptions = new WebConfigurationOptionSet("Plugin options");
        pluginoptions.setConfigurationSetDescription("Below are two options, depending on the first option the second one is applicable. When you let the driver manage the devices this driver will "
                + "automatically create or delete devices depending on what the philips hue bridge reports back to the server.<br/><br/>"
                + "The option Light check interval is an interval in seconds in which the plugins asks the bridge what the status of the lights/fixtures are. This is a default of 10 seconds. "
                + "The reason for this is that the hue bridge does not tell the server what the status is, but the server needs to ask for it. The server does this "
                + "just in case you change the light with an other application and the server needs to be updated for this. This means for every light a request is "
                + "made to be updated. If you have enabled the option Driver manages devices with every request there will be a check done if a device exists or is removed and will handle this "
                + "automatically for you.");

        WebOption driverManages = new WebOption("DRIVERISMANAGER", "Driver manages devices", "You can let the driver manage the device, or do this yourself using auto discovery", WebOption.WebOptionConfigurationFieldType.BOOLEAN);
        driverManages.setDefaultValue("false");
        pluginoptions.addOption(driverManages);
        
        WebOption timeout = new WebOption("CHECKTIMEOUT", "Light check interval", "The default is 10 seconds", WebOption.WebOptionConfigurationFieldType.INT);
        timeout.setDefaultValue("10");
        pluginoptions.addOption(timeout);

        conf.addOptionSet(pluginoptions);
        setConfiguration(conf);
        runningInternal = true;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setConfigurationValues(Map<String, String> configuration) throws WebConfigurationException {
        this.configuration = configuration;
        this.username = configuration.get("USERNAME");
        if (configuration.get("CHECKTIMEOUT") != null && !configuration.get("CHECKTIMEOUT").equals("")) {
            this.lightsCheckTimeout = Integer.parseInt(configuration.get("CHECKTIMEOUT"));
        }
        this.ip = configuration.get("IPADDRESS");
        workUrlNonUser = "http://" + this.ip + ":" + this.port + "/api";
        workUrl = workUrlNonUser + "/" + this.username;
        this.driverIsManager = configuration.get("DRIVERISMANAGER").toLowerCase().equals("true");
        try {
            registerPiDomeOnHue();
            setRunning(runningInternal);
        } catch (PhilipsHueRegisterException ex1) {
            LOG.error("Could not register: {}", ex1.getMessage());
        }
    }

    /**
     * Register PiDome on the Hue.
     *
     * @throws PhilipsHueRegisterException
     */
    private void registerPiDomeOnHue() throws PhilipsHueRegisterException {
        try {
            if (this.port != 0 && !this.username.equals("") && !this.ip.equals("")) {
                LOG.debug("Check if we need to register {} on : {}", this.username, this.ip);
                JSONConnector getifRegister = new JSONConnector(createUrl("lights"));
                getifRegister.setRequestTimeout(3000);
                getifRegister.setHTTPMethod("GET");
                try {
                    List<Map<String, Map<String, String>>> jsonRegData = (List<Map<String, Map<String, String>>>) getifRegister.getJSON(null, null, null).getArrayData();
                    if (jsonRegData.get(0).get("error") != null) {
                        JSONConnector getRegister = new JSONConnector(workUrlNonUser);
                        LOG.debug("Trying to register with: {}", this.username);
                        Map<String, Object> registerData = new HashMap<>();
                        registerData.put("devicetype", "PiDome - Home Automation");
                        registerData.put("username", this.username);
                        getRegister.setRequestTimeout(3000);
                        getRegister.setHTTPMethod("POST");
                        List<Map<String, Map<String, String>>> jsonData = (List<Map<String, Map<String, String>>>) getRegister.postJSON(null, registerData, null).getArrayData();
                        if (jsonData.get(0).get("error") != null) {
                            throw new PhilipsHueRegisterException("Register error: " + jsonData.get(0).get("error").get("description"));
                        } else {
                            this.registered = true;
                        }
                    } else {
                        this.registered = true;
                    }
                } catch (ClassCastException ex) {
                    //// The first i encounter an incorrect correctness.
                    //// When the hue bridge returns an object, it gives us bridge information.
                    //// When the hue gives us an array, it gives us an error. So we expect an array with an error.
                    //// and when we get an object we are registered.
                    this.registered = true;
                }
            } else {
                LOG.error("Can not register: Invalid plugin configuration: {}", this.configuration);
                throw new PhilipsHueRegisterException("Can not register: Invalid plugin configuration, refer to log file");
            }
        } catch (MalformedURLException | PidomeJSONRPCException ex) {
            throw new PhilipsHueRegisterException("Can not register: " + ex.getMessage());
        }
    }

    /**
     * Handles the device command.
     * @param device
     * @param group
     * @param set
     * @param data 
     */
    private void handleDeviceCommand(final Device device, final DeviceCommandRequest dcr) {
        Runnable exec = () -> {
            LOG.debug("Data from device group:{}, control: {}, action: {}, extra: {}", dcr.getGroupId(), dcr.getControlId(), dcr.getCommandValue(), dcr.getExtraValue());
            PHBridge bridge = phHueSDK.getSelectedBridge();
            for(PHLight light:bridge.getResourceCache().getAllLights()){
                LOG.trace("Lights walk - Address light: {}, address device: {}, equals: {}", light.getIdentifier(), device.getAddress(), (light.getIdentifier().equals(device.getAddress())));
                if(light.getIdentifier().equals(device.getAddress())){
                    PHLightState lightState = new PHLightState();
                    boolean send = false;
                    try {
                        lightState.setTransitionTime(deviceDelays.get(device.getAddress()));
                    } catch (Exception ex){
                        ///// no transition time known yet
                    }
                    switch (dcr.getControlId()) {
                        case "onoff":
                            lightState.setOn((boolean)dcr.getCommandValue());
                            send = true;
                            break;
                        case "setcolor":
                            try {
                                DeviceColorPickerControlColorData colorData = new DeviceColorPickerControlColorData(dcr.getCommandValue());
                                Map<String,Double> colors = colorData.getHSB();
                                LOG.debug("Sending the following colors: {}", colors);
                                lightState.setBrightness((int)Math.round(MathImpl.map(colors.get("b"), 0.0, 1.0, 0.0, 254.0)));
                                lightState.setSaturation((int)Math.round(MathImpl.map(colors.get("s"), 0.0, 1.0, 0.0, 254.0)));
                                lightState.setHue((int)Math.round(MathImpl.map(colors.get("h"), 0.0, 1.0, 0.0, 65535.0)));
                                LOG.debug("Having valid send h: {}, s: {}, b: {}", lightState.getHue(), lightState.getSaturation(), lightState.getBrightness());
                                send = true;
                            } catch (Exception ex){
                                LOG.error("Could not send color to hue fixture {}", device.getDeviceName());
                            }
                            break;
                        case "sendalert":
                            lightState.setAlertMode(PHLight.PHLightAlertMode.ALERT_SELECT);
                            send = true;
                            break;
                        case "transtime":
                            deviceDelays.put(device.getAddress(), (int)dcr.getCommandValue());
                            break;
                    }
                    LOG.debug("Going to send new light state {} to {} ({})", lightState.hashCode(), light.getIdentifier(), send);
                    if(send){
                        try {
                            /// Why sleep? Well, sometimes the bridge seems slower then us?
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            //// No message
                        }
                        bridge.updateLightState(light, lightState);
                    }
                }
            }
        };
        lightActionExecutor.submit(exec);
    }

    /**
     * This runs the hue device updater.
     */
    private void updateLightDevices(final PHBridge bridge) {
        LOG.trace("Running update for {} devices", getHardwareDevice().getSoftwareDriver().getRunningDevices().size());
        List<Device> devices = getHardwareDevice().getSoftwareDriver().getRunningDevices();
        List<PHLight> lights = bridge.getResourceCache().getAllLights();
        if(deviceSyncRunning==false){
            for(Device device:devices){
                for(PHLight light:lights){
                    if(device.getAddress().equals(light.getIdentifier())){
                        DeviceNotification notification = new DeviceNotification();
                        PHLightState lightState = light.getLastKnownLightState();
                        double bri = 1.0f;
                        if(light.supportsBrightness()){
                            bri = (float)MathImpl.map(lightState.getBrightness(), 0, 254, 0, 1);
                        }
                        if (light.supportsColor()){
                            double hue = MathImpl.map(lightState.getHue(), 0, 65535, 0, 1);
                            double sat = MathImpl.map(lightState.getSaturation(), 0, 254, 0, 1);
                            LOG.trace("Reported colors for '{}': hue: {}, bri: {}, sat: {}", light.getName(),hue, bri, sat);
                            Map<String,Object> lightCollection = new HashMap<>();
                            lightCollection.put("h", hue);
                            lightCollection.put("s", sat);
                            lightCollection.put("b", bri);
                            try {
                                DeviceControl deviceControl = device.getFullCommandSet().getControlsGroup("lightactions").getDeviceControl("setcolor");
                                deviceControl.setLastKnownValue(lightCollection);
                                notification.addData("lightactions", "setcolor", deviceControl.getValue(), false);
                            } catch (DeviceControlsGroupException | DeviceControlException ex) {
                                LOG.error("Could not update control, does not exist");
                            }
                        }
                        notification.addData("lightactions", "onoff", lightState.isOn(), false);
                        device.dispatchToHost(notification);
                    }
                }
            }
        }
        if(this.driverIsManager && (devices.size()!=lights.size()) && (deviceSyncRunning==false)){
            scanForNewDevices();
        }
    }

    @Override
    public boolean scanForNewDevices() {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        List<Device> devices = getHardwareDevice().getSoftwareDriver().getRunningDevices();
        List<PHLight> lights = bridge.getResourceCache().getAllLights();
        LOG.trace("scanForNewDevices: Amount of devices: {}, Amount of lights: {}. Current sync running: {}",devices.size(), lights.size(), deviceSyncRunning);
        if(this.driverIsManager && (devices.size()!=lights.size()) && (deviceSyncRunning==false)){
            Runnable deviceSync = () -> {
                deviceSyncRunning = true;
                LOG.debug("Starting lights -> device sync.");
                if(devices.size()>lights.size()){
                    if(this.driverIsManager){
                        List<Integer> toRemove = new ArrayList<>();
                        for(Device device:devices){
                            boolean remove = true;
                            for(PHLight light:lights){
                                if(light.getIdentifier().equals(device.getAddress())) remove = false;
                            }
                            if(remove){
                                toRemove.add(device.getId());
                                deviceDelays.remove(device.getAddress());
                            }
                        }
                        for(int id:toRemove){
                            try {
                                deleteDevice(id);
                                LOG.info("Removed device id: {}", id);
                            } catch (PluginDeviceMutationException ex) {
                                LOG.error("plugin could not delete device id: {}", id);
                            }
                        }
                    }
                }
                if(devices.size()<lights.size()){
                    for(PHLight light:lights){
                        boolean exists = false;
                        for(Device device:devices){
                            if(light.getIdentifier().equals(device.getAddress())) exists = true;
                        }
                        if(exists == false){
                            if(this.driverIsManager){
                                try {
                                    saveDevice("org.pidome.driver.device.pidomePhilipsHueDevice", light.getName(), light.getIdentifier(), 3);
                                    deviceDelays.put(light.getIdentifier(), 4);
                                    LOG.debug("Created new device: {}", light.getName());
                                } catch (PluginDeviceMutationException ex) {
                                    LOG.error("Could not create device {} by plugin: {}", light.getName(), ex.getMessage());
                                }
                            } else {
                                DiscoveredDevice newDevice = new DiscoveredDevice(light.getIdentifier(),"MySensors Device node id (address request) via Serial");
                                newDevice.addVisualInformation("Reported name", light.getName());
                                newDevice.addVisualInformation("Reported fixture type", light.getLightType());
                                newDevice.addVisualInformation("Reported model number", light.getModelNumber());
                                try {
                                    DiscoveredItemsCollection.addDiscoveredDevice(this, newDevice);
                                } catch (DeviceDiscoveryServiceException ex) {
                                    LOG.error("Could not add '{}' to discovery because of: {}", light.getName(), ex.getMessage());
                                }
                            }
                        }
                    }
                }
                deviceSyncRunning = false;
                DiscoveredItemsCollection.signalDevicesScanDone(PhilipsHue.this);
            };
            deviceSync.run();
        }
        return true;
    }
    

    @Override
    public boolean stopScanForNewDevices() {
        ///Not supported yet.
        return false;
    }
    
    /**
     * Prepares for deletion.
     */
    @Override
    public void prepareDelete() {
        try {
            List<Device> devices = getHardwareDevice().getSoftwareDriver().getRunningDevices();
            List<Integer> deviceIds= new ArrayList<>();
            for(Device device: devices){
                deviceIds.add(device.getId());
            }
            for(Integer id:deviceIds){
                try {
                    this.deleteDevice(id);
                } catch (PluginDeviceMutationException ex) {
                    LOG.error("Could not delete device {}: {}", id,ex.getMessage());
                }
            }
        }  catch (NullPointerException ex){
            /// In cases the plugin can not continue because of no bridge etc... the software driver is not started and hence not available causing nullpointer exceptions.
        }
    }
    
    /**
     * Stops the update service.
     */
    private void stopHueDeviceUpdater() {
        
    }

    /**
     * Creates the send url.
     *
     * @param path
     * @return
     */
    private String createUrl(String path) {
        return workUrl + "/" + path;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void startPlugin() throws PluginException {
        phHueSDK = PHHueSDK.create();
        phHueSDK.getNotificationManager().registerSDKListener(heuSDKListener);
        phHueSDK.setDeviceName("PiDome - Home Automation");
        PHAccessPoint hueAccessPoint = new PHAccessPoint();
        hueAccessPoint.setIpAddress(ip);
        hueAccessPoint.setUsername(this.username);
        phHueSDK.connect(hueAccessPoint);
    }

    /**
     * Private listener for the SDK
     */
    private final PHSDKListener heuSDKListener = new PHSDKListener() {

        @Override
        public void onAuthenticationRequired(PHAccessPoint accessPoint) {
            /// Already handled.
        }

        @Override
        public void onBridgeConnected(PHBridge phb) {
            phHueSDK.setSelectedBridge(phb);
            phHueSDK.enableHeartbeat(phb, lightsCheckTimeout * 1000);
            connected = true;
            LOG.info("Connected with: {}", phb.getResourceCache().getBridgeConfiguration().getIpAddress());
        }

        @Override
        public void onAccessPointsFound(List<PHAccessPoint> list) {
            /// already taken care of
        }

        @Override
        public void onError(int i, String string) {
            /// future implementation
            LOG.error("Hue error. Code: {}, Message: {}", i, string);
        }

        @Override
        public void onConnectionResumed(PHBridge phb) {
            connected = true;
            LOG.trace("updating: {}", phb.getResourceCache().getBridgeConfiguration().getIpAddress());
            updateLightDevices(phb);
        }

        @Override
        public void onConnectionLost(PHAccessPoint phap) {
            connected = false;
            LOG.error("Hue error. Connection lost with: {}", phap.getIpAddress());
        }

        @Override
        public void onCacheUpdated(List<Integer> list, PHBridge phb) {
            //// not used yet
        }

        @Override
        public void onParsingErrors(List<PHHueParsingError> list) {
            //// not used yet
        }

    };

    /**
     * @inheritDoc
     */
    @Override
    public void stopPlugin() {
        phHueSDK.getNotificationManager().unregisterSDKListener(heuSDKListener);
        phHueSDK.destroySDK();
        stopHueDeviceUpdater();
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getExpectedDriverId() {
        return "NATIVE_PIDOMEPHILIPSHUE_DRIVER";
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
    public void handleDeviceData(Device device, String group, String set, byte[] data, boolean userIntent) {
        /// Not used
    }

    /**
     * @inheritDoc
     */
    @Override
    public void handleCustomWebCommand(String function, Map<String, String> values) {
        switch (function) {
            case "":

                break;
        }
    }

    @Override
    public void deviceRemoved(Device device) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deviceAdded(Device device) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void prepareWebPresentation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasGraphData() {
        return false;
    }

    @Override
    public void handleDeviceData(Device device, DeviceCommandRequest dcr) {
        if (registered) {
            handleDeviceCommand(device, dcr);
        }
    }

    @Override
    public void handleNewDeviceRequest(WebPresentAddExistingDeviceRequest wpaedr) throws PeripheralDriverDeviceMutationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void discoveryEnabled() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void discoveryDisabled() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}