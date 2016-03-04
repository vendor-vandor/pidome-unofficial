/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.visuals.controls.devices;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import org.pidome.client.entities.devices.DeviceColorPickerControl.CommandButton;

/**
 * Device bound color picker.
 * This color picker has been generously posted at http://stackoverflow.com/questions/27171885/display-custom-color-dialog-directly-javafx-colorpicker by José Pereda. This code is used with permission and for this many thanks!
 * 
 * Some small modifications are made where the combobox has replaced the new color rectangle with the width bound to the hue selection.
 * 
 * @author John
 */
public class VisualDeviceColorPickerHueControl extends VBox {

    private final ObjectProperty<Color> currentColorProperty = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> customColorProperty  = new SimpleObjectProperty<>(Color.TRANSPARENT);

    private Pane colorRect;
    private final Pane colorBar;
    private final Pane colorRectOverlayOne;
    private final Pane colorRectOverlayTwo;
    private Region colorRectIndicator;
    private final Region colorBarIndicator;

    private DoubleProperty hue = new SimpleDoubleProperty(-1);
    private DoubleProperty sat = new SimpleDoubleProperty(-1);
    private DoubleProperty bright = new SimpleDoubleProperty(-1);

    private DoubleProperty alpha = new SimpleDoubleProperty(100) {
        @Override protected void invalidated() {
            setCustomColor(new Color(getCustomColor().getRed(), getCustomColor().getGreen(), 
                    getCustomColor().getBlue(), clamp(alpha.get() / 100)));
        }
    };

    final ComboBox newColorRect = new ComboBox();
    
    public VisualDeviceColorPickerHueControl(List<CommandButton> commands) {

        getStyleClass().add("my-custom-color");

        VBox box = new VBox();

        box.getStyleClass().add("color-rect-pane");
        customColorProperty().addListener((ov, t, t1) -> colorChanged());

        colorRectIndicator = new Region();
        colorRectIndicator.setId("color-rect-indicator");
        colorRectIndicator.setManaged(false);
        colorRectIndicator.setMouseTransparent(true);
        colorRectIndicator.setCache(true);

        final Pane colorRectOpacityContainer = new StackPane();

        colorRect = new StackPane();
        colorRect.getStyleClass().addAll("color-rect");

        Pane colorRectHue = new Pane();
        colorRectHue.backgroundProperty().bind(new ObjectBinding<Background>() {

            {
                bind(hue);
            }

            @Override protected Background computeValue() {
                return new Background(new BackgroundFill(
                        Color.hsb(hue.getValue(), 1.0, 1.0), 
                        CornerRadii.EMPTY, Insets.EMPTY));

            }
        });            

        colorRectOverlayOne = new Pane();
        colorRectOverlayOne.getStyleClass().add("color-rect");
        colorRectOverlayOne.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, 
                new Stop(0, Color.rgb(255, 255, 255, 1)), 
                new Stop(1, Color.rgb(255, 255, 255, 0))), 
                CornerRadii.EMPTY, Insets.EMPTY)));

        colorRectOverlayTwo = new Pane();
        colorRectOverlayTwo.getStyleClass().addAll("color-rect");
        colorRectOverlayTwo.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, 
                new Stop(0, Color.rgb(0, 0, 0, 0)), new Stop(1, Color.rgb(0, 0, 0, 1))), 
                CornerRadii.EMPTY, Insets.EMPTY)));
        
        colorRectOverlayTwo.setOnMouseDragged((event) -> {
            final double x = event.getX();
            final double y = event.getY();
            sat.set(clamp(x / colorRect.getWidth()) * 100);
            bright.set(100 - (clamp(y / colorRect.getHeight()) * 100));
            updateHSBColor();
        });
        colorRectOverlayTwo.setOnMousePressed((event) -> {
            final double x = event.getX();
            final double y = event.getY();
            sat.set(clamp(x / colorRect.getWidth()) * 100);
            bright.set(100 - (clamp(y / colorRect.getHeight()) * 100));
            updateHSBColor();
        });

        Pane colorRectBlackBorder = new Pane();
        colorRectBlackBorder.setMouseTransparent(true);
        colorRectBlackBorder.getStyleClass().addAll("color-rect", "color-rect-border");

        colorBar = new Pane();
        colorBar.getStyleClass().add("color-bar");
        colorBar.setBackground(new Background(new BackgroundFill(createHueGradient(), CornerRadii.EMPTY, Insets.EMPTY)));

        colorBarIndicator = new Region();
        colorBarIndicator.setId("color-bar-indicator");
        colorBarIndicator.setMouseTransparent(true);
        colorBarIndicator.setCache(true);

        colorRectIndicator.layoutXProperty().bind(sat.divide(100).multiply(colorRect.widthProperty()));
        colorRectIndicator.layoutYProperty().bind(Bindings.subtract(1, bright.divide(100)).multiply(colorRect.heightProperty()));
        colorBarIndicator.layoutXProperty().bind(hue.divide(360).multiply(colorBar.widthProperty()));
        colorRectOpacityContainer.opacityProperty().bind(alpha.divide(100));

        colorBar.setOnMouseDragged((event) -> {
            final double x = event.getX();
            hue.set(clamp(x / colorRect.getWidth()) * 360);
            updateHSBColor();
        });
        colorBar.setOnMousePressed((event) -> {
            final double x = event.getX();
            hue.set(clamp(x / colorRect.getWidth()) * 360);
            updateHSBColor();
        });

        List<String> commandSet = new ArrayList<>();
        for(CommandButton command:commands){
            commandSet.add(command.getLabel());
        }
        newColorRect.getItems().addAll(commandSet);     
        newColorRect.getSelectionModel().select(commandSet.get(0));
        newColorRect.setMinWidth(Control.USE_PREF_SIZE);
        newColorRect.setMaxWidth(Control.USE_PREF_SIZE);
        
        colorBar.widthProperty().addListener((ChangeListener<Number>)(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            newColorRect.setPrefWidth(newValue.doubleValue());
        });
        newColorRect.setId("new-color");
        newColorRect.backgroundProperty().bind(new ObjectBinding<Background>() {
            {
                bind(customColorProperty);
            }
            @Override protected Background computeValue() {
                return new Background(new BackgroundFill(customColorProperty.get(), CornerRadii.EMPTY, Insets.EMPTY));
            }
        });

        colorBar.getChildren().setAll(colorBarIndicator);
        colorRectOpacityContainer.getChildren().setAll(colorRectHue, colorRectOverlayOne, colorRectOverlayTwo);
        colorRect.getChildren().setAll(colorRectOpacityContainer, colorRectBlackBorder, colorRectIndicator);
        VBox.setVgrow(colorRect, Priority.ALWAYS);
        box.getChildren().addAll(colorBar, colorRect, newColorRect);
        VBox.setVgrow(box, Priority.ALWAYS);
        getChildren().add(box);

        if (currentColorProperty.get() == null) {
            currentColorProperty.set(Color.TRANSPARENT);
        }
        updateValues();

    }

    public final String getSelectedCommand(){
       return (String) this.newColorRect.getSelectionModel().getSelectedItem();
    }
    
    private void updateValues() {
        hue.set(getCurrentColor().getHue());
        sat.set(getCurrentColor().getSaturation()*100);
        bright.set(getCurrentColor().getBrightness()*100);
        alpha.set(getCurrentColor().getOpacity()*100);
        setCustomColor(Color.hsb(hue.get(), clamp(sat.get() / 100), 
                clamp(bright.get() / 100), clamp(alpha.get()/100)));
    }

    private void colorChanged() {
        hue.set(getCustomColor().getHue());
        sat.set(getCustomColor().getSaturation() * 100);
        bright.set(getCustomColor().getBrightness() * 100);
    }

    private void updateHSBColor() {
        Color newColor = Color.hsb(hue.get(), clamp(sat.get() / 100), 
                        clamp(bright.get() / 100), clamp(alpha.get() / 100));
        setCustomColor(newColor);
    }

    @Override 
    protected void layoutChildren() {
        super.layoutChildren();            
        colorRectIndicator.autosize();
    }

    static double clamp(double value) {
        return value < 0 ? 0 : value > 1 ? 1 : value;
    }

    private static LinearGradient createHueGradient() {
        double offset;
        Stop[] stops = new Stop[255];
        for (int x = 0; x < 255; x++) {
            offset = (double)((1.0 / 255) * x);
            int h = (int)((x / 255.0) * 360);
            stops[x] = new Stop(offset, Color.hsb(h, 1.0, 1.0));
        }
        return new LinearGradient(0f, 0f, 1f, 0f, true, CycleMethod.NO_CYCLE, stops);
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColorProperty.set(currentColor);
        updateValues();
    }

    public final Color getCurrentColor() {
        return currentColorProperty.get();
    }

    final ObjectProperty<Color> customColorProperty() {
        return customColorProperty;
    }

    void setCustomColor(Color color) {
        customColorProperty.set(color);
    }

    public Color getCustomColor() {
        return customColorProperty.get();
    }
}