/*
 * Copyright 2014 John.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.client.system.scenes.components.mainstage.displays.controls;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.components.mainstage.displays.components.LogarithmicAxis;

/**
 *
 * @author John
 */
public class PiDomeHistoricChart extends Region {
    
    XYChart chart;
    ObservableList<XYChart.Data<String,Double>> graphData = FXCollections.observableArrayList();
    XYChart.Series dataSeries = new XYChart.Series<>(graphData);

    PiDomeHistoricChartDataClickedHandler clickedHandler;
    
    static Logger LOG = LogManager.getLogger(PiDomeHistoricChart.class);
    
    private final Glow glow = new Glow(.8);
    
    PiDomeHistoricChartType chartType;
    
    String plotType = "time-series";
    
    double minValue = Double.MAX_VALUE;
    double maxValue = Double.MIN_VALUE;
    
    public PiDomeHistoricChart(PiDomeHistoricChartDataClickedHandler object, PiDomeHistoricChartType type, String plotType){
        clickedHandler = object;
        this.chartType = type;
        this.plotType  = plotType;
    }
    
    public final void addSingleData(Object key, double value){
        graphData.add(new XYChart.Data(key,value));
        maxValue = Math.max(maxValue, value);
        minValue = Math.min(minValue, value);
    }
    
    public final void handleGraphData(final Change<? extends String,? extends Double> change){
        Platform.runLater(() -> { 
            String key;
            if(change.wasAdded() && change.wasRemoved()){
                key = change.getKey();
                graphData.stream().filter((graphData1) -> (graphData1.getXValue().equals(key))).forEach((graphData1) -> {
                    graphData1.setYValue(change.getValueAdded());
                });
            } else if(change.wasRemoved()){
                List<XYChart.Data<String,Double>> stringToRemove = new ArrayList();
                key = change.getKey();
                graphData.stream().filter((graphData1) -> (graphData1.getXValue().equals(key))).forEach((graphData1) -> {
                    stringToRemove.add(graphData1);
                });
                graphData.removeAll(stringToRemove);
            } else if(change.wasAdded()){
                key = change.getKey();
                final XYChart.Data<String,Double> newData = new XYChart.Data(key, change.getValueAdded());
                if (graphData!=null){
                    graphData.add(newData);
                    LOG.info("{} - {}", newData.getXValue(),newData.getYValue());
                    final Node n = newData.getNode();

                    n.setEffect(null);
                    n.setOnMouseEntered((MouseEvent e) -> {
                        n.setEffect(glow);
                    });
                    n.setOnMouseExited((MouseEvent e) -> {
                        n.setEffect(null);
                    });
                    n.setOnMouseClicked((MouseEvent e) -> {
                        n.setCursor(Cursor.HAND);
                        n.addEventHandler(MouseEvent.MOUSE_ENTERED, (MouseEvent me) -> {
                            LOG.info("Clicked and got values: {} - {}", newData.getXValue(),newData.getYValue());
                            clickedHandler.handleGraphPlotValue(newData.getXValue(), newData.getYValue());
                        });
                    });
                }
            }
        });
    }
    
    public final void unsetGraphData(){
        try {
            List<XYChart.Data<String,Double>> itemsToRemove = new ArrayList();
            graphData.subList(0, graphData.size()).stream().forEach((item) -> {
                itemsToRemove.add(item);
            });
            graphData.removeAll(itemsToRemove);
            //// The below seems inconsistent sometimes graph data is cleared earlier then expected
            //// and the only difference is the use of a graph type.
            //// Line is always ok, bar seems to need it,
            //// Area seems to be unpredictable.
            //// This is due the FX thread?
            //graphData.clear();
        } catch (Exception ex) {
            LOG.info("could not clear data: {}", ex.getMessage(), ex);
        }
    }
    
    public final void removeChart(){
        unsetGraphData();
        chart.getData().remove(dataSeries);
        getChildren().remove(chart);
        chart = null;
    }
    
    public final void createChart(){
        ValueAxis yAxis;
        switch(plotType){
            case "time-log":
                if(minValue < 0.001){
                    minValue = 0.0001;
                } else if(minValue < 0.01){
                    minValue = 0.001;
                } else if(minValue < 0.1){
                    minValue = 0.01;
                } else if(minValue < 1){
                    minValue = 0.1;
                } else if(minValue < 10){
                    minValue = 1.0;
                }
                if(maxValue < 1){
                    maxValue = 1;
                } else if(maxValue < 10){
                    maxValue = 10;
                } else if(maxValue < 100){
                    maxValue = 100;
                } else if(maxValue < 1000){
                    maxValue = 1000;
                } else if(maxValue < 10000){
                    maxValue = 10000;
                } else if(maxValue < 100000){
                    maxValue = 100000;
                }else if(maxValue < 1000000){
                    maxValue = 1000000;
                } else if(maxValue < 10000000){
                    maxValue = 10000000;
                }
                yAxis = new LogarithmicAxis(minValue, maxValue);
                yAxis.minorTickVisibleProperty().set(false);
            break;
            default:
                yAxis = new NumberAxis();
            break;
        }
        CategoryAxis DateAxisCategorized = new CategoryAxis();
        switch(this.chartType){
            case LINE:
                chart = new LineChart(DateAxisCategorized,yAxis);
            break;
            case BAR:
                chart = new BarChart(DateAxisCategorized,yAxis);
            break;
            case AREA:
                chart = new AreaChart(DateAxisCategorized,yAxis);
            break;
        }
        yAxis.setSide(Side.RIGHT);
        if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_LOW)){
            chart.setAnimated(false);
            yAxis.setAnimated(false);
            DateAxisCategorized.setAnimated(false);
        }
        chart.setVerticalGridLinesVisible(false);
        
        chart.setPrefSize(800*DisplayConfig.getWidthRatio(), 400*DisplayConfig.getHeightRatio());
        chart.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        chart.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        chart.setLegendVisible(false);
        getChildren().add(chart);
        if (Platform.isFxApplicationThread()) {
            chart.getData().add(dataSeries);
        } else {
            Platform.runLater(() -> {
                chart.getData().add(dataSeries);
            });
        }
    }
    
}