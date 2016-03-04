/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.scenes;

import javafx.scene.layout.Pane;
import org.pidome.client.scenes.ScenePaneImpl;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class ScenesSmallScreen implements ScenePaneImpl {

    private ScenesComposer scenesPane = new ScenesComposer();
    
    @Override
    public String getTitle() {
        return "Scenes";
    }

    @Override
    public void start() {
        scenesPane.start();
    }

    @Override
    public void close() {
        scenesPane.close();
    }

    @Override
    public Pane getPane() {
        return scenesPane;
    }

    @Override
    public void setSystem(PCCSystem system, ServiceConnector connector) {
        scenesPane.setSystem(system, connector);
    }

    @Override
    public void removeSystem() {
        scenesPane.removeSystem();
    }
    
}