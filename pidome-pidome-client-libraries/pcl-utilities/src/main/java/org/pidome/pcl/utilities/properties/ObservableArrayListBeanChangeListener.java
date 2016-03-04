/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.utilities.properties;

import java.util.Collection;

/**
 * Change listener support for array list.
 * @author John
 * @param <Type> The type for the change listener.
 */
@FunctionalInterface
public interface ObservableArrayListBeanChangeListener<Type> {
    
    /**
     * The change event class.
     * @param <Type> The type of the change.
     */
    public class Change<Type> {
        
        /**
         * If there is a change next.
         */
        private boolean hasNext    = true;
        /**
         * If there was added to the list.
         */
        private boolean wasAdded   = false;
        /**
         * If there was removed from the list.
         */
        private boolean wasRemoved = false;
        
        /**
         * Collection of added items.
         */
        Collection<? extends Type> addedList;
        /**
         * Collection of removed items.
         */
        Collection<? extends Type> removedList;
        
        /**
         * Constructor.
         */
        protected void Change(){}
        
        /**
         * Returns true if there is a next change.
         * @return true if there is a change.
         */
        public final boolean hasNext(){
            return hasNext;
        }
        
        /**
         * Returns if there is an upcoming change.
         * @return true if there is a next change and advances.
         */
        public final boolean next(){
            if(hasNext==true){
                hasNext = false;
                return true;
            } else {
                return hasNext;
            }
        }

        /**
         * Check if it was an add action.
         * @return true if was added.
         */
        public final boolean wasAdded(){
            return wasAdded;
        }
        
        /**
         * Check if there was a remove action.
         * @return true if items where removed.
         */
        public final boolean wasRemoved(){
            return wasRemoved;
        }
        
        /**
         * Sets the items added.
         * @param list The items to add.
         */
        public void setAddedList(Collection<? extends Type> list){
            wasAdded  = true;
            addedList = list;
        }
        
        /**
         * Sets the items removed.
         * @param list The items removed.
         */
        public void setRemovedList(Collection<? extends Type> list){
            wasRemoved  = true;
            removedList = list;
        }
        
        /**
         * Returns the list of added items.
         * @return The added items.
         */
        public Collection<? extends Type> getAddedSubList(){
            return addedList;
        }
        
        /**
         * Returns the list of removed items.
         * @return The removed items.
         */
        public Collection<? extends Type> getRemoved(){
            return removedList;
        }
        
    }
    
    /**
     * The change event.
     * @param c The change event.
     */
    public void onChanged(ObservableArrayListBeanChangeListener.Change<? extends Type> c);
   
}
