/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.categories;

import java.util.Map;
import org.pidome.pcl.utilities.properties.ReadOnlyStringPropertyBindingBean;
import org.pidome.pcl.utilities.properties.StringPropertyBindingBean;

/**
 *
 * @author John
 */
public class Category {
    
    /**
     * Main category id.
     */
    private int mainCatId;
    
    /**
     * Sub category id where all is linked to.
     */
    private final int subCatId;
    
    /**
     * Boundable String property.
     */
    private final StringPropertyBindingBean name = new StringPropertyBindingBean();
    
    /**
     * This category's constant.
     */
    private final String constant;
    
    /**
     * Constructor.
     * @param data Promised data from RPC.
     */
    protected Category(Map<String,Object> data){
        this.mainCatId = ((Number)data.get("id")).intValue();
        this.subCatId = ((Number)data.get("category")).intValue();
        name.setValue((String)data.get("name"));
        constant = (String)data.get("constant");
    }
    
    /**
     * Update basic data.
     * @param data RPC promised data.
     */
    protected final void update(Map<String,Object> data){
        this.mainCatId = ((Number)data.get("id")).intValue();
        name.setValue((String)data.get("name"));
    }
    
    /**
     * Returns the category id used by other entities.
     * @return int caategory id.
     */
    public final int getCategoryId(){
        return this.subCatId;
    }
    
    /**
     * Returns a read only boundable bean.
     * @return Boundable String bean with the name.
     */
    public final ReadOnlyStringPropertyBindingBean getName(){
        return name.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Returns the constant for this category.
     * @return Strinng presentation of this category constant.
     */
    public final String getConstant(){
        return this.constant;
    }
    
}
