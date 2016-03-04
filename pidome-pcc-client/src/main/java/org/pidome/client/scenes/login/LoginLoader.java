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
import org.pidome.pcl.networking.connections.server.ServerConnection.Profile;

/**
 *
 * @author John
 */
public class LoginLoader extends StackPane implements ScenePaneImpl {
    
    LoginPopUp conForm;
    
    private PCCSystem system;
    
    public LoginLoader(){

    }
    
    @Override
    public void setSystem(PCCSystem system, ServiceConnector connector) {
        this.system = system;
        if(system.getConnection().getConnectionProfile()==Profile.FIXED){
            conForm = new LoginPopUpFixed();
        } else {
            conForm = new LoginPopUpMobile();
        }
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
        conForm.start();
    }
    
    @Override
    public final void close(){
        conForm.close();
    }
    
    @Override
    public final String getTitle() {
        return "Login";
    }

    @Override
    public Pane getPane() {
        return this;
    }
    
}