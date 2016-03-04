/*
 * Copyright 2014 John.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.client.system.scenes.components.mainstage.applicationsbar;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.AppProperties;
import org.pidome.client.config.AppPropertiesException;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.MainScene;
import org.pidome.client.system.scenes.MainSceneEvent;
import org.pidome.client.system.scenes.MainSceneEventListener;
import org.pidome.client.system.scenes.components.mainstage.applicationsbar.widgeticons.ApplicationsBarWidgetIcon;
import org.pidome.client.system.scenes.components.mainstage.applicationsbar.widgeticons.DraggableApplicationbarWidgetIcon;
import org.pidome.client.system.scenes.components.mainstage.applicationsbar.widgeticons.hasWidgetIconInterface;
import org.pidome.client.system.scenes.windows.SimpleQuickNotificationMessage;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 *
 * @author John
 */
public class ApplicationsBarWidgetIcons extends HBox implements MainSceneEventListener {


    @Override
    public void handleMainSceneEvent(MainSceneEvent event) {
        switch(event.getEventType()){
            case MainSceneEvent.SCENEBUILDDONE:
                loadInitialIconSet();
            break;
        }
    }

    public enum Position {
        LEFT,RIGHT;
    }
    
    Position barPosition;
    
    int items;
    static int maxItems = 4;
    
    final List<ApplicationsBarWidgetIcon> icons = new ArrayList();
    final ObservableList<ApplicationsBarWidgetIcon> iconList = FXCollections.observableList(icons);
    
    Map<Integer,Map<String,Object>> iconsSet = new TreeMap<>();
    
    static Logger LOG = LogManager.getLogger(ApplicationsBarWidgetIcons.class);
    
    static Map<Position,ApplicationsBarWidgetIcons> instances = new HashMap<>();
    
    public static ApplicationsBarWidgetIcons getInstance(Position pos){
        if(instances.containsKey(pos)){
            return instances.get(pos);
        } else {
            ApplicationsBarWidgetIcons instance = new ApplicationsBarWidgetIcons(pos);
            instances.put(pos, instance);
            return instance;
        }
    }
    
    public final Position getPosition(){
        return this.barPosition;
    }
    
    private ApplicationsBarWidgetIcons(Position pos){
        super(4);
        barPosition = pos;
        switch(pos){
            case LEFT:
                setAlignment(Pos.CENTER_LEFT);
            break;
            case RIGHT:
                setAlignment(Pos.CENTER_RIGHT);
            break;
        }
        setMinSize(226*DisplayConfig.getWidthRatio(),46*DisplayConfig.getHeightRatio());
        setMaxSize(226*DisplayConfig.getWidthRatio(),46*DisplayConfig.getHeightRatio());
        setPrefSize(226*DisplayConfig.getWidthRatio(),46*DisplayConfig.getHeightRatio());
        createIconListListener();
        MainScene.addDoneListener(this);
        
        Background noHoverbg = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));
        Background hoverbg = new Background(new BackgroundFill(Color.web("00ff0010"), CornerRadii.EMPTY, Insets.EMPTY));
        Background wrongHoverbg = new Background(new BackgroundFill(Color.web("ff000010"), CornerRadii.EMPTY, Insets.EMPTY));
        
        setOnDragDropped((DragEvent event) -> {
            if (event.getTransferMode().equals(TransferMode.LINK) && event.getGestureSource() instanceof hasWidgetIconInterface) {
                String transferString = event.getDragboard().getString();
                ((hasWidgetIconInterface)event.getGestureSource()).dragDropDone(this);
                event.setDropCompleted(true);
            }
            event.consume();
        });
        setOnDragOver((DragEvent event) -> {
            event.acceptTransferModes(TransferMode.LINK);
            event.consume();
        });
        setOnDragEntered((DragEvent event) -> {
            event.acceptTransferModes(TransferMode.LINK);
            if (event.getGestureSource() instanceof hasWidgetIconInterface) {
                this.setBackground(hoverbg);
            } else {
                this.setBackground(wrongHoverbg);
            }
            event.consume();
        });
        setOnDragExited((DragEvent event) -> {
            this.setBackground(noHoverbg);
            event.consume();
        });
    }
 
    final void createIconListListener(){
        iconList.addListener((ListChangeListener.Change<? extends ApplicationsBarWidgetIcon> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().stream().forEach((icon) -> {
                        HBox.setMargin(icon, new Insets(2*DisplayConfig.getHeightRatio(),0,0,0));
                        Platform.runLater(() -> { getChildren().add(icon); });
                    });
                } else if (change.wasRemoved()) {
                    change.getRemoved().stream().forEach((icon) -> {
                        Platform.runLater(() -> { getChildren().remove(icon); });
                    });
                }
            }
        });
    }
    
    /**
     * Check if a widget icon is already in one of the widget icon sections.
     * @param path
     * @param varList
     * @return 
     */
    public final boolean hasIcon(String path, ArrayList<String> varList) {
        return instances.values().stream().anyMatch((instance) -> (instance.iconsSet.values().stream().anyMatch((iconEntry) -> (iconEntry.get("path").equals(path) && iconEntry.get("vars").equals(varList)))));
    }
    
    public final void createIcon(Position pos, String path, ArrayList<String> varList) throws ApplicationsBarWidgetIconsException {
        if(iconList.size()>=maxItems){
            throw new ApplicationsBarWidgetIconsException("Maximum amount of items reached");
        }
        if(!hasIcon(path,varList)){
            System.out.println("I do not have this icon yet");
            String barSide = barPosition.toString() +".";
            addIcon(path,varList);
            StringBuilder sb = new StringBuilder();
            varList.stream().forEach((s) -> {
                sb.append(s);
                sb.append(",");
            });
            int iconAmount = iconList.size()-1;
            AppProperties.setProperty("applicationswidgetbar", barSide+iconAmount+".path", path);
            AppProperties.setProperty("applicationswidgetbar", barSide+iconAmount+".vars", sb.toString());
            try {
                AppProperties.store("applicationswidgetbar", null);
            } catch (IOException ex) {
                LOG.error("Could not save new icon: " + ex.getMessage());
            }
            Map<String,Object> iconDetails = new HashMap<>();
            iconDetails.put("path", path);
            iconDetails.put("vars", varList);
            iconsSet.put(iconAmount, iconDetails);
        } else {
            SimpleQuickNotificationMessage message = new SimpleQuickNotificationMessage("Widget add error");
            message.setMessage("Widget already has been added");
            WindowManager.openWindow(message);
            throw new ApplicationsBarWidgetIconsException("Item has already been added");
        }
    }

    public final void removeIcon(String path, ArrayList<String> varList) throws ApplicationsBarWidgetIconsException  {
        if(hasIcon(path,varList)){
            String barSide = barPosition.toString() +".";
            System.out.println("Icon has been found");
            int iconKey = getIconIndex(path,varList);
            int origLength = iconsSet.size()-1;
            AppProperties.deleteProperty("applicationswidgetbar", barSide+iconKey+".path");
            AppProperties.deleteProperty("applicationswidgetbar", barSide+iconKey+".vars");
            iconsSet.remove(iconKey);
            /// icons are ordered, so if a removed icon has an index lower then the amount-1 the higher icons should be set index-1.
            try {
                if(iconKey<origLength){
                    for(int curPos = iconKey; curPos < origLength; curPos++){
                        /// Move the settings one position lower
                        AppProperties.setProperty("applicationswidgetbar", barSide+curPos+".path", AppProperties.getProperty("applicationswidgetbar", barSide+(curPos+1)+".path"));
                        AppProperties.setProperty("applicationswidgetbar", barSide+curPos+".vars", AppProperties.getProperty("applicationswidgetbar", barSide+(curPos+1)+".vars"));
                        /// Moce the set one position lower
                        iconsSet.put(curPos, iconsSet.get(curPos+1));
                    }
                    /// remove the last in the index
                    AppProperties.deleteProperty("applicationswidgetbar", barSide+(origLength)+".path");
                    AppProperties.deleteProperty("applicationswidgetbar", barSide+(origLength)+".vars");
                    iconsSet.remove(origLength);
                }
                try {
                    iconList.get(iconKey).done();
                    iconList.remove(iconKey);
                } catch (Exception ex){
                    /// Not present in list, bad loading previous
                }
                try {
                    AppProperties.store("applicationswidgetbar", null);
                } catch (IOException ex) {
                    LOG.error("Could not save new icon set after delete: {}", ex.getMessage());
                }
            } catch(AppPropertiesException ex){
                throw new ApplicationsBarWidgetIconsException("Could not remove, please restart ("+ex.getMessage()+")");
            }
        }
    }
    
    final int getIconIndex(String path, ArrayList<String> varList) throws ApplicationsBarWidgetIconsException {
        for(ApplicationsBarWidgetIcons instance:instances.values()){
            for(Map.Entry<Integer,Map<String,Object>> set:instance.iconsSet.entrySet()){
                if(set.getValue().get("path").equals(path) && set.getValue().get("vars").equals(varList)){
                    return set.getKey();
                }
            }
        }
        throw new ApplicationsBarWidgetIconsException("Icon does not exist");
    }
    
    final void loadInitialIconSet(){
        try {
            Set<Map.Entry<Object,Object>> propertiesSet = AppProperties.getPropertiesNVP("applicationswidgetbar");
            /// We use a treemap so we know the order is always the same
            for (Map.Entry<Object,Object> entry : propertiesSet){
                /// First split out the key in icon enum and icon value type
                /// There is no guarantee all the entries are in order, so create
                /// a map entries containing the correct params for each icon
                String key = (String)entry.getKey();
                String value = (String)entry.getValue();
                
                ///Split to identify the icon number and icon parameter type
                String[] keyPairs = key.split("\\.");
                ///More parameters can exist in this file, we only need the "Position" ones
                if(keyPairs[0].equals(barPosition.toString())){
                    int index = Integer.parseInt(keyPairs[1]);
                    /// Create map if it does not exist
                    if(!iconsSet.containsKey((Integer.parseInt(keyPairs[1])))){
                        Map<String,Object>iconParams = new HashMap<>();
                        iconsSet.put(index, iconParams);
                    }
                    /// check if it is a varargs list, if so split the values, and add them to an ArrayList as needed varargs parameter for the class to be ran.
                    /// else just copy the icon params to the map in the treelist
                    if(keyPairs[2].equals("vars")){
                        ArrayList varList = new ArrayList();
                        String[] varListSplitted = value.split(",");
                        varList.addAll(Arrays.asList(varListSplitted));
                        iconsSet.get(index).put(keyPairs[2], varList);
                    } else {
                        iconsSet.get(index).put(keyPairs[2], value);
                    }
                    iconsSet.get(index).put("index", index);
                }
            }
            Map<String,ArrayList> toRemove = new HashMap<>();
            iconsSet.values().stream().forEach((iconpair) -> {
                try {
                    addIcon((String)iconpair.get("path"),(ArrayList)iconpair.get("vars"));
                } catch (Exception ex){
                    //// Somehting went wrong with adding, keep track of icons to be removed
                    toRemove.put((String)iconpair.get("path"), (ArrayList)iconpair.get("vars"));
                }
            });
            for(Map.Entry<String,ArrayList>remove:toRemove.entrySet()){
                try {
                    this.removeIcon(remove.getKey(), remove.getValue());
                } catch (ApplicationsBarWidgetIconsException ex) {
                    LOG.error("Could not remove icon marked for deletion: {}", ex.getMessage());
                }
            }
        } catch (AppPropertiesException ex) {
            LOG.error("Could not get user stored iconsets: " + ex.getMessage());
        }
    }
    
    final void addIcon(String path, ArrayList<String> varList){
        try {
            Class<DraggableApplicationbarWidgetIcon> iconContentClass = (Class<DraggableApplicationbarWidgetIcon>)Class.forName(path);
            Constructor ctor = iconContentClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            DraggableApplicationbarWidgetIcon iconContent = (DraggableApplicationbarWidgetIcon)ctor.newInstance();
            iconContent.setParams(varList);
            iconContent.build();
            ApplicationsBarWidgetIcon icon = new ApplicationsBarWidgetIcon(this, iconContent);
            icon.setExtendDetails(path, varList);
            iconList.add(icon);
        } catch (Exception ex) {
            LOG.error("Could not add widget icon: {}",path,ex);
            try {
                this.removeIcon(path, varList);
            } catch (ApplicationsBarWidgetIconsException ex1) {
                LOG.error("Could not remove icon after failing to load: {}", ex.getMessage(), ex);
            }
        }
    }
    
}
