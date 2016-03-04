
function DashBoardBuilder(gridster, parentDiv){
    this.functionDataCollection = new Array();
    this.devicesDashboardCache = [];
    this.deviceSelectCache     = {};
    this.scenesDashboardCache = [];
    this.macrosDashboardCache = [];
    this.serverTimeDashboardCache = [];
    this.weatherPluginsDashboardCache = {};
    this.mainParentDiv = parentDiv;
    this.dashInEdit = false;
    this.gridster = gridster;
    this.buildMenu();
    this.itemSelectionModal = new ItemSelectionModal();
    this.itemSelectionModal.setOptions({"deviceTypeFilter":["devices", "currenttime", "weatherplugin", "macros", "scenes"]});
    this.itemSelectionModal.setSelectionType("all");
    var self = this;
    this.itemSelectionModal.setCallBack(function(returnData){
        self.createWidgetItems(returnData);
    });
}

DashBoardBuilder.prototype.buildMenu = function(){
    ///$("#dashboardaddbutton")
};

DashBoardBuilder.prototype.getMultiplyedSize = function(jqel, baseSize, useAbsGap){
    var sizemultiply = parseInt(jqel.parent().attr("data-sizey"));
    /// We have a 10 pixel margin between each block size, size 1 is no gap, size 2 is a 10 pixel gap needed in consideration to left and height.
    var defaultGrow = (sizemultiply * baseSize)
    if(sizemultiply > 1){
        if(useAbsGap){
            return defaultGrow + (sizemultiply * 10);
        } else {
            return defaultGrow + (defaultGrow*((1/75)*10));
        }
    } else {
        return defaultGrow;
    }
};

DashBoardBuilder.prototype.build = function () {
    var self = this;
    /// headers
    if(self.mainParentDiv.find("[data-type='spacer']").length>0){
        self.mainParentDiv.find("[data-type='spacer']").each(function () {
            if(typeof $(this).attr("data-content") !== "undefined"){
                $(this).append('<div style="text-align:left; display: table-cell; vertical-align:bottom; padding-bottom: 5px; '+
                                           'width:'+$(this).parent().width()+'px; ' + 
                                           'height:'+$(this).parent().height()+'px;">' + 
                                    '<h3 style="font-size: 2.4em; border-bottom: 1px solid #fff; padding-bottom: 10px;">'+
                                        $(this).attr("data-content") + 
                                    '</h3>'+
                               '</div>');
            } else {
               $(this).append('<div style="text-align:center; display: table-cell; vertical-align:middle;"></div>');
            }
        });
    }
    /// weather
    if(self.mainParentDiv.find("[data-type='weather']").length>0){
        try {
            getHttpJsonRPC('{"jsonrpc": "2.0", "method": "WeatherService.getPlugins","id":"WeatherService.getPlugins"}', function (data) {
                if(data.length > 0){
                    getHttpJsonRPC('{"jsonrpc": "2.0", "method": "WeatherService.getCurrentWeather", "params": {"id":'+data[0].id+'} "id":"WeatherService.getCurrentWeather"}', function (data2) {
                        self.weatherPluginsDashboardCache = data2;
                        self.mainParentDiv.find("[data-type='weather']").each(function () {
                            self.dashBoardWeatherBuilder($(this), false);
                        });
                    });
                    pidomeRPCSocket.addDefaultCallback(function(thingy) {
                        $("#weathercityname").text(thingy.params.location);
                        try {
                            $("#weathertemp").text(thingy.params.temperature.toFixed(2));
                        } catch (err) {
                            $("#weathertemp").text("Unknown");
                        }
                        $("#weatherdescription").text(thingy.params.text);    
                        $("#weatherhumidity").text(thingy.params.humidity);
                        $("#weatherpressure").text(thingy.params.pressure);
                        $("#weatherwind").text(thingy.params.winddirection + ", " + thingy.params.windspeed.toFixed(2));
                        try {
                            $("#weatherwithimagecontainer").css('background-image','url(/shared/images/components/weather/dashboard/'+self.weatherPluginsDashboardCache.iconimage.replace("png", "svg")+')');
                        }  catch (err){}

                    }, "WeatherService.getCurrentWeather");
                }
            });
        } catch (err) {}
    }
    /// time
    if(self.mainParentDiv.find("[data-type='time']").length>0){
        getHttpJsonRPC('{"jsonrpc": "2.0", "method": "SystemService.getCurrentTime","id":"SystemService.getCurrentTime"}', function (data) {
            self.serverTimeDashboardCache = data;
            self.mainParentDiv.find("[data-type='time']").each(function () {
                self.dashBoardTimeBuilder($(this), false);
            });
        });
        pidomeRPCSocket.addDefaultCallback(function(thingy) {
            $("#dash-servertime").html(thingy.params.time);
            $("#dash-serverdate").html(thingy.params.shorttext);
            $("#dash-sunrise").html(thingy.params.sunrise);
            $("#dash-sunset").html(thingy.params.sunset);
        }, "SystemService.time");
    }
    //// scenes
    if(self.mainParentDiv.find("[data-type='scene']").length>0){
        getHttpJsonRPC('{"jsonrpc": "2.0", "method": "ScenesService.getScenes","id":"ScenesService.getScenes"}', function (data) {
            self.scenesDashboardCache = data;
            self.mainParentDiv.find("[data-type='scene']").each(function () {
                self.dashBoardSceneBuilder($(this), parseInt($(this).attr("data-id")));
            });
            pidomeRPCSocket.addCallback(function(thingy) {
                try {
                    if(!$('#runScene_'+thingy.params.id).hasClass("on")){
                        $('#runScene_'+thingy.params.id).addClass("on");
                    }
                    $('#runScene_'+thingy.params.id).removeClass("off");
                } catch (err) {}
            }, "ScenesService.activateScene");
            pidomeRPCSocket.addCallback(function(thingy) {
                try {
                    if(!$('#runScene_'+thingy.params.id).hasClass("off")){
                        $('#runScene_'+thingy.params.id).addClass("off");
                    }
                    $('#runScene_'+thingy.params.id).removeClass("on");
                } catch (err) {}
            }, "ScenesService.deActivateScene");
        });
    }
    /// macros
    if(self.mainParentDiv.find("[data-type='macro']").length>0){
        getHttpJsonRPC('{"jsonrpc": "2.0", "method": "MacroService.getMacros","id":"MacroService.getMacros"}', function (data) {
            self.macrosDashboardCache = data;
            self.mainParentDiv.find("[data-type='macro']").each(function () {
                self.macroBuilder($(this), parseInt($(this).attr("data-id")));
            });
            pidomeRPCSocket.addCallback(function(thingy) {
                try {
                    if(!$('#runMacro_'+thingy.params.id).hasClass("on")){
                        $('#runMacro_'+thingy.params.id).addClass("on");
                        $('#runMacro_'+thingy.params.id).removeClass("off");
                        setTimeout( function(){
                            $('#runMacro_'+thingy.params.id).addClass("off");
                            $('#runMacro_'+thingy.params.id).removeClass("on");
                        }, 1000 );
                    }
                } catch (err) {}
            }, "MacroService.runMacro");
        });
    }
    /// devices
    if(self.mainParentDiv.find("[data-type='device']").length>0){
        getHttpJsonRPC('{"jsonrpc": "2.0", "method": "DeviceService.getActiveDevices","id":"DeviceService.getActiveDevices"}', function (data) {
            self.devicesDashboardCache = data;
            self.mainParentDiv.find("[data-type='device']").each(function () {
                self.dashBoardDeviceBuilder($(this), parseInt($(this).attr("data-id")), $(this).attr("data-group"), $(this).attr("data-control"), $(this).attr("data-visual"));
            });
        });
    }
};

DashBoardBuilder.prototype.dashBoardWeatherBuilder = function (jqel, clean, callback){
    var self = this;
    if (self.weatherPluginsDashboardCache.length === 0 || clean === true) {
        getHttpJsonRPC('{"jsonrpc": "2.0", "method": "WeatherService.getPlugins","id":"WeatherService.getPlugins"}', function (data) {
            getHttpJsonRPC('{"jsonrpc": "2.0", "method": "WeatherService.getCurrentWeather", "params": {"id":'+data[0].id+'} "id":"WeatherService.getCurrentWeather"}', function (data2) {
                self.weatherPluginsDashboardCache = data2;
                self.weatherBuilder(jqel);
                self.runCallBack(callback);
            });
        });
    } else {
        self.weatherBuilder(jqel);
        self.runCallBack(callback);
    }
};

DashBoardBuilder.prototype.weatherBuilder = function (jqel){
    var self = this;
    jqel.empty();
    jqel.html('<div class="dashboardboxed dashweathercontainer textstroke" id="weatherwithimagecontainer"'+
                   'style="width:'+jqel.parent().width()+'px; '+
                          'height:'+jqel.parent().height()+'px; '+
                          'background-size: '+jqel.parent().width()+'px '+jqel.parent().height()+'px; ' +
                          'background-image:url(/shared/images/components/weather/dashboard/'+self.weatherPluginsDashboardCache.iconimage.replace("png", "svg")+');">' +
                '<div style="position: absolute; top:3px; text-align:center;width:'+jqel.parent().width()+'px;">' +
                    '<div id="weathercityname">' + self.weatherPluginsDashboardCache.location + '</div>' +
                    '<div style="font-size:'+this.getMultiplyedSize(jqel, 10)+'px;" id="weatherdescription">' + self.weatherPluginsDashboardCache.text + '</div>' +
                '</div>'+
                '<div style="font-size:'+this.getMultiplyedSize(jqel, 18, true)+'px; line-height: '+this.getMultiplyedSize(jqel, 18, true)+'px; margin-top: -15px;"><span id="weathertemp">' + self.weatherPluginsDashboardCache.temperature.toFixed(2) + '</span><span style="line-height: 45px;font-size: 40px;vertical-align: top;">°C</span></div>' +
                '<div style="display:table; text-align: left; position: absolute; bottom: 3px; left: 3px;">'+ 
                   '<div style="display:table-row;">'+
                      '<div style="display:table-cell;">Humidity</div><div style="display:table-cell;">: <span id="weatherhumidity">' + self.weatherPluginsDashboardCache.humidity + '</span>%</div> '+
                   '</div>' +
                   '<div style="display:table-row;">'+
                      '<div style="display:table-cell;">Pressure</div><div style="display:table-cell;">: <span id="weatherpressure">' + self.weatherPluginsDashboardCache.pressure + '</span></div>'+
                   '</div>' +
                   '<div style="display:table-row;">'+
                      '<div style="display:table-cell;">Wind</div><div style="display:table-cell;">: <span id="weatherwind">' + self.weatherPluginsDashboardCache.winddirection + ', ' + self.weatherPluginsDashboardCache.windspeed.toFixed(2) + '</span></div> '+
                   '</div>' +
                '</div>' +
               '</div>');
};

DashBoardBuilder.prototype.runCallBack = function (callback){
    if(typeof callback !== "undefined") callback();
};

DashBoardBuilder.prototype.dashBoardTimeBuilder = function (jqel, clean, callback){
    var self = this;
    if (self.serverTimeDashboardCache.length === 0 || clean === true) {
        getHttpJsonRPC('{"jsonrpc": "2.0", "method": "SystemService.getCurrentTime","id":"SystemService.getCurrentTime"}', function (data) {
            self.serverTimeDashboardCache = data;
            self.timeBuilder(jqel);
            self.runCallBack(callback);
        });
    } else {
        self.timeBuilder(jqel);
        self.runCallBack(callback);
    }
};

DashBoardBuilder.prototype.timeBuilder = function (jqel){
    var self = this;
    jqel.empty();
    jqel.html('<div class="dashboardboxed dashtimecontainer textstroke" '+
                   'style="width:'+jqel.parent().width()+'px; '+
                          'height:'+jqel.parent().height()+'px; '+
                          'background-image:url(/shared/images/components/time/dashboard/time-bg.svg);">' +
                  '<div><span style="margin-top: 3px;" id="dash-serverdate">' + self.serverTimeDashboardCache.shorttext + '</span></div>' +
                  '<div class="timebox" id="dash-servertime" style="margin-top:'+this.getMultiplyedSize(jqel, 18, true)+'px; font-size:'+this.getMultiplyedSize(jqel, 22, true)+'px;">' + self.serverTimeDashboardCache.time + '</div>' +
                  '<div style="display:table;">'+ 
                     '<div style="display:table-row">'+
                        '<div style="position: absolute;bottom: 3px; left:3px; display:table-cell;height:24px;"><img src="/shared/images/components/time/dashboard/sunrise.svg" width="24" height="24" alt="Sunrise"/><span style="padding-left:3px;height: 24px;vertical-align:middle;" id="dash-sunrise">'+self.serverTimeDashboardCache.sunrise+'</span></div> '+
                        '<div style="position: absolute;bottom: 3px; right:3px; display:table-cell;height:24px;"><span style="padding-right:3px;height: 24px;vertical-align:middle;" id="dash-sunset">'+self.serverTimeDashboardCache.sunset+'</span><img src="/shared/images/components/time/dashboard/sunset.svg" width="24" height="24" alt="Sunset"/></div>'+
                     '</div>' +
                  '</div>' +
               '</div>');
};

DashBoardBuilder.prototype.dashBoardMacroBuilder = function (jqel, macroId, callback){
    var self = this;
    if (self.macrosDashboardCache.length === 0) {
        getHttpJsonRPC('{"jsonrpc": "2.0", "method": "MacroService.getMacros","id":"MacroService.getMacros"}', function (data) {
            self.macrosDashboardCache = data;
            self.macroBuilder(jqel, macroId);
            self.runCallBack(callback);
        });
    } else {
        self.macroBuilder(jqel, macroId);
        self.runCallBack(callback);
    }
};

DashBoardBuilder.prototype.macroBuilder = function (jqel, macroId) {
    var self = this;
    var textSize = this.getMultiplyedSize(jqel, 16);
    jqel.empty();
    for (var i = 0; i < self.macrosDashboardCache.length; i++) {
        if (self.macrosDashboardCache[i].id === macroId) {
            var macro = self.macrosDashboardCache[i];
            jqel.html('<div class="switchabledashcontainer macro off" id="runMacro_'+macro.id +'"><div class="dashboardboxed textstroke" '+
                           'style="width:'+jqel.parent().width()+'px;'+
                                  'height:'+jqel.parent().height()+'px;'+
                                  'background-image:url(/shared/images/components/macros/dashboard/macro-bg.svg); '+
                                  'font-size: '+textSize+'px;">' +
                       macro.name +
                       '</div></div>');
            $('#runMacro_'+macro.id).click(function(){
                if(!self.dashInEdit)getHttpJsonRPC("{\"jsonrpc\": \"2.0\", \"method\": \"MacroService.runMacro\", \"params\":{\"id\":"+macro.id+"},\"id\":\"MacroService.runMacro\"}", null);
            });
        }
    }
};

DashBoardBuilder.prototype.dashBoardSceneBuilder = function(jqel, sceneId, callback){
    var self = this;
    if (self.scenesDashboardCache.length === 0) {
        getHttpJsonRPC('{"jsonrpc": "2.0", "method": "ScenesService.getScenes","id":"ScenesService.getScenes"}', function (data) {
            self.scenesDashboardCache = data;
            self.sceneBuilder(jqel, sceneId);
            self.runCallBack(callback);
        });
    } else {
        self.sceneBuilder(jqel, sceneId);
        self.runCallBack(callback);
    }
};

DashBoardBuilder.prototype.sceneBuilder = function (jqel, sceneId) {
    var self = this;
    var textSize = this.getMultiplyedSize(jqel, 16);
    jqel.empty();
    for (var i = 0; i < self.scenesDashboardCache.length; i++) {
        if (self.scenesDashboardCache[i].id === sceneId) {
            var scene = self.scenesDashboardCache[i];
            /// Lets do something cool and create a variable background based on the scene name:
            var sceneNameBg = Base64.encode('<svg xmlns="http://www.w3.org/2000/svg" style="left:0; top:0; width:100%; height:100%;" viewBox="0 0 '+jqel.parent().width()+' '+jqel.parent().height()+'" preserveAspectRatio="xMidYMid meet"><text fill="#fff" x="50%" y="50%" font-size="75" style="text-anchor: middle;alignment-baseline: middle;" fill-opacity="0.2">'+scene.name+'</text></svg>');
            jqel.html('<div class="switchabledashcontainer '+((scene.active===true)?'on':'off')+'" id="runScene_'+scene.id +'"><div class="dashboardboxed textstroke" '+
                           'style="width:'+jqel.parent().width()+'px;'+
                                  'height:'+jqel.parent().height()+'px;'+
                                  'background-image:url(\'data:image/svg+xml;base64,' + sceneNameBg + '\');'+
                                  'font-size: '+textSize+'px;">' +
                       scene.name +
                       '</div></div>');
            $('#runScene_'+scene.id).click(function(){
                if(!self.dashInEdit)getHttpJsonRPC("{\"jsonrpc\": \"2.0\", \"method\": \"ScenesService."+(($('#runScene_'+scene.id).hasClass("on"))?'deActivateScene':'activateScene')+"\", \"params\":{\"id\":"+scene.id+"},\"id\":\"ScenesService.ScenesService."+(($('#runScene_'+scene.id).hasClass("on"))?'deActivateScene':'activateScene')+"\"}", null);
            });
        }
    }
};

DashBoardBuilder.prototype.dashBoardDeviceBuilder = function (jqel, deviceId, groupId, controlId, display, callback) {
    var self = this;
    if (self.devicesDashboardCache.length === 0) {
        getHttpJsonRPC('{"jsonrpc": "2.0", "method": "DeviceService.getActiveDevices","id":"DeviceService.getActiveDevices"}', function (data) {
            self.deviceBuilder(jqel, deviceId, groupId, controlId, display);
            self.runCallBack(callback);
        });
    } else {
        self.deviceBuilder(jqel, deviceId, groupId, controlId, display);
        self.runCallBack(callback);
    }
};

DashBoardBuilder.prototype.deviceBuilder = function (jqel, deviceId, groupId, controlId, display) {
    var self = this;
    for (var i = 0; i < self.devicesDashboardCache.length; i++) {
        if (self.devicesDashboardCache[i].id === deviceId) {
            var device = self.devicesDashboardCache[i];
            for (var j = 0; j < device.commandgroups.length; j++) {
                if (device.commandgroups[j].id === groupId) {
                    var commandGroup = device.commandgroups[j];
                    for (var k = 0; k < commandGroup.commands.length; k++) {
                        if (commandGroup.commands[k].typedetails.id === controlId) {
                            var control = commandGroup.commands[k];
                            switch (control.commandtype) {
                                case "data":
                                    switch (display) {
                                        case "gauge":
                                            var doGauge  = false;
                                            var minValue = 0;
                                            var maxValue = 0;
                                            var warnValue = 0;
                                            var highValue = 0;
                                            var headerLabel = "";
                                            if((jqel.attr("data-minvalue") && jqel.attr("data-maxvalue")) && (Number(jqel.attr("data-minvalue"))!=Number(jqel.attr("data-maxvalue")))){
                                                minValue = Number(jqel.attr("data-minvalue"));
                                                maxValue = Number(jqel.attr("data-maxvalue"));
                                                doGauge = true;
                                            } else if((control.typedetails.maxvalue!==control.typedetails.minvalue)){
                                                minValue = control.typedetails.minvalue;
                                                maxValue = control.typedetails.maxvalue;
                                                doGauge = true;
                                            }
                                            if((jqel.attr("data-warnvalue") && jqel.attr("data-highvalue")) && (Number(jqel.attr("data-warnvalue"))!=Number(jqel.attr("data-highvalue")))){
                                                warnValue = Number(jqel.attr("data-warnvalue"));
                                                highValue = Number(jqel.attr("data-highvalue"));
                                            } else if((control.typedetails.warnvalue!==control.typedetails.highvalue)){
                                                warnValue = control.typedetails.warnvalue;
                                                highValue = control.typedetails.highvalue;
                                            }
                                            if(jqel.attr("data-customlabel") && jqel.attr("data-customlabel")!==""){
                                                headerLabel = jqel.attr("data-customlabel");
                                            }
                                            if(doGauge){
                                                this.dashDeviceDataGauge(jqel, deviceId, groupId, controlId, device.name, device.locationname, control.typedetails.label, control.typedetails.prefix, control.typedetails.suffix, control.currentvalue,minValue,maxValue,warnValue,highValue, headerLabel);
                                            } else {
                                                this.dashDeviceDataText(jqel, deviceId, groupId, controlId, device.name, device.locationname, control.typedetails.label, control.typedetails.prefix, control.typedetails.suffix, control.currentvalue,minValue,maxValue,warnValue,highValue,control.typedetails.boolvis,control.typedetails.falsetext,control.typedetails.truetext, control.typedetails.datatype);
                                            }
                                        break;
                                        case "graph":
                                            this.dashDeviceDataGraph(jqel, deviceId, groupId, controlId, device.name, control.typedetails.label, control.typedetails.graphtype, 0);
                                        break;
                                        default:
                                            this.dashDeviceDataText(jqel, deviceId, groupId, controlId, device.name, device.locationname, control.typedetails.label, control.typedetails.prefix, control.typedetails.suffix, control.currentvalue,minValue,maxValue,warnValue,highValue,control.typedetails.boolvis,control.typedetails.falsetext,control.typedetails.truetext, control.typedetails.datatype);
                                        break;
                                    }
                                break;
                                case "toggle":
                                    //this.dashDeviceToggleButton(jqel, deviceId, device.name, groupId, controlId, control.typedetails.label, control.currentvalue, ((jqel.attr("data-showlabel") && jqel.attr("data-showlabel")==="true")?true:false));
                                    this.dashDeviceToggleButton(jqel, deviceId, device.name, groupId, controlId, control.typedetails.label, control.currentvalue, true);
                                break;
                                case "button":
                                    this.dashDevicePushButton(jqel, deviceId, device.name, groupId, controlId, control.typedetails.label, control.currentvalue, ((jqel.attr("data-showlabel") && jqel.attr("data-showlabel")==="true")?true:false), control.typedetails.datatype);
                                break;
                                case "slider":
                                    this.dashDeviceSlider(jqel, deviceId, device.name, groupId, controlId, control.typedetails.label, control.currentvalue, control.typedetails.min, control.typedetails.max, ((jqel.attr("data-showlabel") && jqel.attr("data-showlabel")==="true")?true:false), control.typedetails.datatype);
                                break;
                                case "colorpicker":
                                    this.dashDeviceColorpicker(jqel, deviceId, device.name, groupId, controlId, control.typedetails.label, control.currentvalue, control, ((jqel.attr("data-showlabel") && jqel.attr("data-showlabel")==="true")?true:false), control.typedetails.datatype);
                                break;
                                case "select":
                                    this.dashDeviceSelect(jqel, deviceId, device.name, groupId, controlId, control.typedetails.label, control.currentvalue, control.typedetails.commandset);
                                break;
                            }
                        }
                    }
                }
            }
            break;
        }
    }
};

DashBoardBuilder.prototype.dashDeviceColorpicker = function (jqel, deviceId, deviceName, groupId, controlId, label, currentvalue, control, showlabel, datatype){
    var self = this;
    var parentWidth = jqel.parent().width();
    var parentHeight = jqel.parent().height();
    var fontSize = this.getMultiplyedSize(jqel, 18);
    var randID = createUUID();
    jqel.empty();
    jqel.html('<div id="'+randID+'" class="dashboardboxed textstroke" style="position:relative background-color: '+currentvalue.hex+';width:'+parentWidth+'px;height:'+parentHeight+'px;">'+
                '<svg class="dashDeviceColorPickerFor_' + deviceId + '-' + groupId + '-' + controlId +'" style="z-index: 1;position: absolute; left:4px; top: 2px; width:'+(parentWidth)+'px;height:'+(parentHeight)+'px;" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1" id="e4bc7bd5-9cf7-499b-b1e1-4164467da09b" x="0px" y="0px" viewBox="0 0 110 110" enable-background="new 0 0 110 110" xml:space="preserve">'+
                    '<path fill="#fff" fill-opacity="0.4" id="Palette_1_" d="M84.71,80.953c-0.169-4.002-6.185-15.902-6.043-20.52c0.162-5.236,3.329-11.685,9.923,0.922  c3.951,7.555,10.016,2.854,10.752-0.822c2.223-11.12-1.078-24.458-11.591-37.527C69.567,0.405,37.605-5.802,16.38,9.144  C-4.844,24.092-5.271,57.752,14.097,77.26c17.555,17.676,41.765,25.445,62.007,16.926C78.197,93.301,85.123,90.59,84.71,80.953z   M27.852,15.419c3.83-2.697,9.12-1.78,11.819,2.052c2.699,3.831,1.779,9.122-2.051,11.819c-3.833,2.699-9.122,1.78-11.821-2.051  C23.1,23.408,24.018,18.119,27.852,15.419z M11.314,49.892c-2.697-3.831-1.777-9.122,2.053-11.819  c3.828-2.697,9.122-1.782,11.819,2.049c2.699,3.832,1.777,9.124-2.051,11.821C19.305,54.641,14.012,53.727,11.314,49.892z   M52.489,27.813c-2.701-3.831-1.781-9.124,2.051-11.821c3.831-2.697,9.12-1.779,11.821,2.053c2.697,3.83,1.778,9.12-2.053,11.819  C60.477,32.562,55.187,31.645,52.489,27.813z M23.794,71.543c-2.697-3.834-1.78-9.127,2.05-11.824  c3.831-2.697,9.124-1.779,11.821,2.051c2.697,3.832,1.78,9.125-2.051,11.822C31.784,76.291,26.492,75.371,23.794,71.543z"></path>'+
                '</svg>' +
                '<div style="font-size: '+fontSize+'px; z-index:2; position:relative;">' + ((showlabel===true)?label:deviceName) + "</div>" + 
              '</div>');
    $('.dashDeviceColorPickerFor_' + deviceId + '-' + groupId + '-' + controlId + ' path').css("fill", currentvalue.hex);
    var localColorGetObject = {h: 0, s: 0, b: 0};
    var localSetColorObject = {h: control.typedetails.color.hsb.h * 360, s: control.typedetails.color.hsb.s, v: control.typedetails.color.hsb.b};
    var deviceData = {'id': deviceId, 'friendlyname' : deviceName};
    var popId = createColorPicker(deviceData, groupId + '-' + controlId, control, localColorGetObject, localSetColorObject);
    $('#'+randID).off("click").on("click", function(){
        if(!self.dashInEdit) $('#'+popId).modal("show");
    });
};

DashBoardBuilder.prototype.dashDeviceSelect = function(jqel, deviceId, name, groupId, controlId, label, currentvalue, commandset){
    var parentWidth = jqel.parent().width();
    var parentHeight = jqel.parent().height();
    var uniqueid = createUUID();
    jqel.empty();
    if(currentvalue == null) { currentvalue = "Unknown" ; }
    jqel.html('<div id="'+uniqueid+'" class="dashboardboxed textstroke" style="width:'+parentWidth+'px;height:'+parentHeight+'px; vertical-align:top;">'+
                '<div style="font-size:0.8em">' + name + "</div>" + 
                '<div style="margin-left:auto;margin-right:auto;margin-top:4px;"><div class="deviceCommandSelectValue_' + deviceId + '-' + groupId + '-' + controlId +'" type="text" id="' + uniqueid + '">'+currentvalue+'</div></div>'+ 
              '</div>');
    var self = this;
    $('#'+uniqueid).off("click").on("click", function(){
        var selectSet = '<select class="deviceCommandSelectValue_' + deviceId + '-' + groupId + '-' + controlId +' form-control" data-group="' + groupId + '" data-control="' + controlId + '" data-id="' + deviceId + '">';
        for(var j=0;j<commandset.length;j++){
            var value = commandset[j].value.toString();
            selectSet += '<option value="'+value+'" '+((value===currentvalue.toString())?' selected':'')+'>'+commandset[j].label+'</option>';
        }
        selectSet += '</select>';
        simpleDialog("change select value", selectSet, function(){}, "Close");
        $('.deviceCommandSelectValue_' + deviceId + '-' + groupId + '-' + controlId ).off("change").on("change",function(){
            runDeviceCommand(parseInt($(this).attr("data-id")), $(this).attr("data-group"), $(this).attr("data-control"), $(this).val(),"","string");
        });
    });
}

DashBoardBuilder.prototype.dashDeviceSlider = function (jqel, deviceId, deviceName, groupId, controlId, label, currentvalue, min, max, showlabel, datatype){
    var parentWidth = jqel.parent().width();
    var parentHeight = jqel.parent().height();
    var uniqueid = createUUID();
    jqel.empty();
    jqel.html('<div class="dashboardboxed textstroke" style="width:'+parentWidth+'px;height:'+parentHeight+'px; background-image:url(/shared/images/devices/controls/dashboard/slider-bg.svg);">'+
                '<div style="margin-left:auto;margin-right:auto;margin-top:0px;width:'+parentWidth*0.7+'px;height:'+parentHeight*0.7+'px;"><input class="deviceSliderFor_' + deviceId + '-' + groupId + '-' + controlId +'" type="text" id="' + uniqueid + '"></input></div>'+ 
                '<div style="margin-top: -20px;">' + label + "<br/><span style=\"font-size:0.8em;\">"+deviceName+"</span></div>" + 
              '</div>');
    var self = this;
    createPiDomeRangedSlider($('#'+uniqueid), min, max, currentvalue, datatype, function(val){
        if(!self.dashInEdit) runDeviceCommand(deviceId, groupId, controlId, Number(val), "", datatype);
    }, parentWidth*0.7, parentHeight*0.7);
};

DashBoardBuilder.prototype.dashDevicePushButton = function (jqel, deviceId, deviceName, groupId, controlId, label, currentvalue, showlabel, datatype){
    var parentWidth = jqel.parent().width();
    var parentHeight = jqel.parent().height();
    jqel.empty();
    jqel.html('<div class="dashboardboxed pressablebuttoncontainer textstroke"'+
                   'style="width:'+parentWidth+'px;height:'+parentHeight+'px; background-image:url(/shared/images/devices/controls/dashboard/pushbutton-bg.svg);"'+
                   'id="deviceCommandPush_' + deviceId + '-' + groupId + '-' + controlId + '">'+
               deviceName + ((showlabel===true)?"<br/><span style=\"font-size:0.8em;\">"+label+"</span>":"") +
              '</div>');
    var self = this;
    $('#deviceCommandPush_' + deviceId + '-' + groupId + '-' + controlId).click(function(){
        if(!self.dashInEdit) runDeviceCommand(deviceId, groupId, controlId, currentvalue, "", datatype);
    });
};

DashBoardBuilder.prototype.dashDeviceToggleButton = function (jqel, deviceId, deviceName, groupId, controlId, label, currentvalue, showlabel){
    var parentWidth = jqel.parent().width();
    var parentHeight = jqel.parent().height();
    jqel.empty();
    jqel.html('<div class="deviceCommandToggle_' + deviceId + '-' + groupId + '-' + controlId + ' dashboardboxed switchabledashcontainer '+((currentvalue===true)?'on':'off')+' textstroke"'+
                   'style="width:'+parentWidth+'px;height:'+parentHeight+'px; background-image:url(/shared/images/devices/controls/dashboard/togglebutton-bg.svg);"'+
                   'data-id="deviceCommandToggle_' + deviceId + '-' + groupId + '-' + controlId + '">'+
               ((showlabel===true)?"<span style=\"font-size:0.8em;\">"+label+"</span><br/>":"") + deviceName + 
              '</div>');
    var self = this;
    $('.deviceCommandToggle_' + deviceId + '-' + groupId + '-' + controlId).off("click").click(function(){
        if(!self.dashInEdit) runDeviceCommand(deviceId, groupId, controlId, ($(this).hasClass('on')?false:true), "");
    });
};

DashBoardBuilder.prototype.dashDeviceDataText = function (jqel, deviceId, groupId, controlId, deviceName, LocationName, controlName, prefix, suffix, currentValue, minValue, maxValue, warnValue, highValue, boolvis, falsetext, truetext, datatype){
    var fontSizePrefs = this.getMultiplyedSize(jqel, 8);
    var fontSizePrefsHeight = this.getMultiplyedSize(jqel, 10);
    var fontSizeData = this.getMultiplyedSize(jqel, 15);
    jqel.empty();
    jqel.html('<div class="dashboardboxed textstroke" style="width:'+jqel.parent().width()+'px; height:'+jqel.parent().height()+'px; text-align: center; vertical-align:top;">'+ 
                '<div style="font-size:'+this.getMultiplyedSize(jqel, 12)+'px;">' + (jqel.attr("data-showdevice")?deviceName:controlName) + '</div>' +
                '<div style="display: table-cell; vertical-align: middle; text-align: center;width:'+jqel.parent().width()+'px;">' +
                    '<div style="font-size:'+fontSizePrefs+'px;min-height:'+fontSizePrefsHeight+'px;">' + prefix + '</div>' +
                    '<div data-datatype="'+datatype+'" data-booltype="'+boolvis+'" data-booltrue="'+truetext+'" data-boolfalse="'+falsetext+'" class="' + deviceId + '_deviceLabelFor_' + groupId + '-' + controlId + '" style="min-height:'+(fontSizeData+2)+'px;font-size:'+fontSizeData+'px;">' + ((currentValue===true)?truetext:falsetext) + '</div> ' + 
                    '<div style="font-size:'+fontSizePrefs+'px;min-height:'+fontSizePrefsHeight+'px;">' + suffix + '</div>' +
                '</div>' +
              '</div>');
    if(datatype==="boolean"){
       setBooleanColor($('.' + deviceId + '_deviceLabelFor_' + groupId + '-' + controlId), currentValue);
    }
};

DashBoardBuilder.prototype.dashDeviceDataGraph = function (jqel, deviceId, groupId, controlId, deviceName, controlName, graphtype, startOption) {
    jqel.empty();
    jqel.html('<div style="width:inherit; height:inherit;" id="' + deviceId + '_dashgraph_' + groupId + '-' + controlId + '-graph"></div>');
    new Graphing(deviceId + '_dashgraph_' + groupId + '-' + controlId + '-graph', window.location.host, deviceId, groupId, controlId, deviceName , controlName, "DEVICE", graphtype, 2, null, true).createGraph(jqel.parent().width(), jqel.parent().height(), startOption);
};

DashBoardBuilder.prototype.dashDeviceDataGauge = function (jqel, deviceId, groupId, controlId, deviceName, LocationName, controlName, prefix, suffix, currentValue, minValue, maxValue, warnValue, highValue, headerLabel) {
    var sizemultiply = (parseInt(jqel.parent().attr("data-sizey"))*.80);
    var stopOptions;
    if(warnValue!=0 && highValue!=0){
        stopOptions = [[(1/maxValue)*(warnValue*0.8), '#55BF3B'], // green
                       [(1/maxValue)*warnValue, '#DDDF0D'], // yellow
                       [(1/maxValue)*highValue, '#DF5353'] // red
                      ];
    } else if (highValue!=0){
        stopOptions = [[(1/maxValue)*(highValue*0.33), '#55BF3B'], // green
                       [(1/maxValue)*(highValue*0.66), '#DDDF0D'], // yellow
                       [(1/maxValue)*highValue, '#DF5353'] // red
                      ];
    } else {
        stopOptions = [[1.0, '#1e78b0'] // blue
                      ];
    }
    var gaugeOptions = {
        chart: {
            type: 'solidgauge',
            backgroundColor:'rgba(255, 255, 255, 0)',
            height: jqel.parent().height(),
            width: null
        },
        title: {
            text: null
        },
        pane: {
            startAngle: -120,
            endAngle: 120,
            background: {
                innerRadius: '60%',
                outerRadius: '100%',
                shape: 'arc',
            }
        },
        tooltip: {
            enabled: false
        },
        // the value axis
        yAxis: {
            stops: stopOptions,
            lineWidth: 0,
            minorTickInterval: null,
            tickInterval: maxValue,
            tickWidth: 0,
            title: {
                y: 52*sizemultiply,
                useHTML: true,
                text: '<center>'+(((headerLabel==="")?'<div style="font-size: '+10*sizemultiply+'px; margin-top: 10px;">' + controlName + '</div><div style="font-size: '+7*sizemultiply+'px;">'+deviceName+'</div>':'<div style="font-size: '+10*sizemultiply+'px;"><br/>' + headerLabel + '</div>'))+'</center>',
                style:{ "color": "#ffffff", "fontWeight": "normal" }
            },
            labels: {
                y: 8*sizemultiply,
                format: '<span style="font-size: '+5*sizemultiply+'px;">{value}</span>',
                style: {"color":"#ffffff","fontWeight":"bold"},
                useHTML: true
            },
            min: minValue,
            max: maxValue
        },
        plotOptions: {
            solidgauge: {
                dataLabels: {
                    y: 2*sizemultiply,
                    borderWidth: 0,
                    useHTML: true
                }
            }
        },
        credits: {
            enabled: false
        },
        series: [{
                name: controlName,
                data: [currentValue],
                dataLabels: {
                    y: -20*sizemultiply,
                    style: {"color":"#ffffff","fontWeight":"normal"},
                    format: '<center><span style="font-size: '+6*sizemultiply+'px;">' + prefix + '<br/></span><span style="font-size: '+12*sizemultiply+'px;">{y}</span><span style="font-size: '+6*sizemultiply+'px;"><br/>' + suffix + '</span></center>'
                }
            }],
        navigation: {
            buttonOptions: {
                enabled: false
            }
        }
    };
    jqel.empty();
    jqel.html('<div style="width:inherit; height:inherit;" id="' + deviceId + '_dashgauge_' + groupId + '-' + controlId + '-gauge"></div>');
    $('#' + deviceId + '_dashgauge_' + groupId + '-' + controlId + '-gauge').highcharts(gaugeOptions);
    pidomeRPCSocket.addCallback(function(params) {
        try {
            var paramSet = params.params;
            if(deviceId === paramSet.id){
                var deviceUpdateId = paramSet.id;
                for(var i=0;i<paramSet.groups.length;i++){
                    if(groupId === paramSet.groups[i].groupid){
                        var groupUpdateId = paramSet.groups[i].groupid;
                        for(var controlSetId in paramSet.groups[i].controls){
                            if(controlSetId === controlId){
                                var chart = $('#' + deviceUpdateId + '_dashgauge_' + groupUpdateId + '-' + controlSetId + '-gauge').highcharts();
                                if (chart) {
                                    var point = chart.series[0].points[0];
                                    point.update(paramSet.groups[i].controls[controlSetId]);
                                }
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        } catch (err) {}
    }, "DeviceService.sendDevice");
};

DashBoardBuilder.prototype.inEdit = function () {
    return this.dashInEdit;
}

DashBoardBuilder.prototype.setEdit = function (inEdit) {
    this.dashInEdit = inEdit;
    if(this.dashInEdit){
        this.setDeleteIcons();
        this.bindAddWidgetsSelection();
        this.mainParentDiv.find("[data-type='spacer']").each(function () {
            $(this).parent().attr('style','background-color:rgba(0,0,0,.5);');
        });
    } else {
        this.unsetDeleteIcons();
        this.mainParentDiv.find("[data-type='spacer']").each(function () {
            $(this).parent().attr('style','background-color:transparent;');
        });
    }
};

DashBoardBuilder.prototype.bindAddWidgetsSelection = function (){
    var self = this;
    $("#dashboardaddbutton").off("click").on("click", function(){
        self.itemSelectionModal.open();
    });
}

DashBoardBuilder.prototype.createWidgetItems = function (data){
    var self = this;
    switch(data.itemType){
        case "currenttime":
            if(self.mainParentDiv.find("[data-type='time']").length>0){
                quickMessage("info", "There already is a time widget");
            } else {
                var cell = self.addGridCell('<div class="dashboard-tile gs-resize-disabled" data-type="time"></div>',2,1);
                self.dashBoardTimeBuilder(cell,true, function(){ self.setDeleteIcon(cell.parent()); });
            }
        break;
        case "weatherplugin":
            if(self.mainParentDiv.find("[data-type='weather']").length>0){
                quickMessage("info", "There already is a weather widget");
            } else {
                var cell = self.addGridCell('<div class="dashboard-tile gs-resize-disabled" data-type="weather"></div>',3,2);
                self.dashBoardWeatherBuilder(cell, true, function(){ self.setDeleteIcon(cell.parent()); });
            }
        break;
        case "macro":
            if(self.mainParentDiv.find("[data-type='macro'][data-id='"+data.macroId+"']").length>0){
                quickMessage("info", "This macro is already present");
            } else {
                var cell = self.addGridCell('<div class="dashboard-tile gs-resize-disabled" data-type="macro" data-id="'+data.macroId+'"></div>',1,1);
                self.dashBoardMacroBuilder(cell,parseInt(data.macroId), function(){ self.setDeleteIcon(cell.parent()); });
            }
        break;
        case "scene":
            if(self.mainParentDiv.find("[data-type='scene'][data-id='"+data.sceneId+"']").length>0){
                quickMessage("info", "This Scene is already present");
            } else {
                var cell = self.addGridCell('<div class="dashboard-tile gs-resize-disabled" data-type="scene" data-id="'+data.sceneId+'"></div>',1,1);
                self.dashBoardSceneBuilder(cell,parseInt(data.sceneId), function(){ self.setDeleteIcon(cell.parent()); });
            }
        break;
        case "device":
            if(self.mainParentDiv.find("[data-type='device'][data-id='"+data.deviceId+"'][data-group='"+data.deviceGroupId+"'][data-control='"+data.deviceCommandId+"']").length>0){
                quickMessage("info", "This specific device control is already present");
            } else {
                switch(data.deviceCommandType){
                    case "toggle":
                    case "button":
                        var toggleCell = self.addGridCell('<div class="dashboard-tile gs-resize-disabled" data-type="device" data-id="'+data.deviceId+'" data-group="'+data.deviceGroupId+'" data-control="'+data.deviceCommandId+'"></div>',1,1);
                        self.dashBoardDeviceBuilder(toggleCell, parseInt(data.deviceId), data.deviceGroupId, data.deviceCommandId, null, function(){ self.setDeleteIcon(toggleCell.parent()); });
                    break;
                    case "select":
                        var selectCell = self.addGridCell('<div class="dashboard-tile gs-resize-disabled" data-type="device" data-id="'+data.deviceId+'" data-group="'+data.deviceGroupId+'" data-control="'+data.deviceCommandId+'"></div>',1,1);
                        self.dashBoardDeviceBuilder(selectCell, parseInt(data.deviceId), data.deviceGroupId, data.deviceCommandId, null, function(){ self.setDeleteIcon(selectCell.parent()); });
                    break;
                    case "slider":
                        var sliderCell = self.addGridCell('<div class="dashboard-tile gs-resize-disabled" data-type="device" data-id="'+data.deviceId+'" data-group="'+data.deviceGroupId+'" data-control="'+data.deviceCommandId+'"></div>',2,2);
                        self.dashBoardDeviceBuilder(sliderCell, parseInt(data.deviceId), data.deviceGroupId, data.deviceCommandId, null, function(){ self.setDeleteIcon(sliderCell.parent()); });                        
                    break;
                    case "colorpicker":
                        yesnoConfirmation(data.deviceName + ' - ' + data.deviceCommandName, '<form id="dash-colorpicker-text-type-selection">'+
                            '<div id="dash-data-numeric-type-selection-typetext">'+
                            '    <div class="form-group">'+
                            '        <label for="dash-colorpicker-typetext-showcontrol" class="control-label">Show:</label>'+
                            '        <select id="dash-colorpicker-typetext-showcontrol" class="form-control">'+
                            '            <option value="control">Control name</option>'+
                            '            <option value="device">Device name</option>'+
                            '        </select>'+
                            '    </div>'+
                            '</div>'+
                            '</form>', function(){
                                var showDevice = '';
                                if($("#dash-colorpicker-typetext-showcontrol").val()==="device"){
                                    showDevice = ' data-showdevice="true"';
                                }
                                var colorPickerCell = self.addGridCell('<div class="dashboard-tile gs-resize-disabled" data-type="device" data-id="'+data.deviceId+'" data-group="'+data.deviceGroupId+'" data-control="'+data.deviceCommandId+'"'+showDevice+'></div>',1,1);
                                self.dashBoardDeviceBuilder(colorPickerCell, parseInt(data.deviceId), data.deviceGroupId, data.deviceCommandId, null, function(){ self.setDeleteIcon(colorPickerCell.parent()); });
                        }, "Add", "Cancel");
                    break;
                    case "data":
                        switch(data.deviceCommandDataType){
                            case "float":
                            case "integer":
                                yesnoConfirmation(data.deviceName + ' - ' + data.deviceCommandName, '<form id="dash-data-numeric-type-selection">'+
                                    '<div class="form-group">'+
                                    '    <label for="dash-data-numeric-type-selection-type" class="control-label">How do you want to view the data</label>'+
                                    '    <select id="dash-data-numeric-type-selection-type" class="form-control">'+
                                    '        <option value="">Select visualization type</option>'+
                                    '        <option value="text">Textual</option>'+
                                    '        <option value="gauge">Gauge</option>'+
                                    '        <option value="graph">Graph</option>'+
                                    '    </select>'+
                                    '</div>'+
                                    '<div style="display:none;" id="dash-data-numeric-type-selection-typetext">'+
                                    '    <div class="form-group">'+
                                    '        <label for="dash-data-numeric-type-selection-typetext-showcontrol" class="control-label">Show:</label>'+
                                    '        <select id="dash-data-numeric-type-selection-typetext-showcontrol" class="form-control">'+
                                    '            <option value="control">Control name</option>'+
                                    '            <option value="device">Device name</option>'+
                                    '        </select>'+
                                    '    </div>'+
                                    '</div>'+
                                    '<div style="display:none;" id="dash-data-numeric-type-selection-typegraph">'+
                                    '    <p>Press add to add the graph</p>'+
                                    '</div>'+
                                    '<div style="display:none;" id="dash-data-numeric-type-selection-typegauge">'+
                                    '    <div class="form-group">'+
                                    '        <label for="dash-data-numeric-type-selection-typegauge-minvalue" class="control-label">Minimal value</label>'+
                                    '        <input type="text" data-type="numeric" class="form-control" name="dash-data-numeric-type-selection-typegauge-minvalue" id="dash-data-numeric-type-selection-typegauge-minvalue" placeholder="Enter the minimum possible value" required>'+
                                    '    </div>'+
                                    '    <div class="form-group">'+
                                    '        <label for="dash-data-numeric-type-selection-typegauge-maxvalue" class="control-label">Maximum value</label>'+
                                    '        <input type="text" data-type="numeric" class="form-control" name="dash-data-numeric-type-selection-typegauge-maxvalue" id="dash-data-numeric-type-selection-typegauge-maxvalue" placeholder="Enter the maximum possible value" required>'+
                                    '    </div>'+
                                    '    <div class="form-group">'+
                                    '        <label for="dash-data-numeric-type-selection-typegauge-warnvalue" class="control-label">Warning value</label>'+
                                    '        <input type="text" data-type="numeric" class="form-control" name="dash-data-numeric-type-selection-typegauge-warnvalue" id="dash-data-numeric-type-selection-typegauge-warnvalue" placeholder="Enter the warning value" required>'+
                                    '    </div>'+
                                    '    <div class="form-group">'+
                                    '        <label for="dash-data-numeric-type-selection-typegauge-highvalue" class="control-label">High value</label>'+
                                    '        <input type="text" data-type="numeric" class="form-control" name="dash-data-numeric-type-selection-typegauge-highvalue" id="dash-data-numeric-type-selection-typegauge-highvalue" placeholder="Enter the high value" required>'+
                                    '    </div>'+
                                    '    <div class="form-group">'+
                                    '        <label for="dash-data-numeric-type-selection-typegauge-highvalue" class="control-label">Custom label</label>'+
                                    '        <input type="text" data-type="numeric" class="form-control" name="dash-data-numeric-type-selection-typegauge-customlabel" id="dash-data-numeric-type-selection-typegauge-customlabel" placeholder="Enter a text for a custom description, leave empty for defaults">'+
                                    '    </div>'+
                                    '</div>'+
                                    '</form>', function(){
                                        switch($("#dash-data-numeric-type-selection-type").val()){
                                            case "text":
                                                var showDevice = '';
                                                if($("#dash-data-numeric-type-selection-typetext-showcontrol").val()==="device"){
                                                    showDevice = ' data-showdevice="true"';
                                                }
                                                var dataCell = self.addGridCell('<div class="dashboard-tile  gs-resize-disabled" data-type="device" data-id="'+data.deviceId+'" data-group="'+data.deviceGroupId+'" data-control="'+data.deviceCommandId+'" data-visual="text" '+showDevice+'></div>',1,1);
                                                self.dashBoardDeviceBuilder(dataCell, parseInt(data.deviceId), data.deviceGroupId, data.deviceCommandId, "text", function(){ self.setDeleteIcon(dataCell.parent()); });
                                            break;
                                            case "gauge":
                                                var dataCell = self.addGridCell('<div class="dashboard-tile  gs-resize-disabled" data-customlabel="'+$("#dash-data-numeric-type-selection-typegauge-customlabel").val()+'" data-type="device" data-id="'+data.deviceId+'" data-group="'+data.deviceGroupId+'" data-control="'+data.deviceCommandId+'" data-visual="gauge" data-minvalue="'+$("#dash-data-numeric-type-selection-typegauge-minvalue").val()+'" data-maxvalue="'+$("#dash-data-numeric-type-selection-typegauge-maxvalue").val()+'" data-warnvalue="'+$("#dash-data-numeric-type-selection-typegauge-warnvalue").val()+'" data-highvalue="'+$("#dash-data-numeric-type-selection-typegauge-highvalue").val()+'"></div>',2,2);
                                                self.dashBoardDeviceBuilder(dataCell, parseInt(data.deviceId), data.deviceGroupId, data.deviceCommandId, "gauge", function(){ self.setDeleteIcon(dataCell.parent()); });
                                            break;
                                            case "graph":
                                                var dataCell = self.addGridCell('<div class="dashboard-tile  gs-resize-disabled" data-type="device" data-id="'+data.deviceId+'" data-group="'+data.deviceGroupId+'" data-control="'+data.deviceCommandId+'" data-visual="graph"></div>',5,3);
                                                self.dashBoardDeviceBuilder(dataCell, parseInt(data.deviceId), data.deviceGroupId, data.deviceCommandId, "graph", function(){ self.setDeleteIcon(dataCell.parent()); });
                                            break;
                                        }
                                }, "Add", "Cancel");
                                $("#dash-data-numeric-type-selection-typegauge-minvalue").val(data.deviceCommandMinValue);
                                $("#dash-data-numeric-type-selection-typegauge-maxvalue").val(data.deviceCommandMaxValue);
                                $("#dash-data-numeric-type-selection-typegauge-warnvalue").val(data.deviceCommandWarnValue);
                                $("#dash-data-numeric-type-selection-typegauge-highvalue").val(data.deviceCommandHighValue);
                                $("#dash-data-numeric-type-selection-type").on("change", function(){
                                    $("#dash-data-numeric-type-selection-typetext").hide();
                                    $("#dash-data-numeric-type-selection-typegraph").hide();
                                    $("#dash-data-numeric-type-selection-typegauge").hide();
                                    switch($(this).val()){
                                        case "text":
                                            $("#dash-data-numeric-type-selection-typetext").show();
                                        break;
                                        case "gauge":
                                            $("#dash-data-numeric-type-selection-typegauge").show();
                                        break;
                                        case "graph":
                                            $("#dash-data-numeric-type-selection-typegraph").show();
                                        break;
                                    }
                                });
                            break;
                            default:
                                var dataCell = self.addGridCell('<div class="dashboard-tile  gs-resize-disabled" data-type="device" data-id="'+data.deviceId+'" data-group="'+data.deviceGroupId+'" data-control="'+data.deviceCommandId+'"></div>',1,1);
                                self.dashBoardDeviceBuilder(dataCell, parseInt(data.deviceId), data.deviceGroupId, data.deviceCommandId, "text", function(){ self.setDeleteIcon(dataCell.parent()); });
                            break;
                        }
                    break;
                    default:
                        quickMessage("error", "The selected control is currently unsupported.");
                    break;
                }
            }
        break;
    }
};

DashBoardBuilder.prototype.addGridCell = function (content, width, height){
    var cell = this.gridster.add_widget.apply(this.gridster, ['<li>'+content+'</li>', width, height]).children(":first");
    return cell;
};

DashBoardBuilder.prototype.setDeleteIcon = function(cell){
    var gridst = this.gridster;
    cell.append('<div style="z-index:10000;position:absolute; top: 0px; right: 0px; width:20px; height:20px; background-color: rgba(0,0,0,0.4);" class="dashboard-delete-tile">'+
                       '<span class="glyphicon glyphicon-remove-circle" aria-hidden="true" style="font-size:20px; width:20px;height:20px;"></span>' +
                    '</div>');
    cell.find(".dashboard-delete-tile").each(function(){
        $(this).on("click", function(){
            gridst.remove_widget( $(this).parent());
        });
    });
};

DashBoardBuilder.prototype.setDeleteIcons = function (){
    var self = this;
    $(".gs-w").each(function(){
        self.setDeleteIcon($(this));
    });
};

DashBoardBuilder.prototype.unsetDeleteIcons = function (){
    $(".dashboard-delete-tile").each(function(){
        $(this).off("click");
        $(this).remove();
    });
}

DashBoardBuilder.prototype.saveGrid = function (id, name, type, clientid, personid){
    var data = JSON.stringify(this.gridster.serialize());
    switch(type){
        case "WEB":
            postHttpJsonRPC('{"jsonrpc": "2.0", "method": "DashboardService.saveUserDashboard", "params":{"id":'+id+', "name":"'+name+'", "personid":'+personid+', "type":"'+type+'", "construct":'+data+'},"id":"DashboardService.saveUserDashboard"}', function(){
                quickMessage("success", "Personal web dashboard saved");
                refreshPageContent("/dashboards.html");
            });
        break;
        case "MOBILE":
            postHttpJsonRPC('{"jsonrpc": "2.0", "method": "DashboardService.saveUserMobileDashboard", "params":{"id":'+id+', "name":"'+name+'", "personid":'+personid+', "clientid":'+clientid+', "construct":'+data+'},"id":"DashboardService.saveUserMobileDashboard"}', function(){
                quickMessage("success", "Personal mobile dashboard saved");
                refreshPageContent("/dashboards.html");
            });
        break;
        case "DISPLAY":
            postHttpJsonRPC('{"jsonrpc": "2.0", "method": "DashboardService.saveClientDashboard", "params":{"id":'+id+', "name":"'+name+'", "clientid":'+clientid+', "construct":'+data+'},"id":"DashboardService.saveClientDashboard"}', function(){
                quickMessage("success", "Display dashboard saved");
                refreshPageContent("/dashboards.html");
            });
        break;
        case "HYBRID":
            postHttpJsonRPC('{"jsonrpc": "2.0", "method": "DashboardService.saveUserDashboardForClient", "params":{"id":'+id+', "name":"'+name+'", "personid":'+personid+', "clientid":'+clientid+', "construct":'+data+'},"id":"DashboardService.saveUserDashboardForClient"}', function(){
                quickMessage("success", "Personalized display dashboard saved");
                refreshPageContent("/dashboards.html");
            });
        break;
    }
}