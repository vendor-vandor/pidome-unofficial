/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base entity class used internally.
 * @author John
 */
public abstract class Entity {
    
    static {
        Logger.getLogger(Entity.class.getName()).setLevel(Level.ALL);
    }
    
    protected boolean loaded = false;
    
    /**
     * Initializes an entity.
     * This can be used to set listeners and pre-data.
     */
    protected abstract void initilialize();
    
    /**
     * Releases any information.
     * This should be implemented so entities are able to release their data
     * and/or remove listeners in case of unloading.
     */
    protected abstract void release();
    
    /**
     * Runs preloaders.
     * Pre loaders are run to make sure that all the data is available.
     * @throws org.pidome.client.entities.EntityNotAvailableException When preload fails.
     */
    public abstract void preload() throws EntityNotAvailableException;
    
    /**
     * Reloads listings and/or contents.
     * @throws EntityNotAvailableException When reloading fails.
     */
    public abstract void reload() throws EntityNotAvailableException;
    
    /**
     * Unloads any content.
     * @throws EntityNotAvailableException When unloading fails.
     */
    public abstract void unloadContent() throws EntityNotAvailableException;
    
}
