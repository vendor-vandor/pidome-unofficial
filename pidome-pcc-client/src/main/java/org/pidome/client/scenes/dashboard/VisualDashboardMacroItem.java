/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.dashboard.DashboardItem;
import org.pidome.client.entities.dashboard.DashboardMacroItem;
import org.pidome.client.entities.macros.Macro;
import org.pidome.client.entities.macros.MacroServiceException;
import org.pidome.client.scenes.dashboard.svg.macros.MacroBG;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public final class VisualDashboardMacroItem extends VisualDashboardItem {

    private final PropertyChangeListener macroRunningListener = this::macroRunning;
    private Macro macro;
    private final Text macroNameText = new Text("");
    
    protected VisualDashboardMacroItem(PCCSystem system, DashboardItem item) {
        super(system, item);
        this.getStyleClass().add("dashboard-macro");
        setBackGround(new MacroBG());
        macroNameText.getStyleClass().add("macroname");
        macroNameText.setWrappingWidth(this.getPaneWidth());
        macroNameText.setTextAlignment(TextAlignment.CENTER);
        getChildren().add(macroNameText);
    }
    
    private void macroRunning(PropertyChangeEvent evt){
        if((boolean)evt.getNewValue()==true){
            Platform.runLater(() -> {
                this.getStyleClass().add("active");
            });
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                     Platform.runLater(() -> {
                         VisualDashboardMacroItem.this.getStyleClass().remove("active");
                     });
                }
            }, 500);
        }
    }
    
    @Override
    protected void build() {
        try {
            macro = getSystem().getClient().getEntities().getMacroService().getMacro(((DashboardMacroItem)this.getDashboardItem()).getMacroId());
            Platform.runLater(() -> { macroNameText.setText(macro.getName()); });
            this.setOnMouseClicked((MouseEvent me) -> {
                macro.runMacro();
            });
            macro.getRunning().addPropertyChangeListener(macroRunningListener);
        } catch (EntityNotAvailableException | MacroServiceException ex) {
            Logger.getLogger(VisualDashboardMacroItem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void destruct() {
        macro.getRunning().removePropertyChangeListener(macroRunningListener);
    }
    
}
