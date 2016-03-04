/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.presences;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.users.User;
import org.pidome.client.entities.users.UserService;
import org.pidome.client.entities.users.UserServiceException;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public class PresenceComposer extends TilePane {
    
    private PCCSystem system;
    
    private double gap = 10;
    
    protected PresenceComposer(){
        setHgap(gap);
        setVgap(gap);
        this.setAlignment(Pos.TOP_CENTER);
    }
    
    private UserService userService;
    private ReadOnlyObservableArrayListBean<User> userList;
    private ObservableArrayListBeanChangeListener<User> userListHelper = this::userListChanged;
    
    private void userListChanged(ObservableArrayListBeanChangeListener.Change<? extends User> change){
        if(change.wasAdded()){
            if(change.hasNext()){
                final List<VisualUser> visualUsersList = new ArrayList<>();
                if(change.hasNext()){
                    for(User user:change.getAddedSubList()){
                        if(!user.getUserName().equals("admin")){
                            visualUsersList.add(new VisualUser(user));
                        }
                    }
                }
                Platform.runLater(() -> {
                    getChildren().addAll(visualUsersList);
                });
            }
            if(change.wasRemoved()){
                List<VisualUser> toRemove = new ArrayList<>();
                if(change.hasNext()){
                    for(User user:change.getRemoved()){
                        for(Node pane:getChildren()){
                            if(((VisualUser)pane).getUser().equals(user)){
                                VisualUser removePane = (VisualUser)pane;
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
    
    private static class VisualUser extends VBox {

        private static double uImageSize = 60;
        
        private Text personPresent = GlyphsDude.createIcon(MaterialDesignIcon.ACCOUNT, String.valueOf(uImageSize));
        private Text personAway = GlyphsDude.createIcon(MaterialDesignIcon.ACCOUNT_OUTLINE, String.valueOf(uImageSize));
        private PropertyChangeListener statusChanged = this::propertyChange;
        
        User user;
        
        StackPane imageContainer = new StackPane();
        
        private VisualUser(User user){
            this.user = user;
            setAlignment(Pos.CENTER);
            Text name = new Text(user.getFirstName().getValueSafe());
            name.setWrappingWidth(uImageSize + 10);
            name.setTextAlignment(TextAlignment.CENTER);
            name.getStyleClass().add("text");
            
            imageContainer.setPadding(new Insets(10));
            imageContainer.setAlignment(Pos.CENTER);
            imageContainer.getStyleClass().add("visual-user-container");
            
            Circle mask = new Circle();
            mask.setTranslateX((uImageSize / 2) + imageContainer.getPadding().getLeft()); ////include the insets
            mask.setTranslateY((uImageSize / 2) + imageContainer.getPadding().getTop()); ////include the insets
            mask.setRadius(uImageSize / 2);
            imageContainer.setClip(mask);
            
            getChildren().addAll(imageContainer, name);
            
            if(user.getPresent().getValue()==true){
                imageContainer.getChildren().add(personPresent);
                imageContainer.getStyleClass().add("present");
            } else {
                imageContainer.getChildren().add(personAway);
                imageContainer.getStyleClass().add("away");
            }
            this.user.getPresent().addPropertyChangeListener(statusChanged);
        }
        
        private void propertyChange(PropertyChangeEvent evt) {
            Platform.runLater(() -> {
                if((boolean)evt.getNewValue()==true){
                    imageContainer.getChildren().remove(personAway);
                    imageContainer.getChildren().add(personPresent);
                    imageContainer.getStyleClass().remove("away");
                    imageContainer.getStyleClass().add("present");
                } else {
                    imageContainer.getChildren().remove(personPresent);
                    imageContainer.getChildren().add(personAway);
                    imageContainer.getStyleClass().remove("present");
                    imageContainer.getStyleClass().add("away");
                }
            });
        }
        
        private User getUser(){
            return this.user;
        }
        
        private void destroy(){
            user.getPresent().removePropertyChangeListener(statusChanged);
        }
        
    }
    
    public void start() {
        try {
            userService = this.system.getClient().getEntities().getUserService();
            this.userList = userService.getUserList();
            this.userList.addListener(userListHelper);
            userService.reload();
        } catch (EntityNotAvailableException | UserServiceException ex) {
            Logger.getLogger(PresenceComposer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close() {
        this.userList.removeListener(userListHelper);
        for(Node pane:getChildren()){
            if(pane instanceof VisualUser){
                ((VisualUser)pane).destroy();
            }
        }
    }
    
    public void setSystem(PCCSystem system, ServiceConnector connector) {
        this.system = system;
    }

    public void removeSystem() {
        this.system = null;
    }
    
}