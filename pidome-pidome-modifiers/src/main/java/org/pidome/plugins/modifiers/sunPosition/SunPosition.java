/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.plugins.modifiers.sunPosition;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.SPA;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.interfaces.web.configuration.WebConfiguration;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.interfaces.web.configuration.WebOption;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.datamodifiers.DataModifierPlugin;
import org.pidome.server.connector.plugins.hooks.DeviceHook;
import org.pidome.server.connector.plugins.hooks.DeviceHookListener;
import org.pidome.server.connector.shareddata.SharedServerTimeService;

/**
 *
 * @author John
 */
public final class SunPosition extends DataModifierPlugin implements DeviceHookListener {
 
    private static AzimuthZenithAngle position;
    
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    
    boolean hasdeviceResources = false;
    
    private static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(SunPosition.class);
    
    private Map<String, String> configuration;
    
    private int tempDeviceId = 0;
    private String tempDeviceCtrl = "";
    private double temp = 0;
    
    private int pressDeviceId = 0;
    private String pressDeviceCtrl = "";
    private double press = 0;
    
    private double elevation = 0D;
    
    public SunPosition(){
        WebConfiguration conf = new WebConfiguration();
        WebConfigurationOptionSet optionSetEnvData = new WebConfigurationOptionSet("Environment options");
        optionSetEnvData.setConfigurationSetDescription("Select a device which delivers current temperature and a device delivering current pressure as this will "
                                                      + "make sure calculations are more accurate based on air refraction. The devices are not mandatory, but if you "
                                                      + "want to use them you need to use them both. The elevation is required.");
        optionSetEnvData.addOption(new WebOption("TEMPDEVICE", "Select device for temperature", "Select a device control delivering temperature in degrees", WebOption.WebOptionConfigurationFieldType.DEVICEDATA));
        optionSetEnvData.addOption(new WebOption("PRESSDEVICE", "Select device for pressure", "Select a device control delivering pressure in Hpa", WebOption.WebOptionConfigurationFieldType.DEVICEDATA));
        
        WebOption elevation = new WebOption("ELEVATION", "Elevation", "Set an elevation in meters", WebOption.WebOptionConfigurationFieldType.DOUBLE);
        elevation.setDefaultValue("0");
        optionSetEnvData.addOption(elevation);
        
        conf.addOptionSet(optionSetEnvData);
        this.setConfiguration(conf);
    }
    
    @Override
    public final String getCurrentValue(){
        if(position!=null){
            return String.format(Locale.US,"%.2f", position.getAzimuth()) + "," + String.format(Locale.US,"%.2f", position.getZenithAngle());
        } else {
            return "0,0";
        }
    }   
    
    @Override
    public DeviceControl.DataModifierDirection getDirection() {
        return DeviceControl.DataModifierDirection.OUTPUT;
    }

    @Override
    public void handleInput(DeviceCommandRequest dcr) {
        // Not used.
    }

    @Override
    public void setConfigurationValues(Map<String, String> map) throws WebConfigurationException {
        this.configuration = map;
        LOG.debug("Having configuration values: {}", configuration);
        
        boolean hasTemp = false;
        boolean hasPress = false;
        
        if(configuration.get("TEMPDEVICE")!=null && !configuration.get("TEMPDEVICE").equals("")){
            String[] tempSplitted = configuration.get("TEMPDEVICE").split(";");
            try {
                tempDeviceId = Integer.valueOf(tempSplitted[0]);
                tempDeviceCtrl = tempSplitted[2];
                DeviceHook.addDevice(this, tempDeviceId, tempDeviceCtrl);
            } catch (Exception ex){
                LOG.warn("No temperature used due to setup: {}, config: {} - units: {}",ex.getMessage(), configuration.get("TEMPDEVICE"));
            }
        }
        if(configuration.get("PRESSDEVICE")!=null && !configuration.get("PRESSDEVICE").equals("")){
            String[] pressSplitted = configuration.get("PRESSDEVICE").split(";");
            try {
                pressDeviceId = Integer.valueOf(pressSplitted[0]);
                pressDeviceCtrl = pressSplitted[2];
                DeviceHook.addDevice(this, pressDeviceId, pressDeviceCtrl);
            } catch (Exception ex){
                LOG.warn("No pressure used due to setup: {}, config: {} - units: {}",ex.getMessage(), configuration.get("PRESSDEVICE"));
            }
        }
        if(hasTemp && hasPress){
            this.hasdeviceResources = true;
        }
        if(configuration.get("ELEVATION")!=null && !configuration.get("ELEVATION").equals("")){
            elevation = Double.parseDouble(configuration.get("ELEVATION"));
        }
    }

    @Override
    public void startPlugin() throws PluginException {
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            if(hasdeviceResources){
                position = SPA.calculateSolarPosition(SharedServerTimeService.getCalendar(),
                                                      Double.valueOf(SharedServerTimeService.getLatitude()), // latitude (degrees)
                                                      Double.valueOf(SharedServerTimeService.getLongitude()), // longitude (degrees)
                                                      elevation, // elevation (m)
                                                      DeltaT.estimate(SharedServerTimeService.getCalendar()), // delta T (s)
                                                      press, // avg. air pressure (hPa)
                                                      temp); // avg. air temperature (°C)
            } else {
                position = SPA.calculateSolarPosition(SharedServerTimeService.getCalendar(),
                                                      Double.valueOf(SharedServerTimeService.getLatitude()), // latitude (degrees)
                                                      Double.valueOf(SharedServerTimeService.getLongitude()), // longitude (degrees)
                                                      elevation, // elevation (m)
                                                      DeltaT.estimate(SharedServerTimeService.getCalendar())); // delta T (s)
            }
            LOG.debug("SPA - Azimuth: {}, Zenith: {} (having temp and pressure: {})", position.getAzimuth(), position.getZenithAngle(), hasdeviceResources);
            this.passToControls(String.format(Locale.US,"%.2f", position.getAzimuth()) + "," + String.format(Locale.US,"%.2f", position.getZenithAngle()));
        }, 0L, 5L, TimeUnit.MINUTES);
        this.setRunning(true);
    }

    @Override
    public void stopPlugin() throws PluginException {
        if(executor != null){
            executor.shutdownNow();
            executor = null;
        }
        this.setRunning(false);
    }

    @Override
    public void handleCustomWebCommand(String string, Map<String, String> map) {
        ///Not used now.
    }

    @Override
    public void prepareWebPresentation() {
        // No presentation for now.
    }

    @Override
    public boolean hasGraphData() {
        ///This would be cool to implement for this plugin.
        return false;
    }

    @Override
    public void prepareDelete() {
        this.clearListeners();
    }

    @Override
    public void handleDeviceData(Device device, String group, String control, DeviceControl deviceControl, Object deviceValue) {
        if(device.getId()==this.tempDeviceId && control.equals(this.tempDeviceCtrl) && deviceValue instanceof Number){
            this.temp = ((Number)deviceValue).doubleValue();
        }
        if(device.getId()==this.pressDeviceId && control.equals(this.pressDeviceCtrl) && deviceValue instanceof Number){
            this.press = ((Number)deviceValue).doubleValue();
        }
    }
    
    
}