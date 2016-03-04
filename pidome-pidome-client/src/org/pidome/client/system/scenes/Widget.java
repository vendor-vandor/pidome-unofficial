/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.PidomeClient;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.Devices;
import org.pidome.client.system.domotics.components.devices.DevicesEvent;
import org.pidome.client.system.domotics.components.devices.DevicesEventListener;
import org.pidome.client.system.domotics.components.userpresence.UserPresenceEvent;
import org.pidome.client.system.domotics.components.userpresence.UserPresences;
import org.pidome.client.system.domotics.components.userpresence.UserPresencesEventListener;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;
import org.pidome.client.system.scenes.components.mainstage.NotificationBlock;
import org.pidome.client.system.scenes.components.mainstage.QuickAppMenu;
import org.pidome.client.system.scenes.components.mainstage.desktop.WidgetDesktop;
import org.pidome.client.system.scenes.components.mainstage.displays.clientsettings.ClientSettings;
import org.pidome.client.system.scenes.windows.SimpleErrorMessage;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 *
 * @author John Sirach
 */
public class Widget extends Scene implements DevicesEventListener,ClientDataConnectionListener,UserPresencesEventListener {

    final static double width  = 207;
    final static double height = 572;

    static Stage mainStage;
    final Pane rootPane;
    
    NotificationBlock notificationBlock;
    StackPane widgetDesktop;
    QuickAppMenu quickMenu = new QuickAppMenu();
    
    StackPane widgetContent = new StackPane();
    
    static Logger LOG = LogManager.getLogger(Widget.class);
    
    ImageLoader bgImage = new ImageLoader("widget/background.png", width, height);
    ImageLoader desktopDevider = new ImageLoader("widget/desktopdevider.png", 180, 14);
    
    boolean mouseStagePressed = false;
    double windowPosForDragX;
    double windowPosForDragY;
    
    java.awt.TrayIcon sysTrayIcon;
    boolean stateMinimized = false;
    boolean hiddenBefore = false;
    
    public Widget(Pane root, Stage stage){
        super(root, width, height, Color.TRANSPARENT);
        mainStage = stage;
        rootPane = root;
        widgetContent.setAlignment(Pos.TOP_LEFT);
        rootPane.setBackground(Background.EMPTY);
        rootPane.setStyle("-fx-backround-color: transparent;");
        draggable();
        UserPresences.addPresencesEventListener(this);
    }
    
    public final void setNotificationBlock(NotificationBlock notBlock){
        notificationBlock = notBlock;
    }
    
    public final void setDesktopBlock(WidgetDesktop desktop){
        widgetDesktop = desktop.getPane();
    }
    
    public final void build(){
        setsysTray();
        widgetContent.getChildren().add(new ImageView(bgImage.getImage()));
        
        TilePane widgetButtons = getWidgetButtons();
        widgetButtons.setTranslateX(164);
        widgetButtons.setTranslateY(9);
        
        quickMenu.setTranslateX(-10);
        quickMenu.setTranslateY(177);
        
        ImageView desktopDeviderImage = new ImageView(desktopDevider.getImage());
        desktopDeviderImage.setMouseTransparent(true);
        desktopDeviderImage.setTranslateX(5);
        desktopDeviderImage.setTranslateY(280);
        
        widgetDesktop.setTranslateX(7);
        widgetDesktop.setTranslateY(281);
        
        widgetContent.getChildren().addAll(notificationBlock,quickMenu,widgetDesktop,desktopDeviderImage,widgetButtons);
        
        rootPane.getChildren().add(widgetContent);
        rootPane.setBackground(Background.EMPTY);
        Devices.addDevicesEventListener(this);
        ClientData.addClientDataConnectionListener(this);
    }
    
    final TilePane getWidgetButtons(){
        TilePane widgetButtons = new TilePane();
        widgetButtons.setHgap(1);
        widgetButtons.setMaxSize(36, 36);
        if (SystemTray.isSupported()) {
            Button appTaskBarButton = widgetButton("widget/appicons/app_taskbar.png", "Hide to system tray");
            appTaskBarButton.setOnAction((ActionEvent t) -> {
                hideApp();
            });
            widgetButtons.getChildren().add(appTaskBarButton);
        }
        
        Button appCloseButton = widgetButton("widget/appicons/app_close.png", "Exit PiDome client");
        appCloseButton.setOnAction((ActionEvent t) -> {
            exitApp();
        });
        
        Button appHelpButton = widgetButton("widget/appicons/app_help.png", "PiDome Help");
        appHelpButton.setOnAction((ActionEvent t) -> {
            notSupportedYet("System help", "System help is not yet supported");
        });
        
        Button appSettingsButton = widgetButton("widget/appicons/app_settings.png", "PiDome settings");
        appSettingsButton.setOnAction((ActionEvent t) -> {
            WindowManager.openWindow(new ClientSettings());
        });
        
        widgetButtons.getChildren().addAll(appCloseButton, appHelpButton, appSettingsButton);
        
        return widgetButtons;
    }
    
    final Button widgetButton(String image, String tooltip){
        ImageView appTaskBarImage = new ImageView(new ImageLoader(image, 15, 15).getImage());
        Button appTaskBarButton = new Button();
        appTaskBarButton.setCursor(Cursor.HAND);
        appTaskBarButton.getStyleClass().add("widgetactionbutton");
        appTaskBarButton.setMaxSize(15, 15);
        appTaskBarButton.setGraphic(appTaskBarImage);
        Tooltip toolTip = new Tooltip(tooltip);
        toolTip.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT);
        toolTip.setAnchorX(0);
        toolTip.setAnchorY(0);
        appTaskBarButton.setTooltip(toolTip);
        return appTaskBarButton;
    }
    
    final void notSupportedYet(String title, String message){
        SimpleErrorMessage error = new SimpleErrorMessage(title);
        error.setMessage(message);
        WindowManager.openWindow(error);
    }
    
    final void systTrayMessage(String header, String message, TrayIcon.MessageType type){
        if(stateMinimized && sysTrayIcon!=null){
            sysTrayIcon.displayMessage(header, message, type);
        }
    }
    
    final void setsysTray(){
        if (SystemTray.isSupported()) {
            try {
                SystemTray sysTray = SystemTray.getSystemTray();
                PopupMenu popup = new PopupMenu();
                MenuItem item = new MenuItem("Exit");
                popup.add(item);
                
                BufferedImage trayIconImage = ImageIO.read(getClass().getResource(PidomeClient.appLogo));
                int trayIconWidth = new TrayIcon(trayIconImage).getSize().width;
                sysTrayIcon = new TrayIcon(trayIconImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH), "Pidome client", popup);

                ActionListener listener = (java.awt.event.ActionEvent arg0) -> {
                    exitApp();
                };
                ActionListener listenerTray = (java.awt.event.ActionEvent arg0) -> {
                    Platform.runLater(() -> {
                        if(mainStage.isShowing()){
                            hideApp();
                        } else {
                            showApp();
                        }
                    });
                };
                sysTrayIcon.addActionListener(listenerTray);
                sysTrayIcon.setToolTip("PiDome Client");
                item.addActionListener(listener);
                try {
                    sysTray.add(sysTrayIcon);
                    Platform.setImplicitExit(false);
                } catch (AWTException ex) {
                    LOG.error("System tray unavailable: " + ex.getMessage());
                }
            } catch (IOException ex) {
                LOG.error("System tray unavailable: " + ex.getMessage());
            }
        } else {
            LOG.error("System tray unavailable");
        }
    }
    
    
    final void draggable(){
        addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent me) -> {
            handleStageClickPos(me.getScreenX(), me.getScreenY());
            mouseStagePressed = true;
        });
        addEventHandler(MouseEvent.MOUSE_RELEASED, (MouseEvent me) -> {
            if (!me.isConsumed()) {
                mouseStagePressed = false;
            }
        });
        addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent me) -> {
            if (!me.isConsumed()) {
                moveStage(me.getScreenX(), me.getScreenY());
            }
        });
    }
    
    final void handleStageClickPos(double x, double y){
        windowPosForDragX = (mainStage.getX()) - x;
        windowPosForDragY = (mainStage.getY()) - y;
    }
    
    final void moveStage(double x, double y){
        mainStage.setX(x + windowPosForDragX);
        mainStage.setY(y + windowPosForDragY);
    }
    
    final void hideApp(){
        stateMinimized = true;
        WindowManager.closeAll();
        mainStage.hide();
        if(!hiddenBefore)systTrayMessage("Notice", "The pidome client is still running in background", TrayIcon.MessageType.INFO);
        hiddenBefore = true;
    }
    
    final void showApp(){
        stateMinimized = false;
        mainStage.show();
    }
    
    final void exitApp(){
        Platform.exit();
    }

    @Override
    public void handleUserPresencesEvent(UserPresenceEvent event) {
        switch(event.getEventType()){
            case UserPresenceEvent.PRESENCECHANGED:
                systTrayMessage("Presence", "Presence is now " + event.getPresenceName(), TrayIcon.MessageType.INFO);
            break;
        }
    }
    
    @Override
    public void handleDevicesEvent(DevicesEvent event) {
        Device device;
        switch (event.getEventType()) {
            case DevicesEvent.DEVICEADDED:
                device = event.getSource();
                systTrayMessage("System", "Added device " + device.getName(), TrayIcon.MessageType.INFO);
            break;
            case DevicesEvent.DEVICEREMOVED:
                device = event.getSource();
                systTrayMessage("System", "Removed device " + device.getName(), TrayIcon.MessageType.WARNING);
            break;
        }
    }

    @Override
    public void handleClientDataConnectionEvent(ClientDataConnectionEvent event) {
        /*
        switch (event.getEventType()) {
            case ClientDataConnectionEvent.SYSRECEIVED:
                Map<String, Object> sysData = (Map<String, Object>) event.getData();
                switch (event.getMethod()) {
                    case "setDaypart":
                        Platform.runLater(() -> {
                            try {
                                systTrayMessage("System", "State changed: " + SystemMacros.getSysMacro(String.valueOf(sysData.get("id"))).get("friendlyname"),TrayIcon.MessageType.INFO);
                            } catch (DomComponentsException ex) {
                                ////
                            }
                        });
                    break;
                }
            break;
        }
        */
    }
    
}
