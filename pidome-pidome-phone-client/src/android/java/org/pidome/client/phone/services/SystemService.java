/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.phone.dialogs.settings.LocalizationInfoInterface;
import org.pidome.client.phone.network.connectivity.AndroidBroadcastReceiver;
import org.pidome.client.phone.network.connectivity.ConnectionWatchdog;
import org.pidome.client.phone.utils.DeviceUtils;
import org.pidome.client.system.PCCCLientStatusListener;
import org.pidome.client.system.PCCClientEvent;
import org.pidome.client.system.PCCConnection;
import org.pidome.client.system.PCCConnectionEvent;
import org.pidome.client.system.PCCConnectionListener;
import org.pidome.client.system.PCCConnectionNameSpaceRPCListener;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityEvent;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityEventListener;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityProvider;
import org.pidome.pcl.backend.data.interfaces.storage.LocalSettingsStorageInterface;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.networking.connections.server.ServerConnection;

/**
 *
 * @author John
 */
public final class SystemService extends Service implements NetworkAvailabilityEventListener {

    /**
     * Registers running or not.
     */
    private static boolean running = false;
    
    /**
     * The pidome system which handles all the stuff.
     * Yes it really does handle EVERYTHING.
     */
    private PCCSystem system;
    
    /**
     * This service IBinder.
     * When you connect to this service it return the IBinder which can be used 
     * to get the PCC system, or this service specific components (GPS etc..).
     */
    private final BindExposer systemBinder = new BindExposer();
    
    private final ConnectionListener connectionListener = new ConnectionListener();
    private final ClientListener     clientListener     = new ClientListener();
    
    private final PCCConnectionNameSpaceRPCListener  notificationListener = new NotificationListener();
    
    /**
     * Localization services like GPS and set home on home network.
     */
    private LocalizationService localizationService;
    
    /**
     * There is only one listener as this one put's the user in 
     */
    private ServiceConnectorListener signalHandler;
    
    /**
     * We want to know if the application is in the foreground or not.
     */
    private LifeCycleHandler lifeCycleHandler = new LifeCycleHandler();
    
    private ConnectionWatchdog watchdog;
    private AndroidPreferences prefs;
    
    private Context context;
    
    private static boolean isCalled = false;
    
    public SystemService(){
        super();
        isCalled = true;
    }
    
    public static boolean isCalled(){
        return isCalled;
    }
    
    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        context = getBaseContext();
        if(system==null){
            getApplication().registerActivityLifecycleCallbacks(lifeCycleHandler);
            watchdog = new ConnectionWatchdog();
            watchdog.setInitialContext(context);
            prefs = new AndroidPreferences(getBaseContext());
            system = new PCCSystem(prefs, 
                                   new AndroidSettings(getBaseContext()),
                                   ServerConnection.Profile.MOBILE, 
                                   watchdog);
            system.getConnection().setCustomBroadcastListener(new AndroidBroadcastReceiver(context));
            system.getConnection().addPCCConnectionListener(connectionListener);
            system.getConnection().addPCCConnectionNameSpaceListener("NotificationService", notificationListener);
            system.getClient().addListener(clientListener);

            localizationService = new LocalizationService(this, prefs);

            LocalSettingsStorageInterface settings = system.getLocalSettings();
            if(settings.getStringSetting("user.login", "").equals("") || settings.getStringSetting("user.userinfo", "").equals("")){
                settings.setStringSetting("user.login", generateDeviceId(context));
                settings.setStringSetting("user.userinfo", DeviceUtils.getDeviceName());
                try {
                    settings.storeSettings();
                } catch (IOException ex) {
                    Logger.getLogger(SystemService.class.getName()).log(Level.SEVERE, "Problem storing initial settings", ex);
                }
            }
            Log.i("SystemService", "PiDome SystemService started");
            running = true;
            serviceLogin();
        }
        return START_STICKY;
    }
    
    private void serviceLogin(){
        new Thread() { 
            @Override 
            public final void run(){ 
                System.out.println("Currently logged in: " + system.getClient().isloggedIn() + ", in connection progress: " + system.getConnection().inConnectionProgress() + ", in login progress: " + system.getClient().inLoginProgress());
                if(!system.getConnection().inConnectionProgress() && !system.getClient().inLoginProgress() && !system.getClient().isloggedIn()){
                    if(system.getConnection().hasInitialManualConnectData()){
                        system.getConnection().startInitialConnection();
                    } else {
                        system.getConnection().startSearch();
                    }
                }
                System.out.println("Adding system service to network watchdog");
                watchdog.addEventListener(SystemService.this);
            } 
        }.start();
    }
    
    /**
     * When bound to this service it returns the local binder.
     * This local binder guarantees access to this service components
     * and the PCC system.
     * @param arg0
     * @return 
     */
    @Override
    public BindExposer onBind(Intent arg0) {
        return systemBinder;
    }
    
    /**
     * Returns if a service is running.
     * @return true when running, false when not.
     */
    public static boolean isServiceRunning(){
        return running;
    }

    @Override
    public void handleNetworkAvailabilityEvent(NetworkAvailabilityEvent nae) {
        System.out.println("Received network availbility event: " + nae.getEventType());
        if(nae.getEventType().equals(NetworkAvailabilityProvider.Status.NETWORKAVAILABLE)){
            serviceLogin();
        }
    }
    
    /**
     * Used to retrieve the 
     */
    public final class BindExposer extends Binder implements SystemBinder {
        @Override
        public final PCCSystem getPCCSystem() {
            return system;
        }
        @Override
        public final void setSignalHandler(ServiceConnectorListener receiver){
            signalHandler = receiver;
        }
        @Override
        public final LifeCycleHandler getLifeCycleHandler(){
            return lifeCycleHandler;
        }
        @Override
        public final LocalizationInfoInterface getPresenceServiceSettings(){
            return localizationService;
        }
    }
    
    protected final PCCSystem getSystem(){
        return this.system;
    }
    
    /**
     * Generate an unique id for this device.
     * @param context
     * @return 
     */
    private String generateDeviceId(Context context) {
        final String macAddr, androidId;
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        macAddr = wifiInf.getMacAddress();
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid;
        try {
            deviceUuid = new UUID(androidId.hashCode(), macAddr.hashCode());
        } catch (NullPointerException ex){
            deviceUuid = new UUID(androidId.hashCode(), androidId.hashCode());
        }
        return deviceUuid.toString();
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
            Log.i("SystemService", "Received connection event: " + status.toString());
            switch (status) {
                case CONNECTED:
                    system.getClient().login();
                break;
                case DISCONNECTED:
                    system.getClient().logout();
                    if(signalHandler!=null){
                        signalHandler.handleUserLoggedOut();
                    }
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
            Log.i("SystemService", "Received client event: " + event.getStatus().toString());
            switch (event.getStatus()) {
                case LOGGED_IN:
                    try {
                        system.getLocalSettings().storeSettings();
                    } catch (IOException ex) {
                        Logger.getLogger(SystemService.class.getName()).log(Level.SEVERE, "Could not store connection settings", ex);
                    }
                break;
                case INIT_DONE:
                    if(signalHandler!=null){
                        signalHandler.handleUserLoggedIn();
                    }
                    localizationService.restartGPSExecutor();
                    setHome();
                break;
            }
        }
    }
    
    private void setHome(){
        if(prefs!=null){
            if(prefs.getBoolPreference("wifiConnectHomeEnabled", false)){
                ConnectivityManager connManager =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
                if (networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                    if (connectionInfo != null) {
                        String SSID = connectionInfo.getSSID();
                        String BSSID= connectionInfo.getBSSID();
                        if(SSID!=null && BSSID!=null){
                            if(SSID.equals(prefs.getStringPreference("wifiConnectSSID", java.util.UUID.randomUUID().toString())) &&
                               BSSID.equals(prefs.getStringPreference("wifiConnectBSSID",java.util.UUID.randomUUID().toString()))){
                                if(this.system!=null){
                                    try {
                                        system.getClient().getEntities().getPresenceService().setPresence(1);
                                    } catch (EntityNotAvailableException ex) {
                                        Logger.getLogger(SystemService.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private class NotificationListener implements PCCConnectionNameSpaceRPCListener {

        @Override
        public void handleRPCCommandByBroadcast(PCCEntityDataHandler pccedh) {
            if(pccedh.getMethod().equals("sendNotification")){
                handleNotificationMessage(pccedh.getParameters());
            }
        }

        private void handleNotificationMessage(final Map<String,Object> params){
            if(lifeCycleHandler.inForeground()){
                
                Handler handler = new Handler(context.getMainLooper());

                 handler.post(new Runnable() {
                     @Override
                     public void run() {
                        Toast toast = Toast.makeText(context, new StringBuilder((String)params.get("subject")).append(": ").append(params.get("message")), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 40);
                        toast.show();
                     }
                 });
                
            } else {
                
                Intent resultIntent = new Intent(SystemService.this, javafxports.android.FXActivity.class);
                PendingIntent pIntent = PendingIntent.getActivity(SystemService.this, 0, resultIntent, 0);
                /*
                Uri smallIcon = Uri.parse("/images/logo/notification-small.png");
                try {
                    InputStream inputStream = getContentResolver().openInputStream(smallIcon);
                    Drawable yourDrawable = Drawable.createFromStream(inputStream, smallIcon.toString() );
                } catch (FileNotFoundException e) {
                    ///
                }
                */
                NotificationCompat.Builder notification = new NotificationCompat.Builder(SystemService.this)
                        .setOnlyAlertOnce(true)
                        .setSmallIcon(SystemService.this.getResources().getIdentifier("notificationsmall", "drawable", SystemService.this.getPackageName()))
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("notification", "drawable", SystemService.this.getPackageName())))
                        .setContentTitle((String)params.get("subject"))
                        .setContentText((String)params.get("message"))
                        .setContentIntent(pIntent)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setAutoCancel(true);
                
                NotificationManager mNotificationManager = (NotificationManager) SystemService.this.getSystemService(Context.NOTIFICATION_SERVICE);
                /// Set an id so notification will override, it is possible when an user has a lot of notifications the list could stack up.
                /// A todo would be to create notifications which are distinguised by categories. But this is being to be discussed.
                int mId = 12345;
                mNotificationManager.notify(mId, notification.build());
            }
        }
        
        @Override
        public void handleRPCCommandByResult(PCCEntityDataHandler pccedh) {
            /// Not used
        }
    
    }
    
}