/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.photoframe.screens.photoscreen.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import org.pidome.client.photoframe.FrameSettings;
import org.pidome.client.photoframe.ScreenDisplay;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class TopTable extends Table {
    
    PCCSystem system;
    
    boolean isActive = false;
    
    public TopTable(PCCSystem system){
        super();
        this.system = system;
    }
    
    public final void preload (){
        if(FrameSettings.showLogo()){
            Texture tex = new Texture(Gdx.files.internal("resources/appimages/logoimages/full-white-very-small.png"));
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            Image splashImage = new Image(tex);
            add(splashImage).center().expandX().left().padLeft(10).size(181 * ScreenDisplay.getCurrentScale(), 40 * ScreenDisplay.getCurrentScale());
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
