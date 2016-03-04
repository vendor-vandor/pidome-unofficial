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

import eu.hansolo.enzo.common.Section;
import eu.hansolo.enzo.gauge.Gauge;
import eu.hansolo.enzo.gauge.GaugeBuilder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.components.mainstage.applicationsbar.ApplicationsBarWidgetIcons;
import org.pidome.client.system.scenes.components.mainstage.applicationsbar.ApplicationsBarWidgetIconsException;
import org.pidome.client.system.scenes.components.mainstage.applicationsbar.widgeticons.hasWidgetIconInterface;

/**
 *
 * @author John
 */
public class UtilityMeasurementGauge extends DragDropLinkPane implements hasWidgetIconInterface {
    
    String renderType = "";
    
    HBox totalKwh;
    HBox totalWater;
    HBox totalGas;
    
    Gauge gauge;
    
    boolean quality = false;
    
    public UtilityMeasurementGauge(String type, String unitName, double thresholdValue){
        this.renderType = type;
        if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){
            quality = true;
        }
        switch(this.renderType){
            case "kwh":
                gauge = buildTotalKwhGauge(unitName, thresholdValue);
            break;
            case "water":
                gauge = buildTotalWaterGauge(unitName, thresholdValue);
            break;
            case "gas":
                gauge = buildTotalGasGauge(unitName, thresholdValue);
            break;
            default:
                gauge = buildCurrentWattGauge(unitName, thresholdValue);
            break;
        }
        gauge.setMaxSize(200*DisplayConfig.getHeightRatio(), 200*DisplayConfig.getWidthRatio());
        gauge.setMouseTransparent(true);
        getChildren().add(gauge);
    }

    public final Gauge getGauge(){
        return gauge;
    }
    
    public final StackPane getPane(){
        return this;
    }
    
    final Gauge buildCurrentWattGauge(String unitName, double thresholdValue){
        double thresholdPart = Math.round(thresholdValue/5);
        return GaugeBuilder.create()
                              .maxSize(200*DisplayConfig.getHeightRatio(), 200*DisplayConfig.getWidthRatio())
                              .startAngle(330)
                              .angleRange(300)
                              .minValue(0)
                              .maxValue(thresholdPart*14)
                              .sections(new Section(0, thresholdPart*4),
                                        new Section(thresholdPart*4, thresholdPart*5),
                                        new Section(thresholdPart*5, thresholdPart*14))
                              .majorTickSpace(thresholdPart)
                              .minorTickSpace(Math.round(thresholdPart/10))
                              .plainValue(false)
                              .tickLabelOrientation(Gauge.TickLabelOrientation.HORIZONTAL)
                              .title("Current")
                              .unit(unitName)
                              .decimals(0)
                              .value(0d)
                              .sectionFill0(Color.web("#aceeaa"))
                              .sectionFill1(Color.web("#eedaaa"))
                              .sectionFill2(Color.web("#eeb0aa"))
                              .animated(quality)
                              .build();
    }
    
    final Gauge buildTotalKwhGauge(String unitName, double thresholdValue){
        double thresholdPart = Math.round(thresholdValue/5);
        return GaugeBuilder.create()
                              .maxSize(200*DisplayConfig.getHeightRatio(), 200*DisplayConfig.getWidthRatio())
                              .startAngle(330)
                              .angleRange(300)
                              .minValue(0)
                              .maxValue(thresholdPart*7)
                              .sections(new Section(0, thresholdPart*4),
                                        new Section(thresholdPart*4, thresholdPart*5),
                                        new Section(thresholdPart*5, thresholdPart*7))
                              .majorTickSpace(thresholdPart)
                              .minorTickSpace(Math.round(thresholdPart/10))
                              .plainValue(false)
                              .tickLabelOrientation(Gauge.TickLabelOrientation.HORIZONTAL)
                              .title("Today")
                              .unit(unitName)
                              .decimals(3)
                              .value(0d)
                              .sectionFill0(Color.web("#aceeaa"))
                              .sectionFill1(Color.web("#eedaaa"))
                              .sectionFill2(Color.web("#eeb0aa"))
                              .animated(quality)
                              .build();
    }
    
    final Gauge buildTotalWaterGauge(String unitName, double thresholdValue){
        double thresholdPart = Math.round(thresholdValue/5);
        double thresholdNormal = new BigDecimal(thresholdPart*4).setScale(1, RoundingMode.HALF_UP).doubleValue();
        double thresholdMiddle = new BigDecimal(thresholdPart*5).setScale(1, RoundingMode.HALF_UP).doubleValue();
        double thresholdUpper = new BigDecimal(thresholdPart*7).setScale(1, RoundingMode.HALF_UP).doubleValue();
        return GaugeBuilder.create()
                              .maxSize(200*DisplayConfig.getHeightRatio(), 200*DisplayConfig.getWidthRatio())
                              .startAngle(330)
                              .angleRange(300)
                              .minValue(0)
                              .maxValue(thresholdUpper)
                              .sections(new Section(0, thresholdNormal),
                                        new Section(thresholdNormal, thresholdMiddle),
                                        new Section(thresholdMiddle, thresholdUpper))
                              .majorTickSpace(thresholdPart)
                              .minorTickSpace(Math.round(thresholdPart/10))
                              .plainValue(false)
                              .tickLabelOrientation(Gauge.TickLabelOrientation.HORIZONTAL)
                              .title("Today")
                              .unit(unitName)
                              .decimals(2)
                              .value(0d)
                              .sectionFill0(Color.web("#aceeaa"))
                              .sectionFill1(Color.web("#eedaaa"))
                              .sectionFill2(Color.web("#eeb0aa"))
                              .animated(quality)
                              .build();
    }
    
    final Gauge buildTotalGasGauge(String unitName, double thresholdValue){
        double thresholdPart = Math.round(thresholdValue/5);
        double thresholdNormal = new BigDecimal(thresholdPart*4).setScale(1, RoundingMode.HALF_UP).doubleValue();
        double thresholdMiddle = new BigDecimal(thresholdPart*5).setScale(1, RoundingMode.HALF_UP).doubleValue();
        double thresholdUpper = new BigDecimal(thresholdPart*7).setScale(1, RoundingMode.HALF_UP).doubleValue();
        return GaugeBuilder.create()
                              .maxSize(200*DisplayConfig.getHeightRatio(), 200*DisplayConfig.getWidthRatio())
                              .startAngle(330)
                              .angleRange(300)
                              .minValue(0)
                              .maxValue(thresholdUpper)
                              .sections(new Section(0, thresholdNormal),
                                        new Section(thresholdNormal, thresholdMiddle),
                                        new Section(thresholdMiddle, thresholdUpper))
                              .majorTickSpace(thresholdPart)
                              .minorTickSpace(Math.round(thresholdPart/10))
                              .plainValue(false)
                              .tickLabelOrientation(Gauge.TickLabelOrientation.HORIZONTAL)
                              .title("Today")
                              .unit(unitName)
                              .decimals(3)
                              .value(0d)
                              .sectionFill0(Color.web("#aceeaa"))
                              .sectionFill1(Color.web("#eedaaa"))
                              .sectionFill2(Color.web("#eeb0aa"))
                              .animated(quality)
                              .build();
    }
    
    public void createWidgetIcon(ApplicationsBarWidgetIcons iconPane) {
        String widgetPath = "org.pidome.client.system.scenes.components.mainstage.applicationsbar.widgeticons.UtilityMeasurementWidgetIcon";
        ArrayList<String> widgetItem = new ArrayList<>();
        switch(this.renderType){
            case "kwh":
                widgetItem.add("kwh");
            break;
            case "water":
                widgetItem.add("water");
            break;
            case "gas":
                widgetItem.add("gas");
            break;
            default:
                widgetItem.add("watt");
            break;
        }
        try {
            iconPane.createIcon(iconPane.getPosition(), widgetPath, widgetItem);
        } catch (ApplicationsBarWidgetIconsException ex) {
            System.out.println("Could not create widget icon: " + ex.getMessage());
        }
    }

    @Override
    public void dragDropDone(Object source) {
        createWidgetIcon((ApplicationsBarWidgetIcons)source);
    }

}
