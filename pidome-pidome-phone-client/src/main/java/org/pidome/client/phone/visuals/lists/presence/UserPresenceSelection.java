/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.visuals.lists.presence;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcons;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.presences.Presence;
import org.pidome.client.entities.presences.PresenceService;
import org.pidome.client.entities.users.User;
import org.pidome.client.entities.users.UserServiceException;
import org.pidome.client.phone.scenes.menus.ParentClosable;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class UserPresenceSelection extends VBox {

    private final PresenceService presenceService;
    private final ParentClosable parentClosable;
    private final User me;
    private final PCCSystem system;
    
    private final ToggleGroup presenceGroup = new ToggleGroup();
    
    public UserPresenceSelection(PresenceService presenceService, PCCSystem system, User me, ParentClosable parentClosable) {
        super();
        this.parentClosable = parentClosable;
        this.presenceService = presenceService;
        this.me = me;
        this.system = system;
        getStyleClass().add("custom-list-view");
    }
    
    public void setItems() {
        try {
            
            if(this.me!=null){
                Iterator<Presence> list = presenceService.getPresenceList().iterator();
                if(list.hasNext()){
                    while(list.hasNext()) {
                        
                        Presence presence = list.next();
                        
                        RadioButton selected = new RadioButton(presence.getName().getValueSafe());
                        selected.setPrefWidth(Double.MAX_VALUE);
                        selected.getStyleClass().add("list-item");
                        selected.setToggleGroup(presenceGroup);
                        
                        if(this.me.getCurrentPresenceId()==presence.getPresenceId()){
                            selected.setSelected(true);
                        }

                        selected.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
                            this.parentClosable.closeChild(this);
                            try {
                                system.getClient().getEntities().getUserService().setPresence(presence.getPresenceId());
                            } catch (EntityNotAvailableException | UserServiceException ex) {
                                Logger.getLogger(UserPresenceSelection.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        });
                        getChildren().add(selected);
                    }
                } else {
                    Label visualMenuItem = new Label("Could not load presences");
                    visualMenuItem.setMinWidth(Double.MAX_VALUE);
                    visualMenuItem.getStyleClass().add("menu-item");
                    this.getChildren().add(visualMenuItem);
                }
            } else {
                HBox nono = new HBox(5);
                nono.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
                    this.parentClosable.closeChild(this);
                });
                nono.setMinWidth(1);
                Label nonoHeader = new Label("Unsupported device");
                nonoHeader.setMinWidth(Double.MAX_VALUE);
                nono.getStyleClass().addAll("menu-item");
                nono.getChildren().addAll(GlyphsDude.createIcon(FontAwesomeIcons.EXCLAMATION, "2em;"),nonoHeader);
                
                getChildren().add(nono);
            }
        } catch (Exception ex) {
            Logger.getLogger(UserPresenceSelection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void destroy() {
        this.getChildren().clear();
    }
    
}