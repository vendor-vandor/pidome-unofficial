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

package org.pidome.server.services.messengers;

import org.apache.logging.log4j.LogManager;
import org.pidome.server.connector.plugins.hooks.PiDomeRPCHook;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.services.clients.remoteclient.RemoteClientsConnectionPool;
import org.pidome.server.services.http.rpc.PidomeJSONRPC;

/**
 *
 * @author John Sirach
 */
public class ClientMessenger {
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(ClientMessenger.class);
    
    public static void send(String nameSpace, String method, int locationId, Object paramContent){
        try {
            LOG.debug("Initiating clients send with namespace: {}, method: {}, locationid: {}, content: {} ", nameSpace, method, locationId, paramContent);
            String message = getConstructBroadcast(nameSpace, method, paramContent);
            RemoteClientsConnectionPool.sendClientMessage(nameSpace, locationId, message);
            PiDomeRPCHook.handleRPCMessage(message);
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not send out RPC broadcast: {}", ex.getMessage());
        }
    }
    
    public static void send(String nameSpace, String method, int locationId, Object paramContent, int butNot){
        try {
            LOG.debug("Initiating clients send with namespace: {}, method: {}, locationid: {}, content: {}, excluding: {} ", nameSpace, method, locationId, paramContent, butNot);
            String message = getConstructBroadcast(nameSpace, method, paramContent);
            RemoteClientsConnectionPool.sendClientMessage(nameSpace, locationId, message, butNot);
            PiDomeRPCHook.handleRPCMessage(message);
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not send out RPC broadcast: {}", ex.getMessage());
        }
    }
    
    public static String getConstructBroadcast(String nameSpace, String method, Object paramContent) throws PidomeJSONRPCException{
        return PidomeJSONRPC.constructBroadcast(nameSpace, method, paramContent);
    }
    
}