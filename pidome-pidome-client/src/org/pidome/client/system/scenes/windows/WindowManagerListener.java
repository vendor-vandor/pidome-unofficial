/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.windows;

/**
 *
 * @author John Sirach
 */
public interface WindowManagerListener {

    public void windowAdded(WindowComponent window);
    
    public void windowRemoved(WindowComponent window);
    
}
