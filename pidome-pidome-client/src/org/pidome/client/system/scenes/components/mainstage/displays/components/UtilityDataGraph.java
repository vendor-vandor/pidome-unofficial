/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays.components;

import java.text.DecimalFormat;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.collections.MapChangeListener.Change;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.scenes.components.mainstage.displays.controls.GraphDataHistory;
import org.pidome.client.system.domotics.components.utilitymeasurement.UtilityMeasurement;
import org.pidome.client.system.scenes.components.mainstage.displays.controls.PiDomeHistoricChart;
import org.pidome.client.system.scenes.components.mainstage.displays.controls.PiDomeHistoricChartDataClickedHandler;
import org.pidome.client.system.scenes.components.mainstage.displays.controls.PiDomeHistoricChartType;

/**
 *
 * @author John Sirach
 */
public class UtilityDataGraph extends Region implements PiDomeHistoricChartDataClickedHandler {

    String title;
    String setName;
    
    Text currentText = new Text();
    
    GraphDataHistory utilityHistData;
    
    PiDomeHistoricChart chart;
    
    String curTimeLine;
    
    static Logger LOG = LogManager.getLogger(UtilityDataGraph.class);
    
    TabbedContent tabs = new TabbedContent();
    
    static DecimalFormat dfThree = new DecimalFormat("#.###");
    static DecimalFormat dfTwo = new DecimalFormat("#.##");
    
    Text clickedText = new Text("Click/press to view");
    
    UtilityMeasurement utility;
    
    ChangeListener<Number> valuesUpdater = this::currentValuesUpdateHelper;
    MapChangeListener graphUpdater = this::graphDataHelper;
    
    final void currentValuesUpdateHelper(ObservableValue<? extends Number> ov, Number t, Number t1){
        Platform.runLater(() -> { 
            switch(setName){
                case "WATT":
                case "WATER":
                    currentText.setText(dfTwo.format(utility.getPropertyByName(setName).get()) + " " + utility.getMappedName(setName));
                break;
                default:
                    currentText.setText(dfThree.format(utility.getPropertyByName(setName).get()) + " " + utility.getMappedName(setName));
                break;
            }
        });
    }

    public UtilityDataGraph(UtilityMeasurement utility, String measureName, String measureType){
        this.title   = measureName;
        this.setName = measureType;
        utilityHistData = new GraphDataHistory(GraphDataHistory.ResourceType.UTILITY, utility.getPluginId(), "UTILITY", setName);
        utilityHistData.refreshData();
        
        this.utility = utility;
        
        switch(setName){
            case "WATT":
            case "WATER":
                currentText = new Text(dfTwo.format(utility.getPropertyByName(setName).get()) + " " + utility.getMappedName(setName));
            break;
            default:
                currentText = new Text(dfThree.format(utility.getPropertyByName(setName).get()) + " " + utility.getMappedName(setName));
            break;
        }
        this.utility.getPropertyByName(setName).addListener(valuesUpdater);
    }
    
    final String graphTitle(){
        return this.title;
    }
    
    final void graphDataHelper(Change<? extends String, ? extends Double> change){
        chart.handleGraphData(change);
    }
    
    final VBox createChart(String timeLine){
        VBox chartData = new VBox();
        if(chart!=null)chart.removeChart();
        chart = new PiDomeHistoricChart(this,PiDomeHistoricChartType.BAR, "time-series");
        chart.createChart();
        try {
            utilityHistData.getInitialSet(timeLine).entrySet().stream().forEach((data) -> {
                chart.addSingleData(data.getKey(), data.getValue());
            });
            curTimeLine = timeLine;
            utilityHistData.getDataConnection(curTimeLine).addListener(graphUpdater);

            TilePane contentDesc = new TilePane();
            contentDesc.getStyleClass().add("graphdescription");
            contentDesc.setPrefColumns(2);
            contentDesc.setPadding(new Insets(10, 0, 10, 5));

            GridPane contentDescLeft = new GridPane();
            contentDescLeft.setHgap(5);
            contentDescLeft.setVgap(10);

            contentDescLeft.add(new Text((setName.equals("WATT"))?"Current":"Total today"), 1, 0); 
            contentDescLeft.add(new Text(":"), 2, 0); 
            contentDescLeft.add(currentText, 3, 0);

            contentDescLeft.add(new Text("Selected value"), 1, 1); 
            contentDescLeft.add(new Text(":"), 2, 1); 
            contentDescLeft.add(clickedText, 3, 1);

            GridPane contentDescRight = new GridPane();
            contentDescRight.setHgap(5);
            contentDescRight.setVgap(10);

            /*
            switch(setName){
                case "WATT":
                case "KWH":
                    contentDescRight.add(new Text("Costs high"), 1, 0); 
                    contentDescRight.add(new Text(":"), 2, 0);
                    contentDescRight.add(new Text("N/A"), 3, 0);

                    contentDescRight.add(new Text("Costs low"), 1, 1); 
                    contentDescRight.add(new Text(":"), 2, 1);
                    contentDescRight.add(new Text("N/A"), 3, 1);
                break;
                default:
                    contentDescRight.add(new Text("Costs"), 1, 0); 
                    contentDescRight.add(new Text(":"), 2, 0);
                    contentDescRight.add(new Text("N/A"), 3, 0);
                break;
            }
            */
            contentDesc.getChildren().addAll(contentDescLeft, contentDescRight);
            chartData.getChildren().addAll(chart, contentDesc);
        } catch (java.util.NoSuchElementException ex){
            ////ok
        }
        return chartData;
    }
    
    final void unsetGraphDisplay(){
        utilityHistData.getDataConnection(curTimeLine).removeListener(graphUpdater);
        this.utility.getPropertyByName(setName).removeListener(valuesUpdater);
        utilityHistData.clearData();
        if(chart!=null)chart.removeChart();
        getChildren().clear();
    }
    
    final void tabSwitchHelper(String oldTab, String newTab){
        tabs.setTabContent(newTab,createChart(newTab),graphTitle());
    }
    
    public final void setupContent() {
        tabs.addTabChangedListener(this::tabSwitchHelper);
        tabs.addTab("hour", "Last Hour");
        tabs.addTab("day", "Last Day");
        tabs.addTab("week", "Last Week");
        tabs.addTab("month", "Last Month");
        tabs.addTab("year", "Last Year");
        getChildren().add(tabs);
    }

    public final void removeContent() {
        unsetGraphDisplay();
        tabs.removeTabChangedListener(this::tabSwitchHelper);
        tabs.destroy();
        getChildren().remove(tabs);
    }

    @Override
    public void handleGraphPlotValue(String category,double value) {
        clickedText.setText(category + " - " + value);
    }

}
