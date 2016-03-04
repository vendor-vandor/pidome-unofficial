/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.weather;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.CacheHint;
import javafx.scene.layout.GridPane;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.pidome.client.entities.plugins.weather.WeatherData;
import org.pidome.client.scenes.dashboard.VisualDashboardWeatherItem;
import org.pidome.client.scenes.dashboard.svg.SVGBase;

/**
 *
 * @author John
 */
public abstract class AbstractWeatherPlane extends GridPane {

    
    public final StackPane getWeatherIcon(WeatherData.WeatherIcon icon, double width, double height, boolean smaller) {
        if(smaller){
            width = width * 0.7;
            height = height * 0.7;
        }
        StackPane item;
        try {
            Class<SVGBase> loadedClass = (Class<SVGBase>)WeatherLargeScreen.class.getClassLoader().loadClass("org.pidome.client.scenes.dashboard.svg.weather.WeatherIcon_" + icon.toString());
            SVGBase background = loadedClass.getConstructor().newInstance();
            background.build(width, height);
            Platform.runLater(() -> { background.updateOpacity(1.0); background.updateFill(Color.web("#F5F5F5")); });
            item = background;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(VisualDashboardWeatherItem.class.getName()).log(Level.SEVERE, null, ex);
            item = new StackPane();
        }
        item.setPrefSize(width, height);
        item.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        item.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        return item;
    }
    
}