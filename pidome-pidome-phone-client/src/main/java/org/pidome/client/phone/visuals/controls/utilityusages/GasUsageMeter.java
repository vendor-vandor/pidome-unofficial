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
import org.pidome.client.entities.plugins.utilityusages.UtilitiesGasUsage;

/**
 *
 * @author John
 */
public class GasUsageMeter extends StackPane {
    
    private double currentValue = 0;
    private final OneEightyGauge gas;
    
    private final UtilitiesGasUsage usage;
    
    public GasUsageMeter(UtilitiesGasUsage usage){
        getStyleClass().add("utilities-gas-meter");
        this.usage = usage;
        gas = OneEightyGaugeBuilder.create()
                                     .unit(usage.getTodayGasName())
                                     .maxValue((usage.getThreshold().getValue()/5)*7)
                                     .barColor(Color.GREEN)
                                     .animated(true)
                                     .decimals(2)
                                     .build();
        getChildren().add(gas);
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
            gas.setValue(value);
            gas.setBarColor(barColor);
        });
    }
    
}