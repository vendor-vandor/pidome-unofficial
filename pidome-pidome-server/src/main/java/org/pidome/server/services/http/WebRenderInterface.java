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

package org.pidome.server.services.http;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.services.clients.remoteclient.RemoteClientInterface;

/**
 *
 * @author John Sirach
 */
public abstract class WebRenderInterface {
    public abstract String render() throws Exception;
    public abstract int getOutputSize();
    public abstract void setOutputStream(ByteArrayOutputStream outputStream) throws UnsupportedEncodingException;
    public abstract void setTemplate(String template);
    public abstract void setRequestData(Map<String,List<String>> data);
    public abstract void setPostData(Map data);
    public abstract void setFileData(Map<String,byte[]> data);
    public abstract void collect();
    public abstract void setHostData(String hostName, int port, String remoteClientHost);
    public abstract void setLoginData(RemoteClientInterface client, RemoteClient initiator, String loginError);
}
