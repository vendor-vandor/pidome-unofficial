/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.scenes.login;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.pidome.pcl.networking.connections.server.ServerConnection.Profile;

/**
 *
 * @author John
 */
public class InternalLoginConnection extends VBox {
    
    private final TextField ipField   = new TextField();
    private final TextField portField = new TextField();
    
    public InternalLoginConnection(Profile connectionProfile){
        Label setIp   = new Label("Server address");
        Label setPort = new Label("Server port");
        ipField.setPromptText("ip/host");
        portField.setPromptText("Websocket port");
        getChildren().addAll(setIp,ipField,setPort,portField);
    }
    
    public final String getIpFieldContent(){
        return ipField.getCharacters().toString();
    }
    
    public final int getPortFieldContent(){
        try {
            return Integer.parseInt(portField.getCharacters().toString());
        } catch (Exception ex){
            return 0;
        }
    }
    
}
