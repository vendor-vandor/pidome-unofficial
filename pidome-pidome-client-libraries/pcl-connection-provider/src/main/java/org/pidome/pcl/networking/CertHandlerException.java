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

package org.pidome.pcl.networking;

/**
 * USed when there is an issue with certificate handling.
 * @author John
 */
public class CertHandlerException extends Exception {

    /**
     * Creates a new instance of <code>CertHandlerException</code> without
     * detail message.
     */
    public CertHandlerException() {
    }

    /**
     * Constructs an instance of <code>CertHandlerException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public CertHandlerException(String msg) {
        super(msg);
    }
}
