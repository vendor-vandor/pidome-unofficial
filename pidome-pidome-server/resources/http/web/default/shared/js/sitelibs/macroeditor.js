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

    var itemSelectionModal = new ItemSelectionModal();
    itemSelectionModal.setOptions({"deviceFilter": ["!data"], "deviceTypeFilter":["devices", "macros", "presences", "dayparts", "userstatuses","mediaplugins", "remotes"]});
    itemSelectionModal.setSelectionType("exec");
    
    function createExecItemsByMapping(data){
        var newData;
        try {
            switch(data.itemtype){
                case "device":
                    newData = {
                        "itemType"             : "device",
                        "deviceName"           : data.devicename,
                        "deviceCommandName"    : data.commandname,
                        "deviceCommandType"    : data.commandtype,
                        "deviceId"             : data.deviceid,
                        "deviceGroupId"        : data.group,
                        "deviceCommandId"      : data.command,
                        "deviceCommandValue"   : data.value,
                        "deviceCommandDataType": data.datatype,
                        "deviceCommandExtra"   : data.extra
                    };
                    createExecItems(newData);
                break;
                case "macro":
                    newData = {
                        "itemType"  : "macro",
                        "macroId"   : data.macroid,
                        "macroName" : data.macroname
                    };
                    createExecItems(newData);
                break;
                case "presence":
                    newData = {
                        "itemType"   : "presence",
                        "presenceid" : data.presenceid
                    };
                    createExecItems(newData);
                break;
                case "daypart":
                    newData = {
                        "itemType"   : "daypart",
                        "daypartid" : data.daypartid
                    };
                    createExecItems(newData);
                break;
                case "userstatus":
                    newData = {
                        "itemType"    : "userstatus",
                        "userstatusid": data.userstatusid
                    };
                    createExecItems(newData);
                break;
                case "mediaplugin":
                    newData = {
                        "itemType"            : data.itemtype,
                        "mediaId"             : data.mediaId,
                        "mediaName"           : data.mediaName,
                        "mediaCommandTypeId"  : data.mediaCommandTypeId,
                        "mediaCommandTypeName": data.mediaCommandTypeName,
                        "mediaCommandId"      : data.mediaCommandId
                    };
                    createExecItems(newData);
                break;
                case "remoteplugin":
                    newData = {
                        "itemType"       : data.itemtype,
                        "remoteId"       : data.remoteId,
                        "remoteName"     : data.remoteName,
                        "remoteButtonId" : data.remoteButtonId
                    };
                    createExecItems(newData);
                break;
            }
        } catch (err){
            alert(err);
        }
    }
    
    function createExecItems(returnData){
        var deviceCommandValue;
        if(typeof returnData.deviceCommandValue==="undefined"){
            deviceCommandValue = "";
        } else {
            deviceCommandValue = returnData.deviceCommandValue;
        }
        var uniqueItemId = createUUID();
        var deviceDivId = 'DIV_macroExecItem_' + uniqueItemId;
        switch(returnData.itemType){
            case "device":
                getHttpJsonRPC('{"jsonrpc": "2.0", "method": "DeviceService.getDeviceCommand", "params":{"id":' + returnData.deviceId + ', "groupid":"' + returnData.deviceGroupId + '", "commandid":"' + returnData.deviceCommandId + '"},"id":"DeviceService.getDeviceCommand"}', function(data){
                    var deviceHtml = '<div style="display:table;" id="divTableId'+uniqueItemId+'"><button id="deleteExecItem_' + uniqueItemId + '" class="btn btn-danger">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; height: 40px; min-width:450px;">Set <strong>' + returnData.deviceCommandName + '</strong> of <strong>' + returnData.deviceName + '</strong> to:&nbsp; </span><div style="display:table-cell; vertical-align:middle; height: 40px;"><div id="DIV_macroExecItemValue_' + uniqueItemId +'"></div></div>' +
                                    '<input type="hidden" name="execDeviceDeviceId_' + uniqueItemId+'" id="execDeviceDeviceId_' + uniqueItemId+'" value="' + returnData.deviceId + '" />' +
                                    '<input type="hidden" name="execDeviceGroup_' + uniqueItemId+'" id="execDeviceGroup_' + uniqueItemId+'" value="' + returnData.deviceGroupId + '" />' +
                                    '<input type="hidden" name="execDeviceCommand_' + uniqueItemId+'" id="execDeviceCommand_' + uniqueItemId+'" value="' + returnData.deviceCommandId + '" />' +
                                    '<input type="hidden" name="execDeviceDeviceName_' + uniqueItemId+'" id="execDeviceDeviceName_' + uniqueItemId+'" value="' + returnData.deviceName + '" />' +
                                    '<input type="hidden" name="execDeviceCommandName_' + uniqueItemId+'" id="execDeviceCommandName_' + uniqueItemId+'" value="' + returnData.deviceCommandName + '" />' +
                                    '<input type="hidden" name="execDeviceCommandType_' + uniqueItemId+'" id="execDeviceCommandType_' + uniqueItemId+'" value="' + returnData.deviceCommandType + '" />' +
                                    '<input type="hidden" name="execDeviceCommandDataType_' + uniqueItemId+'" id="execDeviceCommandDataType_' + uniqueItemId+'" value="' + returnData.deviceCommandDataType + '" />' +
                                    '<input type="hidden" name="execDeviceCommandValue_' + uniqueItemId+'" id="execDeviceCommandValue_' + uniqueItemId+'" value="" />'+
                                    '<input type="hidden" name="execDeviceCommandExtra_' + uniqueItemId+'" id="execDeviceCommandExtra_' + uniqueItemId+'" value="" /></div>';
                    $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#macro_exec_list');
                    $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                        $('#DIV_macroExecItem_' + uniqueItemId).empty();
                    });
                    switch(returnData.deviceCommandType){
                        case "button":
                            $('#DIV_macroExecItemValue_' + uniqueItemId).html("Button pressed");
                            if(typeof deviceCommandValue!=="undefined" && deviceCommandValue!==""){
                                $('#execDeviceCommandValue_' + uniqueItemId).val(deviceCommandValue);
                            } else {
                                $('#execDeviceCommandValue_' + uniqueItemId).val(data.typedetails.deviceCommandValue);
                            }
                        break;
                        case "select":
                            $('#DIV_macroExecItemValue_' + uniqueItemId).replaceWith('<select class="form-control" id="DIV_macroExecItemValue_' + uniqueItemId+'"></select>');
                            var selectData = data.typedetails.commandset;
                            for(var i=0;i<selectData.length;i++){
                                $('#DIV_macroExecItemValue_' + uniqueItemId).append('<option value="'+selectData.value+'">'+selectData.label+'</option>');
                            }
                            $('#DIV_macroExecItemValue_' + uniqueItemId).off("change").on("change", function(event){
                                $('#execDeviceCommandValue_' + uniqueItemId).val($(this).val());
                            });
                        break;
                        case "toggle":
                            $('#DIV_macroExecItemValue_' + uniqueItemId).replaceWith('<select class="form-control" id="DIV_macroExecItemValue_' + uniqueItemId+'"></select>');
                            var offStuff = data.typedetails.commandset.off;
                            var onStuff = data.typedetails.commandset.on;
                            $('#DIV_macroExecItemValue_' + uniqueItemId).append(
                                    '<option value="true">'+onStuff.label+'</option>' +
                                    '<option value="false">'+offStuff.label+'</option>');
                            $('#DIV_macroExecItemValue_' + uniqueItemId).off("change").on("change", function(event){
                                $('#execDeviceCommandValue_' + uniqueItemId).val($(this).val()=="true");
                            });
                            if(typeof deviceCommandValue!=="undefined" && deviceCommandValue!==""){
                                $('#execDeviceCommandValue_' + uniqueItemId).val(deviceCommandValue);
                                $('#DIV_macroExecItemValue_' + uniqueItemId).val(deviceCommandValue);
                            } else {
                                $('#execDeviceCommandValue_' + uniqueItemId).val("true");
                            }
                        break;
                        case "colorpicker":
                            $('#DIV_macroExecItemValue_' + uniqueItemId).html('<div style="display:table-cell; vertical-align:middle; height: 40px;"><div id="DIV_macroExecItemValuePicker_' + uniqueItemId +'"></div><div style="float:left;display:table-cell; vertical-align:middle; height: 40px; margin-left: 5px;"><select class="form-control" id="DIV_macroExecItemValuePickerExecSelect_' + uniqueItemId +'"></select></div></div>');
                            var colorObject = {h:0,s:0,b:0};
                            if(deviceCommandValue!==""){
                                colorObject = deviceCommandValue;
                            }
                            var localHsl = tinycolor({h: colorObject.h*360, s: colorObject.s, v: colorObject.b}).toHsl();
                            var colorpickerdivModified = uniqueItemId.replace(/-/g,"_");
$('#DIV_macroExecItemValue_' + uniqueItemId).prepend('<div id="'+colorpickerdivModified+'_PopUp" style="display:none;" class="popupwindowshadow">\n\
<div>Select color</div>\n\
<div><div id="deviceColorPickerFor_'+colorpickerdivModified+'"></div></div>\n\
</div>\n\<script>\n\
$("#'+colorpickerdivModified+'_PopUp").jqxWindow({ width: 450,height: 250, theme: siteSettings.getTheme() });\n\
function openColorPickerPopup'+colorpickerdivModified+'(){\n\
$("#'+colorpickerdivModified+'_PopUp").jqxWindow("open");\n\
}\n\
<\/script>\n\
<div onclick="openColorPickerPopup'+colorpickerdivModified+'();" id="blockTypePreview_'+colorpickerdivModified+'" style="margin-top:5px;float:left;width:50px;height:30px;background: hsl('+localHsl.h+', '+localHsl.s*100+'%, '+localHsl.l*100+'%);cursor:pointer;"></div>');
                            switch (data.typedetails.mode) {
                                case 'hsb':
                                    $('#deviceColorPickerFor_'+colorpickerdivModified).ColorPickerSliders({
                                        flat: true,
                                        customswatches: false,
                                        swatches: ['red', 'yellow', 'lime', 'aqua', 'blue', 'magenta', 'white'],
                                        previewformat: 'hsl',
                                        order: {
                                            hsl: 1,
                                            preview: 2
                                        },
                                        labels: {
                                            rgbred: 'Red',
                                            rgbgreen: 'Green',
                                            rgbblue: 'Blue'
                                        },
                                        color: localHsl,
                                        onchange: function(container, color) {
                                            var newColor = color.tiny.toHsv();
                                            var updateHsl = color.tiny.toHsl();
                                            colorObject.h = newColor.h/360;
                                            colorObject.s = newColor.s;
                                            colorObject.b = newColor.v;
                                            try {
                                                $('#blockTypePreview_'+colorpickerdivModified).css({'background':'hsl('+updateHsl.h+', '+updateHsl.s*100+'%, '+updateHsl.l*100+'%'});
                                                $('#execDeviceCommandValue_' + uniqueItemId).val(JSON.stringify(colorObject));
                                                $('#execDeviceCommandExtra_' + uniqueItemId).val($('#DIV_macroExecItemValuePickerExecSelect_' + uniqueItemId).val());
                                            } catch (err) {}
                                        }
                                    });
                                break;
                                case "rgb":
                                    $('#deviceColorPickerFor_'+colorpickerdivModified).ColorPickerSliders({
                                        flat: true,
                                        customswatches: false,
                                        swatches: ['red', 'yellow', 'lime', 'aqua', 'blue', 'magenta', 'white'],
                                        previewformat: 'rgb',
                                        order: {
                                            rgb: 1,
                                            preview: 2
                                        },
                                        labels: {
                                            rgbred: 'Red',
                                            rgbgreen: 'Green',
                                            rgbblue: 'Blue'
                                        },
                                        color: localHsl,
                                        onchange: function(container, color) {
                                            var newColor = color.tiny.toHsv();
                                            var updateHsl = color.tiny.toHsl();
                                            colorObject.h = newColor.h/360;
                                            colorObject.s = newColor.s;
                                            colorObject.b = newColor.v;
                                            try {
                                                $('#blockTypePreview_'+colorpickerdivModified).css({'background':'hsl('+updateHsl.h+', '+updateHsl.s*100+'%, '+updateHsl.l*100+'%'});
                                                $('#execDeviceCommandValue_' + uniqueItemId).val(JSON.stringify(colorObject));
                                                $('#execDeviceCommandExtra_' + uniqueItemId).val($('#DIV_macroExecItemValuePickerExecSelect_' + uniqueItemId).val());
                                            } catch (err) {}
                                        }
                                    });
                                break;
                                case "cie":
                                    $('#deviceColorPickerFor_'+colorpickerdivModified).ColorPickerSliders({
                                        flat: true,
                                        customswatches: false,
                                        swatches: ['red', 'yellow', 'lime', 'aqua', 'blue', 'magenta', 'white'],
                                        previewformat: 'hsl',
                                        order: {
                                            cie: 1,
                                            preview: 2
                                        },
                                        labels: {
                                            rgbred: 'Red',
                                            rgbgreen: 'Green',
                                            rgbblue: 'Blue'
                                        },
                                        color: localHsl,
                                        onchange: function(container, color) {
                                            var newColor = color.tiny.toHsv();
                                            var updateHsl = color.tiny.toHsl();
                                            colorObject.h = newColor.h/360;
                                            colorObject.s = newColor.s;
                                            colorObject.b = newColor.v;
                                            try {
                                                $('#blockTypePreview_'+colorpickerdivModified).css({'background':'hsl('+updateHsl.h+', '+updateHsl.s*100+'%, '+updateHsl.l*100+'%'});
                                                $('#execDeviceCommandValue_' + uniqueItemId).val(JSON.stringify(colorObject));
                                                $('#execDeviceCommandExtra_' + uniqueItemId).val($('#DIV_macroExecItemValuePickerExecSelect_' + uniqueItemId).val());
                                            } catch (err) {}
                                        }
                                    });
                                break;
                            }
                            for(var i=0;i<data.typedetails.commandset.length;i++){
                                $('#DIV_macroExecItemValuePickerExecSelect_' + uniqueItemId).append('<option value="'+data.typedetails.commandset[i].value+'">'+data.typedetails.commandset[i].label+'</option>');
                            }
                            $('#DIV_macroExecItemValuePickerExecSelect_' + uniqueItemId).on("change", function(){
                                $('#execDeviceCommandExtra_' + uniqueItemId).val($(this).val());
                            });
                            if(returnData.deviceCommandExtra!==undefined && returnData.deviceCommandExtra!==""){
                                $('#execDeviceCommandExtra_' + uniqueItemId).val(returnData.deviceCommandExtra);
                            } else {
                                $('#execDeviceCommandExtra_' + uniqueItemId).val($('#DIV_macroExecItemValuePickerExecSelect_' + uniqueItemId).val());
                            }
                        break;
                        case "slider":
                            var setValue = ((deviceCommandValue==="")?data.typedetails.max/2:Number(deviceCommandValue));
                            $('#DIV_macroExecItemValue_' + uniqueItemId).html('<input style="float:left;" id="DIV_macroExecItemSlider_' + uniqueItemId +'" type="text" data-slider-min="'+data.typedetails.min+'" data-slider-max="'+data.typedetails.max+'" data-slider-step="1" data-slider-value="'+setValue+'"/>');
                            $('#DIV_macroExecItemSlider_' + uniqueItemId).bootstrapSlider({
                                    formatter: function(value) {
                                            return 'Value: ' + value;
                                    },
                                    tooltip: 'show',
                                    precision: ((data.typedetails.datatype==="integer")?0:2)
                            }).on("slideStop", function(val){
                                $('#execDeviceCommandValue_' + uniqueItemId).val( val.value );
                            });
                            $('#execDeviceCommandValue_' + uniqueItemId).val( setValue );
                        break;
                    }
                });
            break;
            case "userstatus":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '" class="btn btn-danger">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Set user status to</span>' +
                                '<div style="display:table-cell; vertical-align:middle"><select class="form-control" id="execUserstatusId_' + uniqueItemId+'"></select></div></div>';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#macro_exec_list');
                getHttpJsonRPC('{"jsonrpc": "2.0", "method": "UserStatusService.getUserStatuses","id":"UserStatusService.getUserStatuses"}', function(data){
                    for(var i=0;i<data.length;i++){
                        $('#execUserstatusId_' + uniqueItemId).append('<option value="'+data[i].id+'">'+data[i].name+'</option>');
                    }
                    if(typeof returnData.userstatusid !=="undefined" && returnData.userstatusid!==""){
                        $('#execUserstatusId_' + uniqueItemId).val(returnData.userstatusid);
                    }
                });  
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    $('#DIV_macroExecItem_' + uniqueItemId).empty();
                });
            break;
            case "presence":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '" class="btn btn-danger">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Set global presence to</span>' +
                                '<div style="display:table-cell; vertical-align:middle"><select class="form-control" id="execPresenceId_' + uniqueItemId+'"></select></div></div>';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#macro_exec_list');
                getHttpJsonRPC('{"jsonrpc": "2.0", "method": "PresenceService.getPresences", "id":"PresenceService.getPresences"}', function(data){
                    for(var i=0;i<data.length;i++){
                        $('#execPresenceId_' + uniqueItemId).append('<option value="'+data[i].id+'">'+data[i].name+'</option>');
                    }
                    if(typeof returnData.presenceid !=="undefined" && returnData.presenceid!==""){
                        $('#execPresenceId_' + uniqueItemId).val(returnData.presenceid);
                    }
                });  
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    $('#DIV_macroExecItem_' + uniqueItemId).empty();
                });
            break;
            case "daypart":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '" class="btn btn-danger">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Set part of day to</span>' +
                                '<div style="display:table-cell; vertical-align:middle"><select class="form-control" id="execDaypartId_' + uniqueItemId+'"></select></div></div>';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#macro_exec_list');
                getHttpJsonRPC('{"jsonrpc": "2.0", "method": "DayPartService.getDayParts", "id":"DayPartService.getDayParts"}', function(data){
                    for(var i=0;i<data.length;i++){
                        $('#execDaypartId_' + uniqueItemId).append('<option value="'+data[i].id+'">'+data[i].name+'</option>');
                    }
                    if(typeof returnData.daypartid !=="undefined" && returnData.daypartid!==""){
                        $('#execDaypartId_' + uniqueItemId).val(returnData.daypartid);
                    }
                });  
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    $('#DIV_macroExecItem_' + uniqueItemId).empty();
                });
            break;
            case "macro":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '" class="btn btn-danger">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Run macro <strong>' + returnData.macroName + '</strong></span></div>' +
                                '<input type="hidden" name="execMacroId_' + uniqueItemId+'" id="execMacroId_' + uniqueItemId+'" value="' + returnData.macroId + '" />' +
                                '<input type="hidden" name="execMacroName_' + uniqueItemId+'" id="execMacroName_' + uniqueItemId+'" value="' + returnData.macroName + '" />';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#macro_exec_list');
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    $('#DIV_macroExecItem_' + uniqueItemId).empty();
                });
            break;
            case "remoteplugin":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '" class="btn btn-danger">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Press a button on <strong>' + returnData.remoteName + '</strong></span>'+
                                    '<div style="display:table-cell; vertical-align:middle"><div id="execRemoteButtonId_' + uniqueItemId+'"></div>'+
                                 '</div>' +
                                 
                                '<input type="hidden" name="execRemoteId_' + uniqueItemId+'" id="execRemoteId_' + uniqueItemId+'" value="' + returnData.remoteId + '" />' +
                                '<input type="hidden" name="execRemoteName_' + uniqueItemId+'" id="execRemoteName_' + uniqueItemId+'" value="' + returnData.remoteName + '" />';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#macro_exec_list');
                $.get('/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "RemotesService.getRemoteButtons", "params":{"id":' + returnData.remoteId + '},"id":"RemotesService.getRemoteButtons"}')
                    .done(function(data) {
                        var dataSet = [];
                        var remoteData = data.result.data.remotevisuals.sections;
                        for(var i=0; i< remoteData.length; i++){
                            var section = remoteData[i].section;
                            for(var j = 0; j < section.rows.length;j++){
                                var row = section.rows[j].row;
                                for(var k=0; k < row.buttons.length; k++){
                                    var button = row.buttons[k];
                                    var buttonname = "";
                                    var buttonId = "";
                                    var color = "";
                                    if(button.type!=="btn_none"){
                                        buttonId = button.id;
                                        if(button.type==="btn_col"){
                                            buttonname += '<div style="height: 20px;"><span style="height: 20px; line-height:20px; vertical-allign:middle;">' + section.name + ' - </span><span style="height: 16px; width:16px; background-color: '+button.color+';"></span></div>';
                                            color = button.color;
                                        } else if(button.type==="btn_def"){
                                            buttonname += section.name + " - " + button.label;
                                        } else {
                                            buttonname += section.name + " - " + button.sdesc;
                                        }
                                    }
                                    if(buttonId!==""){
                                        dataSet.push({"id"   :buttonId,
                                                      "label":buttonname,
                                                      "type" :button.type,
                                                      "color":color});
                                    }
                                }
                            }
                        }
                        var remoteButtonsList = {
                            datatype: "json",
                            datafields: [
                                {name: 'id', type: 'string', map: 'id' },
                                {name: 'label', type: 'string', map: 'label' },
                                {name: 'type', type: 'string', map: 'type' },
                                {name: 'color', type: 'string', map: 'color' }
                            ],
                            localdata: dataSet
                        };
                        var allRemoteButtonsList = new $.jqx.dataAdapter(remoteButtonsList);
                        $('#execRemoteButtonId_' + uniqueItemId).jqxDropDownList({source: allRemoteButtonsList, valueMember: "id", displayMember: "label", width: '300', theme: siteSettings.getTheme()});
                        try {
                            $('#execRemoteButtonId_' + uniqueItemId).jqxDropDownList('selectIndex', $('#execRemoteButtonId_' + uniqueItemId).jqxDropDownList('getItemByValue', returnData.remoteButtonId).index );
                        } catch(err){}
                    }
                );
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    clearInternalWidgetHandlers('#DIV_macroExecItem_' + uniqueItemId);
                    $('#DIV_macroExecItem_' + uniqueItemId).empty();
                });
            break;
            case "mediaplugin":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '" class="btn btn-danger">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Run a <strong>'+returnData.mediaCommandTypeName+'</strong> on <strong>' + returnData.mediaName + '</strong> which is:</span><div style="display:table-cell; vertical-align:middle"><div id="execMediaCommandId_' + uniqueItemId+'"></div></div>' +
                                '<input type="hidden" name="execMediaId_' + uniqueItemId+'" id="execMediaId_' + uniqueItemId+'" value="' + returnData.mediaId + '" />' +
                                '<input type="hidden" name="execMediaName_' + uniqueItemId+'" id="execMediaName_' + uniqueItemId+'" value="' + returnData.mediaName + '" />' +
                                '<input type="hidden" name="execMediaCommandTypeId_' + uniqueItemId+'" id="execMediaCommandTypeId_' + uniqueItemId+'" value="' + returnData.mediaCommandTypeId + '" />' +
                                '<input type="hidden" name="execMediaCommandTypeName_' + uniqueItemId+'" id="execMediaCommandTypeName_' + uniqueItemId+'" value="' + returnData.mediaCommandTypeName + '" />';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#macro_exec_list');
                var mediaCommandSetList = {
                    datatype: "json",
                    datafields: [
                        {name: 'id', type: 'string'},
                        {name: 'name', type: 'string'}
                    ],
                    url: '/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "MediaService.getCapabilities","params":{"id":'+returnData.mediaId+', "type":"'+returnData.mediaCommandTypeId+'"}, "id":"MediaService.getCapabilities"}',
                    root: "result>data"
                };
                var DataMediaCommandSetList = new $.jqx.dataAdapter(mediaCommandSetList);
                $('#execMediaCommandId_' + uniqueItemId).jqxDropDownList({source: DataMediaCommandSetList, valueMember: "id", displayMember: "name", width: '100', theme: siteSettings.getTheme()});
                $('#execMediaCommandId_' + uniqueItemId).on('bindingComplete', function (event) { 
                    try {
                        $('#execMediaCommandId_' + uniqueItemId).jqxDropDownList('selectIndex', $('#execMediaCommandId_' + uniqueItemId).jqxDropDownList('getItemByValue', returnData.mediaCommandId).index );
                    } catch(err){}
                    $('#execMediaCommandId_' + uniqueItemId).off('bindingComplete');
                });
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    clearInternalWidgetHandlers('#DIV_macroExecItem_' + uniqueItemId);
                    $('#DIV_macroExecItem_' + uniqueItemId).empty();
                });
            break;
        }
    }

//    $("#save_macr").on('click', function() {
    $('#macroform').bootstrap3Validate(function(e, formData) {
        e.preventDefault();
        var execCollection = new Array();
        var execCounter = 0;
        var inError = false;
        if(inputFieldValid($('#macro_name')) && inputFieldValid($('#macro_description'))){
            $('#macro_exec_list>').each(function() {
                if ($(this).attr("id") !== undefined && $(this).attr("id").startsWith('DIV_macroExecItem_')) {
                    var workWith = $(this).attr("id").split("_")[2];
                    if ($('#execDeviceDeviceId_' + workWith).length!==0) {
                        var item = {
                            "itemtype"   : "device",
                            "deviceid"   : parseInt($("#execDeviceDeviceId_" + workWith).val()),
                            "group"      : $("#execDeviceGroup_" + workWith).val(),
                            "command"    : $("#execDeviceCommand_" + workWith).val(),
                            "devicename" : $("#execDeviceDeviceName_" + workWith).val(),
                            "commandname": $("#execDeviceCommandName_" + workWith).val(),
                            "commandtype": $("#execDeviceCommandType_" + workWith).val(),
                            "value"      : ($("#execDeviceCommandDataType_" + workWith).val()=="string")?$("#execDeviceCommandValue_" + workWith).val():JSON.parse($("#execDeviceCommandValue_" + workWith).val()),
                            "extra"      : $("#execDeviceCommandExtra_" + workWith).val(),
                            "datatype"   : $("#execDeviceCommandDataType_" + workWith).val()
                        };
                        execCollection.push(item);
                        execCounter++;
                    } else if ($('#execMacroId_' + workWith).length!==0) {
                        var item = {
                            "itemtype": "macro",
                            "macroid": parseInt($("#execMacroId_" + workWith).val()),
                            "macroname": $("#execMacroName_" + workWith).val()
                        };
                        execCollection.push(item);
                        execCounter++;
                    } else if ($('#execPresenceId_' + workWith).length!==0) {
                        var item = {
                            "itemtype"  : "presence",
                            "presenceid": parseInt($("#execPresenceId_" + workWith).val()),
                        };
                        execCollection.push(item);
                        execCounter++;
                    } else if ($('#execDaypartId_' + workWith).length!==0) {
                        var item = {
                            "itemtype" : "daypart",
                            "daypartid": parseInt($("#execDaypartId_" + workWith).val()),
                        };
                        execCollection.push(item);
                        execCounter++;
                    } else if ($('#execUserstatusId_' + workWith).length!==0) {
                        var item = {
                            "itemtype" : "userstatus",
                            "userstatusid": parseInt($("#execUserstatusId_" + workWith).val())
                        };
                        execCollection.push(item);
                        execCounter++;
                    } else if ($('#execMediaId_' + workWith).length!==0) {
                        var item = {
                            "itemtype"            : "mediaplugin",
                            "mediaId"             : parseInt($("#execMediaId_" + workWith).val()),
                            "mediaName"           : $("#execMediaName_" + workWith).val(),
                            "mediaCommandTypeId"  : $("#execMediaCommandTypeId_" + workWith).val(),
                            "mediaCommandTypeName": $("#execMediaCommandTypeName_" + workWith).val(),
                            "mediaCommandId"      : $("#execMediaCommandId_" + workWith).val()
                        };
                        execCollection.push(item);
                        execCounter++;
                    } else if ($('#execRemoteId_' + workWith).length!==0) {
                        var item = {
                            "itemtype"            : "remoteplugin",
                            "remoteId"            : parseInt($("#execRemoteId_" + workWith).val()),
                            "remoteName"          : $("#execRemoteName_" + workWith).val(),
                            "remoteButtonId"      : $("#execRemoteButtonId_" + workWith).val()
                        };
                        execCollection.push(item);
                        execCounter++;
                    }
                }
            });
            if(inError===false && execCounter===0){
                extendedPageError("Macro error", "Please use a minimum of one execution item");
            } else if(inError===true) {
                /// do nothing and keep message
            } else {
                var macroId = $("#macro_id").val();
                if(macroId!==undefined && macroId!==0 && macroId!==""){
                    var params = { "id"          : parseInt(macroId),
                                   "name"        : $('#macro_name').val(), 
                                   "description" : $('#macro_description').val(),
                                   "favorite"    : $('#macro_favorite').is(':checked'),
                                   "executions"  : execCollection
                    };
                    var rpcCommand = { "jsonrpc"     : "2.0",
                                       "method"      : "MacroService.updateMacro",
                                       "params"      : params,
                                       "id"          : "MacroService.updateMacro"};
                } else {
                    var params = { "name"        : $('#macro_name').val(), 
                                   "description" : $('#macro_description').val(),
                                   "favorite"    : $('#macro_favorite').is(':checked'),
                                   "executions"  : execCollection
                    };
                    var rpcCommand = { "jsonrpc"     : "2.0",
                                       "method"      : "MacroService.saveMacro",
                                       "params"      : params,
                                       "id"          : "MacroService.saveMacro"};
                }
                var postField = {};
                postField["rpc"] = JSON.stringify(rpcCommand);
                postHttpJsonRPC(JSON.stringify(rpcCommand), function(data){
                    quickMessage("success", "Macro has been saved");
                    refreshPageContent('/macros.html');
                });
            }
        } else {
            extendedPageError("Macro save error", "Make sure you use a correct macro name, description and some executions.");
        }
    });

    $("#add_exec").on('click', function() {
        itemSelectionModal.setCallBack(function(returnData){
            createExecItems(returnData);
        });
        itemSelectionModal.open();
    });
    
    $("#clear_exec").on('click', function() {
        clearInternalWidgetHandlers("#macro_exec_list");
        $("#macro_exec_list").empty();
    });
    
    var macroId = $("#macro_id").val();
    if(typeof macroId!=="undefined" && macroId!==0 && macroId!==""){
        getHttpJsonRPC('{"jsonrpc": "2.0", "method": "MacroService.getMacro", "params":{"id":' + macroId + '},"id":"MacroService.getMacro"}',function(macroData) {
            $("#macro_name").val(macroData.name);
            $("#macro_description").val(macroData.description);
            $('#macro_favorite').prop('checked', (macroData.favorite==true));
            $("#macro_id").val(macroData.id);
            if(macroData.executions!==undefined){
                for(var i=0;i<macroData.executions.length;i++){
                    createExecItemsByMapping(macroData.executions[i]);
                }
            } else {
                extendedPageError("Macro error", "Could not load macro correctly, make sure it exists");
            }
        });
    }