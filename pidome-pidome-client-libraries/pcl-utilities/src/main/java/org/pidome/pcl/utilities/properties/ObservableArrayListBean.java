/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.utilities.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * An observable array list of type Type.
 * @author John
 * @param <Type> The type for this arraylist.
 */
public class ObservableArrayListBean<Type> extends ArrayList<Type> {
    
    /**
     * List of listeners.
     */
    ArrayList<ObservableArrayListBeanChangeListener> listeners = new ArrayList<>();
    
    /**
     * Init empty list.
     */
    public ObservableArrayListBean(){}
    
    /**
     * Adds a list listener.
     * @param listener ObservableArrayListBean listener.
     */
    public void addListener(ObservableArrayListBeanChangeListener<? super Type> listener){
        if(!listeners.contains(listener)){
            listeners.add(listener);
        }
    }
    
    /**
     * Removes a listener.
     * @param listener ObservableArrayListBean listener.
     */
    public void removeListener(ObservableArrayListBeanChangeListener<? super Type> listener){
        listeners.remove(listener);
    }
    
    /**
     * Returns a list of listeners.
     * @return List of ObservableArrayListBean listeners.
     */
    protected final ArrayList<ObservableArrayListBeanChangeListener> getListeners(){
        return listeners;
    }
    
    /**
     * Add an item to the list.
     * @param item Item to be added of Type.
     * @return true if added.
     */
    @Override
    public boolean add(Type item){
        boolean result = super.add(item);
        if(result){
            ArrayList<Type> added = new ArrayList<>();
            added.add(item);
            publishAddedChange(added);
        }
        return result;
    }
    
    /**
     * Adds a list of items.
     * @param items Collection of item of type Type.
     * @return true if added.
     */
    @Override
    public boolean addAll(Collection<? extends Type> items){
        boolean result = super.addAll(items);
        if(result){
            publishAddedChange(items);
        }
        return result;
    }
    
    /**
     * Notifies listeners of items added.
     * @param added The collection added.
     */
    private void publishAddedChange(Collection<? extends Type> added){
        ObservableArrayListBeanChangeListener.Change<Type> change = new ObservableArrayListBeanChangeListener.Change();
        change.setAddedList(added);
        Iterator<ObservableArrayListBeanChangeListener> listenersSet = listeners.iterator();
        while(listenersSet.hasNext()){
            listenersSet.next().onChanged(change);
        }
    }
    
    /**
     * Removes an item.
     * @param item The item to remove.
     * @return true if the item is removed.
     */
    @Override
    public boolean remove(Object item){
        boolean result = super.remove(item);
        if(result){
            ArrayList<Object> added = new ArrayList<>();
            added.add(item);
            publishRemovedChange((Collection<? extends Type>)added);
        }
        return result;
    }
    
    /**
     * Remove a list of items.
     * @param items Collection of items of Type to remove.
     * @return Remove result.
     */
    @Override
    public boolean removeAll(Collection<? extends Object> items){
        boolean result = super.removeAll(items);
        if(result){
            publishRemovedChange((Collection<? extends Type>)items);
        }
        return result;
    }
    
    @Override 
    public final void clear(){
        List<Type> newList = new ArrayList<>(this);
        removeAll(newList);
    }
    
    /**
     * Publishers removal of items.
     * @param removed The collection removed.
     */
    private void publishRemovedChange(Collection<? extends Type> removed){
        ObservableArrayListBeanChangeListener.Change<Type> change = new ObservableArrayListBeanChangeListener.Change();
        change.setRemovedList(removed);
        Iterator<ObservableArrayListBeanChangeListener> listenersSet = listeners.iterator();
        while(listenersSet.hasNext()){
            listenersSet.next().onChanged(change);
        }
    }
    
}