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
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import org.pidome.client.photoframe.ScreenDisplay;

public class ShadedLabel extends Label {

    static private final BitmapFont bitmap;
    
    static {
        bitmap = new BitmapFont(Gdx.files.internal("resources/fonts/freesans.fnt"), new TextureRegion(new Texture(Gdx.files.internal("resources/fonts/freesans.png"))), false);
        bitmap.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        bitmap.setColor(Color.WHITE);
    }

    private final DistanceFieldShader shader = new DistanceFieldShader();
    
    public static BitmapFont getBitmapFontForExternalResource(){
        return bitmap;
    }
    
    public ShadedLabel(CharSequence text) {
        this(text, 1.0f);
    }

    public ShadedLabel(CharSequence text, float fontScale) {
        this(text, fontScale, new LabelStyle(bitmap, Color.WHITE));
    }
    
    public ShadedLabel(CharSequence text, float fontScale, LabelStyle style) {
        super(text, style);
        setFontScale(fontScale * ScreenDisplay.getCurrentScale());
    }
    
    @Override 
    public void draw(Batch batch, float alpha){
        this.setPosition(this.getX(), this.getY()+(4*ScreenDisplay.getCurrentScale()));
        batch.setShader(shader);
        super.draw(batch, alpha);
        shader.setSmoothing(1f / (4 * this.getFontScaleX()));
        batch.setShader(null);
    }
    
    public static class DistanceFieldShader extends ShaderProgram {

        public DistanceFieldShader() {
            super(Gdx.files.internal("resources/fonts/largefont.vertex"), Gdx.files.internal("resources/fonts/largefont.fragment"));
            if (!isCompiled()) {
                throw new RuntimeException("Shader compilation failed:\n" + getLog());
            }
        }

        /**
         * @param smoothing a value between 0 and 1
         */
        protected void setSmoothing(float smoothing) {
            float delta = 0.5f * MathUtils.clamp(smoothing, 0, 1);
            setUniformf("u_lower", 0.5f - delta);
            setUniformf("u_upper", 0.5f + delta);
            ///setUniformf("u_outlineColor", 1f, 0f, 0f, 1f);
        }
    }
    
}
