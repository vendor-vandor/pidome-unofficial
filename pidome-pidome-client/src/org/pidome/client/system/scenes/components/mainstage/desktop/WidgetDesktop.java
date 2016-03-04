/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.desktop;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author John Sirach
 */
public class WidgetDesktop extends DesktopBase {

    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(WidgetDesktop.class);
    
    VBox iconPane = new VBox();
    
    double width  = 196.0;
    double height = 285.0;
    
    StackPane contentPane = new StackPane();
    
    public WidgetDesktop(){
        contentPane.setId("widgetdesktop");
        contentPane.setAlignment(Pos.TOP_LEFT);
        contentPane.setMaxWidth(width);
        iconPane.setMaxWidth(178);
        contentPane.getChildren().addAll(iconPane);
        attachScrollbar();
        contentPane.setMinHeight(height);
        contentPane.setMaxHeight(height);
        Rectangle mask = new Rectangle(width,height);
        contentPane.setClip(mask);
        addDefaultIcons();
    }
    
    public final StackPane getPane(){
        return contentPane;
    }
    
    final void attachScrollbar(){
        Slider slider = new Slider();
        slider.setOrientation(Orientation.VERTICAL);
        slider.setMin(0.0);
        slider.setTranslateX(179);
        slider.setTranslateY(20);
        slider.setMaxHeight(255);
        slider.setMinHeight(255);
        slider.valueProperty().addListener((ChangeListener)(ObservableValue ov, Object t, Object t1) -> {
            iconPane.setTranslateY((double)t1 - (iconPane.getHeight() - height));
        });
        iconPane.heightProperty().addListener((ChangeListener)(ObservableValue ov, Object t, Object t1) -> {
            if((double)t1>height){
                slider.setMax((double)t1 - height);
                slider.setValue((double)t1 - height);
            } else {
                slider.setMax(0.1);
                slider.setValue(0.1);
            }
        });
        contentPane.getChildren().addAll(slider);
    }

    @Override
    void addIcon(DesktopIcon icon) {
        iconPane.getChildren().add(icon);
    }

    @Override
    void removeIcon(DesktopIcon icon) {
        iconPane.getChildren().remove(icon);
    }
    
}
