/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.photoframe.screens.photoscreen.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.plugins.weather.WeatherData;
import org.pidome.client.entities.plugins.weather.WeatherPluginException;
import org.pidome.client.photoframe.ScreenDisplay;
import org.pidome.client.photoframe.utils.ShadedLabel;
import org.pidome.client.photoframe.utils.TimeTempShadedLabel;
import org.pidome.client.system.PCCClient;

/**
 *
 * @author John
 */
public class WeatherActor extends Table {
    
    private final PCCClient client;
    
    private final TimeTempShadedLabel temp = new TimeTempShadedLabel(" °C");
    private final ShadedLabel cityName = new ShadedLabel("",0.7f);
    
    private final ShadedLabel description = new ShadedLabel("",0.5f);
    
    WeatherIcon weather;
    
    Table dataTable = new Table();
    
    private final PropertyChangeListener weatherHelper = this::weatherUpdateHelper;
    
    public WeatherActor(PCCClient client) {
        this.client = client;
        weather = new WeatherIcon();
        cityName.setWrap(true);
        description.setWrap(true);
    }
    
    /**
     * Helper to update weather data.
     * @param pce 
     */
    private void weatherUpdateHelper(PropertyChangeEvent pce){
        WeatherData data = (WeatherData)pce.getNewValue();
        temp.setText(data.getTemperature() + " °C");
        weather.setIcon(data.getIconImage());
        description.setText((String)data.getDescription());
        try {
            cityName.setText(this.client.getEntities().getWeatherPluginService().getWeather().getCityName());
        } catch (EntityNotAvailableException | WeatherPluginException ex) {
            Logger.getLogger(WeatherActor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Creates the table.
     */
    public final void populate(){
        try {
            this.client.getEntities().getWeatherPluginService().getWeather().getWeatherDataProperty().addPropertyChangeListener(weatherHelper);
            new Thread(() -> { 
                try {
                    this.client.getEntities().getWeatherPluginService().preload();
                } catch (EntityNotAvailableException ex) {
                    Logger.getLogger(TimeDateActor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }).start();
        } catch (EntityNotAvailableException | WeatherPluginException ex) {
            Logger.getLogger(WeatherActor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        add(weather).center().size(200 * ScreenDisplay.getCurrentScale(),200 * ScreenDisplay.getCurrentScale());
        
        dataTable.add(temp).left();
        dataTable.row();
        dataTable.add(description).left();
        
        add(dataTable).left().center().padLeft(20* ScreenDisplay.getCurrentScale());
    }

    private class WeatherIcon extends Image {
        
        TextureAtlas iconsPack = new TextureAtlas("resources/appimages/baseTextures.pack");
        
        String currentIcon = "-1.png";
        
        WeatherIcon(){
            setIcon(currentIcon);
        }
        
        private void setIcon(String name){
            if(!name.isEmpty()){
                String icon = name.substring(0, name.indexOf("."));
                Gdx.app.postRunnable(() -> {
                    TextureRegion newTexture = iconsPack.findRegion(new StringBuilder("weather/").append(icon).toString());
                    setDrawable(new SpriteDrawable(new Sprite(newTexture)));
                });
            }
        }
        
    }
 
}