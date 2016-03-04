/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.weather;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.pidome.client.entities.plugins.weather.WeatherData;
import org.pidome.client.entities.plugins.weather.WeatherPlugin;
import org.pidome.client.entities.system.ServerTime;
import org.pidome.client.scenes.ScenesHandler;

/**
 *
 * @author John
 */
public class ForecastWeather extends AbstractWeatherPlane {

    private StackPane currentIcon = new StackPane();

    double[] highlightColor = {0, 0, .96};

    Text detailsDateLabel = new Text("When");
    Label detailsDateValue = new Label(": Unknown");

    Text detailsTempLabel = new Text("Temperature");
    Text detailsTempValue = new Text(": Unknown");

    Text detailsDescLabel = new Text("Description");
    Label detailsDescValue = new Label(": Unknown");

    Text detailsPressLabel = new Text("Pressure");
    Text detailsPressValue = new Text(": Unknown");

    Text detailsHumLabel = new Text("Humidity");
    Text detailsHumValue = new Text(": Unknown");

    Text detailsWindLabel = new Text("Wind");
    Text detailsWindValue = new Text(": Unknown");

    Text detailsWindDirLabel = new Text("Wind direction");
    Text detailsWindDirValue = new Text(": Unknown");

    protected ForecastWeather() {
        ///this.setGridLinesVisible(true);
        this.setPadding(new Insets(0,7,0,7));
        this.getStyleClass().add("weather-current");

        GridPane.setValignment(currentIcon, VPos.CENTER);
        GridPane.setHalignment(currentIcon, HPos.CENTER);
        this.add(currentIcon, 0, 1, 2, 1);

        //RowConstraints firstRow = new RowConstraints();
        //firstRow.setPercentHeight(40);
        
        //RowConstraints detailsRow = new RowConstraints();
        //this.getRowConstraints().addAll(firstRow, detailsRow);

        GridPane details = new GridPane();
        details.setMaxWidth(Double.MAX_VALUE);

        GridPane.setValignment(details, VPos.BOTTOM);
        
        GridPane.setValignment(detailsDateLabel, VPos.TOP);
        detailsDateLabel.getStyleClass().add("text");
        detailsTempLabel.getStyleClass().add("text");
        GridPane.setValignment(detailsDescLabel, VPos.TOP);
        detailsDescLabel.getStyleClass().add("text");
        detailsPressLabel.getStyleClass().add("text");
        detailsHumLabel.getStyleClass().add("text");
        detailsWindLabel.getStyleClass().add("text");
        detailsWindDirLabel.getStyleClass().add("text");

        detailsDateValue.getStyleClass().add("text");
        detailsDateValue.setWrapText(true);
        detailsTempValue.getStyleClass().add("text");
        detailsDescValue.getStyleClass().add("text");
        detailsDescValue.setWrapText(true);
        detailsPressValue.getStyleClass().add("text");
        detailsHumValue.getStyleClass().add("text");
        detailsWindValue.getStyleClass().add("text");
        detailsWindDirValue.getStyleClass().add("text");

        ColumnConstraints equalCols1 = new ColumnConstraints();
        equalCols1.setPercentWidth(40);
        
        this.getColumnConstraints().add(equalCols1);
        
        details.add(detailsDateLabel, 0, 1);
        details.add(detailsDateValue, 1, 1);

        details.add(detailsTempLabel, 0, 3);
        details.add(detailsTempValue, 1, 3);

        details.add(detailsDescLabel, 0, 4);
        details.add(detailsDescValue, 1, 4);

        details.add(detailsPressLabel, 0, 5);
        details.add(detailsPressValue, 1, 5);

        details.add(detailsHumLabel, 0, 6);
        details.add(detailsHumValue, 1, 6);

        details.add(detailsWindLabel, 0, 7);
        details.add(detailsWindValue, 1, 7);

        details.add(detailsWindDirLabel, 0, 8);
        details.add(detailsWindDirValue, 1, 8);

        GridPane.setHgrow(details, Priority.ALWAYS);
        this.add(details, 0, 2, 2, 1);

        updateHighlights();

    }

    private void updateHighlights() {
        setStyle("-fx-background-color: linear-gradient(transparent 50%, hsba(" + highlightColor[0] + ", " + highlightColor[1] * 100 + "%, " + highlightColor[2] * 100 + "%, 0.3) 100%);");
    }

    protected final void update(WeatherPlugin plugin, WeatherData data, double[] highlightColor, boolean daily) {
        this.highlightColor = highlightColor;
        Platform.runLater(() -> {
            this.getChildren().remove(currentIcon);
            double iconRatioBecauseItIsFixed = (1d / 1920d) * ScenesHandler.getContentWidthProperty().getValue();
            if(iconRatioBecauseItIsFixed < 0.478){
                iconRatioBecauseItIsFixed = 0.478;
            }
            currentIcon = getWeatherIcon(data.getIcon(), 210*iconRatioBecauseItIsFixed, 210*iconRatioBecauseItIsFixed, true);
            GridPane.setMargin(currentIcon, new Insets(10));
            GridPane.setHalignment(currentIcon, HPos.CENTER);
            this.add(currentIcon, 0, 1, 2, 1);

            if(daily){
                detailsDateValue.setText(new StringBuilder(": ").append(ServerTime.getLongDateFormat().format(data.getWeatherDate())).toString());
            } else {
                detailsDateValue.setText(new StringBuilder(": ").append(ServerTime.getTimeFormat().format(data.getWeatherDate())).toString());
            }
            detailsTempValue.setText(new StringBuilder(": ").append(data.getTemperature()).append("°C").toString());
            detailsDescValue.setText(new StringBuilder(": ").append(data.getDescription()).toString());
            detailsPressValue.setText(new StringBuilder(": ").append(data.getPressure()).toString());
            detailsHumValue.setText(new StringBuilder(": ").append(data.getHumidity()).toString());
            detailsWindValue.setText(new StringBuilder(": ").append(data.getWindSpeed()).toString());
            detailsWindDirValue.setText(new StringBuilder(": ").append(data.getWindDirection()).append(" (").append(data.getWindDirectionDegrees()).append(")").toString());

            updateHighlights();

        });
    }

}
