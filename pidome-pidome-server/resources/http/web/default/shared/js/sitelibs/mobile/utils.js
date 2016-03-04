var initDone = false;
$( document ).on( "mobileinit" , function () {
    $.mobile.linkBindingEnabled = true;
    $.mobile.ajaxEnabled = true;
    $.mobile.page.prototype.options.domCache = false;
});
$(document).ready(function() {
    if(!initDone){
        $( "#mainmenupanel" ).panel();
        $( "[data-role='header'], [data-role='footer']" ).toolbar();
        $("#simplenotification").popup();
        $("#mainmenupanel").enhanceWithin();
        $("#menuset li").each(function(){
            if($(this).attr("id")!==undefined){
                $(this).on("click", function(){
                    var page = $(this).attr("id").replace("page_","")+".html?requesttype=ajax";
                    $("#main-content-body").load(page, function(){
                        pidomeRPCSocket.clearFallbacks();
                        $("#main-content-body").enhanceWithin();
                        setTimeout(function(){ 
                            $( "#mainmenupanel" ).panel("close");
                        }, 400);
                    });
                });
            }
        });
    }
    initDone = true;
});

/**
 * Set a duration.
 * @param {type} seconds
 * @returns {String}
 */
function toHHMMSS(seconds) {
    var sec_num = parseInt(seconds, 10);
    var hours   = Math.floor(sec_num / 3600);
    var minutes = Math.floor((sec_num - (hours * 3600)) / 60);
    var seconds = sec_num - (hours * 3600) - (minutes * 60);

    if (hours   < 10) {hours   = "0"+hours;}
    if (minutes < 10) {minutes = "0"+minutes;}
    if (seconds < 10) {seconds = "0"+seconds;}
    var time    = hours+':'+minutes+':'+seconds;
    return time;
}

function setPage(id, name){
    $("#menuset li").each(function(){
        if($(this).attr("id")!==undefined){
            var workId = $(this).attr("id");
            var selectId = "page_" + id;
            if(workId === selectId){
                var imgSrc = $("#" + workId + " img").attr("src");
                imgSrc = imgSrc.replace("-active", "");
                $("#" + workId + " img").attr("src", imgSrc.replace(id, id+"-active"));
                $("#" + workId + " span").addClass("active");
            } else {
                $("#" + workId + " img").attr("src", $("#" + workId + " img").attr("src").replace("-active", ""));
                $("#" + workId + " span").removeClass("active");
            }
        }
    });
    $("#pagetitle").html(" - " + name);
}

/**
 * Show a popup
 * @param {type} text
 * @returns {undefined}
 */
function showPopUp(text){
    $("#simplenotification-message").html(text);
    $("#simplenotification").popup("open");
}
function closePupUp(){
    $("#simplenotification").popup("close");
}
function updatePopupText(text){
    $("#simplenotification-message").html(text);
    $("#simplenotification").popup('reposition', 'positionTo: window');
}


function createDeviceShortcutItem(pos, deviceData){
    for(var i=0;i<deviceData.commandgroups.length;i++){
        var groupData = deviceData.commandgroups[i];
        for(var j=0;j<groupData.commands.length;j++){
            var curCommand = groupData.commands[j];
            var groupCommandSet = groupData.id+'-'+curCommand.typedetails.id;
            if(curCommand.typedetails.shortcut!==undefined && curCommand.typedetails.shortcut==pos){
                switch(curCommand.commandtype){
                    case "data":
                        return '<span>'+curCommand.typedetails.prefix+' <span id="'+deviceData.id+'_deviceLabelFor_'+groupData.id+'-'+curCommand.typedetails.id+'">'+curCommand.currentvalue+'</span> '+curCommand.typedetails.suffix +'</span>';    
                    break;
                    case "button":
                        return '<button data-role="button" data-mini="true" data-inline="true" data-corners="false" class="ui-mini" id="'+deviceData.id+'_deviceAction-'+groupCommandSet+'" name="'+deviceData.id+'_deviceAction-'+groupCommandSet+'">'+curCommand.typedetails.label+'</button>\n\
                            <script>\n\
                                $( "#'+deviceData.id+'_deviceAction-'+groupCommandSet+'" ).off("click");\n\
                                $( "#'+deviceData.id+'_deviceAction-'+groupCommandSet+'" ).on("click",function(){ \n\
                                    runDeviceCommand('+deviceData.id+', "'+groupData.id+'", "'+curCommand.typedetails.id+'", "'+curCommand.typedetails.deviceCommandValue+'","", "string");\n\
                                });\n\
                            <\/script>';
                    break;
                    case 'toggle':
                        return '<select data-corners="false" data-mini="true" data-role="flipswitch" data-on-text="'+curCommand.typedetails.commandset.on.label+'" name="flip-checkbox-1" id="'+deviceData.id+'_deviceToggleFor_'+groupCommandSet+'">\n\
                                    <option value="off" '+((curCommand.currentvalue==false)?'selected="selected"':'')+'>'+curCommand.typedetails.commandset.off.label+'</option>\
                                    <option value="on" '+((curCommand.currentvalue==true)?'selected="selected"':'')+'>'+curCommand.typedetails.commandset.on.label+'</option>\
                                </select>\n\
<script>\n\
$("#'+deviceData.id+'_deviceToggleFor_'+groupCommandSet+'").on("change", function () {\n\
        if ($(this).val() == "on") {\n\
            runDeviceCommand('+deviceData.id+', "'+groupData.id+'", "'+curCommand.typedetails.id+'", true,"");\n\
        }\n\
        if ($(this).val() == "off") {\n\
            runDeviceCommand('+deviceData.id+', "'+groupData.id+'", "'+curCommand.typedetails.id+'", false,"");\n\
        }\n\
    });\n\
<\/script>';
                    break;
                    case "slider":
                        return '<div style="height:1.7em; min-width:200px;"><input class="slideractions" data-highlight="true" type="range" name="'+deviceData.id+'_deviceSliderFor_'+groupCommandSet+'" id="'+deviceData.id+'_deviceSliderFor_'+groupCommandSet+'" min="'+curCommand.typedetails.min+'" max="'+curCommand.typedetails.max+'" value="'+curCommand.currentvalue+'" /></div>';
                    break;
                    case "colorpicker":
                                                    var colorPickerAdd = 'var localColorGetObject = {h:0,s:0,b:0};\n';
                                                    colorPickerAdd += 'var localSetColorObject = {h: '+curCommand.typedetails.color.hsb.h*360+', s: '+curCommand.typedetails.color.hsb.s+', v: '+curCommand.typedetails.color.hsb.b+'};\n';
                                                    colorPickerAdd += 'var localHsl = tinycolor(localSetColorObject).toHsl();\n';
                                                    switch(curCommand.typedetails.mode){
                                                        case "rgb":
                                                           colorPickerAdd += '$("#' + deviceData.id+'_deviceColorPickerFor_'+groupCommandSet+'").ColorPickerSliders({\n\
                                                                flat: true,\n\
                                                                customswatches: false,\n\
                                                                swatches: ["red", "yellow", "lime", "aqua", "blue", "magenta", "white"],\n\
                                                                previewformat: "rgb",\n\
                                                                order: {\n\
                                                                    rgb: 1,\n\
                                                                    preview: 2\n\
                                                                },\n\
                                                                labels: {\n\
                                                                    rgbred: "Red",\n\
                                                                    rgbgreen: "Green",\n\
                                                                    rgbblue: "Blue"\n\
                                                                },\n\
                                                                color: localSetColorObject,\n\
                                                                onchange: function(container, color) {\n\
                                                                    var newColor = color.tiny.toHsv();\n\
                                                                    localColorGetObject.h = newColor.h/360;\n\
                                                                    localColorGetObject.s = newColor.s;\n\
                                                                    localColorGetObject.b = newColor.v;\n\
                                                                }\n\
                                                            });';
                                                        break;
                                                        case "cie":
                                                            colorPickerAdd += '$("#' + deviceData.id+'_deviceColorPickerFor_'+groupCommandSet+'").ColorPickerSliders({\n\
                                                                flat: true,\n\
                                                                customswatches: false,\n\
                                                                swatches: ["red", "yellow", "lime", "aqua", "blue", "magenta", "white"],\n\
                                                                previewformat: "hsl",\n\
                                                                order: {\n\
                                                                    cie: 1,\n\
                                                                    preview: 2\n\
                                                                },\n\
                                                                labels: {\n\
                                                                    rgbred: "Red",\n\
                                                                    rgbgreen: "Green",\n\
                                                                    rgbblue: "Blue"\n\
                                                                },\n\
                                                                color: localSetColorObject,\n\
                                                                onchange: function(container, color) {\n\
                                                                    var newColor = color.tiny.toHsv();\n\
                                                                    localColorGetObject.h = newColor.h/360;\n\
                                                                    localColorGetObject.s = newColor.s;\n\
                                                                    localColorGetObject.b = newColor.v;\n\
                                                                }\n\
                                                            });';
                                                        break;
                                                        case "hsb":
                                                            colorPickerAdd += '$("#' + deviceData.id+'_deviceColorPickerFor_'+groupCommandSet+'").ColorPickerSliders({\n\
                                                                flat: true,\n\
                                                                customswatches: false,\n\
                                                                swatches: ["red", "yellow", "lime", "aqua", "blue", "magenta", "white"],\n\
                                                                previewformat: "hsl",\n\
                                                                order: {\n\
                                                                    hsl: 1,\n\
                                                                    preview: 2\n\
                                                                },\n\
                                                                labels: {\n\
                                                                    rgbred: "Red",\n\
                                                                    rgbgreen: "Green",\n\
                                                                    rgbblue: "Blue"\n\
                                                                },\n\
                                                                color: localSetColorObject,\n\
                                                                onchange: function(container, color) {\n\
                                                                    var newColor = color.tiny.toHsv();\n\
                                                                    localColorGetObject.h = newColor.h/360;\n\
                                                                    localColorGetObject.s = newColor.s;\n\
                                                                    localColorGetObject.b = newColor.v;\n\
                                                                }\n\
                                                            });';
                                                        break;
                                                    }
                                                    var buttonsetlist = "";
                                                    for(var button = 0; button < curCommand.typedetails.commandset.length;button++){
                                                        var curButton = curCommand.typedetails.commandset[button];
                                                        buttonsetlist += '<div style="margin-bottom: 3px;">\n\
                                                                            <button class="colorActionButton" data-corners="false" data-role="button" id="deviceColorPickerFor_'+deviceData.id+'-'+groupCommandSet+'-'+curButton.value+'" name="deviceColorPickerFor_'+groupCommandSet+'-'+curButton.value+'" value="'+curButton.label+'">'+curButton.label+'</button>\n\
                                                                          </div>';
                                                    }
                                                    colorPickerAdd += '$( ".colorActionButton").off("click");\n$( ".colorActionButton").on("click", function() {\n\
                                                                            var deviceStruct = $(this).attr("id").split("_")[1].split("-");\n\
                                                                            runDeviceCommand(parseInt(deviceStruct[0]), deviceStruct[1], deviceStruct[2],JSON.stringify(localColorGetObject),deviceStruct[3]);\n\
                                                                        });';
                                                    return '<div data-position-to="window" data-role="popup" id="' + deviceData.id+'_opencolorpickerfor_'+groupCommandSet+'">\n\
                                                                    <a href="#" data-rel="back" data-role="button" data-theme="a" data-icon="delete" data-iconpos="notext" class="ui-btn-right">Close</a>\n\
                                                                    <div class="colorpickercontainer" style="padding:1em;">\n\
                                                                        <div style="float:left; margin-top: 0.5em;" id="' + deviceData.id+'_deviceColorPickerFor_'+groupCommandSet+'"></div>\n\
                                                                        <div style="float:left; margin-left: 1em;" id="' + deviceData.id+'_colorpickerbuttonsetfor_'+groupCommandSet+'">'+buttonsetlist+'</div>\n\
                                                                        <div style="clear:both"></div>\n\
                                                                    </div>\n\
                                                                </div>\n\
                                                                <a data-rel="popup" href="#' + deviceData.id+'_opencolorpickerfor_'+groupCommandSet+'" class="ui-btn ui-btn-inline button-control colorpicker" data-corners="false" data-mini="true" class="ui-btn" id="'+deviceData.id+'_colorpickeropener_'+groupCommandSet+'">'+curCommand.typedetails.label+'</a>\n\
<script>'+colorPickerAdd+'\n\
$("#' + deviceData.id+'_opencolorpickerfor_'+groupCommandSet+'").enhanceWithin().popup();\n$("#'+deviceData.id+'_colorpickeropener_'+groupCommandSet+'").css("background-color",tinycolor(localSetColorObject).toHexString());<\/script>';
                    break;
                }
            }
        }
    }
    return "";
}

function runDeviceCommand(id, group, control, value, extra, dataType){
    var url;
    if(dataType!=undefined && dataType=="string"){
        url = "/jsonrpc.json?rpc={\"jsonrpc\": \"2.0\", \"method\": \"DeviceService.sendDevice\", \"params\": ["+id+", \""+group+"\", \""+control+"\", {\"value\":\""+value+"\",\"extra\":\""+extra+"\"}] \"id\": \"DeviceService.sendDevice\"}";
    } else {
        url = "/jsonrpc.json?rpc={\"jsonrpc\": \"2.0\", \"method\": \"DeviceService.sendDevice\", \"params\": ["+id+", \""+group+"\", \""+control+"\", {\"value\":"+value+",\"extra\":\""+extra+"\"}] \"id\": \"DeviceService.sendDevice\"}";
    }
    $.get(url)
        .done(function(data) {
            try {
                if(data.result === undefined){
                    var message = "<strong>Message</strong>:<br/>";
                    if(data.error.data.trace!==undefined){
                        message += data.error.message + "<br/><br/><strong>Trace</strong>:<br/>" + data.error.data.trace;
                    } else {
                        message += data.error.message;
                    }
                    showErrorMessage("Device command error", message);
                } else if (data.error !== undefined){
                    showErrorMessage(data.error.message, data.result.data.message);
                }
            } catch(err){
                showErrorMessage("Server error", err);
            }
        }, "json");
}

//////////////////////////
///
/// For each specific device command set type a function so it can be live updated
/**
 * Used for setting device data by an incoming json data stream result.
 * @param {type} paramSet
 * @returns {undefined}
 */
function updateDeviceByJSONRPC(paramSet){
    var deviceId = paramSet.id;
    for(var i=0;i<paramSet.groups.length;i++){
         var groupId = paramSet.groups[i].groupid;
         for(var controlSetId in paramSet.groups[i].controls){
             var controlId = groupId + "-" + controlSetId;
            if ($("#"+deviceId+"_deviceToggleFor_" + controlId).length !== 0) {
                setDeviceToggleValue(deviceId, controlId, paramSet.groups[i].controls[controlSetId]);
            } else if ($("#"+deviceId+"_deviceSelectFor_" + controlId).length !== 0) {
                setDeviceSelectValue(deviceId, controlId, paramSet.groups[i].controls[controlSetId]);
            } else if ($("#"+deviceId+"_deviceSliderFor_" + controlId).length !== 0) {
                setDeviceSliderValues(deviceId, controlId, paramSet.groups[i].controls[controlSetId]);
            } else if ($("#"+deviceId+"_deviceLabelFor_" + controlId).length !== 0) {
                setDeviceDataValues(deviceId, controlId, paramSet.groups[i].controls[controlSetId]);
            } else if ($("#"+deviceId+"_deviceColorPickerFor_" + controlId).length !== 0) {
                setDeviceColorPickerValues(deviceId, controlId, paramSet.groups[i].controls[controlSetId]);
            }
        }
    }
}

/**
 * Update color picker.
 * @param {type} deviceId
 * @param {type} controlId
 * @param {type} value
 * @returns {undefined}
 */
function setDeviceColorPickerValues(deviceId, controlId, value){
    if ($("#"+deviceId+"_deviceColorPickerFor_" + controlId).length !== 0) {
        var localSetColorObject = {h: value.hsb.h*360, s: value.hsb.s, v: value.hsb.b};
        $("#"+deviceId+"_deviceColorPickerFor_" + controlId).trigger("colorpickersliders.updateColor", localSetColorObject);
        var localHsl = tinycolor(localSetColorObject).toHsl();
        $('#'+deviceId+'_colorpickeropener_'+controlId).css({'background':'hsl('+localHsl.h+', '+localHsl.s*100+'%, '+localHsl.l*100+'%'});
    }
}
/**
 * Sets the values for the data labels for devices.
 * @param {type} deviceId
 * @param {type} controlId
 * @param {type} value
 * @returns {undefined}
 */
function setDeviceDataValues(deviceId, controlId, value){
    if ($("#"+deviceId+"_deviceLabelFor_" + controlId).length !== 0) {
        $("#"+deviceId+"_deviceLabelFor_" + controlId).text(value)
    }
}
/**
 * Sets the selected toggle value
 * @param {type} deviceId
 * @param {type} toggleId
 * @param {type} buttonId
 * @returns {undefined}
 */
function setDeviceToggleValue(deviceId, toggleId, buttonId){
    if ($("#"+deviceId+"_deviceToggleFor_" + toggleId).length !== 0) {
        $("#"+deviceId+"_deviceToggleFor_" + toggleId).val((buttonId===true)?"on":"off").flipswitch("refresh");
    }
}
/**
 * Sets the current selection
 * @param {type} deviceId
 * @param {type} selectId
 * @param {type} valueId
 * @returns {undefined}
 */
function setDeviceSelectValue(deviceId, selectId, valueId){
    if ($("#"+deviceId+"_deviceSelectFor_" + selectId).length !== 0) {
        var item = $("#"+deviceId+"_deviceSelectFor_" + selectId).jqxDropDownList('getItemByValue', valueId); 
        if(item!==undefined && item.index!==undefined){
            $("#"+deviceId+"_deviceSelectFor_" + selectId).jqxDropDownList('selectIndex', item.index);
        }
    }
}

/**
 * Sets the device values for any slider.
 * @param {type} deviceId
 * @param {type} selectId
 * @param {type} valueId
 * @returns {undefined}
 */
function setDeviceSliderValues(deviceId, selectId, valueId){
    if ($("#"+deviceId+"_deviceSliderFor_" + selectId).length !== 0) {
        $("#"+deviceId+"_deviceSliderFor_" + selectId).val(valueId).slider("refresh");
    }
}
