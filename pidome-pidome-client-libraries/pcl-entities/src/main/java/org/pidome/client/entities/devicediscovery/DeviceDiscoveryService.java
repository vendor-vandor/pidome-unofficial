/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.devicediscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.Entity;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.devices.DeviceService;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.client.system.PCCConnectionNameSpaceRPCListener;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;

/**
 *
 * @author John
 */
public class DeviceDiscoveryService extends Entity implements PCCConnectionNameSpaceRPCListener {

    /**
     * Connection interface.
     */
    private PCCConnectionInterface connection;
    
    List<DeviceDiscoveryDriver> driversList = new ArrayList<>();
    
    static {
        Logger.getLogger(DeviceService.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Possible discovery types.
     */
    public enum DiscoveryType {
        
        /**
         * Discovery is disabled.
         */
        NONE(-1000),
        /**
         * Discovery enabled for a single device.
         */
        SINGLE_DEVICE(-2),
        /**
         * Discovery enabled for one minute.
         */
        MINUTE_1(1),
        /**
         * Discovery enabled for 5 minutes.
         */
        MINUTE_5(5),
        /**
         * Discovery enabled for 10 minutes.
         */
        MINUTE_10(10),
        /**
         * Discovery enabled for 30 minutes.
         */
        MINUTE_30(30),
        /**
         * Discovery enabled indefintely until disabled.
         */
        INDEFINITELY(-1);
        
        private int level = -1000;
        
        private DiscoveryType(int level){
            this.level = level;
        }
        
        protected final int getLevel(){
            return this.level;
        }
        
    }
    
    /**
     * Discovery methds supported by the driver.
     * These are not used in enabling discovery and only for informative purposes. With the exception for NONE.
     */
    public enum DiscoveryMethod {
        /**
         * Misconfigured discovery driver.
         * When NONe is in the list of discovery methods you can be asured the 
         * driver is misconfigured and hence no discovery will be enabled.
         * Trying to enable discovery when NONE is in the list will result in an
         * exception.
         */
        NONE,
        /**
         * Discovery type scan.
         * A scan scans a network/bus for new devices. Server driven.
         */
        SCAN,
        /**
         * Discovery type discover.
         * Discovery type receives data from new devices. Devices driven.
         */
        DISCOVERY
    }
    
    public enum DeviceAddFunctionType {
        FUNCTION_ADD_DEVICE("FUNCTION_ADD_DEVICE"),
        FUNCTION_REQUEST_ADDRESS("FUNCTION_REQUEST_ADDRESS");
        
        private final String value; 
        
        private DeviceAddFunctionType(String value){
            this.value = value;
        }
        
        public final String getValue(){
            return this.value;
        }
        
    }
    
    /**
     * Constructor.
     * @param connection The system's connection.
     */
    public DeviceDiscoveryService(PCCConnectionInterface connection){
        this.connection = connection;
    }
    
    /**
     * Initializes listeners.
     */
    @Override
    protected void initilialize() {
        this.connection.addPCCConnectionNameSpaceListener("DeviceService", this);
    }

    /**
     * Clean up.
     */
    @Override
    protected void release() {
        this.connection.removePCCConnectionNameSpaceListener("DeviceService", this);
        connection = null;
    }

    /**
     * Return a list of drivers which have discovery possibilities.
     * Use this instead of reload or preload.
     * @return List of drivers with discovery possibilities
     * @throws org.pidome.client.entities.devicediscovery.DeviceDiscoveryException When remote resources can not be loaded.
     */
    public final synchronized List<DeviceDiscoveryDriver> getDiscoveryEnabledDrivers() throws DeviceDiscoveryException {
        if(driversList.isEmpty()){
            try {
                ArrayList<Map<String,Object>> data = (ArrayList<Map<String,Object>>)this.connection.getJsonHTTPRPC("DeviceService.getDiscoveryEnabledDrivers", null, "DeviceService.getDiscoveryEnabledDrivers").getResult().get("data");
                for(Map<String,Object> set: data){
                    DeviceDiscoveryDriver driver = new DeviceDiscoveryDriver(set, this.connection);
                    driversList.add(driver);
                }
            } catch (NullPointerException | PCCEntityDataHandlerException ex) {
                Logger.getLogger(DeviceDiscoveryService.class.getName()).log(Level.SEVERE, "Could not get enabled drivers list", ex);
                throw new DeviceDiscoveryException(ex);
            }
        }
        getDiscoveredDevices();
        return driversList;
    }
    
    private void getDiscoveredDevices(){
        try {
            ArrayList<Map<String,Object>> data = (ArrayList<Map<String,Object>>)this.connection.getJsonHTTPRPC("DeviceService.getDiscoveredDevices", null, "DeviceService.getDiscoveredDevices").getResult().get("data");
            for(Map<String,Object> fullSet: data){
                for(DeviceDiscoveryDriver driver : this.driversList.subList(0, this.driversList.size())){
                    if(driver.getPort().equals((String)fullSet.get("port"))){
                        for(Map<String,Object> set: (List<Map<String,Object>>)fullSet.get("devices")){
                            driver.addFoundDeviceFromBroadcast(new DiscoveredDevice(connection, driver, set, false));
                        }
                    }
                }
            }
        } catch (NullPointerException | PCCEntityDataHandlerException ex) {
            Logger.getLogger(DeviceDiscoveryService.class.getName()).log(Level.SEVERE, "Could not get enabled drivers list", ex);
        }
    }
    
    /**
     * Preloads content.
     * Please use getDiscoveryEnabledDrivers() as this entity must use this blocking feature to preload.
     * @throws EntityNotAvailableException Always thrown when using this.
     */
    @Override
    public void preload() throws EntityNotAvailableException {
        throw new EntityNotAvailableException("Please use getDiscoveryEnabledDrivers() as this entity must use this blocking feature to preload.");
    }

    /**
     * Reloads content.
     * Please use unloadContent() followed by getDiscoveryEnabledDrivers() as this entity must use this blocking feature to reload.
     * @throws EntityNotAvailableException Always thrown when using this.
     */
    @Override
    public void reload() throws EntityNotAvailableException {
        throw new EntityNotAvailableException("Please use unloadContent() followed by getDiscoveryEnabledDrivers() as this entity must use this blocking feature to reload.");
    }

    /**
     * unloads any content held.
     * @throws EntityNotAvailableException When content can not be released.
     */
    @Override
    public void unloadContent() throws EntityNotAvailableException {
        for(DeviceDiscoveryDriver driver: driversList){
            driver.destroy();
        }
        driversList.clear();
    }

    
    /**
     * Handles rpc result from broadcasts.
     * @param rpcDataHandler Broadcast data handler.
     */
    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        switch(rpcDataHandler.getMethod()){
            case "discoveryEnabled":
                String enablePort = (String)rpcDataHandler.getParameters().get("port");
                for(DeviceDiscoveryDriver driver:driversList){
                    if(driver.getPort().equals(enablePort)){
                        driver.setDiscoveryEnabled(((Number)rpcDataHandler.getParameters().get("time")).intValue());
                        break;
                    }
                }
            break;
            case "discoveryDisabled":
                String disablePort = (String)rpcDataHandler.getParameters().get("port");
                for(DeviceDiscoveryDriver driver:driversList){
                    if(driver.getPort().equals(disablePort)){
                        driver.setDiscoveryDisabled();
                        break;
                    }
                }
            break;
            case "discoveredNewDevice":
                String discoveredPort = (String)rpcDataHandler.getParameters().get("port");
                for(DeviceDiscoveryDriver driver:driversList){
                    if(driver.getPort().equals(discoveredPort)){
                        driver.addFoundDeviceFromBroadcast(new DiscoveredDevice(connection, driver, rpcDataHandler.getParameters()));
                        break;
                    }
                }
            break;
            case "removedDiscoveredDevice":
                String removedPort = (String)rpcDataHandler.getParameters().get("port");
                String removedAddress = (String)rpcDataHandler.getParameters().get("deviceaddress");
                for(DeviceDiscoveryDriver driver:driversList){
                    if(driver.getPort().equals(removedPort)){
                        driver.removeFoundDeviceFromBroadcast(removedAddress);
                        break;
                    }
                }
            break;
        }
    }

    /**
     * Handles RPC results from client requests.
     * @param rpcDataHandler Request result handler.
     */
    @Override
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
        ///
    }
    
}
