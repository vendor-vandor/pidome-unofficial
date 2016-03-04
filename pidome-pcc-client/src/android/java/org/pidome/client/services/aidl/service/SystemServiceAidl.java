/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services.aidl.service;

import android.app.NotificationManager;
import org.pidome.client.services.aidl.AndroidSettingsAidl;
import org.pidome.client.services.aidl.AndroidPreferencesAidl;
import android.app.Service;
import static android.app.Service.START_STICKY;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.system.SystemService;
import org.pidome.client.network.connectivity.AndroidBroadcastReceiver;
import org.pidome.client.network.connectivity.ConnectionWatchdog;
import org.pidome.client.services.ServiceConnectorListener;
import org.pidome.client.services.aidl.client.ClientCallbacksServiceAidl;
import org.pidome.client.utils.DeviceUtils;
import org.pidome.client.system.PCCCLientStatusListener;
import org.pidome.client.system.PCCClient;
import org.pidome.client.system.PCCClientEvent;
import org.pidome.client.system.PCCConnection;
import org.pidome.client.system.PCCConnectionEvent;
import org.pidome.client.system.PCCConnectionException;
import org.pidome.client.system.PCCConnectionListener;
import org.pidome.client.system.PCCConnectionNameSpaceRPCListener;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityEvent;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityEventListener;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityProvider;
import org.pidome.pcl.data.connection.ConnectionException;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.networking.connections.server.ServerConnection;

/**
 *
 * @author John
 */
public final class SystemServiceAidl extends Service implements NetworkAvailabilityEventListener {

    private static final String TAG = "SystemServiceAidl";
    
    /**
     * Registers running or not.
     */
    private static boolean running = false;
    
    /**
     * Using a seperate pcc connection.
     */
    private PCCConnection connection;
    /**
     * Using a seperate pcc client.
     */
    private PCCClient client;
    
    private final ConnectionListener connectionListener = new ConnectionListener();
    private final ClientListener     clientListener     = new ClientListener();
    
    private final PCCConnectionNameSpaceRPCListener notificationListener = new NotificationListener();
    private final PCCConnectionNameSpaceRPCListener dispatchListener     = new DispatchDataListener();
    
    /**
     * Localization services like GPS and set home on home network.
     */
    private LocalizationServiceAidl localizationService;
    
    /**
     * There is only one listener as this one put's the user in 
     */
    private ServiceConnectorListener signalHandler;
    
    private ConnectionWatchdog watchdog;
    private AndroidPreferencesAidl prefs;
    private AndroidSettingsAidl localSettings;
    
    private Context context;
    
    private static boolean isCalled = false;
    
    /**
     * Make it possible to do callbacks to the connecting client.
     */
    private final RemoteCallbackList<ClientCallbacksServiceAidl> clientCallBackList = new RemoteCallbackList<ClientCallbacksServiceAidl>();
    
    private final ExecutorService singleThreadfPipeExecutor = Executors.newSingleThreadExecutor();
    
    public SystemServiceAidl(){
        super();
        isCalled = true;
    }
    
    public static boolean isCalled(){
        return isCalled;
    }
    
    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        context = getBaseContext();
        if(connection==null && client==null){
            try {
                watchdog = new ConnectionWatchdog();
                watchdog.setInitialContext(context);
                
                prefs = new AndroidPreferencesAidl(context);
                localSettings = new AndroidSettingsAidl(context);
                
                connection = new PCCConnection(localSettings, ServerConnection.Profile.MOBILE, watchdog);
                client = new PCCClient(localSettings, prefs, connection, false);
                
                connection.setCustomBroadcastListener(new AndroidBroadcastReceiver(context));
                connection.addPCCConnectionListener(connectionListener);
                connection.addPCCConnectionNameSpaceListener("NotificationService", notificationListener);
                connection.addPCCConnectionNameSpaceListener(dispatchListener);
                client.addListener(clientListener);
                
                localizationService = new LocalizationServiceAidl(this, prefs);
                
                if(localSettings.getStringSetting("user.login", "").equals("") || localSettings.getStringSetting("user.userinfo", "").equals("")){
                    localSettings.setStringSetting("user.login", generateDeviceId(context));
                    localSettings.setStringSetting("user.userinfo", DeviceUtils.getDeviceName());
                    try {
                        localSettings.storeSettings();
                    } catch (IOException ex) {
                        Logger.getLogger(SystemService.class.getName()).log(Level.SEVERE, "Problem storing initial settings", ex);
                    }
                }
                Log.i(TAG, "PiDome SystemService started");
                running = true;
                serviceLogin();
            } catch (ConnectionException ex) {
                Logger.getLogger(SystemServiceAidl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return START_STICKY;
    }
    
    protected final PCCClient getClient(){
        return this.client;
    }

    protected final PCCConnection getConnection(){
        return this.connection;
    }
    
    public void serviceLogin(){
        new Thread() { 
            @Override 
            public final void run(){ 
                if(!connection.inConnectionProgress() && !client.inLoginProgress() && !client.isloggedIn()){
                    System.out.println("Is connected: " + connection.inConnectionProgress() + ", " + client.inLoginProgress() + ", " + client.isloggedIn());
                    if(connection.hasInitialManualConnectData()){
                        connection.startInitialConnection();
                    } else {
                        connection.startSearch();
                    }
                }
                watchdog.addEventListener(SystemServiceAidl.this);
            } 
        }.start();
    }
    
    /**
     * When bound to this service it returns the local binder.
     * This local binder guarantees access to this service components
     * and the PCC system.
     * @param intent
     * @return 
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "OnBind");
        final ResultReceiver rec = (ResultReceiver) intent.getParcelableExtra("rec");
         
        return new SystemServiceAidlInterface.Stub() {
            @Override
            public void serviceLogin() throws RemoteException {
                SystemServiceAidl.this.serviceLogin();
            }

            @Override
            public void manualConnect(String host, int port, boolean secure) throws RemoteException {
                SystemServiceAidl.this.connection.startManualConnection(host, port, secure);
            }

            @Override
            public void startSearch() throws RemoteException {
                SystemServiceAidl.this.connection.startSearch();
            }

            @Override
            public void startInitialConnection() throws RemoteException {
                SystemServiceAidl.this.connection.startInitialConnection();
            }

            @Override
            public boolean hasInitialManualConnectData() throws RemoteException {
                return SystemServiceAidl.this.connection.hasInitialManualConnectData();
            }

            @Override
            public void disconnect() throws RemoteException {
                SystemServiceAidl.this.connection.disconnect();
            }

            @Override
            public String getHost() throws RemoteException {
                return SystemServiceAidl.this.connection.getConnectionData().getHost();
            }

            @Override
            public int getSocketPort() throws RemoteException {
                return SystemServiceAidl.this.connection.getConnectionData().getSocketPort();
            }

            @Override
            public int getSocketSSLPort() throws RemoteException {
                return SystemServiceAidl.this.connection.getConnectionData().getSocketSSLPort();
            }

            @Override
            public boolean getSocketHasSSL() throws RemoteException {
                return SystemServiceAidl.this.connection.getConnectionData().getSocketHasSSL();
            }

            @Override
            public int getHttpPort() throws RemoteException {
                return SystemServiceAidl.this.connection.getConnectionData().getHttpPort();
            }

            @Override
            public int getHttpSSLPort() throws RemoteException {
                return SystemServiceAidl.this.connection.getConnectionData().getHttpSSLPort();
            }

            @Override
            public boolean getHttpHasSSL() throws RemoteException {
                return SystemServiceAidl.this.connection.getConnectionData().getHttpHasSSL();
            }

            @Override
            public void sendData(String data) throws RemoteException {
                try {
                    SystemServiceAidl.this.connection.sendData(data);
                } catch (PCCConnectionException ex) {
                    Logger.getLogger(SystemServiceAidl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public String getSimpleXmlHttp(String url, Map params) throws RemoteException {
                try {
                    return SystemServiceAidl.this.connection.getSimpleXmlHttp(url, params);
                } catch (PCCEntityDataHandlerException ex) {
                    Logger.getLogger(SystemServiceAidl.class.getName()).log(Level.SEVERE, null, ex);
                    return "";
                }
            }

            @Override
            public String getJsonHTTPRPCAsString(String method, Map params, String requestId) throws RemoteException {
                try {
                    Log.v(TAG, "Sending http rpc method: " + method + ", params: " + params + ", request id: " + requestId);
                    String returnString = SystemServiceAidl.this.connection.getJsonHTTPRPCAsString(method, params, requestId);
                    Log.v(TAG, "Returning http rpc: " + returnString);
                    return returnString;
                } catch (PCCEntityDataHandlerException ex) {
                    Logger.getLogger(SystemServiceAidl.class.getName()).log(Level.SEVERE, null, ex);
                    return "";
                }
            }

            @Override
            public byte[] getBinaryHttp(String url, Map params) throws RemoteException {
                try {
                    Log.v(TAG, "Sending http get binary url: " + url + ", params: " + params);
                    return SystemServiceAidl.this.connection.getBinaryHttp(url, params);
                } catch (PCCEntityDataHandlerException ex) {
                    Logger.getLogger(SystemServiceAidl.class.getName()).log(Level.SEVERE, null, ex);
                    return new byte[0];
                }
            }
            
            @Override
            public boolean GPSEnabled() throws RemoteException {
                return SystemServiceAidl.this.localizationService.GPSEnabled();
            }

            @Override
            public long getGPSDelay() throws RemoteException {
                return SystemServiceAidl.this.localizationService.getGPSDelay();
            }

            @Override
            public void setLocalizationPreferences(boolean enabled, long timeToWait, boolean wifiHomeEnabled) throws RemoteException {
                SystemServiceAidl.this.localizationService.setLocalizationPreferences(enabled, timeToWait, wifiHomeEnabled);
            }

            @Override
            public boolean getHomeNetworkHomePresenceEnabled() throws RemoteException {
                return SystemServiceAidl.this.localizationService.getHomeNetworkHomePresenceEnabled();
            }

            @Override
            public String getHomeNetworkHomePresenceWifiNetworkName() throws RemoteException {
                try {
                    return SystemServiceAidl.this.localizationService.getHomeNetworkHomePresenceWifiNetworkName();
                } catch (IOException ex) {
                    Logger.getLogger(SystemServiceAidl.class.getName()).log(Level.SEVERE, null, ex);
                    return "";
                }
            }

            @Override
            public void registerCallBack(ClientCallbacksServiceAidl callback) throws RemoteException {
                clientCallBackList.register(callback);
            }

            @Override
            public boolean inConnectionProgress() throws RemoteException {
                return SystemServiceAidl.this.connection.inConnectionProgress();
            }

            @Override
            public String getCurrentConnectionStatusAsString() throws RemoteException {
                return SystemServiceAidl.this.connection.getCurrentStatus().toString();
            }
            
            @Override
            public String getCurrentClientStatusAsString() throws RemoteException {
                return SystemServiceAidl.this.client.getCurrentClientStatus().toString();
            }

            @Override
            public boolean isLoggedIn() throws RemoteException {
                try {
                    return SystemServiceAidl.this.client.isloggedIn();
                } catch (Exception ex){
                    /// Not ready
                    System.out.println("Unknown login: " + ex.getMessage());
                    System.out.println(ex);
                    return false;
                }
            }

            @Override
            public boolean inLoginProgress() throws RemoteException {
                return SystemServiceAidl.this.client.inLoginProgress();
            }

            @Override
            public void clientLogin() throws RemoteException {
                SystemServiceAidl.this.client.login();
            }

        };
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
        if(nae.getEventType().equals(NetworkAvailabilityProvider.Status.NETWORKAVAILABLE)){
            serviceLogin();
        }
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
            Log.i(TAG, "Received connection event: " + status.toString());
            singleThreadfPipeExecutor.execute((Runnable)() -> {
                switch (status) {
                    case CONNECTED:
                        client.login();
                    break;
                    case DISCONNECTED:
                        client.logout();
                    break;
                }
                int i = SystemServiceAidl.this.clientCallBackList.beginBroadcast();
                System.out.println("Amount of items in broadcast list for connection: " + i);
                while(i>0){
                    i--;
                    System.out.println("Running from position: " + i);
                    try {
                        SystemServiceAidl.this.clientCallBackList.getBroadcastItem(i).broadcastConnectionEvent(status.toString(), 0, "");
                        switch (status) {
                            case CONNECTED:
                                /// Not here, broadcast must be fast
                            break;
                            case DISCONNECTED:
                                try {
                                    clientCallBackList.getBroadcastItem(i).handleUserLoggedOut();
                                } catch (RemoteException ex) {
                                    Logger.getLogger(SystemServiceAidl.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            break;
                        }
                    } catch (RemoteException ex) {
                        Logger.getLogger(SystemServiceAidl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                SystemServiceAidl.this.clientCallBackList.finishBroadcast();
            });
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
            Log.v(TAG, "Received client event: " + event.getStatus().toString());
            singleThreadfPipeExecutor.execute((Runnable)() -> {
                switch (event.getStatus()) {
                    case LOGGED_IN:
                        try {
                            localSettings.storeSettings();
                        } catch (IOException ex) {
                            Logger.getLogger(SystemService.class.getName()).log(Level.SEVERE, "Could not store connection settings", ex);
                        }
                    break;
                    case INIT_DONE:
                        setHome();
                    break;
                }
                int i = SystemServiceAidl.this.clientCallBackList.beginBroadcast();
                System.out.println("Amount of items in broadcast list for client: " + i);
                while(i>0){
                    i--;
                    try {
                        System.out.println("Sending to broadcast: " + event.getStatus().toString() + ",  "+ event.getErrorCode() +", "+ event.getMessage());
                        SystemServiceAidl.this.clientCallBackList.getBroadcastItem(i).broadcastLoginEvent(event.getStatus().toString(), event.getErrorCode(), event.getMessage());
                        switch (event.getStatus()) {
                            case LOGGED_IN:
                                /// already done.
                            break;
                            case INIT_DONE:
                                try {
                                    clientCallBackList.getBroadcastItem(i).handleUserLoggedIn();
                                } catch (RemoteException ex) {
                                    Logger.getLogger(SystemServiceAidl.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            break;
                        }
                    } catch (RemoteException ex) {
                        Logger.getLogger(SystemServiceAidl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                SystemServiceAidl.this.clientCallBackList.finishBroadcast();
            });
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
                                singleThreadfPipeExecutor.execute((Runnable)() -> {
                                    int i = SystemServiceAidl.this.clientCallBackList.beginBroadcast();
                                    while(i>0){
                                        i--;
                                        try {
                                            clientCallBackList.beginBroadcast();
                                            clientCallBackList.getBroadcastItem(i).updateUserPresence(1);
                                            clientCallBackList.finishBroadcast();
                                        } catch (RemoteException ex) {
                                            Logger.getLogger(SystemServiceAidl.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                });
                                localizationService.setHome(true);
                            } else {
                                localizationService.setHome(false);
                            }
                        }
                    }
                } else if (networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    localizationService.setHome(false);
                }
            }
        }
    }
    
    private class DispatchDataListener implements PCCConnectionNameSpaceRPCListener {

        @Override
        public void handleRPCCommandByBroadcast(PCCEntityDataHandler pccedh) {
            Log.v(TAG, "Received RPC: " + pccedh.getPlainData());
            singleThreadfPipeExecutor.execute((Runnable)() -> {
                int i = SystemServiceAidl.this.clientCallBackList.beginBroadcast();
                while(i>0){
                    i--;
                    try {
                        SystemServiceAidl.this.clientCallBackList.getBroadcastItem(i).broadcastServerRPCFromStream(pccedh.getPlainData());
                    } catch (RemoteException ex) {
                        Logger.getLogger(SystemServiceAidl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                SystemServiceAidl.this.clientCallBackList.finishBroadcast();
            });
        }

        @Override
        public void handleRPCCommandByResult(PCCEntityDataHandler pccedh) {
            /// not used.
        }
        
    }
    
    private class NotificationListener implements PCCConnectionNameSpaceRPCListener {

        @Override
        public void handleRPCCommandByBroadcast(PCCEntityDataHandler pccedh) {
            if(pccedh.getMethod().equals("sendNotification")){
                handleNotificationMessage(pccedh.getParameters());
            }
        }

        /**
         * Handle notification messages.
         * @todo handle ipc stub to check if app is in foreground or not.
         * @param params 
         */
        private void handleNotificationMessage(final Map<String,Object> params){
            singleThreadfPipeExecutor.execute((Runnable)() -> {
                boolean appIsInForeGround = false;
                int i = SystemServiceAidl.this.clientCallBackList.beginBroadcast();
                while(i>0){
                    i--;
                    try {
                        appIsInForeGround = SystemServiceAidl.this.clientCallBackList.getBroadcastItem(i).appIsInForeGround();
                    } catch (RemoteException ex) {
                        Logger.getLogger(SystemServiceAidl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                SystemServiceAidl.this.clientCallBackList.finishBroadcast();
                if(appIsInForeGround){
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
                    NotificationCompat.Builder notification = new NotificationCompat.Builder(SystemServiceAidl.this)
                            .setOnlyAlertOnce(true)
                            .setSmallIcon(SystemServiceAidl.this.getResources().getIdentifier("notificationsmall", "drawable", SystemServiceAidl.this.getPackageName()))
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("notification", "drawable", SystemServiceAidl.this.getPackageName())))
                            .setContentTitle((String)params.get("subject"))
                            .setContentText((String)params.get("message"))
                            ///.setContentIntent(pIntent) We are going to use this based on message types in the future.
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setAutoCancel(true);

                    NotificationManager mNotificationManager = (NotificationManager) SystemServiceAidl.this.getSystemService(Context.NOTIFICATION_SERVICE);
                    /// Set an id so notification will override, it is possible when an user has a lot of notifications the list could stack up.
                    /// A todo would be to create notifications which are distinguised by categories. But this is being to be discussed.
                    int mId = 12345;
                    mNotificationManager.notify(mId, notification.build());
                }
            });
        }
        
        @Override
        public void handleRPCCommandByResult(PCCEntityDataHandler pccedh) {
            /// Not used
        }
    
    }
    
}