/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.datamodifiers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl.DataModifierDirection;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.freeform.FreeformPlugin;

/**
 *
 * @author John
 */
public abstract class DataModifierPlugin extends FreeformPlugin {

    /**
     * Tracking of controls.
     */
    private final List<DeviceControl> controls = Collections.synchronizedList(new ArrayList<DeviceControl>());

    /**
     * Returns thr amount of controls attached
     * @return 
     */
    public final int getAttachedControlsAmount(){
        return controls.size();
    }
    
    /**
     * Returns a list of bound controls.
     * @return 
     */
    public final List<DeviceControl> getBoundControls(){
        return this.controls;
    }
    
    /**
     * Adds a device control to the list.
     * @param control 
     */
    public final void addDeviceControl(DeviceControl control){
        if(!this.controls.contains(control)){
            control.setDataDirection(this.getDirection());
            controls.add(control);
        }
    }
    
    /**
     * Removes a device control from the list.
     * @param control 
     */
    public final void removeDevicecontrol(DeviceControl control){
        controls.remove(control);
    }
    
    /**
     * Pass a string to the controls.
     * @param data 
     */
    public final void passToControls(String data){
        for(DeviceControl control:controls){
            control.handleModifierOutput(data);
        }
    }

    /**
     * Pass a color map to the controls.
     * @param data 
     */
    public final void passToControls(Map<String,Object> data){
        for(DeviceControl control:controls){
            control.handleModifierOutput(data);
        }
    }
    
    /**
     * Pass an int to the controls.
     * @param data 
     */
    public final void passToControls(int data){
        for(DeviceControl control:controls){
            control.handleModifierOutput(data);
        }
    }
    
    /**
     * Pass a float to the controls.
     * @param data 
     */
    public final void passToControls(float data){
        for(DeviceControl control:controls){
            control.handleModifierOutput(data);
        }
    }
    
    /**
     * Pass a color map to the controls.
     * @param data 
     */
    public final void passToControl(DeviceControl deviceControl, Map<String,Object> data){
        for(DeviceControl control:controls){
            if(control == deviceControl){
                control.handleModifierOutput(data);
            }
        }
    }
    
    /**
     * Pass string data to a single control.
     * @param deviceControl
     * @param data 
     */
    public final void passToControl(DeviceControl deviceControl, String data){
        for(DeviceControl control:controls){
            if(control == deviceControl){
                control.handleModifierOutput(data);
            }
        }
    }
    
    /**
     * Pass an int to a single control.
     * @param deviceControl
     * @param data 
     */
    public final void passToControl(DeviceControl deviceControl, int data){
        for(DeviceControl control:controls){
            if(control == deviceControl){
                control.handleModifierOutput(data);
            }
        }
    }
    
    /**
     * Pass a float to a single control.
     * @param deviceControl
     * @param data 
     */
    public final void passToControl(DeviceControl deviceControl, float data){
        for(DeviceControl control:controls){
            if(control == deviceControl){
                control.handleModifierOutput(data);
            }
        }
    }
    
    
    /**
     * Pass a boolean to the controls.
     * @param data 
     */
    public final void passToControls(boolean data){
        for(DeviceControl control:controls){
            control.handleModifierOutput(data);
        }
    }
    
    /**
     * Clears the listeners list.
     */
    public final void clearListeners(){
        this.controls.clear();
    }
    
    /**
     * Returns the current modifier value.
     * As some modifiers only start to work when a control is bound it is only
     * for informative purposes.
     * When overridden use the datatype you are using.
     * @return 
     */
    public abstract Object getCurrentValue();
    
    /**
     * Returns the control directions.
     * @return 
     */
    public abstract DataModifierDirection getDirection();
    
    /**
     * Handle input coming from a control.
     * @param input 
     */
    public abstract void handleInput(DeviceCommandRequest input);
    
    /**
     * If you have any configuration values to be presented to the web interface they will be returned here.
     * @param configuration
     * @throws WebConfigurationException 
     */
    @Override
    public abstract void setConfigurationValues(Map<String, String> configuration) throws WebConfigurationException;

    /**
     * Initialize the plugin.
     * @throws PluginException 
     */
    @Override
    public abstract void startPlugin() throws PluginException;

    /**
     * De-initialize the plugin, remove any references.
     * @throws PluginException 
     */
    @Override
    public abstract void stopPlugin() throws PluginException;
    
    /**
     * This gives the possibility to execute commands from the plugin's web interface.
     * @param function
     * @param values 
     */
    public abstract void handleCustomWebCommand(String function, Map<String,String> values);
    
    /**
     * Used to build the web interface components.
     */
    @Override
    public abstract void prepareWebPresentation();
    
}