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
package org.pidome.server.services.http.management.desktop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.system.network.http.HttpClientLoggedInOnOtherLocationException;
import org.pidome.server.system.network.http.HttpClientNotAuthorizedException;
import org.pidome.server.system.network.http.HttpServiceClient;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.services.clients.remoteclient.RemoteClientsConnectionPool;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John
 */
public class Webclient_login extends Webservice_renderer {
    
    static Logger LOG = LogManager.getLogger(Webclient_login.class);
    
    
    @Override
    public String render() throws Exception {
        if(postDataMap.containsKey("username") && postDataMap.containsKey("password")){
            if(postDataMap.get("username").isEmpty() || postDataMap.get("password").isEmpty()){
                return "error:Please fill in username and password.";
            } else {
                try {
                    RemoteClient client = new HttpServiceClient(postDataMap.get("username"), postDataMap.get("password"), postDataMap.containsKey("override"),getRemoteClientIp());
                    LOG.warn("Client logged in: {} via {}", client.getClientName(), client.getRemoteHost());
                    return "success:" + client.getKey();
                } catch (HttpClientLoggedInOnOtherLocationException ex){
                    LOG.warn("Client {} already logged in: {}", postDataMap.get("username"), ex.getMessage());
                    return "alreadyloggedin:" + ex.getMessage() + " log in again to override and log out at the other location";
                } catch (HttpClientNotAuthorizedException ex){
                    LOG.warn("Client not logged in: {}", ex.getMessage());
                    return "error:" + ex.getMessage();
                }
            }
        } else if(getDataMap.containsKey("signOff")){
            RemoteClientsConnectionPool.disconnectClient(getRemoteUser().getLoginName(), "Web interface logoff");
            return "signedoff:true";
        } else {
            return super.render();
        }
    }
    
    
}
