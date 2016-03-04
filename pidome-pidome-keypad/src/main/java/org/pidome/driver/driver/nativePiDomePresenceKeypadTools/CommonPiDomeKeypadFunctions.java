/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativePiDomePresenceKeypadTools;

import org.pidome.driver.driver.nativePiDomePresenceKeypadDriver.PiDomePresenceKeypadDataHelpers;

/**
 *
 * @author John
 */
public interface CommonPiDomeKeypadFunctions {
    public void handleTokenActionData(PiDomePresenceKeypadDataHelpers.TokenActionTypes action, int uid, char[] tokenData);
    public void handleCustomData(String data);
    public void handleKeypadData(String group, String control, Object data);
    public void handlePersonRequest(PiDomePresenceKeypadDataHelpers.PersonRequestType type);
}
