/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.utilities.properties.generics;

import java.beans.PropertyChangeEvent;

/**
 * A property value change event is a Java Beans property change event
 * retrofitted to use generics to cast to proper value type.
 *
 * @param <V> The type of property value.
 * @author Garret Wilson
 */
public class GenericPropertyChangeEvent<V> extends PropertyChangeEvent {

    public GenericPropertyChangeEvent(final Object source, final String propertyName, final V oldValue, V newValue) {
        super(source, propertyName, oldValue, newValue);
    }

    @SuppressWarnings("unchecked")
    public GenericPropertyChangeEvent(final PropertyChangeEvent propertyChangeEvent) {
        this(propertyChangeEvent.getSource(), propertyChangeEvent.getPropertyName(), (V) propertyChangeEvent.getOldValue(),(V) propertyChangeEvent.getNewValue());
        setPropagationId(propertyChangeEvent.getPropagationId());
    }

    @SuppressWarnings("unchecked")
    @Override
    public V getOldValue() {
        return (V) super.getOldValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public V getNewValue() {
        return (V) super.getNewValue();
    }
}
