/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.pidomeremote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfiguration;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.interfaces.web.configuration.WebOption;
import org.pidome.server.connector.plugins.freeform.FreeformPlugin;
import org.pidome.server.connector.plugins.hooks.DeviceHook;
import org.pidome.server.connector.plugins.hooks.DeviceHookListener;

/**
 *
 * @author John
 */
public abstract class PiDomeRemote extends FreeformPlugin implements DeviceHookListener {
    
    static Logger LOG = LogManager.getLogger(PiDomeRemote.class);

    public enum RemoteType {
        DEFAULT,UNIVERSAL
    }
    
    RemoteType type = RemoteType.DEFAULT;
    
    List<PiDomeRemoteEventListener> listeners = new ArrayList();
    
    List<RemoteButton> buttons = new ArrayList<>();
    
    int recorderDeviceId          = 0;
    String recorderDeviceValueSet = "";
    
    int senderDeviceId          = 0;
    String senderDeviceValueSet = "";
    
    String recorderDeviceGroup = "";
    String recorderDeviceSet = "";
    
    String senderDeviceGroup = "";
    String senderDeviceSet = "";
    
    private final ScheduledExecutorService delayedExecutor = Executors.newSingleThreadScheduledExecutor();
    
    public PiDomeRemote(){
        constructSettings();
    }
    
    /**
     * Creates a button.
     * @param id 
     * @param type 
     * @param isUniversal 
     * @param row 
     * @param column 
     * @param signal 
     * @param delay 
     */
    public abstract void createButton(String id, String type, boolean isUniversal, int row, int column, String signal, long delay);
    /**
     * Deletes a button.
     * @param id 
     */
    public abstract void deleteButton(String id);
    /**
     * Returns a single button.
     * @param id 
     * @return RemoteButton the button requested.
     * @throws org.pidome.pluginconnector.pidomeremote.PiDomeRemoteButtonException 
     */
    public abstract RemoteButton getButton(String id) throws PiDomeRemoteButtonException;
    
    /**
     * Handles the button pressed.
     * @param id 
     * @throws org.pidome.pluginconnector.pidomeremote.PiDomeRemoteButtonException 
     */
    public abstract void handleButton(String id) throws PiDomeRemoteButtonException ;

    public abstract void createButtonsSet(Map<String,Object> remoteData);
    
    /**
     * Adds a button press to the queue.
     * @param buttons 
     */
    public final void handleButtonBatchExecution(ArrayList<Runnable> buttons){
        LOG.debug("Handling execution list: {}", buttons);
        buttons.stream().forEach((button) -> {
            delayedExecutor.execute(button);
        });
    }
    
    /**
     * Creates an array of runnables applicable for the remote specific's work queue.
     * Default remote: Adds to it's own executions list.
     * Universal remote: Dispatches to the correct plugin so it can handle the queue itself.
     * This means multiple remotes can work at the same time keeping it's own track of it's own button delays and cross post to the output devices creating parallel execution.
     * @param button
     * @return 
     */
    public final ArrayList<Runnable> createButtonActions(RemoteButton button){
        ArrayList<Runnable> runnables = new ArrayList<>();
        if(button.getIsUniversal()){
            ((UniversalRemoteButton)button).getButtonsActionList().stream().forEach((RemoteButton workButton) -> {
                PiDomeRemote plugin = workButton.getRemotePlugin();
                plugin.handleButtonBatchExecution(plugin.createButtonActions(button));
            });
        } else {
            runnables.add(() -> {
                LOG.debug("Adding button action {} to be delivered to {}, {}, {} with delay: {}", ((DefaultRemoteButton)button).getButtonAction(),senderDeviceId ,senderDeviceGroup ,senderDeviceSet, ((DefaultRemoteButton)button).getButtonDelay());
                DeviceHook.deliver(this, 
                                   senderDeviceId, 
                                   senderDeviceGroup, 
                                   senderDeviceSet, 
                                   ((DefaultRemoteButton)button).getButtonAction());
                try {
                    Thread.sleep(((DefaultRemoteButton)button).getButtonDelay());
                } catch (InterruptedException ex) {
                    /// do nothing.
                }
            });
        }
        return runnables;
    }
    
    /**
     * Sets a universal remote buttons button list.
     * @param buttonId
     * @param buttonList 
     */
    public abstract void setUniversalButtonButtons(String buttonId, ArrayList<RemoteButton> buttonList);
    
    
    /**
     * Returns the button list.
     * @return 
     */
    public final List<RemoteButton> getButtons(){
        return this.buttons;
    }
    
    public final void setButtonPlugin(RemoteButton button){
        button.setRemotePlugin(this);
    }
    
    /**
     * Sets the remote type.
     * Based on the remote type the button action configuration is applied.
     * An universal remote uses buttons from a default remote.
     * @param type 
     */
    public final void setRemoteType(RemoteType type){
        this.type = type;
    }
    
    /**
     * Constructs the settings.
     */
    public final void constructSettings(){
        WebConfiguration conf = new WebConfiguration();
        WebConfigurationOptionSet optionSetWhichDevices = new WebConfigurationOptionSet("Select recording and transmitting devices");
        optionSetWhichDevices.addOption(new WebOption("RECORDERDEVICE", "Select device for recording", "Select a device which is used to record the remote data for this remote.", WebOption.WebOptionConfigurationFieldType.DEVICEDATA));
        optionSetWhichDevices.addOption(new WebOption("SENDERDEVICE", "Select device for transmitting", "Select device to use which will send the data for this remote.", WebOption.WebOptionConfigurationFieldType.DEVICEDATA));
        conf.addOptionSet(optionSetWhichDevices);
        
        WebConfigurationOptionSet optionsSetRemoteOptions = new WebConfigurationOptionSet("Select remote options");
        
        Map<String,String> remoteTypeOptions = new HashMap<>();
        ///remoteTypeOptions.put("REMOTE_TYPE_UNIVERSAL", "Universal remote");
        remoteTypeOptions.put("REMOTE_TYPE_DEFAULT", "Normal remote");
        optionsSetRemoteOptions.addOption(new WebOption("REMOTE_TYPE", "Select remote type", "Select universal if you want to assign buttons from other remotes, otherwise choose normal.", WebOption.WebOptionConfigurationFieldType.SELECT,remoteTypeOptions));
                
        conf.addOptionSet(optionsSetRemoteOptions);
        
        this.setConfiguration(conf);
    }
    
    
    @Override
    public void setConfigurationValues(Map<String, String> configuration) throws WebConfigurationException {
        LOG.debug("plugin id: {} having configuration values: {}", this.getPluginId(), configuration);
        if(configuration.get("RECORDERDEVICE")!=null && !configuration.get("RECORDERDEVICE").equals("")){
            String[] recorderSplitted = configuration.get("RECORDERDEVICE").split("_");
            setRecorderDevice(Integer.valueOf(recorderSplitted[0]), "ACTIONDATA", "SENDDATA");
        }
        if(configuration.get("SENDERDEVICE")!=null && !configuration.get("SENDERDEVICE").equals("")){
            String[] senderSplitted = configuration.get("SENDERDEVICE").split("_");
            setSenderDevice(Integer.valueOf(senderSplitted[0]), "ACTIONDATA", "SENDDATA");
        }
    }

    /**
     * Sets recording device.
     * @param deviceId
     * @param deviceGroup
     * @param deviceSet 
     */
    public final void setRecorderDevice(int deviceId, String deviceGroup, String deviceSet) {
        DeviceHook.remove(this, deviceId, "");
        recorderDeviceId = deviceId;
        recorderDeviceGroup = deviceGroup;
        recorderDeviceSet = deviceSet;
        DeviceHook.addDevice(this, recorderDeviceId, deviceSet);
    }
    
    /**
     * Sets sender device.
     * @param deviceId
     * @param deviceGroup
     * @param deviceSet 
     */
    public final void setSenderDevice(int deviceId, String deviceGroup, String deviceSet) {
        DeviceHook.remove(this, deviceId, "");
        senderDeviceId = deviceId;
        senderDeviceGroup = deviceGroup;
        senderDeviceSet = deviceSet;
        DeviceHook.addDevice(this, senderDeviceId, deviceSet);
    }
    
    /**
     * Starts the plugin.
     * @throws PluginException 
     */
    @Override
    public void startPlugin() throws PluginException {
        this.setRunning(true);
    }

    /**
     * Stops the plugin.
     * @throws PluginException 
     */
    @Override
    public void stopPlugin() throws PluginException {
        this.setRunning(false);
    }
    
    /**
     * Adds a listener.
     * @param l 
     */
    public final void addListener(PiDomeRemoteEventListener l){
        if(!listeners.contains(l)) listeners.add(l);
    }
    
    /**
     * Removes a listener;
     * @param l 
     */
    public final void removeListener(PiDomeRemoteEventListener l){
        if(listeners.contains(l)) listeners.remove(l);
    }

    /**
     * Returns the recorder device id.
     * @return 
     */
    public final int getRecorderDevice(){
        return this.recorderDeviceId;
    }
    
    /**
     * Returns the sender device id.
     * @return 
     */
    public final int getSenderDevice(){
        return this.senderDeviceId;
    }
    
    /**
     * Returns the device group.
     * @return 
     */
    protected final String getSenderDeviceGroup(){
        return this.senderDeviceGroup;
    }
    
    /**
     * Returns the device set.
     * @return 
     */
    protected final String getSenderDeviceSet(){
        return this.senderDeviceSet;
    }
    
}