/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.plugins.mqtt.pidomeMQTTBroker;

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.fusesource.mqtt.client.Tracer;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceToggleControl;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.hooks.PiDomeRPCHook;
import org.pidome.server.connector.plugins.hooks.PiDomeRPCHookListener;

/**
 *
 * @author John
 */
public class PluginClientRunner implements PluginRunner,PiDomeRPCHookListener {
    
    private MQTT mqtt;
    private CallbackConnection connection;
    
    static Logger LOG = LogManager.getLogger(PluginClientRunner.class);
    
    private String PUB_DEVICE_ROOT = "/hooks/devices/";
    private String PUB_RPC_ROOT = "/hooks/jsonrpc/";
    
    Map<String, String> config = new HashMap<>();
    
    PidomeMQTTBroker parent;
    
    Tracer tracer = new MQTTTracer();
    
    protected PluginClientRunner(Map<String,String> options) throws WebConfigurationException {
        config = options;
        if(!config.containsKey("PORT") || config.get("PORT").equals("")){
            throw new WebConfigurationException("Check your port.");
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
        if(mqtt==null){
            mqtt = new MQTT();
            mqtt.setTracer(tracer);
            try {
                mqtt.setHost(config.get("IPADDRESS"), Integer.parseInt(config.get("PORT")));
                mqtt.setClientId("PiDome");
                mqtt.setCleanSession(true);
                mqtt.setReconnectDelay(2000);
                mqtt.setReconnectDelayMax(30000);
                mqtt.setVersion("3.1");
                connection = mqtt.callbackConnection();
                connection.listener(new MQTTListener());
                connection.connect(new MQTTConnectListener());
                parent.setRunning(true);
            } catch (Exception ex) {
                parent.LogError(new StringBuilder("Could not start plugin (ip: ")
                                    .append(config.get("IPADDRESS"))
                                    .append(", port:")
                                    .append(config.get("PORT"))
                                    .append("): ")
                                    .append(ex.getMessage()).toString());
            }
        }
    }

    @Override
    public void stop() {
        if(connection!=null)connection.disconnect(new MQTTDisConnectListener());
        mqtt = null;
        parent.setRunning(false);
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
    public void handleDeviceData(Device device, String group, String control, byte[] data, boolean userIntent) {
        LOG.debug("(debug) User intent test for MQTT client publish: {}, with data {} IS USERINTENT: {}", new StringBuilder(PUB_DEVICE_ROOT).append(device.getId()).append("/").append(group).append("/").append(control).toString(), new String(data), userIntent);
        if(userIntent && parent.getRunning()){
            final String path = new StringBuilder(PUB_DEVICE_ROOT).append(device.getId()).append("/").append(group).append("/").append(control).toString();
            LOG.debug("Publish: {} from device {} to broker path: {}", data, device.getDeviceName(), path);
            connection.getDispatchQueue().execute(() -> {
                connection.publish(path, data, QoS.AT_MOST_ONCE, false, new Callback<Void>() {
                    @Override
                    public void onSuccess(Object v) {
                      LOG.trace("Succesfully published: {} to {}", data,path);
                    }
                    @Override
                    public void onFailure(Throwable ex) {
                        LOG.error("Problem publishing to {}: ", path,ex.getMessage(), ex);
                    }
                });
            });
        } else if(!parent.getRunning()) {
            LOG.error("Plugin not running, not published {} from device {} to broker", data, device.getDeviceName());
        }      
    }

    @Override
    public void handleRPCString(String RPCString) {
        if(parent.getRunning()){
            connection.getDispatchQueue().execute(() -> {
                connection.publish(PUB_RPC_ROOT, RPCString.getBytes(), QoS.AT_MOST_ONCE, false, new Callback<Void>() {
                    @Override
                    public void onSuccess(Object v) {
                      LOG.trace("Succesfully published: {} to {}", RPCString,PUB_RPC_ROOT);
                    }
                    @Override
                    public void onFailure(Throwable ex) {
                        LOG.error("Problem publishing to {}: ", RPCString,ex.getMessage(), ex);
                    }
                });
            });
        }
    }

    @Override
    public String getFriendlyName() {
        return parent.getFriendlyName();
    }

    @Override
    public int getPluginId() {
        return parent.getPluginId();
    }

    
    
    class MQTTListener implements Listener {

        @Override
        public void onConnected() {
            //// Not used.
        }

        @Override
        public void onDisconnected() {
            //// Not used.
        }

        @Override
        public void onPublish(final UTF8Buffer utfb, final Buffer buffer, final Runnable r) {
            byte[] data = utfb.getData();
            LOG.debug("Got data from broker: {} - {}", utfb.toString(), buffer.toString());
            Runnable run = () -> {
                String pathFull = utfb.toString();
                if(pathFull.startsWith(PUB_RPC_ROOT)){
                    Runnable runRpc = () -> {
                        LOG.debug("Passing to interpreter hook");
                        LOG.debug("Inteperter result {}", PiDomeRPCHook.interpretRPCMessage(PluginClientRunner.this, buffer.ascii().toString()));
                    };
                    runRpc.run();
                } else {
                    String[] path = pathFull.replace(PUB_DEVICE_ROOT,"").split("/");
                    try {
                        int deviceId   = Integer.parseInt(path[0]);
                        String groupId   = path[1];
                        String controlId = path[2];
                        for(Device device:parent.getHardwareDevice().getSoftwareDriver().getRunningDevices()){
                            if(device.getId() == deviceId){
                                DeviceControl control = device.getFullCommandSet().getControlsGroup(groupId).getDeviceControl(controlId);
                                DeviceNotification notification = new DeviceNotification();
                                switch(control.getControlType()){
                                    case TOGGLE:
                                        if(buffer.ascii().toString().equals(((DeviceToggleControl)control).getOnValue().toString())){
                                            notification.addData(groupId, controlId, true);
                                        } else {
                                            notification.addData(groupId, controlId, false);
                                        }
                                    break;
                                    default:
                                        switch(control.getDataType()){
                                            case STRING:
                                            case HEX:
                                                notification.addData(groupId, controlId, buffer.ascii().toString());
                                            break;
                                            case INTEGER:
                                                notification.addData(groupId, controlId, Integer.valueOf(buffer.ascii().toString()));
                                            break;
                                            case FLOAT:
                                                notification.addData(groupId, controlId, Float.valueOf(buffer.ascii().toString()));
                                            break;
                                            case BOOLEAN:
                                                notification.addData(groupId, controlId, Boolean.valueOf(buffer.ascii().toString()));
                                            break;
                                        }
                                    break;
                                }
                                device.dispatchToHost(notification, false);
                            }
                        }
                    } catch (Exception ex){
                        LOG.error("Can not work with/handle the following data '{}', '{}'. Reason: {}", path, buffer, ex.getMessage(), ex);
                    }
                }
            };
            run.run();
        }

        @Override
        public void onFailure(Throwable thrwbl) {
            LOG.error("An MQTT publish message failed: {} ({}:{})", thrwbl.getMessage(), config.get("IPADDRESS"), config.get("PORT"));
        }
        
    }
    
    /**
     * Actions to be taken for the connection listener.
     */
    class MQTTConnectListener implements Callback<Void> {

        @Override
        public void onSuccess(Object t) {

            String stringOk = new StringBuilder("Connected, with ")
                                        .append(config.get("IPADDRESS"))
                                        .append(":")
                                        .append(config.get("PORT"))
                                        .append(" at topic: ")
                                        .append(PUB_DEVICE_ROOT)
                                        .append("#")
                                        .toString();
            
            parent.LogError(stringOk);
            LOG.debug(stringOk);
            
            Topic[] topics = {new Topic(PUB_DEVICE_ROOT+"#", QoS.AT_MOST_ONCE),new Topic(PUB_RPC_ROOT+"#", QoS.AT_MOST_ONCE)};
            connection.subscribe(topics, new Callback<byte[]>() {
                public void onSuccess(byte[] qoses) {
                    
                    parent.LogError("Connected to " + config.get("IPADDRESS") +":"+ (config.get("PORT") +" at topic: " + PUB_DEVICE_ROOT +"/#"));
                    
                    LOG.trace("Subscribed: {}", new String(qoses));
                }
                @Override
                public void onFailure(Throwable value) {
                    parent.LogError("Not connected: " + value.getMessage());
                    LOG.trace("Not subscribed: {}", value.getMessage());
                }

                @Override
                public void onSuccess(Object o) {
                    LOG.trace("Subscribed: {}", o.toString());
                }
            });

        }

        @Override
        public void onFailure(Throwable thrwbl) {
            LOG.debug("Disconnected from or connect failure with: {}:{} - ", config.get("IPADDRESS"), config.get("PORT"), thrwbl.getMessage());
            parent.LogError("Disconnected from or connect failure with: "+config.get("IPADDRESS")+":"+config.get("PORT")+" - " + thrwbl.getMessage());
        }
        
    }
    
    /**
     * Actions to be taken for the connection listener.
     */
    class MQTTDisConnectListener implements Callback<Void> {

        @Override
        public void onSuccess(Object t) {
            LOG.debug("Disconnected from: {}:{}", config.get("IPADDRESS"), config.get("PORT"));
            parent.LogError("Disconnected from: " + config.get("IPADDRESS") +":"+ config.get("PORT"));
        }

        @Override
        public void onFailure(Throwable thrwbl) {
            /// disconnect should not fail
            LOG.error("Failed to disconnect from: {}:{}", config.get("IPADDRESS"), config.get("PORT"));
        }
        
    }
    
    private class MQTTTracer extends Tracer {
        @Override
        public void debug(String message, Object[] args) {
            LOG.debug("{}: {}", message, args);
        }
    }
    
}
