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

package org.pidome.client.system.scenes.components.mainstage.displays.components;

import javafx.collections.MapChangeListener.Change;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.scenes.components.mainstage.displays.controls.GraphDataHistory;
import org.pidome.client.system.domotics.components.locations.Locations;
import org.pidome.client.system.scenes.components.mainstage.displays.controls.PiDomeHistoricChart;
import org.pidome.client.system.scenes.components.mainstage.displays.controls.PiDomeHistoricChartDataClickedHandler;
import org.pidome.client.system.scenes.components.mainstage.displays.controls.PiDomeHistoricChartType;

/**
 *
 * @author John
 */
public class DeviceDataGraph extends Region implements PiDomeHistoricChartDataClickedHandler {

    Device device;
    String groupName;
    String setName;
    
    GraphDataHistory deviceHistData;
    
    PiDomeHistoricChart lineChart;
    
    String curTimeLine;
    
    static Logger LOG = LogManager.getLogger(UtilityDataGraph.class);
    
    TabbedContent tabs = new TabbedContent();
    
    Text selectedText = new Text("Press/click to select");
    
    public DeviceDataGraph(Device device, String group, String dataSet){
        this.device = device;
        this.groupName = group;
        this.setName = dataSet;
        
        deviceHistData = new GraphDataHistory(GraphDataHistory.ResourceType.DEVICE, this.device.getId(), groupName, setName);
        deviceHistData.refreshData();
        
    }
    
    final String graphTitle(String type){
        String location;
        try {
            location = (Locations.getLocation(device.getLocation()));
        } catch (DomComponentsException ex) {
            location = "";
        }
        return location + " " + 
               device.getName() + ": " + 
               device.getCommandGroups().get(groupName).getSetDetails(setName).get("label") + " " + 
               type + " graph";
    }
    
    final void graphDataHelper(Change<? extends String, ? extends Double> change){
        lineChart.handleGraphData(change);
    }
    
    final VBox createChart(String timeLine){
        VBox chartData = new VBox();
        lineChart = new PiDomeHistoricChart(this,PiDomeHistoricChartType.LINE, (String)device.getCommandGroups().get(groupName).getSetDetails(setName).get("graphtype"));
        if(device.getCommandGroups().get(groupName).hasDataHistory(setName)){
            deviceHistData.getInitialSet(timeLine).entrySet().stream().forEach((data) -> {
                lineChart.addSingleData(data.getKey(), data.getValue());
            });
            lineChart.createChart();
            curTimeLine = timeLine;
            deviceHistData.getDataConnection(curTimeLine).addListener(this::graphDataHelper);
            
            TilePane contentDesc = new TilePane();
            contentDesc.getStyleClass().add("graphdescription");
            contentDesc.setPrefColumns(2);
            contentDesc.setPadding(new Insets(10, 0, 10, 5));

            GridPane contentDescLeft = new GridPane();
            contentDescLeft.setHgap(5);
            contentDescLeft.setVgap(10);
            
            contentDescLeft.add(new Text("Selected value"), 1, 0); 
            contentDescLeft.add(new Text(":"), 2, 0); 
            contentDescLeft.add(selectedText, 3, 0);
            
            contentDesc.getChildren().add(contentDescLeft);
            
            chartData.getChildren().addAll(lineChart, contentDesc);
        }
        return chartData;
    }
    
    final void unsetGraphDisplay(){
        deviceHistData.getDataConnection(curTimeLine).removeListener(this::graphDataHelper);
        lineChart.removeChart();
    }
    
    final void tabSwitchHelper(String oldTab, String newTab){
        tabs.setTabContent(newTab,createChart(newTab),graphTitle(newTab));
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
        tabs.destroy();
        getChildren().remove(tabs);
        tabs.removeTabChangedListener(this::tabSwitchHelper);
    }

    @Override
    public void handleGraphPlotValue(String category, double value) {
        selectedText.setText(category + " - " + value);
    }

}