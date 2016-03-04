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

package org.pidome.server.system.config;

/**
 *
 * @author John
 */
public class ConfigPropertiesException extends Exception {

    /**
     * Creates a new instance of <code>ConfigPropertiesException</code> without
     * detail message.
     */
    public ConfigPropertiesException() {
    }

    /**
     * Constructs an instance of <code>ConfigPropertiesException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ConfigPropertiesException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs a ConfigProperties exception with a throwable.
     * @param thrw 
     */
    public ConfigPropertiesException(Throwable thrw){
        super(thrw);
    }
    
}
