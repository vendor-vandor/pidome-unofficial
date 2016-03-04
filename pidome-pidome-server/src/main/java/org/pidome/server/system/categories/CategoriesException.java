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

package org.pidome.server.system.categories;

/**
 *
 * @author John
 */
public class CategoriesException extends Exception {

    /**
     * Creates a new instance of <code>CategoriesException</code> without detail
     * message.
     */
    public CategoriesException() {
    }

    /**
     * Constructs an instance of <code>CategoriesException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public CategoriesException(String msg) {
        super(msg);
    }
}
