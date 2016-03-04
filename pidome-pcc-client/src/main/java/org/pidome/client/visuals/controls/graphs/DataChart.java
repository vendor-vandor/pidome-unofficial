/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.visuals.controls.graphs;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import org.pidome.client.entities.devices.DeviceDataControl;
import org.pidome.client.entities.devices.DeviceDataControl.GraphType;
import org.pidome.client.services.ServiceConnector.DisplayType;
import org.pidome.client.tools.DisplayTools;

/**
 *
 * @author John
 */
public class DataChart {
    
    XYChart chart;
    ObservableList<XYChart.Data<Date,Double>> graphData = FXCollections.observableArrayList();
    XYChart.Series dataSeries = new XYChart.Series<>(graphData);
 
    private double minValue = Double.MAX_VALUE;
    private double maxValue = Double.MIN_VALUE;
    
    private final DeviceDataControl.GraphType plotType;
    
    private String dataSuffix = "";
    private String dataPrefix = "";
    
    private String seriesName = "";
    private String valueName  = "";
    
    private double prefWidth = 0;
    private double prefHeight = 0;
    
    public DataChart(DeviceDataControl.GraphType type){
        plotType = type;
    }
    
    public final void addSingleData(Date key, double value){
        graphData.add(new XYChart.Data(key,value));
        maxValue = Math.max(maxValue, value);
        minValue = Math.min(minValue, value);
    }
    
    public final void handleGraphData(final MapChangeListener.Change<? extends Date,? extends Double> change){
        Platform.runLater(() -> { 
            Date key;
            if(change.wasRemoved()){
                List<XYChart.Data<Date,Double>> stringToRemove = new ArrayList();
                key = change.getKey();
                graphData.stream().filter((graphData1) -> (graphData1.getXValue().equals(key))).forEach((graphData1) -> {
                    stringToRemove.add(graphData1);
                });
                graphData.removeAll(stringToRemove);
            } else if(change.wasAdded()){
                key = change.getKey();
                final XYChart.Data<Date,Double> newData = new XYChart.Data(key, change.getValueAdded());
                if (graphData!=null){
                    graphData.add(newData);
                }
            }
        });
    }
    
    public final void setDataSuffix(String suffix){
        dataSuffix = suffix;
    }
    
    public final void setDataPrefix(String prefix){
        dataPrefix = prefix;
    }
    
    public final void setSeriesName(String name){
        seriesName = name;
    }
    
    public final void setValueName(String name){
        valueName = name;
    }
    
    public final void setPrefSize(double prefWidth, double prefHeight){
        //this.prefWidth  = prefWidth;
        //this.prefHeight = prefHeight;
    }
    
    public final XYChart createChart(){
        ValueAxis yAxis;
        switch(plotType){
            case LOG:
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
                yAxis = new LogarithmicAxis();
                yAxis.minorTickVisibleProperty().set(false);
            break;
            default:
                yAxis = new NumberAxis();
                yAxis.setAutoRanging(true);
                ((NumberAxis)yAxis).setForceZeroInRange(false);
            break;
        }
        DateAxis DateAxisCategorized = new DateAxis();
        switch(this.plotType){
            case TOTAL:
                chart = new BarChart(new CategoryAxis(),yAxis);
            break;
            default:
                chart = new LineChart(DateAxisCategorized,yAxis);
                ((LineChart)chart).setCreateSymbols(false);
            break;
        }
        if(DisplayTools.getUserDisplayType()==DisplayType.LARGE){
            StringBuilder yLabel = new StringBuilder(valueName);
            if(!dataSuffix.equals("") && !dataPrefix.equals("")){
                yLabel.append(" (").append(dataSuffix).append(", ").append(dataPrefix).append(")");
            } else if(!dataSuffix.equals("")){
                yLabel.append(" (").append(dataSuffix).append(")");
            } else if(!dataPrefix.equals("")){
                yLabel.append(" (").append(dataPrefix).append(")");
            }
            yAxis.setLabel(yLabel.toString());
        }
        yAxis.setSide(Side.LEFT);
        dataSeries.setName(seriesName);
        //if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_LOW)){
        //    chart.setAnimated(false);
        //    yAxis.setAnimated(false);
        //    DateAxisCategorized.setAnimated(false);
        //}
        chart.setVerticalGridLinesVisible(false);
        StackPane.setMargin(chart, new Insets(0,10,0,0));
        if (Platform.isFxApplicationThread()) {
            chart.getData().add(dataSeries);
        } else {
            Platform.runLater(() -> {
                chart.getData().add(dataSeries);
            });
        }
        if(this.prefWidth!=0 && this.prefHeight != 0){
            chart.setPrefSize(prefWidth, prefHeight);
        }
        return chart;
    }
    
    public final void preFill(Map<Date, Double> dataSet){
        SortedSet<Map.Entry<Date, Double>> sortedset = new TreeSet<>(
            (Map.Entry<Date, Double> e1, Map.Entry<Date, Double> e2) -> ((Long)e1.getKey().getTime()).compareTo(e2.getKey().getTime())
        );
        sortedset.addAll(dataSet.entrySet());
        if(this.plotType==GraphType.TOTAL){
            DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
            for(Map.Entry<Date,Double> data:sortedset){
                graphData.add(new XYChart.Data(dateFormat.format(data.getKey()), data.getValue()));
            }
        } else {
            for(Map.Entry<Date,Double> data:sortedset){
                graphData.add(new XYChart.Data(data.getKey(), data.getValue()));
            }
        }
    }
    
    public final XYChart getChart(){
        return chart;
    }
    
    public final ObservableList<XYChart.Data<Date,Double>> getData(){
        return graphData;
    }
    
    public final void unsetGraphData(){
        try {
            chart.getData().remove(dataSeries);
        } catch (Exception ex) {
            
        }
    }
    
    public final void updateGraphData(Number newValue){
        Platform.runLater(() -> { 
            graphData.remove(graphData.get(0));
            if(this.plotType==GraphType.TOTAL){
                DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
                graphData.add(new XYChart.Data(dateFormat.format(new Date()), newValue.doubleValue()));
            } else {
                graphData.add(new XYChart.Data(new Date(), newValue.doubleValue()));
            }
        });
    }
    
}