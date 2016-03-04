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

package org.pidome.server.services.clients.socketservice;

/**
 *
 * @author John Sirach
 */
public final class SocketServiceException extends Exception {
    
    public SocketServiceException (){}

    public SocketServiceException (String message){
        super (message);
    }

    public SocketServiceException (Throwable cause){
        super (cause);
    }

    public SocketServiceException (String message, Throwable cause){
        super (message, cause);
    }
}