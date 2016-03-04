/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.utilities.properties.generics;

import java.beans.PropertyChangeListener;

/**
 * A Java Beans property change listener retrofitted to use generics to cast to
 * proper value type.
 *
 * @param <V> The type of property value.
 * @author Garret Wilson
 */
public interface GenericPropertyChangeListener<V> extends PropertyChangeListener {
    public void propertyChange(final GenericPropertyChangeEvent<V> genericPropertyChangeEvent);
}