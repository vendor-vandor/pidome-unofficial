/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.scenes;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcons;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.pidome.client.phone.scenes.menus.MainMenu;
import org.pidome.client.phone.scenes.menus.MenuActionReceiver;
import org.pidome.client.phone.scenes.menus.MenuBase;
import org.pidome.client.phone.scenes.visuals.SceneBackHandler;
import org.pidome.client.phone.services.ServiceConnector;
import org.pidome.client.phone.visuals.interfaces.Destroyable;
import org.pidome.client.phone.visuals.panes.ItemPane;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class SceneHeader extends ItemPane implements MenuActionReceiver {

    HBox content = new HBox();
    
    private PCCSystem system;
    private ServiceConnector serviceConnector;
    
    private final Button applicationMenuButton = new Button();
    private final Text applicationMenuText = GlyphsDude.createIcon(FontAwesomeIcons.ELLIPSIS_V, "2em;");
    
    private final PositionedPane baseScene;
    
    Label sceneTitle = new Label();
    
    HBox titleContainer = new HBox();
    
    MainMenu mainMenu ;
    
    EventHandler<ActionEvent> backEvent;
    Button backButton = new Button();
    
    Button mainMenuButton = new Button();
    
    public SceneHeader(BaseScene baseScene) {
        super();
        this.baseScene = baseScene;
        getStyleClass().add("app-header");
        content.getStyleClass().add("content");
        
        StackPane AppMenu = new StackPane();
        HBox.setHgrow(AppMenu, Priority.ALWAYS);
        AppMenu.setAlignment(Pos.CENTER_RIGHT);
        
        backButton.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.ANGLE_LEFT, "2em;"));
        backButton.getStyleClass().add("back");
        
        mainMenuButton.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.BARS, "2em;"));
        mainMenuButton.getStyleClass().add("quickmenu");
        mainMenuButton.setOnAction((MouseEvent) -> {
            baseScene.showLeftMenu();
        });
        
        applicationMenuText.getStyleClass().add("app-menu");
        applicationMenuButton.setGraphic(applicationMenuText);
        applicationMenuButton.getStyleClass().add("menu");
        AppMenu.getChildren().add(applicationMenuButton);
        
        sceneTitle.getStyleClass().add("scene-header-text");
        titleContainer.getChildren().add(sceneTitle);
        content.getChildren().addAll(titleContainer, AppMenu);
        this.setContent(content);
    }
    
    public final void setSceneTitle(String title){
        sceneTitle.setText(title);
        sceneTitle.getStyleClass().remove("scene-header-text-padding");
        Platform.runLater(() -> { 
            titleContainer.getChildren().clear();
            titleContainer.getChildren().addAll(mainMenuButton,sceneTitle);
        });
    }
    
    public final void setSceneBackTitle(final SceneBackHandler handler, final String id, String title){
        sceneTitle.getStyleClass().add("scene-header-text-padding");
        sceneTitle.setText(title);
        backEvent = (MouseEvent) -> {
            handler.handleSceneBack(id);
        };
        Platform.runLater(() -> { 
            titleContainer.getChildren().clear();
            titleContainer.getChildren().addAll(backButton, sceneTitle);
            backButton.setOnAction(backEvent);
        });
    }
    
    @Override
    public void closeMenu(MenuBase menu) {
        if(this.baseScene.hasPositioned(menu)){
            this.baseScene.closePositioned(menu);
            ((Destroyable)menu).destroy();
        }
    }
    
    public final void setSystem(PCCSystem system, ServiceConnector serviceConnector){
        this.system = system;
        this.serviceConnector = serviceConnector;
        buildMenus();
    }
    
    /**
     * Menu's can be build when specific connectors are present.
     */
    private void buildMenus(){
        
        mainMenu = new MainMenu(serviceConnector, baseScene,this);
        mainMenu.setItems();
        
        applicationMenuButton.addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent) -> {
            MouseEvent.consume();
            if(!this.baseScene.hasPositioned(mainMenu)){
                this.baseScene.openAtPosition(getWidth()-(mainMenu.getWidth()+10), (getHeight()/8)*7, mainMenu);
            } else {
                this.baseScene.closePositioned(mainMenu);
            }
            mainMenu.widthProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
                mainMenu.setTranslateX(getWidth()-(mainMenu.getWidth()+10));
            });
        });
        
    }
    
    @Override
    public void destroy() {

    }

    @Override
    public void handleClickedMainMenuItem(MainMenu.MainMenuItem menuItem) {
        switch(menuItem){
            case PRESENCE_SETTINGS:
                
            break;
            case GLOBAL_SETTINGS:
                
            break;
        }
    }

}