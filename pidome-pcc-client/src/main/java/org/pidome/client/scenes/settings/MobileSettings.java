/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.settings;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.scene.control.Label;
import org.pidome.client.services.ServiceConnector;

/**
 *
 * @author John
 */
public class MobileSettings extends SharedSettings {

    @Override
    protected void composeSettings(ServiceConnector serviceConnector) {
        addSetting("GPS", MaterialDesignIcon.CROSSHAIRS_GPS, null);
        this.getChildren().add(new Label("Not yet available"));
    }
    
}
