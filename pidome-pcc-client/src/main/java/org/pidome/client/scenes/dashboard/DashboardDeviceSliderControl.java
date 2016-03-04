/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlCommandException;
import org.pidome.client.entities.devices.DeviceSliderControl;
import org.pidome.client.entities.devices.DeviceSliderControl.SliderCommand;
import org.pidome.client.scenes.dashboard.svg.devices.DeviceSliderBG0;

/**
 *
 * @author John
 */
public class DashboardDeviceSliderControl extends DashboardDeviceControlBase {

    Text curValue = new Text("0");
    
    StackPane gaugeHolder = new StackPane();
    GridPane fullGauge   = new GridPane();
    
    Arc arcActual = new Arc();
    
    private boolean updated = false;
    
    Timeline timeline = new Timeline();
    
    private final PropertyChangeListener deviceDataListener = this::dataListener;
    
    ChangeListener<Number> framePrefWidthListener = this::framePrefWidthListener;
    
    StackPane frame = new StackPane();
    
    private void dataListener(PropertyChangeEvent evt){
        updated = true;
        animateActualArc(fromValToArc(((Number)evt.getNewValue()).doubleValue()));
    }
    
    public DashboardDeviceSliderControl(VisualDashboardDeviceItem parent, DeviceControl control) {
        super(parent, control);
        this.getPane().getStyleClass().addAll("slider-control");
    }
    
    private final DeviceSliderControl getSliderControl(){
        return (DeviceSliderControl)this.getControl();
    }
    
    @Override
    public void build() {
        DeviceSliderBG0 bg = new DeviceSliderBG0();
        this.getPane().setBackGround(bg);
        buildSlider();
    }

    private void buildSlider(){
        
        double width;
        if(this.getPane().getPaneWidth() > this.getPane().getPaneHeight()){
            width = this.getPane().getPaneHeight();
        } else {
            width = this.getPane().getPaneWidth();
        }
        width = width - 20;
        double height = width /2;
        
        Text minValue = new Text(String.valueOf(getSliderControl().getMin().doubleValue()));
        Text maxValue = new Text(String.valueOf(getSliderControl().getMax().doubleValue()));
        
        Text controlFullName = new Text(new StringBuilder(this.getSliderControl().getName()).append("\n").append(this.getSliderControl().getControlGroup().getDevice().getDeviceName()).toString());
        controlFullName.getStyleClass().add("control-text");
        controlFullName.setWrappingWidth(width);
        controlFullName.setTextAlignment(TextAlignment.CENTER);
        GridPane.setHalignment(controlFullName, HPos.CENTER);
        GridPane.setValignment(controlFullName, VPos.TOP);
        controlFullName.setStyle("-fx-font-size: " + this.getPane().calcFontSize(8, false));
        
        curValue.getStyleClass().addAll("control-text","value");
        minValue.getStyleClass().addAll("control-text");
        maxValue.getStyleClass().addAll("control-text");
        
        curValue.setStyle("-fx-font-size: " + this.getPane().calcFontSize(10, false));
        minValue.setStyle("-fx-font-size: " + this.getPane().calcFontSize(8, false));
        maxValue.setStyle("-fx-font-size: " + this.getPane().calcFontSize(8, false));
        
        Arc arcCutOut = new Arc();
        arcCutOut.getStyleClass().add("slider-cutout");
        arcCutOut.setStartAngle(0);
        arcCutOut.setType(ArcType.OPEN);
        arcCutOut.setLength(180);
        
        arcCutOut.centerXProperty().bind(frame.widthProperty().multiply(0.25));
        arcCutOut.centerYProperty().bind(frame.widthProperty().multiply(0.25));

        arcCutOut.radiusXProperty().bind(frame.widthProperty().multiply(0.25));
        arcCutOut.radiusYProperty().bind(frame.widthProperty().multiply(0.25));
        
        arcCutOut.setTranslateY(1);
        
        arcActual.getStyleClass().add("slider");
        arcActual.setStartAngle(180);
        arcActual.setType(ArcType.ROUND);
        arcActual.setLength(0);
        arcActual.setMouseTransparent(true);
        
        Arc arcSet = new Arc();
        arcSet.getStyleClass().add("slider-highlight");
        arcSet.setStartAngle(180);
        arcSet.setType(ArcType.ROUND);
        arcSet.setLength(0);
        arcSet.setMouseTransparent(true);

        Arc arcBg = new Arc();
        arcBg.getStyleClass().add("slider-background");
        arcBg.setStartAngle(0);
        arcBg.setType(ArcType.ROUND);
        arcBg.setLength(180);
        
        this.getPane().widthProperty().addListener(framePrefWidthListener);

        arcActual.centerXProperty().bind(frame.widthProperty().divide(2));
        arcActual.centerYProperty().bind(frame.widthProperty().divide(2));

        arcActual.radiusXProperty().bind(frame.widthProperty().divide(2));
        arcActual.radiusYProperty().bind(frame.widthProperty().divide(2));
        
        arcActual.lengthProperty().addListener((ChangeListener<Number>)(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> { 
            curValue.setText(String.valueOf(fromArcToVal(newValue.doubleValue())));
        });
        
        arcSet.centerXProperty().bind(frame.widthProperty().divide(2));
        arcSet.centerYProperty().bind(frame.widthProperty().divide(2));

        arcSet.radiusXProperty().bind(frame.widthProperty().divide(2));
        arcSet.radiusYProperty().bind(frame.widthProperty().divide(2));
        
        arcBg.centerXProperty().bind(frame.widthProperty().divide(2));
        arcBg.centerYProperty().bind(frame.widthProperty().divide(2));

        arcBg.radiusXProperty().bind(frame.widthProperty().divide(2));
        arcBg.radiusYProperty().bind(frame.widthProperty().divide(2));
        
        arcSet.setOpacity(0);
        
        arcBg.setOnMouseDragged((MouseEvent event) -> {
            double threshold = frame.getWidth()/2;
            double xMouse = event.getX()-(threshold);
            double yMouse = event.getY()-(threshold);
            if((xMouse>-threshold && xMouse<threshold) && (yMouse<0.0 && yMouse>=-threshold)){
               // arcSet.setLength();
                arcSet.setLength(-180 + (Math.toDegrees(Math.atan2(-yMouse/arcSet.radiusYProperty().doubleValue(), xMouse/arcSet.radiusXProperty().doubleValue()))));
            }
        });
        
        arcBg.setOnMousePressed((MouseEvent event) -> {
            double threshold = frame.getWidth()/2;
            arcSet.setOpacity(0.6);
            double xMouse = event.getX()-(threshold);
            double yMouse = event.getY()-(threshold);
            if((xMouse>-threshold && xMouse<threshold) && (yMouse<0.0 && yMouse>=-threshold)){
               // arcSet.setLength();
                arcSet.setLength(-180 + (Math.toDegrees(Math.atan2(-yMouse/arcSet.radiusYProperty().doubleValue(), xMouse/arcSet.radiusXProperty().doubleValue()))));
            }
        });
        
        arcBg.setOnMouseReleased((MouseEvent event) -> {
            arcSet.setOpacity(0);
            Runnable runner = () -> {
                try {
                    this.getControl().getControlGroup().getDevice().sendCommand(this.getSliderControl().createSendCommand(new SliderCommand(fromArcToVal(arcSet.getLength()))));
                } catch (DeviceControlCommandException ex) {
                    Logger.getLogger(DashboardDeviceSliderControl.class.getName()).log(Level.SEVERE, null, ex);
                }
            };
            runner.run();
        });
        
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setColor(Color.rgb(0, 0, 0, 0.15));
        innerShadow.setBlurType(BlurType.TWO_PASS_BOX);
        innerShadow.setRadius(0.030 * width);
        innerShadow.setOffsetY(0.030 * width);

        arcBg.setEffect(innerShadow);
        arcActual.setEffect(innerShadow);
        arcSet.setEffect(innerShadow);
        
        StackPane.setAlignment(arcSet, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(arcActual, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(arcBg, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(arcCutOut, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(curValue, Pos.BOTTOM_CENTER);
        
        GridPane.setHalignment(minValue, HPos.CENTER);
        GridPane.setHalignment(maxValue, HPos.CENTER);
        GridPane.setValignment(minValue, VPos.TOP);
        GridPane.setValignment(maxValue, VPos.TOP);
        GridPane.setFillWidth(minValue, true);
        GridPane.setFillWidth(maxValue, true);

        frame.getChildren().addAll(arcBg, arcActual, arcSet, arcCutOut);
        
        gaugeHolder.getChildren().addAll(frame,curValue);
        
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(25);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setPercentWidth(25);
        
        fullGauge.getColumnConstraints().addAll(column1, column2, column3);
        fullGauge.setGridLinesVisible(false);
        fullGauge.setMaxWidth(width);
        
        fullGauge.add(gaugeHolder, 0, 0, 3, 1);
        fullGauge.add(minValue, 0, 1);
        fullGauge.add(maxValue, 2, 1);
        fullGauge.add(controlFullName, 0, 2, 3, 1);
        
        fullGauge.setAlignment(Pos.CENTER);
        
        this.getPane().getChildren().addAll(fullGauge);
        
        frame.setMaxSize(width, height);
        frame.setPrefSize(width, height);
        
        Timer timer = new Timer();  //At this line a new Thread will be created
        timer.schedule( new TimerTask() {
            @Override
            public final void run(){
                if(!updated){
                    animateActualArc(fromValToArc(((Number)getSliderControl().getValueData()).doubleValue()));
                }
            }
        }, 5000); //delay in milliseconds
        getSliderControl().getValueProperty().addPropertyChangeListener(deviceDataListener);
    }
    
    private void framePrefWidthListener(ObservableValue<? extends Number> observable, Number oldValue, Number newValue){
        frame.setPrefWidth(newValue.doubleValue());
    };
 
    private void animateActualArc(final double newValue){
        timeline.stop();
        timeline = new Timeline();
        timeline.setAutoReverse(false);
        final KeyFrame kfTo = new KeyFrame(Duration.seconds(1), new KeyValue(arcActual.lengthProperty(), newValue, Interpolator.SPLINE(0.5, 0.4, 0.4, 1.0)));
        timeline.getKeyFrames().add(kfTo);
        Platform.runLater(() -> {
            timeline.play();
        });
    }
    
    private double fromValToArc(double val){
        double totalRange = getSliderControl().getMax().doubleValue() - getSliderControl().getMin().doubleValue();
        return -(180 * ((1/totalRange)*val));
    }

    private int fromArcToVal(double val){
        /// getThe full range first.
        double totalRange = getSliderControl().getMax().doubleValue() - getSliderControl().getMin().doubleValue();
        return (int)Math.round(-(val/180 * totalRange));
    }
    
    @Override
    public void destruct() {
        this.getPane().widthProperty().removeListener(framePrefWidthListener);
        getSliderControl().getValueProperty().removePropertyChangeListener(deviceDataListener);
    }
    
}