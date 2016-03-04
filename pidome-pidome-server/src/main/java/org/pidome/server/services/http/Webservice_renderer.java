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

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.services.clients.remoteclient.RemoteClientInterface;
import org.pidome.server.system.network.http.HttpClientNotAuthorizedException;
import org.pidome.server.system.dayparts.DayPartException;
import org.pidome.server.system.dayparts.DayPartsService;
import org.pidome.server.system.presence.PresenceException;
import org.pidome.server.system.presence.PresenceService;
import org.pidome.server.system.userstatus.UserStatusException;
import org.pidome.server.system.userstatus.UserStatusService;

/**
 * Renderer for endpoint http parsing.
 * @author John Sirach
 */
public abstract class Webservice_renderer extends WebRenderInterface {

    /**
     * Parser configuration.
     */
    static Configuration cfg;
    
    /**
     * Local logger.
     */
    static Logger LOG = LogManager.getLogger(Webservice_renderer.class);
    
    /**
     * Boolean if heap dumps are enabled.
     */
    private boolean heapDumpEnabled = false;
    
    /**
     * The template to use.
     */
    private String tpl;
    /**
     * Local map containing server data.
     */
    private Map<Object,Object> dataMap = new HashMap<>();
    /**
     * Local map containing request data.
     */
    protected Map<String,Map<String,String>> requestData = new HashMap<>();
    /**
     * Local map containing data from GET requests.
     * This map currently only supports non array values.
     */
    public Map<String,String> getDataMap = new HashMap<>();
    /**
     * Local map containing data from POST requests
     */
    public Map<String,String> postDataMap = new HashMap<>();
    /**
     * Local map containing data from a FILE POST request.
     */
    public Map<String,byte[]> fileDataMap = new HashMap<>();
    /**
     * Local map containing server information.
     */
    public Map<String,Object> serverData = new HashMap<>();
    /**
     * Local map referring to all local data.
     */
    protected Map<Object,Object> fullDataMap = new HashMap<>();
    /**
     * The local hostname.
     */
    private String hostName;
    
    /**
     * The remote user if any.
     */
    private RemoteClientInterface user;
    /**
     * Link to the request initiator.
     */
    private RemoteClient initiator;
    /**
     * The output stream containing template parsed data.
     */
    ByteArrayOutputStream outputStream;
    /**
     * The size of the template including the parsed data.
     */
    int outputSize = 0;
    
    /**
     * Sets the current host data.
     * @param hostName The server's ip.
     * @param port The Server's port.
     * @param remoteClientHost The remote client's ip
     */
    @Override
    public final void setHostData(String hostName, int port, String remoteClientHost){
        serverData.put("hostname", hostName);
        serverData.put("hostport", String.valueOf(port));
        this.hostName = remoteClientHost;
    }
    
    /**
     * Returns a remote user.
     * @return 
     * @throws org.pidome.server.system.network.http.HttpClientNotAuthorizedException 
     */
    public final RemoteClientInterface getRemoteUser() throws HttpClientNotAuthorizedException {
        if(user==null){
            throw new HttpClientNotAuthorizedException("Client not authenticated");
        }
        return user;
    }
    
    /**
     * Request for getting the request initiator.
     * @return the initiator for the current request.
     * @throws HttpClientNotAuthorizedException 
     */
    public final RemoteClient getRemoteUserInitiator() throws HttpClientNotAuthorizedException {
        if(initiator==null){
            throw new HttpClientNotAuthorizedException("No client initiator (no valid endpoint)");
        }
        return initiator;
    }
    
    /**
     * Returns the remote socket.
     * @return 
     */
    public final String getRemoteClientIp(){
        return hostName;
    }
    
    /**
     * Sets the initial config.
     * @param documentRoot 
     */
    public static void setConfig(String documentRoot){
        if(cfg==null){
            try {
                cfg = new Configuration();
                cfg.setDirectoryForTemplateLoading(new File(documentRoot));
                cfg.setObjectWrapper(new DefaultObjectWrapper());
                cfg.setDefaultEncoding("UTF-8");
            } catch (IOException ex) {
                LOG.error("Could not instantiate webservices renderer: {}", ex.getMessage());
            }
        }
    }

    /**
     * Sets the remote users login data.
     * @param client
     * @param initiator
     * @param loginError 
     */
    @Override
    public final void setLoginData(RemoteClientInterface client, RemoteClient initiator, String loginError){
        if(loginError!=null && !loginError.isEmpty()){
            serverData.put("userloginerror", loginError);
        } else if(client==null){
            serverData.put("userloginname", "Anonymous");
            serverData.put("userlogintime", "Never");
        } else {
            serverData.put("userloginname",  client.getLoginName());
            serverData.put("userlogintime",  client.getLastLogin());
            serverData.put("userpasschange", client.getIfCpwd());
            serverData.put("userloginid",    client.getId());
            try {
                serverData.put("userloginrole",  client.getRole().role().toString());
            } catch (Exception ex) {
                serverData.put("userloginrole",  "");
            }
            user = client;
            this.initiator = initiator;
        }
    }
    
    /**
     * Sets the output stream.
     * @param outputStream
     * @throws UnsupportedEncodingException 
     */
    @Override
    public void setOutputStream(ByteArrayOutputStream outputStream) throws UnsupportedEncodingException {
        this.outputStream = outputStream;
    }
    
    /**
     * Sets the tempate to be rendered
     * @param template 
     */
    @Override
    public void setTemplate(String template){
        tpl = template;
    }
    
    /**
     * Puts data from Webclient_ classes.
     * @param name
     * @param value 
     */
    public void setData(String name, Object value){
        dataMap.put(name, value);
    }
    
    /**
     * Sets a data map from Webclient_ classes.
     * @param data 
     */
    public void setData(Map data){
        dataMap = data;
    }
    
    /**
     * Sets clients _GET request data.
     * @param data 
     */
    @Override
    public void setRequestData(Map<String,List<String>> data){
        for(Map.Entry<String,List<String>> tmp:data.entrySet()){
            getDataMap.put(tmp.getKey(), tmp.getValue().get(0));
        }
        requestData.put("_GET", getDataMap);
    }
    
    /**
     * Sets clients _POST data.
     * @param data 
     */
    @Override
    public void setPostData(Map data){
        postDataMap = data;
        requestData.put("_POST", postDataMap);
    }
    
    /**
     * Sets file data from uploads.
     * @param data 
     */
    @Override
    public void setFileData(Map<String,byte[]> data){
        fileDataMap = data;
    }
    
    /**
     * Returns a rendered page.
     * @return
     * @throws Exception 
     */
    @Override
    public String render() throws Exception {
        return getRender();
    }
    
    /**
     * Primary collect for putting data.
     */
    @Override
    public void collect() {
        setData(new HashMap<>());
    }
    
    /**
     * Returns the output size used by end renderer initiator.
     * This is for example to return the web content-length output. It is called
     * after the render process to calculate the output size of an UTF-8 page.
     * @return 
     */
    @Override
    public int getOutputSize(){
        return outputSize;
    }
    
    /**
     * Merges all the known data including server wide data like presences etc.
     */
    final void mergeSetData(){
        fullDataMap.putAll(dataMap);
        fullDataMap.putAll(requestData);
        Date oCurDate = new Date();
        fullDataMap.put("date", setZeros(oCurDate.getDate()) + "-" + setZeros(oCurDate.getMonth() + 1) + "-" + (oCurDate.getYear() + 1900));
        fullDataMap.put("time", setZeros(oCurDate.getHours()) + ":" + setZeros(oCurDate.getMinutes()));
        try {
            fullDataMap.put("daypart", DayPartsService.current().getName());
        } catch (DayPartException ex) {
            fullDataMap.put("daypart", "Unknown");
        }
        try {
            fullDataMap.put("presence", PresenceService.current().getName());
        } catch (PresenceException ex) {
            fullDataMap.put("presence", "Unknown");
        }
        try {
            fullDataMap.put("userstatus", UserStatusService.current().getName());
        } catch (UserStatusException ex) {
            fullDataMap.put("userstatus", "Unknown");
        }
        serverData.put("heapdumpsEnabled", System.getProperties().containsKey("enableHeapdumps"));
        fullDataMap.put("_SERVER", serverData);
    }
    
    /**
     * Zero padding.
     * @param number
     * @return 
     */
    private String setZeros(int number) {
        String sNumber = String.valueOf(number);
        return (sNumber.length() == 1) ? "0" + sNumber : sNumber;
    }
    
    /**
     * Returns the render as meant without modifications.
     * @return
     * @throws Exception 
     */
    public String getRender() throws Exception {
        try {
            Template renderTpl = cfg.getTemplate(tpl);
            try (Writer writer = new OutputStreamWriter(outputStream, "UTF-8")) {
                mergeSetData();
                renderTpl.process(fullDataMap, writer);
                this.outputSize = outputStream.size();
            }
            return outputStream.toString();
        } catch (IOException ex) {
            LOG.error("Could not find template: " + tpl, ex);
            throw new Exception(ex);
        } catch (TemplateException ex) {
            LOG.error("Templates parsing error: " + ex.getMessage());
            throw new Exception(ex);
        }
    }
    
    /**
     * Renders a custom part which can be placed within an other render.
     * This is used with custom data to be passed to the function. This for example
     * is used with XSL transformers.
     * @param content
     * @return
     * @throws Exception 
     */
    public String getCustomRender(String content) throws Exception {
        try {
            try (Writer writer = new OutputStreamWriter(outputStream, "UTF-8")) {
                writer.write(content);
                writer.flush();
                this.outputSize = outputStream.size();
                writer.close();
            }
            return outputStream.toString();
        } catch (UnsupportedEncodingException ex) {
            throw new Exception("Custom renderer does not support UTF-8");
        }
    }
    
}
