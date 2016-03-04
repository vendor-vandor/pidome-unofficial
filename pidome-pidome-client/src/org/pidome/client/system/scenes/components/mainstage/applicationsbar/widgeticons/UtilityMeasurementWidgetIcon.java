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

package org.pidome.client.system.scenes.components.mainstage.applicationsbar.widgeticons;

import eu.hansolo.enzo.gauge.OneEightyGauge;
import eu.hansolo.enzo.gauge.OneEightyGaugeBuilder;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.components.utilitymeasurement.UtilityMeasurement;
import org.pidome.client.system.domotics.components.utilitymeasurement.UtilityMeasurements;
import org.pidome.client.system.domotics.components.utilitymeasurement.UtilityMeasurementsException;
import org.pidome.client.system.scenes.components.mainstage.displays.UtilityMeasurementsDisplay;
import org.pidome.client.system.scenes.windows.WindowComponent;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 *
 * @author John
 */
public class UtilityMeasurementWidgetIcon extends DraggableApplicationbarWidgetIcon {

    ArrayList<String> parameters = new ArrayList();
    boolean currentWattUpdated = false;
    boolean currentKwhUpdated = false;
    boolean currentWaterUpdated = false;
    boolean currentGasUpdated = false;
    
    boolean quality = false;
    
    Object[] openerParams;
    
    OneEightyGauge watt;
    OneEightyGauge kwh;
    OneEightyGauge water;
    OneEightyGauge gas;

    ChangeListener<Number> wattUpdater = this::wattUpdaterHelper;
    ChangeListener<Number> kwhUpdater = this::kwhUpdaterHelper;
    ChangeListener<Number> waterUpdater = this::waterUpdaterHelper;
    ChangeListener<Number> gasUpdater = this::gasUpdaterHelper;
    
    UtilityMeasurement utility;

    @Override
    public void setParams(ArrayList<String> params) {
        parameters = params;
    }
    
    @Override
    public void build() {
        if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){
            quality = true;
        }
        try {
            utility = UtilityMeasurements.getMeasurementsPlugin();
            openerParams = new String[parameters.size()+1];
            openerParams[0] = parameters.get(0);
            switch((String)parameters.get(0)){
                case "watt":
                    openerParams[1] = "Utilities: Current power";
                    watt = OneEightyGaugeBuilder.create()
                            .maxValue(8000)
                            .minValue(0)
                            .barColor(Color.web("#5399c6"))
                            .valueColor(Color.web("#5399c6"))
                            .minTextColor(Color.TRANSPARENT)
                            .maxTextColor(Color.TRANSPARENT)
                            .animationDuration(400)
                            .decimals(0)
                            .barBackgroundColor(Color.web("#0e0e0e97"))
                            .shadowsEnabled(quality)
                            .animated(quality)
                            .build();
                    getChildren().add(watt);
                    watt.valueProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
                        Platform.runLater(() -> { 
                            double value = (double)t1;
                            if(value < 2500){
                                watt.setBarColor(Color.web("#aceeaa"));
                                watt.setValueColor(Color.web("#aceeaa"));
                            } else if (value < 5000){
                                watt.setBarColor(Color.web("#eedaaa"));
                                watt.setValueColor(Color.web("#eedaaa"));
                            } else {
                                watt.setBarColor(Color.web("#eeb0aa"));
                                watt.setValueColor(Color.web("#eeb0aa"));
                            }
                        });
                    });
                    utility.getCurrentWattProperty().addListener(wattUpdater);
                    watt.setMouseTransparent(true);
                    Thread waitForIt = new Thread(){
                        @Override
                        public final void run(){
                            try {
                                Thread.sleep(1000);
                                Platform.runLater(() -> { if(!currentWattUpdated)watt.setValue(utility.getCurrentWattProperty().get()); });
                            } catch (InterruptedException ex) {

                            }
                        }

                    };
                    waitForIt.start();
                break;
                case "kwh":
                    openerParams[1] = "Utilites: Total power";
                    kwh = OneEightyGaugeBuilder.create()
                            .maxValue(50)
                            .minValue(0)
                            .barColor(Color.web("#5399c6"))
                            .valueColor(Color.web("#5399c6"))
                            .minTextColor(Color.TRANSPARENT)
                            .maxTextColor(Color.TRANSPARENT)
                            .animationDuration(400)
                            .decimals(3)
                            .barBackgroundColor(Color.web("#0e0e0e97"))
                            .shadowsEnabled(quality)
                            .animated(quality)
                            .build();
                    getChildren().add(kwh);
                    kwh.valueProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
                        Platform.runLater(() -> { 
                            double value = (double)t1;
                            if(value < 20){
                                kwh.setBarColor(Color.web("#aceeaa"));
                                kwh.setValueColor(Color.web("#aceeaa"));
                            } else if (value < 35){
                                kwh.setBarColor(Color.web("#eedaaa"));
                                kwh.setValueColor(Color.web("#eedaaa"));
                            } else {
                                kwh.setBarColor(Color.web("#eeb0aa"));
                                kwh.setValueColor(Color.web("#eeb0aa"));
                            }
                        });
                    });
                    utility.getTodayKwhProperty().addListener(kwhUpdater);
                    kwh.setMouseTransparent(true);
                    Thread waitForItKwh = new Thread(){
                        @Override
                        public final void run(){
                            try {
                                Thread.sleep(1000);
                                Platform.runLater(() -> { if(!currentKwhUpdated)kwh.setValue(utility.getTodayKwhProperty().get()); });
                            } catch (InterruptedException ex) {

                            }
                        }

                    };
                    waitForItKwh.start();
                break;
                case "water":
                    openerParams[1] = "Utilites: Total water";
                    water = OneEightyGaugeBuilder.create()
                            .maxValue(700)
                            .minValue(0)
                            .barColor(Color.web("#5399c6"))
                            .valueColor(Color.web("#5399c6"))
                            .minTextColor(Color.TRANSPARENT)
                            .maxTextColor(Color.TRANSPARENT)
                            .animationDuration(400)
                            .decimals(2)
                            .barBackgroundColor(Color.web("#0e0e0e97"))
                            .shadowsEnabled(quality)
                            .animated(quality)
                            .build();
                    getChildren().add(water);
                    water.valueProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
                        Platform.runLater(() -> { 
                            double value = (double)t1;
                            if(value < 300){
                                water.setBarColor(Color.web("#aceeaa"));
                                water.setValueColor(Color.web("#aceeaa"));
                            } else if (value < 500){
                                water.setBarColor(Color.web("#eedaaa"));
                                water.setValueColor(Color.web("#eedaaa"));
                            } else {
                                water.setBarColor(Color.web("#eeb0aa"));
                                water.setValueColor(Color.web("#eeb0aa"));
                            }
                        });
                    });
                    utility.getTodayWaterProperty().addListener(waterUpdater);
                    water.setMouseTransparent(true);
                    Thread waitForItWater = new Thread(){
                        @Override
                        public final void run(){
                            try {
                                Thread.sleep(1000);
                                Platform.runLater(() -> { if(!currentWaterUpdated)water.setValue(utility.getTodayWaterProperty().get()); });
                            } catch (InterruptedException ex) {

                            }
                        }

                    };
                    waitForItWater.start();
                break;
                case "gas":
                    openerParams[1] = "Utilites: Total gas";
                    gas = OneEightyGaugeBuilder.create()
                            .maxValue(15)
                            .minValue(0)
                            .barColor(Color.web("#5399c6"))
                            .valueColor(Color.web("#5399c6"))
                            .minTextColor(Color.TRANSPARENT)
                            .maxTextColor(Color.TRANSPARENT)
                            .animationDuration(400)
                            .decimals(3)
                            .barBackgroundColor(Color.web("#0e0e0e97"))
                            .shadowsEnabled(quality)
                            .animated(quality)
                            .build();
                    getChildren().add(gas);
                    gas.valueProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
                        Platform.runLater(() -> { 
                            double value = (double)t1;
                            if(value < 4){
                                gas.setBarColor(Color.web("#aceeaa"));
                                gas.setValueColor(Color.web("#aceeaa"));
                            } else if (value < 9){
                                gas.setBarColor(Color.web("#eedaaa"));
                                gas.setValueColor(Color.web("#eedaaa"));
                            } else {
                                gas.setBarColor(Color.web("#eeb0aa"));
                                gas.setValueColor(Color.web("#eeb0aa"));
                            }
                        });
                    });
                    utility.getTodayGasProperty().addListener(gasUpdater);
                    gas.setMouseTransparent(true);
                    Thread waitForItGas = new Thread(){
                        @Override
                        public final void run(){
                            try {
                                Thread.sleep(1000);
                                Platform.runLater(() -> { if(!currentGasUpdated)gas.setValue(utility.getTodayGasProperty().get()); });
                            } catch (InterruptedException ex) {

                            }
                        }

                    };
                    waitForItGas.start();
                break;
            }
            addEventFilter(MouseEvent.MOUSE_CLICKED, this::utilityGraphOpenerHelper);
        } catch (UtilityMeasurementsException ex) {
            ///LOG.error("Could not run utility measurement icon");
        }
    }

    final void wattUpdaterHelper(ObservableValue<? extends Number> ov, Number t, Number t1){
        currentWattUpdated = true;
        Platform.runLater(() -> { 
            watt.setValue((double)t1); 
        });
    }

    final void kwhUpdaterHelper(ObservableValue<? extends Number> ov, Number t, Number t1) {
        currentKwhUpdated = true;
        Platform.runLater(() -> {
            kwh.setValue((double) t1);
        });
    }

    final void waterUpdaterHelper(ObservableValue<? extends Number> ov, Number t, Number t1) {
        currentWaterUpdated = true;
        Platform.runLater(() -> {
            water.setValue((double) t1);
        });
    }

    final void gasUpdaterHelper(ObservableValue<? extends Number> ov, Number t, Number t1) {
        currentGasUpdated = true;
        Platform.runLater(() -> {
            gas.setValue((double) t1);
        });
    }
    
    final void utilityGraphOpenerHelper(MouseEvent t){
        try {
            WindowComponent window = new UtilityMeasurementsDisplay(openerParams);
            WindowManager.openWindow(window, t.getSceneX(), t.getSceneY());
        } catch (UtilityMeasurementsException ex){

        }
    }
    
    @Override
    public void destroy() {
        removeEventFilter(MouseEvent.MOUSE_CLICKED, this::utilityGraphOpenerHelper);
        switch((String)parameters.get(0)){
            case "watt":
                utility.getCurrentWattProperty().removeListener(wattUpdater);
            break;
            case "kwh":
                utility.getTodayKwhProperty().removeListener(kwhUpdater);
            break;
            case "water":
                utility.getTodayWaterProperty().removeListener(waterUpdater);
            break;
            case "gas":
                utility.getTodayGasProperty().removeListener(gasUpdater);
            break;
        }
    }

    @Override
    public void applicationBarWidgetRemoved() {
        this.destroy();
    }
}