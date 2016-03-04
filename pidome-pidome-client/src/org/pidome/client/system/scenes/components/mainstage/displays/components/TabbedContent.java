/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;

/**
 *
 * @author John Sirach
 */
public final class TabbedContent extends VBox {
    
    Map<String,Tabbed> tabs = new HashMap<>();
    ObservableMap<String,Tabbed> tabList = FXCollections.observableMap(tabs);
    
    HBox tabsHeader = new HBox(3 * DisplayConfig.getWidthRatio());
    StackPane tabContent = new StackPane();
    
    List<TabbedContentTabChangedListener> listeners = new ArrayList();
    
    String selectedTab = "";
    
    final void tabsHeaderHelper(MapChangeListener.Change<? extends String,? extends Tabbed> change){
        if (change.wasAdded()) {
            if (!tabsHeader.getChildren().contains(change.getValueAdded().title())) {
                tabsHeader.getChildren().add(change.getValueAdded().title());
            }
        } else if (change.wasRemoved()) {
            if (tabsHeader.getChildren().contains(change.getValueRemoved().title())) {
                change.getValueRemoved().destroy();
                tabsHeader.getChildren().remove(change.getValueRemoved().title());
            }
        }
    }
    
    public TabbedContent() {
        setId("tabbed");
        tabList.addListener(this::tabsHeaderHelper);
        
        setAlignment(Pos.TOP_LEFT);
        setPadding(new Insets(10*DisplayConfig.getHeightRatio()));
        
        tabsHeader.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        tabsHeader.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        
        tabContent.setAlignment(Pos.TOP_LEFT);
        tabContent.getStyleClass().add("tabbedcontent");
        
        getChildren().addAll(tabsHeader,tabContent);
    }
    
    public final void addTab(String id, String name){
        Tabbed tab = new Tabbed(id, name);
        if(tabList.isEmpty()){
            tabList.put(id, tab);
            tabSwitched(selectedTab, id);
            tab.setSelected(true);
        } else {
            tabList.put(id, tab);
            tab.setSelected(false);
        }
    }
    
    public final void setContentMinSize(double width, double height){
        tabContent.setMinSize(width* DisplayConfig.getWidthRatio(), height* DisplayConfig.getHeightRatio());
    }
    
    public final void destroy(){
        for(Tabbed tab:tabList.values()){
            tab.destroy();
        }
        tabs.clear();
        tabList.removeListener(this::tabsHeaderHelper);
        tabContent.getChildren().clear();
        getChildren().removeAll(tabsHeader,tabContent);
    }
    
    final void tabSwitched(String oldTab, String newTab){
        Iterator l = listeners.iterator();
        if(!oldTab.equals(newTab)){
            while (l.hasNext()) {
                ((TabbedContentTabChangedListener) l.next()).tabSwitched(oldTab, newTab);
            }
        }
    }
    
    public final void addTabChangedListener(TabbedContentTabChangedListener l){
        listeners.add(l);
    }

    public final void removeTabChangedListener(TabbedContentTabChangedListener l){
        listeners.remove(l);
    }
    
    public final void setTabContent(String newTab, Node content, String shortDesc){
        if(tabList.containsKey(selectedTab)){
            tabList.get(selectedTab).setSelected(false);
        }
        tabContent.getChildren().clear();
        tabList.get(newTab).setSelected(true);
        tabContent.getChildren().add(content);
    }
    
    class Tabbed {
        
        Image tabActive = new ImageLoader("displays/tabbed-selected.png",124,22).getImage();
        Image tabInactive = new ImageLoader("displays/tabbed-unselected.png",124,22).getImage();
        
        ImageView tabActiveImage = new ImageView(tabInactive);
        
        StackPane name = new StackPane();
        
        String tabId;
        
        final void tabClickHelper(MouseEvent t){
            tabSwitched(selectedTab, tabId);
        }
        
        public Tabbed(String tabId, String tabName){
            this.tabId = tabId;
            name.setAlignment(Pos.CENTER_LEFT);
            name.setPrefSize(124*DisplayConfig.getWidthRatio(), 22*DisplayConfig.getHeightRatio());
            name.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            name.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            Label lbl = new Label(tabName);
            lbl.setTranslateX(3*DisplayConfig.getWidthRatio());
            name.getChildren().addAll(tabActiveImage,lbl);
            name.addEventFilter(MouseEvent.MOUSE_RELEASED, this::tabClickHelper);
        }
        
        public final String getId(){
            return this.tabId;
        }
        
        public final StackPane title(){
            return name;
        }
        
        public final void destroy(){
            name.removeEventFilter(MouseEvent.MOUSE_RELEASED, this::tabClickHelper);
        }
        
        public final void setSelected(boolean select){
            if(select==true){
                selectedTab = tabId;
                tabActiveImage.setImage(tabActive);
            } else {
                tabActiveImage.setImage(tabInactive);
            }
        }
        
    }
    
}
