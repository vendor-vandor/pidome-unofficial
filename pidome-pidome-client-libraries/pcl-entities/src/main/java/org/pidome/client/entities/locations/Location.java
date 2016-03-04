/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.locations;

import java.util.Map;
import org.pidome.pcl.utilities.properties.ReadOnlyStringPropertyBindingBean;
import org.pidome.pcl.utilities.properties.StringPropertyBindingBean;

/**
 *
 * @author John
 */
public class Location {
    
    /**
     * Main category id.
     */
    private int floorId;
    
    /**
     * Sub category id where all is linked to.
     */
    private final int locationdId;
    
    /**
     * Bindable String property with room name.
     */
    private final StringPropertyBindingBean name = new StringPropertyBindingBean();
    
    /**
     * Bindable String property with floor name.
     */
    private final StringPropertyBindingBean floorName = new StringPropertyBindingBean();
    
    /**
     * Bindable String property with combined names.
     */
    private final StringPropertyBindingBean combinedName = new StringPropertyBindingBean();
    
    /**
     * Constructor.
     * @param data Promised data from RPC.
     */
    protected Location(Map<String,Object> data){
        this.locationdId = ((Number)data.get("id")).intValue();
        this.floorId = ((Number)data.get("floor")).intValue();
        floorName.setValue((String)data.get("floorname"));
        name.setValue((String)data.get("name"));
        combinedName.setValue((String)data.get("floorname") + " - " + (String)data.get("name"));
    }
    
    /**
     * Update basic data.
     * @param data RPC promised data.
     */
    protected final void update(Map<String,Object> data){
        this.floorId = ((Number)data.get("floorid")).intValue();
        floorName.setValue((String)data.get("floorname"));
        name.setValue((String)data.get("name"));
    }
    
    /**
     * Returns the location id used by other entities.
     * @return int location id.
     */
    public final int getLocationId(){
        return this.locationdId;
    }
    
    /**
     * Returns a read only boundable bean.
     * @return Bindable String bean with the name.
     */
    public final ReadOnlyStringPropertyBindingBean getRoomName(){
        return name.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Returns a read only boundable bean.
     * @return Bindable String bean with the name.
     */
    public final ReadOnlyStringPropertyBindingBean getFloorName(){
        return floorName.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Returns the combined name.
     * @return Bindable combined name.
     */
    public final ReadOnlyStringPropertyBindingBean getCombinedName(){
        return this.combinedName.getReadOnlyBooleanPropertyBindingBean();
    }
    
}