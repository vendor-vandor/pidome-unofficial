/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.visuals.controls.devices.controls;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlCommandException;
import org.pidome.client.entities.devices.DeviceSliderControl;
import org.pidome.client.entities.devices.DeviceSliderControl.SliderCommand;
import org.pidome.client.phone.scenes.BaseScene;
import org.pidome.client.phone.scenes.visuals.DialogBox;
import org.pidome.client.system.PCCConnection;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;

/**
 *
 * @author John
 */
public class VisualDeviceSliderControl extends StackPane implements VisualDeviceControlInterface {

    private final BaseScene scene;
    private final PCCConnection connection;
    private final DeviceSliderControl control;
    
    private Slider slider;
    private VBox parent;
    private Button sliderValue;
    
    DialogBox sliderPopup;
    
    PropertyChangeListener dataChangeEvent = this::dataChange;
    
    public VisualDeviceSliderControl(BaseScene scene, PCCConnection connection, DeviceSliderControl control){
        this(scene,connection, control, false);
    }
    
    public VisualDeviceSliderControl(BaseScene scene, PCCConnection connection, DeviceSliderControl control, boolean inline){
        this.scene = scene;
        this.connection = connection;
        this.control = control;
        createSlider();
        if(inline){
            this.getChildren().add(parent);
        } else {
            this.getChildren().add(createPopUpSlider());
        }
        this.control.getValueProperty().addPropertyChangeListener(dataChangeEvent);
    }
 
    private void dataChange(PropertyChangeEvent evt){
        Platform.runLater(() -> { 
            if(sliderValue!=null){
                sliderValue.setText(String.valueOf(evt.getNewValue()));
            }
            if(slider!=null){
                slider.setValue(((Number)control.getValueData()).doubleValue());
            }
        });
    }
    
    private Button createPopUpSlider(){
        sliderValue = new Button(String.valueOf(this.control.getValueData()));
        sliderPopup = new DialogBox(this.control.getName());
        sliderPopup.setContent(parent);
        sliderPopup.setButtons(new DialogBox.PopUpButton[]{new DialogBox.PopUpButton("CANCEL", "Cancel"), new DialogBox.PopUpButton("OK", "Set")});
        sliderPopup.addListener((String buttonId) -> {
            scene.closePopup(sliderPopup);
            switch(buttonId){
                case "CANCEL":
                    /// Well, do nothing i guess :)
                break;
                case "OK":
                    try {
                        SliderCommand sliderCommand;
                        switch(this.control.getDataType()){
                            case INTEGER:
                                sliderCommand = new DeviceSliderControl.SliderCommand(this.slider.valueProperty().intValue());
                            break;
                            case FLOAT:
                                sliderCommand = new DeviceSliderControl.SliderCommand(this.slider.valueProperty().doubleValue());
                            break;
                            default:
                                throw new DeviceControlCommandException("Invalid control data type");
                        }
                        DeviceControl.DeviceCommandStructure command = this.control.createSendCommand(sliderCommand);
                        this.connection.getJsonHTTPRPC(command.getMethod(), command.getParameters(), command.getId());
                    } catch (DeviceControlCommandException | PCCEntityDataHandlerException ex){
                        Logger.getLogger(VisualDeviceSliderControl.class.getName()).log(Level.SEVERE, "Could not send command: " + ex.getMessage(), ex);
                    }
                break;
            }
        });
        sliderPopup.build();
        sliderValue.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            if(!scene.hasPopup(sliderPopup)){
                scene.showPopup(sliderPopup);
            }
        });
        return sliderValue;
    }
    
    private VBox createSlider(){
        parent = new VBox();
        BorderPane header = new BorderPane();
        
        StackPane sliderGroup = new StackPane();
        sliderGroup.getStyleClass().add("tracked-slider");
        this.slider = new Slider();
        this.slider.setMin(control.getMin().doubleValue());
        this.slider.setMax(control.getMax().doubleValue());
        
        Text min = new Text(String.valueOf(control.getMin()));
        min.getStyleClass().add("text");
        Text cur = new Text(String.valueOf(this.slider.valueProperty().doubleValue()));
        cur.getStyleClass().add("text");
        Text max = new Text(String.valueOf(control.getMax()));
        max.getStyleClass().add("text");
        
        final ProgressBar pb = new ProgressBar(this.slider.getValue()/this.slider.maxProperty().doubleValue());
        pb.setPrefWidth(Double.MAX_VALUE);
        
        this.slider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            pb.setProgress(new_val.doubleValue()/this.slider.maxProperty().doubleValue());
            cur.setText(String.valueOf(Math.round(new_val.doubleValue())));
        });
        this.slider.heightProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            pb.setPrefWidth(this.slider.widthProperty().doubleValue() - this.slider.heightProperty().doubleValue());
        });
        sliderGroup.getChildren().addAll(pb, this.slider);
        
        this.slider.setValue(((Number)control.getValueData()).floatValue());
        
        header.setLeft(min);
        header.setCenter(cur);
        header.setRight(max);
        header.setPrefWidth(Double.MAX_VALUE);
        
        parent.getChildren().addAll(header, sliderGroup);
        return parent;
    }
    
    @Override
    public void destroy() {
        this.control.getValueProperty().removePropertyChangeListener(dataChangeEvent);
    }
}