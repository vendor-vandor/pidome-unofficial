/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.shareddata;

/**
 *
 * @author John
 */
public interface SharedDayPartServiceListener {
    public void setNewDayPart(int currentStatusId,String currentStatusName);
}
