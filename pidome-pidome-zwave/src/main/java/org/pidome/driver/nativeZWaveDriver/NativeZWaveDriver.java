package org.pidome.driver.nativeZWaveDriver;

import org.pidome.driver.nativeZWaveDriver.zwave.ZWaveDataHelper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryServiceException;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDevice;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDeviceNotFoundException;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredItemsCollection;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriver;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentSimpleNVP;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentationGroup;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentCustomFunction;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentCustomFunctionInterface;
import org.pidome.driver.nativeZWaveDriver.zwave.ZWaveCommandClass;
import org.pidome.driver.nativeZWaveDriver.zwave.ZWaveCommandClass.ZWaveCommand;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceStatusException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceColorPickerControlColorData;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlType;
import org.pidome.server.connector.drivers.devices.devicestructure.builder.ColorPickerButtonBuilder;
import org.pidome.server.connector.drivers.devices.devicestructure.builder.CustomDataBuilder;
import org.pidome.server.connector.drivers.devices.devicestructure.builder.DeviceStructureCreator;
import org.pidome.server.connector.drivers.devices.devicestructure.builder.OptionListBuilder;
import org.pidome.server.connector.drivers.devices.devicestructure.builder.SliderDataBuilder;
import org.pidome.server.connector.drivers.devices.devicestructure.builder.ToggleDataBuilder;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryInterface;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryScanInterface;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralDriverDeviceMutationException;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentAddExistingDeviceRequest;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentCustomFunctionRequest;
import org.zwave4j.*;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author John
 */
public final class NativeZWaveDriver extends PeripheralSoftwareDriver implements PeripheralSoftwareDriverInterface, DeviceDiscoveryInterface, DeviceDiscoveryScanInterface, WebPresentCustomFunctionInterface {

    static Logger LOG = LogManager.getLogger(NativeZWaveDriver.class);

    private ZwaveThread zwaveThread;

    private boolean on = false;

    private boolean run = true;
    
    private long homeId;

    private Manager manager;
    NotificationWatcher dataListener;
    
    private String peripheralPort;
    
    private BlockingQueue<ZwaveOutgoingMessage> messageQueue;
    
    private BlockingQueue<Notification> incommingNewNotificationQueue;
    private Thread notififactionHandler;
    
    WebPresentationGroup present = new WebPresentationGroup("ZWave adapter info", "ZWave adapter information");
    
    WebPresentationGroup driverFunctions  = new WebPresentationGroup("Driver handles", "Below you can perform actions on the driver");
    
    WebPresentationGroup nodeFunctionsTable = new WebPresentationGroup("Node functions", "Functions for nodes.");
    
    public NativeZWaveDriver() {
        
        WebPresentCustomFunction newDeviceRequestFunction = new WebPresentCustomFunction("Remove node");
        newDeviceRequestFunction.setIdentifier("removeNode");
        WebPresentSimpleNVP newDeviceFunctionNVP = new WebPresentSimpleNVP("custom_driver_function");
        newDeviceFunctionNVP.setValue(newDeviceRequestFunction.getPresentationValue());
        
        WebPresentCustomFunction removeDeviceRequestFunction = new WebPresentCustomFunction("Cancel remove node");
        removeDeviceRequestFunction.setIdentifier("cancelRemoveNode");
        WebPresentSimpleNVP removeDeviceFunctionNVP = new WebPresentSimpleNVP("custom_driver_function");
        removeDeviceFunctionNVP.setValue(removeDeviceRequestFunction.getPresentationValue());
        
        nodeFunctionsTable.add(newDeviceFunctionNVP);
        nodeFunctionsTable.add(removeDeviceFunctionNVP);
        
        this.addWebPresentationGroup(nodeFunctionsTable);
        
    }

    @Override
    public boolean sendData(String string, String string1) throws IOException {
        LOG.info("Got a sendData(String string, String string1) event: {}, {}", string, string1);
        return false;
    }

    @Override
    public boolean sendData(String string) throws IOException {
        LOG.info("Got a sendData(String string) event: {}", string);
        return false;
    }

    @Override
    public boolean handleDeviceData(Device device, String string, String string1, String string2) throws IOException {
        /// Not used
        return false;
    }
    
    @Override
    public boolean handleDeviceData(Device device, DeviceCommandRequest request){
        LOG.info("Having custom data: {}", request.getControl().getControlCustomData());
        ZwaveOutgoingMessage newMessage = new ZwaveOutgoingMessage(homeId, request.getControl().getControlCustomData().getString("class", ""), request.getControl().getControlCustomData().getString("valuetype", ""));
        newMessage.setDeviceAddressFromString(device.getAddress());
        newMessage.setInstanceFromNumber(request.getControl().getControlCustomData().getNumber("instance", 0x01).shortValue());
        newMessage.setIndexFromNumber(request.getControl().getControlCustomData().getNumber("index", 0x00).shortValue());
        newMessage.setValueGenreFromString(request.getControl().getControlCustomData().getString("genre", "USER"));
        if(request.getControlType() == DeviceControlType.COLORPICKER){
            //newMessage.setPayload(String.format("#%06X", (0xFFFFFF & (int)(Math.random()*16581375))));
            DeviceColorPickerControlColorData colorData = new DeviceColorPickerControlColorData(request.getCommandValue());
            newMessage.setPayload(colorData.getHex() + "0000");
        } else if (!request.getControl().getControlCustomData().getString("colorsect", "").equals("")){
            switch(request.getControl().getControlCustomData().getString("colorsect", "WW")){
                case "CW":
                    newMessage.setPayload("#00000000FF");
                break;
                case "WW":
                    newMessage.setPayload("#000000FF00");
                break;
            }
        } else {
            newMessage.setPayload(request.getCommandValue());
        }
        try {
            messageQueue.put(newMessage);
            return true;
        } catch (InterruptedException ex) {
            LOG.error("Could not pass message into queue: {}", ex.getMessage());
        }
        return false;
    }
    
    /**
     * first entry in data receiving.
     *
     * @param oEvent
     */
    @Override
    public void driverBaseDataReceived(PeripheralHardwareDataEvent oEvent) {
        ///Not used, driver is running NoOp;
    }

    @Override
    public final void driverStart() {
        peripheralPort = this.getHardwareDriver().getPort();
        if(notififactionHandler==null){
            incommingNewNotificationQueue = new ArrayBlockingQueue<>(200);
            notififactionHandler = new NotififactionHandler(this.incommingNewNotificationQueue);
            notififactionHandler.start();
        }
        messageQueue = new ArrayBlockingQueue<>(100);
        zwaveThread = new ZwaveThread(messageQueue);
        zwaveThread.start();
    }

    private void addNewFoundDevice(Notification notification){
        LOG.info("Node new node id: {}",notification.getNodeId());
        if(DiscoveredItemsCollection.discoveryEnabled(this)){
            DiscoveredDevice newDevice = new DiscoveredDevice(String.valueOf(notification.getNodeId()), "New ZWave device");
            newDevice.setDeviceStructure(new DeviceStructureCreator());
            try {
                DiscoveredItemsCollection.addDiscoveredDevice(this, newDevice);
            } catch (DeviceDiscoveryServiceException ex) {
                LOG.error("Device discovery not enabled");
            }
        } else {
            LOG.warn("Enable discovery first");
        }
    }

    private void addNodeInformation(Notification notification){
        try {
            DiscoveredDevice device = DiscoveredItemsCollection.getDiscoveredDevice(NativeZWaveDriver.this, String.valueOf(notification.getNodeId()));
            if(notification.getValueId().getGenre().equals(ValueGenre.SYSTEM) && notification.getType().equals(NotificationType.NODE_NAMING)){
                String name = manager.getNodeName(homeId, notification.getNodeId());
                if(!name.equals("")){
                    device.getDeviceStructureCreator().setName(manager.getNodeName(homeId, notification.getNodeId()));
                }
                
            } else if(notification.getValueId().getGenre().equals(ValueGenre.USER) || notification.getValueId().getGenre().equals(ValueGenre.CONFIG)){
                
                String range = " (" + manager.getValueMin(notification.getValueId()) + " - " + manager.getValueMax(notification.getValueId())+ ")";
                ZWaveCommand zwaveGroupId = ZWaveCommandClass.getByString(notification.getValueId().getCommandClassId());
                device.getDeviceStructureCreator().addGroup(zwaveGroupId.toString().replace("_", ""), zwaveGroupId.getDescritpion());
                addControl(device.getDeviceStructureCreator(), zwaveGroupId, notification);
                
                device.addVisualInformation(zwaveGroupId.getDescritpion() + " (" + notification.getValueId().getInstance() + notification.getValueId().getIndex() + ")", manager.getValueLabel(notification.getValueId()) + range);
            } else if (notification.getType().equals(NotificationType.NODE_PROTOCOL_INFO)){
                device.addVisualInformation("Node type", manager.getNodeType(notification.getHomeId(), notification.getNodeId()));
                device.getDeviceStructureCreator().createAddressConfiguration("integer", "The remote node address", "The remote node address as a number");
            }
        } catch (DiscoveredDeviceNotFoundException ex) {
            // Not there yet
        }
    }
    
    private void addControl(DeviceStructureCreator creator, ZWaveCommand zwaveGroupId, Notification notification){
        if(notification.getValueId().getCommandClassId() == 51){
            
            String valueLabel = manager.getValueLabel(notification.getValueId());
            
            String groupId = zwaveGroupId.toString().replace("_", "");
            String commandClassId = String.valueOf(notification.getValueId().getCommandClassId());
            
            if(notification.getValueId().getType() == ValueType.STRING){
                CustomDataBuilder customDataRGB = new CustomDataBuilder();
                customDataRGB.setIntValue("index", notification.getValueId().getIndex());
                customDataRGB.setIntValue("instance", notification.getValueId().getInstance());
                customDataRGB.setStringValue("colorsect", "RGB");
                customDataRGB.setStringValue("valuetype", "STRING");
                customDataRGB.setStringValue("genre", notification.getValueId().getGenre().toString());
                customDataRGB.setStringValue("class", zwaveGroupId.toString());
                ColorPickerButtonBuilder button = new ColorPickerButtonBuilder("Set color", "btn_1");
                ArrayList<ColorPickerButtonBuilder> buttons = new ArrayList<>();
                buttons.add(button);
                creator.addColorControl(groupId, 
                                        commandClassId + "RGB", 
                                        valueLabel, 
                                        "rgb", 0, false, false, "", 
                                        buttons, 
                                        customDataRGB);

                CustomDataBuilder customDataWW = new CustomDataBuilder();
                customDataWW.setIntValue("index", notification.getValueId().getIndex());
                customDataWW.setIntValue("instance", notification.getValueId().getInstance());
                customDataWW.setStringValue("genre", notification.getValueId().getGenre().toString());
                customDataWW.setStringValue("class", zwaveGroupId.toString());
                customDataWW.setStringValue("colorsect", "WW");
                customDataWW.setStringValue("valuetype", "STRING");

                creator.addButtonControl(groupId, 
                                         commandClassId + "WW", 
                                         "Warm White" , 
                                         "boolean", 
                                         "Warm White",
                                         true, false, 0, "", customDataWW);

                CustomDataBuilder customDataCW = new CustomDataBuilder();
                customDataCW.setIntValue("index", notification.getValueId().getIndex());
                customDataCW.setIntValue("instance", notification.getValueId().getInstance());
                customDataCW.setStringValue("genre", notification.getValueId().getGenre().toString());
                customDataCW.setStringValue("class", zwaveGroupId.toString());
                customDataCW.setStringValue("colorsect", "CW");
                customDataCW.setStringValue("valuetype", "STRING");

                creator.addButtonControl(zwaveGroupId.toString().replace("_", ""), 
                                         commandClassId + "CW", 
                                         "Cold White" , 
                                         "boolean", 
                                         "Cold White",
                                         true, false, 0, "", customDataCW);
            } else if(notification.getValueId().getType() == ValueType.LIST){
                
                CustomDataBuilder customDataList = new CustomDataBuilder();
                customDataList.setIntValue("index", notification.getValueId().getIndex());
                customDataList.setIntValue("instance", notification.getValueId().getInstance());
                customDataList.setStringValue("genre", notification.getValueId().getGenre().toString());
                customDataList.setStringValue("class", zwaveGroupId.toString());
                customDataList.setStringValue("valuetype", "LIST");
                
                List<String> list = new ArrayList<>();
                manager.getValueListItems(notification.getValueId(), list);
                OptionListBuilder options = new OptionListBuilder();
                for(int i=0;i<list.size();i++){
                    options.addOptionItem(list.get(i), String.valueOf(i));
                }
                creator.addSelectControl(groupId,
                                  commandClassId,
                                  valueLabel,
                                  "string",
                                  false,
                                  false,
                                  0,
                                  "",
                                  options,
                                  customDataList);
            }
        } else {
            ValueId itemId = notification.getValueId();
            CustomDataBuilder customData = new CustomDataBuilder();
            customData.setIntValue("index", itemId.getIndex());
            customData.setIntValue("instance", itemId.getInstance());
            customData.setStringValue("genre", itemId.getGenre().toString());
            customData.setStringValue("valuetype", itemId.getType().toString());
            customData.setStringValue("class", zwaveGroupId.toString());
            
            String valueLabel = manager.getValueLabel(notification.getValueId());
            
            String groupId = zwaveGroupId.toString().replace("_", "");
            String commandClassId = String.valueOf(notification.getValueId().getCommandClassId());
            

            switch(notification.getValueId().getType()){
                case BUTTON:
                    creator.addButtonControl(groupId, 
                                             commandClassId, 
                                             valueLabel, 
                                             "boolean", 
                                             valueLabel,
                                             true, 
                                             false, 
                                             0, 
                                             "", 
                                             customData);
                break;
                case BYTE:
                case DECIMAL:
                case INT:
                case SHORT:
                    int min = manager.getValueMin(notification.getValueId());
                    int max = manager.getValueMax(notification.getValueId());
                    String datatype = ((notification.getValueId().getType()==ValueType.DECIMAL)?"float":"integer");
                    if(min != max){
                        SliderDataBuilder sliderData = new SliderDataBuilder(min, max);
                        creator.addSliderControl(groupId,
                                          commandClassId,
                                          valueLabel,
                                          datatype,
                                          false,
                                          false,
                                          0,
                                          "",
                                          sliderData,
                                          customData);
                    } else {
                        if(manager.isValueReadOnly(itemId)){
                            creator.addDataControl(groupId, 
                                                   commandClassId, 
                                                   valueLabel, 
                                                   datatype, 
                                                   false, 
                                                   false, 
                                                   0, 
                                                   "", 
                                                   "", 
                                                   0, 
                                                   0, 
                                                   0, 
                                                   0, 
                                                   "", 
                                                   "", 
                                                   customData);
                        } else {
                            creator.addButtonControl(groupId, 
                                                     commandClassId, 
                                                     valueLabel, 
                                                     datatype, 
                                                     valueLabel,
                                                     "true", 
                                                     false, 
                                                     0, 
                                                     "", 
                                                     customData);
                        }
                    }
                break;
                case LIST:

                break;
                case STRING:
                    creator.addButtonControl(groupId, 
                                             commandClassId, 
                                             valueLabel, 
                                             "boolean", 
                                             valueLabel,
                                             "true", 
                                             false, 
                                             0, 
                                             "", 
                                             customData);
                break;
                case BOOL:
                        ToggleDataBuilder toggleData = new ToggleDataBuilder();
                        toggleData.setOnData("On", "1");
                        toggleData.setOnData("Off", "0");
                        creator.addToggleControl(groupId,
                                          commandClassId,
                                          valueLabel,
                                          "boolean",
                                          false,
                                          false,
                                          0,
                                          "",
                                          toggleData,
                                          customData);
                break;
                case RAW:
                case SCHEDULE:
                case NOT_SUPPORTED:
                default:
                    /// implement later.
                break;
            }
        }
    }
    
    @Override
    public final boolean scanForNewDevices(){
        return true;
    }
    
    @Override
    public final boolean stopScanForNewDevices(){
        return true;
    }
    
    @Override
    public void discoveryEnabled() {
        manager.beginControllerCommand(homeId, ControllerCommand.ADD_DEVICE);
    }

    @Override
    public void discoveryDisabled() {
        manager.cancelControllerCommand(homeId);
    }
    
    
    @Override
    public void handleNewDeviceRequest(WebPresentAddExistingDeviceRequest request) throws PeripheralDriverDeviceMutationException {
        int newDevice = 0;
        Map<String,Object> customData = request.getCustomData();
        if(request.getDeviceId()==-1 && DiscoveredItemsCollection.hasDiscoveredDevice(this, (String)customData.get("address"))){
            try {
                DiscoveredDevice device = DiscoveredItemsCollection.getDiscoveredDevice(this, (String)customData.get("address"));
                if(!request.getDeviceSkeletonName().equals("")){
                    device.getDeviceStructureCreator().setName(request.getDeviceSkeletonName());
                }
                newDevice = this.createDeviceSkeleton(device);
            } catch (DiscoveredDeviceNotFoundException ex) {
                LOG.error("Discovered device with address '{}' not found", customData.get("address"));
                throw new PeripheralDriverDeviceMutationException(ex.getMessage());
            }
        } else {
            newDevice = request.getDeviceId();
        }
        if(newDevice!=0 && DiscoveredItemsCollection.hasDiscoveredDevice(this, (String)customData.get("address"))){
            String localName = request.getName();
            if(localName.equals("")){
                localName = "Unknown device";
            }
            this.createFromExistingDevice(newDevice, localName, (String)customData.get("address"), request.getLocationId(), request.getCategoryId());
            DiscoveredItemsCollection.removeDiscoveredDevice(this, (String)customData.get("address"));
        } else {
            throw new PeripheralDriverDeviceMutationException("Incorrect new device selected/Device type creation failed.");
        }
    }

    @Override
    public void handleCustomFunctionRequest(WebPresentCustomFunctionRequest wpcfr) throws Exception {
        LOG.info("Received web identifier: {}", wpcfr.getIdentifier());
        switch(wpcfr.getIdentifier()){
            case "removeNode":
                manager.beginControllerCommand(homeId, ControllerCommand.REMOVE_DEVICE);
            break;
            case "cancelRemoveNode":
               manager.cancelControllerCommand(homeId);
            break;
        }
    }
    
    private class NotififactionHandler extends Thread {
    
        private BlockingQueue<Notification> pickupQueue;
        
        private NotififactionHandler(BlockingQueue<Notification> pickupQueue){
            this.pickupQueue = pickupQueue;
            Thread.currentThread().setName("ZWaveNotificationHandler");
        }
        
        @Override
        public final void run(){
            while (run) {
                try {
                    final Notification notification = pickupQueue.take();
                    switch (notification.getType()) {
                        case NOTIFICATION:
                            LOG.info("Handling notification code {} of type: {}", notification.getNotification(), notification.getType());
                            if(notification.getNotification() == NotificationCode.DEAD){
                                String addr = String.valueOf(notification.getNodeId());
                                for(Device device : NativeZWaveDriver.this.getRunningDevices()){
                                    if(device.getAddress().equals(addr)){
                                        try {
                                            device.setDeviceStatus(Device.DeviceStatus.DEAD, "Node does not respond.");
                                        } catch (UnsupportedDeviceStatusException ex) {
                                            LOG.error("Could not mark node {} as dead", addr);
                                        }
                                        break;
                                    }
                                }
                            }
                            if(notification.getNotification() == NotificationCode.ALIVE){
                                String addr = String.valueOf(notification.getNodeId());
                                for(Device device : NativeZWaveDriver.this.getRunningDevices()){
                                    if(device.getAddress().equals(addr)){
                                        device.setDeviceStatusOk();
                                        break;
                                    }
                                }
                            }
                        break;
                        case NODE_ADDED:
                            addNewFoundDevice(notification);
                        break;
                        case NODE_QUERIES_COMPLETE:
                        ///case ESSENTIAL_NODE_QUERIES_COMPLETE:
                            if(DiscoveredItemsCollection.discoveryEnabled(NativeZWaveDriver.this)){
                                DiscoveredItemsCollection.signalDevicesScanDone(NativeZWaveDriver.this);
                                try {
                                    LOG.info("Got device struct: {}", DiscoveredItemsCollection.getDiscoveredDevice(NativeZWaveDriver.this, String.valueOf(notification.getNodeId())).getDeviceStructureCreator().getCollection());
                                } catch (DiscoveredDeviceNotFoundException ex) {
                                    LOG.error("No discovered device found with given id");
                                } catch (Exception ex) {
                                    LOG.error("Problem creating struct: {}", ex.getMessage());
                                }
                            }
                        break;
                        case NODE_PROTOCOL_INFO:
                        case NODE_NAMING:
                            if(DiscoveredItemsCollection.discoveryEnabled(NativeZWaveDriver.this)){
                                addNodeInformation(notification);
                            }
                        break;
                        case VALUE_ADDED:
                            if(DiscoveredItemsCollection.discoveryEnabled(NativeZWaveDriver.this)){
                                addNodeInformation(notification);
                            }
                            LOG.info(String.format("Value added\n"
                                    + "\tnode id: %d\n"
                                    + "\tcommand class: %d\n"
                                    + "\tinstance: %d\n"
                                    + "\tindex: %d\n"
                                    + "\tgenre: %s\n"
                                    + "\ttype: %s\n"
                                    + "\tlabel: %s\n"
                                    + "\tvalue: %s",
                                    notification.getNodeId(),
                                    notification.getValueId().getCommandClassId(),
                                    notification.getValueId().getInstance(),
                                    notification.getValueId().getIndex(),
                                    notification.getValueId().getGenre().name(),
                                    notification.getValueId().getType().name(),
                                    manager.getValueLabel(notification.getValueId()),
                                    ZWaveDataHelper.getValue(notification.getValueId())
                            ));
                        break;
                        case VALUE_CHANGED:
                            LOG.info(String.format("Value changed\n"
                                    + "\tnode id: %d\n"
                                    + "\tcommand class: %d\n"
                                    + "\tinstance: %d\n"
                                    + "\tindex: %d\n"
                                    + "\tvalue: %s",
                                    notification.getNodeId(),
                                    notification.getValueId().getCommandClassId(),
                                    notification.getValueId().getInstance(),
                                    notification.getValueId().getIndex(),
                                    ZWaveDataHelper.getValue(notification.getValueId())
                            ));
                            break;
                        case VALUE_REFRESHED:
                            LOG.info(String.format("Value refreshed\n"
                                    + "\tnode id: %d\n"
                                    + "\tcommand class: %d\n"
                                    + "\tinstance: %d\n"
                                    + "\tindex: %d"
                                    + "\tvalue: %s",
                                    notification.getNodeId(),
                                    notification.getValueId().getCommandClassId(),
                                    notification.getValueId().getInstance(),
                                    notification.getValueId().getIndex(),
                                    ZWaveDataHelper.getValue(notification.getValueId())
                            ));
                            break;
                    }
                } catch (InterruptedException ex) {
                    LOG.error("ZWaveDriver message handler stopped: {}", ex.getMessage());
                }
            }
        }
        
    }
    
    @Override
    public final void driverStop() {
        zwaveThread.intentialStop();
        if(notififactionHandler!=null){
            notififactionHandler.interrupt();
            notififactionHandler = null;
        }
        if(incommingNewNotificationQueue!=null){
            incommingNewNotificationQueue.clear();
        }
        try {
            messageQueue.put(new ZwaveOutgoingMessage(homeId,null,null).done());
        } catch (InterruptedException ex) {
            LOG.error("Could not pass ZWave finalizer to queue: {}", ex.getMessage());
        }
    }

    private final class ZwaveThread extends Thread {

        private final String linkedThreadName;

        private Options options;

        final BlockingQueue<ZwaveOutgoingMessage> pickupQueue;
        
        private ZwaveThread(BlockingQueue<ZwaveOutgoingMessage> queue) {
            this.pickupQueue = queue;
            this.linkedThreadName = "ZWaveHandlerThread";
        }

        protected final void intentialStop() {
            run = false;
        }
        
        @Override
        public final void run() {
            Thread.currentThread().setName(linkedThreadName);
            Thread.currentThread().setContextClassLoader(ZWave4j.class.getClassLoader());

            NativeLibraryLoader.loadLibrary(ZWave4j.LIBRARY_NAME, ZWave4j.class);
            
            dataListener = new ZWaveIncomingDataHandler();
            
            options = Options.create(new File("packages/user/pidome-zwave/config").getAbsolutePath(), new File("packages/user/pidome-zwave/config/userData/").getAbsolutePath(), "");
            options.addOptionBool("ConsoleOutput", false);
            options.addOptionInt("DriverMaxAttempts", 1);
            options.addOptionBool("SaveConfiguration", true);
            options.addOptionBool("Logging", false);
            options.addOptionInt("SaveLogLevel", 9);
            options.addOptionInt("QueueLogLevel", 9);
            options.lock();

            LOG.info("Starting Z-Wave handler");
            manager = Manager.create();
            LOG.info("Setting Z-Wave listener");
            manager.addWatcher(dataListener, null);
            LOG.info("Starting Z-Wave driver");
            manager.addDriver(peripheralPort);
            LOG.info("Z-Wave driver started");
            while (run) {
                try {
                    final ZwaveOutgoingMessage message = pickupQueue.take();
                    if(message.exit()){
                        pickupQueue.clear();
                        break;
                    } else {
                        final ValueId vId = message.getValueId();
                        LOG.info("Message data to send: \n"
                                    + "\tnode id: {}\n"
                                    + "\tcommand class: {}\n"
                                    + "\tinstance: {}\n"
                                    + "\tindex: {}\n"
                                    + "\tgenre: {}\n"
                                    + "\ttype: {}\n"
                                    + "\tlabel: {}\n"
                                    + "\tvalue: {}",
                                    vId.getNodeId(),
                                    vId.getCommandClassId(),
                                    vId.getInstance(),
                                    vId.getIndex(),
                                    vId.getGenre().name(),
                                    vId.getType().name(),
                                    manager.getValueLabel(vId),
                                    message.getObjectPayload()
                            );
                        switch(vId.getType()){
                            case BYTE:
                                manager.setValueAsByte(vId, message.getBytePayload());
                            break;
                            case STRING:
                                manager.setValueAsString(vId, message.getStringPayload());
                            break;
                            case BOOL:
                                manager.setValueAsBool(vId, message.getBooleanPayload());
                            break;
                            case DECIMAL:
                                manager.setValueAsFloat(vId, message.getFloatPayload());
                            break;
                            case INT:
                                manager.setValueAsInt(vId, message.getIntegerPayload());
                            break;
                            case LIST:
                                manager.setValueListSelection(vId, message.getStringPayload());
                            break;
                            case SHORT:
                                manager.setValueAsShort(vId, message.getShortPayload());
                            break;
                            case BUTTON:
                                manager.pressButton(vId);
                            break;
                            case RAW:
                            case SCHEDULE:
                                manager.setValueAsRaw(vId, message.getShortArrayPayload());
                            break;
                            case NOT_SUPPORTED:
                                LOG.warn("Unsupported requested. Message data\n"
                                    + "\tnode id: {}\n"
                                    + "\tcommand class: {}\n"
                                    + "\tinstance: {}\n"
                                    + "\tindex: {}\n"
                                    + "\tgenre: {}\n"
                                    + "\ttype: {}\n"
                                    + "\tlabel: {}\n"
                                    + "\tvalue: {}",
                                    vId.getNodeId(),
                                    vId.getCommandClassId(),
                                    vId.getInstance(),
                                    vId.getIndex(),
                                    vId.getGenre().name(),
                                    vId.getType().name(),
                                    manager.getValueLabel(vId),
                                    ZWaveDataHelper.getValue(vId)
                            );
                            break;
                        }
                    }
                } catch (InterruptedException ex) {
                    if (run) {
                        LOG.warn("Possible unintentional wake up, loopback...");
                    }
                }
            }
            LOG.info("Stopping Z-Wave");
            manager.removeWatcher(dataListener, null);
            manager.removeDriver(peripheralPort);
            Manager.destroy();
            Options.destroy();
            LOG.info("Stopped Z-Wave");
        }
    };
    
    final class ZWaveIncomingDataHandler implements NotificationWatcher {

        @Override
        public void onNotification(Notification notification, Object context) {
            LOG.info("Got {} width code: {}", notification.getType(), notification.getNotification());
            switch (notification.getType()) {
                case DRIVER_READY:
                    LOG.info(String.format("Driver ready\n"
                            + "\thome id: %d",
                            notification.getHomeId()
                    ));
                    homeId = notification.getHomeId();
                    break;
                case DRIVER_FAILED:
                    LOG.info("Driver failed");
                    break;
                case DRIVER_RESET:
                    LOG.info("Driver reset");
                    break;
                case AWAKE_NODES_QUERIED:
                    LOG.info("Awake nodes queried");
                    break;
                case ALL_NODES_QUERIED:
                    LOG.info("All nodes queried");
                    manager.writeConfig(homeId);
                    //ready = true;
                    break;
                case ALL_NODES_QUERIED_SOME_DEAD:
                    LOG.info("All nodes queried some dead");
                    manager.writeConfig(homeId);
                    //ready = true;
                    break;
                case POLLING_ENABLED:
                    LOG.info("Polling enabled");
                    break;
                case POLLING_DISABLED:
                    LOG.info("Polling disabled");
                    break;
                case NODE_REMOVED:
                    LOG.info(String.format("Node removed\n"
                            + "\tnode id: %d",
                            notification.getNodeId()
                    ));
                    break;
                case NODE_EVENT:
                    LOG.info(String.format("Node event\n"
                            + "\tnode id: %d\n"
                            + "\tevent id: %d",
                            notification.getNodeId(),
                            notification.getEvent()
                    ));
                    break;
                case VALUE_REMOVED:
                    LOG.info(String.format("Value removed\n"
                            + "\tnode id: %d\n"
                            + "\tcommand class: %d\n"
                            + "\tinstance: %d\n"
                            + "\tindex: %d",
                            notification.getNodeId(),
                            notification.getValueId().getCommandClassId(),
                            notification.getValueId().getInstance(),
                            notification.getValueId().getIndex()
                    ));
                    break;
                case GROUP:
                    LOG.info(String.format("Group\n"
                            + "\tnode id: %d\n"
                            + "\tgroup id: %d",
                            notification.getNodeId(),
                            notification.getGroupIdx()
                    ));
                    break;

                case SCENE_EVENT:
                    LOG.info(String.format("Scene event\n"
                            + "\tscene id: %d",
                            notification.getSceneId()
                    ));
                    break;
                case CREATE_BUTTON:
                    LOG.info(String.format("Button create\n"
                            + "\tbutton id: %d",
                            notification.getButtonId()
                    ));
                    break;
                case DELETE_BUTTON:
                    LOG.info(String.format("Button delete\n"
                            + "\tbutton id: %d",
                            notification.getButtonId()
                    ));
                    break;
                case BUTTON_ON:
                    LOG.info(String.format("Button on\n"
                            + "\tbutton id: %d",
                            notification.getButtonId()
                    ));
                    break;
                case BUTTON_OFF:
                    LOG.info(String.format("Button off\n"
                            + "\tbutton id: %d",
                            notification.getButtonId()
                    ));
                    break;
                default:
                    LOG.info(notification.getType().name());
                    try {
                        incommingNewNotificationQueue.put(notification);
                    } catch (InterruptedException ex) {
                        LOG.error("Unable to pass notification to message queue: {}", ex.getMessage());
                    }
                    break;
            }
        }

    }

}
