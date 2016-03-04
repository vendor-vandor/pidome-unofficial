/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.settings.LocalizationInfoInterface;
import org.pidome.client.system.PCCCLientStatusListener;
import org.pidome.client.system.PCCClientEvent;
import org.pidome.client.system.PCCConnection;
import org.pidome.client.system.PCCConnectionEvent;
import org.pidome.client.system.PCCConnectionListener;
import org.pidome.client.system.PCCConnectionNameSpaceRPCListener;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.backend.data.interfaces.storage.LocalPreferenceStorageInterface;
import org.pidome.pcl.backend.data.interfaces.storage.LocalSettingsStorageInterface;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.networking.connections.server.ServerConnection;
import org.pidome.pcl.networking.interfaces.NetInterface;
import org.pidome.pcl.storage.preferences.LocalPreferenceStorage;
import org.pidome.pcl.storage.settings.LocalSettingsStorage;
import org.robovm.apple.foundation.NSDate;
import org.robovm.apple.foundation.NSOperationQueue;
import org.robovm.apple.foundation.NSTimeZone;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UILocalNotification;
import org.robovm.apple.uikit.UIUserInterfaceIdiom;
import org.robovm.apple.uikit.UIUserNotificationSettings;
import org.robovm.apple.uikit.UIUserNotificationType;

/**
 *
 * @author John
 */
public class IOSServiceConnector implements ServiceConnector {

    private static PCCSystem system;
    ServiceConnectorListener listener;

    private final ConnectionListener connectionListener = new ConnectionListener();
    private final ClientListener clientListener = new ClientListener();
    private final IOSLifeCycleHandler lifeCycleHandler = new IOSLifeCycleHandler();

    private static PlatformOrientation orientationListener;

    private final LocalPreferenceStorageInterface prefStorage = new IOSPreferenceStorage();
    private final LocalSettingsStorageInterface setStorage = new IOSSettingsStorage();

    private IOSGPSHandler IosGpsHandler;

    private final PCCConnectionNameSpaceRPCListener notificationListener = new NotificationListener();

    public IOSServiceConnector() {
    }

    /**
     * Returns the plaform base
     *
     * @return
     */
    @Override
    public PlatformBase getPlatformBase() {
        return PlatformBase.MOBILE;
    }

    @Override
    public void startService() {
        system = new PCCSystem(prefStorage,
                setStorage,
                ServerConnection.Profile.MOBILE,
                new NetInterface());
        IosGpsHandler = new IOSGPSHandler(prefStorage, system);
        this.listener.serviceConnected(system);
        system.getConnection().addPCCConnectionListener(connectionListener);
        system.getClient().addListener(clientListener);

        system.getConnection().addPCCConnectionNameSpaceListener("NotificationService", notificationListener);


        
        UIUserNotificationType a = UIUserNotificationType.Alert;
        UIUserNotificationType b = UIUserNotificationType.Badge;
        UIUserNotificationType s = UIUserNotificationType.Sound;

        //UIUserNotificationType types = new UIUserNotificationType(a|b|s);
        //UIUserNotificationSettings settings = UIUserNotificationSettings.create(types, null);
        UIApplication.getSharedApplication().registerUserNotificationSettings(new UIUserNotificationSettings(a, null));
        UIApplication.getSharedApplication().registerUserNotificationSettings(new UIUserNotificationSettings(b, null));
        UIApplication.getSharedApplication().registerUserNotificationSettings(new UIUserNotificationSettings(s, null));

        UIApplication.getSharedApplication().setApplicationIconBadgeNumber(0);
        UIApplication.getSharedApplication().cancelAllLocalNotifications();

        if (system.getConnection().hasInitialManualConnectData()) {
            system.getConnection().startInitialConnection();
        } else {
            system.getConnection().startSearch();
        }
    }

    @Override
    public void stopService() {
        system.getClient().logout();
    }

    @Override
    public void setServiceConnectionListener(ServiceConnectorListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the lifecycle handler so we know if the app is in the foreground
     * or not.
     *
     * @return
     */
    @Override
    public LifeCycleHandlerInterface getLifeCycleHandler() {
        return lifeCycleHandler;
    }

    /**
     * Returns the localization interface for GPS interfacing
     *
     * @return
     * @throws UnsupportedOperationException
     */
    @Override
    public LocalizationInfoInterface getLocalizationService() throws UnsupportedOperationException {
        return IosGpsHandler;
    }

    @Override
    public void serviceLogin() {
        if (!system.getConnection().inConnectionProgress() && !system.getClient().isloggedIn()) {
            new Thread() {
                @Override
                public final void run() {
                    if (system.getConnection().hasInitialManualConnectData()) {
                        system.getConnection().startInitialConnection();
                    } else {
                        system.getConnection().startSearch();
                    }
                }
            }.start();
        }
    }

    /**
     * Return the display type used
     *
     * @return
     */
    @Override
    public DisplayType userDisplayType() {
        if (UIUserInterfaceIdiom.Pad.equals(UIDevice.getCurrentDevice().getUserInterfaceIdiom())) {
            return DisplayType.LARGE;
        } else {
            return DisplayType.SMALL;
        }
    }

    @Override
    public void addOrientationListener(PlatformOrientation listener) {
        if (orientationListener == null) {
            orientationListener = listener;
        }
    }

    public static void handleOrientationChanged(PlatformOrientation.Orientation orient) {
        if (orientationListener != null) {
            orientationListener.handleOrientationChanged(orient);
        }
    }

    /**
     * Used to force an orientation type.
     *
     * @param orientation
     */
    @Override
    public void forceOrientation(PlatformOrientation.Orientation orientation) {
        switch (orientation) {
            case LANDSCAPE:
                break;
            case PORTRAIT:
                break;
        }
    }

    /**
     * Not used on this platform as it needs initialization with the service
     * connector
     *
     * @return
     */
    @Override
    public LocalPreferenceStorage getPreferences() {
        throw new UnsupportedOperationException("Not used on IOS");
    }

    /**
     * Not used on this platform as it needs initialization with the service
     * connector
     *
     * @return
     */
    @Override
    public LocalSettingsStorage getSettings() {
        throw new UnsupportedOperationException("Not used on IOS"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getMaxWorkWidth() {
        throw new UnsupportedOperationException("Not used on IOS"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PhysicalDisplayInterface getPhysicalDisplayInterface() {
        return new UnsupportedPhysicalDisplayInterface(IOSServiceConnector.this);
    }

    @Override
    public void storeUserDisplayType(DisplayType type) {
        // i doubt this will be used on IOS as it as far better display size methods.
    }

    private class ConnectionListener implements PCCConnectionListener {

        /**
         * Handles connection events.
         *
         * @param status The connection status event.
         * @param event Event is null when no server information is available.
         * This is mainly when for example server search fails.
         */
        @Override
        public final void handlePCCConnectionEvent(PCCConnection.PCCConnectionStatus status, PCCConnectionEvent event) {

            Logger.getLogger(ConnectionListener.class.getName()).log(Level.INFO, "Received connection event: " + status.toString());
            switch (status) {
                case CONNECTED:
                    if (IOSServiceConnector.this.setStorage.getStringSetting("user.login", "").equals("") || IOSServiceConnector.this.setStorage.getStringSetting("user.userinfo", "").equals("")) {
                        IOSServiceConnector.this.setStorage.setStringSetting("user.login", UIDevice.getCurrentDevice().getIdentifierForVendor().asString());
                        IOSServiceConnector.this.setStorage.setStringSetting("user.userinfo", UIDevice.getCurrentDevice().getName());
                        try {
                            IOSServiceConnector.this.setStorage.storeSettings();
                        } catch (IOException ex) {
                            Logger.getLogger(IOSServiceConnector.class.getName()).log(Level.SEVERE, "Problem storing initial settings", ex);
                        }
                    }
                    system.getClient().login();

                    break;
                case DISCONNECTED:
                    if (listener != null) {
                        listener.handleUserLoggedOut();
                    }
                    IOSServiceConnector.this.IosGpsHandler.stop();
                    break;
            }
        }
    }

    private class ClientListener implements PCCCLientStatusListener {

        /**
         * Handles events that have to do with the client. These events have to
         * do with plain client stuff.
         *
         * @param event The PCCClientEvent to handle.
         */
        @Override
        public final void handlePCCClientEvent(PCCClientEvent event) {
            Logger.getLogger(IOSServiceConnector.class.getName()).log(Level.INFO, "Received client event: " + event.getStatus().toString());
            switch (event.getStatus()) {
                case LOGGED_IN:
                    try {
                        system.getLocalSettings().storeSettings();
                        IosGpsHandler.start();
                    } catch (IOException ex) {
                        Logger.getLogger(IOSServiceConnector.class.getName()).log(Level.SEVERE, "Could not store connection settings", ex);
                    }
                    break;
                case INIT_DONE:
                    if (listener != null) {
                        listener.handleUserLoggedIn();
                    }
                    break;
            }
        }
    }

    private class NotificationListener implements PCCConnectionNameSpaceRPCListener {

        @Override
        public void handleRPCCommandByBroadcast(PCCEntityDataHandler pccedh) {
            if (pccedh.getMethod().equals("sendNotification")) {
                handleNotificationMessage(pccedh.getParameters());
            }
        }

        private void handleNotificationMessage(final Map<String, Object> params) {

            System.out.println("\n\n\n\n\nIk ben hierzo!!!\n\n");

            String message = new StringBuilder((String) params.get("subject")).append(": ").append(params.get("message")).toString();

            ///NSOperationQueue.getMainQueue().addOperation(() -> {
            
            NSDate date = new NSDate();
            //5 seconds from now
            NSDate secondsMore = date.newDateByAddingTimeInterval(5);

            UILocalNotification localNotification = new UILocalNotification();
            localNotification.setTimeZone(NSTimeZone.getDefaultTimeZone());
            localNotification.setFireDate(secondsMore);
            localNotification.setApplicationIconBadgeNumber(UIApplication.getSharedApplication().getApplicationIconBadgeNumber() + 1);
            localNotification.setSoundName(UILocalNotification.getDefaultSoundName());
            localNotification.setAlertBody((String) params.get("subject"));
            localNotification.setAlertAction((String) params.get("message"));
                
            UIApplication.getSharedApplication().presentLocalNotificationNow(localNotification);
            //UIApplication.getSharedApplication().scheduleLocalNotification(localNotification);
            
            //});
            
            System.out.println("Sound: " + UILocalNotification.getDefaultSoundName());
            System.out.println("\n\n\n\n\nDONE!!!!!!\n\n");

            /*
             NSDate date = new NSDate();
 
             System.out.println("en");
            
             //NSTimeZone* sourceTimeZone = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];
             //NSTimeZone* destinationTimeZone = [NSTimeZone systemTimeZone];
            
             NSTimeZone sourceTimeZone = new NSTimeZone("GMT");
             //NSTimeZone sourceTimeZone = new NSTimeZone(NSTimeZone.getSystemTimeZone().getAbbreviation());
             NSTimeZone destinationTimeZone = new NSTimeZone(NSTimeZone.getLocalTimeZone().getAbbreviation());
 
             System.out.println("hier");
            
             long sourceGMTOffset = sourceTimeZone.getSecondsFromGMTForDate(date);
             long destinationGMTOffset = destinationTimeZone.getSecondsFromGMTForDate(date);
            
             System.out.println("dus");
            
             long interval = destinationGMTOffset - sourceGMTOffset;
 
             System.out.println("ook");
            
             NSDate destinationDate = NSDate.createWithTimeIntervalSinceDate((double)interval, date);
            
             System.out.println("Orig: " + date.toString());
             System.out.println("New?: " + destinationDate.toString() + "\n\n\n\n\n");
            
             ///NSDate destinationDate = [[[NSDate alloc] initWithTimeInterval:interval sinceDate:sourceDate] autorelease];

            
            
            
             UILocalNotification notif = new UILocalNotification();
             notif.setFireDate(destinationDate);
             notif.setAlertBody(message);
             //notif.setUserInfo(...);
             UIApplication.getSharedApplication().scheduleLocalNotification(notif);

            
            
             /*            
             application.registerUserNotificationSettings(UIUserNotificationSettings(forTypes: UIUserNotificationType.Sound | UIUserNotificationType.Alert | UIUserNotificationType.Badge, categories: null));

             UILocalNotification localNotification = UILocalNotification();
             localNotification.alertAction = "Testing";
             localNotification.alertBody = "Hello World!";
             localNotification.fireDate = NSDate(timeIntervalSinceNow: 5);
             UIApplication.sharedApplication().scheduleLocalNotification(localNotification);
             */
            // Doe hier iets met data 'message'
        }

        @Override
        public void handleRPCCommandByResult(PCCEntityDataHandler pccedh) {
            /// not used.
        }
    }
}
