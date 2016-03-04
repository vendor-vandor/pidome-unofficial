/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.device.bareboneI2CDevice;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsSet.IntervalCommand;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceColorPickerControl;
import org.pidome.server.connector.tools.MiscImpl;

/**
 *
 * @author John Sirach
 */
public class BareboneI2CDevice extends Device {

    static Logger LOG = LogManager.getLogger(BareboneI2CDevice.class);
    
    List<Thread> threadPool = new ArrayList();
    
    public BareboneI2CDevice(){}

    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        switch(command.getControlType()){
            case COLORPICKER:
                Map<String,Map<String,Object>> colorMap = ((DeviceColorPickerControl)command.getControl()).getFullColorMap();
                dispatchToDriver(command.getGroupId(), command.getControlId(), ":WRITE:"+command.getExtraValue()+":"+colorMap.get("rgb").get("r")+","+colorMap.get("rgb").get("g")+","+colorMap.get("rgb").get("b"));
            break;
            case SLIDER:
                dispatchToDriver(command.getGroupId(), command.getControlId(), ":WRITE:"+command.getControlId()+":"+command.getCommandValue());
            break;
            default:
                String value;
                switch(command.getDataType()){
                    case STRING:
                        StringBuilder sendoptions = new StringBuilder();
                        Map<Integer,Object> sortedoptions = getDeviceOptions().getSortedOptionMap();
                        for(int key:sortedoptions.keySet()){
                            sendoptions.append(sortedoptions.get(key)).append(" ");
                        }
                        value = sendoptions.append((String)command.getCommandValue()).toString();
                    break;
                    case BOOLEAN:
                        value = String.valueOf(command.getCommandValueData());
                    break;
                    default:
                        value = String.valueOf(command.getCommandValue());
                    break;
                }
                if(value.contains(" ")){
                    String[] cmdSplitted = value.split(" ");
                    String sendCmd = cmdSplitted[0]+":";
                    for(int i=1; i< cmdSplitted.length; i++){
                        sendCmd += Integer.parseInt(cmdSplitted[i].substring(2),16) + ",";
                    }
                    dispatchToDriver(command.getGroupId(), command.getControlId(), ":WRITE:"+sendCmd.substring(0, sendCmd.length()-1));
                } else {
                    dispatchToDriver(command.getGroupId(), command.getControlId(), ":WRITE:"+value);
                }
            break;
        }
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public final void handleData(String data, Object object) {
        LOG.debug("Got data to handle: {} --- {}", data, object);
        /// We are using the object for the data. This object is a byte array with "Command/address" specific data
        /// without any standardisation in it.
        byte[] byteArray = (byte[])object;
        NumberFormat formatter;
        try {
            String[] input = data.split(":");
            float floatData = MiscImpl.byteArrayToFloat(byteArray);
            formatter = new DecimalFormat("##0.####");
            
            DeviceNotification notification = new DeviceNotification();
            notification.addData(input[1],input[1], Float.valueOf(formatter.format(floatData)));
            dispatchToHost(notification);
            
        } catch (IndexOutOfBoundsException ex){
            LOG.error("Illegal return type");
        } catch (Exception ex){
            LOG.warn("Unsupported return set: {}", ex.getMessage());
        }
    }


    public void setPrivateScheduledItems() {
        for(IntervalCommand intervalCommand:getFullCommandSet().getReceiverSet()){
            final String intervalName = intervalCommand.getGroupName() + "_" + intervalCommand.getSetName();
            Thread receiverThread = new Thread(){
                @Override
                public void run(){
                    Thread.currentThread().setName(intervalName);
                    LOG.debug("New device intervalled thread '{}' interval set at: {}", intervalName, intervalCommand.getInterval());
                    for(;;){
                        try {
                            Thread.sleep(1000 * (intervalCommand.getInterval() * 60));
                            dispatchToDriver(intervalCommand.getGroupName(), intervalCommand.getSetName(),":READ:"+intervalCommand.getCommand()+":4");/// Float reading
                        } catch (InterruptedException ex) {
                            LOG.error("Device read thread '"+intervalName+"' stopped, bailing out: {}", ex.getMessage());
                            break;
                        }
                    }
                }
            };
            receiverThread.start();
            threadPool.add(receiverThread);
        }
    }
    
    public void stopReceivers(){
        LOG.debug("Stop temp receiver thread");
        if(threadPool!=null && threadPool.size()>0){
            for(int i = 0; i < threadPool.size(); i++){
                if(threadPool.get(i)!=null && threadPool.get(i).isAlive()){
                    threadPool.get(i).interrupt();
                }
            }
            threadPool.clear();
        }
    }

    @Override
    public void shutdownDevice() {
        stopReceivers();
    }

    @Override
    public void startupDevice() {
        setPrivateScheduledItems();
    }
    
}