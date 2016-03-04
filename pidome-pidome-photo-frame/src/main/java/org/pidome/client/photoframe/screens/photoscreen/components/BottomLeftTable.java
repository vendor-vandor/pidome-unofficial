/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.photoframe.screens.photoscreen.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import org.pidome.client.photoframe.FrameSettings;
import org.pidome.client.photoframe.ScreenDisplay;
import org.pidome.client.photoframe.screens.photoscreen.actors.RoomTemperatureActor;
import org.pidome.client.photoframe.screens.photoscreen.actors.UserStatusesActor;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class BottomLeftTable extends Table {
 
    PCCSystem system;
    
    boolean isActive = false;
    
    float fontSize = 0.8f;
    
    Table container;
    
    public BottomLeftTable(PCCSystem system){
        super();
        this.system = system;
    }
    
    protected final void preload(){
        if(FrameSettings.getRoomDeviceEnabled()){
            RoomTemperatureActor roomtemp = new RoomTemperatureActor(system.getClient());
            roomtemp.populate();
            roomtemp.addAction(Actions.sequence(Actions.alpha(0),Actions.fadeIn(1f),Actions.delay(2)));
            add(roomtemp).left().top().expandX().padLeft(20 * ScreenDisplay.getCurrentScale());
            row();
            isActive = true;
        }
        if(FrameSettings.getGlobalUserStatusEnabled()){
            add(new UserStatusesActor(system.getClient())).left().top().expand().padLeft(20 * ScreenDisplay.getCurrentScale());
        }
    }
    
    public final boolean isActive(){
        return this.isActive;
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);
        if(container!=null){
            container.draw(batch, parentAlpha);
        }
    }
    
}
