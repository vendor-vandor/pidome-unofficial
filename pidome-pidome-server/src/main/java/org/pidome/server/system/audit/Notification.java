/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.audit;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author John
 */
public class Notification {
 
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(Notification.class);
    
    private final int id;
    private Date date;
    private String type;
    private String subject;
    private String message;
    private boolean read;
    
    protected Notification(int id){
        this.id = id;
    }
    
    public final int getId(){
        return this.id;
    }
    
    protected final void setDate(String date){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            this.date = format.parse(date);
        } catch (ParseException ex) {
            LOG.error("Could not parse '{}' to system type date", date);
        }
    }
    
    public final Date getDate(){
        return this.date;
    }
    
    protected final void setType(String type){
        this.type = type;
    }
    
    public final String getType(){
        return this.type;
    }
    
    protected final void setSubject(String subject){
        this.subject = subject;
    }
    
    public final String getSubject(){
        return this.subject;
    }
    
    protected final void setMessage(String message){
        this.message = message;
    }
    
    public final String getMessage(){
        return this.message;
    }
    
    protected final void setRead(boolean read){
        this.read = read;
    }
 
    public final boolean getRead(){
        return this.read;
    }
    
}