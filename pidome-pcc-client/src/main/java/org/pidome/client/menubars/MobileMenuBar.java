/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.menubars;

import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class MobileMenuBar extends MenuBarBase {

    public MobileMenuBar(){
        super();
    }
    
    @Override
    protected final void build() {
        
    }
    
    @Override
    protected final void resume(PCCSystem system){
        
    }
    
    @Override
    protected final void resumeDestroy() {
        /// Set started at the end.
        setStarted(false);
    }
    
}