/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays.controls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.scenes.components.mainstage.desktop.DesktopIcon;
import org.pidome.client.system.scenes.components.mainstage.desktop.NewDesktopShortcut;
import org.pidome.client.system.scenes.components.mainstage.displays.DevicesDisplay;
import org.pidome.client.system.scenes.components.mainstage.displays.components.DeviceContentPane;

/**
 * creates the components used by devices.
 * @author John Sirach
 */
public class DeviceComponents extends HBox {

    static Logger LOG = LogManager.getLogger(DeviceComponents.class);
    
    Device device;
    double deviceLabelWidth;
    double buttonContainerWidth;
    
    Label deviceName;
    int category;
    int location;
    
    final Map<String,Device.CommandGroup> cmdGroup;
    
    final Map<String,GroupCollection> colGroups = new HashMap<>();
     
    double height = 45 * DisplayConfig.getWidthRatio();
    
    DevicesDisplay parent;
    
    StackPane fullDeviceDetails;
    
    StackPane deviceLabel = new StackPane();
    
    List<Object> reDrawables = new ArrayList();
    Map<Integer,Node> drawList = new TreeMap<>();
    
    List<DeviceCmd> buttonList = new ArrayList();
    
    boolean onlyShortcuts = false;
    
    /**
     * Constructor.
     * @param parent
     * @param device 
     */
    public DeviceComponents(DevicesDisplay parent, Device device){
        super(15*DisplayConfig.getWidthRatio());
        this.device = device;
        this.parent = parent;
        
        setAlignment(Pos.CENTER_LEFT);
        
        category = this.device.getCategory();
        location = this.device.getLocation();
        
        deviceLabelWidth = 300 * DisplayConfig.getWidthRatio();
        buttonContainerWidth = 385 * DisplayConfig.getWidthRatio();
        
        cmdGroup = this.device.getCommandGroups();
        
    }
    
    public final void onlyShortcuts(boolean value){
        onlyShortcuts = value;
    }
    
    /**
     * Returns the device
     * @return 
     */
    public final Device getDevice(){
        return this.device;
    }
    
    /**
     * Returns the device label which is part of a component.
     * @return 
     */
    public final String getLabel(){
        return deviceName.getText();
    }
    
    /**
     * Returns the category.
     * @return 
     */
    public final int getCategory(){
        return category;
    }
    
    /**
     * Updates a category.
     * @param category 
     */
    public final void updateCategory(int category){
        this.category = category;
    }
    
    /**
     * Updates a label.
     * @param labelName 
     */
    public final void updateLabel(final String labelName){
        deviceName.setText(labelName);
    }
    
    /**
     * Returns the device id.
     * @return 
     */
    public final int getDeviceId(){
        return this.device.getId();
    }
    
    /**
     * Creates every initial component.
     */
    public final void create(){
        getChildren().add(getDeviceLabel());
        for(String id: cmdGroup.keySet()){
            Map<String,Map<String,Object>> setDetails = cmdGroup.get(id).getFullSetList();
            GroupCollection grpCol = new GroupCollection(id, (String)cmdGroup.get(id).getGroupDetails().get("label"));
            for(String setId:setDetails.keySet()){
                LOG.debug("create type button: {}", setDetails.get(setId).get("type"));
                LOG.debug(setDetails.get(setId));
                switch((String)setDetails.get(setId).get("type")){
                    case "button":
                        CommandButtonSet cmdButton = new CommandButtonSet(device);
                        cmdButton.setGroupName(id);
                        cmdButton.setSetName(setId);
                        cmdButton.setSetLabel((String)cmdGroup.get(id).getSetDetails(setId).get("label"));
                        cmdButton.setSet(cmdGroup.get(id).getCommandSet(setId));
                        grpCol.addSetCmd(setId, (String)setDetails.get(setId).get("label"), cmdButton);
                        Button pushMe = cmdButton.getButton();
                        pushMe.setFocusTraversable(false);
                        if(setDetails.get(setId).containsKey("shortcut")){
                            drawList.put(Integer.parseInt(setDetails.get(setId).get("shortcut").toString()), pushMe);
                        }
                        buttonList.add(cmdButton);
                    break;
                    case "select":
                        CommandSelectSet cmdSet = new CommandSelectSet(parent, device);
                        cmdSet.setGroupName(id);
                        cmdSet.setSetName(setId);
                        cmdSet.setSetLabel((String)cmdGroup.get(id).getSetDetails(setId).get("label"));
                        cmdSet.setSet(cmdGroup.get(id).getCommandSet(setId));
                        grpCol.addSetCmd(setId, (String)setDetails.get(setId).get("label"), cmdSet);
                        Button selectMe = cmdSet.getButton();
                        selectMe.setFocusTraversable(false);
                        if(setDetails.get(setId).containsKey("shortcut")){
                            drawList.put(Integer.parseInt(setDetails.get(setId).get("shortcut").toString()), selectMe);
                        }
                        buttonList.add(cmdSet);
                    break;
                    case "toggle":
                        CommandToggleButton cmdToggle = new CommandToggleButton(device);
                        cmdToggle.setGroupName(id);
                        cmdToggle.setSetName(setId);
                        cmdToggle.setSet(cmdGroup.get(id).getCommandSet(setId));
                        cmdToggle.setSetLabel((String)cmdGroup.get(id).getSetDetails(setId).get("label"));
                        grpCol.addSetCmd(setId, (String)cmdGroup.get(id).getSetDetails(setId).get("label"), cmdToggle);
                        VBox toggleMe = cmdToggle.getButton();
                        if(setDetails.get(setId).containsKey("shortcut")){
                            drawList.put(Integer.parseInt(setDetails.get(setId).get("shortcut").toString()), toggleMe);
                        }
                        buttonList.add(cmdToggle);
                    break;
                    case "data":
                        CommandDataField cmdData = new CommandDataField(device);
                        cmdData.onlyShortcut(onlyShortcuts);
                        //cmdData.setSize(buttonContainerWidth/4,height-1);
                        cmdData.setGroupName(id);
                        cmdData.setSetName(setId);
                        cmdData.setSet(cmdGroup.get(id).getCommandSet(setId));
                        cmdData.setSetLabel((String)cmdGroup.get(id).getSetDetails(setId).get("label"));
                        grpCol.addSetCmd(setId, (String)cmdGroup.get(id).getSetDetails(setId).get("label"), cmdData);
                        HBox inputLabel = cmdData.getButton();
                        if(setDetails.get(setId).containsKey("shortcut")){
                            drawList.put(Integer.parseInt(setDetails.get(setId).get("shortcut").toString()), inputLabel);
                        }
                        buttonList.add(cmdData);
                    break;
                    case "colorpicker":
                        CommandColorPicker cmdCPicker = new CommandColorPicker(parent,device);
                        cmdCPicker.setSize(buttonContainerWidth/4,height-1);
                        cmdCPicker.setGroupName(id);
                        cmdCPicker.setSetName(setId);
                        cmdCPicker.setSet(cmdGroup.get(id).getCommandSet(setId));
                        cmdCPicker.setSetLabel((String)cmdGroup.get(id).getSetDetails(setId).get("label"));
                        grpCol.addSetCmd(setId, (String)cmdGroup.get(id).getSetDetails(setId).get("label"), cmdCPicker);
                        HBox colorPicker = cmdCPicker.getButton();
                        if(setDetails.get(setId).containsKey("shortcut")){
                            drawList.put(Integer.parseInt(setDetails.get(setId).get("shortcut").toString()), colorPicker);
                        }
                        buttonList.add(cmdCPicker);
                    break;
                }
            }
            colGroups.put(id, grpCol);
        }
        drawList.entrySet().stream().forEach((entry) -> {
            getChildren().add(entry.getValue());
        });
        
    }
    
    /**
     * If a a component list needs to be redrawn call this function and they will be re-rendered.
     */
    public void redraw(){
        drawList.entrySet().stream().forEach((entry) -> {
            getChildren().remove(entry.getValue());
        });
        drawList.entrySet().stream().forEach((entry) -> {
            getChildren().add(entry.getValue());
        });
    }
    
    /**
     * Returns the device label.
     * @return 
     */
    StackPane getDeviceLabel(){
        deviceLabel.setAlignment(Pos.CENTER_LEFT);
        deviceLabel.setPrefSize(deviceLabelWidth, height);
        deviceLabel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        deviceLabel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        deviceName = new Label(device.getName());
        deviceLabel.getChildren().add(deviceName);
        
        Map<String,Object> shortcutOptions = new HashMap<>();
        shortcutOptions.put("id", this.device.getId());
        shortcutOptions.put("favorite", true);
        NewDesktopShortcut creator = new NewDesktopShortcut(deviceName);
        creator.setServerCall("DeviceService.setFavorite", shortcutOptions);
        creator.setIconType(DesktopIcon.DEVICE);
        
        deviceLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, this::showDeviceContentHelper);
        return deviceLabel;
    }

    final void showDeviceContentHelper(MouseEvent e){
        DeviceContentPane deviceDisplay = new DeviceContentPane(this.parent,this.device);
        this.parent.setDeviceContent(deviceDisplay);
    }
    
    /**
     * Removes listeners.
     */
    public final void removeListeners(){
        buttonList.stream().forEach((button) -> {
            button.removeListener();
        });
        deviceLabel.removeEventHandler(MouseEvent.MOUSE_CLICKED, this::showDeviceContentHelper);
    }
    
    /**
     * Helper class for grouping the device components.
     */
    final class GroupCollection {
        
        String groupDescription;
        String groupId;
        Map<String,String>setDescriptions = new HashMap<>();
        Map<String,List<DeviceCmd>> groupSets = new HashMap<>();
        
        /**
         * Constructor.
         * @param groupId
         * @param description 
         */
        public GroupCollection(String groupId, String description){
            this.groupDescription = description;
            this.groupId = groupId;
        }
        
        /**
         * Returns the group description.
         * @return 
         */
        public final String getDescription(){
            return groupDescription;
        }
        
        /**
         * Returns the description for a particular set.
         * @return 
         */
        public final Map<String,String> getSetDescriptions(){
            return setDescriptions;
        }
        
        /**
         * Returns the set's command.
         * @param id
         * @return 
         */
        public final DeviceCmd getSetCmd(String id){
            return groupSets.get(id).get(0);
        }
        
        /**
         * Adds a command.
         * @param id
         * @param description
         * @param DeviceCmds 
         */
        public final void addSetCmd(String id, String description, DeviceCmd DeviceCmds){
            setDescriptions.put(id, description);
            if(groupSets.containsKey(id)){
                groupSets.get(id).add(DeviceCmds);
            } else {
                List<DeviceCmd> cmdList = new ArrayList();
                cmdList.add(DeviceCmds);
                groupSets.put(id, cmdList);
            }
        }
        
    }
        
}