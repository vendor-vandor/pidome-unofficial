/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.presences;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import org.pidome.client.scenes.panes.popups.PopUp;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class PresencesPopUpLargeScreen extends PopUp {

    private PresenceComposer presencePane = new PresenceComposer();
    
    public PresencesPopUpLargeScreen(PCCSystem system, ServiceConnector connector) {
        super(MaterialDesignIcon.ACCOUNT_MULTIPLE, "Presences");
        presencePane.setMinWidth(400);
        setContent(presencePane);
        presencePane.setSystem(system, connector);
        presencePane.start();
    }

    @Override
    public void unload() {
        presencePane.close();
        presencePane.removeSystem();
    }
    
}