package org.pidome.client.phone.services;

import javafxports.android.FXActivity;
import org.pidome.client.phone.dialogs.settings.LocalizationInfoInterface;
import org.pidome.client.phone.services.SystemService.BindExposer;
import org.pidome.client.system.PCCSystem;

public class AndroidServiceConnector implements ServiceConnector, SystemServiceConnectorListener {
    
    ServiceConnectorListener listener;
    LifeCycleHandler lifeCycleHandler;
    LocalizationInfoInterface userPresenceSettingsInterface;
    
    public AndroidServiceConnector(){
        System.out.println("Rewriting java.io.tmpdir to " + FXActivity.getInstance().getCacheDir().getAbsolutePath());
        System.setProperty("java.io.tmpdir", FXActivity.getInstance().getCacheDir().getAbsolutePath());
    }
    
    @Override
    public void setServiceConnectionListener(ServiceConnectorListener listener){
        this.listener = listener;
    }
    
    @Override
    public final void startService(){
        SystemServiceConnector.addServiceListener(this);
        SystemServiceConnector.bind();
    }
    
    @Override
    public void stopService() {
        SystemServiceConnector.unBind();
    }
    
    
    @Override
    public void handleSystemServiceDisConnected() {

    }

    @Override
    public void handleSystemServiceConnected(BindExposer binder) {
        PCCSystem system              = binder.getPCCSystem();
        lifeCycleHandler              = binder.getLifeCycleHandler();
        userPresenceSettingsInterface = binder.getPresenceServiceSettings();
        this.listener.serviceConnected(system);
        binder.setSignalHandler(this.listener);
    }

    @Override
    public LifeCycleHandlerInterface getLifeCycleHandler() {
        return lifeCycleHandler;
    }

    @Override
    public LocalizationInfoInterface getLocalizationService() {
        return userPresenceSettingsInterface;
    }

}