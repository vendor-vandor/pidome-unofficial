/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.interfaces.web.presentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author John
 */
public final class WebPresentListNVP extends WebPresentation {

    List<WebPresentSimpleNVP> listing = new ArrayList<>();
    
    public WebPresentListNVP(String label) {
        super(WebPresentation.TYPE.LIST_NVP, label);
    }

    @Override
    public final void setValue(Object value) {
        listing = (List<WebPresentSimpleNVP>)value;
    }
    
    /**
     * Returns the presentation object.
     * @return 
     */
    @Override
    public Object getPresentationValue(){
        List<Map<String,Object>> list = new ArrayList<>();
        for(WebPresentSimpleNVP value:listing){
            Map<String,Object> map = new HashMap<>();
            map.put(value.getLabel(), value.getPresentationValue());
            list.add(map);
        }
        return list;
    }
    
}
