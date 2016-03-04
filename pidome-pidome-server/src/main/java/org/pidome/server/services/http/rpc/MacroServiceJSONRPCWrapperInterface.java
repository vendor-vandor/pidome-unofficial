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

import java.util.ArrayList;
import org.pidome.server.services.macros.MacroException;

/**
 *
 * @author John
 */
public interface MacroServiceJSONRPCWrapperInterface {
    
    /**
     * Runs a specific macro.
     * @param macroId
     * @return 
     * @throws org.pidome.server.services.macros.MacroException 
     */
    public Object runMacro(Long macroId) throws MacroException;
    
    /**
     * Returns a list of macro's.
     * @return 
     */
    public Object getMacros();
    
    /**
     * Returns a list of favorite macro's
     * @return 
     */
    public Object getFavoriteMacros();
    
    /**
     * Returns a single trigger.
     * @param id
     * @return
     * @throws org.pidome.server.services.macros.MacroException
     */
    public Object getMacro(Long id) throws MacroException;
    
    /**
     * Deletes a trigger
     * @param macroId
     * @return
     * @throws org.pidome.server.services.macros.MacroException
     */
    @PiDomeJSONRPCPrivileged
    public Object deleteMacro(Long macroId) throws MacroException;
    
    /**
     * Saves a trigger on the system.
     * @param name
     * @param description
     * @param favorite
     * @param exec
     * @return
     * @throws org.pidome.server.services.macros.MacroException
     */
    @PiDomeJSONRPCPrivileged
    public Object saveMacro(String name, String description, Boolean favorite, ArrayList exec) throws MacroException;
    
    /**
     * Updates a trigger in the system.
     * @param macroId
     * @param name
     * @param description
     * @param favorite
     * @param exec
     * @return
     * @throws org.pidome.server.services.macros.MacroException
     */
    @PiDomeJSONRPCPrivileged
    public Object updateMacro(Long macroId, String name, String description, Boolean favorite, ArrayList exec) throws MacroException;
    
    /**
     * Set a favorite macro.
     * @param macroId
     * @param favorite
     * @return 
     * @throws org.pidome.server.services.macros.MacroException 
     */
    public Object setFavorite(Long macroId, Boolean favorite) throws MacroException;
    
}
