/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.scenes;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import org.pidome.client.scenes.panes.popups.PopUp;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class ScenesPopUpLargeScreen extends PopUp {

    private ScenesComposer scenesPane = new ScenesComposer();
    
    public ScenesPopUpLargeScreen(PCCSystem system, ServiceConnector connector) {
        super(FontAwesomeIcon.LIGHTBULB_ALT, "Scenes");
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