/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays;

import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.Devices;
import org.pidome.client.system.scenes.components.mainstage.displays.components.DeviceContentPane;
import org.pidome.client.system.scenes.windows.TitledWindow;

/**
 *
 * @author John Sirach
 */
public class DeviceDisplay extends TitledWindow {

    Device device;
    
    DeviceContentPane content;
    
    public DeviceDisplay(Object... deviceIds) throws Exception {
        this((Device)Devices.getDeviceById(Integer.valueOf((String)deviceIds[0])));
    }
    
    public DeviceDisplay(Device device){
        super("device"+device.getId(),device.getName());
        this.device = device;
        getStyleClass().add("devicedisplay");
        setSize(500*DisplayConfig.getWidthRatio(), 500*DisplayConfig.getHeightRatio());
    }

    @Override
    protected void setupContent() {
        content = new DeviceContentPane(this, this.device);
        content.setupContent();
        setContent(content);
    }
    
    @Override
    protected void removeContent() {
        content.removeContent();
    }

}
