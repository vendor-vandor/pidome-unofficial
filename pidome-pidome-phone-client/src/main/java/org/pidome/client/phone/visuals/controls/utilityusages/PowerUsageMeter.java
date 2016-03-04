/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.visuals.controls.utilityusages;

import eu.hansolo.enzo.gauge.OneEightyGauge;
import eu.hansolo.enzo.gauge.OneEightyGaugeBuilder;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.pidome.client.entities.plugins.utilityusages.UtilitiesPowerUsage;

/**
 *
 * @author John
 */
public class PowerUsageMeter extends StackPane {
    
    private double currentValue = 0;
    private final OneEightyGauge power;
    private final UtilitiesPowerUsage usage;
    
    public PowerUsageMeter(UtilitiesPowerUsage usage){
        getStyleClass().add("utilities-power-meter");
        this.usage = usage;
        power = OneEightyGaugeBuilder.create()
                                     .unit(usage.getTodayKwhName())
                                     .maxValue((usage.getThreshold().getValue()/5)*7)
                                     .animated(true)
                                     .decimals(2)
                                     .build(); 
        getChildren().add(power);
    }
    
    protected final void setValue(double value){
        Paint barColor;
        double thresholdDevided = usage.getThreshold().getValue()/5;
        if(value < thresholdDevided*4){
            barColor = Color.GREEN;
        } else if (value < thresholdDevided*5){
            barColor = Color.ORANGE;
        } else {
            barColor = Color.RED;
        }
        Platform.runLater(() -> {
            power.setValue(value);
            power.setBarColor(barColor);
        });
    }
    
}