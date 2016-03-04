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

package org.pidome.client.scenes.floormap;

/**
 *
 * @author John
 */
public class VisualFloorsAssetItemException extends Exception {

    /**
     * Creates a new instance of <code>VisualFloorsAssetItemException</code>
     * without detail message.
     */
    public VisualFloorsAssetItemException() {
    }

    /**
     * Constructs an instance of <code>VisualFloorsAssetItemException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public VisualFloorsAssetItemException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>VisualFloorsAssetItemException</code>
     * with the specified throwable.
     *
     * @param ex throwable.
     */
    public VisualFloorsAssetItemException(Throwable ex) {
        super(ex);
    }
    
}
