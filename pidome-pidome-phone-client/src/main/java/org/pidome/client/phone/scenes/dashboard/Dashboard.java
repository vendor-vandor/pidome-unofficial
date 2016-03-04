/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.scenes.dashboard;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.devices.Device;
import org.pidome.client.entities.devices.DeviceService;
import org.pidome.client.entities.plugins.utilityusages.UtilitiesUsagesPluginException;
import org.pidome.client.entities.plugins.utilityusages.UtilityPlugin;
import org.pidome.client.phone.scenes.BaseScene;
import org.pidome.client.phone.visuals.controls.devices.DeviceSingleControlPane;
import org.pidome.client.phone.visuals.controls.utilityusages.UtilitiesDashBoard;
import org.pidome.client.phone.visuals.panes.ItemPane;
import org.pidome.pcl.utilities.math.MathUtilities;
import org.pidome.pcl.utilities.properties.ObjectPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public class Dashboard extends BaseScene {

    private double minWidth = 320;
    private int columnCount;
    
    VBox dashboard = new VBox();
    
    private GridPane devicesPane = new GridPane();
    private DeviceService deviceService;
    private ReadOnlyObservableArrayListBean<Device> devices;
    private ObservableArrayListBeanChangeListener<Device> deviceMutator = this::deviceMutator;
    
    UtilitiesDashBoard utilityDash;
    
    public Dashboard() throws IOException{
        super(true);
        dashboard.setId("dashboard");
        dashboard.getChildren().addAll(devicesPane);
        setupDevicesPane();
        setSceneTitle("Dashboard");
    }
    
    private void setupDevicesPane(){
        devicesPane.setId("dashboard-devices-gridpane");
        
        columnCount = MathUtilities.floorDiv((int)Screen.getPrimary().getVisualBounds().getWidth(),(int)minWidth);
        
        ColumnConstraints column1Devices;
        ColumnConstraints column2Devices;

        switch(columnCount){
            case 0:
                column1Devices = new ColumnConstraints();
                column1Devices.setPercentWidth(100);
                devicesPane.getColumnConstraints().add(column1Devices);
            break;
            default:
                column1Devices = new ColumnConstraints();
                column1Devices.setPercentWidth(50);
                column2Devices = new ColumnConstraints();
                column2Devices.setPercentWidth(50);
                devicesPane.getColumnConstraints().addAll(column1Devices, column2Devices);
            break;
        }
    }
    
    @Override
    public void run() {
        try {
            deviceService = getSystem().getClient().getEntities().getDeviceService();
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.setContent(dashboard);
        createUtilityMeasurement();
        startDevicesLoader();
    }

    private void startDevicesLoader() {
        devices = deviceService.getDevices();
        devices.addListener(deviceMutator);
        new Thread() {
            @Override
            public final void run() {
                try {
                    deviceService.reload();
                } catch (EntityNotAvailableException ex) {
                    Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }
    
    @Override
    public void stop() {
        devices.removeListener(deviceMutator);
        if(utilityDash!=null){
            utilityDash.destroy();
        }
    }

    private void deviceMutator(ObservableArrayListBeanChangeListener.Change<? extends Device> change) {
        if(change.wasAdded()){
            if(hasSystem()){
                final List<DeviceSingleControlPane> visualDevicesList = new ArrayList<>();
                if(change.hasNext()){
                    for(Device device:change.getAddedSubList()){
                        if(device.getDeviceIsFavorite()){
                            visualDevicesList.add(
                                                    new DeviceSingleControlPane(
                                                        this,getSystem().getConnection(),
                                                        device
                                                    )
                                                  );
                        }
                    }
                }
                createDevices(visualDevicesList);
            }
        } else if (change.wasRemoved()){
            List<DeviceSingleControlPane> toRemove = new ArrayList<>();
            if(change.hasNext()){
                for(Device device:change.getRemoved()){
                    for(Node pane:devicesPane.getChildren()){
                        if(((DeviceSingleControlPane)pane).getDevice().equals(device)){
                            DeviceSingleControlPane removePane = (DeviceSingleControlPane)pane;
                            removePane.destroy();
                            toRemove.add(removePane);
                            System.out.println("Pane for " + device.getDeviceName() + " destroyed");
                        }
                    }
                }
            }
            Platform.runLater(() -> { 
                devicesPane.getChildren().removeAll(toRemove);
            });
        }
    }

    private void createDevices(List<DeviceSingleControlPane> visualDevicesList){
        Platform.runLater(() -> {
            for(ItemPane item:visualDevicesList){
                int amount = devicesPane.getChildren().size();
                switch(columnCount){
                    case 0:
                        devicesPane.add(item,0,amount);
                    break;
                    default:
                        devicesPane.add(item,amount%2,amount/2);
                    break;
                }
            }
        });
    }

    private void createUtilityMeasurement(){
        try {
            ObjectPropertyBindingBean<UtilityPlugin> plugin = getSystem().getClient().getEntities().getUtilityService().getUtilitiesUsages();
            plugin.addPropertyChangeListener((PropertyChangeEvent evt) -> {
                UtilityPlugin plug = (UtilityPlugin)evt.getNewValue();
                if(utilityDash==null || !dashboard.getChildren().contains(utilityDash)){
                    utilityDash = new UtilitiesDashBoard(plug);
                    dashboard.getChildren().add(0, utilityDash);
                    Platform.runLater(() -> {
                        utilityDash.start();
                    });
                }
            });
            getSystem().getClient().getEntities().getUtilityService().reload();
        } catch (UtilitiesUsagesPluginException | EntityNotAvailableException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}