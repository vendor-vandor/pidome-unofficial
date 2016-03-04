/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.quickappmenuitems;

import org.pidome.client.system.scenes.windows.SimpleErrorMessage;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 *
 * @author John Sirach
 */
public class QuickAppClockButton extends QuickAppItem {

    public QuickAppClockButton(){
        super("clock", "Clock / Agenda");
    }
    
    @Override
    public final void clicked(double openX, double openY){
        WindowManager.openWindow(new SimpleErrorMessage("Not suppoerted yet"), openX, openY);
    }
    
}
