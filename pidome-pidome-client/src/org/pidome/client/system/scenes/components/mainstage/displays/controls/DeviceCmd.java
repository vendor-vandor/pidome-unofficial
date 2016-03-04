/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays.controls;

import java.util.Map;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

/**
 *
 * @author John Sirach
 */
public abstract class DeviceCmd extends HBox {
    
    String setName;
    String setLabel;
    String setDescription;
    String groupName;
    
    Map<String,Map<String,Object>> cmdSet;
    
    public final void setSetName(String setName){
        this.setName = setName;
    }
    
    public final void setSetLabel(String label){
        this.setLabel = label;
    }

    public final void setDescription(String description){
        this.setDescription = description;
    }
    
    public final void setGroupName(String groupName){
        this.groupName = groupName;
    }
    
    public final void setSet(Map<String,Map<String,Object>> cmdSet){
        this.cmdSet = cmdSet;
        build();
    }
    
    public abstract Node getInterface();
    
    abstract void build();
    
    public abstract void removeListener();
    
}