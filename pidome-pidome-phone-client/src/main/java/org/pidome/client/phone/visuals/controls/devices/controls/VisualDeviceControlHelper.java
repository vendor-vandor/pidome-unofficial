/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.visuals.controls.devices.controls;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcons;
import de.jensd.fx.glyphs.weathericons.WeatherIcons;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.pidome.client.entities.devices.DeviceColorPickerControl;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceDataControl;
import org.pidome.client.entities.devices.DeviceSliderControl;
import org.pidome.client.entities.devices.DeviceToggleControl;
import org.pidome.client.phone.scenes.BaseScene;
import org.pidome.client.phone.visuals.interfaces.Destroyable;
import org.pidome.client.system.PCCConnection;

/**
 *
 * @author John
 */
public class VisualDeviceControlHelper extends HBox implements Destroyable {
    
    private final DeviceControl control;
    private final String size = "2em;";
    private final PCCConnection connection;
    private final BaseScene scene;
    Node visualControl;
    
    public VisualDeviceControlHelper(BaseScene scene, PCCConnection connection, DeviceControl control, boolean named){
        this.control = control;
        this.connection = connection;
        this.scene = scene;
        HBox.setHgrow(this, Priority.ALWAYS);
        createDeviceControl(named);
        getStyleClass().add("device-control");
    }
    
    private void createDeviceControl(boolean named){
        if(named){
            Label controlName = new Label(control.getName());
            controlName.getStyleClass().add("control-name");
            getChildren().add(controlName);
        } else {
            Text awesomeIcon;
            switch(control.getVisualType()){
                case LIGHT_LUX:
                case LIGHT_PERC: 
                    awesomeIcon = GlyphsDude.createIcon(WeatherIcons.DAY_SUNNY, size);
                break;
                case BATTERY:
                    awesomeIcon = GlyphsDude.createIcon(FontAwesomeIcons.BOLT, size);
                break;
                case TEMPERATURE_C:
                case TEMPERATURE_F: 
                    awesomeIcon = GlyphsDude.createIcon(WeatherIcons.THERMOMETER, size);
                break;
                case MOVEMENT:
                    awesomeIcon = GlyphsDude.createIcon(FontAwesomeIcons.STREET_VIEW, size);
                break;
                case PCVALUE:
                    awesomeIcon = GlyphsDude.createIcon(FontAwesomeIcons.SERVER, size);
                break;
                case HUMIDITY:
                    awesomeIcon = GlyphsDude.createIcon(WeatherIcons.SPRINKLES, size);
                break;
                case PRESSURE:
                    awesomeIcon = GlyphsDude.createIcon(FontAwesomeIcons.COG, size); /// Needs replacement
                break;
                case FLUID:
                    awesomeIcon = GlyphsDude.createIcon(WeatherIcons.SPRINKLES, size);
                break;
                case WIND:
                    awesomeIcon = GlyphsDude.createIcon(WeatherIcons.STRONG_WIND, size);
                break;
                default:
                    awesomeIcon = GlyphsDude.createIcon(FontAwesomeIcons.COG, size);
                break;
            }
            awesomeIcon.setTextAlignment(TextAlignment.LEFT);
            getChildren().add(awesomeIcon);
        }

        HBox controlContent = new HBox();
        HBox.setHgrow(controlContent, Priority.ALWAYS);
        controlContent.setAlignment(Pos.CENTER_RIGHT);
        
        switch(control.getControlType()){
            case DATA:
                visualControl = new VisualDeviceDataControl((DeviceDataControl)control);
                controlContent.getChildren().add(visualControl);
            break;
            case TOGGLE:
                visualControl = new VisualDeviceToggleButtonControl(connection, (DeviceToggleControl)control);
                controlContent.getChildren().add(visualControl);
            break;
            case SLIDER:
                visualControl = new VisualDeviceSliderControl(scene, connection, (DeviceSliderControl)control);
                controlContent.getChildren().add(visualControl);
            break;
            case COLORPICKER:
                visualControl = new VisualDeviceColorPickerControl(scene, connection, (DeviceColorPickerControl)control);
                controlContent.getChildren().add(visualControl);
            break;
        }
        getChildren().add(controlContent);
    }       

    @Override
    public void destroy() {
        if(visualControl!=null){
            ((VisualDeviceControlInterface)visualControl).destroy();
        }
    }
}
