/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.dashboard.DashboardItem;
import org.pidome.client.entities.plugins.weather.WeatherData;
import org.pidome.client.entities.plugins.weather.WeatherPlugin;
import org.pidome.client.entities.plugins.weather.WeatherPluginException;
import org.pidome.client.scenes.dashboard.svg.SVGBase;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public final class VisualDashboardWeatherItem extends VisualDashboardItem {

    private final PropertyChangeListener locationNameUpdater = this::locationNameUpdater;
    private final PropertyChangeListener weatherDataUpdater  = this::weatherDataUpdater;
    
    private final Text locationText = new Text("Weather");
    private final Text descriptionText = new Text("");
    private final Text tempText = new Text("No data");
    
    private final Text humidAmountText = new Text("");
    private final Text pressureAmountText = new Text("");
    private final Text windAmountText = new Text("");
    
    WeatherPlugin plugin;
    
    BorderPane container = new BorderPane();
    
    protected VisualDashboardWeatherItem(PCCSystem system, DashboardItem item) {
        super(system, item);
        this.getStyleClass().add("dashboard-weather");
        locationText.getStyleClass().add("locationtext");
        descriptionText.getStyleClass().add("descriptiontext");
        tempText.getStyleClass().add("temperature");
        
        Rectangle rect = new Rectangle(width, height);
        this.setClip(rect);
    }

    @Override
    protected void build() {
        try {
            plugin = getSystem().getClient().getEntities().getWeatherPluginService().getWeather();

            VBox top = new VBox();
            top.setAlignment(Pos.CENTER);
            descriptionText.setStyle("-fx-font-size: " + calcFontSize(10, false));
            top.getChildren().addAll(locationText, descriptionText);
            BorderPane.setAlignment(top, Pos.CENTER);
            container.setTop(top);

            BorderPane.setAlignment(tempText, Pos.CENTER);
            tempText.setStyle("-fx-font-size: " + calcFontSize(18, true));
            container.setCenter(tempText);

            GridPane detailGrid = new GridPane();
            BorderPane.setMargin(detailGrid, new Insets(0,0,0,3));
            detailGrid.setVgap(-2);
            detailGrid.add(new Text("Humidity"), 0, 0);detailGrid.add(new Text(": "), 1, 0);detailGrid.add(humidAmountText, 2, 0);
            detailGrid.add(new Text("Pressure"), 0, 1);detailGrid.add(new Text(": "), 1, 1);detailGrid.add(pressureAmountText, 2, 1);
            detailGrid.add(new Text("Wind"), 0, 2);detailGrid.add(new Text(": "), 1, 2);detailGrid.add(windAmountText, 2, 2);
            for (Node node:detailGrid.getChildren()){
                node.getStyleClass().add("detailstext");
            }
            container.setBottom(detailGrid);
            getChildren().add(container);
            
            plugin.getCityName().addPropertyChangeListener(locationNameUpdater);
            plugin.getCurrentWeatherDataProperty().addPropertyChangeListener(weatherDataUpdater);
            if((WeatherData)plugin.getCurrentWeatherDataProperty().getValue()!=null){
                setWeatherData((WeatherData)plugin.getCurrentWeatherDataProperty().getValue());
            } else {
                getSystem().getClient().getEntities().getWeatherPluginService().preload();
            }
            
        } catch (EntityNotAvailableException | WeatherPluginException ex){
            Logger.getLogger(VisualDashboardWeatherItem.class.getName()).log(Level.SEVERE, "Could not build weather", ex);
        }
    }

    private void locationNameUpdater(PropertyChangeEvent evt){
        Platform.runLater(() -> {
            locationText.setText((String)evt.getNewValue());
        });
    }
    
    private void weatherDataUpdater(PropertyChangeEvent evt){
        setWeatherData((WeatherData)evt.getNewValue());
    }
    
    private void setWeatherData(WeatherData data){
        Platform.runLater(() -> {
            locationText.setText(plugin.getCityName().getValueSafe());
            descriptionText.setText(data.getDescription());
            tempText.setText(new StringBuilder().append(data.getTemperature()).append("°C").toString());
            humidAmountText.setText(String.valueOf(data.getHumidity()));
            pressureAmountText.setText(String.valueOf(data.getPressure()));
            windAmountText.setText(new StringBuilder().append(data.getWindSpeed()).append(", ").append(data.getWindDirection()).toString());
        });
        setBackground(data.getIcon());
    }
    
    private void setBackground(WeatherData.WeatherIcon icon){
        ClassLoader loader = this.getClass().getClassLoader();
        try {
            Class<SVGBase> loadedClass = (Class<SVGBase>)loader.loadClass("org.pidome.client.scenes.dashboard.svg.weather.WeatherIcon_" + icon.toString());
            SVGBase iconBG = loadedClass.getConstructor().newInstance();
            Platform.runLater(() -> {
                this.removeBackground();
                this.setBackGround(iconBG);
            });
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(VisualDashboardWeatherItem.class.getName()).log(Level.SEVERE, "Can not set icon", ex);
        }
    }
    
    @Override
    protected void destruct() {
        plugin.getCityName().removePropertyChangeListener(locationNameUpdater);
        plugin.getCurrentWeatherDataProperty().removePropertyChangeListener(weatherDataUpdater);
    }
    
}