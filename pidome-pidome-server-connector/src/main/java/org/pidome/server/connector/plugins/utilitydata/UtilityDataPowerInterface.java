/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.utilitydata;

/**
 *
 * @author John
 */
public interface UtilityDataPowerInterface extends UtilityDataDataInterface {

    /**
     * Sets a today kwh value.
     * @param value 
     */
    public abstract void setTodayKwh(double value);
    
    /**
     * Returns the today kwh value.
     * @return 
     */
    public abstract UtilityDataData getKwh();
}
