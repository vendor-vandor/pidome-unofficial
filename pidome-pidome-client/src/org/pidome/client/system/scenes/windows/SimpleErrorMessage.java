/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.windows;

import java.util.Calendar;
import java.util.GregorianCalendar;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.utils.MiscImpl;

/**
 *
 * @author John Sirach
 */
public class SimpleErrorMessage extends WindowComponent {
    
    Label date;
    Label message = new Label();
    Label subMessage = new Label();
    
    StackPane windowMessagePane = new StackPane();
    
    static Logger LOG = LogManager.getLogger(SimpleErrorMessage.class);
    
    public SimpleErrorMessage(String iconMessage){
        super(iconMessage);
        this.message.setText("Not implemented yet");
        this.subMessage.setText("//system/pidome/client/feature/not/implemented");
    }
    

    @Override
    protected void constructWindow() {
        pressToClose(true);
        windowMessagePane.getStyleClass().add("error-message");
        windowMessagePane.setPrefSize(500, 110);
        setSize(500, 110);
        windowMessagePane.setAlignment(Pos.TOP_LEFT);
        this.setAlignment(Pos.TOP_LEFT);
        this.message.getStyleClass().add("message");
        this.message.setTranslateX(20);
        this.message.setTranslateY(39);
        Calendar cal = new GregorianCalendar();
        date = new Label("Date: " + MiscImpl.setZeroLeading(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)), 2) + 
                              "." + MiscImpl.setZeroLeading(String.valueOf(cal.get(Calendar.MONTH))+1, 2) + 
                              "." + cal.get(Calendar.YEAR) +
                              " " + MiscImpl.setZeroLeading(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)), 2) + 
                              ":" + MiscImpl.setZeroLeading(String.valueOf(cal.get(Calendar.MINUTE)), 2));
        date.getStyleClass().add("date");
        date.setTranslateX(20);
        date.setTranslateY(11);
        subMessage.getStyleClass().add("submessage");
        subMessage.setTranslateX(20);
        subMessage.setTranslateY(77);
    }
    
    public final void setMessage(String message){
        this.message.setText(message);
        this.subMessage.setText("//system/pidome/client/error/");
        LOG.info(message);
    }
    
    public final void setSubMessage(String subMessage){
        this.subMessage.setText(subMessage);
    }
    
    public final void removeMessage(){
        windowMessagePane.getChildren().removeAll(date,message,subMessage);
    }
    
    @Override
    public void destructWindow(){
        WindowManager.closeWindow(this);
        removeMessage();
    }

    @Override
    protected void setupContent() {
        windowMessagePane.getChildren().addAll(date, this.message, subMessage);
        getChildren().add(windowMessagePane);
    }

    @Override
    protected void removeContent() {
        getChildren().remove(windowMessagePane);
    }
    
}