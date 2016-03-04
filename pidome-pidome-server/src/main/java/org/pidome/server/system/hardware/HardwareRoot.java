/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.server.system.hardware;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author John Sirach <john.sirach@gmail.com>
 */
public abstract class HardwareRoot {
    
    /**
     * List of listeners
     */
    List _listeners = new ArrayList();
    
    /**
     * Returns a list of listeners.
     * @return 
     */
    public final List getListeners(){
        return _listeners;
    }
    
    /**
     * Starts discovery with the specified hardware.
     * @throws UnsupportedOperationException 
     */
    public abstract void discover() throws UnsupportedOperationException;
    
    /**
     * For starting the internals of the hardware peripheral system.
     * This should e used for setting listeners etc. if needed.
     * @throws UnsupportedOperationException 
     */
    public abstract void start() throws UnsupportedOperationException;
    
    /**
     * Stopping the hardware internals.
     * @throws UnsupportedOperationException 
     */
    public abstract void stop() throws UnsupportedOperationException;
    
    /**
     * Adds a mutation listener.
     * @param l 
     */
    public final synchronized void addHardwareMutationListener( HardwareMutationListener l ) {
        _listeners.add( l );
    }
    
    /**
     * Removes a mutation listener.
     * @param l 
     */
    public final synchronized void removeHardwareMutationListener( HardwareMutationListener l ) {
        _listeners.remove( l );
    }    
    
    /**
     * An event which should be fired when discovery is done.
     * If designed correctly the discovery should NOT start any internal threads
     */
    protected final void _fireDiscoveryDoneEvent(){
        HardwareDiscoveryDoneEvent event = new HardwareDiscoveryDoneEvent(this);
        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (HardwareDiscoveryDoneListener) listeners.next() ).discoveryDoneHandler(event );
        }
    }
    
    
}
