/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComLighting2;

import org.pidome.driver.device.rfxcom.RFXComDevice;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComCommand;
import org.pidome.server.connector.drivers.devices.DeviceNotification;

/**
 *
 * @author John
 */
public final class RFXComLighting2Command extends RFXComCommand {
    
    private boolean hasSwitch = false;
    private boolean setSwitch = false;
    
    private boolean hasGroupSwitch = false;
    private boolean setGroupSwitch = false;
    
    private boolean hasDim = false;
    private int setDim = 0;
    
    private boolean hasGroupDim = false;
    private int setGroupDim = 0;
    
    /**
     * Constructor.
     */
    public RFXComLighting2Command(){}
 
    /// setters.
    
    protected final void setHasSwitch(boolean has){
        hasSwitch = has;
    }
    
    protected final void setSwitch(boolean set){
        setSwitch = set;
    }
    
    protected final void setHasDim(boolean has){
        hasDim = has;
    }
    
    protected final void setDim(int dim){
        setDim = dim;
    }
    
    protected final void setHasGroupSwitch(boolean has){
        hasGroupSwitch = has;
    }
    
    protected final void setGroupSwitch(boolean set){
        setGroupSwitch = set;
    }
    
    protected final void setHasGroupDim(boolean has){
        hasGroupDim = has;
    }
    
    protected final void setGroupDim(int dim){
        setGroupDim = dim;
    }
    
    /// getters
    
    public final boolean hasSwitch(){
        return hasSwitch;
    }
    
    public final boolean getSwitch(){
        return setSwitch;
    }
    
    public final boolean hasDim(){
        return hasDim;
    }
    
    public final int getDim(){
        return setDim;
    }
    
    public final boolean hasGroupSwitch(){
        return hasGroupSwitch;
    }
    
    public final boolean getGroupSwitch(){
        return setGroupSwitch;
    }
    
    public final boolean hasGroupDim(){
        return hasGroupDim;
    }
    
    public final int getGroupDim(){
        return setGroupDim;
    }

    @Override
    public void handle(RFXComDevice device) {
        String group   = "deviceactions";
        String control = null;
        Object data    = null;
        boolean send   = false;
        if(hasSwitch()){
            control = "switch";
            data = getSwitch();
            send = true;
        } else if(hasGroupSwitch()){
            control = "groupswitch";
            data = getGroupSwitch();
            send = true;
        } else if(hasDim()){
            control = "dimlevel";
            data = getDim();
            send = true;
        } else if(hasGroupDim()){
            control = "groupdimlevel";
            data = getGroupDim();
            send = true;
        }
        if(send){
            DeviceNotification notification = new DeviceNotification();
            notification.addData(group, control, data);
            device.dispatchToHost(notification);
        }
    }
    
}