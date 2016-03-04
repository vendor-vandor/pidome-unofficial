/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.macros;

import javafx.scene.layout.Pane;
import org.pidome.client.scenes.ScenePaneImpl;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class MacrosSmallScreen implements ScenePaneImpl {

    private MacrosComposer scenesPane = new MacrosComposer();
    
    @Override
    public String getTitle() {
        return "Macro's";
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