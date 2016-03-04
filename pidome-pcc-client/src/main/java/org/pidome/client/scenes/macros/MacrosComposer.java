/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.macros;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.macros.Macro;
import org.pidome.client.entities.macros.MacroService;
import org.pidome.client.entities.macros.MacroServiceException;
import org.pidome.client.scenes.media.MediaLargeScene;
import org.pidome.client.scenes.presences.PresenceComposer;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public class MacrosComposer extends VBox {
    
    private PCCSystem system;
    
    private MacroService macroService;
    private ReadOnlyObservableArrayListBean<Macro> macrosList;
    private ObservableArrayListBeanChangeListener<Macro> macrosListHelper = this::macrosListChanged;
    
    protected MacrosComposer(){
        this.getStyleClass().add("custom-list-view");
    }
    
    public void start() {
        try {
            macroService = this.system.getClient().getEntities().getMacroService();
            this.macrosList = macroService.getMacroList();
            this.macrosList.addListener(macrosListHelper);
            macroService.reload();
        } catch (EntityNotAvailableException | MacroServiceException ex) {
            Logger.getLogger(PresenceComposer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close() {
        this.macrosList.removeListener(macrosListHelper);
        for(Node pane:getChildren()){
            if(pane instanceof VisualMacro){
                ((VisualMacro)pane).destroy();
            }
        }
    }
    
    private void macrosListChanged(ObservableArrayListBeanChangeListener.Change<? extends Macro> change){
        if(change.wasAdded()){
            if(change.hasNext()){
                final List<VisualMacro> visualMacrosList = new ArrayList<>();
                if(change.hasNext()){
                    for(Macro macro:change.getAddedSubList()){
                        visualMacrosList.add(new VisualMacro(macro));
                    }
                }
                Collections.sort(visualMacrosList, (VisualMacro arg0, VisualMacro arg1) -> arg0.getName().compareToIgnoreCase(arg1.getName()));
                Platform.runLater(() -> {
                    getChildren().setAll(visualMacrosList);
                });
            }
            if(change.wasRemoved()){
                final List<VisualMacro> toRemove = new ArrayList<>();
                if(change.hasNext()){
                    for(Macro macro:change.getRemoved()){
                        for(Node pane:getChildren()){
                            if(((VisualMacro)pane).getPiDomeMacro().equals(macro)){
                                VisualMacro removePane = (VisualMacro)pane;
                                removePane.destroy();
                                toRemove.add(removePane);
                            }
                        }
                    }
                }
                Platform.runLater(() -> { 
                    getChildren().removeAll(toRemove);
                });
            }
        }
    }
    
    public void setSystem(PCCSystem system, ServiceConnector connector) {
        this.system = system;
    }

    public void removeSystem() {
        this.system = null;
    }
    
    protected static class VisualMacro extends HBox {
        
        private Macro macro;
        Text macroName = new Text();
        
        Text macroIcon = GlyphsDude.createIcon(MaterialDesignIcon.PLAY, "1.1em");
        
        protected VisualMacro(Macro macro){
            super(5);
            this.getStyleClass().addAll("list-item", "macro-item");
            this.macro = macro;
            macroName.setText(macro.getName());
            build();
            addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent) -> {
                macro.runMacro();
            });
        }
        
        public final String getName(){
            return this.macroName.getText();
        }
        
        private void build(){
            macroIcon.getStyleClass().add("icon");
            macroName.getStyleClass().add("text");
            this.getChildren().addAll(macroIcon, macroName);
        }
        
        protected final void destroy(){
            this.macro = null;
        }
        
        protected final Macro getPiDomeMacro(){
            return this.macro;
        }
        
    }
    
}