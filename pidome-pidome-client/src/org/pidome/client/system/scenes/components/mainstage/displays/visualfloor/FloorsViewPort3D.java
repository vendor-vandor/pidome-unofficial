/*
 * Copyright 2014 John.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pidome.client.system.scenes.components.mainstage.displays.visualfloor;

import java.util.ArrayList;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.AmbientLight;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;

/**
 *
 * @author John
 */
final class FloorsViewPort3D extends FloorsViewManagement {

    SubScene scene3D;
    Group paneHolder3D = new Group();

    static Logger LOG = LogManager.getLogger(FloorsViewPort3D.class);

    ArrayList<Floor3DGroup> floorsList = new ArrayList<>();

    Floor3DGroup activeFloor;

    double width, height;

    Floor3DGroup largestWidthGroup, largestHeightGroup;

    boolean animationBusy = true;

    Move curStyle = Move.ROTATE;

    FloorsControl externalRotateActiveListener;

    public FloorsViewPort3D(double width, double height) {
        this.width = width;
        this.height = height;
        setMinSize(width, height);
        setMaxSize(width, height);
        setPrefSize(width, height);
        pleaseWait.setPadding(new Insets(30));
        getChildren().add(pleaseWait);
        try {
            if (!isSupported()) {
                pleaseWait.setText("To use this functionality go to the server's interface\n and create a visual floor plan.");
            } else {
                pleaseWait.setText("Please wait, loading floors");
            }
        } catch (VisualFloorsAssetItemException ex) {
            pleaseWait.setText(ex.getMessage());
        }
    }

    @Override
    protected void buildScene() {
        try {
            AmbientLight highlight = new AmbientLight();
            for (VisualFloor floor : getFloorsAsList()) {
                Floor3DGroup group = new Floor3DGroup(floor, highlight);
                if (largestWidthGroup == null || largestWidthGroup.getWidth() < group.getWidth()) {
                    largestWidthGroup = group;
                }
                if (largestHeightGroup == null || largestHeightGroup.getHeight() < group.getHeight()) {
                    largestHeightGroup = group;
                }
                floorsList.add(group);
                group.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent t) -> {
                    if (activeFloor == null) {
                        setFloorActive(floorsList.lastIndexOf(group));
                        curStyle = Move.ROTATE;
                        if (externalRotateActiveListener != null) {
                            externalRotateActiveListener.setRotateActive(group.getFloorId());
                        }
                        t.consume();
                    }
                });
            }
            paneHolder3D.getChildren().addAll(floorsList);
            paneHolder3D.getChildren().add(highlight);
            getChildren().remove(pleaseWait);
            getChildren().add(getSubScene());
            setStartPositions();
            showRegions(false);
            showRoomNames(false);
            if (DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)) {
                SequentialTransition seqT = new SequentialTransition();
                for (Floor3DGroup floor : floorsList) {
                    seqT.getChildren().add(floor.goStartPositionAnimated());
                    seqT.setOnFinished((ActionEvent t) -> {
                        animationBusy = false;
                        if (floorsList.size() == 1) {
                            setFloorActive(0);
                            curStyle = Move.ROTATE;
                            if (externalRotateActiveListener != null) {
                                externalRotateActiveListener.setRotateActive(floor.getFloorId());
                            }
                        }
                    });
                }
                seqT.play();
            } else {
                for (Floor3DGroup floor : floorsList) {
                    floor.goStartPosition();
                }
                animationBusy = false;
            }
        } catch (VisualFloorsAssetItemException ex) {
            LOG.error("Could not load first floor: {}", ex.getMessage());
        }
        pleaseWait.setText("Could not start");
    }

    @Override
    protected boolean isAnimating() {
        return animationBusy;
    }

    protected final void setExternalFloorActiveListener(FloorsControl externalRotateActiveListener) {
        this.externalRotateActiveListener = externalRotateActiveListener;
    }

    @Override
    final void setFloorActive(int floorId) {
        if (!animationBusy) {
            if (activeFloor == null) {
                activeFloor = floorsList.get(floorId);
                moveOutFromSet(floorId).play();
            } else if (activeFloor.getFloorId() != floorsList.get(floorId).getFloorId()) {
                SequentialTransition seqT = new SequentialTransition();
                seqT.getChildren().addAll(resetFloorsFromFocus(activeFloor), moveOutFromSet(floorId));
                seqT.play();
            }
            animationBusy = true;
        }
    }

    private Transition resetFloorsFromFocus(Floor3DGroup resetFloor) {
        resetFloor.setMoveStyle(Move.NONE);
        setActive(false);
        SequentialTransition seqT = new SequentialTransition();
        ParallelTransition parT = new ParallelTransition();
        for (Floor3DGroup floor : floorsList) {
            if (resetFloor != floor) {
                parT.getChildren().add(floor.goStartPositionAnimated());
            }
        }
        seqT.getChildren().addAll(resetFloor.goStartPositionAnimated(), parT);
        seqT.setOnFinished((ActionEvent t) -> {
            activeFloor = null;
        });
        return seqT;
    }

    private Transition moveOutFromSet(int floorId) {
        SequentialTransition seqT = new SequentialTransition();
        ParallelTransition parT = new ParallelTransition();

        for (Floor3DGroup floor : floorsList) {
            if (floor.getFloorLevel() < floorsList.get(floorId).getFloorLevel()) {
                parT.getChildren().add(floor.animateToPositionYAxis(height * 2));
            } else if (floor.getFloorLevel() > floorsList.get(floorId).getFloorLevel()) {
                parT.getChildren().add(floor.animateToPositionYAxis(-height * 2));
            }
        }

        /// Move to the front, set it closer to the camera and center the floor.
        /// Centering the floor in front of the camera needs consideration of out of bounds
        /// regions on other floors.
        double floorWidthDiff = (largestWidthGroup.getWidth() - floorsList.get(floorId).getWidth()) / 2;
        Transition trans = floorsList.get(floorId).animateToFront(-floorsList.get(floorId).getHeight() / 3, -30, -floorWidthDiff);

        trans.setOnFinished((ActionEvent t) -> {
            activeFloor = floorsList.get(floorId);
            activeFloor.setMoveStyle(curStyle);
            setActive(true);
            animationBusy = false;
        });

        seqT.getChildren().addAll(parT, trans);
        return seqT;
    }

    @Override
    int getActiveFloorId() {
        if (activeFloor != null) {
            return activeFloor.getFloorId();
        } else {
            return 0;
        }
    }

    final SubScene getSubScene() {
        if (DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)) {
            scene3D = new SubScene(paneHolder3D, width, height, true, SceneAntialiasing.BALANCED);
        } else {
            scene3D = new SubScene(paneHolder3D, width, height, true, SceneAntialiasing.DISABLED);
        }
        paneHolder3D.setDepthTest(DepthTest.ENABLE);
        PointLight defaultLight = new PointLight(Color.WHITE);
        defaultLight.setColor(Color.web("#dddddd"));
        defaultLight.setLightOn(true);
        defaultLight.setLayoutX(width / 2);
        defaultLight.setLayoutY(height / 2);
        defaultLight.setRotationAxis(Rotate.X_AXIS);
        defaultLight.setTranslateZ(-1400);
        defaultLight.setRotate(90);
        paneHolder3D.getChildren().add(defaultLight);
        scene3D.setFill(Color.TRANSPARENT);
        PerspectiveCamera camera = new PerspectiveCamera(false);
        scene3D.setCamera(camera);
        return scene3D;
    }

    /**
     * Based on the largest floor plane set the default distance and angles
     * against a steady camera. The positions are based on the largest floor
     * group but with the next kept in consideration: The image used to define a
     * floor (and not the room regions) has a center point. This center point is
     * leading in aligning the stacked groups.
     * <br/>
     * This means a room image with flat dimensions 100x100 will have its center
     * point at 50*50. An image with 200x200 will have it's center point at
     * 100x100. These center points will be stacked right above each other.
     * <br/>
     * When a floor image of 100X100 is in a group of 200X100, the center point
     * will be at 150x100 if the base floor image is at it's most right top
     * position.
     * <br/>
     * Next to the above is the start position for each group is starting at 15
     * (-75) degrees. Where the front is on Y up (-y/2), and the back is Y(+y/2)
     * up based on the degrees at -y/2 = -z/2. With calculating the z axis depth
     * length of the largest group the z+ position can be set for all the floor
     * groups. Need to keep in mind that the center Point3D is at x/2,y/2,z/2
     */
    final void setStartPositions() {
        //// To know where the floors center position should be, we need to get the largest width and height positions (2D)
        //// These are in group positioned bounds and based on x = 0 and y = 0.
        double largestFloorCenterX = largestWidthGroup.getTranslatedCenterBounds().getX();/// Center of the floor width including drawed room width (outer) bounds.
        double largestFloorCenterY = largestHeightGroup.getTranslatedCenterBounds().getY();/// Center of the floor height including drawed room height (outer) bounds.

        double startAngle = -62; /// Always more then -90 degrees, othewise the ground plane image disappears because it is a one sided mesh (now).

        //// to create a nice distance from the camera (this setup) we need to calculate the distance from the lower front
        //// to the higher back measured over the z+ axis.
        //// zero degrees in this setup is y (straight up). Only we measure from the ground (z axis).
        //// So the angle then would be abs(-75 + 90) = 15.
        //// We have an 15 degree angle at the front. To get the length at the z-axis this then would be:
        //// Cosinus(15(degr.)) = z-axis / y-axis (2D point because the height of the image is set on the y up axis).
        //// 
        zAxis length = (angle, yAxis) -> Math.cos(Math.toRadians(angle)) * yAxis;

        double zLength = length.calc(Math.abs(startAngle + 90), largestHeightGroup.getHeight());
        double largestWidth = largestWidthGroup.getLocalBounds().getWidth() / 2;

        for (Floor3DGroup floor : floorsList) {
            //// Get the difference between the largest and the current pivot x point
            double translate;
            if (largestWidthGroup != floor) {
                translate = (largestWidth - (floor.getLocalBounds().getWidth() / 2));
            } else {
                translate = floor.getOuterBoundsDiff().getX() / 2;
            }
            floor.setStartPosition(width / 2 + translate,
                    (height / 2 + (largestFloorCenterY / 2)) - (70 * floor.getFloorLevel()),
                    zLength, startAngle, 0);
            floor.setInitPosition(-zLength * 2);
        }
    }

    @Override
    void setMoveStyle(Move style) {
        curStyle = style;
        if (activeFloor != null) {
            activeFloor.setMoveStyle(curStyle);
        }
        if (style == Move.NONE && activeFloor != null) {
            resetFloorsFromFocus(activeFloor).play();
        }
    }

    private void sceneMousePressed(MouseEvent event) {
        if (curStyle != Move.NONE && !animationBusy) {
            activeFloor.anchorX = event.getSceneX();
            activeFloor.anchorY = event.getSceneY();
            activeFloor.anchorLocationX = activeFloor.getTranslateX();
            activeFloor.anchorLocationY = activeFloor.getTranslateY();
            activeFloor.anchorLocationZ = activeFloor.getTranslateZ();
            activeFloor.anchorAngleX = activeFloor.ry.getAngle();
            activeFloor.anchorAngleY = activeFloor.rx.getAngle();
        }
        event.consume();
    }

    protected final void setActive(boolean active) {
        if (active) {
            scene3D.addEventHandler(MouseEvent.MOUSE_PRESSED, this::sceneMousePressed);
            scene3D.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::sceneMouseDragged);
        } else {
            scene3D.removeEventHandler(MouseEvent.MOUSE_PRESSED, this::sceneMousePressed);
            scene3D.removeEventHandler(MouseEvent.MOUSE_DRAGGED, this::sceneMouseDragged);
        }
    }

    private void sceneMouseDragged(MouseEvent event) {
        switch (curStyle) {
            case ROTATE:
                activeFloor.curXAngle = activeFloor.anchorAngleX + (activeFloor.anchorX - event.getSceneX()) / 2;
                activeFloor.curYAngle = activeFloor.anchorAngleY - (activeFloor.anchorY - event.getSceneY()) / 2;
                if (activeFloor.curXAngle > activeFloor.maxMin && activeFloor.curXAngle < activeFloor.maxMax) {
                    activeFloor.ry.setAngle(activeFloor.curXAngle);
                        //// To get the pivot x point for the rotation. This goes from -90 to 90 so over a
                    //// range of 180 which is mapped from 0 to 100. The max percentage will be 99.99
                    activeFloor.pivotXPercentage.setValue(((activeFloor.curXAngle + 90) * 0.5555) / 100);
                }
                if (activeFloor.curYAngle > activeFloor.maxMin && activeFloor.curYAngle < activeFloor.maxMax) {
                    activeFloor.rx.setAngle(activeFloor.curYAngle);
                }
                break;
            case PAN:
                activeFloor.setTranslateX(activeFloor.anchorLocationX - (activeFloor.anchorX - event.getSceneX()));
                activeFloor.setTranslateY(activeFloor.anchorLocationY - (activeFloor.anchorY - event.getSceneY()) * 2);
                break;
            case ZOOM:
                activeFloor.setTranslateZ(activeFloor.anchorLocationZ + (activeFloor.anchorY - event.getSceneY()) * 2);
                break;
        }
        event.consume();
    }

    @Override
    boolean hasActiveFloor() {
        return activeFloor != null;
    }

    @Override
    void showRegions(boolean show) {
        for (Floor3DGroup floor : floorsList) {
            floor.showRooms(show);
        }
    }

    @Override
    void showFloorImage(boolean show) {
        for (Floor3DGroup floor : floorsList) {
            floor.showFloorImage(show);
        }
    }

    @Override
    void showDevices(boolean show) {
        for (Floor3DGroup floor : floorsList) {
            floor.showDevices(show);
        }
    }

    @Override
    void animateDevices(boolean animate) {
        for (Floor3DGroup floor : floorsList) {
            floor.animateDevices(animate);
        }
    }

    @Override
    void showRoomNames(boolean show) {
        for (Floor3DGroup floor : floorsList) {
            floor.showRoomNames(show);
        }
    }

    @Override
    void destroy() {
        if(floorsList.size()>0){
            setActive(false);
            for (Floor3DGroup floor : floorsList) {
                floor.destroy();
            }
        }
    }

    @Override
    void deviceInfluences(boolean show) {
        for (Floor3DGroup floor : floorsList) {
            floor.deviceInfluences(show);
        }
    }

    @Override
    void movementVisualization(boolean show){
        for (Floor3DGroup floor : floorsList) {
            floor.movementVisualization(show);
        }        
    }

    @Override
    void luxVisualization(boolean show){
        for (Floor3DGroup floor : floorsList) {
            floor.luxVisualization(show);
        }   
    }
    
    @Override
    void tempVisualization(boolean show){
        for (Floor3DGroup floor : floorsList) {
            floor.tempVisualization(show);
        }   
    }
    
    interface zAxis {
        double calc(double angle, double yAxis);
    }

}
