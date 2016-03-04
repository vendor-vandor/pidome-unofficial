/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.utilities.properties;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Read only observable array list support.
 * @author John
 * @param <Type> List of type Type.
 */
public final class ReadOnlyObservableArrayListBean<Type> {
    
    /**
     * Original ObservableArrayListBean.
     */
    ObservableArrayListBean<Type> original = new ObservableArrayListBean<>();
    
    /**
     * Constructor.
     * @param original original list.
     */
    public ReadOnlyObservableArrayListBean(ObservableArrayListBean<Type> original){
        this.original = original;
    }
 
    /**
     * Adds a change listener.
     * @param listener Property change listener.
     */
    public void addListener(ObservableArrayListBeanChangeListener<? super Type> listener){
        original.addListener(listener);
    }
    
    /**
     * Removes a listener.
     * @param listener Property change listener.
     */
    public void removeListener(ObservableArrayListBeanChangeListener<? super Type> listener){
        original.removeListener(listener);
    }

    /**
     * Returns list size.
     * @return The size.
     */
    public int size() {
        return original.size();
    }

    /**
     * Checks if list is empty
     * @return true when empty.
     */
    public boolean isEmpty() {
        return original.isEmpty();
    }

    /**
     * Check if a specific object is present in the list.
     * @param o Object to check
     * @return true when present.
     */
    public boolean contains(Type o) {
        return original.contains(o);
    }

    /**
     * Returns the index of Type in the list.
     * @param o The object to check
     * @return object index in the list.
     */
    public int indexOf(Type o) {
        return original.indexOf(o);
    }

    /**
     * Returns the last index of type in the list.
     * @param o The object to check
     * @return last index in the list.
     */
    public int lastIndexOf(Type o) {
        return original.lastIndexOf(o);
    }
    
    /**
     * Returns the list as an array.
     * @param <T> Object Type
     * @param ts an Array of Type.
     * @return The result.
     */
    public <T extends Object> T[] toArray(T[] ts) {
        return original.toArray(ts);
    }
    
    /**
     * Returns an object at position i.
     * @param i index number.
     * @return The object found at i
     */
    public Type get(int i) {
        return original.get(i);
    }
    
    /**
     * Returns a list iterator.
     * @param i index
     * @return List iterator.
     */
    public ListIterator<Type> listIterator(int i) {
        return original.listIterator(i);
    }

    /**
     * Returns a list iterator.
     * @return The list iterator.
     */
    public ListIterator<Type> listIterator() {
        return original.listIterator();
    }

    /**
     * Returns an iterator.
     * @return The list iterator.
     */
    public Iterator<Type> iterator() {
        return original.iterator();
    }

    /**
     * Returns a sublist.
     * @param i begin index.
     * @param i1 end index
     * @return a sublist between indexes.
     */
    public List<Type> subList(int i, int i1) {
        return original.subList(i, i1);
    }
    
}