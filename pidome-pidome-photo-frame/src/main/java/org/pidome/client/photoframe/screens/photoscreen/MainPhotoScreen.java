/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.photoframe.screens.photoscreen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.pidome.client.photoframe.FrameSettings;
import org.pidome.client.photoframe.RaspberryMirror;
import org.pidome.client.photoframe.ScreenDisplay;
import org.pidome.client.photoframe.screens.photoscreen.actors.PhotosActor;
import org.pidome.client.photoframe.screens.photoscreen.components.BottomTable;
import org.pidome.client.photoframe.screens.photoscreen.components.LeftTable;
import org.pidome.client.photoframe.screens.photoscreen.components.TopTable;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class MainPhotoScreen implements Screen, InputProcessor {
    
    private final PCCSystem system;
    
    SpriteBatch batch = new SpriteBatch();
    Stage stage;
    TopTable topTable;
    BottomTable bottomTable;
    LeftTable leftTable;
    PhotosActor photos;

    float topBarHeight;
    float bottomBarHeight;
    
    static org.lwjgl.input.Cursor emptyCursor;
    Texture cursor;
    int xHotspot, yHotspot;
    
    boolean cursorLive = false;
    
    InputMultiplexer multiplexer = new InputMultiplexer();
    
    public MainPhotoScreen(RaspberryMirror mainApp, PCCSystem system){
        super();
        this.system  = system;
        GLTexture.setEnforcePotImages(false);
        topBarHeight = 50 * ScreenDisplay.getCurrentScale();
        bottomBarHeight = ScreenDisplay.getStageHeight()/4;
        //// When dev ok uncomment next and put at appropiate places
        ///Gdx.graphics.setContinuousRendering(false);
        ///Stage.setActionsRequestRendering(true);
    }

    @Override
    public void render(float f) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(f);
        stage.draw();
        batch.begin();
            if(cursorLive){
                int x = Gdx.input.getX();
                int y = Gdx.input.getY();
                batch.draw(cursor, x - xHotspot, Gdx.graphics.getHeight() - y - 1 - yHotspot);
            }
        batch.end();
        if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
            if(system.getClient().isloggedIn()){
                system.getClient().logout();
            }
            Gdx.app.exit();
        }
    }

    @Override
    public void resize(int i, int i1) {
        stage.setViewport(i, i1, false);
    }

    @Override
    public void show() {
        multiplexer.addProcessor(this);
        stage = new Stage(ScreenDisplay.getStageWidth(), ScreenDisplay.getStageHeight(), true);
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);
        
        if(!FrameSettings.isMirrorMode()){
            photos = new PhotosActor(stage);
            photos.preload();
        }
        
        cursor = new Texture(Gdx.files.internal("resources/skins/cursor.png"));
        xHotspot = 0;
        yHotspot = cursor.getHeight();
        
        topTable = new TopTable(system);
        topTable.preload();
        
        if(topTable.isActive()){
            if(!FrameSettings.isMirrorMode()){
                Pixmap pm1 = new Pixmap(1, 1, Format.RGBA8888);
                pm1.setColor(0f, 0f, 0f, .5f);
                pm1.fill();
                topTable.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(pm1))));
            }
            topTable.setSize(ScreenDisplay.getStageWidth(),topBarHeight);
            topTable.setPosition(0, ScreenDisplay.getStageHeight() - topBarHeight);
            stage.addActor(topTable);
        }
        
        bottomTable = new BottomTable(system,bottomBarHeight);
        bottomTable.preload();
        
        if(bottomTable.isActive()){
            bottomTable.setSize(ScreenDisplay.getStageWidth(),bottomBarHeight);

            if(!FrameSettings.isMirrorMode()){
                
                Pixmap pm2 = new Pixmap(1, 1, Format.RGBA8888);
                pm2.setColor(0f, 0f, 0f, .7f);
                pm2.fill();
                bottomTable.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(pm2))));
                /*
                Texture tex = new Texture(Gdx.files.internal("resources/appimages/bars/bottom/background.png"));
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                bottomTable.setBackground(new TextureRegionDrawable(new TextureRegion(tex)));
                */
            }
            stage.addActor(bottomTable);
        }
        
        if(FrameSettings.getAutomationControlsEnabled()){
            leftTable = new LeftTable(system, stage);
            leftTable.preload();

            if(topTable.isActive()){
                leftTable.setHeight(ScreenDisplay.getStageHeight());
                leftTable.setPosition(0, 0);
                stage.addActor(leftTable);
            }
        }
    }

    @Override
    public void hide() {
        
    }

    @Override
    public void pause() {
        
    }

    @Override
    public void resume() {
        
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
    
    private static void setHWCursorVisible(boolean visible) throws LWJGLException {
        if (Gdx.app.getType() != Application.ApplicationType.Desktop && Gdx.app instanceof LwjglApplication) {
            return;
        }
        if (emptyCursor == null) {
            if (Mouse.isCreated()) {
                int min = org.lwjgl.input.Cursor.getMinCursorSize();
                IntBuffer tmp = BufferUtils.createIntBuffer(min * min);
                emptyCursor = new org.lwjgl.input.Cursor(min, min, min / 2, min / 2, 1, tmp, null);
            } else {
                throw new LWJGLException("Could not create empty cursor before Mouse object is created");
            }
        }
        if (Mouse.isInsideWindow()) {
            Mouse.setNativeCursor(visible ? null : emptyCursor);
        }
    }
    
    @Override
    public boolean mouseMoved(int i, int i1) {
        if(!cursorLive){
            try {
                setHWCursorVisible(true);
                cursorLive = true;
            } catch (LWJGLException ex) {
                Logger.getLogger(MainPhotoScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    @Override
    public boolean keyDown(int i) {
        if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
            if(system.getClient().isloggedIn()){
                system.getClient().logout();
            }
            Gdx.app.exit();
            return false;
        }
        return true;
    }

    @Override
    public boolean keyUp(int i) {
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean scrolled(int i) {
        return false;
    }
    
}