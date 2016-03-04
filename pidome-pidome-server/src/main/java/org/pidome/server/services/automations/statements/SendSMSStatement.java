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
import org.apache.logging.log4j.LogManager;
import org.pidome.server.connector.plugins.messengers.MessengerException;
import org.pidome.server.services.automations.variables.TextAutomationVariable;
import org.pidome.server.services.plugins.MessengerPluginService;

/**
 *
 * @author John
 */
public final class SendSMSStatement extends AutomationStatement {

    List<TextAutomationVariable> texts;
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(SendSMSStatement.class);
    
    public SendSMSStatement(){
        super("SendSMSStatement");
    }
    
    public final void setMessage(List<TextAutomationVariable> message){
        this.texts = message;
    }
    
    @Override
    public boolean run() {
        StringBuilder message = new StringBuilder();
        for(TextAutomationVariable text:texts){
            message.append(text.getProperty().getValue());
        }
        try {
            MessengerPluginService.getInstance().sendSmsMessage(message.toString());
        } catch (MessengerException ex) {
            LOG.warn("Could not send sms: {}", ex.getMessage());
        }
        return true;
    }

    @Override
    public void destroy() {
        for(TextAutomationVariable text:texts){
            text.destroy();
        }
    }
    
}
