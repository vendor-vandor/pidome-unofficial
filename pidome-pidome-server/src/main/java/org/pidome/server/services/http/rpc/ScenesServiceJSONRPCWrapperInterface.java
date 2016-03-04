/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.http.rpc;

import java.util.ArrayList;
import org.pidome.server.services.scenes.ServerScenesException;

/**
 *
 * @author John
 */
public interface ScenesServiceJSONRPCWrapperInterface {
 
    public Object getScenes();
    
    public Object getScene(Number id) throws ServerScenesException;
    
    @PiDomeJSONRPCPrivileged
    public Object saveScene(String name, String description, ArrayList dependencies) throws ServerScenesException;
    
    @PiDomeJSONRPCPrivileged
    public Object editScene(Number id, String name, String description, ArrayList dependencies) throws ServerScenesException;
    
    @PiDomeJSONRPCPrivileged
    public Object deleteScene(Number id) throws ServerScenesException;
    
    public Object activateScene(Number id) throws ServerScenesException;
    
    public Object deActivateScene(Number id) throws ServerScenesException;
    
}