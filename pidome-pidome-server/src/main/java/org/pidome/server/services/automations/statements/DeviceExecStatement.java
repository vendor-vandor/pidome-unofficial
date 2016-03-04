/*
 * Copyright 2014 John.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.server.services.automations.statements;

import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.connector.drivers.devices.UnknownDeviceException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlDataType;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroupException;
import org.pidome.server.services.hardware.DeviceService;

/**
 *
 * @author John
 */
public class DeviceExecStatement extends AutomationStatement {

    private int deviceId;
    private String group; 
    private String control;
    private Object command;
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(DeviceExecStatement.class);
    
    public DeviceExecStatement(int deviceId, String group, String control, Object command){
        super(new StringBuilder("Rundevice_").append(deviceId).append("_").append(group).append("_").append(control).toString());
        this.deviceId = deviceId;
        this.group    = group;
        this.control  = control;
        this.command  = command;
    }
    
    @Override
    public boolean run() {
        try {
            String batchName = new StringBuilder("rule_").append(new Date().getTime()).toString();
            DeviceControlDataType type = DeviceService.getDevice(deviceId).getFullCommandSet().getControlsGroup(group).getDeviceControl(control).getDataType();
            LOG.trace("Adding '{}' to batch {}: Device id:{}, group: {}, control: {}, command:{}",type, batchName,group,control,command);
            switch(type){
                case BOOLEAN:
                    try {
                        DeviceService.addBatch(deviceId, group, control, Boolean.valueOf(command.toString()), "", batchName);
                    } catch (Exception ex){
                        DeviceService.addBatch(deviceId, group, control, command, "", batchName);
                    }
                break;
                case STRING:
                    try {
                        DeviceService.addBatch(deviceId, group, control, command.toString(), "", batchName);
                    } catch (Exception ex){
                        DeviceService.addBatch(deviceId, group, control, command, "", batchName);
                    }
                break;
                case INTEGER:
                    try {
                        DeviceService.addBatch(deviceId, group, control, Integer.valueOf(command.toString()), "", batchName);
                    } catch (Exception ex){
                        DeviceService.addBatch(deviceId, group, control, command, "", batchName);
                    }
                break;
                case FLOAT:
                    try {
                        DeviceService.addBatch(deviceId, group, control, Float.valueOf(command.toString()), "", batchName);
                    } catch (Exception ex){
                        DeviceService.addBatch(deviceId, group, control, command, "", batchName);
                    }
                break;
                default:
                    /// Try to make the best of it.
                    DeviceService.addBatch(deviceId, group, control, command.toString(), "", batchName);
                break;
            }
            DeviceService.runBatch(batchName);
        } catch (UnknownDeviceException | DeviceControlsGroupException | DeviceControlException ex) {
            LOG.error("Could not determine device control type (yet): ", ex.getMessage());
        }
        return true;
    }

    @Override
    public void destroy() {
        /// Not used
    }
    
}
