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

$(document).ready(function() {

    var itemSelectionModal = new ItemSelectionModal();
    
    var presenceOptionsList = {
        datatype: "json",
        datafields: [
            {name: 'id', type: 'int'},
            {name: 'name', type: 'string'}
        ],
        url: '/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "PresenceService.getPresences", "id":"PresenceService.getPresences"}',
        root: "result>data"
    };
    var allPresenceOptionsList = new $.jqx.dataAdapter(presenceOptionsList);
    
    var daypartsOptionsList = {
        datatype: "json",
        datafields: [
            {name: 'id', type: 'int'},
            {name: 'name', type: 'string'}
        ],
        url: '/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "DayPartService.getDayParts", "id":"DayPartService.getDayParts"}',
        root: "result>data"
    };
    var allDaypartsOptionsList = new $.jqx.dataAdapter(daypartsOptionsList);
    
    var userStatusesOptionsList = {
        datatype: "json",
        datafields: [
            {name: 'id', type: 'int'},
            {name: 'name', type: 'string'}
        ],
        url: '/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "UserStatusService.getUserStatuses","id":"UserStatusService.getUserStatuses"}',
        root: "result>data"
    };
    var dataUserStatusesOptionsList = new $.jqx.dataAdapter(userStatusesOptionsList);
    
    function createExecItemsByMapping(data){
        var newData;
        try {
            switch(data.itemtype){
                case "device":
                    newData = {
                        "itemType"          : "device",
                        "deviceName"        : data.devicename,
                        "deviceCommandName" : data.commandname,
                        "deviceCommandType" : data.commandtype,
                        "deviceId"          : data.deviceid,
                        "deviceGroupId"     : data.group,
                        "deviceCommandId"   : data.command,
                        "deviceCommandValue": JSON.stringify(data.value),
                        "deviceCommandExtra": data.extra
                    };
                    createExecItems(newData);
                break;
                case "scene":
                    newData = {
                        "itemType"  : "scene",
                        "sceneId"   : data.sceneid,
                        "sceneName" : data.scenename
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
                        "presenceid" : data.daypartid
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
            //// invalid data
        }
    }
    
    function createExecItems(returnData){
        var deviceCommandValue;
        if(returnData.deviceCommandValue===undefined){
            deviceCommandValue = "";
        } else {
            deviceCommandValue = returnData.deviceCommandValue;
        }
        var uniqueItemId = createUUID();
        var deviceDivId = 'DIV_sceneExecItem_' + uniqueItemId;
        switch(returnData.itemType){
            case "device":
                var deviceHtml = '<div style="display:table;" id="divTableId'+uniqueItemId+'"><button id="deleteExecItem_' + uniqueItemId + '">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Set <strong>' + returnData.deviceCommandName + '</strong> of <strong>' + returnData.deviceName + '</strong> to:&nbsp; </span><div style="display:table-cell; vertical-align:middle"><div id="DIV_sceneExecItemValue_' + uniqueItemId +'"></div></div>' +
                                '<input type="hidden" name="execDeviceDeviceId_' + uniqueItemId+'" id="execDeviceDeviceId_' + uniqueItemId+'" value="' + returnData.deviceId + '" />' +
                                '<input type="hidden" name="execDeviceGroup_' + uniqueItemId+'" id="execDeviceGroup_' + uniqueItemId+'" value="' + returnData.deviceGroupId + '" />' +
                                '<input type="hidden" name="execDeviceCommand_' + uniqueItemId+'" id="execDeviceCommand_' + uniqueItemId+'" value="' + returnData.deviceCommandId + '" />' +
                                '<input type="hidden" name="execDeviceDeviceName_' + uniqueItemId+'" id="execDeviceDeviceName_' + uniqueItemId+'" value="' + returnData.deviceName + '" />' +
                                '<input type="hidden" name="execDeviceCommandName_' + uniqueItemId+'" id="execDeviceCommandName_' + uniqueItemId+'" value="' + returnData.deviceCommandName + '" />' +
                                '<input type="hidden" name="execDeviceCommandType_' + uniqueItemId+'" id="execDeviceCommandType_' + uniqueItemId+'" value="' + returnData.deviceCommandType + '" />' +
                                '<input type="hidden" name="execDeviceCommandValue_' + uniqueItemId+'" id="execDeviceCommandValue_' + uniqueItemId+'" value="" />'+
                                '<input type="hidden" name="execDeviceCommandExtra_' + uniqueItemId+'" id="execDeviceCommandExtra_' + uniqueItemId+'" value="" /></div>';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#scene_exec_list');
                $('#deleteExecItem_' + uniqueItemId).jqxButton({ width: '50', theme: siteSettings.getTheme()});
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    clearInternalWidgetHandlers('#DIV_sceneExecItem_' + uniqueItemId);
                    $('#DIV_sceneExecItem_' + uniqueItemId).empty();
                });
                switch(returnData.deviceCommandType){
                    case "button":
                        $.get('/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "DeviceService.getDeviceCommand", "params":{"id":' + returnData.deviceId + ', "groupid":"' + returnData.deviceGroupId + '", "commandid":"' + returnData.deviceCommandId + '"},"id":"DeviceService.getDeviceCommand"}')
                            .done(function(data) {
                                try {
                                    if(data.result.success !== true){
                                        showErrorMessage("Command details", data.result.message);
                                    } else {
                                        $('#DIV_sceneExecItemValue_' + uniqueItemId).html("Selected");
                                        $('#execDeviceCommandValue_' + uniqueItemId).val(data.result.data.typedetails.deviceCommandValue);
                                    }
                                } catch(err){
                                    var message = "<strong>Message</strong>:<br/>";
                                    if(data.error.data.trace!==undefined){
                                        message += data.error.data.message + "<br/><br/><strong>Trace</strong>:<br/>" + data.error.data.trace;
                                    } else {
                                        message += data.error.data.message;
                                    }
                                    showErrorMessage("Server error: " + data.error.message, message);
                                }
                            }, "json");
                    break;
                    case "select":
                        var deviceCommandSetActionList = {
                            datatype: "json",
                            datafields: [
                                {name: 'value', type: 'string', map: 'value' },
                                {name: 'label', type: 'string', map: 'label' }
                            ],
                            url: '/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "DeviceService.getDeviceCommand", "params":{"id":' + returnData.deviceId + ', "groupid":"' + returnData.deviceGroupId + '", "commandid":"' + returnData.deviceCommandId + '"},"id":"DeviceService.getDeviceCommand"}',
                            root: 'result>data>typedetails>commandset'
                        };
                        var dataDeviceCommandSetActionList = new $.jqx.dataAdapter(deviceCommandSetActionList);
                        $('#DIV_sceneExecItemValue_' + uniqueItemId).jqxDropDownList({source: dataDeviceCommandSetActionList, selectedIndex: 0, valueMember: "value", displayMember: "label", width: '150', theme: siteSettings.getTheme()});
                        $('#DIV_sceneExecItemValue_' + uniqueItemId).on("select", function(event){
                            var args = event.args;
                            if(args){
                                var item = args.item;
                                $('#execDeviceCommandValue_' + uniqueItemId).val(item.value);
                            }
                        });
                        $('#DIV_sceneExecItemValue_' + uniqueItemId).on('bindingComplete', function (event) { 
                            if(deviceCommandValue!==""){
                                $('#DIV_sceneExecItemValue_' + uniqueItemId).jqxDropDownList('selectIndex', $('#DIV_sceneExecItemValue_' + uniqueItemId).jqxDropDownList('getItemByValue', deviceCommandValue).index );
                                $('#execDeviceCommandValue_' + uniqueItemId).val(deviceCommandValue);
                            } else {
                                try {
                                    $('#execDeviceCommandValue_' + uniqueItemId).val($('#DIV_sceneExecItemValue_' + uniqueItemId).jqxDropDownList('getSelectedItem').value);
                                } catch (err){}
                            }
                            $('#DIV_sceneExecItemValue_' + uniqueItemId).off('bindingComplete');
                        });
                    break;
                    case "toggle":
                        $.get('/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "DeviceService.getDeviceCommand", "params":{"id":' + returnData.deviceId + ', "groupid":"' + returnData.deviceGroupId + '", "commandid":"' + returnData.deviceCommandId + '"},"id":"DeviceService.getDeviceCommand"}')
                            .done(function(data) {
                                try {
                                    if(data.result.success !== true){
                                        showErrorMessage("Command details", data.result.message);
                                    } else {
                                        var selectSet = [{'name':'On', 'id':'true'},{'name':'Off', 'id':'false'}];
                                        var deviceCommandSetActionList = {
                                            datatype: "json",
                                            datafields: [
                                                {name: 'id', type: 'string', map: 'id' },
                                                {name: 'label', type: 'string', map: 'name' }
                                            ],
                                            localdata: selectSet
                                        };
                                        var dataDeviceCommandSetActionList = new $.jqx.dataAdapter(deviceCommandSetActionList);
                                        $('#DIV_sceneExecItemValue_' + uniqueItemId).jqxDropDownList({source: dataDeviceCommandSetActionList, selectedIndex: 0, valueMember: "id", displayMember: "label", width: '50', theme: siteSettings.getTheme()});
                                        if(deviceCommandValue!==""){
                                            $('#DIV_sceneExecItemValue_' + uniqueItemId).jqxDropDownList('selectIndex', (deviceCommandValue=="true")?0:1);
                                            $('#execDeviceCommandValue_' + uniqueItemId).val(deviceCommandValue);
                                        } else {
                                            try {
                                                $('#DIV_sceneExecItemValue_' + uniqueItemId).jqxDropDownList('selectIndex', 0);
                                                $('#execDeviceCommandValue_' + uniqueItemId).val("false");
                                            } catch (err){}
                                        }
                                        $('#DIV_sceneExecItemValue_' + uniqueItemId).on("select", function(event){
                                            var args = event.args;
                                            if(args){
                                                if($('#DIV_sceneExecItemValue_' + uniqueItemId).jqxDropDownList('getSelectedIndex')==0){
                                                    $('#execDeviceCommandValue_' + uniqueItemId).val("true");
                                                } else {
                                                    $('#execDeviceCommandValue_' + uniqueItemId).val("false");
                                                }
                                            }
                                        });
                                    }
                                } catch(err){
                                    var message = "<strong>Message</strong>:<br/>";
                                    if(data.error.data.trace!==undefined){
                                        message += data.error.data.message + "<br/><br/><strong>Trace</strong>:<br/>" + data.error.data.trace;
                                    } else {
                                        message += data.error.data.message;
                                    }
                                    showErrorMessage("Server error: " + data.error.message, message);
                                }
                            }, "json");
                    break;
                    case "colorpicker":
                        $.get('/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "DeviceService.getDeviceCommand", "params":{"id":' + returnData.deviceId + ', "groupid":"' + returnData.deviceGroupId + '", "commandid":"' + returnData.deviceCommandId + '"},"id":"DeviceService.getDeviceCommand"}')
                            .done(function(data) {
                                try {
                                    if(data.result.success !== true){
                                        showErrorMessage("Command details", data.result.message);
                                    } else {

                                        $('#DIV_sceneExecItemValue_' + uniqueItemId).html('<div style="float:left;" id="DIV_sceneExecItemValuePickerDropDown_' + uniqueItemId +'"><div style="padding: 3px;"><div id="DIV_sceneExecItemValuePicker_' + uniqueItemId +'"></div></div></div><div style="float:left;" id="DIV_sceneExecItemValuePickerExecSelect_' + uniqueItemId +'"></div>');
                                        var colorObject = {h:0,s:0,b:0};
                                        if(deviceCommandValue!==""){
                                            colorObject = JSON.parse(deviceCommandValue);
                                        }
                                        var localHsl = tinycolor({h: colorObject.h*360, s: colorObject.s, v: colorObject.b}).toHsl();
                                        var colorpickerdivModified = uniqueItemId.replace(/-/g,"_");
$('#DIV_sceneExecItemValue_' + uniqueItemId).prepend('<div id="'+colorpickerdivModified+'_PopUp" style="display:none;" class="popupwindowshadow">\n\
    <div>Select color</div>\n\
    <div><div id="deviceColorPickerFor_'+colorpickerdivModified+'"></div></div>\n\
</div>\n\<script>\n\
$("#'+colorpickerdivModified+'_PopUp").jqxWindow({ width: 450,height: 250, theme: siteSettings.getTheme() });\n\
function openColorPickerPopup'+colorpickerdivModified+'(){\n\
        $("#'+colorpickerdivModified+'_PopUp").jqxWindow("open");\n\
}\n\
<\/script>\n\
<div onclick="openColorPickerPopup'+colorpickerdivModified+'();" id="blockTypePreview_'+colorpickerdivModified+'" style="float:left;width:50px;height:25px;background: hsl('+localHsl.h+', '+localHsl.s*100+'%, '+localHsl.l*100+'%);cursor:pointer;"></div>');
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
                                                    $('#execDeviceCommandExtra_' + uniqueItemId).val($('#DIV_sceneExecItemValuePickerExecSelect_' + uniqueItemId).jqxDropDownList('getItem', $('#DIV_sceneExecItemValuePickerExecSelect_' + uniqueItemId).jqxDropDownList('getSelectedIndex')).value);
                                                } catch (err) {}
                                            }
                                        });

                                        var deviceCommandSetActionList = {
                                            datatype: "json",
                                            datafields: [
                                                {name: 'id', type: 'string', map: 'value' },
                                                {name: 'label', type: 'string', map: 'label' }
                                            ],
                                            localdata: data.result.data.typedetails.commandset,
                                        };
                                        var dataDeviceCommandSetActionList = new $.jqx.dataAdapter(deviceCommandSetActionList);
                                        $('#DIV_sceneExecItemValuePickerExecSelect_' + uniqueItemId).jqxDropDownList({source: dataDeviceCommandSetActionList, valueMember: "id", displayMember: "label", width: '150', theme: siteSettings.getTheme()});
                                        $('#DIV_sceneExecItemValuePickerExecSelect_' + uniqueItemId).on("select", function(){
                                            $('#execDeviceCommandExtra_' + uniqueItemId).val($('#DIV_sceneExecItemValuePickerExecSelect_' + uniqueItemId).jqxDropDownList('getItem', $('#DIV_sceneExecItemValuePickerExecSelect_' + uniqueItemId).jqxDropDownList('getSelectedIndex')).value);
                                        });
                                        if(returnData.deviceCommandExtra!==undefined && returnData.deviceCommandExtra!==""){
                                            try {
                                                $('#DIV_sceneExecItemValuePickerExecSelect_' + uniqueItemId).jqxDropDownList('selectIndex', $('#DIV_sceneExecItemValuePickerExecSelect_' + uniqueItemId).jqxDropDownList('getItemByValue', returnData.deviceCommandExtra).index);
                                            } catch(err){
                                                /// why the h*ll is this function often failing while there is data present :-/.
                                            }
                                            $('#execDeviceCommandExtra_' + uniqueItemId).val(returnData.deviceCommandExtra);
                                        } else {
                                            $('#DIV_sceneExecItemValuePickerExecSelect_' + uniqueItemId).jqxDropDownList('selectIndex', 0);
                                            $('#execDeviceCommandExtra_' + uniqueItemId).val(dataDeviceCommandSetActionList.records[0].id);
                                        }
                                    }
                                } catch(err){
                                    var error = err;
                                    showErrorMessage("Error: ", error.message + ((error.lineNumber!==undefined)?" at line: " + error.lineNumber:""));
                                }
                            }, "json");
                    break;
                    case "slider":
                        $.get('/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "DeviceService.getDeviceCommand", "params":{"id":' + returnData.deviceId + ', "groupid":"' + returnData.deviceGroupId + '", "commandid":"' + returnData.deviceCommandId + '"},"id":"DeviceService.getDeviceCommand"}')
                            .done(function(data) {
                                try {
                                    if(data.result.success !== true){
                                        showErrorMessage("Command details", data.result.message);
                                    } else {
                                        $('#DIV_sceneExecItemValue_' + uniqueItemId).html('<div style="float:left;" id="DIV_sceneExecItemSlider_' + uniqueItemId +'"></div><div style="float:left; border: 0; color: #f6931f; font-weight: bold; background-color: transparent; cursor:pointer; margin-top: 4px; margin-left: 10px;" id="DIV_sceneExecItemSliderValue_' + uniqueItemId +'"></div>');
                                        $( '#DIV_sceneExecItemSlider_' + uniqueItemId).jqxSlider({ showButtons: false, height: 30, min: parseInt(data.result.data.typedetails.min), max: parseInt(data.result.data.typedetails.max), step: 1, ticksFrequency: 10, mode: 'fixed', width: 200, theme: siteSettings.getTheme() });
                                        $( '#DIV_sceneExecItemSlider_' + uniqueItemId).on('change', function (event) { 
                                            $('#DIV_sceneExecItemSliderValue_' + uniqueItemId).html(event.args.value);
                                        });
                                        $( '#DIV_sceneExecItemSlider_' + uniqueItemId).on('slideEnd', function (event) { 
                                            $('#execDeviceCommandValue_' + uniqueItemId).val( event.args.value );
                                            $('#DIV_sceneExecItemSliderValue_' + uniqueItemId).html(event.args.value);
                                        });
                                        if(deviceCommandValue!==""){
                                            $('#DIV_sceneExecItemSliderValue_' + uniqueItemId).html(deviceCommandValue);
                                            $('#DIV_sceneExecItemSlider_' + uniqueItemId).jqxSlider('setValue', parseInt(deviceCommandValue));
                                            $('#execDeviceCommandValue_' + uniqueItemId).val( deviceCommandValue );
                                        }
                                    }
                                } catch(err){
                                    error = err;
                                    showErrorMessage("Error: ", error.message + ((error.lineNumber!==undefined)?" at line: " + error.lineNumber:""));
                                }
                            }, "json");
                    break;
                }
            break;
            case "userstatus":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Set user status to</span>' +
                                '<div style="display:table-cell; vertical-align:middle"><div id="execUserstatusId_' + uniqueItemId+'"></div></div></div>';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#scene_exec_list');
                $('#execUserstatusId_' + uniqueItemId).jqxDropDownList({source: dataUserStatusesOptionsList, valueMember: "id", displayMember: "name", width: '100', theme: siteSettings.getTheme()});
                $('#execUserstatusId_' + uniqueItemId).on('bindingComplete', function (event) { 
                    try {
                        $('#execUserstatusId_' + uniqueItemId).jqxDropDownList('selectIndex', $('#execUserstatusId_' + uniqueItemId).jqxDropDownList('getItemByValue', returnData.userstatusid).index );
                    } catch(err){}
                    $('#execUserstatusId_' + uniqueItemId).off('bindingComplete');
                });
                $('#deleteExecItem_' + uniqueItemId).jqxButton({ width: '50', theme: siteSettings.getTheme()});
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    clearInternalWidgetHandlers('#DIV_triggerExecItem_' + uniqueItemId);
                    $('#DIV_sceneExecItem_' + uniqueItemId).empty();
                });
            break;
            case "presence":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Set presence to</span>' +
                                '<div style="display:table-cell; vertical-align:middle"><div id="execPresenceId_' + uniqueItemId+'"></div></div></div>';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#scene_exec_list');
                $('#execPresenceId_' + uniqueItemId).jqxDropDownList({source: allPresenceOptionsList, valueMember: "id", displayMember: "name", width: '100', theme: siteSettings.getTheme()});
                $('#execPresenceId_' + uniqueItemId).on('bindingComplete', function (event) { 
                    try {
                        $('#execPresenceId_' + uniqueItemId).jqxDropDownList('selectIndex', $('#execPresenceId_' + uniqueItemId).jqxDropDownList('getItemByValue', returnData.presenceid).index );
                    } catch(err){}
                    $('#execPresenceId_' + uniqueItemId).off('bindingComplete');
                });
                $('#deleteExecItem_' + uniqueItemId).jqxButton({ width: '50', theme: siteSettings.getTheme()});
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    clearInternalWidgetHandlers('#DIV_sceneExecItem_' + uniqueItemId);
                    $('#DIV_sceneExecItem_' + uniqueItemId).empty();
                });
            break;
            case "daypart":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Set part of day to</span>' +
                                '<div style="display:table-cell; vertical-align:middle"><div id="execDaypartId_' + uniqueItemId+'"></div></div></div>';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#scene_exec_list');
                $('#execDaypartId_' + uniqueItemId).jqxDropDownList({source: allDaypartsOptionsList, valueMember: "id", displayMember: "name", width: '100', theme: siteSettings.getTheme()});
                $('#execDaypartId_' + uniqueItemId).on('bindingComplete', function (event) { 
                    try {
                        $('#execDaypartId_' + uniqueItemId).jqxDropDownList('selectIndex', $('#execDaypartId_' + uniqueItemId).jqxDropDownList('getItemByValue', returnData.presenceid).index );
                    } catch(err){}
                    $('#execDaypartId_' + uniqueItemId).off('bindingComplete');
                });
                $('#deleteExecItem_' + uniqueItemId).jqxButton({ width: '50', theme: siteSettings.getTheme()});
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    clearInternalWidgetHandlers('#DIV_sceneExecItem_' + uniqueItemId);
                    $('#DIV_sceneExecItem_' + uniqueItemId).empty();
                });
            break;
            case "scene":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Run scene <strong>' + returnData.sceneName + '</strong></span></div>' +
                                '<input type="hidden" name="execSceneId_' + uniqueItemId+'" id="execSceneId_' + uniqueItemId+'" value="' + returnData.sceneId + '" />' +
                                '<input type="hidden" name="execSceneName_' + uniqueItemId+'" id="execSceneName_' + uniqueItemId+'" value="' + returnData.sceneName + '" />';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#scene_exec_list');
                $('#deleteExecItem_' + uniqueItemId).jqxButton({ width: '50', theme: siteSettings.getTheme()});
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    clearInternalWidgetHandlers('#DIV_sceneExecItem_' + uniqueItemId);
                    $('#DIV_sceneExecItem_' + uniqueItemId).empty();
                });
            break;
            case "remoteplugin":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Press a button on <strong>' + returnData.remoteName + '</strong></span>'+
                                    '<div style="display:table-cell; vertical-align:middle"><div id="execRemoteButtonId_' + uniqueItemId+'"></div>'+
                                 '</div>' +
                                 
                                '<input type="hidden" name="execRemoteId_' + uniqueItemId+'" id="execRemoteId_' + uniqueItemId+'" value="' + returnData.remoteId + '" />' +
                                '<input type="hidden" name="execRemoteName_' + uniqueItemId+'" id="execRemoteName_' + uniqueItemId+'" value="' + returnData.remoteName + '" />';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#scene_exec_list');
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
                $('#deleteExecItem_' + uniqueItemId).jqxButton({ width: '50', theme: siteSettings.getTheme()});
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    clearInternalWidgetHandlers('#DIV_sceneExecItem_' + uniqueItemId);
                    $('#DIV_sceneExecItem_' + uniqueItemId).empty();
                });
            break;
            case "mediaplugin":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Run a <strong>'+returnData.mediaCommandTypeName+'</strong> on <strong>' + returnData.mediaName + '</strong> which is:</span><div style="display:table-cell; vertical-align:middle"><div id="execMediaCommandId_' + uniqueItemId+'"></div></div>' +
                                '<input type="hidden" name="execMediaId_' + uniqueItemId+'" id="execMediaId_' + uniqueItemId+'" value="' + returnData.mediaId + '" />' +
                                '<input type="hidden" name="execMediaName_' + uniqueItemId+'" id="execMediaName_' + uniqueItemId+'" value="' + returnData.mediaName + '" />' +
                                '<input type="hidden" name="execMediaCommandTypeId_' + uniqueItemId+'" id="execMediaCommandTypeId_' + uniqueItemId+'" value="' + returnData.mediaCommandTypeId + '" />' +
                                '<input type="hidden" name="execMediaCommandTypeName_' + uniqueItemId+'" id="execMediaCommandTypeName_' + uniqueItemId+'" value="' + returnData.mediaCommandTypeName + '" />';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#scene_exec_list');
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
                $('#deleteExecItem_' + uniqueItemId).jqxButton({ width: '50', theme: siteSettings.getTheme()});
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    clearInternalWidgetHandlers('#DIV_sceneExecItem_' + uniqueItemId);
                    $('#DIV_sceneExecItem_' + uniqueItemId).empty();
                });
            break;
        }
    }

    $("#save_scene").jqxButton({width: '100', theme: siteSettings.getTheme()});
    $("#save_scene").on('click', function() {
        var execCollection = new Array();
        var execCounter = 0;
        var inError = false;
        if(inputFieldValid($('#scene_name')) && inputFieldValid($('#scene_description')) ){
            $('#scene_exec_list>').each(function() {
                if ($(this).attr("id") !== undefined && $(this).attr("id").startsWith('DIV_sceneExecItem_')) {
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
                            "value"      : JSON.parse($("#execDeviceCommandValue_" + workWith).val()),
                            "extra"      : $("#execDeviceCommandExtra_" + workWith).val()
                        };
                        execCollection.push(item);
                        execCounter++;
                    } else if ($('#execSceneId_' + workWith).length!==0) {
                        var item = {
                            "itemtype": "scene",
                            "sceneid": parseInt($("#execSceneId_" + workWith).val()),
                            "scenename": $("#execSceneName_" + workWith).val()
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
                showErrorMessage("Scene error", "Please use a minimum of one execution item");
            } else if(inError===true) {
                /// do nothing and keep message
            } else {
                var sceneId = $("#scene_id").val();
                if(sceneId!==undefined && sceneId!==0 && sceneId!==""){
                    var params = { "id"          : parseInt(sceneId),
                                   "name"        : $('#scene_name').val(), 
                                   "description" : $('#scene_description').val(),
                                   "dependencies": execCollection
                    };
                    var rpcCommand = { "jsonrpc"     : "2.0",
                                       "method"      : "ScenesService.editScene",
                                       "params"      : params,
                                       "id"          : "ScenesService.editScene"};
                } else {
                    var params = { "name"        : $('#scene_name').val(), 
                                   "description" : $('#scene_description').val(),
                                   "dependencies": execCollection
                    };
                    var rpcCommand = { "jsonrpc"     : "2.0",
                                       "method"      : "ScenesService.saveScene",
                                       "params"      : params,
                                       "id"          : "ScenesService.saveScene"};
                }
                var postField = {};
                postField["rpc"] = JSON.stringify(rpcCommand);
                $.post("/jsonrpc.json",postField)
                    .done(function(data) {
                        try {
                            if(data.result.success !== true){
                                var message = "<strong>Message</strong>:<br/>";
                                if (data.result.data.trace !== undefined) {
                                    message += data.result.data.message + "<br/><br/><strong>Trace</strong>:<br/>" + data.result.data.trace;
                                } else {
                                    message += data.result.data.message;
                                }
                                showErrorMessage("Scene error: " + data.result.error.message, message);
                            } else {
                                showInfoMessage("Scene saved", "Scene has been saved");
                                refreshPageContent('/desktop/scenes.html');
                            }
                        } catch(err){
                            var message = "<strong>Message</strong>:<br/>";
                            if (data.result.data.trace !== undefined) {
                                message += data.result.data.message + "<br/><br/><strong>Trace</strong>:<br/>" + data.result.data.trace;
                            } else {
                                message += data.result.data.message;
                            }
                            showErrorMessage("Server error: " + data.error.message, message);
                        }
                    }, "json");
            }
        } else {
            showErrorMessage("Scene save error", "Make sure you use a correct scene name and description.");
        }
    });

    $("#add_exec").jqxButton({width: '100', theme: siteSettings.getTheme()});
    $("#add_exec").on('click', function() {
        itemSelectionModal.setCallBack(function(returnData){
            createExecItems(returnData);
        });
        itemSelectionModal.setOptions({"deviceFilter": ["!data"], "deviceTypeFilter":["devices"]});
        itemSelectionModal.setSelectionType("exec");
        itemSelectionModal.open();
    });
    
    $("#clear_exec").jqxButton({width: '100', theme: siteSettings.getTheme()});
    $("#clear_exec").on('click', function() {
        clearInternalWidgetHandlers("#scene_exec_list");
        $("#scene_exec_list").empty();
    });
    
    var sceneId = $("#scene_id").val();
    if(sceneId!==undefined && sceneId!==0 && sceneId!==""){
        $.get('/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "ScenesService.getScene", "params":{"id":' + sceneId + '},"id":"ScenesService.getScene"}')
            .done(function(data) {
                var sceneData = data.result.data;
                $("#scene_name").val(sceneData.name);
                $("#scene_description").val(sceneData.description);
                createSizedWebInputField($('#scene_name'), 150);
                createSizedWebInputField($('#scene_description'), 250);
                $("#scene_id").val(sceneData.id);
                if(sceneData.dependencies!==undefined){
                    for(var i=0;i<sceneData.dependencies.length;i++){
                        createExecItemsByMapping(sceneData.dependencies[i]);
                    }
                } else {
                    showErrorMessage("Scene error", "Could not load scene correctly, make sure it exists");
                }
        }, "json");
    } else {
        createSizedWebInputField($('#scene_name'), 150);
        createSizedWebInputField($('#scene_description'), 250);
    }
    
});