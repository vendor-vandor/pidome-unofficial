/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays.components.lists;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
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
public class FilteredList extends BorderPane {
    
    VBox list = new VBox(1);
    
    StackPane listContentHolder = new StackPane();
    Rectangle viewPort = new Rectangle();
    
    int totalItems;
    
    Slider slider = new Slider();
    
    static double widthRatio = DisplayConfig.getWidthRatio();
    static double heightRatio = DisplayConfig.getHeightRatio();
    
    static Logger LOG = LogManager.getLogger(FilteredList.class);
    
    ArrayList<FilteredListItem> items = new ArrayList<>();
    ObservableList<FilteredListItem> itemsList = FXCollections.observableArrayList(items);
    ListChangeListener itemsListBuilder = this::allItemsListHelper;

    Comparator<FilteredListItem> sortedItemsComparator = (FilteredListItem item1, FilteredListItem item2) -> item1.getName().compareTo(item2.getName());
    Set<FilteredListItem>sortedList = new TreeSet<>(sortedItemsComparator);
    ObservableSet<FilteredListItem> sortedItems = FXCollections.observableSet(sortedList);
    SetChangeListener sortedItemsListBuilder = this::sortedItemsListHelper;
        
    ObservableList<String> filterOptions = FXCollections.observableArrayList();
    final ComboBox filterBox = new ComboBox(filterOptions);
    ChangeListener reBuilder = this::rebuilderHelper;

    ObservableList<String> subFilterOptions = FXCollections.observableArrayList();
    final ComboBox subFilterBox = new ComboBox(subFilterOptions);
    ChangeListener subRebuilder = this::subRebuilderHelper;
    
    ChangeListener setContentHeight = this::listContentHeightHelper;
    ChangeListener setContentWidth = this::listContentWidthHelper;
    
    ChangeListener setListPosition = this::sliderScrollContentHelper;
    ChangeListener setSliderValues = this::sliderValueHelper;
    ChangeListener setSliderHight = this::sliderHeightHelper;
    
    String currentCategory;
    
    HBox catBar = new HBox(2);
    
    String standardAllItemsText = "All items";
    
    boolean categorized = false;
    boolean primarySelected = false;
    
    IntegerProperty filteredAmount = new SimpleIntegerProperty(0);
    
    SliderPosition pos;
    
    boolean highlightSelect = false;
    
    public FilteredList(String baseCategory){
        this(baseCategory,SliderPosition.RIGHT);
    }
    
    public FilteredList(String baseCategory, SliderPosition pos){
        this.pos = pos;
        getStyleClass().add("categorylist");
        list.fillWidthProperty().setValue(Boolean.TRUE);
        if(baseCategory!=null){
            currentCategory = baseCategory;
        }
        filterOptions.addAll(standardAllItemsText, currentCategory);
        filterBox.setValue(standardAllItemsText);
        subFilterOptions.add(standardAllItemsText);
        subFilterBox.setValue(standardAllItemsText);
        listContentHolder.setAlignment(Pos.TOP_LEFT);
        listContentHolder.getChildren().add(list);
        listContentHolder.heightProperty().addListener(setContentHeight);
        listContentHolder.widthProperty().addListener(setContentWidth);
        HBox.setHgrow(listContentHolder, Priority.ALWAYS);
        listContentHolder.setClip(viewPort);
        setCenter(listContentHolder);
        switch(this.pos){
            case LEFT:
                setLeft(contentSlider());
            break;
            case RIGHT:
                setRight(contentSlider());
            break;
        }
        setPrefWidth(listContentHolder.getWidth() + slider.getWidth());
        catBar.setPadding(new Insets(5*heightRatio,0,5*heightRatio,5*widthRatio));
        catBar.getStyleClass().add("categorybar");
        catBar.setAlignment(Pos.CENTER_LEFT);
        subFilterBox.setVisibleRowCount(10);
    }
    
    public final void highlightOnSelect(boolean selectRow){
        highlightSelect = selectRow;
    }
    
    final void listContentHeightHelper(ObservableValue ov, Object t, Object t1){
        setPrefHeight((double) t1 + catBar.getHeight());
        viewPort.setHeight((double) t1);
    }
    
    final void listContentWidthHelper(ObservableValue ov, Object t, Object t1){
        setPrefWidth((double) t1 + slider.getWidth());
        viewPort.setWidth((double) t1);
    }
    
    public final void isCategorized(boolean isCat){
        categorized = isCat;
    }
    
    final void rebuilderHelper(ObservableValue ov, Object t, Object t1){
        if(t1!=null){
            if(((String)t1).equals(standardAllItemsText)){
                currentCategory = null;
            } else {
                currentCategory = (String)t1;
            }
            sortedItems.clear();
            subFilterOptions.clear();
            subFilterOptions.add(standardAllItemsText);
            subFilterBox.setValue(standardAllItemsText);
            itemsList.stream().forEach((item) -> {
                if (currentCategory == null) {
                    sortedItems.add(item);
                    item.getCategories().entrySet().stream().filter((subFilter) -> 
                            (!subFilterOptions.contains(subFilter.getValue()))).forEach((subFilter) -> {
                        subFilterOptions.add(subFilter.getValue());
                    });
                } else if (item.hasCategory(currentCategory)) {
                    sortedItems.add(item);
                    item.getCategories().entrySet().stream().filter((subFilter) -> 
                            (subFilter.getKey().equals(currentCategory) && !subFilterOptions.contains(subFilter.getValue()))).forEach((subFilter) -> {
                        subFilterOptions.add(subFilter.getValue());
                    });
                }
            });
            subFilterBox.setPrefWidth(subFilterBox.getMinWidth());
            this.filteredAmount.setValue(sortedItems.size());
        }
    }
    
    final void subRebuilderHelper(ObservableValue ov, Object t, Object t1){
        sortedItems.clear();
        itemsList.stream().forEach((item) -> {
            if (currentCategory == null) {
                item.getCategories().entrySet().stream().filter((category) -> (category.getValue().equals(t1)||t1==null)).forEach((_item) -> {
                    sortedItems.add(item);
                });
            } else if (item.hasCategory(currentCategory)) {
                item.getCategories().entrySet().stream().filter((category) -> (category.getKey().equals(currentCategory) && category.getValue().equals(t1))).forEach((_item) -> {
                    sortedItems.add(item);
                });
            } else if (t1!=null && t1.equals(standardAllItemsText)){
                sortedItems.add(item);
            }
        });
        this.filteredAmount.setValue(sortedItems.size());
    }
    
    public final void build(){
        if(categorized) {
            catBar.getChildren().addAll(new Label("Filter on "),filterBox,new Label(" with "),subFilterBox);
            setTop(catBar);
        }
        filterBox.valueProperty().addListener(reBuilder);
        subFilterBox.valueProperty().addListener(subRebuilder);
        sortedItems.addListener(sortedItemsListBuilder);
        itemsList.addListener(itemsListBuilder);
    }
    
    public final void destroy(){
        listContentHolder.heightProperty().removeListener(setContentHeight);
        listContentHolder.widthProperty().removeListener(setContentWidth);
        heightProperty().removeListener(setSliderHight);
        slider.valueProperty().removeListener(setListPosition);
        list.heightProperty().removeListener(setSliderValues);
        filterBox.valueProperty().removeListener(reBuilder);
        subFilterBox.valueProperty().removeListener(subRebuilder);
        sortedItems.removeListener(sortedItemsListBuilder);
        itemsList.removeListener(itemsListBuilder);
    }
    
    public final void setListSize(double setWidth, double setHeight){
        listContentHolder.setPrefSize(setWidth * DisplayConfig.getWidthRatio(), setHeight * DisplayConfig.getHeightRatio());
        listContentHolder.setMinSize(Region.USE_PREF_SIZE,Region.USE_PREF_SIZE);
        listContentHolder.setMaxSize(Region.USE_PREF_SIZE,Region.USE_PREF_SIZE);
        viewPort.setHeight(listContentHolder.getHeight());
        viewPort.setWidth(listContentHolder.getWidth());
    }
    
    public final void addItem(FilteredListItem node){
        if(node!=null){
            itemsList.add(node);
            node.addCategory(standardAllItemsText, standardAllItemsText);
            node.getCategories().entrySet().stream().map((cat) -> {
                if(!filterOptions.contains(cat.getKey())){
                    filterOptions.add(cat.getKey());
                }
                return cat;
            }).forEach((cat) -> {
                if (currentCategory == null) {
                    if(!subFilterOptions.contains(cat.getValue())) subFilterOptions.add(cat.getValue());
                } else if(cat.getKey().equals(currentCategory)){
                    if(!subFilterOptions.contains(cat.getValue())) subFilterOptions.add(cat.getValue());
                }
            });
            subFilterBox.setPrefWidth(subFilterBox.getMinWidth());
            this.filteredAmount.setValue(sortedItems.size());
        }
    }
    
    public final void removeItems(String category){
        ArrayList<FilteredListItem> toRemove = new ArrayList<>();
        itemsList.stream().filter((node) -> (node.hasCategory(category))).forEach((node) -> {
            toRemove.add(node);
        });
        itemsList.removeAll(toRemove);
    }

    public final void removeItem(String id){
        ArrayList<FilteredListItem> toRemove = new ArrayList<>();
        if(id!=null)
            itemsList.stream().filter((item) -> (item.getItemId().equals(id))).forEach((item) -> {
                toRemove.add(item);
            });
        itemsList.removeAll(toRemove);
    }
    
    public final void removeItem(FilteredListItem node){
        itemsList.remove(node);
        this.filteredAmount.setValue(sortedItems.size());
    }
    
    public final FilteredListItem getItem(String itemId){
        for(FilteredListItem item:itemsList){
            if(item.getItemId().equals(itemId)){
                return item;
            }
        }
        return null;
    }
    
    public final void updateItem(FilteredListItem node){
        itemsList.remove(node);
        itemsList.add(node);
    }
    
    /**
     * Adds and removes items from the list in the viewPort.
     * It handles the inserting of added items at the correct location in the list.
     * @param change 
     */
    final void allItemsListHelper(ListChangeListener.Change change){
        while (change.next()) {
            if (change.wasAdded()) {
                List<FilteredListItem> addedItems = change.getAddedSubList();
                addedItems.stream().forEach((item) -> {
                    if (currentCategory == null || item.hasCategory(currentCategory)) {
                        if(highlightSelect){
                            item.addEventFilter(MouseEvent.MOUSE_CLICKED, this::highlightSelectHelper);
                        }
                        sortedItems.add(item);
                    }
                });
            } else if (change.wasRemoved()) {
                List<FilteredListItem> removedItems = change.getRemoved();
                removedItems.stream().forEach((item) -> {
                    if (sortedItems.contains(item)) {
                        if(highlightSelect){
                            item.removeEventFilter(MouseEvent.MOUSE_CLICKED, this::highlightSelectHelper);
                        }
                        sortedItems.remove(item);
                    }
                });
            }
        }
    }
    
    final void sortedItemsListHelper(SetChangeListener.Change change){
        if (change.wasAdded()) {
            /// We first need to know at which position the item is added.
            int position = 0; 
            for(FilteredListItem item:sortedItems){
                if(item.equals(change.getElementAdded())){
                    list.getChildren().add(position,item);
                    break;
                }
                position++;
            }
        } else if (change.wasRemoved()) {
            FilteredListItem item = (FilteredListItem)change.getElementRemoved();
            if(list.getChildren().contains(item)) list.getChildren().remove(item);
        }
    }
    
    final void sliderScrollContentHelper(ObservableValue ov, Object t, Object t1){
        list.setTranslateY((double)t1 - (list.getHeight() - (getHeight() - catBar.getHeight())));
    }
    
    final void sliderValueHelper(ObservableValue ov, Object t, Object t1){
        if((double)t1>getHeight()){
            slider.setMax((double)t1 - (getHeight() - catBar.getHeight()));
            slider.setValue((double)t1 - (getHeight() - catBar.getHeight()));
            slider.setDisable(false);
        } else {
            slider.setMax(0.01);
            slider.setValue(0.01);
            slider.setDisable(true);
        }
    }
    
    final void sliderHeightHelper(ObservableValue ov, Object t, Object t1){
        slider.setPrefHeight((double) t1-((categorized)?42:12*heightRatio));
    }
    
    final Slider contentSlider(){
        slider.setOrientation(Orientation.VERTICAL);
        slider.setMin(0.0);
        slider.setTranslateY(5*heightRatio);
        slider.setMinHeight(Region.USE_PREF_SIZE);
        slider.setMaxHeight(Region.USE_PREF_SIZE);
        
        slider.setMinWidth(55 * widthRatio);
        
        slider.setPrefHeight(heightProperty().doubleValue()-((categorized)?42:12*heightRatio));
        
        heightProperty().addListener(setSliderHight);
        
        slider.valueProperty().addListener(setListPosition);
        list.heightProperty().addListener(setSliderValues);
        return slider;
    }
    
    final void highlightSelectHelper(MouseEvent e){
        sortedItems.stream().forEach((item) -> {
            if(item.getItemId().equals(((FilteredListItem)e.getSource()).getItemId())){
                highlightRow(item);
            } else {
                deHighlightRow(item);
            }
        });
    }
    
    public final void highLight(final String id){
        for (FilteredListItem item:sortedItems){
            if(item.getItemId().equals(id)){
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
    }

    public final void highlightRow(final String id){
        for (FilteredListItem item:sortedItems){
            if(item.getItemId().equals(id)){
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        highlightRow(item);
                    }
                };
                thread.start();
            }
        }
    }

    public void deHighlightRow(final String id){
        for (FilteredListItem item:sortedItems){
            if(item.getItemId().equals(id)){
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        deHighlightRow(item);
                    }
                };
                thread.start();
            }
        }
    }
    
    final void highlightRow(FilteredListItem item){
        if(!item.getStyleClass().contains("rowhiglight")) Platform.runLater(() -> { item.getStyleClass().add("rowhiglight"); });
    }
    
    final void deHighlightRow(FilteredListItem item){
        Platform.runLater(() -> { item.getStyleClass().remove("rowhiglight"); });
    }
    
    public final int getItemsAmount(){
        return this.itemsList.size();
    }
    public final ReadOnlyIntegerProperty getFilteredAmountProperty(){
        return (ReadOnlyIntegerProperty)this.filteredAmount;
    }
}