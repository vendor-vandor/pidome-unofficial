/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.categories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.domotics.components.DomComponent;

/**
 * Holds all the categories
 * @author John Sirach
 */
public class Categories implements DomComponent {

    static Map<Integer,Map<String,Object>> categories = new HashMap<>();
    
    static Map<String,Object>defaultEmpty = new HashMap<>();
    
    static Logger LOG = LogManager.getLogger(Categories.class);
    
    /**
     * Constructor.
     * @param catArray 
     */
    public Categories(ArrayList<Map<String,Object>> catArray){
        defaultEmpty.put("id", 0);
        defaultEmpty.put("name", "Unknown - Unknown");
        defaultEmpty.put("constant", "UNKNOWN");
        catArray.stream().forEach((category) -> {
            createCategory(category);
        });
    }
    
    /**
     * Creates a category.
     * @param info 
     */
    public final void createCategory(Map<String,Object> info){
        LOG.trace("Create category: {}", info);
        categories.put(((Long)info.get("id")).intValue(), info);
    }
    
    /**
     * Returns a category.
     * @param catId
     * @return 
     */
    public static Map<String,Object>getCategory(int catId){
        if(categories.containsKey(catId)){
            return categories.get(catId);
        } else {
            return defaultEmpty;
        }
    }
    
    /**
     * Returns a category name.
     * @param catId
     * @return 
     */
    public static String getCategoryName(int catId){
        if(categories.containsKey(catId)){
            return (String)categories.get(catId).get("name");
        } else {
            return "Unknown";
        }
    }

    /**
     * Returns a category constant.
     * @param catId
     * @return 
     */
    public static String getCategoryConstant(int catId){
        if(categories.containsKey(catId)){
            return (String)categories.get(catId).get("constant");
        } else {
            return "Unknown";
        }
    }
    
}
