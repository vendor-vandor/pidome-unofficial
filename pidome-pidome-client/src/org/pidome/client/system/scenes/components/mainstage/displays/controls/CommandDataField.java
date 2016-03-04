/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays.controls;

import java.util.ArrayList;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeEvent;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeListener;
import org.pidome.client.system.scenes.ComponentDimensions;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;
import org.pidome.client.system.scenes.components.mainstage.applicationsbar.ApplicationsBarWidgetIcons;
import org.pidome.client.system.scenes.components.mainstage.applicationsbar.ApplicationsBarWidgetIconsException;
import org.pidome.client.system.scenes.components.mainstage.applicationsbar.widgeticons.hasWidgetIconInterface;
import org.pidome.client.system.scenes.components.mainstage.displays.DeviceGraphWindow;
import org.pidome.client.system.scenes.components.mainstage.displays.components.DragDropLinkPane;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 *
 * @author John Sirach
 */
public class CommandDataField extends DeviceCmd implements DeviceValueChangeListener {

    static Logger LOG = LogManager.getLogger(CommandDataField.class);
    
    ComponentDimensions dimensions = new ComponentDimensions();
    double width;
    double height;
    
    Device device;
    
    boolean serverData;
    
    String prefix = "";
    String suffix = "";
    
    HBox interfaceButton;
    
    localDataField dragDroppable = new localDataField();
    
    private boolean onlyShortcut = false;
    
    public CommandDataField(Device device){
        this.device = device;
    }
    
    public void setSize(double width, double height){
        this.width = width;
        this.height= height;
    }
    
    @Override
    final public HBox getInterface(){
        HBox row = new HBox();
        return row;
    }

    public final void onlyShortcut(boolean value){
        onlyShortcut = value;
    }
    
    @Override
    void build() {
        buildLabel();
    }

    void buildLabel(){
        setAlignment(Pos.CENTER_LEFT);
        interfaceButton = new HBox();
        interfaceButton.setPrefHeight(24*dimensions.heightRatio);
        interfaceButton.setMinHeight(Region.USE_PREF_SIZE);
        interfaceButton.setMaxHeight(Region.USE_PREF_SIZE);
        interfaceButton.getStyleClass().add("DataLabel");
        interfaceButton.setAlignment(Pos.CENTER_LEFT);
        interfaceButton.getChildren().add(dragDroppable);
        String getPrefix = (String)device.getCommandGroups().get(groupName).getSetDetails(setName).get("prefix");
        if(getPrefix!=null){
            prefix = getPrefix;
        }
        String getSuffix = (String)device.getCommandGroups().get(groupName).getSetDetails(setName).get("suffix");
        if(getSuffix!=null){
            suffix = getSuffix;
        }
        dragDroppable.getLabel().setText(prefix + device.getLastCmd(groupName, setName) + suffix);
        if(device.getCommandGroups().get(groupName).getSetDetails(setName).containsKey("graph") && 
           device.getCommandGroups().get(groupName).getSetDetails(setName).get("graph").equals("true")){
            if(!onlyShortcut){
                ImageView graphView = new ImageView(new ImageLoader("controls/graphicon.png", 24,24).getImage());
                HBox.setMargin(graphView, new Insets(0,1,0,0));
                /*
                List<String>shortcutOptions = new ArrayList();
                shortcutOptions.add(String.valueOf(device.getId()));
                shortcutOptions.add(groupName);
                shortcutOptions.add(setName);
                NewDesktopShortcut creator = new NewDesktopShortcut(graphView);
                creator.setIconType(DesktopIcon.ICON_GRAPH);
                creator.setTitle(device.getName() + " " + device.getCommandGroups().get(groupName).getSetDetails(setName).get("label").toLowerCase() + " graph");
                creator.setClassInitiator("org.pidome.client.system.scenes.components.mainstage.displays.DataGraph", shortcutOptions);
                */
                graphView.setOnMouseClicked((MouseEvent t) -> {
                    DeviceGraphWindow graphObject;
                    try {
                        graphObject = new DeviceGraphWindow(String.valueOf(device.getId()), groupName + "_" + setName);
                        WindowManager.openWindow(graphObject, t.getSceneX(), t.getSceneY());
                    } catch (Exception ex) {
                        LOG.error("Could not open graph for: {} set: {}", device.getName(), setName);
                    }
                });
                HBox.setMargin(graphView, new Insets(0,5,0,0));
                StackPane img = new StackPane();
                img.getChildren().add(graphView);
                img.setAlignment(Pos.CENTER_RIGHT);
                HBox.setHgrow(img, Priority.ALWAYS);
                interfaceButton.getChildren().add(img);
            }
        }
        device.addDeviceValueEventListener(this, groupName, setName);
    }
    
    @Override
    public void handleDeviceValueChange(DeviceValueChangeEvent event) {
        switch(event.getEventType()){
            case DeviceValueChangeEvent.VALUECHANGED:
                String eventSet  = event.getSet();
                final Object eventValue= event.getValue();
                LOG.debug("Received: {}, data: {}, {}", DeviceValueChangeEvent.VALUECHANGED, eventSet, eventValue);
                if(eventSet.equals(setName)){
                    Platform.runLater(() -> {
                        dragDroppable.getLabel().setText(prefix + eventValue + suffix);
                    });
                }
                serverData = false;
            break;
        }
    }

    final public HBox getButton(){
        return interfaceButton;
    }
    
    final class localDataField extends DragDropLinkPane implements hasWidgetIconInterface {

        Label currenDataLabel = new Label();
        
        public localDataField(){
            getChildren().add(currenDataLabel);
        }
        
        final Label getLabel(){
            return currenDataLabel;
        }
        
        @Override
        public void dragDropDone(Object source) {
            String widgetPath = "org.pidome.client.system.scenes.components.mainstage.applicationsbar.widgeticons.DeviceDataWidgetIcon";
            ArrayList<String> widgetItem = new ArrayList<>();
            widgetItem.add(String.valueOf(device.getId()));
            widgetItem.add(groupName);
            widgetItem.add(setName);
            try {
                ((ApplicationsBarWidgetIcons)source).createIcon(((ApplicationsBarWidgetIcons)source).getPosition(), widgetPath, widgetItem);
            } catch (ApplicationsBarWidgetIconsException ex) {
                System.out.println("Could not create widget icon: " + ex.getMessage());
            }
        }
        
    }
    
    @Override
    public final void removeListener(){
        device.removeDeviceValueEventListener(this, groupName, setName);
    }
    
}
