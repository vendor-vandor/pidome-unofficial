/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.photoframe.screens.photoscreen.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.macros.Macro;
import org.pidome.client.entities.macros.MacroService;
import org.pidome.client.entities.macros.MacroServiceException;
import org.pidome.client.photoframe.ScreenDisplay;
import org.pidome.client.photoframe.utils.ShadedLabel;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public class LeftTable extends Table implements InputProcessor {
 
    PCCSystem system;
    
    boolean isActive = false;
    
    float fontSize = 0.8f;
    
    Table container = new Table();
    Table macrosTable= new Table();
    ScrollPane scroll = new ScrollPane(macrosTable);
    
    boolean hasMacros = false;
    
    float containersHeight;
    float containersWidth;
    
    float containerTopPosition;
    float containerLeftPosition;
    
    ShadedLabel containerLabel = new ShadedLabel("Action title");
    
    boolean containerIsOpen = false;
    
    /**
     * Needed to add window (like) actors.
     */
    Stage stage;
    
    public LeftTable(PCCSystem system, Stage stage){
        super();
        this.system = system;
        this.stage = stage;
        containersWidth = (ScreenDisplay.getStageWidth()/5f)*3.5f;
        containersHeight = (ScreenDisplay.getStageHeight()/4f)*2f;
        
        containerLeftPosition = ScreenDisplay.getStageCenterX() - (containersWidth/2f);
        containerTopPosition = (ScreenDisplay.getStageHeight()/4)*1.5f;
        try {
            this.system.getClient().getEntities().getMacroService().preload();
            hasMacros = true;
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(LeftTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public final void preload(){
        createMacrosButton();
        row();
        createScenesButton();
        pack();
        createContainerTable();
    }
    
    public final boolean isActive(){
        return this.isActive;
    }
    
    private void createMacrosButton(){
        Texture tex = new Texture(Gdx.files.internal("resources/appimages/bars/bottom/macro.png"));
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TextureRegion texture = new TextureRegion(tex);
        Image icon = new Image(new SpriteDrawable(new Sprite(texture)));
        add(icon).left().size(80 * ScreenDisplay.getCurrentScale(),80 * ScreenDisplay.getCurrentScale()).pad(20 * ScreenDisplay.getCurrentScale());
        icon.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMacroTable();
            }
        });
    }
    
    private void createScenesButton(){
        Texture tex = new Texture(Gdx.files.internal("resources/appimages/bars/bottom/scene.png"));
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TextureRegion texture = new TextureRegion(tex);
        Image icon = new Image(new SpriteDrawable(new Sprite(texture)));
        add(icon).left().size(80 * ScreenDisplay.getCurrentScale(),80 * ScreenDisplay.getCurrentScale()).pad(20 * ScreenDisplay.getCurrentScale());
        
        icon.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showScenesTable();
            }
        });
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);
    }
    
    private void createContainerTable(){
        container.setSize(containersWidth, containersHeight);
        container.add(containerLabel).left().top().expandY().padLeft(10 * ScreenDisplay.getCurrentScale());
        scroll.setScrollingDisabled(true, false);
        scroll.setScrollBarPositions(false, true);
        container.add(scroll).left().top().expandX().fillX().pad(20 * ScreenDisplay.getCurrentScale());
        ShadedLabel closeLabel = new ShadedLabel("X");
        closeLabel.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeContainer();
            }
        });
        container.add(closeLabel).right().top().padRight(10 * ScreenDisplay.getCurrentScale());
        Pixmap pm1 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm1.setColor(0f, 0f, 0f, .7f);
        pm1.fill();
        container.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(pm1))));
        container.setPosition(containerLeftPosition, containerTopPosition);
    }
    
    private void closeContainer(){
        if(containerIsOpen){
            container.remove();
            containerIsOpen = false;
        }
    }
    
    private void showMacroTable(){
        Runnable run = () -> {
            try {
                containerLabel.setText("Run macro");
                macrosTable.clearChildren();
                if(hasMacros){
                    MacroService macroService = this.system.getClient().getEntities().getMacroService();
                    ReadOnlyObservableArrayListBean<Macro> macros = macroService.getMacroList();
                    for(Macro macro:macros.subList(0, macros.size()-1)){
                        if(macro.getIsFavorite()){
                            ShadedLabel label = new ShadedLabel(macro.getName(), .7f);
                            label.addListener(new ClickListener(){
                                @Override
                                public void clicked(InputEvent event, float x, float y) {
                                    Runnable run = () -> { macroService.runMacro(macro.getMacroId()); };
                                    run.run();
                                    closeContainer();
                                }
                            });
                            macrosTable.row();
                            macrosTable.add(label).left().top();
                        }
                    }
                } else {
                    ShadedLabel label = new ShadedLabel("There are no macros available", .7f);
                    macrosTable.row();
                    macrosTable.add(label).left().top();
                }
                this.stage.addActor(container);
            } catch (EntityNotAvailableException | MacroServiceException ex) {
                Logger.getLogger(LeftTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        openContainer(run);
    }
   
    private void showScenesTable(){
        Runnable run = () -> {
            containerLabel.setText("Set scene");
            macrosTable.clearChildren();
            ShadedLabel label = new ShadedLabel("Scenes are not yet supported", .7f);
            macrosTable.row();
            macrosTable.add(label).left().top();
            this.stage.addActor(container);
        };
        openContainer(run);
    }
    
    private void openContainer(Runnable runContainerOpen){
        closeContainer();
        containerIsOpen = true;
        runContainerOpen.run();
    }
    
    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean keyDown(int i) {
        return false;
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
