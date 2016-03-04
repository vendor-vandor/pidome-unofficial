/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.windows;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;
import static org.pidome.client.system.scenes.windows.WindowComponent.windowingStyle;

/**
 *
 * @author John Sirach
 */
public abstract class TitledWindowBase extends WindowComponent {
    
    StackPane contentPane = new StackPane();
    
    Label windowTitle = new Label();
    Label bottomBarLabel = new Label();
    
    ImageView closeButton = new ImageView(new ImageLoader("displays/closebutton.png", 50, 29).getImage());
    
    static Logger LOG = LogManager.getLogger(TitledWindowBase.class);
    
    VBox windowContent = new VBox();
    
    StackPane topTitleBar = new StackPane();
    StackPane bottomLabelBar = new StackPane();
    
    public TitledWindowBase(String windowId, String windowName) {
        super(windowName);
    }

    public TitledWindowBase(WindowComponent parent, String windowId, String windowName) {
        super(parent,windowName);
    }
    
    @Override
    protected abstract void setupContent();
    @Override
    protected abstract void removeContent();
    
    final void contentHelperHeight(ObservableValue<? extends Number> observable, Number oldValue, Number newValue){
        this.setHeight((double)newValue);
    }

    final void contentHelperWidth(ObservableValue<? extends Number> observable, Number oldValue, Number newValue){
        this.setWidth((double)newValue);
    }
    
    @Override
    protected final void constructWindow(){
        if(windowingStyle.equals(DisplayConfig.RUNMODE_WIDGET)){
            windowContent.heightProperty().addListener(this::contentHelperHeight);
            windowContent.widthProperty().addListener(this::contentHelperWidth);
        }
        draggable(true);
        setAlignment(Pos.TOP_LEFT);

        getStyleClass().add("windowbase");
        
        windowContent.setAlignment(Pos.TOP_LEFT);
        
        topTitleBar.getStyleClass().add("windowtop");
        topTitleBar.setAlignment(Pos.CENTER_LEFT);
        
        StackPane.setAlignment(closeButton, Pos.CENTER_RIGHT);
        closeButton.addEventFilter(MouseEvent.MOUSE_PRESSED, this::windowCloseHelper);
        
        topTitleBar.getChildren().addAll(title(), closeButton);
        
        contentPane.getStyleClass().add("windowcontent");
        
        bottomLabelBar.getStyleClass().add("windowbottom");
        bottomLabelBar.setAlignment(Pos.CENTER_LEFT);
        bottomLabelBar.getChildren().add(bottomLabel());

        windowContent.getChildren().addAll(topTitleBar,contentPane,bottomLabelBar );
        
        getChildren().add(windowContent);
    }
    
    public final double getContentWidth(){
        return contentPane.getWidth();
    }
    
    public final double getContentHeight(){
        return contentPane.getHeight();
    }
    
    protected final void assignContent(Region region){
        contentPane.getChildren().add(region);
    }
    
    protected final void setBottomLabel(String string){
        Platform.runLater(() -> {
            bottomBarLabel.setText(string);
        });
    }
    
    @Override
    public final void setSize(double width, double height){
        super.setSize(width+2, height + (28*DisplayConfig.getHeightRatio()) + (30*DisplayConfig.getHeightRatio()));
    }
    
    final Label bottomLabel(){
        bottomBarLabel.setPrefHeight(28*DisplayConfig.getHeightRatio());
        bottomBarLabel.setMinHeight(Region.USE_PREF_SIZE);
        bottomBarLabel.setMaxHeight(Region.USE_PREF_SIZE);
        bottomBarLabel.setText(this.getWindowName());
        bottomBarLabel.setTranslateX(5*DisplayConfig.getWidthRatio());
        bottomBarLabel.setAlignment(Pos.CENTER_LEFT);
        return bottomBarLabel;
    }
    
    final Label title(){
        windowTitle.getStyleClass().add("windowtitle");
        windowTitle.setPrefHeight(30*DisplayConfig.getHeightRatio());
        windowTitle.setMinHeight(Region.USE_PREF_SIZE);
        windowTitle.setMaxHeight(Region.USE_PREF_SIZE);
        windowTitle.setText(this.getWindowName());
        windowTitle.setTranslateX(5*DisplayConfig.getWidthRatio());
        windowTitle.setAlignment(Pos.CENTER_LEFT);
        return windowTitle;
    }
    
    final void windowCloseHelper(MouseEvent me){
        me.consume();
        WindowManager.closeWindow(this);
    }
    
    @Override
    public final void destructWindow(){
        closeButton.removeEventFilter(MouseEvent.MOUSE_PRESSED, this::windowCloseHelper);
        topTitleBar.getChildren().remove(closeButton);
        windowContent.getChildren().removeAll(topTitleBar,contentPane,bottomLabelBar );
        removeContent();
        getChildren().remove(windowContent);
        if(windowingStyle.equals(DisplayConfig.RUNMODE_WIDGET)){
            windowContent.heightProperty().removeListener(this::contentHelperHeight);
            windowContent.widthProperty().removeListener(this::contentHelperWidth);
        }
    }
    
}
