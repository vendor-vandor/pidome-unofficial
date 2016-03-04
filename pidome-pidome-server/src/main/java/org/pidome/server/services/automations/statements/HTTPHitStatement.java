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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.automations.variables.TextAutomationVariable;

/**
 *
 * @author John
 */
public final class HTTPHitStatement extends AutomationStatement {

    private String url = "http://127.0.0.1/";
    private String method = "GET";
    
    List<TextAutomationVariable> urlContent;
    
    static Logger LOG = LogManager.getLogger(HTTPHitStatement.class);
    
    public HTTPHitStatement(){
        super("HTTPHitStatament");
    }
    
    /**
     * Sets the url.
     * @param url 
     */
    public final void setUrl(String url){
        this.url = url;
    }
    
    /**
     * Sents the content to be send.
     * @param content 
     */
    public final void setContent(List<TextAutomationVariable> content){
        this.urlContent = content;
    }
    
    /**
     * Sets the url method.
     * @param type 
     */
    public final void setMethod(String type){
        switch(type){
            case "POST":
                this.method = "POST";
            break;
            default:
                this.method = "GET";
            break;
        }
    }
    
    /**
     * Runs the statement.
     * @return 
     */
    @Override
    public boolean run() {
        StringBuilder message;
        int counter = 0;
        if(method.equals("GET")){
            message = new StringBuilder("?");
        } else {
            message = new StringBuilder();
        }
        for(TextAutomationVariable text:urlContent){
            if(text.isVarSet()){
                message.append(createCombinedSet(text.getVarSet())).append(((counter%2==0)?"=":""));
            } else {
                if(method.equals("GET")){
                    message.append(URLEncoder.encode(String.valueOf(text.getProperty().getValue()))).append(((counter%2==0)?"=":""));
                } else {
                    message.append(String.valueOf(text.getProperty().getValue())).append(((counter%2==0)?"=":""));
                }
            }
            if(counter>0 && counter%2==1){
                message.append("&");
            }
            counter++;
        }
        String toUse = message.toString();
        LOG.debug("Created url parameters: {}", toUse);
        Runnable run = () -> {
            if(method.equals("GET")){
                sendGet(new StringBuilder(this.url).append(toUse).toString());
            } else {
                sendPost(this.url, toUse);
            }
        };
        run.run();
        return true;
    }

    private String createCombinedSet(List<TextAutomationVariable> textSet){
        StringBuilder combinedSet = new StringBuilder();
        for(TextAutomationVariable var:textSet){
            combinedSet.append(var.getProperty().getValue());
        }
        return combinedSet.toString();
    }
    
    /**
     * Send a get request
     * @param fullUrl 
     */
    private void sendGet(String fullUrl) {
        try {
            URL obj = new URL(fullUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Pidome Automation rule agent");
            try (InputStreamReader input = new InputStreamReader(con.getInputStream());
                BufferedReader in = new BufferedReader(input)) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                LOG.trace("HTTP get response: {}, body: {}", con.getResponseCode(), response.toString());
            } catch (IOException ex) {
                LOG.error("Could not read output from: {} ({})", fullUrl, ex.getMessage());
            }
            con.disconnect();
        } catch (MalformedURLException ex) {
            LOG.error("Incorrect url build up: {} ({})", fullUrl, ex.getMessage());
        } catch (IOException ex) {
            LOG.error("Could open connection to: {} ({})", fullUrl, ex.getMessage());
        }
    }
    
    /**
     * Send a post request
     * @param url
     * @param content 
     */
    private void sendPost(String url, String content) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Pidome Automation rule agent");
            con.setDoOutput(true);
            try (OutputStream out = con.getOutputStream();
                 DataOutputStream wr = new DataOutputStream(out)) {
                wr.writeBytes(content);
                wr.flush();
            } catch (IOException ex) {
                LOG.error("Could not write output to: {} ({})", url, ex.getMessage());
            }
            try (InputStreamReader input = new InputStreamReader(con.getInputStream());
                BufferedReader in = new BufferedReader(input)) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                LOG.trace("HTTP get response: {}, body: {}", con.getResponseCode(), response.toString());
            } catch (IOException ex) {
                LOG.error("Could not read output from: {} ({})", url, ex.getMessage());
            }
            con.disconnect();
        } catch (MalformedURLException ex) {
            LOG.error("Incorrect url build up: {} ({})", url, ex.getMessage());
        } catch (IOException ex) {
            LOG.error("Could open connection to: {} ({})", url, ex.getMessage());
        }
    }
    
    /**
     * Destroys the statement's variables.
     */
    @Override
    public void destroy() {
        for(TextAutomationVariable text:urlContent){
            text.destroy();
        }
    }
    
}
