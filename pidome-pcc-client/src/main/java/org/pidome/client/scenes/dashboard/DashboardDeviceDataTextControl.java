/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceDataControl;

/**
 *
 * @author John
 */
public class DashboardDeviceDataTextControl extends DashboardDeviceDataBaseControl {
    
    private final VBox container = new VBox();
    private final Text controlName = new Text();
    
    private final Text prefix = new Text();
    private final Text data = new Text();
    private final Text suffix = new Text();
    
    private final PropertyChangeListener deviceDataListener = this::dataListener;

    public DashboardDeviceDataTextControl(DashboardDeviceDataControl control) {
        super(control);
    }
    
    private void dataListener(PropertyChangeEvent evt){
        Platform.runLater(() -> { 
            if(this.getDashboardDeviceDataControl().getDataControl().getDataType()==DeviceControl.DeviceControlDataType.BOOLEAN){
                setBooleanField((boolean)evt.getNewValue());
            } else {
                data.setText(String.valueOf(evt.getNewValue()));
            }
        });
    }
    
    private void setBooleanField(boolean value){
        if(getDashboardDeviceDataControl().getDataControl().getBoolVisualType()!=DeviceDataControl.BoolVisualType.TEXT){
            if(value == true){
                this.getContainer().getStyleClass().remove("boolean-false");
                this.getContainer().getStyleClass().add("boolean-true");
            } else {
                this.getContainer().getStyleClass().remove("boolean-true");
                this.getContainer().getStyleClass().add("boolean-false");
            }
        }
        if(getDashboardDeviceDataControl().getDataControl().getBoolVisualType()!=DeviceDataControl.BoolVisualType.COLOR){
            if(value == true){
                data.setText(getDashboardDeviceDataControl().getDataControl().getTrueText());
            } else {
                data.setText(getDashboardDeviceDataControl().getDataControl().getFalseText());
            }
        }
    }
    
    @Override
    protected void build() {
        if(getDashboardDeviceDataControl().getDeviceItem().getShowDeviceName()){
            controlName.setText(getDashboardDeviceDataControl().getDataControl().getControlGroup().getDevice().getDeviceName());
        } else {
            controlName.setText(getDashboardDeviceDataControl().getDataControl().getName());
        }
        controlName.setWrappingWidth(getDashboardDeviceDataControl().getPane().getPaneWidth());
        controlName.setTextAlignment(TextAlignment.CENTER);
        controlName.setStyle("-fx-font-size: " + getDashboardDeviceDataControl().getPane().calcFontSize(12, false));
        
        setTextProperties(controlName, 12);
        setTextProperties(prefix, 10);
        setTextProperties(suffix, 10);
        setTextProperties(data, 15);
        
        prefix.setText(getDashboardDeviceDataControl().getDataControl().getPrefix());
        data.setText(getDashboardDeviceDataControl().getDataControl().getValueData().toString());
        suffix.setText(getDashboardDeviceDataControl().getDataControl().getSuffix());
        
        Platform.runLater(() -> { 
            container.getChildren().addAll(controlName, prefix, data, suffix); 
            if(this.getDashboardDeviceDataControl().getDataControl().getDataType()==DeviceControl.DeviceControlDataType.BOOLEAN){
                setBooleanField((boolean)getDashboardDeviceDataControl().getDataControl().getValueData());
            }
        });
        
        getDashboardDeviceDataControl().getDataControl().getValueProperty().addPropertyChangeListener(deviceDataListener);
    }

    private void setTextProperties(Text text, double size){
        text.getStyleClass().add("control-text");
        text.setWrappingWidth(getDashboardDeviceDataControl().getPane().getPaneWidth());
        text.setTextAlignment(TextAlignment.CENTER);
        text.setStyle("-fx-font-size: " + getDashboardDeviceDataControl().getPane().calcFontSize(size, false));
    }
    
    @Override
    protected void destruct() {
        getDashboardDeviceDataControl().getDataControl().getValueProperty().removePropertyChangeListener(deviceDataListener);
        Platform.runLater(() -> { container.getChildren().removeAll(controlName, prefix, data, suffix);  });
    }

    @Override
    protected VBox getContainer() {
        return container;
    }
    
}