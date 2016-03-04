/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.dashboard.Dashboard;
import org.pidome.client.entities.dashboard.DashboardItem;
import org.pidome.client.scenes.ScenePaneImpl;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;
import org.pidome.client.tools.DisplayTools;
import org.pidome.pcl.utilities.math.MathUtilities;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;

/**
 * LEt's display some stuff.
 * @author John
 */
public final class VisualDashboard extends GridPane implements ScenePaneImpl {

    private PCCSystem system;
    
    private final ObservableArrayListBeanChangeListener<Dashboard> dashMutator = this::dashMutator;
    private final ObservableArrayListBeanChangeListener<DashboardItem> dashItemsMutator = this::dashItemsMutator;
    
    private Dashboard currentBoard;
    
    private final Label noDash = new Label("You do not have a dashboard or it is empty. On the server at Designers > Dashboards you will be able to create one for this device.\n\nIn future releases if no dashboard is present favorite items will be put on this screen.");
    
    private int hAmount = 1;
    private int vAmount = 1;
    
    public VisualDashboard(){
        this.getStyleClass().add("dashboard");
        setHgap(10);
        setVgap(10);
        //// Calculate the amount of cells possible in the width
        hAmount = MathUtilities.floorDiv((int)Screen.getPrimary().getBounds().getWidth()-20, (int)(90 + this.getHgap()));
        vAmount = MathUtilities.floorDiv((int)Screen.getPrimary().getBounds().getHeight(), (int)(90 + this.getVgap()));
        buildConstraints();
        this.setPadding(new Insets(10,0,0,0));
        this.setAlignment(Pos.TOP_CENTER);
        
        
        noDash.setWrapText(true);
        noDash.setTextAlignment(TextAlignment.CENTER);
        noDash.setMaxWidth((hAmount * 90) - (this.getHgap() * 2));
        noDash.getStyleClass().add("text");
        
    }
    
    private void buildConstraints(){
        getColumnConstraints().clear();
        for(int i = 1; i < hAmount; i++){
            ColumnConstraints column = new ColumnConstraints(90);
            column.setHgrow(Priority.NEVER);
            getColumnConstraints().add(column);
        }
        if(DisplayTools.getUserDisplayType()==ServiceConnector.DisplayType.LARGE){
            getRowConstraints().clear();
            for(int i = 1; i < vAmount; i++){
                RowConstraints row = new RowConstraints(90);
                row.setVgrow(Priority.NEVER);
                getRowConstraints().add(row);
            }
        }
    }
    
    @Override
    public String getTitle() {
        return "Dashboard";
    }

    @Override
    public void start() {
        try {
            this.system.getClient().getEntities().getDashboardService().getDashboardsList().addListener(dashMutator);
            if(!this.system.getClient().getEntities().getDashboardService().hasDashboard()){
                this.system.getClient().getEntities().getDashboardService().preload();
            } else {
                if(this.system.getClient().getEntities().getDashboardService().getDashboardsList().size()>0){
                    buildDashboard(this.system.getClient().getEntities().getDashboardService().getDashboardsList().get(0));
                }
            }
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(VisualDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void dashItemsMutator(ObservableArrayListBeanChangeListener.Change<? extends DashboardItem> change) {
        if(change.wasAdded()){
            if(change.hasNext()){
                builDashboardItems((List<DashboardItem>)change.getAddedSubList());
            }
        } else if (change.wasRemoved()){
            destroyDashboardItems();
        }
    }
    
    private void dashMutator(ObservableArrayListBeanChangeListener.Change<? extends Dashboard> change) {
        if(change.wasAdded()){
            if(change.hasNext()){
                for(Dashboard dash:change.getAddedSubList()){
                    buildDashboard(dash);
                }
            }
        } else if (change.wasRemoved()){
            if(change.hasNext()){
                destroyDashboard();
            }
        }
    }
    
    private void destroyDashboardItems(){
        for(Node node:getChildren()){
            if(node instanceof VisualDashboardItem){
                ((VisualDashboardItem)node).destruct();
                ((VisualDashboardItem)node).removeSystem();
            }
        }
        Platform.runLater(() -> {
            getChildren().clear();
        });
    }
    
    private void builDashboardItems(List<DashboardItem> items){
        if(items.size()>0){
            Platform.runLater(() -> {
                this.getChildren().remove(noDash);
                for(DashboardItem dashItem:items){
                    VisualDashboardItem item = null;
                    switch(dashItem.getItemType()){
                        case DEVICE:
                            item = new VisualDashboardDeviceItem(this.system, dashItem);
                        break;
                        case TIME:
                            item = new VisualDashboardTimeItem(this.system, dashItem);
                        break;
                        case WEATHER:
                            item = new VisualDashboardWeatherItem(this.system, dashItem);
                        break;
                        case MACRO:
                            item = new VisualDashboardMacroItem(this.system, dashItem);
                        break;
                        case SCENE:
                            item = new VisualDashboardSceneItem(this.system, dashItem);
                        break;
                        case SPACER:
                            item = new VisualDashboardSpacerItem(this.system, dashItem);
                        break;
                    }
                    if(item!=null){
                        item.build();
                        add(item, item.getDashboardItem().getColumn()-1, item.getDashboardItem().getRow()-1, item.getDashboardItem().getSizeX(), item.getDashboardItem().getSizeY());
                    }
                }
            });
        } else {
            checkEmpty();
        }
    }
    
    private void checkEmpty(){
        Platform.runLater(() -> {
            if(this.getChildren().size()==0 && !this.getChildren().contains(noDash)){
                add(noDash, 0, 0, hAmount, vAmount);
            }
        });
    }
    
    private void buildDashboard(Dashboard dash){
        currentBoard = dash;
        buildConstraints();
        builDashboardItems(dash.getItems().subList(0, dash.getItems().size()));
        currentBoard.getItems().addListener(dashItemsMutator);
    }
    
    private void destroyDashboard(){
        if(currentBoard!=null){
            currentBoard.getItems().removeListener(dashItemsMutator);
        }
        Platform.runLater(() -> {
            destroyDashboardItems();
        });
    }
    
    @Override
    public void close() {
        try {
            this.system.getClient().getEntities().getDashboardService().getDashboardsList().removeListener(dashMutator);
            destroyDashboard();
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(VisualDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Pane getPane() {
        return this;
    }

    @Override
    public void setSystem(PCCSystem system, ServiceConnector connector) {
        this.system = system;
    }

    @Override
    public void removeSystem() {
        system = null;
    }
    
}
