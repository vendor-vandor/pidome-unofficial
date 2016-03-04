/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.dayparts.DayParts;
import org.pidome.client.system.domotics.components.userpresence.UserPresenceEvent;
import org.pidome.client.system.domotics.components.userpresence.UserPresences;
import org.pidome.client.system.domotics.components.userpresence.UserPresencesEventListener;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;
import org.pidome.client.system.scenes.components.mainstage.applicationsbar.ApplicationsBarSettingsUserPresence;
import org.pidome.client.system.scenes.windows.TitledWindow;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 *
 * @author John Sirach
 */
public class NotificationBlock extends StackPane implements ClientDataConnectionListener,UserPresencesEventListener{

    Label date            = new Label();
    Label clientName      = new Label();
    Label currentPresence = new Label();
    
    Calendar cal = new GregorianCalendar();
    String[] shortWeekdays = new DateFormatSymbols().getShortWeekdays();
    
    ImageView sysStateIcon = new ImageView(new ImageLoader("widget/appicons/sys_state.png", 17, 17).getImage());
    
    static Logger LOG = LogManager.getLogger(NotificationBlock.class);
    
    public NotificationBlock(){
        setAlignment(Pos.TOP_LEFT);
        addTimeBar();
        addClientNameBar();
        addSysStateNameBar();
        addSysStateInitiator();
        
        ClientData.addClientLoggedInConnectionListener(this);
        ClientData.addClientDataConnectionListener(this);
        UserPresences.addPresencesEventListener(this);
        
        setId("notificationblock");
        
    }
    
    final void addTimeBar(){
        date.getStyleClass().add("date");
        date.setTranslateX(15);
        date.setTranslateY(37);
        getChildren().add(date);
    }
    
    final void addClientNameBar(){
        clientName.getStyleClass().add("clientinfo");
        clientName.setTranslateX(35);
        clientName.setTranslateY(12);
        clientName.setMaxWidth(120);
        clientName.setMinWidth(120);
        getChildren().add(clientName);
    }
    
    final void addSysStateNameBar(){
        currentPresence.getStyleClass().add("sysstateinfo");
        currentPresence.setTranslateX(40);
        currentPresence.setTranslateY(67);
        try {
            currentPresence.setText(UserPresences.getPresence(UserPresences.getCurrent()));
        } catch (DomComponentsException ex) {
            currentPresence.setText("Unknown");
        }
        getChildren().add(currentPresence);
    }
    
    final void addSysStateInitiator(){
        sysStateIcon.setTranslateX(152);
        sysStateIcon.setTranslateY(70);
        sysStateIcon.setCursor(Cursor.HAND);
        getChildren().add(sysStateIcon);
        sysStateIcon.setOnMouseClicked((MouseEvent t) -> {
            UserPresenceWindow window = new UserPresenceWindow();
            WindowManager.openWindow(window);
        });
    }
    
    class UserPresenceWindow extends TitledWindow {

        ApplicationsBarSettingsUserPresence content = new ApplicationsBarSettingsUserPresence();
        
        public UserPresenceWindow() {
            super("setUSerPResence", "User presence");
        }

        @Override
        protected void setupContent() {
            setContent(content);
        }

        @Override
        protected void removeContent() {
            content = null;
        }
        
    }
    
    @Override
    public void handleClientDataConnectionEvent(final ClientDataConnectionEvent event) {
        Platform.runLater(() -> {
            switch (event.getEventType()) {
                case ClientDataConnectionEvent.LOGGEDIN:
                    String[] data = event.getClientData();
                    clientName.setText(data[0]);
                    //case ClientDataConnectionEvent.CONNECTED:
                    //    netIcon.swapStatus("on");
                break;
                case ClientDataConnectionEvent.DISCONNECTED:
                    clientName.setText("Disconnected");
                    //    loggedInIcon.swapStatus("off");
                    //    netIcon.swapStatus("off");
                break;
                case ClientDataConnectionEvent.SYSRECEIVED:
                    LOG.debug("handling sys data: {}, {}", event.getMethod(), event.getData());
                    Map<String,Object> sysData = (Map<String,Object>)event.getData();
                    switch(event.getMethod()){
                        case "time":
                            Platform.runLater(() -> {
                                date.setText((String)sysData.get("shorttext"));
                            });
                        break;
                    }
                break;
            }
        });
    }
    
    @Override
    public void handleUserPresencesEvent(UserPresenceEvent event) {
        switch(event.getEventType()){
            case UserPresenceEvent.PRESENCECHANGED:
                Platform.runLater(() -> { currentPresence.setText(event.getPresenceName()); });
            break;
            case UserPresenceEvent.PRESENCEUPDATED:
                if(DayParts.getCurrent()==event.getPresenceId()){
                    Platform.runLater(() -> { currentPresence.setText(event.getPresenceName()); });
                }
            break;
            case UserPresenceEvent.PRESENCEREMOVED:
                if(DayParts.getCurrent()==event.getPresenceId()){
                    Platform.runLater(() -> { currentPresence.setText("Unknown"); });
                }
            break;
        }
    }
    
    final void animateSystateEvent(){
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(500), sysStateIcon);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(1);
        rotateTransition.play();
    }
    
}
