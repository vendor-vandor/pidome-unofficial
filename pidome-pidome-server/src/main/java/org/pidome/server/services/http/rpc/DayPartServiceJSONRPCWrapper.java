/*
 * Copyright 2014 John.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.server.services.http.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.pidome.server.system.dayparts.DayPart;
import org.pidome.server.system.dayparts.DayPartException;
import org.pidome.server.system.dayparts.DayPartsService;

/**
 *
 * @author John
 */
public class DayPartServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements DayPartServiceJSONRPCWrapperInterface {

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer,Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getDayParts", null);
                put("setDayPart", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getDayPart", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("addDayPart", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("name", "");}});
                        put(1,new HashMap<String,Object>(){{put("description", "");}});
                    }
                });
                put("updateDayPart", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("name", "");}});
                        put(2,new HashMap<String,Object>(){{put("description", "");}});
                    }
                });
                put("deleteDayPart", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
            }
        };
        return mapping;
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<Map<String, Object>> getDayParts() throws DayPartException {
        List<DayPart> daypartList = DayPartsService.getDayParts();
        List<Map<String, Object>> dayParts = new ArrayList();
        for(int i=0; i < daypartList.size(); i++){
            Map<String,Object> dayPart = new HashMap<>();
            dayPart.put("id", daypartList.get(i).getId());
            dayPart.put("name", daypartList.get(i).getName());
            dayPart.put("description", daypartList.get(i).getDescription());
            dayPart.put("lastactivated", daypartList.get(i).getLastActivated());
            dayPart.put("active", daypartList.get(i).getId()==DayPartsService.current().getId());
            dayPart.put("fixed", daypartList.get(i).getIsFixed());
            dayParts.add(dayPart);
        }
        return dayParts;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object setDayPart(Long id) throws DayPartException {
        return DayPartsService.setDayPart(id.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getDayPart(Long id) throws DayPartException {
        DayPart daypart = DayPartsService.getDayPart(id.intValue());
        Map<String,Object>dayPartMap = new HashMap<>();
        dayPartMap.put("id", daypart.getId());
        dayPartMap.put("name", daypart.getName());
        dayPartMap.put("description", daypart.getDescription());
        dayPartMap.put("lastactivated", daypart.getLastActivated());
        dayPartMap.put("active", daypart.getId()==DayPartsService.current().getId());
        dayPartMap.put("fixed", daypart.getIsFixed());
        return dayPartMap;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object addDayPart(String name, String description) throws DayPartException {
        return DayPartsService.addDayPart(name, description);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object updateDayPart(Long id, String name, String description) throws DayPartException {
        return DayPartsService.updateDayPart(id.intValue(), name, description);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object deleteDayPart(Long id) throws DayPartException {
        return DayPartsService.deleteDayPart(id.intValue());
    }

}
