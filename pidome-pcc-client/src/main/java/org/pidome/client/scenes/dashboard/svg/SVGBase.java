/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard.svg;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author John
 */
public abstract class SVGBase extends StackPane {
    
    private final SVGPath svgShape;
    private final double baseHeight;
    
    private SVGBase stackedItem;
    
    private boolean hasFill = false;
    
    public SVGBase(String data, double baseHeight){
        this(data, baseHeight, true, true);
        this.getStyleClass().add("svg-base");
    }
    
    public SVGBase(String data, double baseHeight, boolean asBg, boolean fill){
        this.baseHeight = baseHeight;
        this.hasFill = fill;
        svgShape = new SVGPath();
        if(this.hasFill){
            svgShape.setFill(Paint.valueOf(Color.WHITESMOKE.toString()));
        } else {
            svgShape.setFill(Paint.valueOf(Color.TRANSPARENT.toString()));
            svgShape.setStroke(Paint.valueOf(Color.WHITESMOKE.toString()));
        }
        if(asBg) svgShape.setOpacity(0.2);
        svgShape.setContent(data);
        svgShape.getStyleClass().add("svg-shape");
    }
    
    public SVGBase(String data, double baseHeight, boolean asBg, boolean fill, double strokewidth){
        this.baseHeight = baseHeight;
        svgShape = new SVGPath();
        svgShape.setStrokeWidth(strokewidth);
        if(fill){
            svgShape.setFill(Paint.valueOf(Color.WHITESMOKE.toString()));
        } else {
            svgShape.setFill(Paint.valueOf(Color.TRANSPARENT.toString()));
            svgShape.setStroke(Paint.valueOf(Color.WHITESMOKE.toString()));
        }
        if(asBg) svgShape.setOpacity(0.2);
        svgShape.setContent(data);
        svgShape.getStyleClass().add("svg-shape");
    }
    
    public final void updateFill(Paint fill){
        if(this.hasFill){
            svgShape.setFill(fill);
        } else {
            svgShape.setStroke(fill);
        }
        svgShape.getStyleClass().remove("svg-shape");
        svgShape.getStyleClass().add("svg-shape");
    }
    
    public final void updateOpacity(double amount){
        svgShape.setOpacity(amount);
    }
    
    public final void build(double width, double height){
        double scale = ((100/baseHeight) * height)/100;
        svgShape.setScaleX(scale);
        svgShape.setScaleY(scale);
        this.setMinSize(width,height);
        this.setMaxSize(width,height);
        this.setPrefSize(width,height);
        if(stackedItem!=null){
            stackedItem.build(width, height);
            getChildren().addAll(svgShape, stackedItem.getSVG().getChildren().get(0));
        } else {
            getChildren().add(svgShape);
        }
    }
    
    public final SVGPath getClipSVG(){
        return svgShape;
    }
    
    public final void stack(SVGBase svg){
        stackedItem = svg;
    }
    
    public final StackPane getSVG(){
        return this;
    }
    
}