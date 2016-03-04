/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComLighting1;

import org.pidome.driver.device.rfxcom.RFXComDevice;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComCommand;
import org.pidome.server.connector.drivers.devices.DeviceNotification;

/**
 *
 * @author John
 */
public final class RFXComLighting1Command extends RFXComCommand {
    
    private boolean hasSwitch = false;
    private boolean setSwitch = false;
    
    private boolean hasGroupSwitch = false;
    private boolean setGroupSwitch = false;
    
    private boolean hasDim = false;
    private boolean setDim = false;

    private boolean hasBright = false;
    private boolean setBright = false;
    
    private boolean hasGroupDim = false;
    private boolean setGroupDim = false;
    
    private boolean hasChime = false;
    private boolean setChime = false;
    
    /**
     * Constructor.
     */
    public RFXComLighting1Command(){}
 
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
    
    protected final void setDim(boolean dim){
        setDim = dim;
    }
    
    protected final void setHasBright(boolean has){
        hasBright = has;
    }
    
    protected final void setBright(boolean dim){
        setBright = dim;
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
    
    protected final void setGroupDim(boolean dim){
        setGroupDim = dim;
    }
    
    protected final void setHasChime(boolean has){
        hasChime = has;
    }
    
    protected final void setChime(boolean set){
        setChime = set;
    }
    
    
    /// getters
    
    public final boolean getHasSwitch(){
        return hasSwitch;
    }
    
    public final boolean getSwitch(){
        return setSwitch;
    }
    
    public final boolean getHasDim(){
        return hasDim;
    }
    
    public final boolean getDim(){
        return setDim;
    }
    
    public final boolean getHasBright(){
        return hasDim;
    }
    
    public final boolean getBright(){
        return setDim;
    }
    
    public final boolean getHasGroupSwitch(){
        return hasGroupSwitch;
    }
    
    public final boolean getGroupSwitch(){
        return setGroupSwitch;
    }
    
    public final boolean getHasGroupDim(){
        return hasGroupDim;
    }
    
    public final boolean getGroupDim(){
        return setGroupDim;
    }
    
    public final boolean hasChime(){
        return hasChime;
    }
    
    public final boolean getChime(){
        return setChime;
    }

    @Override
    public void handle(RFXComDevice device) {
        String group   = "deviceactions";
        String control = null;
        Object data    = null;
        boolean send   = false;
        if(getHasSwitch()){
            control = "switch";
            data = getSwitch();
            send = true;
        } else if(getHasGroupSwitch()){
            control = "groupswitch";
            data = getGroupSwitch();
            send = true;
        } else if(getHasDim()){
            control = "dim";
            data = getDim();
            send = true;
        } else if(getHasBright()){
            control = "bright";
            data = getBright();
            send = true;
        } else if(hasChime()){
            control = "chime";
            data = getChime();
            send = true;            
        }
        if(send){
            DeviceNotification notification = new DeviceNotification();
            notification.addData(group, control, data);
            device.dispatchToHost(notification);
        }
    }
    
}