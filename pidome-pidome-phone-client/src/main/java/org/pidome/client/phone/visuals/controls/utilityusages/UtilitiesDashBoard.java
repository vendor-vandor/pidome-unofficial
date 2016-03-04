/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.visuals.controls.utilityusages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.pidome.client.entities.plugins.utilityusages.UtilityPlugin;
import org.pidome.client.phone.visuals.panes.ItemPane;

/**
 *
 * @author John
 */
public class UtilitiesDashBoard extends ItemPane {

    HBox horizontal = new HBox();
    VBox vertical = new VBox();
    
    private final WaterUsageMeter water;
    private final GasUsageMeter gas;
    private final PowerUsageMeter power;
    
    UtilityPlugin plugin;
    
    PropertyChangeListener powerUpdater = this::updatePower;
    PropertyChangeListener waterUpdater = this::updateWater;
    PropertyChangeListener gasUpdater   = this::updateGas;
    
    public UtilitiesDashBoard(UtilityPlugin plugin){
        super("Utility usages today");
        this.plugin = plugin;
        
        power = new PowerUsageMeter(this.plugin.getPower());
        water = new WaterUsageMeter(this.plugin.getWater()); 
        gas   = new GasUsageMeter(this.plugin.getGas());
                
        horizontal.getStyleClass().add("utilities-dashboard");
        vertical.getChildren().addAll(water, gas);
        horizontal.getChildren().addAll(power,vertical);
        setContent(horizontal);
        
    }
    
    public void start(){
        power.setValue(plugin.getPower().getTodayKwhUsage().getValue());
        plugin.getPower().getTodayKwhUsage().addPropertyChangeListener(powerUpdater);
        
        water.setValue(plugin.getWater().getTodayWaterUsage().getValue());
        plugin.getWater().getTodayWaterUsage().addPropertyChangeListener(waterUpdater);

        gas.setValue(plugin.getGas().getTodayGasUsage().getValue());
        plugin.getGas().getTodayGasUsage().addPropertyChangeListener(gasUpdater);
    }

    private void updatePower(PropertyChangeEvent evt){
        power.setValue((double)evt.getNewValue());
    }
    
    private void updateGas(PropertyChangeEvent evt){
        gas.setValue((double)evt.getNewValue());
    }
    
    private void updateWater(PropertyChangeEvent evt){
        water.setValue((double)evt.getNewValue());
    }
    
    @Override
    public void destroy() {
        plugin.getPower().getTodayKwhUsage().removePropertyChangeListener(powerUpdater);
        plugin.getWater().getTodayWaterUsage().removePropertyChangeListener(waterUpdater);
        plugin.getGas().getTodayGasUsage().removePropertyChangeListener(gasUpdater);
        plugin = null;
    }
    
}
