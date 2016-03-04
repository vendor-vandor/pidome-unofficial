/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.visuals.controls.devices;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlCommandException;
import org.pidome.client.entities.devices.DeviceSliderControl;
import org.pidome.client.entities.devices.DeviceSliderControl.SliderCommand;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;

/**
 *
 * @author John
 */
public class VisualDeviceSliderControl extends Slider implements VisualDeviceControlInterface {

    private final DeviceSliderControl control;
    
    PropertyChangeListener dataChangeEvent = this::dataChange;
    
    public VisualDeviceSliderControl(PCCConnectionInterface connection, DeviceSliderControl control){
        this(connection, control, false);
    }
    
    public VisualDeviceSliderControl(PCCConnectionInterface connection, DeviceSliderControl control, boolean inline){
        this.control = control;
        getStyleClass().add("tracked-slider");
        setMin(control.getMin().doubleValue());
        setMax(control.getMax().doubleValue());
        
        Text min = new Text(String.valueOf(control.getMin()));
        min.getStyleClass().add("text");
        Text cur = new Text(String.valueOf(valueProperty().doubleValue()));
        cur.getStyleClass().add("text");
        Text max = new Text(String.valueOf(control.getMax()));
        max.getStyleClass().add("text");
        
        this.valueChangingProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasChanging, Boolean isNowChanging) -> {
            if (wasChanging && !isNowChanging) {
                try {
                    DeviceControl.DeviceCommandStructure command = this.control.createSendCommand(new SliderCommand(getValue()));
                    connection.getJsonHTTPRPC(command.getMethod(), command.getParameters(), command.getId());
                } catch (DeviceControlCommandException | PCCEntityDataHandlerException ex) {
                    Logger.getLogger(VisualDeviceSliderControl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        this.control.getValueProperty().addPropertyChangeListener(dataChangeEvent);
    }
 
    private void dataChange(PropertyChangeEvent evt){
        Platform.runLater(() -> { 
            //if(sliderValue!=null){
            //    sliderValue.setText(String.valueOf(evt.getNewValue()));
            //}
            setValue(((Number)control.getValueData()).doubleValue());
        });
    }
    
    @Override
    public void destroy() {
        this.control.getValueProperty().removePropertyChangeListener(dataChangeEvent);
    }
}