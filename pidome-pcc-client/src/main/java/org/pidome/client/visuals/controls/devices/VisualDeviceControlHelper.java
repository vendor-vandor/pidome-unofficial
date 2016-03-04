/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.visuals.controls.devices;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.weathericons.WeatherIcon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javax.security.auth.Destroyable;
import org.pidome.client.entities.devices.DeviceButtonControl;
import org.pidome.client.entities.devices.DeviceColorPickerControl;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceDataControl;
import org.pidome.client.entities.devices.DeviceSliderControl;
import org.pidome.client.entities.devices.DeviceToggleControl;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.scenes.panes.popups.DeviceGraphPopup;
import org.pidome.client.system.PCCConnectionInterface;

/**
 *
 * @author John
 */
public class VisualDeviceControlHelper extends HBox implements Destroyable {
    
    private final DeviceControl control;
    private final String size = "2em;";
    private final PCCConnectionInterface connection;
    Node visualControl;

    DeviceGraphPopup popup;
    
    public VisualDeviceControlHelper(PCCConnectionInterface connection, DeviceControl control, boolean named){
        this(connection, control, named, false);
    }
    
    public VisualDeviceControlHelper(PCCConnectionInterface connection, DeviceControl control, boolean named, boolean graphLink){
        this.control = control;
        this.connection = connection;
        createDeviceControl(named, graphLink);
        getStyleClass().add("device-control");
    }
    
    private void createDeviceControl(boolean named, boolean graphLink){
        if(named){
            Label controlName = new Label(control.getName());
            controlName.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(controlName, Priority.ALWAYS);
            controlName.getStyleClass().add("control-name");
            getChildren().add(controlName);
        } else {
            Text awesomeIcon;
            switch(control.getVisualType()){
                case LIGHT_LUX:
                case LIGHT_PERC: 
                    awesomeIcon = GlyphsDude.createIcon(WeatherIcon.DAY_SUNNY, size);
                break;
                case BATTERY:
                    awesomeIcon = GlyphsDude.createIcon(FontAwesomeIcon.BOLT, size);
                break;
                case TEMPERATURE_C:
                case TEMPERATURE_F: 
                    awesomeIcon = GlyphsDude.createIcon(WeatherIcon.THERMOMETER, size);
                break;
                case MOVEMENT:
                    awesomeIcon = GlyphsDude.createIcon(FontAwesomeIcon.STREET_VIEW, size);
                break;
                case PCVALUE:
                    awesomeIcon = GlyphsDude.createIcon(FontAwesomeIcon.SERVER, size);
                break;
                case HUMIDITY:
                    awesomeIcon = GlyphsDude.createIcon(WeatherIcon.SPRINKLE, size);
                break;
                case PRESSURE:
                    awesomeIcon = GlyphsDude.createIcon(FontAwesomeIcon.COG, size); /// Needs replacement
                break;
                case FLUID:
                    awesomeIcon = GlyphsDude.createIcon(WeatherIcon.SPRINKLE, size);
                break;
                case WIND:
                    awesomeIcon = GlyphsDude.createIcon(WeatherIcon.STRONG_WIND, size);
                break;
                default:
                    awesomeIcon = GlyphsDude.createIcon(FontAwesomeIcon.COG, size);
                break;
            }
            awesomeIcon.setTextAlignment(TextAlignment.LEFT);
            getChildren().add(awesomeIcon);
        }

        switch(control.getControlType()){
            case DATA:
                visualControl = new VisualDeviceDataControl((DeviceDataControl)control);
                getChildren().add(visualControl);
                if(((VisualDeviceDataControl)visualControl).hasGraph()){
                    getChildren().add(getGraphLink((DeviceDataControl)control));
                }
            break;
            case TOGGLE:
                visualControl = new VisualDeviceToggleButtonControl(connection, (DeviceToggleControl)control);
                getChildren().add(visualControl);
            break;
            case SLIDER:
                visualControl = new VisualDeviceSliderControl(connection, (DeviceSliderControl)control);
                getChildren().add(visualControl);
            break;
            case COLORPICKER:
                visualControl = new VisualDeviceColorPickerControl(connection, (DeviceColorPickerControl)control);
                getChildren().add(visualControl);
            break;
            case BUTTON:
                visualControl = new VisualDeviceButtonControl(connection, (DeviceButtonControl)control);
                getChildren().add(visualControl);
            break;
        }
        HBox.setHgrow(visualControl, Priority.NEVER);
    }       

    private Text getGraphLink(DeviceDataControl dataControl){
        Text icon = GlyphsDude.createIcon(FontAwesomeIcon.AREA_CHART, "1.4em");
        icon.setPickOnBounds(true);
        icon.setOnMouseClicked((MouseEvent e) -> {
            if(popup!=null){
                ScenesHandler.closePopUp(popup);
            }
            popup = new DeviceGraphPopup(dataControl);
            popup.build();
            popup.setButtons();
            popup.show(true);
            popup.start();
        });
        HBox.setMargin(icon, new Insets(0,0,0,10));
        return icon;
    }
    
    @Override
    public void destroy() {
        if(visualControl!=null){
            ((VisualDeviceControlInterface)visualControl).destroy();
        }
        if(popup!=null){
            ScenesHandler.closePopUp(popup);
        }
    }
}
