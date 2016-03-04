/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.login;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.pidome.client.scenes.ScenePaneImpl;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class OrientationLoader extends StackPane implements ScenePaneImpl {
    
    OrientationChoice conForm;
    
    private PCCSystem system;
    
    public OrientationLoader(){

    }
    
    @Override
    public void setSystem(PCCSystem system, ServiceConnector connector) {
        this.system = system;
        conForm = new OrientationChoice();
        conForm.build();
        conForm.setSystem(system, connector);
    }

    @Override
    public void removeSystem() {
        system = null;
    }
    
    @Override
    public final void start(){
        conForm.show();
    }
    
    @Override
    public final void close(){
        conForm.close();
    }
    
    @Override
    public final String getTitle() {
        return "Select app mode";
    }

    @Override
    public Pane getPane() {
        return this;
    }
    
}