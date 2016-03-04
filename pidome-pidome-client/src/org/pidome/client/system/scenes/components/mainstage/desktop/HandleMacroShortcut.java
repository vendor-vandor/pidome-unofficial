/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system.scenes.components.mainstage.desktop;

import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.domotics.components.macros.Macros;

/**
 *
 * @author John Sirach
 */
public final class HandleMacroShortcut {

    public HandleMacroShortcut(Object... evtId) throws Exception {
        ClientData.sendData(Macros.getMacroCommand(Integer.valueOf((String)evtId[0])));
    }

}
