/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import eu.hansolo.enzo.gauge.OneEightyGauge;
import eu.hansolo.enzo.gauge.OneEightyGaugeBuilder;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author John
 */
public class DashboardDeviceDataGaugeControl extends DashboardDeviceDataBaseControl {
    
    private OneEightyGauge gauge;
    private final PropertyChangeListener deviceDataListener = this::dataListener;
    
    private StackPane container = new StackPane();
    
    private Map<String,Double> values = new HashMap<>();
    
    boolean updated = false;
    
    protected DashboardDeviceDataGaugeControl(DashboardDeviceDataControl control) {
        super(control);
        try {
            String unit = (getDashboardDeviceDataControl().getDataControl().getSuffix().equals(""))?getDashboardDeviceDataControl().getDataControl().getPrefix():getDashboardDeviceDataControl().getDataControl().getSuffix();
            String title;
            if(getDashboardDeviceDataControl().getDeviceItem().getCustomLabel().equals("")){
                title = getDashboardDeviceDataControl().getDataControl().getName() + "\n" + getDashboardDeviceDataControl().getDataControl().getControlGroup().getDevice().getDeviceName();
            } else {
                title = getDashboardDeviceDataControl().getDeviceItem().getCustomLabel();
            }
            setGaugeValues();
            gauge = OneEightyGaugeBuilder.create()
                                         .unit(unit)
                                         .maxValue(values.get("maxValue"))
                                         .minValue(values.get("minValue"))
                                         .barColor(Color.GREEN)
                                         .animated(false)
                                         .animationDuration(1000)
                                         .decimals(2)
                                         .title(title)
                                         .maxSize(getDashboardDeviceDataControl().getPane().getPaneWidth(), getDashboardDeviceDataControl().getPane().getPaneHeight())
                                         .build();
            StackPane.setMargin(gauge, new Insets(10,10,10,10));
            gauge.setMaxSize(getDashboardDeviceDataControl().getPane().getPaneWidth(), getDashboardDeviceDataControl().getPane().getPaneHeight());
            container.setMaxSize(getDashboardDeviceDataControl().getPane().getPaneWidth(), getDashboardDeviceDataControl().getPane().getPaneHeight());
        } catch (NumberFormatException ex){
            Logger.getLogger(DashboardDeviceDataGaugeControl.class.getName()).log(Level.SEVERE, "Invalid numbering in gauges control", ex);
        }
    }

    @Override
    protected void build() {
        if(gauge!=null){
            Platform.runLater(() -> { 
                container.getChildren().add(gauge);
                ////gauge.setValue(((Number)getDashboardDeviceDataControl().getDataControl().getValueData()).doubleValue());
                gauge.setAnimated(true);
            });
            Timer timer = new Timer();  //At this line a new Thread will be created
            timer.schedule( new TimerTask() {
                @Override
                public final void run(){
                    if(!updated){
                        Platform.runLater(() -> { 
                            gauge.setValue(((Number)getDashboardDeviceDataControl().getDataControl().getValueData()).doubleValue());
                        });
                    }
                }
            }, 5000); //delay in milliseconds
        } else {
            Text invalid = new Text("Invalid gauge setup");
            invalid.getStyleClass().add("control-text");
            invalid.setWrappingWidth(getDashboardDeviceDataControl().getPane().getPaneWidth());
            invalid.setTextAlignment(TextAlignment.CENTER);
            invalid.setStyle("-fx-font-size: " + getDashboardDeviceDataControl().getPane().calcFontSize(16, false));
            Platform.runLater(() -> { container.getChildren().add(invalid); });
        }
        getDashboardDeviceDataControl().getDataControl().getValueProperty().addPropertyChangeListener(deviceDataListener);
    }

    private void setGaugeValues() throws NumberFormatException {
        
        if((getDashboardDeviceDataControl().getDeviceItem().getMaxValue().doubleValue() > getDashboardDeviceDataControl().getDeviceItem().getMinValue().doubleValue())){
            values.put("maxValue", getDashboardDeviceDataControl().getDeviceItem().getMaxValue().doubleValue());
            values.put("minValue", getDashboardDeviceDataControl().getDeviceItem().getMinValue().doubleValue());
        } else if((getDashboardDeviceDataControl().getDataControl().getMaxValue().doubleValue()>getDashboardDeviceDataControl().getDataControl().getMinValue().doubleValue())){
            values.put("maxValue", getDashboardDeviceDataControl().getDataControl().getMaxValue().doubleValue());
            values.put("minValue", getDashboardDeviceDataControl().getDataControl().getMinValue().doubleValue());
        } else {
            throw new NumberFormatException();
        }
        
        if((getDashboardDeviceDataControl().getDeviceItem().getHighValue().doubleValue()>getDashboardDeviceDataControl().getDeviceItem().getWarnValue().doubleValue())){
            values.put("highValue", getDashboardDeviceDataControl().getDeviceItem().getHighValue().doubleValue());
            values.put("warnValue", getDashboardDeviceDataControl().getDeviceItem().getWarnValue().doubleValue());
        } else if((getDashboardDeviceDataControl().getDataControl().getHighValue().doubleValue()>getDashboardDeviceDataControl().getDataControl().getWarnValue().doubleValue())){
            values.put("highValue", getDashboardDeviceDataControl().getDataControl().getHighValue().doubleValue());
            values.put("warnValue", getDashboardDeviceDataControl().getDataControl().getWarnValue().doubleValue());
        } else {
            values.put("highValue", 0.0);
            values.put("warnValue", 0.0);
        }
        
    }
    
    private void dataListener(PropertyChangeEvent evt){
        updated = true;
        final Paint barColor;
        double highValue = values.get("highValue");
        double warnValue = values.get("highValue");
        double value = ((Number)evt.getNewValue()).doubleValue();
        if(highValue>warnValue){
            if(value>=highValue){
                barColor = Color.RED;
            } else if(value>=warnValue){
                barColor = Color.ORANGE;
            } else {
                barColor = Color.GREEN;
            }
        } else {
            barColor = Color.GREEN;
        }
        Platform.runLater(() -> {
            gauge.setValue(value);
            ///gauge.setBarColor(barColor);
        });
    }
    
    @Override
    protected void destruct() {
        getDashboardDeviceDataControl().getDataControl().getValueProperty().removePropertyChangeListener(deviceDataListener);
        if(gauge!=null){
            Platform.runLater(() -> { container.getChildren().remove(gauge); });
        } else {
            Platform.runLater(() -> { container.getChildren().clear(); });
        }
    }

    @Override
    protected StackPane getContainer() {
        return container;
    }
    
}
