/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.desktop;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.pidome.client.system.scenes.windows.SimpleErrorMessage;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 *
 * @author John Sirach
 */
public abstract class DesktopBase implements DraggableIconInterface {

    final static List<DesktopIcon> icons = new ArrayList();
    final static ObservableList<DesktopIcon> iconList = FXCollections.observableList(icons);
    
    public DesktopBase(){
        createIconListListener();
    }
    
    final void createIconListListener(){
        iconList.addListener((ListChangeListener.Change<? extends DesktopIcon> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().stream().forEach((icon) -> {
                        addIcon(icon);
                    });
                } else if (change.wasRemoved()) {
                    change.getRemoved().stream().forEach((icon) -> {
                        removeIcon(icon);
                    });
                }
            }
        });
    }
    
    public static void addDesktopIcon(DesktopIcon icon){
        if(!iconList.contains(icon)){
            icon.createIcon();
            iconList.add(icon);
        }
    }

    public static void removeDesktopIcon(DesktopIcon icon){
        if(iconList.contains(icon)) {
            icon.destroy();
            iconList.remove(icon);
        }
    }
    
    abstract void addIcon(DesktopIcon icon);
    abstract void removeIcon(DesktopIcon icon);
    
    final void addDefaultIcons(){
        DesktopIcon aboutIcon = new DesktopIcon(this,DesktopIcon.ITEM, "Global info/about", "org.pidome.client.system.scenes.components.mainstage.displays.SystemInfo", new ArrayList());
        addDesktopIcon(aboutIcon);
        ArrayList deviceData = new ArrayList();
        deviceData.add("1");
        DesktopIcon serverIcon = new DesktopIcon(this,DesktopIcon.DEVICE, "Server info", "org.pidome.client.system.scenes.components.mainstage.displays.DeviceDisplay", deviceData);
        serverIcon.setIcon("PIDOME");
        addDesktopIcon(serverIcon);
        DesktopIcon visualMap = new DesktopIcon(this,DesktopIcon.ITEM, "Floors map", "org.pidome.client.system.scenes.components.mainstage.displays.visualfloor.VisualFloors", new ArrayList());
        addDesktopIcon(visualMap);
        DesktopIcon devicesIcon = new DesktopIcon(this,DesktopIcon.FOLDER, "Device list", "org.pidome.client.system.scenes.components.mainstage.displays.DevicesDisplay", new ArrayList());
        addDesktopIcon(devicesIcon);
        DesktopIcon marosIcon = new DesktopIcon(this,DesktopIcon.FOLDER, "Macro list", "org.pidome.client.system.scenes.components.mainstage.displays.MacrosDisplay", new ArrayList());
        addDesktopIcon(marosIcon);
        DesktopIcon mediaIcon = new DesktopIcon(this,DesktopIcon.FOLDER, "Media list", "org.pidome.client.system.scenes.components.mainstage.displays.MediaListing", new ArrayList());
        addDesktopIcon(mediaIcon);
        DesktopIcon remotesIcon = new DesktopIcon(this,DesktopIcon.FOLDER, "Remotes list", "org.pidome.client.system.scenes.components.mainstage.displays.RemotesListDisplay", new ArrayList());
        addDesktopIcon(remotesIcon);
    }
    
    @Override
    public final void iconAdded(){
        //// not used for the desktop.
    }
    
    @Override
    public final void iconRemoved(){
        SimpleErrorMessage message = new SimpleErrorMessage("Error icon delete");
        message.setMessage("Can not delete a default icon");
        WindowManager.openWindow(message);
    }
    
}
