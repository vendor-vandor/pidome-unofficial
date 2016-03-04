/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays.components.lists;

import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;

/**
 *
 * @author John
 */
public class CategoryList extends HBox {
    
    VBox list = new VBox();
    
    StackPane listContentHolder = new StackPane();
    Rectangle viewPort = new Rectangle();
    
    ObservableMap<String,catBox> catBoxList = FXCollections.observableHashMap();
    
    int totalItems;
    
    static double widthRatio = DisplayConfig.getWidthRatio();
    static double heightRatio = DisplayConfig.getHeightRatio();
    
    Slider slider = new Slider();
    
    static Logger LOG = LogManager.getLogger(CategoryList.class);
    
    ChangeListener setContentHeight = this::listContentHeightHelper;
    ChangeListener setContentWidth  = this::listContentWidthHelper;
    
    ChangeListener setListPosition = this::sliderScrollContentHelper;
    ChangeListener setSliderValues = this::sliderValueHelper;
    ChangeListener setSliderHight  = this::sliderHeightHelper;
    
    SliderPosition pos;
    
    boolean highlightSelect = false;
    
    public CategoryList(){
        this(SliderPosition.RIGHT);
    }
    
    public CategoryList(SliderPosition pos){
        this.pos = pos;
        list.getStyleClass().add("categorylist");
        list.fillWidthProperty().setValue(Boolean.TRUE);
        catBoxList.addListener((MapChangeListener.Change<? extends String,? extends catBox> change) -> {
            if (change.wasAdded()) {
                if (!list.getChildren().contains(change.getValueAdded())) {
                    list.getChildren().add(change.getValueAdded());
                }
            } else if (change.wasRemoved()) {
                if (list.getChildren().contains(change.getValueRemoved())) {
                    list.getChildren().remove(change.getValueRemoved());
                }
            }
        });
        listContentHolder.setAlignment(Pos.TOP_LEFT);
        listContentHolder.getChildren().add(list);
        listContentHolder.heightProperty().addListener(setContentHeight);
        listContentHolder.widthProperty().addListener(setContentWidth);
        HBox.setHgrow(listContentHolder, Priority.ALWAYS);
        listContentHolder.setClip(viewPort);
        switch(this.pos){
            case LEFT:
                getChildren().addAll(contentSlider(),listContentHolder);
            break;
            case RIGHT:
                getChildren().addAll(listContentHolder,contentSlider());
            break;
        }
        setPrefWidth(listContentHolder.getWidth() + slider.getWidth());
    }
    
    public final void highlightOnSelect(boolean selectRow){
        highlightSelect = selectRow;
    }
    
    final void listContentHeightHelper(ObservableValue ov, Object t, Object t1){
        setPrefHeight((double) t1);
        viewPort.setHeight((double) t1);
    }
    
    final void listContentWidthHelper(ObservableValue ov, Object t, Object t1){
        setPrefWidth((double) t1 + slider.getWidth());
        viewPort.setWidth((double) t1);
    }
    
    public final void destroy(){
        listContentHolder.heightProperty().removeListener(setContentHeight);
        listContentHolder.widthProperty().removeListener(setContentWidth);
        slider.valueProperty().removeListener(setListPosition);
        list.heightProperty().removeListener(setSliderValues);
        heightProperty().removeListener(setSliderHight);
    }
    
    public final void setListSize(double setWidth, double setHeight){
        listContentHolder.setMinWidth(Region.USE_PREF_SIZE);
        listContentHolder.setMaxWidth(Region.USE_PREF_SIZE);
        listContentHolder.setMinHeight(Region.USE_PREF_SIZE);
        listContentHolder.setMaxHeight(Region.USE_PREF_SIZE);
        listContentHolder.setPrefSize(setWidth, setHeight);
        viewPort.setHeight(setHeight);
        viewPort.setWidth(setWidth);
    }
    
    final void sliderScrollContentHelper(ObservableValue ov, Object t, Object t1){
        list.setTranslateY((double)t1 - (list.getHeight() - getHeight()));
    }
    
    final void sliderValueHelper(ObservableValue ov, Object t, Object t1){
            if((double)t1>getHeight()){
                slider.setMax((double)t1 - getHeight());
                slider.setValue((double)t1 - getHeight());
                slider.setDisable(false);
            } else {
                slider.setMax(0.1);
                slider.setValue(0.1);
                slider.setDisable(true);
            }
    }
    
    final void sliderHeightHelper(ObservableValue ov, Object t, Object t1){
        slider.setPrefHeight((double) t1-(12*heightRatio));
    }
    
    final Slider contentSlider(){
        slider.setOrientation(Orientation.VERTICAL);
        slider.setMin(0.0);
        slider.setTranslateX(5*widthRatio);
        slider.setTranslateY(5*heightRatio);
        slider.setMinHeight(Region.USE_PREF_SIZE);
        slider.setMaxHeight(Region.USE_PREF_SIZE);
        
        slider.setMinWidth(55 * widthRatio);
        
        slider.setPrefHeight(heightProperty().doubleValue()-(15*heightRatio));
        
        heightProperty().addListener(setSliderHight);
        
        slider.valueProperty().addListener(setListPosition);
        list.heightProperty().addListener(setSliderValues);
        return slider;
    }
    
    public final void addItem(String category, String id, Node node){
        if(node!=null){
            addCategory(category);
            VBox.setMargin(node, new Insets(5,0,5,0));
            catBoxList.get(category).addItem(id, node);
        }
    }
    
    public final void removeItem(String category, String id){
        if(catBoxList.containsKey(category)){
            catBoxList.get(category).removeItem(id);
            if(catBoxList.get(category).isEmpty()){
                catBoxList.remove(category);
            }
        }
    }

    public final Node getItem(String category, String id){
        if(catBoxList.containsKey(category) && catBoxList.get(category).containsItem(id)){
            return catBoxList.get(category).getItem(id);
        }
        return null;
    }
    
    public final void updateItemCategory(String oldCat, String newCat, String itemId){
        addItem(newCat, itemId, getItem(oldCat, itemId));
        removeItem(newCat, itemId);
    }
    
    final void addCategory(String category){
        if(!catBoxList.containsKey(category)){
            catBox cat = new catBox(category);
            VBox.setMargin(cat, new Insets(5,0,5,0));
            catBoxList.put(category, cat);
        }
    }
    
    final void removeCategory(String category){
        if (catBoxList.get(category).isEmpty()) {
            catBoxList.remove(category);
        }
    }
    
    public final void highlightRow(String category, String itemId){
        if(catBoxList.containsKey(category)){
            catBoxList.get(category).highLight(itemId);
        }
    }
    
    public final void highlightRowFixed(String category, String itemId, boolean highlight){
        if(catBoxList.containsKey(category)){
            if(highlight){
                catBoxList.get(category).highlightRow(itemId);
            } else {
                catBoxList.get(category).deHighlightRow(itemId);
            }
        }
    }
    
    public final int getItemsAmount(){
        return this.totalItems;
    }
    
    class catBox extends VBox {
        
        String catName;
        
        Map<String, Node>items = new HashMap<>();
        ObservableMap<String,Node> itemsList = FXCollections.observableMap(items);
        
        public catBox(String catName) {
            this.catName = catName;
        
            Label name;
            name = new Label(catName);
            name.setMaxWidth(Double.MAX_VALUE);
            name.setAlignment(Pos.BASELINE_LEFT);
            name.getStyleClass().add("categorybar");
            
            getChildren().add(name);
            
            itemsList.addListener((MapChangeListener.Change<? extends String, ? extends Node> change) -> {
                if (change.wasAdded()) {
                    Node node = change.getValueAdded();
                    if (!getChildren().contains(node)) {
                        if(getChildren().size()%2==0){
                            node.getStyleClass().add("rowodd");
                        } else {
                            node.getStyleClass().add("roweven");
                        }
                        getChildren().add(node);
                        totalItems++;
                    }
                } else if (change.wasRemoved()) {
                    if (getChildren().contains(change.getValueRemoved())) {
                        getChildren().remove(change.getValueRemoved());
                        totalItems--;
                    }
                }
            });
        }
        
        public final boolean containsItem(String itemId){
            return itemsList.containsKey(itemId);
        }

        public final Node getItem(String itemId){
            if(itemsList.containsKey(itemId)){
                return itemsList.get(itemId);
            } else {
                return null;
            }
        }
        
        public final void addItem(String itemId, Node node){
            if(!itemsList.containsKey(itemId)) itemsList.put(itemId, node);
        }
        
        public final void removeItem(String itemId){
            if(itemsList.containsKey(itemId)) itemsList.remove(itemId);
        }
        
        public final boolean isEmpty(){
            return itemsList.isEmpty();
        }
        
        public final void highLight(final String id){
            if(itemsList.containsKey(id)){
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            highlightRow(id);
                            Thread.sleep(1000);
                            deHighlightRow(id);
                        } catch (InterruptedException ex) {
                            //// no probs dude
                            deHighlightRow(id);
                        }
                    }
                };
                thread.start();
            }
        }

        public final void highlightRow(final String id){
            Platform.runLater(() -> {
                if(itemsList.containsKey(id) && !itemsList.get(id).getStyleClass().contains("rowhiglight")) itemsList.get(id).getStyleClass().add("rowhiglight");
            });
        }

        public void deHighlightRow(final String id){
            Platform.runLater(() -> {
                if(itemsList.containsKey(id)) itemsList.get(id).getStyleClass().remove("rowhiglight");
            });
        }
        
        
    }

    
}
