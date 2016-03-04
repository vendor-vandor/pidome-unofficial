/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.dashboard.DashboardItem;
import org.pidome.client.entities.system.ServerTime;
import org.pidome.client.scenes.dashboard.svg.time.SunriseBG;
import org.pidome.client.scenes.dashboard.svg.time.SunsetBG;
import org.pidome.client.scenes.dashboard.svg.time.TimeBG;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
final class VisualDashboardTimeItem extends VisualDashboardItem {

    private ServerTime serverTime;
    
    private final PropertyChangeListener dateListener = this::updateDate;
    private final PropertyChangeListener timeListener = this::updateTime;
    private final PropertyChangeListener sunriseListener = this::updateSunrise;
    private final PropertyChangeListener sunsetListener = this::updateSunset;
    private final Text dateText = new Text("");
    private final Text timeText = new Text("");
    
    private final BorderPane sunsetBox = new BorderPane();
    private final Text sunRiseText  = new Text("rise");
    private final Text sunSetText  = new Text("set");
    
    protected VisualDashboardTimeItem(PCCSystem system, DashboardItem item) {
        super(system, item);
        getStyleClass().add("dashboard-time");
        dateText.getStyleClass().add("date");
        BorderPane.setAlignment(dateText, Pos.CENTER);
        
        timeText.setStyle("-fx-font-size: " + calcFontSize(22, true));
        timeText.getStyleClass().add("time");
        
        sunRiseText.getStyleClass().add("sunriseset");
        sunSetText.getStyleClass().add("sunriseset");
        
        SunriseBG sunriseIcon = new SunriseBG();
        sunriseIcon.build(20, 20);
        sunriseIcon.getStyleClass().add("sunriseset-icon");
        SunsetBG sunsetIcon = new SunsetBG();
        sunsetIcon.build(20, 20);
        sunsetIcon.getStyleClass().add("sunriseset-icon");
        
        HBox riseBox = new HBox(5);
        riseBox.getChildren().addAll(sunriseIcon.getSVG(),sunRiseText);
        
        HBox setBox = new HBox(5);
        setBox.setAlignment(Pos.CENTER_RIGHT);
        setBox.getChildren().addAll(sunSetText, sunsetIcon.getSVG());

        BorderPane.setMargin(riseBox, new Insets(0,0,3,5));
        BorderPane.setMargin(setBox, new Insets(0,5,3,0));
        
        sunsetBox.setLeft(riseBox);
        sunsetBox.setRight(setBox);
        
        setBackGround(new TimeBG());
    }

    @Override
    protected void build() {
        try {
            serverTime = getSystem().getClient().getEntities().getSystemService().getServerTime();
            
            dateText.setText("");
            timeText.setText("Waiting");
            
            updateAll((this.getDashboardItem().getSizeX()>2)?serverTime.getCurrentConcatenatedDatePeroperty().getValue():serverTime.getCurrentConcatenatedDatePeropertyShort().getValue(), 
                      serverTime.getCurrentTimeProperty().getValue(), 
                      serverTime.getCurrentSunriseProperty().getValue(), 
                      serverTime.getCurrentSunsetProperty().getValue());
            
            if(this.getDashboardItem().getSizeX()>2){
                serverTime.getCurrentConcatenatedDatePeroperty().addPropertyChangeListener(dateListener);
            } else {
                serverTime.getCurrentConcatenatedDatePeropertyShort().addPropertyChangeListener(dateListener);
            }
            serverTime.getCurrentTimeProperty().addPropertyChangeListener(timeListener);
            serverTime.getCurrentSunriseProperty().addPropertyChangeListener(sunriseListener);
            serverTime.getCurrentSunsetProperty().addPropertyChangeListener(sunsetListener);
            
            getSystem().getClient().getEntities().getSystemService().preload();
            
            BorderPane container = new BorderPane();
            container.setTop(dateText);
            container.setCenter(timeText);
            container.setBottom(sunsetBox);
            
            setContent(container);
            
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(VisualDashboardTimeItem.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    private void updateAll(String date, String time, String rise, String set){
        Platform.runLater(() -> {
            dateText.setText(date);
            timeText.setText(time);
            sunRiseText.setText(rise);
            sunSetText.setText(set);
        });
    }
    
    private void updateDate(PropertyChangeEvent evt){
        Platform.runLater(() -> {
            dateText.setText((String)evt.getNewValue());
        });
    }
    
    private void updateTime(PropertyChangeEvent evt){
        Platform.runLater(() -> {
            timeText.setText((String)evt.getNewValue());
        });
    }
    
    private void updateSunrise(PropertyChangeEvent evt){
        Platform.runLater(() -> {
            sunRiseText.setText((String)evt.getNewValue());
        });
    }
    
    private void updateSunset(PropertyChangeEvent evt){
        Platform.runLater(() -> {
            sunSetText.setText((String)evt.getNewValue());
        });
    }
    
    @Override
    protected void destruct() {
        if(serverTime!=null){
            if(this.getDashboardItem().getSizeX()>2){
                serverTime.getCurrentConcatenatedDatePeroperty().removePropertyChangeListener(dateListener);
            } else {
                serverTime.getCurrentConcatenatedDatePeropertyShort().removePropertyChangeListener(dateListener);
            }
            serverTime.getCurrentTimeProperty().removePropertyChangeListener(timeListener);
            serverTime.getCurrentSunriseProperty().removePropertyChangeListener(sunriseListener);
            serverTime.getCurrentSunsetProperty().removePropertyChangeListener(sunsetListener);
        }
    }
    
}
