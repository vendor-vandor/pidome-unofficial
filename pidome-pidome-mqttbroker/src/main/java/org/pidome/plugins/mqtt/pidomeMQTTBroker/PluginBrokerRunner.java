/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.plugins.mqtt.pidomeMQTTBroker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dna.mqtt.moquette.messaging.spi.impl.SimpleMessaging;
import org.dna.mqtt.moquette.proto.messages.AbstractMessage;
import org.dna.mqtt.moquette.proto.messages.PublishMessage;
import org.dna.mqtt.moquette.server.netty.NettyAcceptor;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroupException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceToggleControl;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentSimpleNVP;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.hooks.DeviceHook;
import org.pidome.server.connector.plugins.hooks.DeviceHookListener;
import org.pidome.server.connector.plugins.hooks.MediaHook;
import org.pidome.server.connector.plugins.hooks.MediaHookListener;
import org.pidome.server.connector.plugins.hooks.PiDomeRPCHook;
import org.pidome.server.connector.plugins.hooks.PiDomeRPCHookListener;
import org.pidome.server.connector.plugins.media.Media;
import org.pidome.server.connector.plugins.media.MediaEvent;
import org.pidome.server.connector.plugins.media.MediaPlugin;
import org.pidome.server.connector.shareddata.SharedLocationService;
import org.pidome.server.connector.tools.networking.Networking;

/**
 *
 * @author John
 */
public class PluginBrokerRunner implements PluginRunner, DeviceHookListener,MediaHookListener,FromBrokerToPiDomeInteral,PiDomeRPCHookListener {
    
    static Logger LOG = LogManager.getLogger(PluginBrokerRunner.class);
    
    PidomeMQTTBroker parent;
    
    private SimpleMessaging messaging;
    private NettyAcceptor m_acceptor;
    
    Map<String, String> config = new HashMap<>();
    
    private String PUB_ROOT = "/Home/";
    private String PUB_DEVICE_ROOT = "/hooks/devices/";
    private String PUB_RPC_ROOT = "/hooks/jsonrpc/";
    
    List<WebPresentSimpleNVP> errorList  = new ArrayList<>();
    
    private boolean running = false;
    
    protected PluginBrokerRunner(Map<String,String> options) throws WebConfigurationException {
        config = options;
        if(!config.containsKey("PORT") || config.get("PORT").equals("")){
            throw new WebConfigurationException("Check your port.");
        }
        if(!config.containsKey("PUBROOT") || config.get("PUBROOT").equals("")){
            throw new WebConfigurationException("Check your publication root.");
        } else {
            PUB_ROOT = config.get("PUBROOT");
        }
        if(!config.containsKey("PUBDEVICEROOT") || config.get("PUBDEVICEROOT").equals("")){
            throw new WebConfigurationException("Check your device root.");
        } else {
            PUB_DEVICE_ROOT = config.get("PUBDEVICEROOT");
        }
        if(config.containsKey("PUBRPCROOT") && !config.get("PUBRPCROOT").equals("")){
            PUB_RPC_ROOT = config.get("PUBRPCROOT");
        }
    }
    
    @Override
    public void start() throws PluginException {
        try {
            DeviceHook.addAllDevices(this);
            MediaHook.addAllMedia(this);
            Properties configProps = new Properties();
            configProps.put("password_file", "");
            configProps.put("port", config.get("PORT"));
            configProps.put("host", Networking.getIpAddressProperty().get().getHostAddress());
            
            messaging = SimpleMessaging.getInstance();
            messaging.init(configProps);
            m_acceptor = new NettyAcceptor();
            m_acceptor.initialize(messaging, configProps);
            
            messaging.getProcessor().addPiDomeListener(this);
            running = true;
        } catch (IOException ex) {
            LOG.error("Could not start MQTT Service using port {}: {}", config.get("PORT"), ex.getMessage(),ex);
            parent.LogError(new StringBuilder("Could not start on port ").append(config.get("PORT")).append(": ").append(ex.getMessage()).toString());
        }
    }
    
    @Override
    public void stop(){
        if(messaging!=null) messaging.getProcessor().removeInternalListener();
        if(messaging!=null)messaging.stop();
        if(m_acceptor!=null)m_acceptor.close();
        this.running = false;
        messaging = null;
        m_acceptor = null;
    }

    @Override
    public void handleDeviceData(Device device, String group, String control, byte[] data, boolean userIntent) {
        if(running){
            try {
                PublishMessage message = new PublishMessage();
                message.setTopicName(new StringBuilder(PUB_DEVICE_ROOT)
                                        .append(device.getId())
                                        .append("/")
                                        .append(group)
                                        .append("/")
                                        .append(control).toString());
                message.setRetainFlag(false);
                message.setQos(AbstractMessage.QOSType.MOST_ONE);
                message.setPayload(ByteBuffer.wrap(data));
                messaging.getProcessor().processInternalPublish(message);
            } catch (Exception ex) {
                LOG.error("Could not publish to broker: {}", ex.getMessage(), ex);
            }
        }
    }

    @Override
    public final void handleFromExternal(String clientId, String topic, ByteBuffer message){
        LOG.debug("Broker handling: {}, {}, {}", clientId, topic, new String(message.array()));
        if(topic.startsWith(PUB_RPC_ROOT)){
            Runnable runRpc = () -> {
                LOG.debug("Passing to interpreter hook");
                LOG.debug("Inteperter result {}", PiDomeRPCHook.interpretRPCMessage(PluginBrokerRunner.this, new String(message.array())));
            };
            runRpc.run();
        } else {
            if(topic.startsWith(PUB_DEVICE_ROOT)){
                Runnable run = () -> {
                    String path[] = topic.split("/");
                    int deviceId = Integer.parseInt(path[3]);
                    LOG.trace("Running devices: {}", parent.getHardwareDevice().getSoftwareDriver().getRunningDevices().size());
                    for(Device device:parent.getHardwareDevice().getSoftwareDriver().getRunningDevices()){
                        if(device.getId()==deviceId){
                            String group = path[4];
                            String control = path[5];
                            try {
                                DeviceControl deviceControl = device.getFullCommandSet().getControlsGroup(group).getDeviceControl(control);
                                String toPublish = new String(message.array());
                                DeviceNotification notification = new DeviceNotification();
                                switch(deviceControl.getControlType()){
                                    case TOGGLE:
                                        notification.addData(group, control, toPublish.equals(((DeviceToggleControl)deviceControl).getOnValue().toString()));
                                    break;
                                    default:
                                        switch(deviceControl.getDataType()){
                                            case STRING:
                                            case HEX:
                                                notification.addData(group, control, toPublish);
                                                break;
                                            case INTEGER:
                                                notification.addData(group, control, Integer.valueOf(toPublish));
                                                break;
                                            case FLOAT:
                                                notification.addData(group, control, Float.valueOf(toPublish));
                                                break;
                                            case BOOLEAN:
                                                notification.addData(group, control, Boolean.valueOf(toPublish));
                                                break;
                                        }
                                        break;
                                }
                                device.dispatchToHost(notification);
                            } catch (DeviceControlsGroupException | DeviceControlException ex) {
                                LOG.error("Could not find the control (group): {} - {}: {}", group,control,ex.getMessage());
                            } catch (Exception ex){
                                LOG.error("Error handling data ({}) from broker: {}", message, ex.getMessage());
                            }
                        }
                    }
                };
                run.run();
            }
        }
    }

    @Override
    public void setParent(PidomeMQTTBroker parent) {
        this.parent = parent;
    }

    @Override
    public void unsetParent() {
        this.parent = null;
    }

    @Override
    public void handleDeviceData(Device device, String group, String control, DeviceControl deviceControl, Object deviceValue) {
        if(running){
            try {
                handleDeviceData(device, group, control, deviceValue.toString().getBytes(), true);
                Map<String,Object> location = SharedLocationService.getLocation(device.getLocationId());
                String groupName = device.getFullCommandSet().getControlsGroup(group).getGroupLabel();
                String controlName = device.getFullCommandSet().getControlsGroup(group).getDeviceControl(control).getDescription();
                PublishMessage message = new PublishMessage();
                message.setTopicName(new StringBuilder(PUB_ROOT)
                                        .append((String)location.get("floorname"))
                                        .append("/")
                                        .append((String)location.get("name"))
                                        .append("/device/")
                                        .append(device.getDeviceName())
                                        .append("/")
                                        .append(groupName)
                                        .append("/")
                                        .append(controlName).toString());
                message.setRetainFlag(false);
                message.setQos(AbstractMessage.QOSType.MOST_ONE);
                message.setPayload(ByteBuffer.wrap(deviceValue.toString().getBytes()));
                messaging.getProcessor().processInternalPublish(message); 
            } catch (Exception ex) {
                LOG.error("Could not publish to broker: {}", ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * Handles any media event.
     * @param event 
     */
    @Override
    public void handleMediaEvent(MediaEvent event) {
        if(running){
            try {
                Media media = event.getSource();
                String pluginName = media.getPluginName();
                Map<String,Object> location = SharedLocationService.getLocation(media.getPluginLocationId());
                switch(event.getEventType()){
                    case PLAYER_PLAY:
                        Map<String,Object> playerData = media.getNowPlayingData();
                        publishMediaItem((String)location.get("floorname"),(String)location.get("name"),pluginName,"action",event.getEventType().toString().toLowerCase());
                        publishMediaItem((String)location.get("floorname"),(String)location.get("name"),pluginName,"type",((MediaPlugin.ItemType)playerData.get("ItemType")).toString().toLowerCase());
                        if ((MediaPlugin.ItemType)playerData.get("ItemType") == MediaPlugin.ItemType.AUDIO) {
                            publishMediaItem((String)location.get("floorname"),(String)location.get("name"),pluginName,"title",(String)playerData.get(MediaPlugin.ItemDetails.TITLE.toString()));
                            publishMediaItem((String)location.get("floorname"),(String)location.get("name"),pluginName,"title artist",(String)playerData.get(MediaPlugin.ItemDetails.TITLE_ARTIST.toString()));
                            publishMediaItem((String)location.get("floorname"),(String)location.get("name"),pluginName,"album",(String)playerData.get(MediaPlugin.ItemDetails.ALBUM.toString()));
                            publishMediaItem((String)location.get("floorname"),(String)location.get("name"),pluginName,"album artist",(String)playerData.get(MediaPlugin.ItemDetails.ALBUM_ARTIST.toString()));
                            publishMediaItem((String)location.get("floorname"),(String)location.get("name"),pluginName,"duration",playerData.get(MediaPlugin.ItemDetails.DURATION.toString()).toString());
                        } else {
                            publishMediaItem((String)location.get("floorname"),(String)location.get("name"),pluginName,"title",(String)playerData.get(MediaPlugin.ItemDetails.TITLE.toString()));
                            publishMediaItem((String)location.get("floorname"),(String)location.get("name"),pluginName,"duration",playerData.get(MediaPlugin.ItemDetails.DURATION.toString()).toString());
                        }
                    break;
                    case PLAYER_PAUSE:
                        publishMediaItem((String)location.get("floorname"),(String)location.get("name"),pluginName,"action",event.getEventType().toString().toLowerCase());
                    break;
                    case PLAYER_STOP:
                        publishMediaItem((String)location.get("floorname"),(String)location.get("name"),pluginName,"action",event.getEventType().toString().toLowerCase());
                    break;
                }
            } catch (Exception ex) {
                LOG.error("Could not publish to broker: {}", ex.getMessage(), ex);
            }
        }
    }
    
    private void publishMediaItem(String floor, String room, String mediaName, String subject, String value){
        PublishMessage message = new PublishMessage();
        message.setTopicName(new StringBuilder(PUB_ROOT)
                                .append(floor)
                                .append("/")
                                .append(room)
                                .append("/media/")
                                .append(mediaName)
                                .append("/")
                                .append(subject).toString());
        message.setRetainFlag(false);
        message.setQos(AbstractMessage.QOSType.MOST_ONE);
        message.setPayload(ByteBuffer.wrap(value.getBytes()));
        messaging.getProcessor().processInternalPublish(message);
    }

    @Override
    public void handleRPCString(String string) {
        PublishMessage message = new PublishMessage();
        message.setTopicName(PUB_RPC_ROOT);
        message.setRetainFlag(false);
        message.setQos(AbstractMessage.QOSType.MOST_ONE);
        message.setPayload(ByteBuffer.wrap(string.getBytes()));
        messaging.getProcessor().processInternalPublish(message);
    }

    @Override
    public String getFriendlyName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getPluginId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
