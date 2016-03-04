/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes;

import javafx.scene.layout.Pane;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public interface ScenePaneImpl {
    
    public String getTitle();
    
    public void start();
    
    public void close();
    
    public Pane getPane();
    
    public void setSystem(PCCSystem system, ServiceConnector connector);
    
    public void removeSystem();
    
}