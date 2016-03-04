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
    
    jsPlumb.Defaults.ConnectionsDetachable = false;
    jsPlumb.ready(function() {
        
        var itemSelectionModal = new ItemSelectionModal();

        var defaultArrow = ["Arrow", {location: 1, length: 14, foldback: 0.8}];

        var ruleTypeReccurrences = {
            datatype: "json",
            datafields: [
                {name: 'id', type: 'int'},
                {name: 'name', type: 'string'}
            ],
            localData: [{"id": "TOGGLE", "name": "Toggle"}, {"id": "CONTINUOUS", "name": "Continuous"}, {"id": "ONCE", "name": "Once"}]
        };
        var allRuleTypeReccurrences = new $.jqx.dataAdapter(ruleTypeReccurrences);
        $("#trigger_reccurrence").jqxDropDownList({ source: allRuleTypeReccurrences, valueMember: "id", displayMember: "name", width: '150', theme: siteSettings.getTheme()});
        createSizedWebInputField($('#trigger_name'), 150);
        createSizedWebInputField($('#trigger_description'), 250);

        var ruleTypeOptions = {
            datatype: "json",
            datafields: [
                {name: 'id', type: 'int'},
                {name: 'name', type: 'string'}
            ],
            localData: [{"id": "simple", "name": "A single"}, {"id": "or", "name": "One off"}, {"id": "and", "name": "All"}]
        };
        var allRuleTypeOptions = new $.jqx.dataAdapter(ruleTypeOptions);

        var matchTypeOptions = {
            datatype: "json",
            datafields: [
                {name: 'id', type: 'int'},
                {name: 'name', type: 'string'}
            ],
            localData: [{"id": "EQUALS", "name": "equal to"}, {"id": "DIFFER", "name": "else then"}, {"id": "LESSTHEN", "name": "less then"}, {"id": "GREATERTHEN", "name": "more then"}]
        };
        var allMatchTypeOptions = new $.jqx.dataAdapter(matchTypeOptions);

        var matchTypeDiffOptions = {
            datatype: "json",
            datafields: [
                {name: 'id', type: 'int'},
                {name: 'name', type: 'string'}
            ],
            localData: [{"id": "EQUALS", "name": "equal to"}, {"id": "DIFFER", "name": "else then"}]
        };
        var allMatchTypeDiffOptions = new $.jqx.dataAdapter(matchTypeDiffOptions);

        var matchTypeOnlyMatchOptions = {
            datatype: "json",
            datafields: [
                {name: 'id', type: 'int'},
                {name: 'name', type: 'string'}
            ],
            localData: [{"id": "EQUALS", "name": "equal to"}]
        };
        var allMatchTypeOnlyMatchOptions = new $.jqx.dataAdapter(matchTypeOnlyMatchOptions);

        var matchTypeBooleanOptions = {
            datatype: "json",
            datafields: [
                {name: 'id', type: 'boolean'},
                {name: 'name', type: 'string'}
            ],
            localData: [{"id": true, "name": "True"}, {"id": false, "name": "False"}]
        };
        var allMatchTypeBooleanOptions = new $.jqx.dataAdapter(matchTypeBooleanOptions);

        var matchTypeOptionsTimeDiff = {
            datatype: "json",
            datafields: [
                {name: 'id', type: 'int'},
                {name: 'name', type: 'string'}
            ],
            localData: [{"id": "-", "name": "min"}, {"id": "+", "name": "plus"}]
        };
        var allMatchTypeOptionsTimeDiff = new $.jqx.dataAdapter(matchTypeOptionsTimeDiff);

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
        
        var sourceEndpoint = {
            anchor: "RightMiddle",
            connectorStyle: {strokeStyle: "#5c96bc", lineWidth: 2, outlineColor: "transparent", outlineWidth: 4},
            isSource: true
        };
        var targetEndpoint = {
            anchor: "LeftMiddle",
            connectorStyle: {strokeStyle: "#5c96bc", lineWidth: 2, outlineColor: "transparent", outlineWidth: 4},
            maxConnections: 1,
            isTarget: true
        };
        
        
        var instance = jsPlumb.getInstance({
            ConnectionsDetachable: false,
            HoverPaintStyle: {strokeStyle: "#1e8151", lineWidth: 2},
            PaintStyle: {strokeStyle: "#5c96bc", lineWidth: 2, outlineColor: "transparent", outlineWidth: 4},
            Endpoint: ["Dot", {radius: 2}],
            Connector: ["StateMachine", {curviness: 0, proximityLimit: 150} ],
            Container: $("#editor")
        });
        var windows = jsPlumb.getSelector("#editor .editoritem");
        instance.draggable(windows);
        instance.bind("click", function(c) {
            instance.detach(c);
        });
        $("#primaryrule").jqxDropDownList({ source: allRuleTypeOptions, selectedIndex: 0, valueMember: "id", displayMember: "name", width: '80', theme: siteSettings.getTheme()});
        
        function createTriggerItemsByMapping(data){
            var newData;
            try {
                switch(data.itemtype){
                    case "device":
                        newData = {
                            "itemType"              : "device",
                            "deviceName"            : data.devicename,
                            "deviceCommandName"     : data.commandname,
                            "deviceId"              : data.deviceid,
                            "deviceGroupId"         : data.group,
                            "deviceCommandId"       : data.command,
                            "deviceCommandDataType" : data.datatype,
                            "deviceCommandPrefix"   : data.prefix,
                            "deviceCommandSuffix"   : data.suffix,
                            "deviceCommandValue"    : data.matchvalue,
                            "deviceCommandMatchType": data.matchtype
                        };
                        var uniqueId = createTriggerItems(newData);
                        $('#triggerValueType_' + uniqueId).val(data.matchtype);
                        return uniqueId;
                    break;
                    case "daytime":
                        newData = {
                            "itemType" : "daytime",
                            "modType"  : data.timemod,
                            "matchtype" : data.matchtype,
                            "reccurrenceId" : data.occurrence,
                            "matchvalue" : data.matchvalue,
                            "daytimeItemId" : data.timetype,
                            "reccurrenceName" : data.occurrencename,
                            "daytimeItemName" : data.timetypename
                        };
                        var uniqueId = createTriggerItems(newData);
                        $('#triggerValueType_' + uniqueId).val(data.matchtype);
                        return uniqueId;
                    break;
                    case "presence":
                    case "daypart":
                    case "userstatus":
                        newData = {
                            "itemType"   : data.itemtype,
                            "matchType"  : data.matchtype,
                            "matchValue" : data.matchvalue
                        };
                        var uniqueId = createTriggerItems(newData);
                        $('#triggerValueType_' + uniqueId).val(data.matchtype);
                        return uniqueId;
                    break;
                    case "mediaplugin":
                        newData = {
                            "itemType"   : data.itemtype,
                            "matchType"  : data.matchtype,
                            "matchValue" : data.matchvalue,
                            "mediaId"             : data.mediaId,
                            "mediaName"           : data.mediaName,
                            "mediaCommandTypeId"  : data.mediaCommandTypeId,
                            "mediaCommandTypeName": data.mediaCommandTypeName
                        };
                        var uniqueId = createTriggerItems(newData);
                        $('#triggerValueType_' + uniqueId).val(data.matchtype);
                        return uniqueId;
                    break;
                }
            } catch (err){
                //// invalid data
            }
        }
        
        function createExecItemsByMapping(data){
            var newData;
            try {
                switch(data.itemtype){
                    case "device":
                        newData = {
                            "itemType"              : "device",
                            "deviceName"            : data.devicename,
                            "deviceCommandName"     : data.commandname,
                            "deviceCommandType"     : data.commandtype,
                            "deviceCommandDataType" : data.datatype,
                            "deviceId"              : data.deviceid,
                            "deviceGroupId"         : data.group,
                            "deviceCommandId"       : data.command,
                            "deviceCommandValue"    : data.value,
                            "deviceCommandExtra"    : data.extra
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
                    case "messengerplugin":
                        newData = {
                            "itemType"    : "messengerplugin",
                            "messageType" : data.type,
                            "message"     : data.message,
                            "typeName"    : data.typename
                        };
                        createExecItems(newData);
                    break;
                }
            } catch (err){
                //// invalid data
            }
        }
        
        function createTriggerItems(returnData){
            var uniqueItemId = createUUID();
            var deviceDivId = 'DIV_triggerMatchItem_' + $('#currentItemAddUUID').val() + '_' + uniqueItemId;
            switch(returnData.itemType){
                case "device":
                    var deviceHtml = 'When the <strong>' + returnData.deviceCommandName + '</strong><br/>of <strong>' + returnData.deviceName + '</strong> ' +
                            '<input type="hidden" name="triggerDeviceDeviceName_' + uniqueItemId+'" id="triggerDeviceDeviceName_' + uniqueItemId+'" value="' + returnData.deviceName + '" />' +
                            '<input type="hidden" name="triggerDeviceCommandName_' + uniqueItemId+'" id="triggerDeviceCommandName_' + uniqueItemId+'" value="' + returnData.deviceCommandName + '" />' +
                            '<input type="hidden" name="triggerDeviceCommandPrefix_' + uniqueItemId+'" id="triggerDeviceCommandPrefix_' + uniqueItemId+'" value="' + returnData.deviceCommandPrefix + '" />' +
                            '<input type="hidden" name="triggerDeviceCommandSuffix_' + uniqueItemId+'" id="triggerDeviceCommandSuffix_' + uniqueItemId+'" value="' + returnData.deviceCommandSuffix + '" />' +
                            '<input type="hidden" name="triggerDeviceDeviceId_' + uniqueItemId+'" id="triggerDeviceDeviceId_' + uniqueItemId+'" value="' + returnData.deviceId + '" />' +
                            '<input type="hidden" name="triggerDeviceGroup_' + uniqueItemId+'" id="triggerDeviceGroup_' + uniqueItemId+'" value="' + returnData.deviceGroupId + '" />' +
                            '<input type="hidden" name="triggerDeviceCommand_' + uniqueItemId+'" id="triggerDeviceCommand_' + uniqueItemId+'" value="' + returnData.deviceCommandId + '" />' + 
                            '<input type="hidden" name="triggerDeviceCommandDataType_' + uniqueItemId+'" id="triggerDeviceCommandDataType_' + uniqueItemId+'" value="' + returnData.deviceCommandDataType + '" />';
                    var DeviceDiv = $('<div>', {id: deviceDivId}).
                            html(deviceHtml).appendTo('#editor');
                    $(DeviceDiv).addClass('editoritem triggerMatchItem');
                    instance.draggable($('#' + deviceDivId));
                    instance.addEndpoint($('#' + deviceDivId), targetEndpoint);
                    instance.addEndpoint($('#' + deviceDivId), sourceEndpoint);
                    instance.connect({source: 'DIV_ruleid_' + $('#currentItemAddUUID').val(),
                        connectionsDetachable:false,
                        target: deviceDivId,
                        anchors: ["RightMiddle", "LeftMiddle"],
                        overlays: [
                            defaultArrow,
                            ["Label", {id: 'itemLabelRuleType', label: getItemMatchLabel($('#currentItemAddUUID').val()), cssClass: 'aLabel'}]
                        ]
                    });
                    positionDivGrids('DIV_triggerMatchItem_');
                    if(returnData.deviceCommandValue!==undefined){
                        createDeviceMatchDiv(deviceDivId, uniqueItemId, returnData.deviceCommandPrefix, returnData.deviceCommandSuffix,returnData.deviceCommandValue,returnData.deviceCommandDataType);
                    } else {
                        createDeviceMatchDiv(deviceDivId, uniqueItemId, returnData.deviceCommandPrefix, returnData.deviceCommandSuffix,"",returnData.deviceCommandDataType);
                    }
                    instance.unbind("click");
                break;
                case "daytime":
                    var betweentext = " ";
                    if(returnData.daytimeItemId==="FIXED"){
                        betweentext = " at a ";
                    }
                    var deviceHtml = '<strong>' + returnData.reccurrenceName + '</strong>'+betweentext+'<strong>' + returnData.daytimeItemName + '</strong> ' +
                            '<input type="hidden" name="triggerDaytimeReccurrence_' + uniqueItemId + '" id="triggerDaytimeReccurrence_' + uniqueItemId + '" value="' + returnData.reccurrenceId + '" />' +
                            '<input type="hidden" name="triggerDaytimeReccurrenceName_' + uniqueItemId + '" id="triggerDaytimeReccurrenceName_' + uniqueItemId + '" value="' + returnData.reccurrenceName + '" />' +
                            '<input type="hidden" name="triggerDayTimeDaytimeName_' + uniqueItemId + '" id="triggerDayTimeDaytimeName_' + uniqueItemId + '" value="' + returnData.daytimeItemName + '" />' +
                            '<input type="hidden" name="triggerDayTimeDaytime_' + uniqueItemId + '" id="triggerDayTimeDaytime_' + uniqueItemId + '" value="' + returnData.daytimeItemId + '" />';
                    var DeviceDiv = $('<div>', {id: deviceDivId}).
                            html(deviceHtml).appendTo('#editor');
                    $(DeviceDiv).addClass('editoritem triggerMatchItem');
                    instance.draggable($('#' + deviceDivId));
                    instance.addEndpoint($('#' + deviceDivId), targetEndpoint);
                    instance.addEndpoint($('#' + deviceDivId), sourceEndpoint);
                    instance.connect({source: 'DIV_ruleid_' + $('#currentItemAddUUID').val(),
                        connectionsDetachable:false,
                        target: deviceDivId,
                        anchors: ["RightMiddle", "LeftMiddle"],
                        overlays: [
                            defaultArrow,
                            ["Label", {id: 'itemLabelRuleType', label: getItemMatchLabel($('#currentItemAddUUID').val()), cssClass: 'aLabel'}]
                        ]
                    });
                    positionDivGrids('DIV_triggerMatchItem_');
                    createTimeMatchDiv(deviceDivId, uniqueItemId, returnData.daytimeItemId, returnData.modType, returnData.matchvalue);
                    instance.unbind("click");
                break;
                case "presence":
                    var deviceHtml = '<strong>Presence</strong> ' +
                                     '<input type="hidden" name="triggerPresenceItem_' + uniqueItemId + '" id="triggerPresenceItem_' + uniqueItemId + '" value="true" />';
                    var DeviceDiv = $('<div>', {id: deviceDivId}).
                            html(deviceHtml).appendTo('#editor');
                    $(DeviceDiv).addClass('editoritem triggerMatchItem');
                    instance.draggable($('#' + deviceDivId));
                    instance.addEndpoint($('#' + deviceDivId), targetEndpoint);
                    instance.addEndpoint($('#' + deviceDivId), sourceEndpoint);
                    instance.connect({source: 'DIV_ruleid_' + $('#currentItemAddUUID').val(),
                        connectionsDetachable:false,
                        target: deviceDivId,
                        anchors: ["RightMiddle", "LeftMiddle"],
                        overlays: [
                            defaultArrow,
                            ["Label", {id: 'itemLabelRuleType', label: getItemMatchLabel($('#currentItemAddUUID').val()), cssClass: 'aLabel'}]
                        ]
                    });
                    positionDivGrids('DIV_triggerMatchItem_');
                    createPresenceMatchDiv(deviceDivId, uniqueItemId, returnData.matchType, returnData.matchValue);
                    instance.unbind("click");
                break;
                case "daypart":
                    var deviceHtml = '<strong>Part of day</strong> ' +
                                     '<input type="hidden" name="triggerDaypartItem_' + uniqueItemId + '" id="triggerDaypartItem_' + uniqueItemId + '" value="true" />';
                    var DeviceDiv = $('<div>', {id: deviceDivId}).
                            html(deviceHtml).appendTo('#editor');
                    $(DeviceDiv).addClass('editoritem triggerMatchItem');
                    instance.draggable($('#' + deviceDivId));
                    instance.addEndpoint($('#' + deviceDivId), targetEndpoint);
                    instance.addEndpoint($('#' + deviceDivId), sourceEndpoint);
                    instance.connect({source: 'DIV_ruleid_' + $('#currentItemAddUUID').val(),
                        connectionsDetachable:false,
                        target: deviceDivId,
                        anchors: ["RightMiddle", "LeftMiddle"],
                        overlays: [
                            defaultArrow,
                            ["Label", {id: 'itemLabelRuleType', label: getItemMatchLabel($('#currentItemAddUUID').val()), cssClass: 'aLabel'}]
                        ]
                    });
                    positionDivGrids('DIV_triggerMatchItem_');
                    createDaypartMatchDiv(deviceDivId, uniqueItemId, returnData.matchType, returnData.matchValue);
                    instance.unbind("click");
                break;
                case "userstatus":
                    var deviceHtml = '<strong>User status</strong> ' +
                                     '<input type="hidden" name="triggerUserstatusItem_' + uniqueItemId + '" id="triggerUserstatusItem_' + uniqueItemId + '" value="true" />';
                    var DeviceDiv = $('<div>', {id: deviceDivId}).
                            html(deviceHtml).appendTo('#editor');
                    $(DeviceDiv).addClass('editoritem triggerMatchItem');
                    instance.draggable($('#' + deviceDivId));
                    instance.addEndpoint($('#' + deviceDivId), targetEndpoint);
                    instance.addEndpoint($('#' + deviceDivId), sourceEndpoint);
                    instance.connect({source: 'DIV_ruleid_' + $('#currentItemAddUUID').val(),
                        connectionsDetachable:false,
                        target: deviceDivId,
                        anchors: ["RightMiddle", "LeftMiddle"],
                        overlays: [
                            defaultArrow,
                            ["Label", {id: 'itemLabelRuleType', label: getItemMatchLabel($('#currentItemAddUUID').val()), cssClass: 'aLabel'}]
                        ]
                    });
                    positionDivGrids('DIV_triggerMatchItem_');
                    createUserstatusMatchDiv(deviceDivId, uniqueItemId, returnData.matchType, returnData.matchValue);
                    instance.unbind("click");
                break;
                case "mediaplugin":
                    var deviceHtml = 'When <strong>'+returnData.mediaName+'</strong> command ' +
                                     '<input type="hidden" name="triggerMediaPluginMediaItem_' + uniqueItemId + '" id="triggerMediaPluginMediaItem_' + uniqueItemId + '" value="true" />' +
                                     '<input type="hidden" name="triggerMediaPluginMediaId_' + uniqueItemId + '" id="triggerMediaPluginMediaId_' + uniqueItemId + '" value="' + returnData.mediaId + '" />' +
                                     '<input type="hidden" name="triggerMediaPluginItemMediaName_' + uniqueItemId + '" id="triggerMediaPluginItemMediaName_' + uniqueItemId + '" value="' + returnData.mediaName + '" />' +
                                     '<input type="hidden" name="triggerMediaPluginItemCommandTypeId_' + uniqueItemId + '" id="triggerMediaPluginItemCommandTypeId_' + uniqueItemId + '" value="' + returnData.mediaCommandTypeId + '" />' + 
                                     '<input type="hidden" name="triggerMediaPluginItemCommandTypeName_' + uniqueItemId + '" id="triggerMediaPluginItemMediaName_' + uniqueItemId + '" value="' + returnData.mediaCommandTypeName + '" />';
                    var DeviceDiv = $('<div>', {id: deviceDivId}).
                            html(deviceHtml).appendTo('#editor');
                    $(DeviceDiv).addClass('editoritem triggerMatchItem');
                    instance.draggable($('#' + deviceDivId));
                    instance.addEndpoint($('#' + deviceDivId), targetEndpoint);
                    instance.addEndpoint($('#' + deviceDivId), sourceEndpoint);
                    instance.connect({source: 'DIV_ruleid_' + $('#currentItemAddUUID').val(),
                        connectionsDetachable:false,
                        target: deviceDivId,
                        anchors: ["RightMiddle", "LeftMiddle"],
                        overlays: [
                            defaultArrow,
                            ["Label", {id: 'itemLabelRuleType', label: getItemMatchLabel($('#currentItemAddUUID').val()), cssClass: 'aLabel'}]
                        ]
                    });
                    positionDivGrids('DIV_triggerMatchItem_');
                    createMediaCommandMatchDiv(deviceDivId, uniqueItemId, returnData.matchType, returnData.matchValue, returnData.mediaId, returnData.mediaCommandTypeId);
                    instance.unbind("click");
                break;
            }
            return uniqueItemId;
        }
        
        function createRuleItem(){
            var newRuleDivId = createUUID();
            var Div = $('<div>', {id: 'DIV_ruleid_' + newRuleDivId}).
                    html('<div><div style="float:left;" id="ruletype_' + newRuleDivId + '" style="z-index:10;"></div>' +
                            '<button style="float:left; margin-left: 5px;" id="linktemTo_' + newRuleDivId + '" name="linktemTo_' + newRuleDivId + '" value="Add">Add</button></div>').appendTo('#editor');
            $('#ruletype_' + newRuleDivId).jqxDropDownList({source: allRuleTypeOptions, selectedIndex: 0, valueMember: "id", displayMember: "name", width: '75', theme: siteSettings.getTheme()});
            $('#ruletype_' + newRuleDivId).on('select', function(event) {
                instance.select({
                    source: 'DIV_ruleid_' + $('#currentItemAddUUID').val()
                }).each(function(connection) {
                    connection.getOverlay('itemLabelRuleType').setLabel(getItemMatchLabel($('#currentItemAddUUID').val()));
                });
            });
            $('#linktemTo_' + newRuleDivId).jqxButton({width: '40', theme: siteSettings.getTheme()});
            $('#linktemTo_' + newRuleDivId).on('click', function() {
                $('#currentItemAddUUID').val(newRuleDivId);
                itemSelectionModal.setCallBack(function(returnData){
                    createTriggerItems(returnData);
                });
                itemSelectionModal.setOptions({"deviceFilter": "data"});
                itemSelectionModal.setSelectionType("match");
                itemSelectionModal.open();
            });
            $(Div).addClass('editoritem baseRule');
            instance.draggable($('#DIV_ruleid_' + newRuleDivId));
            instance.addEndpoint($(Div), targetEndpoint);
            instance.addEndpoint($(Div), sourceEndpoint);
            instance.connect({source: "staticWhen",
                target: 'DIV_ruleid_' + newRuleDivId,
                anchors: ["RightMiddle", "LeftMiddle"],
                overlays: [
                    defaultArrow,
                    ["Label", {label: getBaseRuleMatchLabel(), id: 'baseLabelRuleType', cssClass: 'aLabel'}]
                ]
            });
            positionDivGrids('DIV_ruleid_');
            return newRuleDivId;
        }
        
        $("#addRule").jqxButton({width: '50', theme: siteSettings.getTheme()});
        $("#addRule").on('click', function() {
            createRuleItem();
        });

        function createMediaCommandMatchDiv(source, currentItemDiv, matchType, matchValue, mediaId, commmandType){
            //// lets add a device to the graph
            var innerDivUnique = createUUID();
            var deviceHtml = '<div class="nvp" style="width:230px;">\n\
                                            <div class="n" style="width:100px;"><div id="triggerValueType_' + currentItemDiv + '"></div></div>\n\
                                            <div class="v" style="width:100px;"><div id="triggerValue_' + currentItemDiv + '"></div></div>\n\
                                         </div>' +
                             '<div class="nvp" style="width:160px;"><div class="n"style="width:100px;"><a style="color:white;" id="a_delete_'+innerDivUnique+'" href="#delete">Delete</a></div></div>';
            var matchDivId = 'DIV_triggerMatchRuleItem_' + createUUID();
            var MatchDiv = $('<div>', {id: matchDivId}).
                    html(deviceHtml).appendTo('#editor');
            $('#a_delete_'+innerDivUnique).on("click", function(){
                clearInternalWidgetHandlers("#" + matchDivId);
                instance.detachAllConnections(matchDivId);
                instance.removeAllEndpoints(matchDivId);
                instance.detachAllConnections(source);
                instance.removeAllEndpoints(source);
                $('#' + source).remove();
                $('#' + matchDivId).remove();
                positionDivGrids('DIV_triggerMatchItem_');
                positionDivGrids('DIV_triggerMatchRuleItem_');
            });
            $(MatchDiv).addClass('editoritem triggerMatchValue');
            $('#triggerValueType_' + currentItemDiv).jqxDropDownList({source: allMatchTypeOnlyMatchOptions, selectedIndex: 0, valueMember: "id", displayMember: "name", width: '100', theme: siteSettings.getTheme()});
            $('#triggerValueType_' + currentItemDiv).val(matchType);
            var mediaCommandSetList = {
                datatype: "json",
                datafields: [
                    {name: 'id', type: 'string'},
                    {name: 'name', type: 'string'}
                ],
                url: '/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "MediaService.getCapabilities","params":{"id":'+mediaId+', "type":"'+commmandType+'"}, "id":"MediaService.getCapabilities"}',
                root: "result>data"
            };
            var DataMediaCommandSetList = new $.jqx.dataAdapter(mediaCommandSetList);
            $('#triggerValue_' + currentItemDiv).jqxDropDownList({source: DataMediaCommandSetList, valueMember: "id", displayMember: "name", width: '100', theme: siteSettings.getTheme()});
            if(matchValue!==undefined){
                $('#triggerValue_' + currentItemDiv).on('bindingComplete', function (event) { 
                    try {
                        $('#triggerValue_' + currentItemDiv).jqxDropDownList('selectIndex', $('#triggerValue_' + currentItemDiv).jqxDropDownList('getItemByValue', matchValue).index );
                    } catch(err){}
                    $('#triggerValue_' + currentItemDiv).off('bindingComplete');
                });
            }
            instance.draggable($('#' + matchDivId));
            instance.addEndpoint($('#' + matchDivId), targetEndpoint);
            instance.addEndpoint($('#' + matchDivId), sourceEndpoint);
            instance.connect({source: source,
                target: matchDivId,
                anchor: ["RightMiddle", "LeftMiddle"],
                overlays: [
                    defaultArrow,
                    ["Label", {label: " is ", cssClass: 'aLabel'}]
                ]
            });
            positionDivGrids('DIV_triggerMatchRuleItem_');
        }

        function createUserstatusMatchDiv(source, currentItemDiv, matchType, matchValue){
            //// lets add a device to the graph
            var innerDivUnique = createUUID();
            var deviceHtml = '<div class="nvp" style="width:230px;">\n\
                                            <div class="n" style="width:100px;"><div id="triggerValueType_' + currentItemDiv + '"></div></div>\n\
                                            <div class="v" style="width:100px;"><div id="triggerValue_' + currentItemDiv + '"></div></div>\n\
                                         </div>' +
                             '<div class="nvp" style="width:160px;"><div class="n"style="width:100px;"><a style="color:white;" id="a_delete_'+innerDivUnique+'" href="#delete">Delete</a></div></div>';
            var matchDivId = 'DIV_triggerMatchRuleItem_' + createUUID();
            var MatchDiv = $('<div>', {id: matchDivId}).
                    html(deviceHtml).appendTo('#editor');
            $('#a_delete_'+innerDivUnique).on("click", function(){
                clearInternalWidgetHandlers("#" + matchDivId);
                instance.detachAllConnections(matchDivId);
                instance.removeAllEndpoints(matchDivId);
                instance.detachAllConnections(source);
                instance.removeAllEndpoints(source);
                $('#' + source).remove();
                $('#' + matchDivId).remove();
                positionDivGrids('DIV_triggerMatchItem_');
                positionDivGrids('DIV_triggerMatchRuleItem_');
            });
            $(MatchDiv).addClass('editoritem triggerMatchValue');
            $('#triggerValueType_' + currentItemDiv).jqxDropDownList({source: allMatchTypeDiffOptions, selectedIndex: 0, valueMember: "id", displayMember: "name", width: '100', theme: siteSettings.getTheme()});
            $('#triggerValueType_' + currentItemDiv).val(matchType);
            $('#triggerValue_' + currentItemDiv).jqxDropDownList({source: dataUserStatusesOptionsList, valueMember: "id", displayMember: "name", width: '100', theme: siteSettings.getTheme()});
            if(matchValue!==undefined){
                $('#triggerValue_' + currentItemDiv).on('bindingComplete', function (event) { 
                    try {
                        $('#triggerValue_' + currentItemDiv).jqxDropDownList('selectIndex', $('#triggerValue_' + currentItemDiv).jqxDropDownList('getItemByValue', matchValue).index );
                    } catch(err){}
                    $('#triggerValue_' + currentItemDiv).off('bindingComplete');
                });
            }
            instance.draggable($('#' + matchDivId));
            instance.addEndpoint($('#' + matchDivId), targetEndpoint);
            instance.addEndpoint($('#' + matchDivId), sourceEndpoint);
            instance.connect({source: source,
                target: matchDivId,
                anchor: ["RightMiddle", "LeftMiddle"],
                overlays: [
                    defaultArrow,
                    ["Label", {label: " is ", cssClass: 'aLabel'}]
                ]
            });
            positionDivGrids('DIV_triggerMatchRuleItem_');
        }

        function createPresenceMatchDiv(source, currentItemDiv, matchType, matchValue){
            //// lets add a device to the graph
            var innerDivUnique = createUUID();
            var deviceHtml = '<div class="nvp" style="width:230px;">\n\
                                            <div class="n" style="width:100px;"><div id="triggerValueType_' + currentItemDiv + '"></div></div>\n\
                                            <div class="v" style="width:100px;"><div id="triggerValue_' + currentItemDiv + '"></div></div>\n\
                                         </div>' +
                             '<div class="nvp" style="width:160px;"><div class="n"style="width:100px;"><a style="color:white;" id="a_delete_'+innerDivUnique+'" href="#delete">Delete</a></div></div>';
            var matchDivId = 'DIV_triggerMatchRuleItem_' + createUUID();
            var MatchDiv = $('<div>', {id: matchDivId}).
                    html(deviceHtml).appendTo('#editor');
            $('#a_delete_'+innerDivUnique).on("click", function(){
                clearInternalWidgetHandlers("#" + matchDivId);
                instance.detachAllConnections(matchDivId);
                instance.removeAllEndpoints(matchDivId);
                instance.detachAllConnections(source);
                instance.removeAllEndpoints(source);
                $('#' + source).remove();
                $('#' + matchDivId).remove();
                positionDivGrids('DIV_triggerMatchItem_');
                positionDivGrids('DIV_triggerMatchRuleItem_');
            });
            $(MatchDiv).addClass('editoritem triggerMatchValue');
            $('#triggerValueType_' + currentItemDiv).jqxDropDownList({source: allMatchTypeDiffOptions, selectedIndex: 0, valueMember: "id", displayMember: "name", width: '100', theme: siteSettings.getTheme()});
            $('#triggerValueType_' + currentItemDiv).val(matchType);
            $('#triggerValue_' + currentItemDiv).jqxDropDownList({source: allPresenceOptionsList, valueMember: "id", displayMember: "name", width: '100', theme: siteSettings.getTheme()});
            if(matchValue!==undefined){
                $('#triggerValue_' + currentItemDiv).on('bindingComplete', function (event) { 
                    try {
                        $('#triggerValue_' + currentItemDiv).jqxDropDownList('selectIndex', $('#triggerValue_' + currentItemDiv).jqxDropDownList('getItemByValue', matchValue).index );
                    } catch(err){}
                    $('#triggerValue_' + currentItemDiv).off('bindingComplete');
                });
            }
            instance.draggable($('#' + matchDivId));
            instance.addEndpoint($('#' + matchDivId), targetEndpoint);
            instance.addEndpoint($('#' + matchDivId), sourceEndpoint);
            instance.connect({source: source,
                target: matchDivId,
                anchor: ["RightMiddle", "LeftMiddle"],
                overlays: [
                    defaultArrow,
                    ["Label", {label: " is ", cssClass: 'aLabel'}]
                ]
            });
            positionDivGrids('DIV_triggerMatchRuleItem_');
        }

        function createDaypartMatchDiv(source, currentItemDiv, matchType, matchValue){
            //// lets add a device to the graph
            var innerDivUnique = createUUID();
            var deviceHtml = '<div class="nvp" style="width:230px;">\n\
                                            <div class="n" style="width:100px;"><div id="triggerValueType_' + currentItemDiv + '"></div></div>\n\
                                            <div class="v" style="width:100px;"><div id="triggerValue_' + currentItemDiv + '"></div></div>\n\
                                         </div>' +
                             '<div class="nvp" style="width:160px;"><div class="n"style="width:100px;"><a style="color:white;" id="a_delete_'+innerDivUnique+'" href="#delete">Delete</a></div></div>';
            var matchDivId = 'DIV_triggerMatchRuleItem_' + createUUID();
            var MatchDiv = $('<div>', {id: matchDivId}).
                    html(deviceHtml).appendTo('#editor');
            $('#a_delete_'+innerDivUnique).on("click", function(){
                clearInternalWidgetHandlers("#" + matchDivId);
                instance.detachAllConnections(matchDivId);
                instance.removeAllEndpoints(matchDivId);
                instance.detachAllConnections(source);
                instance.removeAllEndpoints(source);
                $('#' + source).remove();
                $('#' + matchDivId).remove();
                positionDivGrids('DIV_triggerMatchItem_');
                positionDivGrids('DIV_triggerMatchRuleItem_');
            });
            $(MatchDiv).addClass('editoritem triggerMatchValue');
            $('#triggerValueType_' + currentItemDiv).jqxDropDownList({source: allMatchTypeDiffOptions, selectedIndex: 0, valueMember: "id", displayMember: "name", width: '100', theme: siteSettings.getTheme()});
            $('#triggerValueType_' + currentItemDiv).val(matchType);
            $('#triggerValue_' + currentItemDiv).jqxDropDownList({source: allDaypartsOptionsList, valueMember: "id", displayMember: "name", width: '100', theme: siteSettings.getTheme()});
            if(matchValue!==undefined){
                $('#triggerValue_' + currentItemDiv).on('bindingComplete', function (event) { 
                    try {
                        $('#triggerValue_' + currentItemDiv).jqxDropDownList('selectIndex', $('#triggerValue_' + currentItemDiv).jqxDropDownList('getItemByValue', matchValue).index );
                    } catch(err){}
                    $('#triggerValue_' + currentItemDiv).off('bindingComplete');
                });
            }
            instance.draggable($('#' + matchDivId));
            instance.addEndpoint($('#' + matchDivId), targetEndpoint);
            instance.addEndpoint($('#' + matchDivId), sourceEndpoint);
            instance.connect({source: source,
                target: matchDivId,
                anchor: ["RightMiddle", "LeftMiddle"],
                overlays: [
                    defaultArrow,
                    ["Label", {label: " is ", cssClass: 'aLabel'}]
                ]
            });
            positionDivGrids('DIV_triggerMatchRuleItem_');
        }


        function createDeviceMatchDiv(source, currentItemDiv, deviceCommandPrefix, deviceCommandSuffix, deviceCommandValue, deviceCommandDatatype) {
            //// lets add a device to the graph
            var innerDivUnique = createUUID();
            var setValueInnerDiv;
            if(deviceCommandDatatype!==undefined && deviceCommandDatatype==="boolean"){
                setValueInnerDiv = '<div id="triggerValue_' + currentItemDiv + '"></div>';
            } else {
                setValueInnerDiv = '<input data-inputtype="'+deviceCommandDatatype+'" type="text" name="triggerValue_' + currentItemDiv + '" id="triggerValue_' + currentItemDiv + '" value=""/>';
            }
            var deviceHtml = '<div class="nvp" style="width:230px;">\n\
                                            <div class="n" style="width:100px;"><div id="triggerValueType_' + currentItemDiv + '"></div></div>\n\
                                            <div class="v" style="width:120px; margin-left:10px;">'+deviceCommandPrefix+setValueInnerDiv+deviceCommandSuffix+'</div>\n\
                                         </div>' +
                             '<div class="nvp" style="width:160px;"><div class="n"style="width:100px;"><a style="color:white;" id="a_delete_'+innerDivUnique+'" href="#delete">Delete</a></div></div>';
            var matchDivId = 'DIV_triggerMatchRuleItem_' + createUUID();
            var MatchDiv = $('<div>', {id: matchDivId}).
                    html(deviceHtml).appendTo('#editor');

            $('#a_delete_'+innerDivUnique).on("click", function(){
                clearInternalWidgetHandlers("#" + matchDivId);
                instance.detachAllConnections(matchDivId);
                instance.removeAllEndpoints(matchDivId);
                instance.detachAllConnections(source);
                instance.removeAllEndpoints(source);
                $('#' + source).remove();
                $('#' + matchDivId).remove();
                positionDivGrids('DIV_triggerMatchItem_');
                positionDivGrids('DIV_triggerMatchRuleItem_');
            });
            $(MatchDiv).addClass('editoritem triggerMatchValue');
            if(deviceCommandDatatype!==undefined && deviceCommandDatatype==="boolean"){
                $('#triggerValueType_' + currentItemDiv).jqxDropDownList({source: allMatchTypeDiffOptions, selectedIndex: 0, valueMember: "id", displayMember: "name", width: '100', theme: siteSettings.getTheme()});
                $('#triggerValue_' + currentItemDiv).jqxDropDownList({source: allMatchTypeBooleanOptions, selectedIndex: 0, valueMember: "id", displayMember: "name", width: '50', theme: siteSettings.getTheme()});
                if(deviceCommandValue!==undefined && (deviceCommandValue=="true"||deviceCommandValue=="false")){
                    try {
                        $('#triggerValue_' + currentItemDiv).jqxDropDownList('selectIndex', $('#triggerValue_' + currentItemDiv).jqxDropDownList('getItemByValue', deviceCommandValue).index );
                    } catch(err){}
                }
            } else {
                $('#triggerValueType_' + currentItemDiv).jqxDropDownList({source: allMatchTypeOptions, selectedIndex: 0, valueMember: "id", displayMember: "name", width: '100', theme: siteSettings.getTheme()});
                $('#triggerValue_' + currentItemDiv).val(deviceCommandValue);
                createSizedWebInputField($('#triggerValue_' + currentItemDiv), 50);
            }
            instance.draggable($('#' + matchDivId));
            instance.addEndpoint($('#' + matchDivId), targetEndpoint);
            instance.addEndpoint($('#' + matchDivId), sourceEndpoint);
            instance.connect({source: source,
                target: matchDivId,
                anchor: ["RightMiddle", "LeftMiddle"],
                overlays: [
                    defaultArrow,
                    ["Label", {label: " is ", cssClass: 'aLabel'}]
                ]
            });
            positionDivGrids('DIV_triggerMatchRuleItem_');
        }

        function createTimeMatchDiv(source, currentItemDiv, matchType, modType, timeValue) {
            //// lets add a device to the graph
            var deviceHtml = '';
            if (matchType === "FIXED"){
                deviceHtml = '<div class="nvp" style="width:250px;">\n\
                                  <div class="n" style="width:100px;"><div id="triggerValueType_' + currentItemDiv + '"></div></div>\n\n\
                                  <div class="v" style="width:140px; margin-left:10px;"><input data-inputtype="time" type="text" name="triggerValue_' + currentItemDiv + '" id="triggerValue_' + currentItemDiv + '" value=""/></div>\n\
                              <div class="n"style="width:100px;"><a style="color:white;" id="a_delete_'+currentItemDiv+'" href="#delete">Delete</a></div></div>';
            } else {
                deviceHtml = '<div class="nvp" style="width:250px;">\n\
                                  <div class="n" style="width:100px;"><div id="triggerValueType_' + currentItemDiv + '"></div></div>\n\n\
                                  <div class="v" style="width:140px; margin-left:10px;"><div style="float:left;" id="triggerTimeValueTypeCalc_' + currentItemDiv + '"></div><input style="float:left; margin-left: 5px;" data-inputtype="time" type="text" name="triggerValue_' + currentItemDiv + '" id="triggerValue_' + currentItemDiv + '" value=""/></div>\n\
                              <div class="n"style="width:100px;"><a style="color:white;" id="a_delete_'+currentItemDiv+'" href="#delete">Delete</a></div></div>';
            }
            var matchDivId = 'DIV_triggerMatchRuleItem_' + createUUID();
            var MatchDiv = $('<div>', {id: matchDivId}).
                    html(deviceHtml).appendTo('#editor');
            $('#a_delete_'+currentItemDiv).on("click", function(){
                clearInternalWidgetHandlers("#" + matchDivId);
                instance.detachAllConnections(matchDivId);
                instance.removeAllEndpoints(matchDivId);
                instance.detachAllConnections(source);
                instance.removeAllEndpoints(source);
                $('#' + source).remove();
                $('#' + matchDivId).remove();
                positionDivGrids('DIV_triggerMatchItem_');
                positionDivGrids('DIV_triggerMatchRuleItem_');
            });
            $('#triggerValueType_' + currentItemDiv).jqxDropDownList({source: allMatchTypeOptions, selectedIndex: 0, valueMember: "id", displayMember: "name", width: '100', theme: siteSettings.getTheme()});
            if (matchType !== "FIXED"){ 
                $('#triggerTimeValueTypeCalc_' + currentItemDiv).jqxDropDownList({source: allMatchTypeOptionsTimeDiff, selectedIndex: 0, valueMember: "id", displayMember: "name", width: '50', theme: siteSettings.getTheme()});
                if(modType!==undefined && modType!==""){
                    $('#triggerTimeValueTypeCalc_' + currentItemDiv).jqxDropDownList('selectIndex', $('#triggerTimeValueTypeCalc_' + currentItemDiv).jqxDropDownList('getItemByValue', modType).index );
                    $('#triggerTimeValueTypeCalc_' + currentItemDiv).val(modType);
                }
            }
            $(MatchDiv).addClass('editoritem triggerMatchValue');
            if(timeValue!==undefined && timeValue!==""){
                $('#triggerValue_' + currentItemDiv).val(timeValue);
            }
            instance.draggable($('#' + matchDivId));
            instance.addEndpoint($('#' + matchDivId), targetEndpoint);
            instance.addEndpoint($('#' + matchDivId), sourceEndpoint);
            instance.connect({source: source,
                target: matchDivId,
                anchor: ["RightMiddle", "LeftMiddle"],
                overlays: [
                    defaultArrow,
                    ["Label", {label: " time is ", cssClass: 'aLabel'}]
                ]
            });
            positionDivGrids('DIV_triggerMatchRuleItem_');
            createSizedWebInputField($('#triggerValue_' + currentItemDiv), 50);
        }

        function positionDivGrids(divName) {
            var internalRulesArray = new Array();
            $('#editor>').each(function() {
                if ($(this).attr("id") !== undefined && $(this).attr("id").startsWith(divName)) {
                    internalRulesArray.push($(this).attr("id"));
                }
            });
            var step = $("#editor").height() / (internalRulesArray.length + 1);
            for (var i = 0; i < internalRulesArray.length; i++) {
                instance.animate($("#" + internalRulesArray[i]).get(0), {"top": (step * (i + 1)) - ($("#" + internalRulesArray[i]).height() / 2 )}, {duration: 0});
            }
        }
        // suspend drawing and initialise.
        instance.doWhileSuspended(function() {
            
            $('#primaryrule').on('select', function(event) {
                instance.select({
                    source: 'staticWhen'
                }).each(function(connection) {
                    connection.getOverlay('baseLabelRuleType').setLabel(getBaseRuleMatchLabel());
                });
            });
            
            instance.makeSource(windows, {
                anchor: "RightMiddle",
                connectorStyle: {strokeStyle: "#5c96bc", lineWidth: 2, outlineColor: "transparent", outlineWidth: 4},
                paintStyle: {strokeStyle: "#5c96bc", lineWidth: 2},
                endpoint: ["Dot", {radius: 2}],
                connectionsDetachable:false,
                isSource: true
            });
            instance.makeTarget(windows, {
                anchor: "LeftMiddle",
                connectorStyle: {strokeStyle: "#5c96bc", lineWidth: 2, outlineColor: "transparent", outlineWidth: 4},
                endpoint: ["Dot", {radius: 2}],
                connectionsDetachable:false,
                isTarget: true
            });
            positionDivGrids('staticWhen');
            
            var triggerId = $("#trigger_id").val();
            if(triggerId!==undefined && triggerId!==0 && triggerId!==""){
                $.get('/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "TriggerService.getTrigger", "params":{"id":' + triggerId + '},"id":"TriggerService.getTrigger"}')
                    .done(function(data) {
                        var triggerData = data.result.data;
                        $("#trigger_name").val(triggerData.name);
                        $("#trigger_description").val(triggerData.description);
                        createSizedWebInputField($('#trigger_name'), 150);
                        createSizedWebInputField($('#trigger_description'), 250);
                        $("#trigger_reccurrence").val(triggerData.reccurrence);
                        $("#trigger_id").val(triggerData.id);
                        if(triggerData.rulesmatch!==undefined && triggerData.rulesmatch!==""){
                            $('#primaryrule').jqxDropDownList('selectIndex', $('#primaryrule').jqxDropDownList('getItemByValue', triggerData.rulesmatch).index );
                        }
                        if(triggerData.rules!==undefined){
                            for(var i=0;i<triggerData.rules.length;i++){

                                /// Set the base rule type.
                                var ruleItemDivId = createRuleItem();
                                $('#ruletype_' + ruleItemDivId).val(triggerData.rules[i].matchtype);
                                $('#currentItemAddUUID').val(ruleItemDivId);
                                instance.select({
                                    source: 'DIV_ruleid_' + $('#currentItemAddUUID').val()
                                }).each(function(connection) {
                                    connection.getOverlay('itemLabelRuleType').setLabel(getItemMatchLabel($('#currentItemAddUUID').val()));
                                });

                                /// Rule type is set, create the connections.
                                for(var j=0;j<triggerData.rules[i].collection.length;j++){
                                    createTriggerItemsByMapping(triggerData.rules[i].collection[j]);
                                }
                            }
                            for(var i=0;i<triggerData.executions.length;i++){
                                createExecItemsByMapping(triggerData.executions[i]);
                            }
                        } else {
                            showErrorMessage("Trigger error", "Could not load trigger correctly, make sure it exists");
                        }
                }, "json");
            }
        });
        
    });

    /**
     * future usage.
     * @param {type} z
     * @param {type} el
     * @returns {undefined}
     */
    var setZoom = function(z, el) {
        var p = ["-webkit-", "-moz-", "-ms-", "-o-", ""],
                s = "scale(" + z + ")";

        for (var i = 0; i < p.length; i++)
            el.css(p[i] + "transform", s);

        instance.setZoom(z);
    };

    function getItemMatchLabel(ruleFrom) {
        try {
            var actionItem = $('#ruletype_' + ruleFrom).jqxDropDownList('getItem', $('#ruletype_' + ruleFrom).jqxDropDownList('getSelectedIndex'));
            switch (actionItem.value) {
                case "simple":
                    return "one ";
                    break;
                case "or":
                    return "or ";
                    break;
                case "and":
                    return "and ";
                    break;
            }
        } catch (err) {

        }
    }

    function getBaseRuleMatchLabel() {
        try {
            var actionItem = $('#primaryrule').jqxDropDownList('getItem', $('#primaryrule').jqxDropDownList('getSelectedIndex'));
            switch (actionItem.value) {
                case "simple":
                    return "one ";
                    break;
                case "or":
                    return "or ";
                    break;
                case "and":
                    return "and ";
                    break;
            }
        } catch (err) {

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
        var deviceDivId = 'DIV_triggerExecItem_' + uniqueItemId;
        switch(returnData.itemType){
            case "device":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Set <strong>' + returnData.deviceCommandName + '</strong> of <strong>' + returnData.deviceName + '</strong> to:&nbsp; </span><div style="display:table-cell; vertical-align:middle"><div id="DIV_triggerExecItemValue_' + uniqueItemId +'"></div></div>' +
                                '<input type="hidden" name="execDeviceDeviceId_' + uniqueItemId+'" id="execDeviceDeviceId_' + uniqueItemId+'" value="' + returnData.deviceId + '" />' +
                                '<input type="hidden" name="execDeviceGroup_' + uniqueItemId+'" id="execDeviceGroup_' + uniqueItemId+'" value="' + returnData.deviceGroupId + '" />' +
                                '<input type="hidden" name="execDeviceCommand_' + uniqueItemId+'" id="execDeviceCommand_' + uniqueItemId+'" value="' + returnData.deviceCommandId + '" />' +
                                '<input type="hidden" name="execDeviceDeviceName_' + uniqueItemId+'" id="execDeviceDeviceName_' + uniqueItemId+'" value="' + returnData.deviceName + '" />' +
                                '<input type="hidden" name="execDeviceCommandName_' + uniqueItemId+'" id="execDeviceCommandName_' + uniqueItemId+'" value="' + returnData.deviceCommandName + '" />' +
                                '<input type="hidden" name="execDeviceCommandType_' + uniqueItemId+'" id="execDeviceCommandType_' + uniqueItemId+'" value="' + returnData.deviceCommandType + '" />' +
                                '<input type="hidden" name="execDeviceCommandValue_' + uniqueItemId+'" id="execDeviceCommandValue_' + uniqueItemId+'" value="" /></div>' + 
                                '<input type="hidden" name="execDeviceCommandExtra_' + uniqueItemId+'" id="execDeviceCommandValue_' + uniqueItemId+'" value="" /></div>';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#trigger_exec_list');
                $('#deleteExecItem_' + uniqueItemId).jqxButton({ width: '50', theme: siteSettings.getTheme()});
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    clearInternalWidgetHandlers('#DIV_triggerExecItem_' + uniqueItemId);
                    $('#DIV_triggerExecItem_' + uniqueItemId).empty();
                });
                switch(returnData.deviceCommandType){
                    case "button":
                        $.get('/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "DeviceService.getDeviceCommand", "params":{"id":' + returnData.deviceId + ', "groupid":"' + returnData.deviceGroupId + '", "commandid":"' + returnData.deviceCommandId + '"},"id":"DeviceService.getDeviceCommand"}')
                            .done(function(data) {
                                try {
                                    if(data.result.success !== true){
                                        showErrorMessage("Command details", data.result.message);
                                    } else {
                                        $('#DIV_triggerExecItemValue_' + uniqueItemId).html("Selected");
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
                                {name: 'id', type: 'string', map: 'value' },
                                {name: 'label', type: 'string', map: 'name' }
                            ],
                            url: '/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "DeviceService.getDeviceCommand", "params":{"id":' + returnData.deviceId + ', "groupid":"' + returnData.deviceGroupId + '", "commandid":"' + returnData.deviceCommandId + '"},"id":"DeviceService.getDeviceCommand"}',
                            root: 'result>data>commandset'
                        };
                        var dataDeviceCommandSetActionList = new $.jqx.dataAdapter(deviceCommandSetActionList);
                        $('#DIV_triggerExecItemValue_' + uniqueItemId).jqxDropDownList({source: dataDeviceCommandSetActionList, selectedIndex: 0, valueMember: "id", displayMember: "label", width: '150', theme: siteSettings.getTheme()});
                        $('#DIV_triggerExecItemValue_' + uniqueItemId).on("select", function(event){
                            var args = event.args;
                            if(args){
                                var item = args.item;
                                $('#execDeviceCommandValue_' + uniqueItemId).val(item.value);
                            }
                        });
                        $('#DIV_triggerExecItemValue_' + uniqueItemId).on('bindingComplete', function (event) { 
                            if(deviceCommandValue!==""){
                                $('#DIV_triggerExecItemValue_' + uniqueItemId).jqxDropDownList('selectIndex', $('#DIV_triggerExecItemValue_' + uniqueItemId).jqxDropDownList('getItemByValue', deviceCommandValue).index );
                                $('#execDeviceCommandValue_' + uniqueItemId).val(deviceCommandValue);
                            } else {
                                try {
                                    $('#execDeviceCommandValue_' + uniqueItemId).val($('#DIV_triggerExecItemValue_' + uniqueItemId).jqxDropDownList('getSelectedItem').value);
                                } catch (err){}
                            }
                            $('#DIV_triggerExecItemValue_' + uniqueItemId).off('bindingComplete');
                        });
                    break;
                    case "toggle":
                        $.get('/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "DeviceService.getDeviceCommand", "params":{"id":' + returnData.deviceId + ', "groupid":"' + returnData.deviceGroupId + '", "commandid":"' + returnData.deviceCommandId + '"},"id":"DeviceService.getDeviceCommand"}')
                            .done(function(data) {
                                try {
                                    if(data.result.success !== true){
                                        showErrorMessage("Command details", data.result.message);
                                    } else {
                                        var selectSet = [{"name":"On/True", "id":"true"},{"name":"Off/False", "id":"false"}];
                                        var deviceCommandSetActionList = {
                                            datatype: "json",
                                            datafields: [
                                                {name: 'id', type: 'string', map: 'id' },
                                                {name: 'label', type: 'string', map: 'name' }
                                            ],
                                            localdata: selectSet,
                                        };
                                        var dataDeviceCommandSetActionList = new $.jqx.dataAdapter(deviceCommandSetActionList);
                                        $('#DIV_triggerExecItemValue_' + uniqueItemId).jqxDropDownList({source: dataDeviceCommandSetActionList, selectedIndex: 0, valueMember: "id", displayMember: "label", width: '75', theme: siteSettings.getTheme()});
                                        if(deviceCommandValue!==""){
                                            $('#DIV_triggerExecItemValue_' + uniqueItemId).jqxDropDownList('selectIndex', $('#DIV_triggerExecItemValue_' + uniqueItemId).jqxDropDownList('getItemByValue', deviceCommandValue).index );
                                            $('#execDeviceCommandValue_' + uniqueItemId).val(deviceCommandValue);
                                        } else {
                                            try {
                                                $('#execDeviceCommandValue_' + uniqueItemId).val($('#DIV_triggerExecItemValue_' + uniqueItemId).jqxDropDownList('getSelectedItem').value);
                                            } catch (err){}
                                        }
                                        $('#DIV_triggerExecItemValue_' + uniqueItemId).on("select", function(event){
                                            var args = event.args;
                                            if(args){
                                                var item = args.item;
                                                $('#execDeviceCommandValue_' + uniqueItemId).val(item.value);
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

                                        $('#DIV_triggerExecItemValue_' + uniqueItemId).html('<div style="float:left;" id="DIV_triggerExecItemValuePickerDropDown_' + uniqueItemId +'"><div style="padding: 3px;"><div id="DIV_triggerExecItemValuePicker_' + uniqueItemId +'"></div></div></div><div style="float:left;" id="DIV_triggerExecItemValuePickerExecSelect_' + uniqueItemId +'"></div>');
                                        if(deviceCommandValue!==""){
                                            $('#DIV_triggerExecItemValuePicker_' + uniqueItemId).jqxColorPicker({ color: deviceCommandValue.substring(1), colorMode: 'hue', width: 220, height: 220, theme: siteSettings.getTheme()});
                                        } else {
                                            $('#DIV_triggerExecItemValuePicker_' + uniqueItemId).jqxColorPicker({ color: "000000", colorMode: 'hue', width: 220, height: 220, theme: siteSettings.getTheme()});
                                        }
                                        $('#DIV_triggerExecItemValuePicker_' + uniqueItemId).on('colorchange', function (event) {
                                            $('#DIV_triggerExecItemValuePickerDropDown_' + uniqueItemId).jqxDropDownButton('setContent', getTextElementByColor(event.args.color));
                                            $('#execDeviceCommandValue_' + uniqueItemId).val("#"+$('#DIV_triggerExecItemValuePicker_' + uniqueItemId).jqxColorPicker('getColor').hex);
                                        });
                                        $('#DIV_triggerExecItemValuePickerDropDown_' + uniqueItemId).jqxDropDownButton({ width: 225, height: 22, theme: siteSettings.getTheme()});
                                        $('#DIV_triggerExecItemValuePickerDropDown_' + uniqueItemId).jqxDropDownButton('setContent', getTextElementByColor($('#DIV_triggerExecItemValuePicker_' + uniqueItemId).jqxColorPicker('getColor')));

                                        var deviceCommandSetActionList = {
                                            datatype: "json",
                                            datafields: [
                                                {name: 'value', type: 'string', map: 'value' },
                                                {name: 'label', type: 'string', map: 'label' }
                                            ],
                                            localdata: data.result.data.typedetails.commandset,
                                        };
                                        var dataDeviceCommandSetActionList = new $.jqx.dataAdapter(deviceCommandSetActionList);
                                        $('#DIV_triggerExecItemValuePickerExecSelect_' + uniqueItemId).jqxDropDownList({source: dataDeviceCommandSetActionList, selectedIndex: 0, valueMember: "value", displayMember: "label", width: '150', theme: siteSettings.getTheme()});
                                        $('#DIV_triggerExecItemValuePickerExecSelect_' + uniqueItemId).on("select", function(){
                                            $('#execDeviceCommandValue_' + uniqueItemId).val("#"+$('#DIV_triggerExecItemValuePicker_' + uniqueItemId).jqxColorPicker('getColor').hex);
                                            $('#execDeviceCommandExtra_' + uniqueItemId).val($('#DIV_triggerExecItemValuePickerExecSelect_' + uniqueItemId).jqxDropDownList('getItem', $('#DIV_triggerExecItemValuePickerExecSelect_' + uniqueItemId).jqxDropDownList('getSelectedIndex')).value);
                                        });
                                        if(returnData.deviceCommandExtra!==undefined){
                                            $('#DIV_triggerExecItemValuePickerExecSelect_' + uniqueItemId).jqxDropDownList('selectIndex', $('#DIV_triggerExecItemValuePickerExecSelect_' + uniqueItemId).jqxDropDownList('getItemByValue', returnData.deviceCommandExtra).index);
                                            $('#execDeviceCommandExtra_' + uniqueItemId).val($('#DIV_triggerExecItemValuePickerExecSelect_' + uniqueItemId).jqxDropDownList('getItem', $('#DIV_triggerExecItemValuePickerExecSelect_' + uniqueItemId).jqxDropDownList('getSelectedIndex')).value);
                                        }
                                        $('#execDeviceCommandValue_' + uniqueItemId).val("#"+$('#DIV_triggerExecItemValuePicker_' + uniqueItemId).jqxColorPicker('getColor').hex);
                                    }
                                } catch(err){
                                    showErrorMessage("Error: ", err);
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
                                        $('#DIV_triggerExecItemValue_' + uniqueItemId).html('<div style="float:left;" id="DIV_triggerExecItemSlider_' + uniqueItemId +'"></div><div style="float:left; border: 0; color: #f6931f; font-weight: bold; background-color: transparent; cursor:pointer; margin-top: 4px; margin-left: 10px;" id="DIV_triggerExecItemSliderValue_' + uniqueItemId +'"></div>');
                                        $( '#DIV_triggerExecItemSlider_' + uniqueItemId).jqxSlider({ showButtons: false, height: 30, min: parseInt(data.result.data.typedetails.min), max: parseInt(data.result.data.typedetails.max), step: 1, ticksFrequency: 10, mode: 'fixed', width: 200, theme: siteSettings.getTheme() });
                                        $( '#DIV_triggerExecItemSlider_' + uniqueItemId).on('change', function (event) { 
                                            $('#DIV_triggerExecItemSliderValue_' + uniqueItemId).html(event.args.value);
                                        });
                                        $( '#DIV_triggerExecItemSlider_' + uniqueItemId).on('slideEnd', function (event) { 
                                            $('#execDeviceCommandValue_' + uniqueItemId).val(event.args.value );
                                            $('#DIV_triggerExecItemSliderValue_' + uniqueItemId).html(event.args.value );
                                        });
                                        if(deviceCommandValue!==""){
                                            $('#DIV_triggerExecItemSlider_' + uniqueItemId).jqxSlider('setValue', parseInt(deviceCommandValue));
                                            $('#DIV_triggerExecItemSliderValue_' + uniqueItemId).html(deviceCommandValue);
                                            $('#execDeviceCommandValue_' + uniqueItemId).val(deviceCommandValue);
                                        }
                                    }
                                } catch(err){
                                    showErrorMessage("Error: ", err);
                                }
                            }, "json");
                    break;
                }
            break;
            case "userstatus":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Set user status to</span>' +
                                '<div style="display:table-cell; vertical-align:middle"><div id="execUserstatusId_' + uniqueItemId+'"></div></div></div>';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#trigger_exec_list');
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
                    $('#DIV_triggerExecItem_' + uniqueItemId).empty();
                });
            break;
            case "presence":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Set presence to</span>' +
                                '<div style="display:table-cell; vertical-align:middle"><div id="execPresenceId_' + uniqueItemId+'"></div></div></div>';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#trigger_exec_list');
                $('#execPresenceId_' + uniqueItemId).jqxDropDownList({source: allPresenceOptionsList, valueMember: "id", displayMember: "name", width: '100', theme: siteSettings.getTheme()});
                $('#execPresenceId_' + uniqueItemId).on('bindingComplete', function (event) { 
                    try {
                        $('#execPresenceId_' + uniqueItemId).jqxDropDownList('selectIndex', $('#execPresenceId_' + uniqueItemId).jqxDropDownList('getItemByValue', returnData.presenceid).index );
                    } catch(err){}
                    $('#execPresenceId_' + uniqueItemId).off('bindingComplete');
                });
                $('#deleteExecItem_' + uniqueItemId).jqxButton({ width: '50', theme: siteSettings.getTheme()});
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    clearInternalWidgetHandlers('#DIV_triggerExecItem_' + uniqueItemId);
                    $('#DIV_triggerExecItem_' + uniqueItemId).empty();
                });
            break;
            case "daypart":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Set part of day to</span>' +
                                '<div style="display:table-cell; vertical-align:middle"><div id="execDaypartId_' + uniqueItemId+'"></div></div></div>';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#trigger_exec_list');
                $('#execDaypartId_' + uniqueItemId).jqxDropDownList({source: allDaypartsOptionsList, valueMember: "id", displayMember: "name", width: '100', theme: siteSettings.getTheme()});
                $('#execDaypartId_' + uniqueItemId).on('bindingComplete', function (event) { 
                    try {
                        $('#execDaypartId_' + uniqueItemId).jqxDropDownList('selectIndex', $('#execDaypartId_' + uniqueItemId).jqxDropDownList('getItemByValue', returnData.daypartid).index );
                    } catch(err){}
                    $('#execDaypartId_' + uniqueItemId).off('bindingComplete');
                });
                $('#deleteExecItem_' + uniqueItemId).jqxButton({ width: '50', theme: siteSettings.getTheme()});
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    clearInternalWidgetHandlers('#DIV_triggerExecItem_' + uniqueItemId);
                    $('#DIV_triggerExecItem_' + uniqueItemId).empty();
                });
            break;
            case "macro":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Run macro <strong>' + returnData.macroName + '</strong></span></div>' +
                                '<input type="hidden" name="execMacroId_' + uniqueItemId+'" id="execMacroId_' + uniqueItemId+'" value="' + returnData.macroId + '" />' +
                                '<input type="hidden" name="execMacroName_' + uniqueItemId+'" id="execMacroName_' + uniqueItemId+'" value="' + returnData.macroName + '" />';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#trigger_exec_list');
                $('#deleteExecItem_' + uniqueItemId).jqxButton({ width: '50', theme: siteSettings.getTheme()});
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    clearInternalWidgetHandlers('#DIV_triggerExecItem_' + uniqueItemId);
                    $('#DIV_triggerExecItem_' + uniqueItemId).empty();
                });
            break;
            case "messengerplugin":
                var deviceHtml = '<div style="display:table;"><button id="deleteExecItem_' + uniqueItemId + '">Delete</button>&nbsp;<span style="display:table-cell; vertical-align:middle; min-width:375px;">Send '+returnData.typeName+': </span></div>' +
                                 '<input type="hidden" name="execMessageType_' + uniqueItemId+'" id="execMessageType_' + uniqueItemId+'" value="' + returnData.messageType + '" />' +
                                 '<input type="hidden" name="execMessageTypeName_' + uniqueItemId+'" id="execMessageTypeName_' + uniqueItemId+'" value="' + returnData.typeName + '" />' +
                                 '<input type="text" name="execMessageMessage_' + uniqueItemId+'" id="execMessageMessage_' + uniqueItemId+'" maxlength="132" value="' + returnData.message + '" />';
                $('<div>', {id: deviceDivId}).html(deviceHtml).appendTo('#trigger_exec_list');
                $('#deleteExecItem_' + uniqueItemId).jqxButton({ width: '50', theme: siteSettings.getTheme()});
                $('#deleteExecItem_' + uniqueItemId).on('click', function () {
                    clearInternalWidgetHandlers('#DIV_triggerExecItem_' + uniqueItemId);
                    $('#DIV_triggerExecItem_' + uniqueItemId).empty();
                });
            break;
        }
    }


    $("#save_trigger").jqxButton({width: '100', theme: siteSettings.getTheme()});
    $("#save_trigger").on('click', function() {
        var ruleCollection = new Array();
        var execCollection = new Array();
        var ruleCounter = 0;
        var execCounter = 0;
        var inError = false;
        if(inputFieldValid($('#trigger_name')) && inputFieldValid($('#trigger_description')) && $('#trigger_reccurrence').jqxDropDownList('getSelectedIndex')!==-1){
            $('#editor>').each(function() {
                if ($(this).attr("id") !== undefined && $(this).attr("id").startsWith('DIV_ruleid_')) {
                    var baseRuleId = $(this).attr("id").split("_")[2];
                    var ruleset = new Array();
                    $('#editor>').each(function() {
                        if ($(this).attr("id") !== undefined && $(this).attr("id").startsWith('DIV_triggerMatchItem_' + baseRuleId)) {
                            var itemtoWorkWith = $(this).attr("id").split("_")[3];
                            if($("#triggerDeviceDeviceId_" + itemtoWorkWith).length!==0){
                                if(inputFieldValid($("#triggerValue_" + itemtoWorkWith))){
                                    var useData;
                                    if($("#triggerDeviceCommandDataType_" + itemtoWorkWith).val()!==undefined && $("#triggerDeviceCommandDataType_" + itemtoWorkWith).val()==="boolean"){
                                        useData = $('#triggerValue_' + itemtoWorkWith).jqxDropDownList('getItem', $('#triggerValue_' + itemtoWorkWith).jqxDropDownList('getSelectedIndex')).value;
                                    } else {
                                        try {
                                            useData = parseInt($("#triggerValue_" + itemtoWorkWith).val());
                                        } catch(err) {
                                            useData = $("#triggerValue_" + itemtoWorkWith).val();
                                        }
                                    }
                                    var item = {
                                        "itemtype"   : "device",
                                        "deviceid"   : $("#triggerDeviceDeviceId_" + itemtoWorkWith).val(),
                                        "group"      : $("#triggerDeviceGroup_" + itemtoWorkWith).val(),
                                        "command"    : $("#triggerDeviceCommand_" + itemtoWorkWith).val(),
                                        "matchtype"  : $('#triggerValueType_' + itemtoWorkWith).jqxDropDownList('getItem', $('#triggerValueType_' + itemtoWorkWith).jqxDropDownList('getSelectedIndex')).value,
                                        "matchvalue" : useData,
                                        "datatype"   : $("#triggerDeviceCommandDataType_" + itemtoWorkWith).val(),
                                        "prefix"     : $("#triggerDeviceCommandPrefix_" + itemtoWorkWith).val(),
                                        "suffix"     : $("#triggerDeviceCommandSuffix_" + itemtoWorkWith).val(),
                                        "commandname": $("#triggerDeviceCommandName_" + itemtoWorkWith).val(),
                                        "devicename" : $("#triggerDeviceDeviceName_" + itemtoWorkWith).val()
                                    };
                                    ruleset.push(item);
                                    ruleCounter++;
                                } else {
                                    inError = true;
                                    showErrorMessage("Device value error", "Please use a correct value for "+$("#triggerDeviceCommandName_" + itemtoWorkWith).val()+" ("+ $("#triggerDeviceDeviceName_" + itemtoWorkWith).val() + ")");
                                }
                            } else if ($("#triggerDaytimeReccurrence_" + itemtoWorkWith).length!==0){
                                if(inputFieldValid($("#triggerValue_" + itemtoWorkWith))){
                                    var item = {
                                        "itemtype"      : "daytime",
                                        "occurrence"    : $("#triggerDaytimeReccurrence_" + itemtoWorkWith).val(),
                                        "timetype"      : $("#triggerDayTimeDaytime_" + itemtoWorkWith).val(),
                                        "timemod"       : (($("#triggerTimeValueTypeCalc_" + itemtoWorkWith).length != 0)?$("#triggerTimeValueTypeCalc_" + itemtoWorkWith).jqxDropDownList('getItem', $('#triggerTimeValueTypeCalc_' + itemtoWorkWith).jqxDropDownList('getSelectedIndex')).value:""),
                                        "matchtype"     : $('#triggerValueType_' + itemtoWorkWith).jqxDropDownList('getItem', $('#triggerValueType_' + itemtoWorkWith).jqxDropDownList('getSelectedIndex')).value,
                                        "matchvalue"    : $("#triggerValue_" + itemtoWorkWith).val(),
                                        "occurrencename": $("#triggerDaytimeReccurrenceName_" + itemtoWorkWith).val(),
                                        "timetypename"  : $("#triggerDayTimeDaytimeName_" + itemtoWorkWith).val()
                                    };
                                    ruleset.push(item);
                                    ruleCounter++;
                                } else {
                                    inError = true;
                                    showErrorMessage("Time value error", "Please use a correct value for a daytime value");
                                }
                            } else if ($("#triggerPresenceItem_" + itemtoWorkWith).length!==0){
                                var item = {
                                    "itemtype"      : "presence",
                                    "matchtype"     : $('#triggerValueType_' + itemtoWorkWith).jqxDropDownList('getItem', $('#triggerValueType_' + itemtoWorkWith).jqxDropDownList('getSelectedIndex')).value,
                                    "matchvalue"    : parseInt($("#triggerValue_" + itemtoWorkWith).val()),
                                };
                                ruleset.push(item);
                                ruleCounter++;
                            } else if ($("#triggerDaypartItem_" + itemtoWorkWith).length!==0){
                                var item = {
                                    "itemtype"      : "daypart",
                                    "matchtype"     : $('#triggerValueType_' + itemtoWorkWith).jqxDropDownList('getItem', $('#triggerValueType_' + itemtoWorkWith).jqxDropDownList('getSelectedIndex')).value,
                                    "matchvalue"    : parseInt($("#triggerValue_" + itemtoWorkWith).val()),
                                };
                                ruleset.push(item);
                                ruleCounter++;
                            } else if ($("#triggerUserstatusItem_" + itemtoWorkWith).length!==0){
                                var item = {
                                    "itemtype"      : "userstatus",
                                    "matchtype"     : $('#triggerValueType_' + itemtoWorkWith).jqxDropDownList('getItem', $('#triggerValueType_' + itemtoWorkWith).jqxDropDownList('getSelectedIndex')).value,
                                    "matchvalue"    : parseInt($("#triggerValue_" + itemtoWorkWith).val()),
                                };
                                ruleset.push(item);
                                ruleCounter++;
                            } else if ($("#triggerMediaPluginMediaItem_" + itemtoWorkWith).length!==0){
                                var item = {
                                    "itemtype"            : "mediaplugin",
                                    "matchtype"           : $('#triggerValueType_' + itemtoWorkWith).jqxDropDownList('getItem', $('#triggerValueType_' + itemtoWorkWith).jqxDropDownList('getSelectedIndex')).value,
                                    "matchvalue"          : $("#triggerValue_" + itemtoWorkWith).val(),
                                    "mediaId"             : $("#triggerMediaPluginMediaId_" + itemtoWorkWith).val(),
                                    "mediaName"           : $("#triggerMediaPluginItemMediaName_" + itemtoWorkWith).val(),
                                    "mediaCommandTypeId"  : $("#triggerMediaPluginItemCommandTypeId_" + itemtoWorkWith).val(),
                                    "mediaCommandTypeName": $("#triggerMediaPluginItemCommandTypeName_" + itemtoWorkWith).val()
                                };
                                ruleset.push(item);
                                ruleCounter++;
                            }
                        }
                    });
                    var ruleBaseSet = { 
                        "matchtype" : $('#ruletype_' + baseRuleId).jqxDropDownList('getItem', $('#ruletype_' + baseRuleId).jqxDropDownList('getSelectedIndex')).value,
                        "collection": ruleset
                    };
                    ruleCollection.push(ruleBaseSet);
                }
            });
            $('#trigger_exec_list>').each(function() {
                if ($(this).attr("id") !== undefined && $(this).attr("id").startsWith('DIV_triggerExecItem_')) {
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
                            "value"      : $("#execDeviceCommandValue_" + workWith).val()
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
                            "presenceid": parseInt($("#execPresenceId_" + workWith).val())
                        };
                        execCollection.push(item);
                        execCounter++;
                    } else if ($('#execDaypartId_' + workWith).length!==0) {
                        var item = {
                            "itemtype" : "daypart",
                            "daypartid": parseInt($("#execDaypartId_" + workWith).val())
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
                    } else if ($('#execMessageType_' + workWith).length!==0) {
                        var item = {
                            "itemtype" : "messengerplugin",
                            "type"     : $("#execMessageType_" + workWith).val(),
                            "message"  : $("#execMessageMessage_" + workWith).val(),
                            "typename" : $("#execMessageTypeName_" + workWith).val(),
                        };
                        execCollection.push(item);
                        execCounter++;
                    }
                }
            });
            if(inError===false && ruleCounter===0){
                showErrorMessage("Trigger error", "Please use a minimum of one added rule");
            } else if(inError===false && execCounter===0){
                showErrorMessage("Trigger error", "Please use a minimum of one execution item");
            } else if(inError===true) {
                /// do nothing and keep message
            } else {
                var triggerId = $("#trigger_id").val();
                if(triggerId!==undefined && triggerId!==0 && triggerId!==""){
                    var params = { "id"          : parseInt(triggerId),
                                   "name"        : $('#trigger_name').val(), 
                                   "description" : $('#trigger_description').val(),
                                   "reccurrence" : $('#trigger_reccurrence').jqxDropDownList('getItem', $('#trigger_reccurrence').jqxDropDownList('getSelectedIndex')).value,
                                   "rulesmatch"  : $('#primaryrule').val(),
                                   "rules"       : ruleCollection,
                                   "executions"  : execCollection
                    };
                    var rpcCommand = { "jsonrpc"     : "2.0",
                                       "method"      : "TriggerService.updateTrigger",
                                       "params"      : params,
                                       "id"          : "TriggerService.updateTrigger"};
                } else {
                    var params = { "name"        : $('#trigger_name').val(), 
                                   "description" : $('#trigger_description').val(),
                                   "reccurrence" : $('#trigger_reccurrence').jqxDropDownList('getItem', $('#trigger_reccurrence').jqxDropDownList('getSelectedIndex')).value,
                                   "rulesmatch"  : $('#primaryrule').val(),
                                   "rules"       : ruleCollection,
                                   "executions"  : execCollection
                    };
                    var rpcCommand = { "jsonrpc"     : "2.0",
                                       "method"      : "TriggerService.saveTrigger",
                                       "params"      : params,
                                       "id"          : "TriggerService.saveTrigger"};
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
                                showErrorMessage("Trigger error: " + data.result.error.message, message);
                            } else {
                                showInfoMessage("Trigger saved", "Trigger has been saved");
                                refreshPageContent('/triggers.html');
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
            showErrorMessage("Trigger save error", "Make sure you use a correct trigger name, description and recurrence.");
        }
    });

    $("#reset_trigger").jqxButton({width: '100', theme: siteSettings.getTheme()});
    $("#reset_trigger").on('click', function() {
        $('#editor>').each(function() {
            if ($(this).attr("id") !== undefined && ($(this).attr("id").startsWith('DIV_ruleid_') || $(this).attr("id").startsWith('DIV_triggerMatchItem_') || $(this).attr("id").startsWith('DIV_triggerMatchRuleItem_'))) {
                instance.detachAllConnections($(this).attr("id"));
                instance.removeAllEndpoints($(this).attr("id"));
                $(this).remove();
            }
        });
    });

    $("#add_exec").jqxButton({width: '100', theme: siteSettings.getTheme()});
    $("#add_exec").on('click', function() {
        itemSelectionModal.setCallBack(function(returnData){
            createExecItems(returnData);
        });
        itemSelectionModal.setOptions({"deviceFilter": ["!data"]});
        itemSelectionModal.setSelectionType("exec");
        itemSelectionModal.open();
    });
    
    $("#clear_exec").jqxButton({width: '100', theme: siteSettings.getTheme()});
    $("#clear_exec").on('click', function() {
        clearInternalWidgetHandlers("#trigger_exec_list");
        $("#trigger_exec_list").empty();
    });
