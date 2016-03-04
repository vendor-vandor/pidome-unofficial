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
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.pidome.client.entities.plugins.weather.WeatherData;
import org.pidome.client.entities.plugins.weather.WeatherPlugin;
import org.pidome.client.entities.system.ServerTime;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.tools.ColorTools;

/**
 *
 * @author John
 */
class CurrentWeatherPlane extends AbstractWeatherPlane {

    private final Label mainTemp = new Label();
    private final Label mainCityName = new Label();
    private final Label mainDesc = new Label();
    private final Label provider = new Label();

    private StackPane currentIcon = new StackPane();

    double[] highlightColor = {0, 0, .96};

    Label currentText = new Label("Current");
    Label detailsHeader = new Label("Current details");

    Text detailsDateLabel = new Text("Updated");
    Label detailsDateValue = new Label(": Unknown");

    Text detailsCityLabel = new Text("City");
    Label detailsCityValue = new Label(": Unknown");

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

    private boolean drawGradient;
    
    protected CurrentWeatherPlane(){
        this(true);
    }
    
    protected CurrentWeatherPlane(boolean drawGradient) {
        this.drawGradient = drawGradient;
        ///this.setGridLinesVisible(true);
        this.setPadding(new Insets(0,7,0,7));
        this.getStyleClass().add("weather-current");

        ColumnConstraints firstCol = new ColumnConstraints();
        firstCol.setPercentWidth(50);
        firstCol.setHalignment(HPos.CENTER);

        ColumnConstraints secondCol = new ColumnConstraints();
        secondCol.setHgrow(Priority.ALWAYS);
        this.getColumnConstraints().addAll(firstCol, secondCol);

        mainTemp.getStyleClass().addAll("text", "temp-title");
        mainCityName.getStyleClass().add("city-title");
        mainCityName.setWrapText(true);

        mainDesc.getStyleClass().add("description-title");
        mainDesc.setWrapText(true);

        provider.getStyleClass().add("provider-title");

        currentText.getStyleClass().addAll("text", "section-title");
        currentText.setMaxWidth(Double.MAX_VALUE);

        RowConstraints firstRow = new RowConstraints();
        this.add(currentText, 0, 0, 2, 1);

        GridPane.setValignment(currentIcon, VPos.CENTER);
        GridPane.setHalignment(currentIcon, HPos.CENTER);
        this.add(currentIcon, 0, 1, 1, 3);

        RowConstraints secondRow = new RowConstraints();
        secondRow.setPercentHeight(8);
        GridPane.setValignment(mainTemp, VPos.BOTTOM);
        this.add(mainTemp, 1, 1);

        RowConstraints thirdRow = new RowConstraints();
        thirdRow.setPercentHeight(8);
        thirdRow.setValignment(VPos.CENTER);
        this.add(mainDesc, 1, 2);

        RowConstraints fourthRow = new RowConstraints();
        fourthRow.setPercentHeight(8);
        GridPane.setValignment(mainCityName, VPos.TOP);
        this.add(mainCityName, 1, 3);

        RowConstraints fifthRow = new RowConstraints();
        fifthRow.setPercentHeight(20.6);
        GridPane.setValignment(provider, VPos.BOTTOM);
        GridPane.setHalignment(provider, HPos.LEFT);
        GridPane.setMargin(provider, new Insets(10));
        this.add(provider, 0, 4, 2, 1);

        RowConstraints detailsRow = new RowConstraints();
        //detailsRow.setPercentHeight(50);
        this.getRowConstraints().addAll(firstRow, secondRow, thirdRow, fourthRow, fifthRow, detailsRow);

        GridPane details = new GridPane();
        details.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints firstDetailCol = new ColumnConstraints();
        firstDetailCol.setPercentWidth(40);
        ColumnConstraints secondDetailCol = new ColumnConstraints();
        secondDetailCol.setPercentWidth(60);
        
        details.getColumnConstraints().addAll(firstDetailCol, secondDetailCol);
        
        detailsHeader.getStyleClass().addAll("text", "section-title");
        detailsHeader.setMaxWidth(Double.MAX_VALUE);

        details.add(detailsHeader, 0, 0, 2, 1);

        GridPane.setValignment(detailsDateLabel, VPos.TOP);
        detailsDateLabel.getStyleClass().add("text");
        GridPane.setValignment(detailsCityLabel, VPos.TOP);
        detailsCityLabel.getStyleClass().add("text");
        detailsTempLabel.getStyleClass().add("text");
        GridPane.setValignment(detailsDescLabel, VPos.TOP);
        detailsDescLabel.getStyleClass().add("text");
        detailsPressLabel.getStyleClass().add("text");
        detailsHumLabel.getStyleClass().add("text");
        detailsWindLabel.getStyleClass().add("text");
        detailsWindDirLabel.getStyleClass().add("text");

        detailsDateValue.getStyleClass().add("text");
        detailsDateValue.setWrapText(true);
        detailsCityValue.getStyleClass().add("text");
        detailsCityValue.setWrapText(true);
        detailsTempValue.getStyleClass().add("text");
        detailsDescValue.getStyleClass().add("text");
        detailsDescValue.setWrapText(true);
        detailsPressValue.getStyleClass().add("text");
        detailsHumValue.getStyleClass().add("text");
        detailsWindValue.getStyleClass().add("text");
        detailsWindDirValue.getStyleClass().add("text");

        details.add(detailsDateLabel, 0, 1);
        details.add(detailsDateValue, 1, 1);

        details.add(detailsCityLabel, 0, 2);
        details.add(detailsCityValue, 1, 2);

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

        GridPane.setValignment(details, VPos.TOP);
        GridPane.setHgrow(details, Priority.ALWAYS);
        this.add(details, 0, 5, 2, 1);

        updateHighlights();

    }

    private void updateHighlights() {
        currentText.setStyle("-fx-border-color: transparent transparent hsb(" + highlightColor[0] + ", " + highlightColor[1] * 100 + "%, " + highlightColor[2] * 100 + "%) transparent;");
        detailsHeader.setStyle("-fx-border-color: transparent transparent hsb(" + highlightColor[0] + ", " + highlightColor[1] * 100 + "%, " + highlightColor[2] * 100 + "%) transparent;");
        mainTemp.setStyle("-fx-fill: hsb(" + highlightColor[0] + ", " + highlightColor[1] * 100 + "%, " + highlightColor[2] * 100 + "%);");
        if(this.drawGradient){
            setStyle("-fx-background-color: linear-gradient(transparent 50%, hsba(" + highlightColor[0] + ", " + highlightColor[1] * 100 + "%, " + highlightColor[2] * 100 + "%, 0.3) 100%);");
        }
    }

    protected final void update(WeatherPlugin plugin, WeatherData data) {
        highlightColor = ColorTools.tempToHsbPureInverted(data.getTemperature());
        Platform.runLater(() -> {
            this.getChildren().remove(currentIcon);
            double iconRatioBecauseItIsFixed = (1d / 1920d) * ScenesHandler.getContentWidthProperty().getValue();
            if(iconRatioBecauseItIsFixed < 0.478){
                iconRatioBecauseItIsFixed = 0.478;
            }
            currentIcon = getWeatherIcon(data.getIcon(), 210*iconRatioBecauseItIsFixed, 210*iconRatioBecauseItIsFixed, false);
            GridPane.setMargin(currentIcon, new Insets(10));
            this.add(currentIcon, 0, 1, 1, 3);
            mainTemp.setText(String.valueOf(data.getTemperature()));
            mainCityName.setText(plugin.getCityName().getValue());
            mainDesc.setText(data.getDescription());
            provider.setText("Data by: " + plugin.getSupplierName().getValue());

            detailsDateValue.setText(new StringBuilder(": ").append(ServerTime.getLongDateTimeFormat().format(data.getWeatherDate())).toString());
            detailsCityValue.setText(new StringBuilder(": ").append(plugin.getCityName().getValue()).toString());
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
