/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.menus;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import org.pidome.client.PiDomeClient;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.scenes.ScenesHandler.ScenePane;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.services.ServiceConnector.DisplayType;
import org.pidome.client.system.PCCCLientStatusListener;
import org.pidome.client.system.PCCClientEvent;
import org.pidome.client.system.PCCSystem;
import org.pidome.client.tools.DisplayTools;
import org.pidome.pcl.networking.connections.server.ServerConnection.Profile;

/**
 *
 * @author John
 */
public abstract class AppMainMenu extends VBox {

    VBox mainButtons = new VBox();

    static double defaultIconWidth = 20;
    static double defaultIconHeight = 20;

    StackPane imageContainer = new StackPane();

    protected final PCCSystem system;
    protected final ServiceConnector serviceConnector;

    protected double uImageSize = 60;
    
    private VBox userContainer = new VBox();
    private Label myName = new Label("Who, am, I?");
    
    HBox footerContainer = new HBox();
    
    /**
     * Constructor.
     * @param system
     * @param serviceConnector 
     */
    protected AppMainMenu(PCCSystem system, ServiceConnector serviceConnector) {
        this.system = system;
        this.serviceConnector = serviceConnector;
        this.system.getClient().addListener(clientStatusListener);
        this.getStyleClass().add("main-menu");
        VBox.setVgrow(mainButtons, Priority.ALWAYS);
        if(serviceConnector.userDisplayType()==DisplayType.TINY){
            uImageSize = ((this.serviceConnector.getMaxWorkWidth()/Screen.getPrimary().getVisualBounds().getWidth())*uImageSize);
            imageContainer.setPadding(new Insets(10));
        } else {
            imageContainer.setPadding(new Insets(10));
        }
        Circle mask = new Circle();
        mask.setTranslateX((uImageSize / 2) + imageContainer.getPadding().getLeft()); ////include the insets
        mask.setTranslateY((uImageSize / 2) + imageContainer.getPadding().getTop()); ////include the insets
        mask.setRadius(uImageSize / 2);
        imageContainer.setClip(mask);
        imageContainer.getStyleClass().add("photo-background");
        userContainer.setMinHeight(uImageSize + imageContainer.getPadding().getTop() + imageContainer.getPadding().getBottom());
        setUnknownUser();
    }

    public final HBox buildWhoAmI() {
        HBox topContainer = new HBox();
        topContainer.getStyleClass().add("who-am-i");
        topContainer.setAlignment(Pos.CENTER_LEFT);

        userContainer.setAlignment(Pos.CENTER_LEFT);
        myName.getStyleClass().add("my-name");
        myName.setWrapText(true);
        userContainer.getChildren().add(myName);
        HBox.setHgrow(userContainer, Priority.ALWAYS);
        topContainer.getChildren().addAll(imageContainer, userContainer);
        return topContainer;

    }

    protected void setPresenceContainer(Node container){
        userContainer.getChildren().addAll(container);
    }
    
    protected void setName(String name){
        myName.setText(name);
    }
    
    /**
     * Listener for the client's status.
     * This is only applicable when an connection is made to the server.
     */
    private final PCCCLientStatusListener clientStatusListener = this::handlePCCClientEvent;
    
    private void handlePCCClientEvent(PCCClientEvent event) {
        switch(event.getStatus()){
            case INIT_DONE:
                bindPresence();
            break;
        }
    }
    
    protected abstract void bindPresence();
    
    private void setUnknownUser() {
        Text unknownUser;
        if(this.system.getConnection().getConnectionProfile() == Profile.FIXED){
            unknownUser = GlyphsDude.createIcon(MaterialDesignIcon.TELEVISION_GUIDE, String.valueOf(uImageSize));
        } else {
            unknownUser = GlyphsDude.createIcon(MaterialIcon.PERSON, String.valueOf(uImageSize));
        }
        imageContainer.getChildren().clear();
        imageContainer.getChildren().add(unknownUser);
    }

    public final void build() {

        this.setMaxWidth(405);
        StackPane.setAlignment(this, Pos.TOP_LEFT);
        addMainBlockItem(new SceneBigMenuItem(GlyphsDude.createIcon(MaterialIcon.WIDGETS, String.valueOf(defaultIconHeight)), ScenePane.DASHBOARD, "Dashboard"));
        addMainBlockItem(new SceneBigMenuItem(GlyphsDude.createIcon(MaterialIcon.DEVELOPER_BOARD, String.valueOf(defaultIconHeight)), ScenePane.DEVICES, "Devices"));
        addMainBlockItem(new SceneBigMenuItem(GlyphsDude.createIcon(MaterialIcon.LIVE_TV, String.valueOf(defaultIconHeight)), ScenePane.MEDIA, "Media"));
        
        if(Platform.isSupported(ConditionalFeature.SCENE3D) && this.system.getConnection().getConnectionProfile()!=Profile.MOBILE && this.serviceConnector.userDisplayType()!=DisplayType.TINY){
            addMainBlockItem(new SceneBigMenuItem(GlyphsDude.createIcon(MaterialDesignIcon.HOME_VARIANT, String.valueOf(defaultIconHeight)), ScenePane.FLOOR_MAP3D, "Floor map"));
        }
        if(DisplayTools.getUserDisplayType()==DisplayType.LARGE){
            addMainBlockItem(new SceneBigMenuItem(GlyphsDude.createIcon(MaterialDesignIcon.PLUS_NETWORK, String.valueOf(defaultIconHeight)), ScenePane.DEVICE_DISCOVERY, "Device Discovery"));
        }

        addMainBlockItem(new SceneBigMenuItem(GlyphsDude.createIcon(MaterialDesignIcon.WEATHER_PARTLYCLOUDY, String.valueOf(defaultIconHeight)), ScenePane.WEATHER, "Weather"));
        
        getChildren().addAll(buildWhoAmI(), mainButtons, buildBottomMenu());

        appendBuild();

    }

    private HBox buildBottomMenu(){
        footerContainer.getStyleClass().add("footer-container");
        double inset = 10.0;
        if(serviceConnector.userDisplayType()==DisplayType.TINY){
            inset = ((this.serviceConnector.getMaxWorkWidth()/Screen.getPrimary().getVisualBounds().getWidth())*inset);
        }
        footerContainer.setPadding(new Insets(inset));
        addBottomContainerItem(GlyphsDude.createIcon(MaterialIcon.SETTINGS, String.valueOf(uImageSize-inset)), () -> {
            PiDomeClient.switchScene(ScenePane.SETTINGS);
        });
        return footerContainer;
    }
    
    protected final void addBottomContainerItem(Node node, Runnable action){
        StackPane container = new StackPane();
        container.setMinHeight(uImageSize);
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("icon-background");
        Circle mask = new Circle();
        container.widthProperty().addListener((ChangeListener<Number>)(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) -> {
            mask.setTranslateX(newVal.doubleValue()/2);
        });
        container.heightProperty().addListener((ChangeListener<Number>)(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) -> {
            mask.setTranslateY(newVal.doubleValue()/2);
        });
        mask.setRadius(uImageSize / 2);
        container.setClip(mask);
        HBox.setHgrow(container, Priority.ALWAYS);
        container.getChildren().add(node);
        container.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            action.run();
        });
        footerContainer.getChildren().add(container);
    }
    
    public final HBox addHeader(FontAwesomeIcon iconLeader, String itemName) {

        HBox menuHeaderBox = new HBox(8);

        menuHeaderBox.setPrefWidth(Double.MAX_VALUE);
        menuHeaderBox.setAlignment(Pos.CENTER_LEFT);

        menuHeaderBox.getStyleClass().add("header");

        Text scenesMenuText = new Text(itemName);
        scenesMenuText.getStyleClass().add("menu-title");

        VBox.setMargin(menuHeaderBox, new Insets(10, 0, 5, 0));
        menuHeaderBox.setPadding(new Insets(0, 0, 0, 3));
        scenesMenuText.setStyle("-fx-font-size: 1.3em;");

        menuHeaderBox.getChildren().addAll(GlyphsDude.createIcon(iconLeader, "2em;"), scenesMenuText);
        getChildren().addAll(menuHeaderBox);

        return menuHeaderBox;

    }

    public final void setMainMenuItem(ScenesHandler.ScenePane scenePane) {
        for (Node node : mainButtons.getChildren()) {
            if (node instanceof SceneBigMenuItem) {
                if (((SceneBigMenuItem) node).isScenePane(scenePane) && !((SceneBigMenuItem) node).getStyleClass().contains("active")) {
                    Platform.runLater(() -> {
                        ((SceneBigMenuItem) node).getStyleClass().add("active");
                    });
                } else {
                    Platform.runLater(() -> {
                        ((SceneBigMenuItem) node).getStyleClass().remove("active");
                    });
                }
            }
        }
    }

    public abstract void opened();
    
    public final void addMainBlockItem(SceneBigMenuItem item) {
        mainButtons.getChildren().add(item);
    }

    protected abstract void appendBuild();

    public static class SceneBigMenuItem extends HBox {

        private final ScenePane scene;

        EventHandler<MouseEvent> clickHandler = this::clicker;

        protected SceneBigMenuItem(ImageView iconBackground, ScenePane scene, String itemName) {
            SetBigMenuItem((Node) iconBackground, itemName);
            this.scene = scene;
            iconBackground.setPreserveRatio(true);
            iconBackground.setFitHeight(defaultIconHeight);
            iconBackground.setFitWidth(defaultIconWidth);
        }

        private void clicker(MouseEvent event) {
            PiDomeClient.switchScene(scene);
        }

        protected SceneBigMenuItem(Text iconBackground, ScenePane scene, String itemName) {
            this.SetBigMenuItem((Node) iconBackground, itemName);
            this.scene = scene;
        }

        protected final void SetBigMenuItem(Node icon, String itemName) {
            this.setSpacing(10);
            setAlignment(Pos.CENTER_LEFT);
            setPadding(new Insets(5,0,5,5));
            this.getStyleClass().add("menu-block");
            icon.getStyleClass().add("menu-block-icon");
            Text sceneItemName = new Text(itemName);
            sceneItemName.getStyleClass().add("menu-block-title");
            
            getChildren().addAll(icon, sceneItemName);
            addEventHandler(MouseEvent.MOUSE_CLICKED, clickHandler);
        }

        private final boolean isScenePane(ScenePane pane) {
            return pane == scene;
        }

    }

}
