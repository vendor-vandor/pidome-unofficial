/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.photoframe.screens.photoscreen.actors;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.photoframe.ScreenDisplay;

/**
 *
 * @author John
 */
public class PhotoImage extends Image {
    
    TextureRegionDrawable tex;
    
    public PhotoImage(TextureRegionDrawable tex){
        super(tex);
        this.tex = tex;
    }
    
    public void setCorrectAspectSize(){
        float width = ScreenDisplay.getStageWidth();
        float height = ScreenDisplay.getStageHeight();
        
        float imageWidth = this.getWidth();
        float imageHeight = this.getHeight();
        
        // calculate new sizes
        float scale = 1f;
        
        if(imageHeight > height){
            scale = (float)height/(float)imageHeight;
        } else if(imageWidth > width){
            scale = (float)width/(float)imageWidth;
        }

        float w = (float)imageWidth*scale;
        float h = (float)imageHeight*scale;
        
        this.setSize(w, h);
        
    }
    
    
    public void dispose(){
        try {
            this.tex.getRegion().getTexture().dispose();
        } catch(Exception ex){
            Logger.getLogger(PhotoImage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}