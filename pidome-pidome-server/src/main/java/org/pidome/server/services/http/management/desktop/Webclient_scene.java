/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.http.management.desktop;

import java.util.HashMap;
import java.util.Map;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John
 */
public class Webclient_scene extends Webservice_renderer {
    @Override
    public void collect() {
        Map<String,Object> pageData = new HashMap<>();
        pageData.put("page_title", "Edit/add scene");
        if(getDataMap.containsKey("id")){
            pageData.put("sceneId", getDataMap.get("id"));
        } else {
            pageData.put("sceneId", "");
        }
        setData(pageData);
    }
}