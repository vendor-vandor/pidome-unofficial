/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.weather;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.plugins.weather.WeatherData;
import org.pidome.client.entities.plugins.weather.WeatherPlugin;
import org.pidome.client.entities.plugins.weather.WeatherPluginException;
import org.pidome.client.scenes.ScenePaneImpl;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.scenes.devices.DevicesSmallScene;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;
import org.pidome.client.tools.ColorTools;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;
import org.pidome.pcl.utilities.properties.ReadOnlyObjectPropertyBindingBean;

/**
 *
 * @author John
 */
public class WeatherLargeScreen extends GridPane implements ScenePaneImpl {

    static {
        Logger.getLogger(WeatherLargeScreen.class.getName()).setLevel(Level.ALL);
    }
    
    private final CurrentWeatherPlane currentWeather = new CurrentWeatherPlane();
    
    private final ForecastContainer forecastNearContainer = new ForecastContainer("Forecast", false);
    private final ForecastContainer forecastFarContainer = new ForecastContainer("3 Days forecast", true);
    
    /**
     * The plugin holding the weather data.
     */
    private WeatherPlugin plugin;
    
    private ReadOnlyObjectPropertyBindingBean<WeatherData> data;
    
    PropertyChangeListener currentDataListener = this::currentDataListener;
    
    private ObservableArrayListBeanChangeListener<WeatherData> weatherNearMutator = forecastNearContainer::updateData;
    private ObservableArrayListBeanChangeListener<WeatherData> weatherFarMutator = forecastFarContainer::updateData;
    
    PCCSystem system;
    
    public WeatherLargeScreen(){
        this.getStyleClass().addAll("weather-scene", "weather-large-screen");
        ///this.setPadding(new Insets(20));
        this.setMaxHeight(ScenesHandler.getContentHeightProperty().getValue());
    }
    
    @Override
    public String getTitle() {
        return "Weather";
    }

    @Override
    public void start() {
        try {
            plugin = this.system.getClient().getEntities().getWeatherPluginService().getWeather();
            buildWeatherPlane();
            data = plugin.getCurrentWeatherDataProperty();
            data.addPropertyChangeListener(currentDataListener);
            plugin.getUpcomingWeatherData().addListener(weatherNearMutator);
            plugin.getUpcomingDaysWeatherData().addListener(weatherFarMutator);
            if(plugin.getCapabilities().contains(WeatherPlugin.Capabilities.THREEDAY_FORECAST) || plugin.getCapabilities().contains(WeatherPlugin.Capabilities.THREEHOURS_FORECAST)){
                /// First column for current weather
                ColumnConstraints firstCol = new ColumnConstraints();
                firstCol.setPercentWidth(34);
                ColumnConstraints secondCol = new ColumnConstraints();
                secondCol.setPercentWidth(66);
                this.getColumnConstraints().addAll(firstCol, secondCol);
                GridPane.setVgrow(currentWeather, Priority.ALWAYS);
                GridPane.setVgrow(forecastNearContainer, Priority.ALWAYS);
                GridPane.setVgrow(forecastFarContainer, Priority.ALWAYS);
            }
            plugin.update();
            this.system.getClient().getEntities().getWeatherPluginService().preload();
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(DevicesSmallScene.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WeatherPluginException ex) {
            Logger.getLogger(WeatherLargeScreen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void buildWeatherPlane(){
        add(currentWeather, 0, 0, 1, 3);
        
        if(plugin.getCapabilities().contains(WeatherPlugin.Capabilities.UPCOMING_FORECAST)){
            add(forecastNearContainer, 1,0);
            if(plugin.getCapabilities().contains(WeatherPlugin.Capabilities.THREEDAY_FORECAST)){
                add(forecastFarContainer, 1,1);
            }
        } else if(plugin.getCapabilities().contains(WeatherPlugin.Capabilities.THREEDAY_FORECAST)){
            add(forecastFarContainer, 1,0);
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
    
    private class ForecastContainer extends GridPane {
        
        private GridPane curentPane = new GridPane();
        
        Label threeForecast;
        
        boolean daily = false;
        
        private ForecastContainer(String title, boolean daily){
            this.daily = daily;
            GridPane.setFillHeight(this, Boolean.TRUE);
            
            threeForecast = new Label(title);
            threeForecast.setStyle("-fx-border-color: transparent transparent hsb(0, 0%, 96%) transparent;");
            threeForecast.getStyleClass().addAll("text", "section-title");
            threeForecast.setMaxWidth(Double.MAX_VALUE);
            
            GridPane.setMargin(threeForecast, new Insets(0,7,0,7));
            
            ColumnConstraints equalCols1 = new ColumnConstraints();
            equalCols1.setPercentWidth(33);
            ColumnConstraints equalCols2 = new ColumnConstraints();
            equalCols2.setPercentWidth(33);
            ColumnConstraints equalCols3 = new ColumnConstraints();
            equalCols3.setPercentWidth(33);
            
            this.getColumnConstraints().addAll(equalCols1, equalCols2, equalCols3);
            
            ForecastWeather[] weatherList = new ForecastWeather[3];
            
            weatherList[0] = new ForecastWeather();
            GridPane.setVgrow(weatherList[0], Priority.ALWAYS);
            GridPane.setMargin(weatherList[0], new Insets(0,5,0,10));
            
            weatherList[1] = new ForecastWeather();
            GridPane.setVgrow(weatherList[1], Priority.ALWAYS);
            GridPane.setMargin(weatherList[1], new Insets(0,5,0,5));
            
            weatherList[2] = new ForecastWeather();
            GridPane.setVgrow(weatherList[2], Priority.ALWAYS);
            GridPane.setMargin(weatherList[2], new Insets(0,10,0,5));
            
            Platform.runLater( () -> { 
                this.add(threeForecast, 0, 0, 3, 1);
                this.addRow(1, weatherList);
            } );
            
        }
        
        private void updateData(ObservableArrayListBeanChangeListener.Change<? extends WeatherData> change){
            if(change.wasAdded()){
                final Object[] data = change.getAddedSubList().toArray();
                List<ForecastWeather> weather = new ArrayList<>();
                for(Node node:this.getChildren()){
                    if(node instanceof ForecastWeather){
                        weather.add((ForecastWeather)node);
                    }
                }
                StringBuilder colorSet = new StringBuilder("-fx-border-color: transparent transparent linear-gradient(to right, ");
                Platform.runLater( () -> {
                    for(int counter = 0; counter < 3; counter++){
                        double[] highlightColor = ColorTools.tempToHsbPureInverted(((WeatherData)data[counter]).getTemperature());
                        colorSet.append("hsb(").append(highlightColor[0]).append(", ").append(highlightColor[1] * 100).append("%, ").append(highlightColor[2] * 100).append("%) ").append((counter+1)*25).append("%, ");
                        weather.get(counter).update(plugin, (WeatherData)data[counter], highlightColor, daily);
                    }
                    threeForecast.setStyle(colorSet.delete(colorSet.length()-2, colorSet.length()).append(") transparent").toString());
                });
            }
        }
        
    }
    
}