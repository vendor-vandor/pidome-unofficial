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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.When;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.scenes.components.mainstage.displays.visualfloor.FloorsViewManagement.Move;

/**
 *
 * @author John
 */
final class Floor3DGroup extends Xform {
    
    double floorMostLeft = 0;
    double floorMostTop = 0;
    
    double anchorX, anchorY, anchorLocationX, anchorLocationZ, anchorLocationY, anchorAngleX,anchorAngleY,// Rotation and move anchors
           startX=0.0, startY=0.0, startZ=0.0, startAnchorX=0.0, startAnchorY=0.0; /// These are the initial positions when the group is placed on it's initial location
    
    Group groundPlane = new Group();
    
    MeshView floorImage;
    
    VisualFloor visualFloor;
    
    Point2D floorCenterPoint;
    
    Box floorLevelMesh;
    
    Move moveStyle = Move.NONE;
    
    double baseRoomsHeight = 60.0;
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(Floor3DGroup.class);
    
    List<Group> boxedRooms = new ArrayList<>();
    List<Label> roomnames = new ArrayList<>();
    List<Item3DVis> items = new ArrayList<>();
    Map<Integer,Item3DMovement> movements = new HashMap<>();
    Map<Integer,FloorsViewLightIntensity> light = new HashMap<>();
    Map<Integer,FloorsViewTempIntensity> tempr = new HashMap<>();
    List<Box> roomFloors = new ArrayList<>();
    List<Box> walls = new ArrayList<>();
    
    Point2D regionBoundsDiff;
    
    SimpleDoubleProperty fixedLabelAngleY = new SimpleDoubleProperty(0);
    SimpleDoubleProperty fixedLabelAngleX = new SimpleDoubleProperty(0);
    SimpleDoubleProperty fixedLabelAngleZ = new SimpleDoubleProperty(0);
    
    SimpleDoubleProperty pivotXPercentage = new SimpleDoubleProperty(0);
    
    //// rotations as of this moment are at a fixed min and max until better calculations are introduced.
    //// These are for the x and y axis rotations.
    double curXAngle = 0.0;
    double curYAngle = 0.0;
    double maxMin = -90.0;
    double maxMax = 90.0;
    
    /**
     * Constructor creates and places all the objects.
     * The floor center point is the center point of the floor image despite of 
     * out of bounds room positions (for example an outside room boundary).
     * @param visualFloor
     * @throws VisualFloorsAssetItemException 
     */
    protected Floor3DGroup(VisualFloor visualFloor, AmbientLight highlight) throws VisualFloorsAssetItemException {
        this.visualFloor = visualFloor;
        createFloorPlane(visualFloor.getAsset());
        floorCenterPoint = new Point2D(visualFloor.getFloorWidth()/2,visualFloor.getFloorHeight()/2);
        floorMostLeft = (0 - floorCenterPoint.getX());
        floorMostTop = (0 - floorCenterPoint.getY());
        double beforeX = getBoundsInLocal().getMinX();
        double beforeY = getBoundsInLocal().getMinY();
        
        
        /// Code below is because there are labels added which are 2d items and need more handling and are not handled as 
        /// ordinairy 3d models.
        fixedLabelAngleX.bind(new When(ry.angleProperty().lessThan(0))
                                 .then(rx.angleProperty().negate().add(ry.angleProperty().negate().divide(90).multiply(rx.angleProperty())))
                                 .otherwise(rx.angleProperty().negate().add(ry.angleProperty().negate().divide(90).multiply(rx.angleProperty().negate())))
                              );
        fixedLabelAngleY.bind(ry.angleProperty().negate());
        fixedLabelAngleZ.bind(new When(ry.angleProperty().greaterThan(0))
                                 .then(rx.angleProperty().negate().multiply(ry.angleProperty().negate().divide(90)))
                                 .otherwise(rx.angleProperty().negate().multiply(ry.angleProperty().negate().divide(90)))
                              );
        
        Rotate RXVisual = new Rotate();
        RXVisual.setAxis(Rotate.X_AXIS);
        RXVisual.angleProperty().bind(rx.angleProperty().negate());
        Rotate RYVisual = new Rotate();
        RYVisual.setAxis(Rotate.Y_AXIS);
        RYVisual.angleProperty().bind(ry.angleProperty().negate());
        
        for(VisualRoom room:visualFloor.getRooms()){
            
            double roomWidth = room.getWidthProperty().doubleValue();
            double roomHeight = room.getHeightProperty().doubleValue();
            
            Group boxedRoom = createRoom(room.getRoomId(), roomWidth, roomHeight);
            boxedRoom.setTranslateX(room.getXProperty().doubleValue() + (room.getWidthProperty().doubleValue()/2) + floorMostLeft);
            boxedRoom.setTranslateY(room.getYProperty().doubleValue() + (room.getHeightProperty().doubleValue()/2) + floorMostTop);
            boxedRoom.setTranslateZ(-(baseRoomsHeight/2));
            boxedRoom.setUserData(room.getRoomId());
            getChildren().add(boxedRoom);
            boxedRooms.add(boxedRoom);
            Label roomName = new Label(room.getRoomName());
            roomName.setStyle("-fx-font-size: 1.6em; ");
            roomName.setTextFill(Color.web("#D7D8DA"));
            
            DropShadow ds = new DropShadow();
            ds.setOffsetX(0);
            ds.setOffsetY(0);
            ds.setRadius(2);
            ds.setSpread(1);
            ds.setColor(Color.web("#000000"));
            
            roomName.setEffect(ds);
            
            getChildren().add(roomName);
            
            roomName.translateXProperty().bind(room.getXProperty().add(room.getWidthProperty().divide(2)).add(floorMostLeft).subtract(roomName.widthProperty().divide(2)));
            roomName.translateYProperty().bind(room.getYProperty().add(room.getHeightProperty().divide(2)).add(floorMostTop).subtract(roomName.heightProperty().divide(2)));
            
            roomName.setTranslateZ(-35);
            
            Rotate labelRX = new Rotate();
            labelRX.setAxis(Rotate.X_AXIS);
            labelRX.pivotXProperty().bind(pivotXPercentage.multiply(roomName.widthProperty()));
            labelRX.angleProperty().bind(fixedLabelAngleX);
            Rotate labelRY = new Rotate();
            labelRY.setAxis(Rotate.Y_AXIS);
            labelRY.pivotXProperty().bind(pivotXPercentage.multiply(roomName.widthProperty()));
            labelRY.angleProperty().bind(fixedLabelAngleY);
            Rotate labelRZ = new Rotate();
            labelRZ.setAxis(Rotate.Z_AXIS);
            labelRZ.pivotXProperty().bind(pivotXPercentage.multiply(roomName.widthProperty()));
            labelRZ.angleProperty().bind(fixedLabelAngleZ);
            
            roomName.getTransforms().addAll(labelRY, labelRX, labelRZ);
            roomnames.add(roomName);
            
            for(MeshedDevice device:visualFloor.getDevices()){
                Map<String,Device.CommandGroup> cmdGroup = device.getDevice().getCommandGroups();
                for(String groupId: cmdGroup.keySet()){
                    Map<String,Map<String,Object>> setDetails = cmdGroup.get(groupId).getFullSetList();
                    for(String setId:setDetails.keySet()){
                        if(setDetails.get(setId).containsKey("visual") && (boolean)setDetails.get(setId).containsKey("visual")==true){
                            
                            switch(((String)setDetails.get(setId).get("visualtype")).trim()){
                                case "temperature":
                                case "temperatureF":
                                    if(room.getRoomId() == device.getDevice().getLocation()){
                                        FloorsViewTempIntensity temp;
                                        if(!tempr.containsKey(device.getDevice().getLocation())){
                                            temp = new FloorsViewTempIntensity((int)room.getWidthProperty().get(), (int)room.getHeightProperty().get());
                                            temp.setTranslateX(room.getXProperty().doubleValue() + floorMostLeft+10);
                                            temp.setTranslateY(room.getYProperty().doubleValue() + floorMostTop+10);
                                            temp.setTranslateZ(-1);
                                            temp.addDevice(device.getDevice(), groupId, setId);
                                            temp.setVisible(false);
                                            tempr.put(device.getDevice().getLocation(), temp);
                                            getChildren().add(temp);
                                        } else {
                                            tempr.get(device.getDevice().getLocation()).addDevice(device.getDevice(), groupId, setId);
                                        }
                                    }
                                break;
                                case "luxlevel":
                                    if(room.getRoomId() == device.getDevice().getLocation()){
                                        FloorsViewLightIntensity image;
                                        if(!light.containsKey(device.getDevice().getLocation())){
                                            image = new FloorsViewLightIntensity((int)room.getWidthProperty().get(), (int)room.getHeightProperty().get());
                                            image.setTranslateX(room.getXProperty().doubleValue() + floorMostLeft+10);
                                            image.setTranslateY(room.getYProperty().doubleValue() + floorMostTop+10);
                                            image.setTranslateZ(-1);
                                            image.addDevice(device.getDevice(), groupId, setId);
                                            image.setVisible(false);
                                            light.put(device.getDevice().getLocation(), image);
                                            getChildren().add(image);
                                        } else {
                                            light.get(device.getDevice().getLocation()).addDevice(device.getDevice(), groupId, setId);
                                        }
                                    }
                                break;
                                case "move":
                                    LOG.debug("RoomId: {}, device location id: {}", room.getRoomId(), device.getDevice().getLocation());
                                    if(room.getRoomId() == device.getDevice().getLocation()){
                                        Item3DMovement move;
                                        if(!movements.containsKey(device.getDevice().getLocation())){
                                            try {
                                                move = new Item3DMovement(device.getPosX() + floorMostLeft,device.getPosY() + floorMostTop);
                                                move.setRoom(boxedRoom);

                                                move.getTransforms().addAll(RXVisual, RYVisual);
                                                move.translateXProperty().bind(room.getXProperty().add(room.getWidthProperty().divide(2)).add(floorMostLeft).subtract(roomName.widthProperty().divide(2)).add(move.getItem().getImage().getWidth()/2));
                                                move.translateYProperty().bind(room.getYProperty().add(room.getHeightProperty().divide(2)).add(floorMostTop).subtract(roomName.heightProperty().divide(2)));
                                                move.setTranslateZ(-((move.getItem().getImage().getHeight()/2)));

                                                move.addMovementDevice(device.getDevice(), groupId, setId);
                                                movements.put(device.getDevice().getLocation(),move);
                                                getChildren().add(move);
                                            } catch (DomComponentsException ex) {
                                                LOG.error("Problem loading movement visualization: {}", ex.getMessage(), ex);
                                            }
                                        } else {
                                            movements.get(device.getDevice().getLocation()).addMovementDevice(device.getDevice(), groupId, setId);
                                        }
                                    }
                                break;
                            }
                        }
                    }
                }
            }
            
        }
        for(MeshedDevice device:visualFloor.getDevices()){
            
            Item3DVis vis = new Item3DVis();
            for(Group boxedRoom:boxedRooms){
                if((int)boxedRoom.getUserData() == device.getDevice().getLocation()){
                    vis.setRoom(boxedRoom);
                }
            }
            vis.setDevice(device);
            
            vis.setTranslateX(device.getPosX() + floorMostLeft);
            vis.setTranslateY(device.getPosY() + floorMostTop);
            vis.setTranslateZ(-(baseRoomsHeight/2));
            
            vis.getTransforms().addAll(RXVisual, RYVisual);
            items.add(vis);
            
            getChildren().add(vis);
            
        }
        regionBoundsDiff = new Point2D(beforeX - getBoundsInLocal().getMinX(), beforeY - getBoundsInLocal().getMinY());
        for(Item3DVis item: items){
            highlight.getScope().add(item.getItem());
        }
        showFloorImage(true);
    }
    
    protected final int getFloorId(){
        return visualFloor.getFloorId();
    }
    
    protected final String getName(){
        return visualFloor.getName();
    }
    
    protected final int getFloorLevel(){
        return visualFloor.getLevel();
    }
    
    /**
     * Get the group width
     * @return 
     */
    protected final double getWidth(){
        return getBoundsInLocal().getWidth();
    }
    
    /**
     * Get the group height
     * @return 
     */
    protected final double getHeight(){
        return getBoundsInLocal().getHeight();
    }
    
    protected void setStartPosition(double x, double y, double z, double rotateX, double rotateY){
        startX=x;
        startY=y;
        startZ=z;
        startAnchorX=rotateX;
        startAnchorY=rotateY;
    }
    
    protected void setInitPosition(double posY){
        setTranslateX(startX);
        setTranslateY(startY + posY);
        setTranslateZ(startZ);
        ry.setAngle(startAnchorY);
        rx.setAngle(0);
    }
    
    protected Transition goStartPositionAnimated() {

        /* set position movement */
        TranslateTransition translatePositionTransition = new TranslateTransition(Duration.millis(750), this);
        translatePositionTransition.setToX(startX);
        translatePositionTransition.setToY(startY);
        translatePositionTransition.setToZ(startZ);
        translatePositionTransition.setCycleCount(1);
        translatePositionTransition.setAutoReverse(false);
        
        final Timeline timelineRotateX = new Timeline();
        timelineRotateX.setCycleCount(1);
        timelineRotateX.setAutoReverse(false);
        final KeyValue kvX = new KeyValue(rx.angleProperty(), startAnchorX);
        final KeyFrame kfX = new KeyFrame(Duration.millis(750), kvX);
        timelineRotateX.getKeyFrames().add(kfX);
        
        final Timeline timelineRotateY = new Timeline();
        timelineRotateY.setCycleCount(1);
        timelineRotateY.setAutoReverse(false);
        final KeyValue kvY = new KeyValue(ry.angleProperty(), startAnchorY);
        final KeyFrame kfY = new KeyFrame(Duration.millis(750), kvY);
        timelineRotateY.getKeyFrames().add(kfY);
        
        final ParallelTransition parallelSet = new ParallelTransition();
        parallelSet.getChildren().addAll(translatePositionTransition, timelineRotateY, timelineRotateX);
        parallelSet.setCycleCount(1);
        parallelSet.setAutoReverse(false);

        return parallelSet;
    }

    final void showRooms(final boolean show){
        for(Box wall: walls){
            wall.setVisible(show);
        }
        adjustVisHeights(show);
    }
    
    final void showRoomNames(final boolean show){
        for(Label name: roomnames){
            name.setVisible(show);
        }
    }
    
    final void showDevices(final boolean show){
        for(Item3DVis item: items){
            item.setVisible(show);
        }
    }
    
    final void animateDevices(final boolean animate){
        for(Item3DVis item: items){
            item.showInteractions(animate);
        }
    }

    final void showRoomFloors(final boolean show){
        for(Box roomFloor: roomFloors){
            roomFloor.setVisible(show);
        }
    }
    
    final void showFloorImage(final boolean show){
        floorImage.setVisible(show);
        showRoomFloors(!show);
    }
    
    final void deviceInfluences(final boolean show){
        for(Item3DVis item: items){
            item.interactEnvironment(show);
        }
    }
    
    final void movementVisualization(final boolean show){
        for(Item3DMovement item: movements.values()){
            item.setItemActive(show);
        }
    }
    
    final void luxVisualization(final boolean show){
        for(FloorsViewLightIntensity item: light.values()){
            item.setVisible(show);
        }
    }
    
    final void tempVisualization(boolean show){
        for(FloorsViewTempIntensity item: tempr.values()){
            item.setVisible(show);
        }
    }
    
    final void adjustVisHeights(boolean adjust){
        double depth;
        double setX;
        double setY;
        double width;
        double height;
        if(adjust) {
            depth = -baseRoomsHeight;
            setX = +10;
            setY = +10;
            width = -20;
            height = -20;
        } else {
            depth = -1;
            setX = -10;
            setY = -10;
            width = +0;
            height = +0;
        }
        for(FloorsViewLightIntensity item: light.values()){
            setItemHeightsAdjust(item, depth, setX, setY, width, height);
        }
        for(FloorsViewTempIntensity item: tempr.values()){
            setItemHeightsAdjust(item, depth, setX, setY, width, height);
        }
    }
    
    private void setItemHeightsAdjust(ImageView item, double depth, double setX, double setY, double width, double height){
        item.setTranslateZ(+depth);
        item.setTranslateX(item.getTranslateX() + setX);
        item.setTranslateY(item.getTranslateY() + setY);
        try {
            item.setFitWidth(item.getImage().getWidth() + width);
            item.setFitHeight(item.getImage().getHeight() + height);
        } catch (NullPointerException ex) {} //// Not defined yet, walls are needed earlier then the rest.
    }
    
    final Transition animateToPositionYAxis(double positionY){
        /* set position movement */
        TranslateTransition translateYPositionTransition = new TranslateTransition(Duration.millis(750), this);
        translateYPositionTransition.setToY(startY + positionY);
        translateYPositionTransition.setCycleCount(1);
        translateYPositionTransition.setAutoReverse(false);
        
        return translateYPositionTransition;
        
    }
    
    final Transition animateToFront(double positionZ, double rotateX, double positionX){
        /* set position movement */
        TranslateTransition translateZPositionTransition = new TranslateTransition(Duration.millis(750), this);
        translateZPositionTransition.setToZ(startZ + positionZ);
        translateZPositionTransition.setCycleCount(1);
        translateZPositionTransition.setAutoReverse(false);
        
        TranslateTransition translateYPositionTransition = new TranslateTransition(Duration.millis(750), this);
        translateYPositionTransition.setToY(getHeight()/2);
        translateYPositionTransition.setCycleCount(1);
        translateYPositionTransition.setAutoReverse(false);
        
        TranslateTransition translateXPositionTransition = new TranslateTransition(Duration.millis(750), this);
        translateXPositionTransition.setToX(startX + positionX);
        translateXPositionTransition.setCycleCount(1);
        translateXPositionTransition.setAutoReverse(false);
        
        final Timeline timelineRotateX = new Timeline();
        timelineRotateX.setCycleCount(1);
        timelineRotateX.setAutoReverse(false);
        final KeyValue kvX = new KeyValue(rx.angleProperty(), rotateX);
        final KeyFrame kfX = new KeyFrame(Duration.millis(750), kvX);
        timelineRotateX.getKeyFrames().add(kfX);
        
        final ParallelTransition parallelSet = new ParallelTransition();
        parallelSet.getChildren().addAll(translateZPositionTransition, translateYPositionTransition, translateXPositionTransition, timelineRotateX);
        parallelSet.setCycleCount(1);
        parallelSet.setAutoReverse(false);

        return parallelSet;
        
    }
    
    protected void goStartPosition() {
        setTranslateX(startX);
        setTranslateY(startY);
        setTranslateZ(startZ);
        ry.setAngle(startAnchorY);
        rx.setAngle(startAnchorX);
    }
    
    /**
     * Returns the center orientation point based on the floor center position in the group.
     * This returns the translated center position of the group which is based
     * on the total distance from the top left of the group to the center of the floor image.
     * @return 
     */
    protected final Point2D getTranslatedCenterBounds(){
        //// First get the x top left position of the floor in the group and add the groups center point to it.
        double translatedCenterX = Math.abs(getBoundsInLocal().getMinX() + floorCenterPoint.getX());
        //// Then get the Y top position of the floor in the group and add the groups center point to it.
        double translatedCenterY = Math.abs(getBoundsInLocal().getMinY() + floorCenterPoint.getY());
        return new Point2D(translatedCenterX, translatedCenterY);
    }
    
    protected final Point2D getOuterBoundsDiff(){
        return regionBoundsDiff;
    }
    
    protected final Bounds getLocalBounds(){
        return getBoundsInLocal();
    }
    
    /**
     * Set movement style
     * @param style 
     */
    protected final void setMoveStyle(Move style){
        this.moveStyle = style;
    }
    
    /**
     * Currently only "plane" like triangle meshes are used in the 3d space for the base floor.
     * The image used as material defines the size of the base floor plane.
     */
    private void createFloorPlane(Image planeTexture){
        floorImage = VisualFloorUtils.getImagePlane(planeTexture);
        
        getChildren().add(floorImage);
        if(visualFloor.getLevel()==0){
            //floorLevelMesh = boxedMesh(planeTexture.getWidth(), planeTexture.getHeight(), 1, Color.web("#c2c2c2"));
            //floorLevelMesh.setTranslateZ(1);
            //getChildren().add(floorLevelMesh);
        }
        
    }
    
    private final Box boxedMesh (double width, double height, double depth, Color color){
        Box box = new Box(width, height, depth);
        PhongMaterial texturedMaterial = new PhongMaterial();
        texturedMaterial.setDiffuseColor(color);
        box.setMaterial(texturedMaterial);
        return box;
    }

    private Group createRoom(int id, double width, double height){
        
        Group room = new Group();
        
        PhongMaterial texturedMaterial = new PhongMaterial();
        texturedMaterial.setDiffuseMap(new Image("file:resources/images/floorsview/DryWallTexture.png"));
        texturedMaterial.setSpecularPower(38.0);
        
        Box top = new Box(width, 10, baseRoomsHeight);
        top.setMaterial(texturedMaterial);
        top.setTranslateY((-height/2)+5);
        top.setUserData(id);
        walls.add(top);
        
        Box bottom = new Box(width, 10, baseRoomsHeight);
        bottom.setMaterial(texturedMaterial);
        bottom.setTranslateY((height/2)-5);
        bottom.setUserData(id);
        walls.add(bottom);
        
        Box left = new Box(10, height, baseRoomsHeight);
        left.setMaterial(texturedMaterial);
        left.setTranslateX((-width/2)+5);
        left.setUserData(id);
        walls.add(left);
        
        Box right = new Box(10, height, baseRoomsHeight);
        right.setMaterial(texturedMaterial);
        right.setTranslateX((width/2)-5);
        right.setUserData(id);
        walls.add(right);
        
        Box floor = new Box(width, height, 1);
        floor.setMaterial(texturedMaterial);
        floor.setTranslateZ((baseRoomsHeight/2));
        
        roomFloors.add(floor);
        
        room.getChildren().addAll(top,right,bottom,left,floor);
        
        return room;
        
    }
    
    protected final void destroy(){
        boxedRooms.clear();
        roomnames.clear();
        for(Item3DVis item: items){
            item.destroy();
        }
        for(Item3DMovement item: movements.values()){
            item.destroy();
        }
        for(FloorsViewLightIntensity item: light.values()){
            item.destroy();
        }
        for(FloorsViewTempIntensity item: tempr.values()){
            item.destroy();
        }
        items.clear();
        movements.clear();
        light.clear();
        tempr.clear();
    }
    
}
