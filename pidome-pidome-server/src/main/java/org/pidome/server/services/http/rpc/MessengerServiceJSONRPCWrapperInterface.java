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

package org.pidome.server.services.http.rpc;

import java.util.List;
import java.util.Map;
import org.pidome.server.system.categories.CategoriesException;

/**
 *
 * @author John
 */
public interface MessengerServiceJSONRPCWrapperInterface {
    
    /**
     * Retrieves the full category list where categories and sub categories are combined and the id is the link id for devices.
     * @return
     * @throws CategoriesException
     */
    public List<Map<String,Object>> getMessageTypes() throws CategoriesException;
    
}
