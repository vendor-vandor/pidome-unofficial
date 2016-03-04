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
import org.pidome.server.system.location.BaseLocations;
import org.pidome.server.system.location.LocationServiceException;

/**
 *
 * @author John
 */
final class LocationServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements LocationServiceJSONRPCWrapperInterface {

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("addLocation", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("name", "");}});
                        put(1,new HashMap<String,Object>(){{put("floor", 0L);}});
                    }
                });
                put("editLocation", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("name", "");}});
                        put(2,new HashMap<String,Object>(){{put("floor", 0L);}});
                    }
                });
                put("getLocation", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("deleteLocation", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getLocations", null);
                put("getLocationsByFloor", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("addFloor", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("name", "");}});
                        put(1,new HashMap<String,Object>(){{put("level", 0L);}});
                    }
                });
                put("editFloor", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("name", "");}});
                        put(2,new HashMap<String,Object>(){{put("level", 0L);}});
                    }
                });
                put("getFloor", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("deleteFloor", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getFloors", null);
                ///// The are in fact private function for use with the visual floor editor and hence not publically documented
                put("setFloorVisual", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}}); //// Floor id.
                        put(1,new HashMap<String,Object>(){{put("image", "");}}); //// Room id.
                    }
                });
                put("removeFloorVisual", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}}); //// Floor id.
                    }
                });
                put("setRoomVisual", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}}); //// Room id.
                        put(1,new HashMap<String,Object>(){{put("x", 0L);}}); //// Start X location
                        put(2,new HashMap<String,Object>(){{put("y", 0L);}}); ///// Start Y location
                        put(3,new HashMap<String,Object>(){{put("w", 0L);}}); ///// room width
                        put(4,new HashMap<String,Object>(){{put("h", 0L);}}); ///// room height
                        put(5,new HashMap<String,Object>(){{put("old", 0L);}}); ///// the old id
                    }
                });
                put("updateRoomVisual", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}}); //// Room id.
                        put(1,new HashMap<String,Object>(){{put("x", 0L);}}); //// Start X location
                        put(2,new HashMap<String,Object>(){{put("y", 0L);}}); ///// Start Y location
                        put(3,new HashMap<String,Object>(){{put("w", 0L);}}); ///// room width
                        put(4,new HashMap<String,Object>(){{put("h", 0L);}}); ///// room height
                    }
                });
                put("removeRoomVisual", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}}); //// Floor id.
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
    public List<Map<String,Object>> getLocations() throws LocationServiceException {
        return BaseLocations.getLocations();
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<Map<String, Object>> getLocationsByFloor(Long floorId) throws LocationServiceException {
        List<Map<String, Object>> newList = new ArrayList<>();
        for(Map<String, Object> location:BaseLocations.getLocations()){
            if((int)location.get("floor")==floorId.intValue()){
                newList.add(location);
            }
        }
        return newList;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Map<String,Object> getLocation(Long locationId) throws LocationServiceException {
        return BaseLocations.getLocation(locationId.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean addLocation(String name, Long floor) throws LocationServiceException {
        return BaseLocations.saveLocation(name, floor.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean editLocation(Long locationId, String name, Long floor) throws LocationServiceException {
        return BaseLocations.editLocation(locationId.intValue(), name, floor.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean deleteLocation(Long locationId) throws LocationServiceException {
        return BaseLocations.deleteLocation(locationId.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<Map<String, Object>> getFloors() throws LocationServiceException {
        return BaseLocations.getFloors();
    }

    /**
     * @inheritDoc
     */
    @Override
    public Map<String, Object> getFloor(Long floorId) throws LocationServiceException {
        return BaseLocations.getFloor(floorId.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean addFloor(String name, Long level) throws LocationServiceException {
        return BaseLocations.addFloor(name, level.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean editFloor(Long floorId, String name, Long level) throws LocationServiceException {
        return BaseLocations.editFloor(floorId.intValue(), name, level.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean deleteFloor(Long floorId) throws LocationServiceException {
        return BaseLocations.deleteFloor(floorId.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean setFloorVisual(Long floorId, String imagePath) throws LocationServiceException {
        return BaseLocations.attachFloorVisual(floorId.intValue(), imagePath);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean removeFloorVisual(Long floorId) throws LocationServiceException {
        return BaseLocations.detachFloorVisual(floorId.intValue());
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public boolean setRoomVisual(Long roomId, Long x, Long y, Long w, Long h, Long old) throws LocationServiceException {
        if(old.intValue()!=0){
            if(!removeRoomVisual(old)){
                throw new LocationServiceException("Could not detach old room, no old room id '"+old.intValue()+"' present");
            }
        }
        return updateRoomVisual(roomId, x, y, w, h);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean updateRoomVisual(Long roomId, Long x, Long y, Long w, Long h) throws LocationServiceException {
        return BaseLocations.attachRoomVisual(roomId.intValue(), x.intValue(), y.intValue(), w.intValue(), h.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean removeRoomVisual(Long roomId) throws LocationServiceException {
        return BaseLocations.detachRoomVisual(roomId.intValue());
    }

}
