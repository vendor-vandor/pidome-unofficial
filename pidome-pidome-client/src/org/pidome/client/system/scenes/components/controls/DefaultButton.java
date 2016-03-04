/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.controls;

import javafx.scene.control.Button;

/**
 *
 * @author John
 */
public class DefaultButton extends Button {
    
    public DefaultButton(){
        super();
        setMinSize();
    }
    
    public DefaultButton(String title){
        super(title);
        setMinSize();
    }
    
    final void setMinSize(){
        setMinSize(60,35);
    }
    
}
