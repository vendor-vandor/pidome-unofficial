/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.settings;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import javafx.scene.control.Label;
import org.pidome.client.services.ServiceConnector;

/**
 *
 * @author John
 */
public class FixedSettings extends SharedSettings {

    @Override
    protected void composeSettings(ServiceConnector serviceConnector) {
        addSetting("Themes", MaterialDesignIcon.THEME_LIGHT_DARK, () -> {
            FixedThemesSettings settings = new FixedThemesSettings(serviceConnector);
            settings.setup();
            settings.show(true);
        });
        addSetting("Display", MaterialDesignIcon.TELEVISION_GUIDE, null);
        addSetting("Dashboards", MaterialIcon.WIDGETS, null);
        this.getChildren().add(new Label("Currently only Themes is available, be aware that this can make the dashboard loading slow!"));
    }
    
}
