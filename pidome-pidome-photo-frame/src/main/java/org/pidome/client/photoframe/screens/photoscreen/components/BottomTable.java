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
import org.pidome.client.photoframe.screens.photoscreen.actors.TimeDateActor;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class BottomTable extends Table {
    
    PCCSystem system;
    
    BottomRightTable rightTable;
    BottomLeftTable leftTable;
    
    float height;
    
    boolean isActive = false;
    
    public BottomTable(PCCSystem system, float height){
        super();
        this.system = system;
        this.height = height;
        rightTable = new BottomRightTable(system);
        leftTable = new BottomLeftTable(system);
    }
    
    public final void preload(){
        
        leftTable.preload();
        if(leftTable.isActive()) isActive = true;
        add(leftTable).left().top().size(ScreenDisplay.getStageWidth()/3, height).expand();
        
        if(FrameSettings.showClock()){
            TimeDateActor timeDate = new TimeDateActor(system.getClient());
            timeDate.populate();
            timeDate.addAction(Actions.sequence(Actions.alpha(0),Actions.fadeIn(1f),Actions.delay(2)));
            this.add(timeDate).center().top().padTop(-10 * ScreenDisplay.getCurrentScale()).size(ScreenDisplay.getStageWidth()/3, height);
            isActive = true;
        }
        
        rightTable.preload();
        if(rightTable.isActive()) isActive = true;
        add(rightTable).right().top().size(ScreenDisplay.getStageWidth()/3, height);
    }
    
    public final boolean isActive(){
        return this.isActive;
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);
    }
    
}
