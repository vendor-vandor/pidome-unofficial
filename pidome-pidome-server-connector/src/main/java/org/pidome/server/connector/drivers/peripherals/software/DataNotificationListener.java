/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.drivers.peripherals.software;

/**
 *
 * @author John
 */
public interface DataNotificationListener {
    public void notifyLedSnd();
    public void notifyLedRcv();
}
