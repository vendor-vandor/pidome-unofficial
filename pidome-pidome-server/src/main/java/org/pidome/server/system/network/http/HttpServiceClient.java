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
package org.pidome.server.system.network.http;

import org.pidome.server.services.clients.remoteclient.AuthenticationException;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.services.clients.remoteclient.RemoteClientException;
import org.pidome.server.services.clients.remoteclient.RemoteClientsAuthentication;
import org.pidome.server.services.clients.remoteclient.RemoteClientsConnectionPool;

/**
 *
 * @author John
 */
public class HttpServiceClient extends RemoteClient {

    String connectedRemote;
    
    public HttpServiceClient(String username, String password, boolean override, String remoteSocketIp) throws HttpClientLoggedInOnOtherLocationException,HttpClientNotAuthorizedException {
        super(Type.WEB);
        connectedRemote = remoteSocketIp;
        setClientName(username);
        setRemoteHost(remoteSocketIp);
        try {
            switch(RemoteClientsAuthentication.authenticateWebClient(this, username, password, override)){
                case FAILED:
                    throw new HttpClientNotAuthorizedException("Invalid username and/or password");
                case OK:
                    //// Done.
                break;
            }
        } catch (AuthenticationException | RemoteClientException ex) {
            throw new HttpClientNotAuthorizedException(ex.getMessage());
        }
    }

    @Override
    public boolean sendSocket(String nameSpace, byte[] message) {
        return true;
    }

    @Override
    public String getRemoteSocketAddress() {
        return this.connectedRemote;
    }

    @Override
    public void finish() {
        RemoteClientsAuthentication.deAuthorize(this);
    }
    
}
