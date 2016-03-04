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
import org.pidome.client.photoframe.screens.photoscreen.actors.WeatherActor;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class BottomRightTable extends Table {
 
    PCCSystem system;
    
    boolean isActive = false;
    
    public BottomRightTable(PCCSystem system){
        super();
        this.system = system;
    }
    
    protected final void preload(){
        
        if(FrameSettings.showWeather()){
            WeatherActor weatherActor = new WeatherActor(system.getClient());
            weatherActor.populate();
            weatherActor.addAction(Actions.sequence(Actions.alpha(0),Actions.fadeIn(1f),Actions.delay(2)));
            add(weatherActor).center();
            isActive = true;
        }
                
    }
    
    public final boolean isActive(){
        return this.isActive;
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);
    }
    
}
