/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.dashboard;

import java.util.Map;

/**
 *
 * @author John
 */
public class DashboardTimeItem extends DashboardItem {

    public DashboardTimeItem() {
        super(ItemType.TIME);
    }

    @Override
    protected void setConfig(Map<String, Object> config) {
        /// There is no config, as it used default system time.
    }
    
}