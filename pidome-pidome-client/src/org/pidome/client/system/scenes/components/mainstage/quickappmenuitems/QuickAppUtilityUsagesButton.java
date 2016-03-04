/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.quickappmenuitems;

import org.pidome.client.system.domotics.components.utilitymeasurement.UtilityMeasurementsException;
import org.pidome.client.system.scenes.windows.SimpleErrorMessage;
import org.pidome.client.system.scenes.components.mainstage.displays.UtilityMeasurementsDisplay;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 *
 * @author John Sirach
 */
public class QuickAppUtilityUsagesButton extends QuickAppItem {

    public QuickAppUtilityUsagesButton(){
        super("utilityusage", "Utility usage");
    }
    
    @Override
    public final void clicked(double openX, double openY){
        try {
            WindowManager.openWindow(new UtilityMeasurementsDisplay(), openX, openY);
        } catch (UtilityMeasurementsException ex) {
            SimpleErrorMessage message = new SimpleErrorMessage("No utility usage");
            message.setMessage("Activate utility usages on the server.");
            WindowManager.openWindow(message, openX, openY);
        }
    }
    
}
