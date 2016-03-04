/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.scenes.macros_events;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcons;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.macros.Macro;
import org.pidome.client.entities.macros.MacroService;
import org.pidome.client.entities.macros.MacroServiceException;
import org.pidome.client.phone.scenes.BaseScene;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;

/**
 *
 * @author John
 */
public class MacrosEvents extends BaseScene {

    private ObservableArrayListBeanChangeListener<Macro> macroMutator = this::macroMutatorHelper;
    
    private VBox macroContainer = new VBox();
    
    MacroService macroService;
    
    public MacrosEvents() {
        super(true);
        setSceneTitle("Macro's");
        macroContainer.getStyleClass().add("custom-list-view");
    }

    @Override
    public void run() {
        bindMacros();
        this.setContent(macroContainer);
    }

    @Override
    public void stop() {
        if(macroService!=null){
            try {
                macroService.getMacroList().removeListener(macroMutator);
            } catch (MacroServiceException ex) {
                Logger.getLogger(MacrosEvents.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private VBox bindMacros(){
        try {
            macroService = getSystem().getClient().getEntities().getMacroService();
            macroService.getMacroList().addListener(macroMutator);
            macroService.reload();
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(MacrosEvents.class.getName()).log(Level.SEVERE, null, ex);
            macroContainer.getChildren().add(new Label("Not available"));
        } catch (MacroServiceException ex) {
            macroContainer.getChildren().add(new Label("No list available"));
        }
        return macroContainer;
    }
    
    private void macroMutatorHelper(ObservableArrayListBeanChangeListener.Change<? extends Macro> change) {
        if(change.wasAdded()){
            final List<VisualMacro> visualMacroList = new ArrayList<>();
            if(change.hasNext()){
                for(Macro macro:change.getAddedSubList()){
                    if(macro.getIsFavorite()){
                        VisualMacro visualMacro = new VisualMacro(macro.getMacroId(), macro.getName());
                        visualMacro.build(getSystem());
                        visualMacroList.add(visualMacro);
                    }
                }
            }
            Platform.runLater(() -> { 
                macroContainer.getChildren().addAll(visualMacroList);
            });
        } else if (change.wasRemoved()){
            List<VisualMacro> toRemove = new ArrayList<>();
            if(change.hasNext()){
                for(Macro macro:change.getRemoved()){
                    for(Node pane:macroContainer.getChildren()){
                        if(((VisualMacro)pane).getMacroId() == macro.getMacroId()){
                            VisualMacro removePane = (VisualMacro)pane;
                            removePane.destroy();
                            toRemove.add(removePane);
                        }
                    }
                }
            }
            Platform.runLater(() -> { 
                macroContainer.getChildren().removeAll(toRemove);
            });
        }
    }
    
    private static class VisualMacro extends HBox {
        
        private final int macroId;
        private final String macroName;
        
        private VisualMacro(int macroId, String macroName){
            setPrefWidth(Double.MAX_VALUE);
            this.macroId = macroId;
            this.macroName = macroName;
            getStyleClass().addAll("list-item", "undecorated-list-item");
        }
        
        private void build(PCCSystem system){
            Label macroNameVisual = new Label(macroName);
            
            Button runMacro = new Button();
            runMacro.getStyleClass().add("list-item-button");
            runMacro.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.CHEVRON_CIRCLE_RIGHT, "1.4em;"));
            
            StackPane buttonHolder = new StackPane();
            buttonHolder.setAlignment(Pos.CENTER_RIGHT);
            HBox.setHgrow(buttonHolder, Priority.ALWAYS);
            buttonHolder.getChildren().add(runMacro);
            
            getChildren().addAll(macroNameVisual, buttonHolder);
            
            runMacro.addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent) -> {
                MouseEvent.consume();
                try {
                    system.getClient().getEntities().getMacroService().runMacro(this.macroId);
                } catch (EntityNotAvailableException ex) {
                    Logger.getLogger(MacrosEvents.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            
        }
        
        private int getMacroId(){
            return this.macroId;
        }
        
        private void destroy(){
            
        }
        
    }
    
}