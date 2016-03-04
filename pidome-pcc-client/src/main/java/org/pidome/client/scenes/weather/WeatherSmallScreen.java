/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.weather;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.plugins.weather.WeatherData;
import org.pidome.client.entities.plugins.weather.WeatherPlugin;
import org.pidome.client.entities.plugins.weather.WeatherPluginException;
import org.pidome.client.scenes.ScenePaneImpl;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.scenes.devices.DevicesSmallScene;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.utilities.properties.ReadOnlyObjectPropertyBindingBean;

/**
 *
 * @author John
 */
public class WeatherSmallScreen extends StackPane implements ScenePaneImpl {

    private final CurrentWeatherPlane currentWeather = new CurrentWeatherPlane();
    
    ScrollPane mainContent = new ScrollPane();
    
    /**
     * The plugin holding the weather data.
     */
    private WeatherPlugin plugin;
    
    private ReadOnlyObjectPropertyBindingBean<WeatherData> data;
    
    PropertyChangeListener currentDataListener = this::currentDataListener;
    
    PCCSystem system;
    
    public WeatherSmallScreen(){
        this.getStyleClass().addAll("weather-scene", "weather-large-screen");
        this.setPadding(new Insets(10,0,0,0));
        buildWeatherPlane();
        mainContent.setHmax(0.1);
    }
    
    private void buildWeatherPlane(){
        currentWeather.setMaxWidth(ScenesHandler.getContentWidthProperty().getValue());
        mainContent.getStyleClass().add("list-view-root");
        mainContent.setContent(currentWeather);
        getChildren().add(mainContent);
    }
    
    @Override
    public String getTitle() {
        return "Weather";
    }

    @Override
    public void start() {
        try {
            plugin = this.system.getClient().getEntities().getWeatherPluginService().getWeather();
            data = plugin.getCurrentWeatherDataProperty();
            data.addPropertyChangeListener(currentDataListener);
            plugin.update();
            this.system.getClient().getEntities().getWeatherPluginService().preload();
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(DevicesSmallScene.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WeatherPluginException ex) {
            Logger.getLogger(WeatherLargeScreen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void currentDataListener(PropertyChangeEvent evt){
        currentWeather.update(plugin, (WeatherData)evt.getNewValue());
    }
    
    @Override
    public void close() {
        data.removePropertyChangeListener(currentDataListener);
    }

    @Override
    public Pane getPane() {
        return this;
    }

    @Override
    public void setSystem(PCCSystem system, ServiceConnector connector) {
        this.system = system;
    }

    @Override
    public void removeSystem() {
        system = null;
    }
    
}