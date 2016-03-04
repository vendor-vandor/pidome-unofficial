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

package org.pidome.server.connector.tools;

/**
 *
 * @author John Sirach
 */
public final class PlatformException extends Exception {
    
    public PlatformException (){}

    public PlatformException (String message){
        super (message);
    }

    public PlatformException (Throwable cause){
        super (cause);
    }

    public PlatformException (String message, Throwable cause){
        super (message, cause);
    }
}