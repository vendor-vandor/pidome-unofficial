/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system.scenes.components.mainstage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;
import org.pidome.client.system.scenes.components.mainstage.applicationsbar.ApplicationsBarPresenceTime;
import org.pidome.client.system.scenes.components.mainstage.applicationsbar.ApplicationsBarSettings;
import org.pidome.client.system.scenes.components.mainstage.applicationsbar.ApplicationsBarWidgetIcons;
import org.pidome.client.system.scenes.components.mainstage.applicationsbar.ApplicationsBarWidgetIcons.Position;
import org.pidome.client.system.scenes.components.mainstage.applicationsbar.TaskBar;
import org.pidome.client.system.scenes.windows.WindowComponent;
import org.pidome.client.system.scenes.windows.WindowManager;
import org.pidome.client.system.scenes.windows.WindowManagerListener;

/**
 *
 * @author John Sirach
 */
public final class ApplicationsBar extends BorderPane implements ClientDataConnectionListener {
    
    double width;
    double height;
    
    final WindowBar windowBar = new WindowBar();
    
    StringProperty showDate = new SimpleStringProperty();
    
    Label clientName = new Label();
    Label sysStateName = new Label();
    Label userState = new Label();
    
    static Logger LOG = LogManager.getLogger(ApplicationsBar.class);
    
    public ApplicationsBar() {
        width = 1920*DisplayConfig.getWidthRatio();
        height = 50*DisplayConfig.getHeightRatio();
        setId("NotificationBar");
        setMinSize(width, height);
        setMaxSize(width, height);
        setPrefSize(width, height);
        
        addTaskBar();
        addCenterBar();
        addWindowBar();
        
        ClientData.addClientLoggedInConnectionListener(this);
        ClientData.addClientDataConnectionListener(this);
    }
    
    public final ReadOnlyStringProperty getDateTextProperty(){
        return this.showDate;
    }
    
    final void addTaskBar(){
        TaskBar taskBar = new TaskBar();
        taskBar.build();
        setLeft(taskBar);
    }
    
    final void addCenterBar(){
        HBox centerBox = new HBox();
        centerBox.setMaxHeight(44*DisplayConfig.getHeightRatio());
        centerBox.setTranslateY(-3*DisplayConfig.getHeightRatio());
        centerBox.setAlignment(Pos.CENTER);        
        
        ApplicationsBarPresenceTime timeBlock = new ApplicationsBarPresenceTime();
        
        centerBox.getChildren().addAll(ApplicationsBarWidgetIcons.getInstance(Position.LEFT), 
                                       timeBlock,
                                       ApplicationsBarWidgetIcons.getInstance(Position.RIGHT));
        
        setCenter(centerBox);
    }
    
    final void addWindowBar(){
        HBox windowBarContainer = new HBox();
        
        Image rightOff = new ImageLoader("notificationbar/scrollcontent.png",13,14).getImage();
        Image rightOn = new ImageLoader("notificationbar/scrollcontentactive.png",13,14).getImage();
        ImageView rightArrow = new ImageView(rightOff);
        
        Image leftOff = new ImageLoader("notificationbar/scrollcontent.png",13,14).getImage();
        Image leftOn = new ImageLoader("notificationbar/scrollcontentactive.png",13,14).getImage();
        ImageView leftArrow = new ImageView(leftOff);
        leftArrow.setScaleX(-1);
        
        StackPane leftButton = new StackPane();
        leftButton.setPrefSize(28*DisplayConfig.getWidthRatio(), 44*DisplayConfig.getHeightRatio());
        leftButton.setAlignment(Pos.CENTER);
        leftButton.getChildren().add(leftArrow);
        leftButton.setOnMousePressed((MouseEvent me) -> {
            windowBar.moveLeft();
            me.consume();
        });
        
        StackPane rightButton = new StackPane();
        rightButton.setPrefSize(28*DisplayConfig.getWidthRatio(), 44*DisplayConfig.getHeightRatio());
        rightButton.setAlignment(Pos.CENTER);
        rightButton.getChildren().add(rightArrow);
        rightButton.setOnMousePressed((MouseEvent me) -> {
            windowBar.moveRight();
            me.consume();
        });
        
        windowBar.leftScrollPos.addListener((ObservableValue<? extends Boolean> ov, Boolean t1, Boolean t2) -> {
            if(t2){
                leftArrow.setImage(leftOn);
            } else {
                leftArrow.setImage(leftOff);
            }
        });
        
        windowBar.rightScrollPos.addListener((ObservableValue<? extends Boolean> ov, Boolean t1, Boolean t2) -> {
            if(t2){
                rightArrow.setImage(rightOn);
            } else {
                rightArrow.setImage(rightOff);
            }
        });
        
        StackPane windowBarHolder = new StackPane();
        windowBarHolder.setAlignment(Pos.TOP_LEFT);
        windowBarHolder.setMinSize(523*DisplayConfig.getWidthRatio(), 44*DisplayConfig.getHeightRatio());
        windowBarHolder.setMaxSize(523*DisplayConfig.getWidthRatio(), 44*DisplayConfig.getHeightRatio());
        windowBarHolder.setPrefSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        windowBarHolder.getChildren().add(windowBar);

        Image windowBarMask = new ImageLoader("notificationbar/topscrollmask.png",523,46).getImage();
        ImageView maskView = new ImageView(windowBarMask);
        
        windowBarHolder.setClip(maskView);
        windowBarContainer.getChildren().addAll(leftButton, windowBarHolder, rightButton);
        windowBarContainer.setTranslateY(-1);
        setRight(windowBarContainer);
        windowBar.heightProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldHeight, Number newHeight) -> {
            maskView.setFitHeight((double)newHeight);
        });
        windowBar.start();
        HBox.setHgrow(windowBar, Priority.ALWAYS);
    }
    
    final Label addSysStateNameBar(){
        sysStateName.getStyleClass().add("sysstateinfo");
        sysStateName.setTranslateY(3*DisplayConfig.getHeightRatio());
        sysStateName.setOnMouseClicked((MouseEvent t) -> {
            ApplicationsBarSettings sysStateDisplay = new ApplicationsBarSettings(this);
            WindowManager.openWindow(sysStateDisplay);
        });
        return sysStateName;
    }
    
    @Override
    public void handleClientDataConnectionEvent(final ClientDataConnectionEvent event) {
        switch (event.getEventType()) {
            case ClientDataConnectionEvent.LOGGEDIN:
                String[] data = event.getClientData();
                Platform.runLater(() -> {
                    clientName.setText(data[0]);
                });
            break;
        }
    }
    
    public final class WindowBar extends HBox implements WindowManagerListener {
        
        Logger LOG = LogManager.getLogger(WindowBar.class);
        
        Map<WindowComponent, WindowIconSet> iconSets = new HashMap<>();
        
        double noScrollWidth = 523.0;
        double currentWidth = 0;
        double maxLeftScroll = 0;
        SimpleDoubleProperty currentPos = new SimpleDoubleProperty(4.0);
        
        double defaultStep = 220;
        double maxMoveStep = 220;
        
        double baseLeft = 4.0;
        double newPosition = baseLeft;
        
        boolean dragging = false;
        double dragStartX;
        
        protected SimpleBooleanProperty leftScrollPos= new SimpleBooleanProperty(false);
        protected SimpleBooleanProperty rightScrollPos = new SimpleBooleanProperty(false);
        
        /// See super constructor and keep it the same size.
        double iconPadding = 6;
        
        public WindowBar(){
            super(6);
            setTranslateY(5);
            setTranslateX(baseLeft);
            
            widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                currentWidth = (double)newValue;
                maxLeftScroll = noScrollWidth - currentWidth;
                if(maxLeftScroll < defaultStep){
                    maxMoveStep = defaultStep - maxLeftScroll;
                }
                scrollButtonsHandler();
            });

            currentPos.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                newPosition = (double)newValue;
                scrollButtonsHandler();
            });
            
            this.setOnMousePressed((MouseEvent event) -> {
                dragStartX = this.getTranslateX() - event.getSceneX();
            });
            this.setOnMouseDragged((MouseEvent event) -> {
                if(currentWidth>noScrollWidth){
                    double translatePos = dragStartX + event.getSceneX();
                    if(translatePos > baseLeft){
                        setTranslateX(baseLeft);
                        currentPos.setValue(baseLeft);
                    } else if (maxLeftScroll < translatePos) {
                        setTranslateX(translatePos);
                        currentPos.setValue(translatePos);
                    } else {
                        setTranslateX(maxLeftScroll);
                        currentPos.setValue(maxLeftScroll);
                    }
                    event.consume();
                }
            });
            this.setOnMouseReleased((MouseEvent event) -> {
                event.consume();
            });
            
        }

        final void windowBarHandleRemovedIcon(double iconWidth){
            double moveRight = getTranslateX() + (iconWidth + iconPadding);
            if(moveRight <= baseLeft){
                animateMovement(getTranslateX(),moveRight);
            } else {
                animateMovement(getTranslateX(), baseLeft);
            }
        }
        
        final void animateMovement(double from, double to){
            if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){
                TranslateTransition translatePositionTransition = new TranslateTransition(Duration.millis(250), this);
                translatePositionTransition.setFromX(from);
                translatePositionTransition.setToX(to);
                translatePositionTransition.setCycleCount(1);
                translatePositionTransition.setAutoReverse(false);
                translatePositionTransition.setOnFinished((ActionEvent t) -> {
                    currentPos.setValue(to);
                });
                translatePositionTransition.play();
            } else {
                setTranslateX(to);
            }
        }
        
        final void scrollButtonsHandler(){
            leftScrollPos.setValue(newPosition < baseLeft);
            rightScrollPos.setValue((-(currentWidth-noScrollWidth)+baseLeft)<currentPos.getValue());
        }
        
        public final void moveRight(){
            if(currentWidth>noScrollWidth){
                double moveRight = getTranslateX() - defaultStep;
                if(maxLeftScroll - moveRight >= baseLeft){
                    animateMovement(getTranslateX(), maxLeftScroll);
                } else {
                    animateMovement(getTranslateX(), moveRight);                                        
                }
            }
        }
        
        public final void moveLeft(){
            if(currentWidth>noScrollWidth){
                double moveLeft = getTranslateX() + defaultStep;
                if(moveLeft >= baseLeft){
                    animateMovement(getTranslateX(),baseLeft);
                } else {
                    animateMovement(getTranslateX(),moveLeft);
                }
            }
        }
        
        public final void start(){
            WindowManager.addWindowListener(this);
        }
        
        @Override
        public void windowAdded(WindowComponent window) {
            LOG.debug("Window added: {}, has parent: {}", window.getWindowName(), window.hasWindowParent());
            if(window.hasWindowParent()){
                if(!iconSets.containsKey((WindowComponent)window.getWindowParent())){
                    WindowIconSet parentIcon = new WindowIconSet((WindowComponent)window.getWindowParent());
                    iconSets.put(window,parentIcon);
                    getChildren().add(parentIcon);
                    parentIcon.addChild(window);
                } else {
                    iconSets.get((WindowComponent)window.getWindowParent()).addChild(window);
                }
            } else {
                WindowIconSet parentIcon = new WindowIconSet(window);
                iconSets.put(window,parentIcon);
                getChildren().add(parentIcon);
            }
        }

        @Override
        public void windowRemoved(final WindowComponent window) {
            if(window.hasWindowParent()){
                if(iconSets.containsKey((WindowComponent)window.getWindowParent())){
                    iconSets.get((WindowComponent)window.getWindowParent()).removeChild(window);
                }
            } else {
                windowBarHandleRemovedIcon(iconSets.get(window).getWidth());
                if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){
                    iconSets.get(window).remove(this);
                } else {
                    iconSets.get(window).remove();
                    Platform.runLater(() -> {
                        getChildren().remove(iconSets.get(window));
                    });
                }
                iconSets.remove(window);
            }
        }
        
        public final class WindowIconSet extends VBox implements WindowIconListener {
            
            WindowIcon parentIcon;
            List<WindowIcon> childList = new ArrayList();
            boolean parentOpen = false;
            
            public WindowIconSet(WindowComponent window){
                super(3);
                parentIcon = new WindowIcon(window);
                parentIcon.isParent();
                parentIcon.setScaleX(0.0);
                parentIcon.setScaleY(0.0);
                getChildren().add(parentIcon);
                if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){
                    /* set size */
                    ScaleTransition scaleSizeTransition = new ScaleTransition(Duration.millis(250), parentIcon);
                    scaleSizeTransition.setToY(1.0);
                    scaleSizeTransition.setToX(1.0);
                    scaleSizeTransition.setCycleCount(1);
                    scaleSizeTransition.setAutoReverse(false);
                    scaleSizeTransition.play();
                } else {
                    parentIcon.setScaleX(1.0);
                    parentIcon.setScaleY(1.0);
                }
            }
            
            final void removeChildren(){
                if(!childList.isEmpty()){
                    childList.stream().forEach((child) -> {
                        child.removeHandlers();
                        getChildren().remove(child);
                    });
                }
            }
            
            public final void remove(){
                if(!Platform.isFxApplicationThread()){
                    Platform.runLater(() -> {
                        removeChildren();
                        getChildren().remove(parentIcon);
                    });
                } else {
                    removeChildren();
                    getChildren().remove(parentIcon);
                }
                childList.clear();
            }
            
            public final void remove(final WindowBar source){
                removeChildren();
                ScaleTransition scaleSizeTransition = new ScaleTransition(Duration.millis(250), parentIcon);
                scaleSizeTransition.setToY(0.0);
                scaleSizeTransition.setToX(0.0);
                scaleSizeTransition.setCycleCount(1);
                scaleSizeTransition.setAutoReverse(false);
                scaleSizeTransition.setOnFinished((ActionEvent t) -> {
                    parentIcon.removeHandlers();
                    getChildren().remove(parentIcon);
                    removeFromSource(source);
                });
                scaleSizeTransition.play();
                childList.clear();
            }
            
            final void removeFromSource(final WindowBar source){
                source.getChildren().remove(this);
            }
            
            public final void addChild(WindowComponent window){
                WindowIcon childIcon = new WindowIcon(window);
                childList.add(childIcon);
                window.setIconReference(childIcon);
                childIcon.setOpacity(0.0);
                getChildren().add(childIcon);
                parentIcon.showMoreItems(true);
            }
            
            public final void removeChild(WindowComponent window){
                WindowIcon icon = window.getIconReference();
                window.removeIconReference(icon);
                childList.remove(childList.indexOf(icon));
                icon.removeHandlers();
                getChildren().remove(icon);
                if(childList.isEmpty()){
                    parentIcon.showMoreItems(false);
                }
            }

            @Override
            public final void parentPressed() {
                if(!childList.isEmpty()){
                    if(parentOpen==false){
                        parentIcon.setOpen();
                        childList.stream().forEach((child) -> {
                            if (DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)) {
                                FadeTransition fi = new FadeTransition(Duration.millis(100), child);
                                fi.setToValue(1.0);
                                fi.setCycleCount(1);
                                fi.setAutoReverse(false);
                                fi.play();
                            } else {
                                child.setOpacity(1.0);
                            }
                        });
                        parentOpen = true;
                    } else {
                        childPressed();
                    }
                }
            }
            
            @Override
            public final void childPressed(){
                parentIcon.setClosed();
                childList.stream().forEach((child) -> {
                    if (DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)) {
                        FadeTransition fo = new FadeTransition(Duration.millis(100), child);
                        fo.setToValue(0.0);
                        fo.setCycleCount(1);
                        fo.setAutoReverse(false);
                        fo.play();
                    } else {
                        child.setOpacity(0.0);
                    }
                });
                parentOpen = false;
            }
            
            public final class WindowIcon extends StackPane {

                WindowComponent window;
                ImageView moreItems;                
                boolean isParent = false;                
                boolean isChild = true;
                
                ImageLoader windowIcon;
                ImageLoader moreWindowsIcon;
                
                double iconWidth = 110; 
                double iconHeight= 39;
                
                EventHandler<MouseEvent> clickEvent = (MouseEvent t) -> {
                    window.toFront();
                    if(isParent==true){
                        parentPressed();
                    }
                    if(isChild==true){
                        childPressed();
                    }
                };
                
                public WindowIcon(WindowComponent window){
                    this.window = window;
                    setAlignment(Pos.TOP_LEFT);
                    Rectangle rect = new Rectangle();
                    rect.setWidth(iconWidth);
                    rect.setHeight(iconHeight);
                    rect.getStyleClass().add("windowicon");
                    setMinSize(iconWidth, iconHeight);
                    setMaxSize(iconWidth, iconHeight);
                    Label buttonName = new Label(window.getWindowName());
                    buttonName.setMaxSize((iconWidth-3)*DisplayConfig.getWidthRatio(), height*DisplayConfig.getHeightRatio());
                    buttonName.setWrapText(true);
                    buttonName.setAlignment(Pos.TOP_LEFT);
                    buttonName.setTranslateX(3*DisplayConfig.getWidthRatio());
                    buttonName.setTranslateY(0);
                    moreWindowsIcon = new ImageLoader("notificationbar/windowicon-subwindows.png", 12, 14);
                    moreItems = new ImageView(moreWindowsIcon.getImage());
                    moreItems.setOpacity(0.0);
                    moreItems.setTranslateX(94*DisplayConfig.getWidthRatio());
                    moreItems.setTranslateY(20*DisplayConfig.getHeightRatio());
                    getChildren().addAll(rect,buttonName,moreItems);
                    addEventHandler(MouseEvent.MOUSE_CLICKED, clickEvent);
                }

                public final void isParent(){
                    isParent = true;
                    isChild = false;
                }
                
                public final void setOpen(){
                    if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){
                        RotateTransition rt = new RotateTransition(Duration.millis(100), moreItems);
                        rt.setToAngle(90);
                        rt.setCycleCount(1);
                        rt.setAutoReverse(false);
                        rt.play();
                    } else {
                        moreItems.setRotate(90);
                    }
                }
                
                public final void setClosed(){
                    if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){
                        RotateTransition rt = new RotateTransition(Duration.millis(100), moreItems);
                        rt.setToAngle(0);
                        rt.setCycleCount(1);
                        rt.setAutoReverse(false);
                        rt.play();
                    } else {
                        moreItems.setRotate(0);
                    }
                }
                
                public final void removeHandlers(){
                    removeEventHandler(MouseEvent.MOUSE_CLICKED, clickEvent);
                }
                
                public final boolean gotMoreItems(){
                    return moreItems.getOpacity()!=0.0;
                }
                
                public final void showMoreItems(boolean show){
                    if(show==true){
                        moreItems.setOpacity(1.0);
                    } else {
                        moreItems.setOpacity(0.0);
                    }
                }
                
            }
            
            
        }
        
    }
    
    public interface WindowIconListener {
        public void parentPressed();
        public void childPressed();
    }
    
}
