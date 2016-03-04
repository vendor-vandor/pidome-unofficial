/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComLighting5;

import org.pidome.driver.device.rfxcom.RFXComDevice;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComCommand;
import org.pidome.server.connector.drivers.devices.DeviceNotification;

/**
 *
 * @author John
 */
public final class RFXComLighting5LightwaveRFCommand extends RFXComCommand {

    /** Command set.
    0x00: "Off",
    0x01: "On",
    0x02: "Group Off",
    0x03: "mood1",
    0x04: "mood2",
    0x05: "mood3",
    0x06: "mood4",
    0x07: "mood5",
    0x08: "reserved",
    0x09: "reserved",
    0x0A: "unlock",
    0x0B: "lock",
    0x0C: "all lock",
    0x0D: "close (inline relay)",
    0x0E: "stop (inline relay)",
    0x0F: "open (inline relay)",
    0x10: "set level",
    0x11: "colour Palette",
    0x12: "Colour Tone",
    0x13: "Colour Cycle"
    **/
    
    private String command = "0x00";
    private int level = 0;
    
    public final void setCommand(String command){
        this.command = command;
    }
        
    public final void setLevel(int level){
        this.level = level;
    }
    
    @Override
    public void handle(RFXComDevice device) {
        String group   = "deviceactions";
        String control = null;
        Object data    = null;
        boolean send   = false;
        switch(command){
            case "0x00":
            case "0x01":
                data    = command.equals("0x01");
                control = "switch";
                send = true;
            break;
            case "0x02":
                data    = true;
                control = "groupoff";
                send = true;
            break;
            case "0x03":
                data    = command;
                control = "moodselect1";
                send = true;
            break;
            case "0x04":
                data    = command;
                control = "moodselect2";
                send = true;
            break;
            case "0x05":    
                data    = command;
                control = "moodselect3";
                send = true;
            break;
            case "0x06":
                data    = command;
                control = "moodselect4";
                send = true;
            break;
            case "0x07":
                data    = command;
                control = "moodselect5";
                send = true;
            break;
            case "0x0A":
            case "0x0B":
                data    = command.equals("0x0B");
                control = "lockswitch";
                send = true;
            break;
            case "0x0C":
                data    = true;
                control = "alllock";
                send = true;
            break;
            case "0x0D":
            case "0x0F":
                data    = command.equals("0x0F");
                control = "relayswitch";
                send = true;
            break;
            case "0x0E":
                data    = true;
                control = "relaystop";
                send = true;
            break;
            case "0x10":
                data    = level;
                control = "level";
                send = true;
            break;
            case "0x11":
                data    = true;
                control = "colornext";
                send = true;
            break;
            case "0x12":
                data    = true;
                control = "colortone";
                send = true;
            break;
            case "0x13":
                data    = true;
                control = "colorcycle";
                send = true;
            break;
        }
        if(send){
            DeviceNotification notification = new DeviceNotification();
            notification.addData(group, control, data);
            device.dispatchToHost(notification);
        }
    }
    
}