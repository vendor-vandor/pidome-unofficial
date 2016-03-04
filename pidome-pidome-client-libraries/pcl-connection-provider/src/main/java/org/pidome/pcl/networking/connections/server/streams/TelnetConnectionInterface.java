/*
 * Copyright 2014 John.
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

package org.pidome.pcl.networking.connections.server.streams;

import java.io.IOException;

/**
 * Interface for connection data.
 * @author John
 */
public interface TelnetConnectionInterface {
    
    /**
     * Connect to a socket.
     * @throws java.io.IOException When connection fails
     */
    public void connect() throws IOException;
    
    /**
     * Raw port reader.
     */
    public void reader();
    
    /**
     * Sends data over a raw socket.
     * @param data data to be send.
     * @throws IOException When data can not be send (socket failure).
     */
    public void send(String data) throws IOException;
    
    /**
     * Stops the connection and closes the port.
     */
    public void stop();
    
    /**
     * Returns the last known data.
     * @return The data received.
     */
    public String getData();
    
    /**
     * Adds a socket listener.
     * @param l Socket events listener.
     */
    public void addEventListener(TelnetEventListener l);

    /**
     * Removes a socket listener.
     * @param l Socket events listener.
     */
    public void removeEventListener(TelnetEventListener l);
    
}
