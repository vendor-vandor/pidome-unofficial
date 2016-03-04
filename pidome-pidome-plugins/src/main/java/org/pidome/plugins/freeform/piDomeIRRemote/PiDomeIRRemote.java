/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.freeform.piDomeIRRemote;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.plugins.pidomeremote.DefaultRemoteButton;
import org.pidome.server.connector.plugins.pidomeremote.PiDomeRemote;
import org.pidome.server.connector.plugins.pidomeremote.PiDomeRemoteButtonException;
import org.pidome.server.connector.plugins.pidomeremote.RemoteButton;
import org.pidome.server.connector.plugins.pidomeremote.UniversalRemoteButton;

/**
 *
 * @author John
 */
public class PiDomeIRRemote extends PiDomeRemote {
    
    static Logger LOG = LogManager.getLogger(PiDomeIRRemote.class);
    
    public PiDomeIRRemote(){}
    
    @Override
    public void handleDeviceData(Device device, String group, String control, DeviceControl deviceControl, Object deviceValue) {
        
    }

    
    /**
     * Prepares for deletion.
     */
    @Override
    public void prepareDelete() {
        /// Not used
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public void createButton(String id, String type, boolean isUniversal, int row, int column, String signal, long delay) {
        LOG.debug("Creating button with id: {}, type: {}, universal: {}, row: {}, column: {}, signal: {}, delay: {}", id,type,isUniversal,row,column,signal,delay);
        RemoteButton button;
        if(isUniversal){
            button = new UniversalRemoteButton(id);
        } else {
            button = new DefaultRemoteButton(id);
            ((DefaultRemoteButton)button).setButtonAction(signal);
        }
        button.setButtonLocation(row, column);
        button.setButtonDelay(delay);
        button.setType(type);
        setButtonPlugin(button);
        this.getButtons().add(button);
    }

    /**
     * Creates a button set.
     * @param remoteData 
     */
    @Override
    public void createButtonsSet(Map<String,Object> remoteData) {
        this.getButtons().clear();
        ArrayList<Map<String,Object>> sectionList = (ArrayList<Map<String,Object>>)remoteData.get("sections");
        sectionList.stream().forEach((sectionData) -> {
            Map<String,Object> dataSingleSection = (Map<String,Object>)sectionData.get("section");
            ArrayList<Map<String,Object>> rows = (ArrayList<Map<String,Object>>)dataSingleSection.get("rows");
            rows.stream().forEach((rowData) -> {
                Map<String,Object> dataSingleRow = (Map<String,Object>)rowData.get("row");
                int rowId = ((Long)dataSingleRow.get("id")).intValue();
                ArrayList<Map<String,Object>> buttons = (ArrayList<Map<String,Object>>)dataSingleRow.get("buttons");
                buttons.stream().forEach((button) -> {
                    long pause = 500;
                    if(button.containsKey("pause")){
                        try {
                            pause = (long)(Float.parseFloat((String)button.get("pause")) * 1000);
                        } catch (Exception ex){}
                    }
                    createButton((String)button.get("id"), (String)button.get("cat"), false, rowId, ((Long)button.get("pos")).intValue(), (String)button.get("signal"), pause);
                });
            });
        });
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public void setUniversalButtonButtons(String buttonId, ArrayList<RemoteButton> buttonList) {
        try {
            RemoteButton button = getButton(buttonId);
            if(button.getIsUniversal()){
                ((UniversalRemoteButton)button).setButtonActionList(buttonList);
            }
        } catch (PiDomeRemoteButtonException ex) {
            LOG.error("Button id {} does not exist, can not assign other remotes buttons");
        }
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public final void deleteButton(String id) {
        try {
            RemoteButton button = getButton(id);
            this.getButtons().remove(button);
        } catch (PiDomeRemoteButtonException ex) {
            LOG.error("Button id {} does not exist", id);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public final RemoteButton getButton(String id) throws PiDomeRemoteButtonException {
        List<RemoteButton> buttons = this.getButtons();
        for(RemoteButton button:buttons){
            if(button.getId()!=null && button.getId().equals(id)){
                return button;
            }
        }
        throw new PiDomeRemoteButtonException("Button id " + id + " does not exist");
    }

    /**
     * @inheritDoc
     */
    @Override
    public void handleButton(String id) throws PiDomeRemoteButtonException {
        LOG.debug("Handling button id {} in {}", id, this.getFriendlyName());
        RemoteButton button = getButton(id);
        handleButtonBatchExecution(createButtonActions(button));
    }

    @Override
    public void prepareWebPresentation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasGraphData() {
        return false;
    }

}