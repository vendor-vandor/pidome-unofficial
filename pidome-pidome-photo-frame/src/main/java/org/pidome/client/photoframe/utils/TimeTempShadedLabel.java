/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.photoframe.utils;

/**
 *
 * @author John
 */
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TimeTempShadedLabel extends ShadedLabel {

    static private final BitmapFont TimeTempBitmap;
    
    static {
        TimeTempBitmap = new BitmapFont(Gdx.files.internal("resources/fonts/timetemp.fnt"), new TextureRegion(new Texture(Gdx.files.internal("resources/fonts/timetemp.png"))), false);
        TimeTempBitmap.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TimeTempBitmap.setColor(Color.WHITE);
    }
    
    private final ShadedLabel.DistanceFieldShader shader = new ShadedLabel.DistanceFieldShader();
    
    public TimeTempShadedLabel(CharSequence text, float scale) {
        super(text, scale, new LabelStyle(TimeTempShadedLabel.TimeTempBitmap, Color.WHITE));
        TimeTempShadedLabel.TimeTempBitmap.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TimeTempShadedLabel.TimeTempBitmap.setColor(Color.WHITE);
    }
    
    public TimeTempShadedLabel(CharSequence text) {
        super(text);
        TimeTempShadedLabel.TimeTempBitmap.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TimeTempShadedLabel.TimeTempBitmap.setColor(Color.WHITE);
    }
    
    @Override 
    public void draw(Batch batch, float alpha){
        batch.setShader(shader);
        super.draw(batch, alpha);
        shader.setSmoothing(1f / (4 * this.getFontScaleX()));
        batch.setShader(null);
    }
    
}