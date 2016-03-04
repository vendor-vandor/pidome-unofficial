/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

/**
 *
 * @author John Sirach
 */
public class ServerGlobalState extends SubScene {

    SubScene scene;
    static Pane content = new Pane();
    Sphere sphere = new Sphere(40);
    PerspectiveCamera camera = new PerspectiveCamera(true);
    
    final Group axisGroup = new Group();
    
    Group lightGroup = new Group();
    
    public ServerGlobalState(){
        super(content, 300, 300, true, SceneAntialiasing.DISABLED);
        content.setBackground(Background.EMPTY);
        this.setFill(Color.TRANSPARENT);
        camera.setFieldOfView(62);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-100);
        setCamera(camera);
        setLayoutX(600);
        setLayoutY(200);
        sphere.setTranslateZ(40);
        content.getChildren().add(sphere);
        
        PointLight light = new PointLight();
        light.setColor(Color.RED);
        lightGroup.setTranslateZ(-75);
        lightGroup.setTranslateX(-75);
        lightGroup.setTranslateY(-75);
        lightGroup.getChildren().add(light);
        content.getChildren().add(lightGroup);
        animate();
    }
    
    final void animate(){
        Timeline animation = new Timeline();
        animation.getKeyFrames().addAll(
                new KeyFrame(Duration.seconds(3),
                        new KeyValue(lightGroup.translateXProperty(), 75)
                ),
                new KeyFrame(Duration.seconds(6),
                        new KeyValue(lightGroup.translateYProperty(), 75)
                ),
                new KeyFrame(Duration.seconds(9),
                        new KeyValue(lightGroup.translateXProperty(), -75)
                ),
                new KeyFrame(Duration.seconds(12),
                        new KeyValue(lightGroup.translateYProperty(), -75)
                ));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();
    }
    
}
