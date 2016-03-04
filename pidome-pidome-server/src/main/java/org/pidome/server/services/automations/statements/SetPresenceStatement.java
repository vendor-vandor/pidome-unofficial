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

package org.pidome.server.services.automations.statements;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.server.system.presence.PresenceException;
import org.pidome.server.system.presence.PresenceService;

/**
 *
 * @author John
 */
public class SetPresenceStatement extends AutomationStatement {

    private int userPresence = 1;
    
    public SetPresenceStatement(int userPresence){
        super(new StringBuilder("UserPresenceStatement_").append(userPresence).toString());
        this.userPresence = userPresence;
    }
    
    @Override
    public boolean run() {
        try {
            PresenceService.activateGlobalPresence(userPresence);
        } catch (PresenceException ex) {
            Logger.getLogger(SetPresenceStatement.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    @Override
    public void destroy() {
        /// Not used
    }
    
}
