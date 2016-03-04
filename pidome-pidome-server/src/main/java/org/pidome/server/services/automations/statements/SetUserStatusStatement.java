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
import org.pidome.server.system.userstatus.UserStatusException;
import org.pidome.server.system.userstatus.UserStatusService;

/**
 *
 * @author John
 */
public class SetUserStatusStatement extends AutomationStatement {

    private int userStatusId = 1;
    
    public SetUserStatusStatement(int userStatusId){
        super(new StringBuilder("UserStatusStatement_").append(userStatusId).toString());
        this.userStatusId = userStatusId;
    }
    
    @Override
    public boolean run() {
        try {
            UserStatusService.setUserStatus(userStatusId);
        } catch (UserStatusException ex) {
            Logger.getLogger(SetUserStatusStatement.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    @Override
    public void destroy() {
        /// Not used
    }
    
}
