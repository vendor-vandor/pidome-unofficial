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

//// Editor initial initialization
var floorCanvas = document.getElementById("floorcanvas");
var floorStage = new createjs.Stage(floorCanvas);
floorStage.enableMouseOver();
updateStage = false;

floorStage.snapToPixelEnabled = true;

createjs.Ticker.addEventListener("tick", tick);
createjs.Ticker.setFPS(60);
function tick(event) {
    if (updateStage) {
        floorStage.update(event);
        updateStage = false;
    }
}

var floor = new createjs.Container();
var activeMouseButtonButton = 0;
floorStage.addChild(floor);

floorImageContainer = new createjs.Container();
floor.addChild(floorImageContainer);

floorRoomsContainer = new createjs.Container();
floor.addChild(floorRoomsContainer);

floorDevicesContainer = new createjs.Container();
floor.addChild(floorDevicesContainer);

var containerOffset = {x: 0, y: 0};
var resizeRoomOffset = {width: 0, height: 0};
var resizeTextOffset = {x: 0, y: 0};
var moveDeviceOffset = {x: 0, y: 0};

var moveFloor = false;

var drawRoom = false;
var deleteRoom = false;
var moveRoom = false;
var resizeRoom = false;

var drawDevice = false;
var moveDevice = false;
var deleteDevice = false;

var scaleFactor = 1.1;
var zoomFactor = 100;

var drawingRoom = false;

var rooms = [];
var devices = [];

$(document).ready(function() {
    
    var handleScroll = function(event){
        if(moveFloor){
            var zoom;
            if (Math.max(-1, Math.min(1, (event.wheelDelta || -event.detail))) > 0)
                zoom = 1.1;
            else
                zoom = 1 / 1.1;
            floorStage.scaleX = floorStage.scaleY *= zoom;
            zoomFactor = (zoomFactor * zoom);
            $("#zoomfactorpage").html(Math.round(zoomFactor) + " %");
            updateStage = true;
        }
    };
    floorCanvas.addEventListener('DOMMouseScroll',handleScroll,false);
    floorCanvas.addEventListener('mousewheel',handleScroll,false);
    $("#zoomfactorpage").html("100 %");

    //// Floors list
    var allFloorsSource = {
        datatype: "json",
        datafields: [
            {name: 'id', type: 'int'},
            {name: 'name', type: 'string'},
            {name: 'image', type: 'string'},
            {name: 'level', type: 'int'}
        ],
        url: '/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "LocationService.getFloors", "id":"LocationService.getFloors"}',
        root: "result>data"
    };
    var allFloorsData = new $.jqx.dataAdapter(allFloorsSource);
    $("#floorslist").jqxListBox({selectedIndex: 0, source: allFloorsData, displayMember: "name", valueMember: "id", width: 175, height: 150, theme: siteSettings.getTheme()});
    $('#floorslist').on('select', function (event) {
        floorImageContainer.removeAllChildren();
        floorRoomsContainer.removeAllChildren();
        floorDevicesContainer.removeAllChildren();
        var args = event.args;
        if (args) {
            var index = args.index;
            if(allFloorsData.records[index].image!=="" && allFloorsData.records[index].image!==null){
                setFloorImage(allFloorsData.records[index].image, false);
                disableAllToolbarButtons();
                $("#floormove").addClass("active");
                moveFloor = true;
                var getRooms = "{\"jsonrpc\": \"2.0\", \"method\": \"LocationService.getLocationsByFloor\", \"params\": {\"id\":"+allFloorsData.records[index].id+"} \"id\": \"LocationService.getLocationsByFloor\"}";
                $.get("/jsonrpc.json?rpc=" + getRooms)
                    .done(function(data) {
                        try {
                            for (var i = 0; i < data.result.data.length; i++) {
                                var roomObject = data.result.data[i];
                                if(roomObject.screenX!==0 && roomObject.screenY!==0 && roomObject.screenW!==0 && roomObject.screenH!==0){
                                    ///alert(JSON.stringify(roomObject));
                                    var outline = new createjs.Shape();
                                    outline.snapToPixelEnabled = true;
                                    outline.x = roomObject.screenX;
                                    outline.y = roomObject.screenY;
                                    outline.graphics.clear().beginFill("rgba(255,0,0,0.2)").beginStroke("red").drawRect(0, 0, roomObject.screenW, roomObject.screenH);
                                    outline.setBounds(0, 0, roomObject.screenW, roomObject.screenH);
                                    floorRoomsContainer.addChild(outline);
                                    var text = new createjs.Text(roomObject.name, "14px Arial", "#000000");
                                    text.textBaseline = "top";
                                    text.snapToPixelEnabled = true;
                                    text.x = roomObject.screenX + ((roomObject.screenW/2) - (text.getMeasuredWidth()/2));
                                    text.y = roomObject.screenY + ((roomObject.screenH/2) - (text.getMeasuredHeight()/2));
                                    
                                    var hit = new createjs.Shape();
                                    hit.graphics.beginFill("#000").drawRect(0, 0, text.getMeasuredWidth(), text.getMeasuredHeight());
                                    text.hitArea = hit;
                                    
                                    floorRoomsContainer.addChild(text);
                                    setRoomHandlers(roomObject.id, text, hit, outline);
                                    rooms.push(roomObject.id);
                                    updateStage = true;
                                }
                            }
                        } catch(err){
                            showErrorMessage("Room builds error", "Could not save image: " + err);
                        }
                    }, "json");
                    
                    
                var getDevices = "{\"jsonrpc\": \"2.0\", \"method\": \"DeviceService.getVisualDevices\", \"params\": {\"floorid\":"+allFloorsData.records[index].id+"} \"id\": \"DeviceService.getVisualDevices\"}";
                $.get("/jsonrpc.json?rpc=" + getDevices)
                    .done(function(data) {
                        try {
                            for (var i = 0; i < data.result.data.length; i++) {
                                var deviceObject = data.result.data[i];
                                if(deviceObject.screenX!==0 && deviceObject.screenY!==0){
                                    ///alert(JSON.stringify(roomObject));
                                    setDeviceImage(deviceObject.id,deviceObject.name,deviceObject.screenX, deviceObject.screenY);
                                    devices.push(deviceObject.id);
                                    updateStage = true;
                                }
                            }
                        } catch(err){
                            showErrorMessage("Room builds error", "Could not save image: " + err);
                        }
                    }, "json");
                    
            }
        }
        updateStage = true;
    });

    function disableAllToolbarButtons(){
        $(".toolbarbutton").each(function(index, elem) {
            $(this).removeClass("active");
        });
        drawRoom = false;
        deleteRoom = false;
        moveRoom = false;
        resizeRoom = false;

        moveFloor = false;

        drawDevice = false;
        moveDevice = false;
        deleteDevice = false;
    }

    $(".toolbarbutton").on("click", function() {
        if(floorImageContainer.getNumChildren ()!==0){
            disableAllToolbarButtons();
            $(this).addClass("active");
            switch ($(this).attr("id")) {
                case "roommove":
                    moveRoom = true;
                    break;
                case "roomdraw":
                    drawRoom = true;
                    break;
                case "roomdelete":
                    deleteRoom = true;
                    break;
                case "roomdresize":
                    resizeRoom = true;
                    break;
                case "floormove":
                    moveFloor = true;
                    break;
                case "devicedraw":
                    drawDevice = true;
                    break;
                case "devicemove":
                    moveDevice = true;
                    break;
                case "devicedelete":
                    deleteDevice = true;
                    break;
            }
        } else {
            showInfoMessage("Editor", "Add a floor picture first");
        }
    });


    //// Layer visibility options
    var possibleLayers = [
        "Floor background",
        "Rooms",
        "Devices/Items"];
    $("#layerslist").jqxListBox({source: possibleLayers, multiple: true, width: 175, height: 78, theme: siteSettings.getTheme()});

    $("#layerslist").jqxListBox('selectIndex', 0);
    $("#layerslist").jqxListBox('selectIndex', 1);
    $("#layerslist").jqxListBox('selectIndex', 2);

    $("#layerslist").on('change', function() {
        var items = $("#layerslist").jqxListBox('getSelectedItems');
        floorImageContainer.alpha = 0.2;
        floorRoomsContainer.alpha = 0.4;
        floorDevicesContainer.alpha = 0.2;
        for (var i = 0; i < items.length; i++) {
            if (items[i].label === "Floor background") {
                floorImageContainer.alpha = 1.0;
            }
            if (items[i].label === "Rooms") {
                floorRoomsContainer.alpha = 1.0;
            }
            if (items[i].label === "Devices/Items") {
                floorDevicesContainer.alpha = 1.0;
            }
        }
        updateStage = true;
    });

    ///// Initial settings
    var curFloorSaved = false;
    var containerOffset = {x: 0, y: 0};

    /*var shape = new createjs.Shape();
     stage.addChild(shape);
     var color = "#0FF";
     var size = 2;*/

    floor.on("mousedown", function(event) {
        if (moveFloor === true && event.nativeEvent.button === 0){
            containerOffset = {x: event.currentTarget.x - event.stageX, y: event.currentTarget.y - event.stageY};
            document.body.style.cursor = 'move';
            updateStage = true;
        }
    });

    floor.on("pressmove", function(event) {
        if (moveFloor === true && event.nativeEvent.button === 0) {
            document.body.style.cursor = 'move';
            event.currentTarget.set({
                x: (event.stageX + containerOffset.x),
                y: (event.stageY + containerOffset.y)
            });
            updateStage = true;
        }
    });
    floor.on("pressup", function(event) {
        if (moveFloor === true && event.nativeEvent.button === 0)
            document.body.style.cursor = 'default';
    });

    floorStage.on("stagemousedown", function(event) {

        var downListener = null;
        var upListener   = null;

        if (moveFloor === true && event.nativeEvent.button === 0)
            containerOffset = {x: floor.x - event.stageX, y: floor.y - event.stageY};

        if (event.nativeEvent.button === 0 && drawDevice === true) {
            var curPosTranslated = floor.globalToLocal(event.stageX, event.stageY);
            if(floorDevicesContainer.getObjectsUnderPoint(curPosTranslated.x, curPosTranslated.y).length===0){
                setDeviceImage(0, "Set device", curPosTranslated.x, curPosTranslated.y);
            }
            
        } else if (event.nativeEvent.button === 0 && drawRoom === true) {
            document.body.style.cursor = "crosshair";
            
            var outline = new createjs.Shape();
            outline.snapToPixelEnabled = true;
            floorRoomsContainer.addChild(outline);

            var text = new createjs.Text("Set Location", "14px Arial", "#000000");
            text.textBaseline = "top";
            text.snapToPixelEnabled = true;
            text.visible = false;
            floorRoomsContainer.addChild(text);

            var curPosObject = floor.globalToLocal(event.stageX, event.stageY);

            var hit = new createjs.Shape();

            var posX = curPosObject.x;
            var posY = curPosObject.y;

            text.x = posX; - (text.getMeasuredWidth() / 2);
            text.y = posY; - (text.getMeasuredHeight() / 2);

            outline.x = posX + 0.5;
            outline.y = posY + 0.5;
            outline.graphics.clear();

            downListener = floorStage.on("stagemousemove", function(event) {
                if(text.visible===false){
                    hit.graphics.beginFill("#000").drawRect(0, 0, text.getMeasuredWidth(), text.getMeasuredHeight());
                    text.hitArea = hit;
                    text.visible = true;
                }
                var curPosObject = floor.globalToLocal(event.stageX, event.stageY);
                document.body.style.cursor = 'crosshair';
                var width = curPosObject.x - outline.x + 0.5;
                var height = curPosObject.y - outline.y + 0.5;

                outline.graphics.clear().beginFill("rgba(255,0,0,0.2)").beginStroke("red").drawRect(0, 0, width, height);
                outline.setBounds(0, 0, width, height);
                text.x = posX + ((width / 2) - (text.getMeasuredWidth() / 2));
                text.y = posY + ((height / 2) - (text.getMeasuredHeight() / 2));
                drawingRoom = true;
                updateStage = true;
            });
            upListener = floorStage.on("stagemouseup", function(event) {
                if(drawRoom && event.nativeEvent.button === 0){
                    try {
                        ///// we are done drawing, check bounds.
                        if (drawingRoom && (outline.getBounds().width < 20 && outline.getBounds().width > -20) && (outline.getBounds().height < 20 && outline.getBounds().height > -20)) {
                            throw new Exception("Too small");
                        }
                        drawingRoom = false;
                        setRoomHandlers(0, text, hit, outline);
                    } catch (Err) {
                        /// not correctly drawed, we need dimensions in the room container.
                        console.log(Err);
                        floorRoomsContainer.removeChild(outline);
                        floorRoomsContainer.removeChild(text);
                        updateStage = true;
                    }
                }
                floorStage.off("stagemousemove", downListener);
                floorStage.off("stagemouseup", upListener);
                document.body.style.cursor = "default";
                updateStage = true;
            });
        }
    });

    function setRoomHandlers(presetRoomId, text, textHitter, outline){
        var roomId = presetRoomId;
        text.on("mouseover", function(e) {
            if (moveRoom === true) {
                document.body.style.cursor = 'move';
                updateStage = true;
            } else if (deleteRoom === true) {
                document.body.style.cursor = "url('http://" + location.host + "/shared/images/flooreditor/delete_red.png'),not-allowed";
                updateStage = true;
            } else if (drawRoom === true) {
                document.body.style.cursor = 'pointer';
                updateStage = true;
            }
        });
        text.on("mouseout", function(e) {
            document.body.style.cursor = "default";
            updateStage = true;
        });
        text.on("mousedown", function(event) {
            if (deleteRoom === true && event.nativeEvent.button === 0) {
                floorRoomsContainer.removeChild(outline);
                floorRoomsContainer.removeChild(text);
                updateStage = true;
                for(var i = rooms.length; i--;) {
                    if(rooms[i] === roomId) {
                        rooms.splice(i, 1);
                    }
                }
                if(roomId!==0){
                    var removeRoom = "{\"jsonrpc\": \"2.0\", \"method\": \"LocationService.removeRoomVisual\", \"params\": {\"id\":" + roomId + "} \"id\": \"LocationService.removeRoomVisual\"}";
                    $.get("/jsonrpc.json?rpc=" + removeRoom)
                            .done(function(data) {
                                try {
                                    if (data.result.data !== true) {
                                        showErrorMessage("Room delete error", "Could not delete room boundaries: " + data.result.message);
                                    }
                                } catch (err) {
                                    showErrorMessage("Room delete error", "Could not delete room boundaries: " + err);
                                }
                            }, "json");
                }
            } else if ((drawRoom || moveFloor) && event.nativeEvent.button === 0) {
                var floorId = $("#floorslist").jqxListBox('getSelectedItem').value;
                var allLocationsSource = {
                    datatype: "json",
                    datafields: [
                        {name: 'id', type: 'int'},
                        {name: 'name', type: 'string'}
                    ],
                    url: '/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "LocationService.getLocationsByFloor", "params": {"id":' + floorId + '} , "id":"LocationService.getLocationsByFloor"}',
                    root: "result>data"
                };
                var allLocationsData = new $.jqx.dataAdapter(allLocationsSource);
                $('#selectroompopup').jqxWindow('open');
                $("#selectlocationslist").jqxComboBox({
                    placeHolder: "Select name", source: allLocationsData, displayMember: "name", valueMember: "id", width: 200, height: 25, theme: siteSettings.getTheme()
                });
                var selectionHandler = function(event) {
                    if (event.args) {
                        var item = event.args.item;
                        if (item) {
                            //// Check if the room does not already exists in the display graph, if not save it as a new room.
                            if (rooms.indexOf(item.value) === -1 && item.value !== roomId) {
                                var oldWidth = text.getMeasuredWidth();
                                text.text = item.label;
                                text.x = text.x - ((text.getMeasuredWidth() - oldWidth) / 2);
                                textHitter.graphics.beginFill("#000").drawRect(0, 0, text.getMeasuredWidth(), text.getMeasuredHeight());
                                rooms.push(item.value);
                                updateStage = true;
                                var saveRoom = "{\"jsonrpc\": \"2.0\", \"method\": \"LocationService.setRoomVisual\", \"params\": {\"id\":" + item.value + ",\"x\":" + Math.round(outline.x) + ",\"y\":" + Math.round(outline.y) + ",\"w\":" + Math.round(outline.getBounds().width) + ",\"h\":" + Math.round(outline.getBounds().height) + ",\"old\":"+roomId+"} \"id\": \"LocationService.setRoomVisual\"}";
                                $.get("/jsonrpc.json?rpc=" + saveRoom)
                                        .done(function(data) {
                                            try {
                                                if (data.result.data !== true) {
                                                    showErrorMessage("Room save error", "Could not save room boundaries: " + data.result.message);
                                                }
                                            } catch (err) {
                                                showErrorMessage("Room save error", "Could not save room boundaries: " + err);
                                            }
                                        }, "json");
                                roomId = item.value;
                            } else {
                                showErrorMessage("Change error", "Room " + item.label + " already present.");
                            }
                        }
                        $('#selectroompopup').jqxWindow('close');
                        $("#selectlocationslist").off('select', selectionHandler);
                    }
                };
                $("#selectlocationslist").on('select', selectionHandler);
            }
        });
        text.on("pressmove", function(event) {
            if (moveRoom === true && event.nativeEvent.button === 0) {
                var curPosObject = floor.globalToLocal(event.stageX, event.stageY);
                text.x = curPosObject.x - (text.getMeasuredWidth() / 2);
                text.y = curPosObject.y - (text.getMeasuredHeight() / 2);
                outline.x = curPosObject.x - (outline.getBounds().width / 2);
                outline.y = curPosObject.y - (outline.getBounds().height / 2);
                updateStage = true;
            }
        });
        
        text.on("pressup", function(event){
            if (moveRoom === true && event.nativeEvent.button === 0) {
                if(roomId!==0){
                    var updateRoom = "{\"jsonrpc\": \"2.0\", \"method\": \"LocationService.updateRoomVisual\", \"params\": {\"id\":" + roomId + ",\"x\":" + Math.round(outline.x) + ",\"y\":" + Math.round(outline.y) + ",\"w\":" + Math.round(outline.getBounds().width) + ",\"h\":" + Math.round(outline.getBounds().height) + "} \"id\": \"LocationService.updateRoomVisual\"}";
                    $.get("/jsonrpc.json?rpc=" + updateRoom)
                            .done(function(data) {
                                try {
                                    if (data.result.data !== true) {
                                        showErrorMessage("Room update error", "Could not update room boundaries: " + data.result.message);
                                    }
                                } catch (err) {
                                    showErrorMessage("Room update error", "Could not update room boundaries: " + err);
                                }
                            }, "json");
                } 
            }
        });
        
        outline.on("mouseover", function(e) {
            if (resizeRoom === true) {
                document.body.style.cursor = "url('http://" + location.host + "/shared/images/flooreditor/resize.png'), se-resize";
                updateStage = true;
            }
        });
        outline.on("mouseout", function(e) {
            document.body.style.cursor = "default";
            updateStage = true;
        });
        outline.on("mousedown", function(event) {
            if (resizeRoom === true && event.nativeEvent.button === 0) {
                resizeTextOffset = {x: text.x, y: text.y};
            }
        });
        outline.on("pressmove", function(event) {
            if (resizeRoom === true && event.nativeEvent.button === 0) {
                var curPosObject = floor.globalToLocal(event.stageX, event.stageY);

                var posX = curPosObject.x - text.getMeasuredWidth();
                var posY = curPosObject.y - text.getMeasuredHeight();

                var width = curPosObject.x - outline.x + 0.5;
                var height = curPosObject.y - outline.y + 0.5;

                outline.graphics.clear().beginFill("rgba(255,0,0,0.2)").beginStroke("red").drawRect(0, 0, width, height);
                outline.setBounds(0, 0, width, height);
                text.x = posX - (width / 2 - text.getMeasuredWidth() / 2);
                text.y = posY - (height / 2 - text.getMeasuredHeight() / 2);
                updateStage = true;
            }
        });

        outline.on("pressup", function(event){
            if (resizeRoom === true && event.nativeEvent.button === 0) {
                if(roomId!==0){
                    var updateRoom = "{\"jsonrpc\": \"2.0\", \"method\": \"LocationService.updateRoomVisual\", \"params\": {\"id\":" + roomId + ",\"x\":" + Math.round(outline.x) + ",\"y\":" + Math.round(outline.y) + ",\"w\":" + Math.round(outline.getBounds().width) + ",\"h\":" + Math.round(outline.getBounds().height) + "} \"id\": \"LocationService.updateRoomVisual\"}";
                    $.get("/jsonrpc.json?rpc=" + updateRoom)
                            .done(function(data) {
                                try {
                                    if (data.result.data !== true) {
                                        showErrorMessage("Room update error", "Could not update room boundaries: " + data.result.message);
                                    }
                                } catch (err) {
                                    showErrorMessage("Room update error", "Could not update room boundaries: " + err);
                                }
                            }, "json");
                } 
            }
        });

    }

    function setDeviceImage(id, name, posX, posY){
        var deviceId = id;
        var deviceName = name;
        var image = new Image();
        image.onload = function(event) {
            // Create a Bitmap from the loaded image
            var img = new createjs.Bitmap(event.target);
            var imgHeight = img.image.height;
            img.x = posX - 13;
            img.y = posY - imgHeight;
            
            floorDevicesContainer.addChild(img);
            updateStage = true;

            var text = new createjs.Text((deviceId===0)?"Set device":deviceName, "14px Arial", "#000000");
            text.textBaseline = "top";
            text.snapToPixelEnabled = true;
            
            img.on("mousedown", function(event) {
                if((drawDevice || moveFloor)  && event.nativeEvent.button === 0 ){
                    var floorId = $("#floorslist").jqxListBox('getSelectedItem').value;
                    var allDevicesSource = {
                        datatype: "json",
                        datafields: [
                            {name: 'id', type: 'int'},
                            {name: 'name', type: 'string'}
                        ],
                        url: '/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "DeviceService.getVisualDevices", "params": {"floorid":' + floorId + '} , "id":"DeviceService.getVisualDevices"}',
                        root: "result>data"
                    };
                    var allDevicesData = new $.jqx.dataAdapter(allDevicesSource);
                    $('#selectdevicepopup').jqxWindow('open');
                    $("#selectdevicelist").jqxComboBox({
                        placeHolder: "Select name", source: allDevicesData, displayMember: "name", valueMember: "id", width: 200, height: 25, theme: siteSettings.getTheme()
                    });
                    var selectionHandler = function(event) {
                        if (event.args) {
                            var item = event.args.item;
                            if (item) {
                                //// Check if the room does not already exists in the display graph, if not save it as a new room.
                                if (devices.indexOf(item.value) === -1 && item.value !== deviceId) {
                                    var setDevice = "{\"jsonrpc\": \"2.0\", \"method\": \"DeviceService.setVisualDevice\", \"params\": {\"id\":" + item.value + ",\"x\":" + Math.round(posX) + ",\"y\":" + Math.round(posY) + ",\"old\":" + deviceId + "} \"id\": \"DeviceService.setVisualDevice\"}";
                                    $.get("/jsonrpc.json?rpc=" + setDevice)
                                            .done(function(data) {
                                                try {
                                                    if (data.result.data !== true) {
                                                        showErrorMessage("Device save error", "Could not save device location: " + data.result.message);
                                                    }
                                                } catch (err) {
                                                    showErrorMessage("Device save error", "Could not save device location: " + err);
                                                }
                                            }, "json");
                                    deviceId = item.value;
                                    text.text = item.label;
                                    devices.push(item.value);
                                    updateStage = true;
                                } else {
                                    showErrorMessage("Change error", "Device " + item.label + " already present.");
                                }
                            }
                            $('#selectdevicepopup').jqxWindow('close');
                            $("#selectdevicelist").off('select', selectionHandler);
                        }
                    };
                    $("#selectdevicelist").on('select', selectionHandler);
                } else if (moveDevice === true && event.nativeEvent.button === 0) {
                    moveDeviceOffset = {x: img.x, y: img.y};
                } else if (deleteDevice === true && event.nativeEvent.button === 0){
                    floorDevicesContainer.removeChild(img);
                    updateStage = true;
                    if((deviceId!==0)){
                        var setDevice = "{\"jsonrpc\": \"2.0\", \"method\": \"DeviceService.removeVisualDevice\", \"params\": {\"id\":" + deviceId + "} \"id\": \"DeviceService.removeVisualDevice\"}";
                        $.get("/jsonrpc.json?rpc=" + setDevice)
                                .done(function(data) {
                                    try {
                                        if (data.result.data !== true) {
                                            showErrorMessage("Device remove error", "Could not remove device: " + data.result.message);
                                        }
                                    } catch (err) {
                                        showErrorMessage("Device remove error", "Could not remove device: " + err);
                                    }
                                }, "json");
                    }
                }
            });
            
            img.on("mouseover", function(e) {
                if (moveDevice) {
                    text.x = (img.x + img.image.width/2) - (text.getMeasuredWidth() / 2);
                    text.y = img.y - text.getMeasuredHeight();
                    floorDevicesContainer.addChild(text);
                    document.body.style.cursor = "move";
                } else if (deleteDevice === true){
                    document.body.style.cursor = "url('http://" + location.host + "/shared/images/flooreditor/delete_red.png'),not-allowed";
                } else if (drawDevice === true || moveFloor){
                    text.x = (img.x + img.image.width/2) - (text.getMeasuredWidth() / 2);
                    text.y = img.y - text.getMeasuredHeight();
                    floorDevicesContainer.addChild(text);
                }
                updateStage = true;
            });
            img.on("mouseout", function(e) {
                if (drawDevice || moveDevice || moveFloor){
                    floorDevicesContainer.removeChild(text);
                }
                document.body.style.cursor = "default";
                updateStage = true;
            });
            
            img.on("pressmove", function(event) {
                if (moveDevice === true && event.nativeEvent.button === 0) {
                    var curPosTranslated = floor.globalToLocal(event.stageX, event.stageY);
                    event.target.x = curPosTranslated.x - 13;
                    event.target.y = curPosTranslated.y - imgHeight;
                    updateStage = true;
                }
            });
            
            img.on("pressup", function(event){
                if (moveDevice === true && event.nativeEvent.button === 0) {
                    if((deviceId!==0)){
                        var curPosTranslated = floor.globalToLocal(event.stageX, event.stageY);
                        var setDevice = "{\"jsonrpc\": \"2.0\", \"method\": \"DeviceService.updateVisualDevice\", \"params\": {\"id\":" + deviceId + ",\"x\":" + Math.round(curPosTranslated.x) + ",\"y\":" + Math.round(curPosTranslated.y) + "} \"id\": \"DeviceService.updateVisualDevice\"}";
                        $.get("/jsonrpc.json?rpc=" + setDevice)
                                .done(function(data) {
                                    try {
                                        if (data.result.data !== true) {
                                            showErrorMessage("Device update error", "Could not update device location: " + data.result.message);
                                        }
                                    } catch (err) {
                                        showErrorMessage("Device update error", "Could not update device location: " + err);
                                    }
                                }, "json");
                    }
                }
            });
            
        };
        image.src = "/shared/images/flooreditor/device_pointer.png";
        return image;
    }

    function setFloorImage(imageSrc, isNew){
        var image = new Image();
        image.onload = function(event) {
            // Create a Bitmap from the loaded image
            var img = new createjs.Bitmap(event.target);
            if (floorImageContainer.getNumChildren() !== 0) {
                floorImageContainer.removeAllChildren();
            }
            floorImageContainer.addChild(img);
            floor.regX = -(floorCanvas.width / 2 - img.image.width / 2);
            floor.regY = -(floorCanvas.height / 2 - img.image.height / 2);
            updateStage = true;
            if(isNew){
                var floorId = $("#floorslist").jqxListBox('getSelectedItem').value;
                var postField = {};
                postField["rpc"] = "{\"jsonrpc\": \"2.0\", \"method\": \"LocationService.setFloorVisual\", \"params\": {\"id\":"+floorId+", \"image\":\""+imageSrc+"\"} \"id\": \"LocationService.setFloorVisual\"}";
                $.post("/jsonrpc.json",postField)
                    .done(function(data) {
                        try {
                            if(data.result.data !== true){
                                showErrorMessage("Room image error", "Could not save image: " + data.result.message);
                            }
                        } catch(err){
                            showErrorMessage("Room save error", "Could not save image: " + err);
                        }
                    }, "json");
            }
        };
        // Load the image
        image.src = imageSrc;
    }

    var uploader = document.getElementById('uploader');
    upclick({
        element: uploader,
        action: '/flooruploader.upload',
        onstart: function(filename) {
            ////alert('Start upload: '+filename);
        },
        oncomplete: function(response_data) {
            // Check upload Status
            var jsonString = response_data.replace('<pre style="word-wrap: break-word; white-space: pre-wrap;">', '').replace('</pre>', '').trim();
            var imageObj = jQuery.parseJSON(jsonString);
            if (imageObj.success === true) {
                // Load the image
                setFloorImage(imageObj.filename, true);
                allFloorsData.dataBind();
            } else {
                showErrorMessage("Upload problem", imageObj.message);
            }
        }
    });

    /*
     circle.on("pressmove", function(evt) {
         evt.target.x = evt.stageX;
         evt.target.y = evt.stageY;
     });
     */
});