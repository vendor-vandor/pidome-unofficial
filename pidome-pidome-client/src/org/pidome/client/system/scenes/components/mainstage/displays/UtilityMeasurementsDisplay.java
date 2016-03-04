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

package org.pidome.client.system.scenes.components.mainstage.displays;

import eu.hansolo.enzo.gauge.Gauge;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.components.utilitymeasurement.UtilityMeasurement;
import org.pidome.client.system.domotics.components.utilitymeasurement.UtilityMeasurements;
import org.pidome.client.system.domotics.components.utilitymeasurement.UtilityMeasurementsException;
import org.pidome.client.system.scenes.components.mainstage.displays.components.UtilityDataGraph;
import org.pidome.client.system.scenes.components.mainstage.displays.components.UtilityMeasurementGauge;
import org.pidome.client.system.scenes.windows.TitledWindow;

/**
 *
 * @author John
 */
public class UtilityMeasurementsDisplay extends TitledWindow {

    boolean currentWattUpdated = false;
    
    boolean todayKwhUpdated = false;
    boolean todayWaterUpdated = false;
    boolean todayGasUpdated = false;
    
    boolean buildCurrentWatt = false;
    boolean buildTotalKwh = false;
    boolean buildTotalWater = false;
    boolean buildTotalGas = false;
    
    boolean fullBuid = true;
    
    HBox totalKwh;
    private Gauge kwhGauge;
    HBox totalWater;
    private Gauge waterGauge;
    HBox totalGas;
    private Gauge gasGauge;
    
    UtilityDataGraph wattGraph;
    UtilityDataGraph kwhGraph;
    UtilityDataGraph waterGraph;
    UtilityDataGraph gasGraph;
    
    HBox currentWatt;
    private Gauge gaugeCurrentWatt;
    UtilityMeasurement utility;
    
    HBox todayAll = new HBox();
    
    boolean quality = false;
    
    ChangeListener<Number> kwhUpdater   = this::kwhUpdaterHelper;
    ChangeListener<Number> wattUpdater  = this::wattUpdaterHelper;
    ChangeListener<Number> waterUpdater = this::waterUpdaterHelper;
    ChangeListener<Number> gasUpdater   = this::gasUpdaterHelper;
    
    static Logger LOG = LogManager.getLogger(UtilityMeasurementsDisplay.class);

    public UtilityMeasurementsDisplay() throws UtilityMeasurementsException {
        super("utilitymeasurements", "Utility usages");
        utility = UtilityMeasurements.getMeasurementsPlugin();
        buildCurrentWatt = true;
        buildTotalKwh = true;
        buildTotalWater = true;
        buildTotalGas = true;
        if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){
            quality = true;
        }
    }

    public UtilityMeasurementsDisplay(Object... buildGraphs) throws UtilityMeasurementsException {
        super("utilitymeasurements"+(String)buildGraphs[0], (String)buildGraphs[1]);
        utility = UtilityMeasurements.getMeasurementsPlugin();
        fullBuid = false;
        switch ((String)buildGraphs[0]) {
            case "watt":
                buildCurrentWatt = true;
                break;
            case "kwh":
                buildTotalKwh = true;
                break;
            case "water":
                buildTotalWater = true;
                break;
            case "gas":
                buildTotalGas = true;
                break;
        }
        if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){
            quality = true;
        }
    }
    
    @Override
    protected void setupContent() {
        if(buildCurrentWatt) { buildCurrentWattGauge(); todayAll.getChildren().add(currentWatt); }
        if(buildTotalKwh) { buildTotalKwhGauge(); todayAll.getChildren().add(totalKwh); }
        if(buildTotalWater) { buildTotalWaterGauge(); todayAll.getChildren().add(totalWater); }
        if(buildTotalGas) { buildTotalGasGauge(); todayAll.getChildren().add(totalGas); }
        assignContent(todayAll);
        Thread waitForIt = new Thread(){
            
            @Override
            public final void run(){
                try {
                    Thread.sleep(1000);
                    if(buildCurrentWatt && !currentWattUpdated)Platform.runLater(() -> { gaugeCurrentWatt.setValue(utility.getCurrentWattProperty().get()); });
                    if(buildTotalKwh    && !todayKwhUpdated)   Platform.runLater(() -> { kwhGauge.setValue(utility.getTodayKwhProperty().get()); });
                    if(buildTotalWater  && !todayWaterUpdated) Platform.runLater(() -> { waterGauge.setValue(utility.getTodayWaterProperty().get()); });
                    if(buildTotalGas    && !todayGasUpdated)   Platform.runLater(() -> { gasGauge.setValue(utility.getTodayGasProperty().get()); });
                } catch (InterruptedException ex) {
                    
                }
            }

        };
        waitForIt.start();
    }

    final void kwhUpdaterHelper(ObservableValue<? extends Number> ov, Number t, Number t1){
        todayKwhUpdated = true;
        Platform.runLater(() -> { if(kwhGauge!=null)kwhGauge.setValue((double)t1); });
    }
    
    final void buildTotalKwhGauge(){
        totalKwh = new HBox();
        UtilityMeasurementGauge gauge = new UtilityMeasurementGauge("kwh", utility.getMappedName("KWH"),utility.getCurrentKwhUpperBound());
        kwhGauge = gauge.getGauge();
        utility.getTodayKwhProperty().addListener(kwhUpdater);
        if(!fullBuid){
            kwhGraph = new UtilityDataGraph(utility, utility.getMappedName("KWH") + " usage", "KWH");
            kwhGraph.setupContent();
            HBox.setMargin(gauge.getPane(), new Insets(20*DisplayConfig.getHeightRatio(),0,0,10*DisplayConfig.getWidthRatio()));
            totalKwh.getChildren().addAll(gauge.getPane(), kwhGraph);
        } else {
            totalKwh.getChildren().add(gauge.getPane());
        }
    }
    
    final void waterUpdaterHelper(ObservableValue<? extends Number> ov, Number t, Number t1){
        todayWaterUpdated = true;
        Platform.runLater(() -> { if(waterGauge!=null)waterGauge.setValue((double)t1); });
    }
    
    final void buildTotalWaterGauge(){
        totalWater = new HBox();
        UtilityMeasurementGauge gauge = new UtilityMeasurementGauge("water", utility.getMappedName("WATER"),utility.getCurrentWaterUpperBound());
        waterGauge = gauge.getGauge();
        utility.getTodayWaterProperty().addListener(waterUpdater);
        if(!fullBuid){
            waterGraph = new UtilityDataGraph(utility, utility.getMappedName("WATER") + " usage", "WATER");
            waterGraph.setupContent();
            HBox.setMargin(gauge.getPane(), new Insets(20*DisplayConfig.getHeightRatio(),0,0,10*DisplayConfig.getWidthRatio()));
            totalWater.getChildren().addAll(gauge.getPane(), waterGraph);
        } else {
            totalWater.getChildren().add(gauge.getPane());
        }
    }
    
    final void gasUpdaterHelper(ObservableValue<? extends Number> ov, Number t, Number t1){
        todayGasUpdated = true;
        Platform.runLater(() -> { if(gasGauge!=null)gasGauge.setValue((double)t1); });
    }
    final void buildTotalGasGauge(){
        totalGas = new HBox();
        UtilityMeasurementGauge gauge = new UtilityMeasurementGauge("gas", utility.getMappedName("GAS"),utility.getCurrentGasUpperBound());
        gasGauge = gauge.getGauge();
        utility.getTodayGasProperty().addListener(gasUpdater);
        if(!fullBuid){
            gasGraph = new UtilityDataGraph(utility, utility.getMappedName("GAS") + " usage", "GAS");
            gasGraph.setupContent();
            HBox.setMargin(gauge.getPane(), new Insets(20*DisplayConfig.getHeightRatio(),0,0,10*DisplayConfig.getWidthRatio()));
            totalGas.getChildren().addAll(gauge.getPane(), gasGraph);
        } else {
            totalGas.getChildren().add(gauge.getPane());
        }
    }
    
    final void wattUpdaterHelper(ObservableValue<? extends Number> ov, Number t, Number t1){
        currentWattUpdated = true;
        Platform.runLater(() -> { 
            if(gaugeCurrentWatt!=null)gaugeCurrentWatt.setValue((double)t1);
        });
    }
    final void buildCurrentWattGauge(){
        currentWatt = new HBox();
        UtilityMeasurementGauge gauge = new UtilityMeasurementGauge("watt", utility.getMappedName("WATT"),utility.getCurrentWattUpperBound());
        gaugeCurrentWatt = gauge.getGauge();
        utility.getCurrentWattProperty().addListener(wattUpdater);
        if(!fullBuid){
            wattGraph = new UtilityDataGraph(utility, utility.getMappedName("WATT") + " usage", "WATT");
            wattGraph.setupContent();
            HBox.setMargin(gauge.getPane(), new Insets(20*DisplayConfig.getHeightRatio(),0,0,10*DisplayConfig.getWidthRatio()));
            currentWatt.getChildren().addAll(gauge.getPane(), wattGraph);
        } else {
            currentWatt.getChildren().add(gauge.getPane());
        }
    }
    
    @Override
    protected void removeContent() {
        
        if(fullBuid || buildCurrentWatt) utility.getCurrentWattProperty().removeListener(wattUpdater);
        if(fullBuid || buildTotalKwh) utility.getTodayKwhProperty().removeListener(kwhUpdater);
        if(fullBuid || buildTotalWater) utility.getTodayWaterProperty().removeListener(waterUpdater);
        if(fullBuid || buildTotalGas) utility.getTodayGasProperty().removeListener(gasUpdater); 

        if(!fullBuid && buildCurrentWatt) wattGraph.removeContent(); 
        if(!fullBuid && buildTotalKwh) kwhGraph.removeContent(); 
        if(!fullBuid && buildTotalWater) waterGraph.removeContent(); 
        if(!fullBuid && buildTotalGas) gasGraph.removeContent(); 
        
        totalKwh = null;
        totalWater = null;
        totalGas = null;
        gaugeCurrentWatt = null;
        
        todayAll.getChildren().clear();
        
    }
    
    
    
}
