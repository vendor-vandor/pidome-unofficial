/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.photoframe.screens.photoscreen.actors;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.photoframe.ScreenDisplay;
import org.pidome.client.photoframe.utils.ShadedLabel;
import org.pidome.client.photoframe.utils.TimeTempShadedLabel;
import org.pidome.client.system.PCCClient;


/**
 *
 * @author John
 */
public class TimeDateActor extends Table {

    public boolean started = false;

    PCCClient client;    
    
    PropertyChangeListener DateHelper = this::dateUpdateHelper;
    PropertyChangeListener TimeHelper = this::timeUpdateHelper;
    
    ShadedLabel fontDate = new ShadedLabel("Waiting for server time update",.75f);
    TimeTempShadedLabel fontTime = new TimeTempShadedLabel("00:00", 3f * ScreenDisplay.getCurrentScale());
    
    float dateHeight = 0;
    float timeHeight = 0;
    
    ShaderProgram fontShader;
    
    public TimeDateActor(PCCClient client) {
        super();
        this.client = client;
    }

    public final void populate(){
        try {
            client.getEntities().getSystemService().getServerTime().getCurrentConcatenatedDatePeroperty().addPropertyChangeListener(DateHelper);
            client.getEntities().getSystemService().getServerTime().getCurrentTimeProperty().addPropertyChangeListener(TimeHelper);
            new Thread(() -> { 
                try {
                    client.getEntities().getSystemService().preload();
                } catch (EntityNotAvailableException ex) {
                    Logger.getLogger(TimeDateActor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }).start();
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(TimeDateActor.class.getName()).log(Level.SEVERE, null, ex);
        }
        add(fontDate).center().padTop(20 * ScreenDisplay.getCurrentScale());
        row();
        add(fontTime).center();

    }
    
    public void dateUpdateHelper(PropertyChangeEvent pce){
        fontDate.setText((String)pce.getNewValue());
    }
    
    public void timeUpdateHelper(PropertyChangeEvent pce){
        fontTime.setText((String)pce.getNewValue());
    }

}