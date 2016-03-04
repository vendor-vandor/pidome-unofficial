package org.pidome.client.services.aidl.client;

import android.content.SharedPreferences;
import org.pidome.client.services.aidl.service.SystemServiceAidlInterface;
import android.content.pm.ActivityInfo;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafxports.android.FXActivity;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.services.LifeCycleHandler;
import org.pidome.client.services.LifeCycleHandlerInterface;
import org.pidome.client.services.PhysicalDisplayInterface;
import org.pidome.client.services.PlatformOrientation;
import static org.pidome.client.services.PlatformOrientation.Orientation.LANDSCAPE;
import static org.pidome.client.services.PlatformOrientation.Orientation.PORTRAIT;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.services.ServiceConnector.DisplayType;
import org.pidome.client.services.ServiceConnectorListener;
import org.pidome.client.services.UnsupportedPhysicalDisplayInterface;
import org.pidome.client.services.aidl.AndroidPreferencesAidl;
import org.pidome.client.services.aidl.AndroidSettingsAidl;
import org.pidome.client.settings.LocalizationInfoInterface;
import org.pidome.client.system.PCCSystem;
import org.pidome.client.tools.DisplayTools;
import org.pidome.pcl.backend.data.interfaces.storage.LocalPreferenceStorageInterface;
import org.pidome.pcl.backend.data.interfaces.storage.LocalSettingsStorageInterface;
import org.pidome.pcl.data.connection.ConnectionException;
import org.pidome.pcl.storage.preferences.LocalPreferenceStorage;
import org.pidome.pcl.storage.settings.LocalSettingsStorage;

public class AndroidServiceConnectorAidl implements ServiceConnector,SystemServiceConnectorListenerAidl,SystemPassthroughFromRemoteServiceInterface {
    
    ServiceConnectorListener listener;
    /**
     * We want to know if the application is in the foreground or not.
     */
    private LifeCycleHandler lifeCycleHandler = new LifeCycleHandler();
    
    LocalizationInfoInterface userPresenceSettingsInterface;
    
    PCCSystem system;
    
    private static PlatformOrientation orientationListener;
    
    SystemServiceConnectorAidl serviceConnection = new SystemServiceConnectorAidl();
    
    private DisplayType currentDisplayType = DisplayType.DUNNO;
    
    public AndroidServiceConnectorAidl(){
        FXActivity.getInstance().getApplication().registerActivityLifecycleCallbacks(lifeCycleHandler);
        serviceConnection.registerPassthrough(this);
    }
    
    @Override
    public void setServiceConnectionListener(ServiceConnectorListener listener){
        this.listener = listener;
    }
    
    @Override
    public final void startService(){
        SystemServiceConnectorAidl.addServiceListener(this);
        serviceConnection.bind();
    }
    
    @Override
    public void stopService() {
        serviceConnection.unBind();
        SystemServiceConnectorAidl.removeServiceListener(this);
        serviceConnection.deRegisterPassthrough();
    }
    
    
    @Override
    public void handleSystemServiceDisConnected() {
        ////
    }

    @Override
    public void handleSystemServiceConnected(SystemServiceAidlInterface service) {
        try {
            PCCConnectionInterfaceAidlProxy connection = new PCCConnectionInterfaceAidlProxy(service);
            LocalPreferenceStorageInterface prefs = new AndroidPreferencesAidl(FXActivity.getInstance().getBaseContext());
            LocalSettingsStorageInterface sets = new AndroidSettingsAidl(FXActivity.getInstance().getBaseContext());
            system = new PCCSystem(new AndroidPreferencesAidl(FXActivity.getInstance().getBaseContext()),
                    new AndroidSettingsAidl(FXActivity.getInstance().getBaseContext()),
                    connection,
                    new PCCClientInterfaceAidlProxy(sets, prefs, connection));
            userPresenceSettingsInterface = new LocalizationInfoAidl(service);
            
            //binder.setSignalHandler(this.listener);
            
            this.listener.serviceConnected(system);
            
        } catch (ConnectionException ex) {
            Logger.getLogger(AndroidServiceConnectorAidl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    @Override
    public void broadcastLoginEventByString(String status, int errCode, String message) {
        ((PCCClientInterfaceAidlProxy)system.getClient()).broadcastLoginEventByString(status, errCode, message);
    }
    
    @Override
    public void broadcastConnectionEventByString(String status, int errCode, String message) {
        if(system!=null){
            ((PCCConnectionInterfaceAidlProxy)system.getConnection()).broadcastConnectionEventByString(status, errCode, message);
        }
    }
    
    @Override
    public void updateGPSDistance(double distance) {
        ((LocalizationInfoAidl)userPresenceSettingsInterface).updateGPSDistance(distance);
    }

    @Override
    public void updateConnectionStatus(String status) {
        ((PCCConnectionInterfaceAidlProxy)system.getConnection()).updateCurrentConnectionStatusFromString(status);
    }

    @Override
    public void updateClientStatus(String status) {
        ((PCCClientInterfaceAidlProxy)system.getConnection()).updateCurrentClientStatusFromString(status);
    }

    @Override
    public void updateUserPresence(int presenceId) {
        try {
            system.getClient().getEntities().getPresenceService().setPresence(presenceId);
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(AndroidServiceConnectorAidl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void broadcastServerRPCFromStream(String RPCMessage) {
        ((PCCConnectionInterfaceAidlProxy)system.getConnection()).broadcastRPCData(RPCMessage);
    }
    
    @Override
    public void handleUserLoggedIn() {
        listener.handleUserLoggedIn();
    }

    @Override
    public void handleUserLoggedOut() {
        listener.handleUserLoggedOut();
    }

    @Override
    public boolean appIsInForeGround() {
        return lifeCycleHandler.inForeground();
    }
    
    @Override
    public LifeCycleHandlerInterface getLifeCycleHandler() {
        return lifeCycleHandler;
    }

    @Override
    public LocalizationInfoInterface getLocalizationService() {
        return userPresenceSettingsInterface;
    }

    @Override
    public void serviceLogin() {
        if(!system.getConnection().inConnectionProgress() && !system.getClient().isloggedIn()){
            new Thread() { 
                @Override 
                public final void run(){ 
                    if(system.getConnection().hasInitialManualConnectData()){
                        system.getConnection().startInitialConnection();
                    } else {
                        system.getConnection().startSearch();
                    }
                }
            }.start();
        }
    }

    @Override
    public DisplayType userDisplayType() {
        String orientation = FXActivity.getInstance().getSharedPreferences("settings", 0).getString("orientation", "dunno");
        if(orientation.equals("dunno")){
            currentDisplayType = DisplayType.DUNNO;
        } else if(orientation.equals("portrait")){
            currentDisplayType = DisplayType.SMALL;
        } else {
            currentDisplayType = DisplayType.LARGE;
        }
        return currentDisplayType;
    }

    @Override 
    public final void storeUserDisplayType(DisplayType type){
        SharedPreferences prefs = FXActivity.getInstance().getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = prefs.edit();
        switch(type){
            case LARGE:
                editor.putString("orientation", "landscape");
            break;
            default:
                editor.putString("orientation", "portrait");
            break;
        }
        currentDisplayType = type;
        DisplayTools.setUserDisplayType(currentDisplayType);
        editor.commit();
    }
    
    @Override
    public void addOrientationListener(PlatformOrientation listener) {
        if(orientationListener==null){
            orientationListener = listener;
        }
    }

    public static void handleOrientationChanged(PlatformOrientation.Orientation orient){
        if(orientationListener!=null){
            orientationListener.handleOrientationChanged(orient);
        }
    }

    @Override
    public void forceOrientation(PlatformOrientation.Orientation orientation) {
        switch(orientation){
            case LANDSCAPE:
                FXActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            break;
            case PORTRAIT:
                FXActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            break;
        }
    }

    /**
     * Returns the plaform base
     * @return 
     */
    @Override
    public PlatformBase getPlatformBase() {
        return PlatformBase.MOBILE;
    }

    /**
     * Not used on this platform as it needs initialization with the service connector
     * @return 
     */
    @Override
    public LocalPreferenceStorage getPreferences() {
        throw new UnsupportedOperationException();
    }

    /**
     * Not used on this platform as it needs initialization with the service connector
     * @return 
     */
    @Override
    public LocalSettingsStorage getSettings() {
        throw new UnsupportedOperationException("Not used on Android"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getMaxWorkWidth() {
        throw new UnsupportedOperationException("Not used on Android"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PhysicalDisplayInterface getPhysicalDisplayInterface() {
        return new UnsupportedPhysicalDisplayInterface(AndroidServiceConnectorAidl.this);
    }

}