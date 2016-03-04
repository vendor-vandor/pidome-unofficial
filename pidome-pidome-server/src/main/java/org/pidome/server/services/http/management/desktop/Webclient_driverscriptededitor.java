/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.http.management.desktop;

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John
 */
public class Webclient_driverscriptededitor extends Webservice_renderer {

    static Logger LOG = LogManager.getLogger(Webclient_driversedit.class);
    
    @Override
    public void collect() {
        Map<String,Object> pageData = new HashMap<>();
        /// this is only here for primitive testing. When this functionality is accepted continue correct build
        pageData.put("scriptedDriver", DeviceService.getScriptedDriver(Integer.parseInt(getDataMap.get("id"))));
        pageData.put("page_title", "Edit Custom driver");
        setData(pageData);
    }
    
}