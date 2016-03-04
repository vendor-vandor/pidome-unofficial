package org.pidome.client.photoframe;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import org.pidome.client.photoframe.screens.loginloader.LoginPreloadProgressListener;
import org.pidome.client.photoframe.screens.loginloader.LoginPreloadProgressScreen;
import org.pidome.client.photoframe.screens.photoscreen.MainPhotoScreen;
import org.pidome.client.system.PCCSystem;

public class RaspberryMirror extends Game implements InputProcessor,LoginPreloadProgressListener {

    /**
     * The PCC system.
     */
    private final PCCSystem system;
    
    AssetManager assetManager = new AssetManager();
    
    public RaspberryMirror(PCCSystem system){
        super();
        this.system = system;
    }

    /**
     * Returns the asset manager.
     * @return 
     */
    public final AssetManager getAssetManager(){
        return assetManager;
    }
    
    @Override
    public void create() {
        Gdx.input.setInputProcessor(this);
        ScreenDisplay.setDisplayData(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        setScreen(new LoginPreloadProgressScreen(this,this.system));
    }


    @Override
    public void preleadDone() {
        setScreen(new MainPhotoScreen(this,this.system));
    }
    
    @Override
    public boolean keyDown(int i) {
        if(Gdx.input.isKeyPressed(Keys.ESCAPE)){
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
        return true;
    }

    @Override
    public boolean keyTyped(char c) {
        return true;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        return true;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return true;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return true;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return true;
    }

    @Override
    public boolean scrolled(int i) {
        return true;
    }

}