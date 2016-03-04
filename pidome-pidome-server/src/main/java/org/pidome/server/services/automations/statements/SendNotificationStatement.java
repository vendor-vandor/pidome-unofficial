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

import java.util.List;
import org.pidome.server.services.automations.variables.TextAutomationVariable;
import org.pidome.server.system.audit.Notifications;

/**
 *
 * @author John
 */
public final class SendNotificationStatement extends AutomationStatement {

    private String title = "Message";
    private String message = "";
    private Notifications.NotificationType type = Notifications.NotificationType.INFO;
    
    List<TextAutomationVariable> texts;
    
    public SendNotificationStatement(){
        super("SendNotificationStatement");
    }
    
    public final void setTitle(String title){
        this.title = title;
    }
    
    public final void setMessage(List<TextAutomationVariable> message){
        this.texts = message;
    }
    
    public final void setType(String type){
        switch(type){
            case "WARNING":
                this.type = Notifications.NotificationType.WARNING;
            break;
            case "ERROR":
                this.type = Notifications.NotificationType.ERROR;
            break;
            case "OK":
                this.type = Notifications.NotificationType.OK;
            break;
            default:
                this.type = Notifications.NotificationType.INFO;
            break;
        }
    }
    
    @Override
    public boolean run() {
        StringBuilder message = new StringBuilder();
        for(TextAutomationVariable text:texts){
            message.append(text.getProperty().getValue());
        }
        Notifications.sendMessage(type, title, message.toString());
        return true;
    }

    @Override
    public void destroy() {
        for(TextAutomationVariable text:texts){
            text.destroy();
        }
    }
    
}
