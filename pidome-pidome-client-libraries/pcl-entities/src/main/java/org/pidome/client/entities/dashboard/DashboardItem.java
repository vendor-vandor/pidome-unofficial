/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.dashboard;

import java.util.Map;

/**
 * A dashboard item.
 * @author John
 */
public abstract class DashboardItem {
    
    private int row;
    private int column;
    private int size_x;
    private int size_y;
    
    private final ItemType itemType;
    
    /**
     * Possible dashboard item types.
     */
    public enum ItemType {
        DEVICE,MACRO,SCENE,WEATHER,TIME,SPACER
    }
    
    /**
     * Protected constructor.
     * @param type The dashboard ItemType
     */
    protected DashboardItem(ItemType type){
        this.itemType = type;
    }
    
    /**
     * Sets item dimensions.
     * @param row The item's row.
     * @param column The item's column
     * @param size_x The item's column span.
     * @param size_y The items row span.
     */
    protected final void setDimentions(int row, int column, int size_x, int size_y){
        this.row = row;
        this.column = column;
        this.size_x = size_x;
        this.size_y = size_y;
    }
    
    /**
     * Returns the item type.
     * @return The dashboard ItemType
     */
    public final ItemType getItemType(){
        return this.itemType;
    }
    
    /**
     * Returns the row.
     * @return the row this item is in.
     */
    public final int getRow(){
        return row;
    }
    
    /**
     * Returns the column.
     * @return The column this item is in.
     */
    public final int getColumn(){
        return column;
    }
    
    /**
     * Returns the column span.
     * @return The amountof columns this item holds.
     */
    public final int getSizeX(){
        return size_x;
    }
    
    /**
     * Return the row span.
     * @return The amount of rows this item holds.
     */
    public final int getSizeY(){
        return size_y;
    }
    
    /**
     * Set the item config.
     * @param config Map with config items from RPC.
     */
    protected abstract void setConfig(Map<String,Object> config);
    
}