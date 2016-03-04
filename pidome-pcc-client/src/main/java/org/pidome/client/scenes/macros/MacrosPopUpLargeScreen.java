/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.macros;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import org.pidome.client.scenes.panes.popups.PopUp;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class MacrosPopUpLargeScreen extends PopUp {

    private MacrosComposer scenesPane = new MacrosComposer();
    
    public MacrosPopUpLargeScreen(PCCSystem system, ServiceConnector connector) {
        super(MaterialDesignIcon.PLAY, "Macro's");
        scenesPane.setMinWidth(400);
        setContent(scenesPane);
        scenesPane.setSystem(system, connector);
        scenesPane.start();
    }

    @Override
    public void unload() {
        scenesPane.close();
        scenesPane.removeSystem();
    }
}