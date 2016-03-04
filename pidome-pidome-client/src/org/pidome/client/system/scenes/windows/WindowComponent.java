/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.windows;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.PidomeClient;
import org.pidome.client.config.AppResources;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.MainScene;
import org.pidome.client.system.scenes.animations.WindowAnimationDoneListener;
import org.pidome.client.system.scenes.animations.WindowAnimations;
import org.pidome.client.system.scenes.components.mainstage.ApplicationsBar.WindowBar.WindowIconSet.WindowIcon;

/**
 *
 * @author John Sirach
 */
public abstract class WindowComponent extends StackPane implements WindowAnimationDoneListener {

    double posX;
    double posY;
    boolean openPos = false;
    
    double locFromX;
    double locFromY;
    boolean openLoc = false;
    
    double windowPosForDragX;
    double windowPosForDragY;
    
    boolean sizeSet = false;
    
    BooleanProperty sizeKnownProperty = new SimpleBooleanProperty(false);
    
    String windowName;
    String windowId;
    
    WindowComponent parent;
    
    Stage stagedWindow;
    
    List<WindowComponent> childs = new ArrayList();
    
    WindowIcon iconReference;
    
    List<WindowComponentListener> windowListeners = new ArrayList();
    
    final String SWIPE_CLOSE = "SWIPE_CLOSE";
    
    static String windowingStyle = DisplayConfig.getRunMode();
    
    boolean mouseStagePressed = false;
    boolean draggable = false;
    
    static Logger LOG = LogManager.getLogger(WindowComponent.class);
    
    public WindowComponent(WindowComponent parent, String windowName, double posX, double posY){
        startSizeListener();
        setPickOnBounds(false);
        setPosition(posX, posY);
        this.windowName = windowName;
        this.parent = parent;
        addChildToParent(this);
        if(windowingStyle.equals(DisplayConfig.RUNMODE_WIDGET)) setStagedWindow(windowName);
    }
    
    public WindowComponent(String windowName, double posX, double posY){
        startSizeListener();
        setPickOnBounds(false);
        setPosition(posX, posY);
        this.windowName = windowName;
        if(windowingStyle.equals(DisplayConfig.RUNMODE_WIDGET)) setStagedWindow(windowName);
    }
    
    public WindowComponent(String windowName){
        startSizeListener();
        setPickOnBounds(false);
        this.windowName = windowName;
        if(windowingStyle.equals(DisplayConfig.RUNMODE_WIDGET)) setStagedWindow(windowName);
    }
    
    public WindowComponent(WindowComponent parent, String windowName){
        startSizeListener();
        setPickOnBounds(false);
        this.parent = parent;
        this.windowName = windowName;
        addChildToParent(this);
        if(windowingStyle.equals(DisplayConfig.RUNMODE_WIDGET)) setStagedWindow(windowName);
    }
    
    final void setStagedWindow(String windowName){
        stagedWindow = new Stage();
        stagedWindow.initOwner(MainScene.getWindow());
        stagedWindow.initStyle(StageStyle.TRANSPARENT);
        stagedWindow.setTitle("PiDome - " + windowName);
        stagedWindow.centerOnScreen();
        stagedWindow.getIcons().add(new Image(PidomeClient.appLogo));
        Scene WindowContent = new Scene(this);
        WindowContent.setFill(Color.TRANSPARENT);
        this.setStyle(("-fx-background-color: transparent;"));
        stagedWindow.setScene(WindowContent);
        heightProperty().addListener((ChangeListener)(ObservableValue ov, Object t, Object t1) -> {
            stagedWindow.setHeight((double) t1);
        });
        widthProperty().addListener((ChangeListener)(ObservableValue ov, Object t, Object t1) -> {
            stagedWindow.setWidth((double) t1);
        });
        getStylesheets().add(AppResources.getCss("main.css"));
        getStylesheets().add(AppResources.getCss("skin-dark.css"));
        getStylesheets().add(AppResources.getCss("high.css"));
    }
    
    ChangeListener<Number> widthObserver = this::sizeKnownHelperWidth;
    ChangeListener<Number> heightObserver = this::sizeKnownHelperHeight;
    ChangeListener<Boolean> positionOpenerObserver = this::positionOpener;
    
    final void sizeKnownHelperWidth(ObservableValue<? extends Number> observable, Number oldValue, Number newValue){
        if(this.heightProperty().get()!=0){
            sizeKnownProperty.setValue(true);
        }
    }

    final void sizeKnownHelperHeight(ObservableValue<? extends Number> observable, Number oldValue, Number newValue){
        if(this.widthProperty().get()!=0){
            sizeKnownProperty.setValue(true);
        }
    }
    
    final void startSizeListener() {
        this.widthProperty().addListener(widthObserver);
        this.heightProperty().addListener(heightObserver);
        this.sizeKnownProperty.addListener(positionOpenerObserver);
    }
    
    public final void autoClose(final int timeOut) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeOut * 1000);
                    close(null);
                } catch (InterruptedException ex) {
                    close(null);
                }
            }
        };
        thread.start();
    }
    
    public final Stage getStagedWindow(){
        if(stagedWindow!=null){
            return stagedWindow;
        } else {
            return null;
        }
    }
    
    public void setSize(double width, double height){
        this.setPrefSize(width*DisplayConfig.getWidthRatio(),height*DisplayConfig.getHeightRatio());
        this.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        if(windowingStyle.equals(DisplayConfig.RUNMODE_WIDGET)) {
            stagedWindow.setWidth(width);
            stagedWindow.setHeight(height);
        }
        sizeSet = true;
    }
    
    public final boolean sizeSet(){
        return sizeSet;
    }
    
    public final void setPosition(double posX, double posY){
        this.posX = posX*DisplayConfig.getWidthRatio();
        this.posY = posY*DisplayConfig.getHeightRatio();
        openPos = true;
    }
    
    protected abstract void constructWindow();
    protected abstract void destructWindow();
    protected abstract void setupContent();
    protected abstract void removeContent();
    
    final void positionOpener(ObservableValue<? extends Boolean> ov, Boolean t1, Boolean t2){
        this.widthProperty().removeListener(widthObserver);
        this.heightProperty().removeListener(heightObserver);
        if(openLoc){
            if(openPos){
                WindowAnimations.openFromToLocation(this, locFromX, locFromY, posX, posY);
            } else {
                WindowAnimations.openFromToCenter(this, locFromX, locFromY);
            }
        } else {
            if(openPos){
                WindowAnimations.openFromCenter(this, posX, posY);
            } else {
                WindowAnimations.openCentered(this);
            }
        }
    }
    
    public void open(double locFromX, double locFromY){
        this.locFromX = locFromX;
        this.locFromY = locFromY;
        openLoc = true;
        openWindow();
    }
    
    public void open(){
        openWindow();
    }
    
    final void openWindow(){
        constructWindow();
        setupContent();
        if(windowingStyle.equals(DisplayConfig.RUNMODE_WIDGET)){
            stagedWindow.show();
        } else {
            WindowAnimations.prepareWindowAnimation(this);
            MainScene.getPane().getChildren().add(this);
        }
    }
    
    public final void addChild(WindowComponent child){
        if(!childs.contains(child)){
            childs.add(child);
        }
    }
    
    final void addChildToParent(WindowComponent child){
        if(parent!=null && parent instanceof WindowComponent){
            ((WindowComponent)parent).addChild(child);
        }
    }
    
    public void close(String method){
        if(method!=null){
            WindowAnimations.closeToLeft(this);
        } else {
            WindowAnimations.closeInPlace(this, 0);
        }
        childs.stream().forEach((child) -> {
            child.close(method);
        });
        notifyWindowClosedState();
    }

    public final boolean hasChildren(){
        return childs.size()>0;
    }
    
    public final boolean hasWindowParent(){
        return parent!=null;
    }
    
    public final WindowComponent getWindowParent(){
        if(hasWindowParent()){
            return parent;
        } else {
            return null;
        }
    }
    
    public final String getWindowName(){
        return windowName;
    }
    
    public final void pressToClose(boolean pressToClose){
        if(pressToClose==true){
            setOnMouseClicked((MouseEvent me) -> {
                close(null);
            });
        }
    }
    
    final void stageMousePressed(MouseEvent me){
        handleStageClickPos(me.getScreenX(), me.getScreenY());
        mouseStagePressed = true;
    }

    final void stageMouseDragged(MouseEvent me){
        if(!me.isConsumed()){
            mouseStagePressed = false;
        }
    }
    
    final void stageMouseStopped(MouseEvent me){
        if(!me.isConsumed()){
            moveStage(me.getScreenX(), me.getScreenY());
        }
    }
    
    final void windowPressStart(MouseEvent me){
        handleWindowClickPos(me.getSceneX(), me.getSceneY());
    }

    final void windowPressDrag(MouseEvent me){
        if(!me.isConsumed()){
            moveMe(me.getSceneX(), me.getSceneY());
        }
    }
    
    public final void draggable(boolean draggable){
        this.draggable = draggable;
        if(windowingStyle.equals(DisplayConfig.RUNMODE_WIDGET)){
            if(draggable==true){
                addEventHandler(MouseEvent.MOUSE_PRESSED, this::stageMousePressed);
                addEventHandler(MouseEvent.MOUSE_DRAGGED, this::stageMouseDragged);
                addEventHandler(MouseEvent.MOUSE_RELEASED, this::stageMouseStopped);
            }
        } else {
            if(draggable==true){
                addEventHandler(MouseEvent.MOUSE_PRESSED, this::windowPressStart);
                addEventHandler(MouseEvent.MOUSE_DRAGGED, this::windowPressDrag);
            }
        }
    }
    
    final void handleStageClickPos(double x, double y){
        if(mouseStagePressed==false){
            windowPosForDragX = (stagedWindow.getX()) - x;
            windowPosForDragY = (stagedWindow.getY()) - y;
        }
    }
    
    final void moveStage(double x, double y){
        stagedWindow.setX(x + windowPosForDragX);
        stagedWindow.setY(y + windowPosForDragY);
    }
    
    final void handleWindowClickPos(double x, double y){
        this.toFront();
        windowPosForDragX = (getLayoutX()) - x;
        windowPosForDragY = (getLayoutY()) - y;
    }
    
    final void moveMe(double x, double y){
        relocate(x + windowPosForDragX,y + windowPosForDragY);
    }
    
    @Override
    public final void handleAnimationDone(String animationType){
        switch(animationType){
            case WindowAnimations.OPEN:
                
            break;
            case WindowAnimations.CLOSE_IN_PLACE:
                destructWindow();
                removeChildren(this);
                MainScene.getPane().getChildren().remove(this);
            break;
        }
    }

    final void removeChildren(Pane pane){
        if(pane.getChildren().size()>0){
            ArrayList<Node> nodeList = new ArrayList<>();
            for(int i=0;i<pane.getChildren().size();i++){
                if(pane.getChildren().get(i) instanceof Pane){
                    removeChildren((Pane)pane.getChildren().get(i));
                } else {
                    nodeList.add(pane.getChildren().get(i));
                }
            }
            pane.getChildren().removeAll(nodeList);
        }
    }
    
    public final WindowIcon getIconReference(){
        return iconReference;
    }
    
    public final void setIconReference(WindowIcon icon){
        iconReference = icon;
    }
    
    public final void removeIconReference(WindowIcon icon){
        iconReference = null;
    }
    
    
    public final void addWindowComponentListener(WindowComponentListener listener){
        if(!windowListeners.contains(listener)){
            windowListeners.add(listener);
        }
    }
    
    public final void removeWindowComponentListener(WindowComponentListener listener){
        if(windowListeners.contains(listener)){
            windowListeners.remove(listener);
        }
    }
    
    public final void notifyWindowClosedState(){
        Iterator listeners = windowListeners.iterator();
        while (listeners.hasNext()) {
            ((WindowComponentListener) listeners.next()).windowClosed(this);
        }
    }
    
    public final void destroy(){
        if(this.draggable){
            if(windowingStyle.equals(DisplayConfig.RUNMODE_WIDGET)){
                removeEventHandler(MouseEvent.MOUSE_PRESSED,  this::stageMousePressed);
                removeEventHandler(MouseEvent.MOUSE_RELEASED, this::stageMouseStopped);
                removeEventHandler(MouseEvent.MOUSE_DRAGGED,  this::stageMouseDragged);
            } else {
                removeEventHandler(MouseEvent.MOUSE_PRESSED, this::windowPressStart);
                removeEventHandler(MouseEvent.MOUSE_DRAGGED, this::windowPressDrag);
            }
        }
        pressToClose(false);
        windowListeners.clear();
        childs.clear();
        this.sizeKnownProperty.removeListener(positionOpenerObserver);
    }
    
}