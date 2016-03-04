/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.menubars;

import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.scenes.navigation.ListBackHandler;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public final class MenuBarProvider {
    
    /**
     * The PCC system.
     */
    private PCCSystem system;
    
    /**
     * The service connector for platform specific access.
     */
    private ServiceConnector serviceConnector;
    
    /**
     * The menu bar.
     */
    private static MenuBarBase menuBar;
    
    /**
     * Constructor.
     */
    public MenuBarProvider(){

    }
    
    public final MenuBarBase build(PCCSystem system, ServiceConnector serviceConnector){
        if(menuBar == null){
            this.system = system;
            this.serviceConnector = serviceConnector;
            switch(this.system.getConnection().getConnectionProfile()){
                case MOBILE:
                    menuBar = new MobileMenuBar();
                break;
                case FIXED:
                    menuBar = new FixedMenuBar();
                break;
            }
            menuBar.build();
        }
        return getMenuBar();
    }
    
    public final void setMainMenuItem(ScenesHandler.ScenePane pane){
        if(menuBar!=null){
            menuBar.setMainMenuItem(pane);
        }
    }
    
    public final void setTitle(String title){
        if(menuBar!=null){
            menuBar.setTitle(title);
        }
    }
    
    public final void setSceneBackTitle(final ListBackHandler handler, final String id, String title){
        if(menuBar!=null){
            menuBar.setSceneBackTitle(handler, id, title);
        }
    }
    
    public final void handleExternalBackAction(){
        if(!menuBar.menuIsOpen()){
             menuBar.handleExternalBackAction();
        } else {
            menuBar.closeMenu();
        }
    }
    
    public final void toggleAppMenu(){
        menuBar.toggleMenu();
    }
    
    /**
     * Action to build.
     */
    public final void start(){
        if(!menuBar.started()){
            menuBar.setStarted(true);
            menuBar.start(this.system, this.serviceConnector);
        }
    }
    
    /**
     * Action to destroy.
     */
    public final void destroy(){
        if(menuBar != null){
            menuBar.destroy();
        }
    }
    
    /**
     * Return the menu bar.
     * @return 
     */
    public final MenuBarBase getMenuBar(){
        return this.menuBar;
    }
    
    public final void closeMenu(){
        if(menuBar != null){
            menuBar.closeMenu();
        }
    }
    
}