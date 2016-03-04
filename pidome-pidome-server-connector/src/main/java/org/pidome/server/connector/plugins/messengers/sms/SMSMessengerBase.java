/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.messengers.sms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.net.ssl.HttpsURLConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfiguration;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.interfaces.web.configuration.WebOption;
import org.pidome.server.connector.plugins.messengers.Messenger;
import org.pidome.server.connector.plugins.messengers.MessengerException;

/**
 *
 * @author John
 */
public abstract class SMSMessengerBase extends Messenger {

    Map<String, String> configuration;
    
    String sendPhone;
    String spoolsendPath = "/var/spool/sms/outgoing/";

    String pushBulletKey = null;
    
    static Logger LOG = LogManager.getLogger(SMSMessengerBase.class);
    
    public SMSMessengerBase(){
        
    }
    
    public final void constructSettings(){
        WebConfiguration conf = new WebConfiguration();
        WebConfigurationOptionSet optionSetPhoneOptions = new WebConfigurationOptionSet("SMS send options");
        optionSetPhoneOptions.addOption(new WebOption("SPOOLPATH", "Fill in the outgoing spool path", "Fill in the sms tools full outgoing spool path (like /var/spool/sms/outgoing/).", WebOption.WebOptionConfigurationFieldType.STRING));
        optionSetPhoneOptions.addOption(new WebOption("SENDNUMBER", "Fill in the receiving number", "Fill in the phone number to send to in international format without the plus (+) sign.", WebOption.WebOptionConfigurationFieldType.STRING));
        conf.addOptionSet(optionSetPhoneOptions);
        
        WebConfigurationOptionSet optionSetPushBulletOptions = new WebConfigurationOptionSet("Pushbullet options");
        optionSetPushBulletOptions.addOption(new WebOption("PUSHBULLETAPIKEY", "Pushbullet API key", "If using Pushbullet, fill in your api key here.", WebOption.WebOptionConfigurationFieldType.STRING));
        conf.addOptionSet(optionSetPushBulletOptions);
        
        setConfiguration(conf);
        
    }
    
    @Override
    public void setConfigurationValues(Map<String, String> configuration) throws WebConfigurationException {
        this.configuration = configuration;
        LOG.debug("Starting config values: {}", configuration);
        if(configuration.get("SENDNUMBER")!=null && !configuration.get("SENDNUMBER").equals("")){
            sendPhone = configuration.get("SENDNUMBER");
        }
        if(configuration.get("SPOOLPATH")!=null && !configuration.get("SPOOLPATH").equals("")){
            spoolsendPath = configuration.get("SPOOLPATH");
        }
        if(configuration.get("PUSHBULLETAPIKEY")!=null && !configuration.get("PUSHBULLETAPIKEY").equals("")){
            byte[] authEncBytes = Base64.getEncoder().encode(configuration.get("PUSHBULLETAPIKEY").getBytes());
            pushBulletKey= new String(authEncBytes);
        }
    }

    public final String getSendPhoneNumber() throws MessengerException {
        if(configuration.get("SENDNUMBER")!=null && !configuration.get("SENDNUMBER").equals("")){
            return configuration.get("SENDNUMBER");
        } else {
            throw new MessengerException("No SMS number known");
        }
    }
    
    @Override
    public void startPlugin() throws PluginException {
        this.setRunning(true);
    }

    @Override
    public void stopPlugin() throws PluginException {
        this.setRunning(false);
    }
    
    @Override
    public void sendSmsMessage(String message) throws MessengerException {
        if(message!=null && spoolsendPath!=null && sendPhone!=null){
            if(message.length()>132){
                throw new MessengerException("Size to large");
            } else {
                UUID randString = UUID.randomUUID();
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(spoolsendPath + randString), "utf-8"))) {
                    writer.write(constructSMSMessage(message));
                } catch (IOException ex) {
                    throw new MessengerException("Could not send message to " + sendPhone + ": " + ex.getMessage());
                }
            }
        }
    }
    
    @Override
    public void sendPushBulletMessage(String message) throws MessengerException {
        if(this.pushBulletKey!=null){
            
            try {
                String url = "https://api.pushbullet.com/v2/pushes";
                URL obj = new URL(url);
                HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
                
                //add reuqest header
                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", "PiDome message plugin/1.0");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Authorization", "Basic " + pushBulletKey);
                
                String urlParameters = "{\"type\": \"note\", \"title\": \"PiDome message\", \"body\": \""+message+"\"}";
                
                // Send post request
                con.setDoOutput(true);
                try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                    wr.writeBytes(urlParameters);
                    wr.flush();
                }
                
                int responseCode = con.getResponseCode();
                LOG.info("\nSending 'POST' request to URL : " + url);
                LOG.info("Post parameters : " + urlParameters);
                LOG.info("Response Code : " + responseCode);
                
                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
                //print result
                LOG.info(response.toString());
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(SMSMessengerBase.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
    
    @Override
    public void sendEmailMessage(String to, String subject, String message) throws MessengerException {
    }

    public final String constructSMSMessage(String message){
        return "To: " + sendPhone + "\n\nPiDome: " + message;
    }
    
    @Override
    public void receiveMessage(String from, String subject, String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
