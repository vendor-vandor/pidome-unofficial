/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.panes.popups;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import org.pidome.client.visuals.controls.graphs.DataChart;
import org.pidome.client.entities.devices.DeviceDataControl;
import org.pidome.client.services.ServiceConnector.DisplayType;
import org.pidome.client.tools.DisplayTools;

/**
 *
 * @author John
 */
public final class DeviceGraphPopup extends PopUp {
    
    private final DeviceDataControl control;
    private final DataChart chart;
    
    private final PropertyChangeListener deviceDataListener = this::dataListener;
    
    StackPane graphHolder = new StackPane();
    
    public DeviceGraphPopup(DeviceDataControl control){
        super(FontAwesomeIcon.AREA_CHART, control.getName());
        this.control = control;
        chart = new DataChart(this.control.getGraph());
        chart.setDataPrefix(this.control.getPrefix());
        chart.setDataSuffix(this.control.getSuffix());
        chart.setSeriesName(this.control.getControlGroup().getDevice().getDeviceName());
        chart.setValueName(this.control.getName());
        if(DisplayTools.getUserDisplayType()!=DisplayType.TINY){
            double prefWidth = 690;
            if(Screen.getPrimary().getBounds().getWidth()<690){
                prefWidth = Screen.getPrimary().getBounds().getWidth();
            }
            double prefHeight = (prefWidth/7) * 4;
            graphHolder.setPrefSize(prefWidth, prefHeight);
        } else {
            graphHolder.setMaxSize(Screen.getPrimary().getBounds().getWidth()-5, Screen.getPrimary().getBounds().getHeight()-120);
        }
        //graphHolder.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        //graphHolder.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        graphHolder.getChildren().add(chart.createChart());
        this.setContent(graphHolder);
    }

    public final void start(){
        chart.preFill(this.control.getHistoricData().update());
        control.getValueProperty().addPropertyChangeListener(deviceDataListener);
    }
    
    private void dataListener(PropertyChangeEvent evt){
        Platform.runLater(() -> { 
            chart.updateGraphData((Number)evt.getNewValue());
        });
    }
    
    @Override
    public void unload() {
        control.getValueProperty().removePropertyChangeListener(deviceDataListener);
    }
    
}
