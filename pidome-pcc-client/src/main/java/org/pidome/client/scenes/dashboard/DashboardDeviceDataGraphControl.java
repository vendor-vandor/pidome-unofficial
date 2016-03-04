/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.pidome.client.visuals.controls.graphs.DataChart;

/**
 * 
 * @author John
 */
public class DashboardDeviceDataGraphControl extends DashboardDeviceDataBaseControl {

    private final StackPane container = new StackPane();
    
    private final PropertyChangeListener deviceDataListener = this::dataListener;
    
    private final DataChart chart;
    
    public DashboardDeviceDataGraphControl(DashboardDeviceDataControl control) {
        super(control);
        chart = new DataChart(getDashboardDeviceDataControl().getDataControl().getGraph());
        chart.setDataPrefix(getDashboardDeviceDataControl().getDataControl().getPrefix());
        chart.setDataSuffix(getDashboardDeviceDataControl().getDataControl().getSuffix());
        chart.setSeriesName(getDashboardDeviceDataControl().getDataControl().getControlGroup().getDevice().getDeviceName());
        chart.setValueName(getDashboardDeviceDataControl().getDataControl().getName());
    }
    
    public final void removeChart(){
        chart.unsetGraphData();
        container.getChildren().remove(chart.getChart());
    }
    
    private void dataListener(PropertyChangeEvent evt){
        Platform.runLater(() -> { 
            chart.updateGraphData((Number)evt.getNewValue());
        });
    }
    
    @Override
    protected void build() {
        if(getDashboardDeviceDataControl().getDataControl().hasGraph()){
            chart.preFill(getDashboardDeviceDataControl().getDataControl().getHistoricData().update());
            XYChart put = chart.createChart();
            if (Platform.isFxApplicationThread()) {
                container.getChildren().add(put);
            } else {
                Platform.runLater(() -> {
                    container.getChildren().add(put);
                });
            }
            getDashboardDeviceDataControl().getDataControl().getValueProperty().addPropertyChangeListener(deviceDataListener);
        } else {
            Text invalid = new Text("Invalid graph setup");
            invalid.getStyleClass().add("control-text");
            invalid.setWrappingWidth(getDashboardDeviceDataControl().getPane().getPaneWidth());
            invalid.setTextAlignment(TextAlignment.CENTER);
            invalid.setStyle("-fx-font-size: " + getDashboardDeviceDataControl().getPane().calcFontSize(16, false));
            Platform.runLater(() -> { container.getChildren().add(invalid); });
        }
    }

    @Override
    protected void destruct() {
        getDashboardDeviceDataControl().getDataControl().getValueProperty().removePropertyChangeListener(deviceDataListener);
        if (Platform.isFxApplicationThread()) {
            removeChart();
        } else {
            Platform.runLater(() -> {
                removeChart();
            });
        }
        
    }

    @Override
    protected StackPane getContainer() {
        return container;
    }
    
}