/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.presences;

import javafx.scene.layout.Pane;
import org.pidome.client.scenes.ScenePaneImpl;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class PresencesSmallScreen implements ScenePaneImpl {

    private PresenceComposer presencePane = new PresenceComposer();
    
    @Override
    public String getTitle() {
        return "Presences";
    }

    @Override
    public void start() {
        presencePane.start();
    }

    @Override
    public void close() {
        presencePane.close();
    }

    @Override
    public Pane getPane() {
        return presencePane;
    }

    @Override
    public void setSystem(PCCSystem system, ServiceConnector connector) {
        presencePane.setSystem(system, connector);
    }

    @Override
    public void removeSystem() {
        presencePane.removeSystem();
    }
    
}