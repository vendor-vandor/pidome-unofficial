/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.scenes;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcons;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.presences.PresenceService;
import org.pidome.client.entities.scenes.Scene;
import org.pidome.client.entities.scenes.ScenesService;
import org.pidome.client.entities.scenes.ScenesServiceException;
import org.pidome.client.entities.users.User;
import org.pidome.client.entities.users.UserService;
import org.pidome.client.entities.users.UserServiceException;
import org.pidome.client.phone.scenes.dashboard.Dashboard;
import org.pidome.client.phone.scenes.menus.ParentClosable;
import org.pidome.client.phone.visuals.lists.presence.UserPresenceSelection;
import org.pidome.client.phone.scenes.visuals.DialogBox;
import org.pidome.client.phone.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public class QuickViewMenu extends VBox implements ParentClosable {

    private final PCCSystem system;
    private final BaseScene baseScene;
    
    private final Button menuCloseButton = new Button();
    
    private final Button personPresenceButton = new Button();
    private Text personPresenceText = GlyphsDude.createIcon(FontAwesomeIcons.EYE_SLASH, "2em;");
    private Label presenceHeaderName = new Label("Presence");
    
    private PresenceService presenceService;
    private final PropertyChangeListener presenceChanger = this::presenceChanged;
    private static User me;
    
    private UserService userService;
    private ReadOnlyObservableArrayListBean<User> userList;
    private ObservableArrayListBeanChangeListener<User> userListHelper = this::userListChanged;
    private HBox usersBox = new HBox();
    
    private ScenesService scenesService;
    private ReadOnlyObservableArrayListBean<Scene> scenesList;
    private ObservableArrayListBeanChangeListener<Scene> sceneListHelper = this::sceneListChanged;
    private VBox scenesBox = new VBox();
    
    UserPresenceSelection presenceMenu;
    DialogBox presencePopup;
    
    private final ServiceConnector service;
    
    private boolean currentPresent = false;
    private final PropertyChangeListener distanceChanger = this::distanceChanged;
    
    QuickViewMenu(ScenesSwitcher switcher, BaseScene baseScene, PCCSystem system, ServiceConnector service){
        this.system = system;
        this.baseScene = baseScene;
        this.service = service;
        
        StackPane.setAlignment(this, Pos.TOP_LEFT);
        getStyleClass().add("top-left-menu");
        
        HBox menuHeaderBox = new HBox();
        menuHeaderBox.setPrefWidth(Double.MAX_VALUE);
        menuHeaderBox.getStyleClass().add("header");
        
        Label menuHeader = new Label("Menu");
        menuHeader.getStyleClass().add("title");
        
        Text menuIcon = GlyphsDude.createIcon(FontAwesomeIcons.BARS, "2em;");
        
        StackPane closeHolder = new StackPane();
        Text closeIcon = GlyphsDude.createIcon(FontAwesomeIcons.CHEVRON_LEFT, "2em;");
        menuCloseButton.getStyleClass().add("close-icon");
        menuCloseButton.setGraphic(closeIcon);
        closeHolder.getChildren().add(menuCloseButton);
        HBox.setHgrow(closeHolder, Priority.ALWAYS);
        closeHolder.setAlignment(Pos.CENTER_RIGHT);
        
        closeIcon.addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent) -> {
            MouseEvent.consume();
            baseScene.hideLeftMenu();
        });
        
        menuHeaderBox.getChildren().addAll(menuIcon,menuHeader,closeHolder);
        
        
        HBox sceneHeaderBox = new HBox();
        sceneHeaderBox.setPrefWidth(Double.MAX_VALUE);
        sceneHeaderBox.getStyleClass().addAll("header", "sub");
        
        Label sceneHeader = new Label("Scenes");
        sceneHeader.getStyleClass().add("title");
        Text scenesIcon = GlyphsDude.createIcon(FontAwesomeIcons.UNIVERSITY, "2em;");
        sceneHeaderBox.getChildren().addAll(scenesIcon,sceneHeader);
        
        HBox presenceHeaderBox = new HBox();
        presenceHeaderBox.setPrefWidth(Double.MAX_VALUE);
        presenceHeaderBox.getStyleClass().addAll("header", "sub");
        presenceHeaderName.getStyleClass().add("title");
        
        personPresenceText.getStyleClass().add("home-personal");
        personPresenceButton.setGraphic(personPresenceText);
        personPresenceButton.getStyleClass().add("presence");
        
        presenceHeaderBox.getChildren().addAll(personPresenceButton, presenceHeaderName);
        
        StackPane fillEmptySpace = new StackPane();
        fillEmptySpace.getChildren().add(this.scenesBox);
        VBox.setVgrow(fillEmptySpace, Priority.ALWAYS);
        
        getChildren().addAll(menuHeaderBox, new EntitiesNavigationMenu(switcher, ScenesHandler.getCurrentScenePane()), sceneHeaderBox, fillEmptySpace, presenceHeaderBox, usersBox);
        setOnSwipeLeft((SwipeEvent event) -> {
            event.consume();
            baseScene.hideLeftMenu();
        });
        bindPresence();
        bindScenes();
        try {
            this.service.getLocalizationService().getCurrentDistanceProperty().addPropertyChangeListener(distanceChanger);
        } catch (UnsupportedOperationException ex){
            /// Not supported on all platforms.
        }
        
    }
    
    private void bindScenes(){
        try {
            scenesService = this.system.getClient().getEntities().getScenesService();
            this.scenesList = scenesService.getScenesList();
            this.scenesList.addListener(sceneListHelper);
            scenesService.reload();
        } catch (ScenesServiceException | EntityNotAvailableException ex) {
            Logger.getLogger(QuickViewMenu.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    private void sceneListChanged(ObservableArrayListBeanChangeListener.Change<? extends Scene> change){
        if(change.wasAdded()){
            if(change.hasNext()){
                final List<VisualScene> visualScenesList = new ArrayList<>();
                if(change.hasNext()){
                    for(Scene scene:change.getAddedSubList()){
                        visualScenesList.add(new VisualScene(scene));
                    }
                }
                Platform.runLater(() -> {
                    scenesBox.getChildren().addAll(visualScenesList);
                });
            }
            if(change.wasRemoved()){
                List<VisualScene> toRemove = new ArrayList<>();
                if(change.hasNext()){
                    for(Scene scene:change.getRemoved()){
                        for(Node pane:scenesBox.getChildren()){
                            if(((VisualScene)pane).getSceneId() == scene.getSceneId()){
                                VisualScene removePane = (VisualScene)pane;
                                removePane.destroy();
                                toRemove.add(removePane);
                            }
                        }
                    }
                }
                Platform.runLater(() -> { 
                    scenesBox.getChildren().removeAll(toRemove);
                });
            }
        }
    }
    
    private void bindUserStatus(){
        try {
            this.userList = userService.getUserList();
            this.userList.addListener(userListHelper);
            userService.reload();
        } catch (UserServiceException | EntityNotAvailableException ex) {
            Logger.getLogger(QuickViewMenu.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
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
                    usersBox.getChildren().addAll(visualUsersList);
                });
            }
            if(change.wasRemoved()){
                List<VisualUser> toRemove = new ArrayList<>();
                if(change.hasNext()){
                    for(User user:change.getRemoved()){
                        for(Node pane:usersBox.getChildren()){
                            if(((VisualUser)pane).getUser().equals(user)){
                                VisualUser removePane = (VisualUser)pane;
                                removePane.destroy();
                                toRemove.add(removePane);
                            }
                        }
                    }
                }
                Platform.runLater(() -> { 
                    usersBox.getChildren().removeAll(toRemove);
                });
            }
        }
    }
    
    private void bindPresence(){
        try {
            presenceService = this.system.getClient().getEntities().getPresenceService();
            userService = this.system.getClient().getEntities().getUserService();
            startPresenceLoader();
            bindUserStatus();
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(QuickViewMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void startPresenceLoader(){
        new Thread() {
            @Override
            public final void run() {
                try {
                    presenceService.reload();
                    try {
                        me = userService.getMyData();
                        me.getPresent().addPropertyChangeListener(presenceChanger);
                        String presenceText;
                        if(me.getPresent().getValue()){
                            presenceText = "At home";
                            currentPresent = true;
                            personPresenceText = GlyphsDude.createIcon(FontAwesomeIcons.HOME, "2em;");
                            personPresenceText.getStyleClass().add("home-personal");
                        } else {
                            presenceText = "Away";
                            currentPresent = false;
                            personPresenceText = GlyphsDude.createIcon(FontAwesomeIcons.ROAD, "2em;");
                            personPresenceText.getStyleClass().add("away-personal");
                        }
                        Platform.runLater(() -> {
                            presenceHeaderName.setText(presenceText);
                            personPresenceButton.setGraphic(personPresenceText);
                        });
                    } catch (UserServiceException ex) {
                        Logger.getLogger(SceneHeader.class.getName()).log(Level.SEVERE, "Personal not available", ex);
                    }
                    presencePopup = new DialogBox("Set your presence");
                    if(presenceMenu == null){
                        presenceMenu = new UserPresenceSelection(presenceService, system, me, QuickViewMenu.this);
                        presenceMenu.setItems();
                    }
                    presencePopup.setContent(presenceMenu);
                    presencePopup.setButtons(new DialogBox.PopUpButton[]{new DialogBox.PopUpButton("CANCEL", "Cancel")});
                    presencePopup.addListener((String buttonId) -> {
                        baseScene.closePopup(presencePopup);
                    });

                    personPresenceButton.addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent) -> {
                        if(!baseScene.hasPopup(presencePopup)){
                            baseScene.showPopup(presencePopup);
                        }
                    });

                    presencePopup.build();
                } catch (EntityNotAvailableException ex) {
                    Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }
    
    private void distanceChanged(PropertyChangeEvent evt){
        Platform.runLater(() -> {
                if(!currentPresent){
                    try {
                        presenceHeaderName.setText("Away: " + ((double)evt.getNewValue()/1000) + " Km.");
                    } catch (Exception ex){
                        presenceHeaderName.setText("Away");
                    }
                } else {
                    presenceHeaderName.setText("At home");
                }
        });
    }
    
    private void presenceChanged(PropertyChangeEvent evt){
        currentPresent = (boolean)evt.getNewValue();
        Platform.runLater(() -> {
            if((boolean)evt.getNewValue()==true){
                presenceHeaderName.setText("At home");
                personPresenceText = GlyphsDude.createIcon(FontAwesomeIcons.HOME, "2em;");
                personPresenceText.getStyleClass().remove("away-personal");
                personPresenceText.getStyleClass().add("home-personal");
                personPresenceButton.setGraphic(personPresenceText);
            } else {
                try {
                    if(this.service.getLocalizationService().GPSEnabled()){
                        presenceHeaderName.setText("Away: " + this.service.getLocalizationService().getCurrentDistanceProperty().getValue() + " Km.");
                    } else {
                        presenceHeaderName.setText("Away");
                    }
                } catch (UnsupportedOperationException ex){
                    presenceHeaderName.setText("Away");
                }
                personPresenceText = GlyphsDude.createIcon(FontAwesomeIcons.ROAD, "2em;");
                personPresenceText.getStyleClass().remove("home-personal");
                personPresenceText.getStyleClass().add("away-personal");
                personPresenceButton.setGraphic(personPresenceText);
            }
        });
    }
    
    public final void destroy(){
        if(this.userList!=null){
            this.userList.removeListener(userListHelper);
        }
        try {
            this.service.getLocalizationService().getCurrentDistanceProperty().removePropertyChangeListener(distanceChanger);
        } catch (UnsupportedOperationException ex){
            //// Not supported on all platforms.
        }
        if(me!=null){
            me.getPresent().removePropertyChangeListener(presenceChanger);
        }
        this.scenesList.removeListener(sceneListHelper);
    }

    @Override
    public void closeChild(Node node) {
        if(node instanceof UserPresenceSelection){
            baseScene.closePopup(presencePopup);
        }
    }
    
    private class VisualScene extends HBox {
        
        private Scene scene;
        
        private Label nameLabel  = new Label();
        private Text sceneActive = GlyphsDude.createIcon(FontAwesomeIcons.EYE, "2em;");
        
        EventHandler<MouseEvent> sceneEvent;
        
        private final PropertyChangeListener sceneActiveChanger = this::sceneActiveChanged;
        
        VisualScene(Scene scene){
            getStyleClass().add("visual-scene");
            this.scene = scene;
            sceneActive.getStyleClass().add("visual-scene-icon");
            nameLabel.getStyleClass().add("visual-scene-name");
            nameLabel.setText(scene.getSceneName().getValueSafe());
            sceneEvent = (MouseEvent) -> {
                if(this.scene.getSceneActive().getValue()){
                    scenesService.deActivateScene(this.scene);
                } else {
                    scenesService.activateScene(this.scene);
                }
            };
            addEventHandler(MouseEvent.MOUSE_CLICKED, sceneEvent);
            getChildren().addAll(sceneActive,nameLabel);
            setSceneActive(this.scene.getSceneActive().getValue());
            this.scene.getSceneActive().addPropertyChangeListener(sceneActiveChanger);
        }
        
        private int getSceneId(){
            return this.scene.getSceneId();
        }
        
        private void destroy(){
            removeEventHandler(MouseEvent.MOUSE_CLICKED, sceneEvent);
            this.scene.getSceneActive().removePropertyChangeListener(sceneActiveChanger);
        }
        
        private void sceneActiveChanged(PropertyChangeEvent evt){
            Platform.runLater(() -> {
                setSceneActive((boolean)evt.getNewValue());
            });
        }
        
        private void setSceneActive(boolean setActive){
            if(setActive){
                sceneActive.getStyleClass().add("active");
            } else {
                sceneActive.getStyleClass().remove("active");
            }
        }
        
    }
    
    private static class VisualUser extends VBox {
        
        private Text person = GlyphsDude.createIcon(FontAwesomeIcons.MALE, "2.4em;");
        private PropertyChangeListener statusChanged = this::propertyChange;
        
        User user;
        
        VisualUser(User user){
            getStyleClass().add("visual-user-icon");
            this.user = user;
            setAlignment(Pos.CENTER);
            Text name = new Text(user.getFirstName().getValueSafe());
            name.getStyleClass().add("text");
            getChildren().addAll(person, name);
            if(user.getPresent().getValue()==true){
                person.getStyleClass().add("present");
            }
            this.user.getPresent().addPropertyChangeListener(statusChanged);
        }
        
        private void propertyChange(PropertyChangeEvent evt) {
            Platform.runLater(() -> {
                if((boolean)evt.getNewValue()==true){
                    person.getStyleClass().remove("away");
                    person.getStyleClass().add("present");
                } else {
                    person.getStyleClass().remove("present");
                    person.getStyleClass().add("away");
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
    
}