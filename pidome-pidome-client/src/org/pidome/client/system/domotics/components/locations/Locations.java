/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.locations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.DomComponent;

/**
 *
 * @author John Sirach
 */
public class Locations implements DomComponent {

    static Map<Integer,String> locations = new HashMap<>();
    ObservableMap<Integer,String> observableLocations = FXCollections.observableMap(locations);
    
    static Logger LOG = LogManager.getLogger(Locations.class);
    
    static List _locationChangedListeners = new ArrayList();
    
    public Locations(ArrayList<Map<String,Object>> locationData){
        startLocationChangeListener();
        locationData.stream().forEach((location) -> {
            createLocation(location);
        });
    }
    
    public final void createLocation(Map<String,Object> info){
        LOG.trace("Create location: {}", info);
        observableLocations.put(((Long)info.get("id")).intValue(), info.get("floorname") + " - " + info.get("name"));
    }

    public final void updateLocation(Map<String,Object> info){
        LOG.debug("Updating: {}", info);
        if(locations.containsKey(((Long)info.get("id")).intValue())){
            observableLocations.put(((Long)info.get("id")).intValue(), info.get("floorname") + " - " + info.get("name"));
        }
    }
    
    final void startLocationChangeListener(){
        observableLocations.addListener((MapChangeListener.Change<? extends Integer, ? extends String> change) -> {
            if (change.wasRemoved() && change.wasAdded()){
                _fireLocationChangeEvent(LocationsEvent.LOCATIONUPDATED, change.getKey(), change.getValueAdded());
            } else if (change.wasRemoved()){
                _fireLocationChangeEvent(LocationsEvent.LOCATIONREMOVED, change.getKey(), change.getValueRemoved());
            } else if(change.wasAdded()){
                _fireLocationChangeEvent(LocationsEvent.LOCATIONADDED, change.getKey(), change.getValueAdded());
            }
        });
    }
    
    public static Map<Integer,String> getLocations(){
        return locations;
    }
    
    public static String getLocation(int id) throws DomComponentsException {
        if(locations.containsKey(id)){
            return locations.get(id);
        } else {
            LOG.error("Location id {} does not exist", id);
            throw new DomComponentsException("Location id " + id + " does not exist");
        }
    }
    
    public static synchronized void addLocationsEventListener(LocationsEventListener l){
        LOG.debug("Added location event listener {}", l.getClass().getName());
        _locationChangedListeners.add(l);
    }

    public static synchronized void removeLocationsEventListener(LocationsEventListener l){
        LOG.debug("Removed location event listener {}", l.getClass().getName());
        _locationChangedListeners.remove(l);
    }
    
    private synchronized void _fireLocationChangeEvent(String EVENTTYPE, int locationId, String locationName) {
        LOG.debug("Event: {}", EVENTTYPE);
        LocationsEvent event = new LocationsEvent(this, EVENTTYPE);
        event.setLocationData(locationId, locationName);
        Iterator listeners = _locationChangedListeners.iterator();
        while (listeners.hasNext()) {
            ((LocationsEventListener) listeners.next()).handleLocationsEvent(event);
        }
    }
    
}
