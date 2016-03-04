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

var globalDevice;

function jsonDevice(){
    return globalDevice;
}

/**
 * Generates a device id from device name.
 * @param {type} content
 * @returns {undefined}
 */
function generateSimpleDeviceId(content){
    var text = content.replace(/[^a-z0-9]/gi,'').toLowerCase();
    $("#visual_name-gen").val(text);
    $("#name").val(text);
}

$( document ).ready(function() {
    
    $( "#vis-selectComponents li" ).draggable({
      cursor: "move",
      helper: "clone",
      distance: 30,
      revert: "invalid",
      zIndex: 1000
    });
   
    $("#visual_name").keyup(function(){
        generateSimpleDeviceId($(this).val());
    });
    $("#visual_name").change(function(){
        //var test = testCharacterLength($("#visual_name-gen"), 5, "The generated device id must be at least 5 characters long, please edit the device name so the generated name is 5 characters.");
        //if(test){
            globalDevice["name"] = $("#visual_name").val();
        //}
    });
    $("#visual_description").keyup(function(){
        globalDevice["description"] = $(this).val();
    });
    
    $("#devicecontent").droppable({
         accept: "#vis-addGroup",
         drop: function(event, ui) {
             switch(ui.draggable.attr("id")){
                 case "vis-addGroup":
                     createDeviceGroup(null, "Set group name");
                 break;
             }
         }
    });
    
    $("#vis-add-input-option").button({icons: { primary: "ui-icon-plus" }}).click(function() {
        try {
            addOptionsNodeIfnotExists();
            var option = {"id"          : "inputoptionid" + globalDevice.options.length, 
                          "label"       : "Input label",
                          "type"        : "text",
                          "datatype"    : "string",
                          "order"       : 0,
                          "description" : "Describe the requested selection",
                         };
            globalDevice.options.push(option);
            addDefaultOptionSet(globalDevice.options.length-1, option);
        } catch (err){ alert(err); }
        return false;
    });

    $("#vis-add-select-option").button({icons: { primary: "ui-icon-plus" }}).click(function() {
        try {
            addOptionsNodeIfnotExists();
            var option = {"id"          : "selectoptionid" + globalDevice.options.length, 
                          "label"       : "Select label",
                          "type"        : "select",
                          "datatype"    : "string",
                          "order"       : 0,
                          "description" : "Describe the requested selection",
                          "selectset"   : [ { "label" : "select label", "value" : "select value"} ]
                         };
            globalDevice.options.push(option);
            addDefaultOptionSet(globalDevice.options.length-1, option);
        } catch (err){ alert(err); }
        return false;
    });
    
    /**
     * Checks if the options node exists and if not preset first asked tag.
     * If the options tag does not exist it is created and the array is
     * automatically filled with the given type with the needed array.
     * @returns {undefined}
     */
    function addOptionsNodeIfnotExists(){
        if(typeof globalDevice.options === "undefined"){
            globalDevice.options = new Array();
        }
    }
    
});

function createEmptyDevice(){
    if(typeof globalDevice === "undefined"){
        globalDevice = {"address": {
                    "input": {
                        "datatype": "string",
                        "description": "Address",
                        "type": "text"
                    },
                    "description": "A description which will be shown when an address is needed to fill in"
                },
                "options": [],
                "name": "A new device",
                "description": "A description for this new device",
                "controlset": {
                    "groups": [{
                                "id": "newgroupid0",
                                "label": "Click the wrench to edit the group, drop controls on it to add them.",
                                "controls": []
                            }]
                }};
        createDeviceComponents(globalDevice);
    }
}

/**
 * Builds the device components
 * @param {type} data
 * @returns {unresolved}
 */
function createDeviceComponents(data){
    $("#vis-options-data").empty();
    var device = data;
    $("#visual_name").val(device.name);
    generateSimpleDeviceId($("#visual_name").val());
    $("#visual_description").val(device.description);
    try {
        $("#visual_address-label").on("keyup", function(){
            device.address.input.description = $("#visual_address-label").val();
        });
        $("#visual_address_description").on("keyup", function(){
            device.address.description = $("#visual_address_description").val();
        });

        $("#visual_address-label").val(device.address.input.description);
        $("#visual_address_description").val(device.address.description);
        $("#visual_location_type").val(device.address.input.datatype);
    } catch (err){
        $("#visual_location_type").val("false");
        $("#visual_address_description").attr("disabled", "disabled");
        $("#visual_address-label").attr("disabled", "disabled");
    }
    for(var groupid = 0; groupid < device.controlset.groups.length; groupid++){
        var group = device.controlset.groups[groupid];
        createDeviceGroup(groupid, group);
        createDeviceGroupControls(groupid, group);
    }

    $("#visual_location_type").on("change", function(){
        if($("#visual_location_type").val()==="false"){
            $("#visual_address_description").attr("disabled", "disabled");
            $("#visual_address-label").attr("disabled", "disabled");
            try {
                delete device.address;
            } catch (err){}
        } else {
            $("#visual_address_description").removeAttr("disabled");
            $("#visual_address-label").removeAttr("disabled");
            device.address = { "input": { "description":$("#visual_address-label").val(),
                                          "datatype"   :$("#visual_location_type").val() 
                                        },
                                        "description" : $("#visual_address_description").val()
                                   };
        }
    });
    createDeviceOptions(device);
    globalDevice = device;
    return device;
}

/**
 * Creates the device options.
 * @param {type} device
 * @returns {undefined}
 */
function createDeviceOptions(device){
    if(typeof device.options !== "undefined"){
        for(var optionId in device.options){
            var option = device.options[optionId];
            switch(option.type){
                case "text":
                    addDefaultOptionSet(optionId, option);
                break;
                case "select":
                    addDefaultOptionSet(optionId, option);
                break;
            }
        }
    }
}

/**
 * Adds default option sets to div.
 * @param {type} selectIndex
 * @param {type} option
 * @returns {undefined}
 */
function addDefaultOptionSet(selectIndex, option){
    var unique = createUUID();
    $('#vis-options-data').append('<div id="option_'+unique+'" style="width:100%; float:left; clear:left; border-top:1px solid #d56909; margin-top:10px; padding-top: 3px;">\n\
    <div class="nvp" style="float:left; width:400px; clear:none;">\n\
        <div class="n">Option type</div>\n\
        <div class="v">'+(option.type==="text"?"Input field":"Selection")+'</div>\n\
        <div class="n">Option id</div>\n\
        <div class="v"><input type="text" name="option_'+unique+'_id" id="option_'+unique+'_id" value="'+option.id+'" /></div>\n\
        <div class="n">Option label</div>\n\
        <div class="v"><input type="text" name="option_'+unique+'_label" id="option_'+unique+'_label" value="'+option.label+'" /></div>\n\
        <div class="n">Option data type</div>\n\
        <div class="v"><input type="text" name="option_'+unique+'_datatype" id="option_'+unique+'_datatype" value="'+option.datatype+'" /></div>\n\
        <div class="n">Option order</div>\n\
        <div class="v"><input type="text" name="option_'+unique+'_order" id="option_'+unique+'_order" value="'+option.order+'" /></div>\n\
    </div>\n\
    <div style="float:right;cursor:pointer;width:16px;height:16px;"><span style="float:left;" id="option_'+unique+'_delete"><img src="/shared/images/deviceeditor/circle-x-2x.png" class="groupEditAction" alt="Remove option"/></div>\n\
    <div class="nvp" style="float:left; width:600px;">\n\
        <div class="n">Option help text</div>\n\
        <div class="v"><textarea name="option_'+unique+'_description" id="option_'+unique+'_description" rows="5" cols="57">'+option.description+'</textarea></div>\n\
        <div class="n">Option Selection</div>\n\
        <div class="v" style="width:400px;">'+(option.type==="text"?"The text control has no extra options, it is an user input field":
            "<button class=\"btn btn-info\" id=\"option_select_button_"+unique+"\">Set select options</button>"
        )+'</div>\n\
    </div>\n\
</div>');
    $('#option_'+unique+'_id').on("keydown", function(e){
        return e.which !== 32;
    });
    $('#option_'+unique+'_id').on("keyup", function(){
        globalDevice.options[selectIndex].id = $(this).val();
    });
    $('#option_'+unique+'_label').on("keyup", function(){
        globalDevice.options[selectIndex].label = $(this).val();
    });
    $('#option_'+unique+'_datatype').on("keyup", function(){
        globalDevice.options[selectIndex].datatype = $(this).val();
    });
    $('#option_'+unique+'_order').on("keyup", function(){
        globalDevice.options[selectIndex].order = parseInt($(this).val());
    });
    $('#option_'+unique+'_description').on("keyup", function(){
        globalDevice.options[selectIndex].description = $('#option_'+unique+'_description').val();
    });
    
    $('#option_'+unique+'_delete').button().click(function() {
        $(this).parent().parent().remove();
        globalDevice.options.splice(selectIndex, 1);
        /// After deletion rebuild the options because of index issues
        $("#vis-options-data").empty();
        createDeviceOptions(globalDevice);
        return false;
    });
    if(option.type==="select"){
        $("#option_select_button_"+unique).button({icons: { primary: "ui-icon-gear" }}).click(function() {
            runSelectOptionEditor(selectIndex);
            return false;
        });
    }
}

/**
 * Opens the select option selection options editor and fills the list with the current select options.
 * @param {type} selectIndex
 * @returns {undefined}
 */
function runSelectOptionEditor(selectIndex){
    $('#optionselectlist').empty();
    for(var i=0; i < globalDevice.options[selectIndex].selectset.length; i++){
        var buttonData = globalDevice.options[selectIndex].selectset[i];
        createOptionSelectButton(buttonData.label, buttonData.value);
    }
    $('#dialog-optionselect').jqxWindow("open");
    $('#optionselect-ok').off("click").on("click", function () { 
        globalDevice.options[selectIndex].selectset = new Array();
        $("#optionselectlist .cpickereditbutton").each(function(){
            globalDevice.options[selectIndex].selectset.push(
                {
                    "label":$('#' + $(this).attr("id") + ' input[data-buttonvis="label"]').val(),
                    "value":$('#' + $(this).attr("id") + ' input[data-buttonvis="value"]').val()
                }
            );
        });
        $('#dialog-optionselect').jqxWindow('close');
    });
}

/**
 * Creates the select options select option.
 * @param {type} label
 * @param {type} value
 * @returns {undefined}
 */
function createOptionSelectButton(label, value){
    var randId = createUUID();
    var bhtml ='<div class="cpickereditbutton" id="editselectbutton_'+randId+'_holder" style="clear:both;">\n\
                    <div style="float:right;cursor:pointer;"><span style="float:left;" id="editselectbutton_'+randId+'_delete"><img src="/shared/images/deviceeditor/circle-x-2x.png" class="groupEditAction" alt="Remove button"/></div>\n\
                    <div style="float:left;">Select label</div>\n\
                    <div style="clear:both;float:left;"><input type="text" data-buttonvis="label" name="editselectbutton_'+randId+'_label" id="editselectbutton_'+randId+'_label" value="'+label+'" /></div>\n\
                    <div style="clear:both;float:left;">Select value</div>\n\
                    <div style="clear:both;float:left;"><input type="text" data-buttonvis="value" name="editselectbutton_'+randId+'_value" id="editselectbutton_'+randId+'_value" value="'+value+'" /></div>\n\
                <div style="clear:both;"></div></div>';

    $('#optionselectlist').append(bhtml);
    $('#editselectbutton_'+randId+'_delete').on("click", function(){
        $(this).parent().parent().remove();
    });
}

/**
 * It's always nice to ask before deleting.
 * @param {type} item
 * @param {type} callBack
 * @returns {undefined}
 */
function askForDeletion(item, callBack){
    $('#delete-ok').off("click").on("click", function () { $('#dialog-deleteconfirm').modal('hide'); callBack(); });
    $('#delete-cancel').off("click").on("click", function (){ $('#dialog-deleteconfirm').modal('hide'); });
    $('#delete-itemname').html(item);
    $('#dialog-deleteconfirm').modal('show');
}


//##############################################################################
/*
 * Device control actions
 */

/**
 * Adds a control to the group list.
 * @param {type} groupIndex
 * @param {type} controlIndex
 * @param {type} controlType
 * @param {type} controlName
 * @returns The unique created id to identify the control.
 */
function addControl(groupIndex, controlIndex, controlType, controlName){
    var unique = createUUID();
    var html = '<div id="control_'+groupIndex+'_'+unique+'" style="padding-left:10px;padding-bottom:4px;">\n\
                <span style="float:left;cursor:pointer;" id="editcontrol_'+groupIndex+'_'+unique+'_delete"><img src="/shared/images/deviceeditor/circle-x-2x.png" class="controlDeleteAction" alt="Delete control"/></span>\n\
                <span style="float:left;cursor:pointer;margin-left:5px;" id="editcontrol_'+groupIndex+'_'+unique+'_edit" class="dataControlEditAction" ><img src="/shared/images/deviceeditor/wrench-2x.png" alt="Edit control"/></span><span style="margin-left:5px;">';
    var script = '<script>';
    switch(controlType){
        case "data":
            html += 'Data field: <span id="editcontrol_'+groupIndex+'_'+unique+'_name">'+controlName+"</span>";
            script += '$("#editcontrol_'+groupIndex+'_'+unique+'_edit").on("click", function (){\n\
                            doDataControlEdit('+groupIndex+', '+controlIndex+', "'+unique+'");\n\
                       });';
            script += '$("#editcontrol_'+groupIndex+'_'+unique+'_delete").on("click", function (){\n\
                            askForDeletion($("#editcontrol_'+groupIndex+'_'+unique+'_name").html(), function(){\n\
                                globalDevice.controlset.groups['+groupIndex+'].controls.splice('+controlIndex+', 1);\n\
                                $("#devicecontent").empty();\n\
                                createDeviceComponents(globalDevice);\n\
                            });\n\
                        });';
        break;
        case "select":
            html += 'Select field: <span id="editcontrol_'+groupIndex+'_'+unique+'_name">'+controlName+"</span>";
            script += '$("#editcontrol_'+groupIndex+'_'+unique+'_edit").on("click", function (){\n\
                            doSelectControlEdit('+groupIndex+', '+controlIndex+', "'+unique+'");\n\
                       });';
            script += '$("#editcontrol_'+groupIndex+'_'+unique+'_delete").on("click", function (){\n\
                            askForDeletion($("#editcontrol_'+groupIndex+'_'+unique+'_name").html(), function(){\n\
                                globalDevice.controlset.groups['+groupIndex+'].controls.splice('+controlIndex+', 1);\n\
                                $("#devicecontent").empty();\n\
                                createDeviceComponents(globalDevice);\n\
                            });\n\
                        });';
        break;
        case "button":
            html += 'Single action button: <span id="editcontrol_'+groupIndex+'_'+unique+'_name">'+controlName+"</span>";
            script += '$("#editcontrol_'+groupIndex+'_'+unique+'_edit").on("click", function (){\n\
                            doButtonControlEdit('+groupIndex+', '+controlIndex+', "'+unique+'");\n\
                       });';
            script += '$("#editcontrol_'+groupIndex+'_'+unique+'_delete").on("click", function (){\n\
                            askForDeletion($("#editcontrol_'+groupIndex+'_'+unique+'_name").html(), function(){\n\
                                globalDevice.controlset.groups['+groupIndex+'].controls.splice('+controlIndex+', 1);\n\
                                $("#devicecontent").empty();\n\
                                createDeviceComponents(globalDevice);\n\
                            });\n\
                        });';
        break;
        case "toggle":
            html += 'Toggle button: <span id="editcontrol_'+groupIndex+'_'+unique+'_name">'+controlName+"</span>";
            script += '$("#editcontrol_'+groupIndex+'_'+unique+'_edit").on("click", function (){\n\
                            doToggleControlEdit('+groupIndex+', '+controlIndex+', "'+unique+'");\n\
                       });';
            script += '$("#editcontrol_'+groupIndex+'_'+unique+'_delete").on("click", function (){\n\
                            askForDeletion($("#editcontrol_'+groupIndex+'_'+unique+'_name").html(), function(){\n\
                                globalDevice.controlset.groups['+groupIndex+'].controls.splice('+controlIndex+', 1);\n\
                                $("#devicecontent").empty();\n\
                                createDeviceComponents(globalDevice);\n\
                            });\n\
                        });';
        break;
        case "slider":
            html += 'Slider control: <span id="editcontrol_'+groupIndex+'_'+unique+'_name">'+controlName+"</span>";
            script += '$("#editcontrol_'+groupIndex+'_'+unique+'_edit").on("click", function (){\n\
                            doSliderControlEdit('+groupIndex+', '+controlIndex+', "'+unique+'");\n\
                       });';
            script += '$("#editcontrol_'+groupIndex+'_'+unique+'_delete").on("click", function (){\n\
                            askForDeletion($("#editcontrol_'+groupIndex+'_'+unique+'_name").html(), function(){\n\
                                globalDevice.controlset.groups['+groupIndex+'].controls.splice('+controlIndex+', 1);\n\
                                $("#devicecontent").empty();\n\
                                createDeviceComponents(globalDevice);\n\
                            });\n\
                        });';
        break;
        case "colorpicker":
            html += 'Color picker: <span id="editcontrol_'+groupIndex+'_'+unique+'_name">'+controlName+"</span>";
            script += '$("#editcontrol_'+groupIndex+'_'+unique+'_edit").on("click", function (){\n\
                            doColorpickerControlEdit('+groupIndex+', '+controlIndex+', "'+unique+'");\n\
                       });';
            script += '$("#editcontrol_'+groupIndex+'_'+unique+'_delete").on("click", function (){\n\
                            askForDeletion($("#editcontrol_'+groupIndex+'_'+unique+'_name").html(), function(){\n\
                                globalDevice.controlset.groups['+groupIndex+'].controls.splice('+controlIndex+', 1);\n\
                                $("#devicecontent").empty();\n\
                                createDeviceComponents(globalDevice);\n\
                            });\n\
                        });';
        break;
    }
    html += '</span></div>';
    script += '<\/script>';
    $('#editgroup_'+groupIndex).append(html);
    $('#editgroup_'+groupIndex).append(script);
    return unique;
}


//##############################################################################
/*
 * Group functions
 */

/**
 * Creates a device group and it's contents.
 * @param {type} groupIndex
 * @param {type} group
 * @returns {undefined}
 */
function createDeviceGroup(groupIndex,group){
    if(typeof groupIndex !== "undefined" && groupIndex !== null){
        try {
            $("#devicecontent").append('<div id="editgroup_'+groupIndex+'" class="well well-small" style="padding:5px;"><h4 class="devicegroup"><span style="float:left;cursor:pointer;" id="editgroup_'+groupIndex+'_edit"><img src="/shared/images/deviceeditor/wrench-2x.png" class="groupEditAction" alt="Edit group"/>&nbsp;</span><span id="editgroup_'+groupIndex+'_name">'+group.label+'</span><span style="float:right;cursor:pointer;" id="editgroup_'+groupIndex+'_delete"><img src="/shared/images/deviceeditor/circle-x-2x.png" class="groupDeleteAction" alt="Delete group"/></span></h4></div>');
            attachGroupDeletion(groupIndex,'#editgroup_'+groupIndex+'_delete');
            attachGroupEdit(groupIndex,group,'#editgroup_'+groupIndex+'_edit');
        } catch (err){

        }
    } else {
        groupIndex = globalDevice.controlset.groups.length;
        var groupContent ={ "id": "newgroupid"+groupIndex, "label":group };
        globalDevice.controlset.groups.push(groupContent);
        $("#devicecontent").append('<div id="editgroup_'+groupIndex+'" class="well well-small" style="padding:5px;"><h4 class="devicegroup" style="clear:both;"><span style="float:left;cursor:pointer;" id="editgroup_'+groupIndex+'_edit"><img src="/shared/images/deviceeditor/wrench-2x.png" class="groupEditAction" alt="Edit group"/>&nbsp;</span><span id="editgroup_'+groupIndex+'_name">'+group+'</span><span style="float:right;cursor:pointer;" id="editgroup_'+groupIndex+'_delete"><img src="/shared/images/deviceeditor/circle-x-2x.png" class="groupDeleteAction" alt="Delete group"/></span></h4></div>');
        attachGroupDeletion(groupIndex,'#editgroup_'+groupIndex+'_delete');
        attachGroupEdit(groupIndex,groupContent,'#editgroup_'+groupIndex+'_edit');
        doGroupEdit(groupIndex, groupContent);
    }
    $('#editgroup_'+groupIndex).droppable({
         accept: ".vis-addItem",
         drop: function(event, ui) {
             switch(ui.draggable.attr("id")){
                 case "vis-addData":
                     //// Add droppable to group.
                     doDataControlEdit(groupIndex, null, null);
                 break;
                 case "vis-addSelect":
                     //// Add droppable to group.
                     doSelectControlEdit(groupIndex, null, null);
                 break;
                 case "vis-addButton":
                     //// Add droppable to group.
                     doButtonControlEdit(groupIndex, null, null);
                 break;
                 case "vis-addToggleButton":
                     //// Add droppable to group.
                     doToggleControlEdit(groupIndex, null, null);
                 break;
                 case "vis-addSlider":
                     //// Add droppable to group.
                     doSliderControlEdit(groupIndex, null, null);
                 break;
                 case "vis-addColorPicker":
                     //// Add droppable to group.
                     doColorpickerControlEdit(groupIndex, null, null);
                 break;
             }
         }
    });
}


/**
 * Creates group controls.
 * @param {type} groupIndex
 * @param {type} group
 * @returns {undefined}
 */
function createDeviceGroupControls(groupIndex, group){
    for(var i = 0; i < group.controls.length; i++){
        switch(group.controls[i].type){
            case "select":
            case "data":
            case "button":
            case "toggle":
            case "slider":
            case "colorpicker":
                var dataName = group.controls[i].description;
                addControl(groupIndex, i, group.controls[i].type, dataName);
            break;
        }
    }
}

/**
 * Group delete function.
 * @param {type} groupIndex
 * @param {type} handleId
 * @returns {undefined}
 */
function attachGroupDeletion(groupIndex, handleId){
    $(handleId).on("click", function (){
        askForDeletion($("#editgroup_" + groupIndex +"_name").html(), function(){
            globalDevice.controlset.groups.splice(groupIndex, 1);
            //// After a group has been deleted, it has to be re-arranged
            $("#devicecontent").empty();
            createDeviceComponents(globalDevice);
        });
    });
}

/**
 * Editor used for the groups.
 * @param {type} groupIndex
 * @param {type} groupNode
 * @param {type} groupId
 * @returns {undefined}
 */
function attachGroupEdit(groupIndex, groupNode, groupId){
    $(groupId).on("click", function (){
        doGroupEdit(groupIndex, groupNode);
    });
}

function doGroupEdit(groupIndex, groupNode){
    $('#groupedit-ok').off("click").on("click", function () { 
        globalDevice.controlset.groups[groupIndex].id = $('#groupeditid').val();
        globalDevice.controlset.groups[groupIndex].label = $('#groupeditname').val();
        $("#editgroup_" + groupIndex +"_name").html($('#groupeditname').val());
        $('#dialog-groupedit').modal("hide");
    });
    $('#groupeditid').val(groupNode.id);
    $('#groupeditname').val(groupNode.label);
    $('#dialog-groupedit').modal("show");
}

//##############################################################################
/*
 * Data specific
 */

/**
 * Data control edit functions.
 * @param {type} groupIndex
 * @param {type} controlIndex
 * @returns {undefined}
 */
function doDataControlEdit(groupIndex, controlIndex, uniqueId){
    var data = {};
    if(controlIndex!==null){
        data = globalDevice.controlset.groups[groupIndex].controls[controlIndex];
    } else {
        /// Adding a new node
        data["id"]          = "newdatacontrolid";
        data["description"] = "Data description";
        data["datatype"]    = "string";
        data["graph"]       = "none";
        data["shortcut"]    = 0;
        data["prefix"]      = "";
        data["suffix"]      = "";
        data["visual"]      = "none";
        data["hidden"]      = false;
        data["retention"]   = false;
        data["minvalue"]    = 0;
        data["maxvalue"]    = 0;
        data["warnvalue"]   = 0;
        data["highvalue"]   = 0;
        data["timeout"]     = 0;
        data["readonly"]    = true;
        data["extra"]       = "";
        data["type"]        = "data";
        data["truetext"]    = "Yes";
        data["falsetext"]   = "No";
        data["boolvis"]     = "text";
        data["modifier"]    = false;
        if(typeof globalDevice.controlset.groups[groupIndex].controls === "undefined" ){
            globalDevice.controlset.groups[groupIndex].controls = new Array();
        }
        controlIndex = globalDevice.controlset.groups[groupIndex].controls.length;
        data["id"]+=controlIndex;
        globalDevice.controlset.groups[groupIndex].controls.push( data );
        uniqueId = addControl(groupIndex, controlIndex, "data", data["description"]);
    }
    $("#data_control_id").val(data.id);
    $("#data_control_description").val(data.description);
    $("#data_control_datatype").val(data.datatype);
    if((data.datatype!=="integer" && data.datatype!=="float") || ( data.hidden!==undefined && data.hidden==="true")){
        $("#data_control_graph").attr("disabled", "disabled");
        $("#data_control_graph").val("none");
    } else {
        $("#data_control_graph").val(data.graph);
    }
    if(typeof data.modifier === "undefined" || data.modifier === null || data.modifier === "undefined"){
        data.modifier = false;
    }
    if(typeof data.hidden === "undefined" || data.hidden === null || data.hidden === "undefined"){
        data.hidden = false;
    }
    if(typeof data.retention === "undefined" || data.retention === null || data.retention === "undefined"){
        data.retention = false;
    }
    if(typeof data.readonly === "undefined" || data.readonly === null || data.readonly === "undefined"){
        data.readonly = true;
    }
    if(typeof data.boolvis === "undefined" || data.boolvis === null || data.boolvis === "undefined"){
        data.boolvis = "text";
    }
    if(typeof data.falsetext === "undefined" || data.falsetext === null || data.falsetext === "undefined"){
        data.falsetext = "False";
    }
    if(typeof data.truetext === "undefined" || data.truetext === null || data.truetext === "undefined"){
        data.truetext = "True";
    }
    if(typeof data.visual === "undefined" || data.visual === null || data.visual === "undefined"){
        data.visual = "none";
    }
    $("#data_control_modifier").val(data.modifier.toString());
    $("#data_control_shortcut").val(data.shortcut);
    $("#data_control_prefix").val(data.prefix);
    $("#data_control_suffix").val(data.suffix);
    $("#data_control_visual").val(data.visual);
    $("#data_control_hidden").val(data.hidden.toString());
    $("#data_control_retention").val(data.retention.toString());
    $("#data_control_extra").val(data.extra);
    $("#data_control_minvalue").val(data.minvalue);
    $("#data_control_maxvalue").val(data.maxvalue);
    $("#data_control_warnvalue").val(data.warnvalue);
    $("#data_control_highvalue").val(data.highvalue);
    $("#data_control_timeout").val(data.timeout);
    $("#data_control_readonly").val(data.readonly.toString());
    $("#data_control_boolvis").val(data.boolvis);
    $("#data_control_falsetext").val(data.falsetext);
    $("#data_control_truetext").val(data.truetext);
 
    $(".databooloptions").hide();
    $(".numthresholds").hide();
    switch(data.datatype){
        case "integer":
        case "float":
            $(".numthresholds").show();
        break;
        case "boolean":
            $(".databooloptions").show();
        break;
    }
    $('#dialog-controldataedit').modal("show");
    $('#controldataedit-ok').off("click").on("click", function () { 
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].id          = $('#data_control_id').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].modifier    = $('#data_control_modifier').val()==="true";
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].description = $('#data_control_description').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].datatype    = $('#data_control_datatype').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].graph       = $('#data_control_graph').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].shortcut    = parseInt($('#data_control_shortcut').val());
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].prefix      = $('#data_control_prefix').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].suffix      = $('#data_control_suffix').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].visual      = $('#data_control_visual').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].hidden      = $('#data_control_hidden').val()==="true";
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].retention   = $('#data_control_retention').val()==="true";
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].extra       = $('#data_control_extra').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].minvalue    = parseInt($('#data_control_minvalue').val());
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].maxvalue    = parseInt($('#data_control_maxvalue').val());
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].warnvalue   = parseInt($('#data_control_warnvalue').val());
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].highvalue   = parseInt($('#data_control_highvalue').val());
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].timeout     = parseInt($('#data_control_timeout').val());
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].readonly    = $('#data_control_readonly').val()==="true";
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].boolvis     = $('#data_control_boolvis').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].falsetext   = $('#data_control_falsetext').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].truetext    = $('#data_control_truetext').val();
        $("#editcontrol_"+groupIndex+"_"+uniqueId+"_name").html($('#data_control_description').val());
        $('#dialog-controldataedit').modal('hide');
    });
}
//###############
/**
 * Button editor.
 * @param {type} groupIndex
 * @param {type} controlIndex
 * @returns {undefined}
 */
function doSelectControlEdit(groupIndex, controlIndex, uniqueId){
    var data = {};
    var selectList = [];
    $("#selectfielditemslist").empty();
    if(controlIndex!==null){
        data = globalDevice.controlset.groups[groupIndex].controls[controlIndex];
        selectList = globalDevice.controlset.groups[groupIndex].controls[controlIndex].parameters;
    } else {
        /// Adding a new node
        /// <button id="curstoresettings" datatype="string" value="s" description="Save current light as default" label="Store" />
        data["id"]          = "newselectcontrolid";
        data["description"] = "Select description";
        data["datatype"]    = "string";
        data["hidden"]      = false;
        data["shortcut"]    = 0;
        data["extra"]       = "";
        data["type"]        = "select";
        data["modifier"]    = false;
        if(typeof globalDevice.controlset.groups[groupIndex].controls === "undefined" ){
            globalDevice.controlset.groups[groupIndex].controls = new Array();
        }
        controlIndex = globalDevice.controlset.groups[groupIndex].controls.length;
        data["id"]+=controlIndex;
        globalDevice.controlset.groups[groupIndex].controls.push( data );
        uniqueId = addControl(groupIndex, controlIndex, "select", data["description"]);
    }
    if(typeof data.modifier === "undefined" || data.modifier === null || data.modifier === "undefined"){
        data.modifier = false;
    }
    $("#select_control_id").val(data.id);
    $("#select_control_modifier").val(data.modifier.toString());
    $("#select_control_description").val(data.description);
    $("#select_control_datatype").val(data.datatype);
    $("#select_control_shortcut").val(data.shortcut);
    $("#select_control_hidden").val(data.hidden.toString());
    $("#select_control_extra").val(data.extra);
    $('#dialog-controlselectedit').modal("show");

    if(typeof selectList !== "undefined"){
        for(var i=0; i < selectList.length; i++){
            var selectData = selectList[i];
            createSelectItem(selectData["label"], selectData["value"]);
        }
    }

    $('#controlselectedit-ok').off("click").on("click", function () { 
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].id          = $('#select_control_id').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].modifier    = $('#select_control_modifier').val()==="true";
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].description = $('#select_control_description').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].datatype    = $('#select_control_datatype').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].shortcut    = parseInt($('#select_control_shortcut').val());
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].hidden      = $('#select_control_hidden').val()==="true";
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].extra       = $('#select_control_extra').val();
        $("#selectfielditemslist .cpickereditbutton").each(function(){
            globalDevice.controlset.groups[groupIndex].controls[controlIndex]["parameters"].push(
                {
                    "label":$('#' + $(this).attr("id") + ' input[data-buttonvis="label"]').val(),
                    "value":$('#' + $(this).attr("id") + ' input[data-buttonvis="value"]').val()
                }
            );
        });
        $("#editcontrol_"+groupIndex+"_"+uniqueId+"_name").html($('#select_control_description').val());
        $('#dialog-controlselectedit').modal('hide');
    });
}
function createSelectItem(label, value){
    var randId = createUUID();
    var bhtml ='<div class="cpickereditbutton" id="editselectitem_'+randId+'_holder" style="clear:both;">\n\
                    <div style="float:right;cursor:pointer;"><span style="float:left;" id="editselectitem_'+randId+'_delete"><img src="/shared/images/deviceeditor/circle-x-2x.png" class="groupEditAction" alt="Remove button"/></div>\n\
                    <div style="float:left;">Select label</div>\n\
                    <div style="clear:both;float:left;"><input type="text" data-buttonvis="label" name="editselectitem_'+randId+'_label" id="editselectitem_'+randId+'_label" value="'+label+'" /></div>\n\
                    <div style="clear:both;float:left;">Select value</div>\n\
                    <div style="clear:both;float:left;"><input type="text" data-buttonvis="value" name="editselectitem_'+randId+'_value" id="editselectitem_'+randId+'_value" value="'+value+'" /></div>\n\
                <div style="clear:both;"></div></div>';

    $('#selectfielditemslist').append(bhtml);
    $('#editselectitem_'+randId+'_delete').on("click", function(){
        $(this).parent().parent().remove();
    });
}
//###############
/**
 * Button editor.
 * @param {type} groupIndex
 * @param {type} controlIndex
 * @returns {undefined}
 */
function doButtonControlEdit(groupIndex, controlIndex, uniqueId){
    var data = {};
    if(controlIndex!==null){
        data = globalDevice.controlset.groups[groupIndex].controls[controlIndex];
    } else {
        /// Adding a new node
        /// <button id="curstoresettings" datatype="string" value="s" description="Save current light as default" label="Store" />
        data["id"] = "newbuttoncontrolid";
        data["description"] = "Button description";
        data["datatype"]    = "string";
        data["label"]       = "Button label";
        data["value"]       = "Button value";
        data["hidden"]      = false;
        data["shortcut"]    = 0;
        data["extra"]       = "";
        data["type"]        = "button";
        data["modifier"]    = false;
        if(typeof globalDevice.controlset.groups[groupIndex].controls === "undefined" ){
            globalDevice.controlset.groups[groupIndex].controls = new Array();
        }
        controlIndex = globalDevice.controlset.groups[groupIndex].controls.length;
        data["id"]+=controlIndex;
        globalDevice.controlset.groups[groupIndex].controls.push( data );
        uniqueId = addControl(groupIndex, controlIndex, "button", data["description"]);
    }
    if(typeof data.modifier === "undefined" || data.modifier === null || data.modifier === "undefined"){
        data.modifier = false;
    }
    $("#button_control_id").val(data.id);
    $("#button_control_modifier").val(data.modifier.toString());
    $("#button_control_description").val(data.description);
    $("#button_control_datatype").val(data.datatype);
    $("#button_control_shortcut").val(data.shortcut);
    $("#button_control_hidden").val(data.hidden.toString());
    $("#button_control_label").val(data.label);
    $("#button_control_value").val(data.value);
    $("#button_control_extra").val(data.extra);
    $('#dialog-controlbuttonedit').modal("show");

    $('#controlbuttonedit-ok').off("click").on("click", function () { 
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].id          = $('#button_control_id').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].modifier    = $('#button_control_modifier').val()==="true";
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].description = $('#button_control_description').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].datatype    = $('#button_control_datatype').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].shortcut    = parseInt($('#button_control_shortcut').val());
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].hidden      = $('#button_control_hidden').val()==="true";
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].label       = $('#button_control_label').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].value       = $('#button_control_value').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].extra       = $('#button_control_extra').val();
        $("#editcontrol_"+groupIndex+"_"+uniqueId+"_name").html($('#button_control_description').val());
        $('#dialog-controlbuttonedit').modal('hide');
    });
}
//###############
/**
 * Toggle editor.
 * @param {type} groupIndex
 * @param {type} controlIndex
 * @returns {undefined}
 */
function doToggleControlEdit(groupIndex, controlIndex, uniqueId){
    var data    = {};
    var onData  = [];
    var offData = [];
    if(controlIndex!==null){
        data    = globalDevice.controlset.groups[groupIndex].controls[controlIndex];
        onData  = globalDevice.controlset.groups[groupIndex].controls[controlIndex].parameters.on;
        offData = globalDevice.controlset.groups[groupIndex].controls[controlIndex].parameters.off;
    } else {
        /// Adding a new node
        data["id"]          = "newtogglecontrolid";
        data["description"] = "Toggle description";
        data["datatype"]    = "boolean";
        data["hidden"]      = false;
        data["retention"]   = false;
        data["shortcut"]    = 0;
        data["extra"]       = "";
        data["type"]        = "toggle";
        data["modifier"]    = false;
        onData  = { "label" : "true", "value":"true"};
        offData = { "label" : "false", "value":"false"};
        if(globalDevice.controlset.groups[groupIndex].controls === undefined ){
            globalDevice.controlset.groups[groupIndex].controls = new Array();
        }
        controlIndex = globalDevice.controlset.groups[groupIndex].controls.length;
        data["id"]+=controlIndex;
        globalDevice.controlset.groups[groupIndex].controls.push( data );
        if(typeof globalDevice.controlset.groups[groupIndex].controls[controlIndex].parameters === "undefined"){
            globalDevice.controlset.groups[groupIndex].controls[controlIndex].parameters = {};
        }
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].parameters["on"] = onData;
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].parameters["off"] = offData;
        uniqueId = addControl(groupIndex, controlIndex, "toggle", data["description"]);
    }
    if(typeof data.modifier === "undefined" || data.modifier === null || data.modifier === "undefined"){
        data.modifier = false;
    }
    $("#toggle_control_id").val(data.id);
    $("#toggle_control_modifier").val(data.modifier.toString());
    $("#toggle_control_description").val(data.description);
    $("#toggle_control_datatype").val(data.datatype);
    $("#toggle_control_shortcut").val(data.shortcut);
    $("#toggle_control_hidden").val(data.hidden.toString());
    $("#toggle_control_retention").val(data.retention.toString());
    $("#toggle_control_on_label").val(onData.label);
    $("#toggle_control_on_value").val(onData.value.toString());
    $("#toggle_control_off_label").val(offData.label);
    $("#toggle_control_off_value").val(offData.value.toString());
    $("#toggle_control_extra").val(data.extra);
    $('#dialog-controltoggleedit').modal("show");

    $('#controltoggleedit-ok').off("click").on("click", function () { 
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].id                   = $('#toggle_control_id').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].modifier             = $('#toggle_control_modifier').val()==="true";
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].description          = $('#toggle_control_description').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].datatype             = $('#toggle_control_datatype').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].shortcut             = parseInt($('#toggle_control_shortcut').val());
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].hidden               = $('#toggle_control_hidden').val()==="true";
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].retention            = $('#toggle_control_retention').val()==="true";
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].extra                = $('#toggle_control_extra').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].parameters.on.label  = $('#toggle_control_on_label').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].parameters.on.value  = $('#toggle_control_on_value').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].parameters.off.label = $('#toggle_control_off_label').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].parameters.off.value = $('#toggle_control_off_value').val();
        $("#editcontrol_"+groupIndex+"_"+uniqueId+"_name").html($('#toggle_control_description').val());
        $('#dialog-controltoggleedit').modal('hide');
    });
}


/**
 * Data control edit functions.
 * @param {type} groupIndex
 * @param {type} controlIndex
 * @returns {undefined}
 */
function doSliderControlEdit(groupIndex, controlIndex, uniqueId){
    var data = {};
    if(controlIndex!==null){
        data = globalDevice.controlset.groups[groupIndex].controls[controlIndex];
    } else {
        /// Adding a new node
        data["id"]          = "newslidercontrolid";
        data["description"] = "Slider description";
        data["datatype"]    = "integer";
        data["shortcut"]    = 0;
        data["hidden"]      = false;
        data["retention"]   = false;
        data["extra"]       = "";
        data["parameters"]  = {"min":0,"max":100};
        data["type"]        = "slider";
        data["modifier"]    = false;
        if(typeof globalDevice.controlset.groups[groupIndex].controls === "undefined" ){
            globalDevice.controlset.groups[groupIndex].controls = new Array();
        }
        controlIndex = globalDevice.controlset.groups[groupIndex].controls.length;
        data["id"]+=controlIndex;
        globalDevice.controlset.groups[groupIndex].controls.push( data );
        uniqueId = addControl(groupIndex, controlIndex, "slider", data["description"]);
    }
    if(typeof data.modifier === "undefined" || data.modifier === null || data.modifier === "undefined"){
        data.modifier = false;
    }
    $("#slider_control_id").val(data.id);
    $("#slider_control_modifier").val(data.modifier.toString());
    $("#slider_control_description").val(data.description);
    $("#slider_control_datatype").val(data.datatype);
    $("#slider_control_shortcut").val(data.shortcut);
    $("#slider_control_min_value").val(data.parameters.min);
    $("#slider_control_max_value").val(data.parameters.max);
    $("#slider_control_hidden").val(data.hidden.toString());
    $("#slider_control_retention").val(data.retention.toString());
    $("#slider_control_extra").val(data.extra);
    $('#dialog-controlslideredit').modal("show");

    $('#controlslideredit-ok').off("click").on("click", function () { 
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].id          = $('#slider_control_id').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].modifier    = $('#slider_control_modifier').val()==="true";
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].description = $('#slider_control_description').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].datatype    = $('#slider_control_datatype').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].shortcut    = parseInt($('#slider_control_shortcut').val());
        if($('#slider_control_datatype').val()==="float"){
            globalDevice.controlset.groups[groupIndex].controls[controlIndex].parameters.min = parseFloat($('#slider_control_min_value').val());
            globalDevice.controlset.groups[groupIndex].controls[controlIndex].parameters.max = parseFloat($('#slider_control_max_value').val());
        } else {
            globalDevice.controlset.groups[groupIndex].controls[controlIndex].parameters.min = parseInt($('#slider_control_min_value').val());
            globalDevice.controlset.groups[groupIndex].controls[controlIndex].parameters.max = parseInt($('#slider_control_max_value').val());
        }
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].hidden      = $('#slider_control_hidden').val()==="true";
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].retention   = $('#slider_control_retention').val()==="true";
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].extra       = $('#slider_control_extra').val();
        $("#editcontrol_"+groupIndex+"_"+uniqueId+"_name").html($('#slider_control_description').val());
        $('#dialog-controlslideredit').modal('hide');
    });
}


/**
 * Data control edit functions.
 * @param {type} groupIndex
 * @param {type} controlIndex
 * @returns {undefined}
 */
function doColorpickerControlEdit(groupIndex, controlIndex, uniqueId){
    var data = {};
    var buttonsList = [];
    $("#colorpickerbuttonslist").empty();
    if(controlIndex!==null){
        data = globalDevice.controlset.groups[groupIndex].controls[controlIndex];
        buttonsList = globalDevice.controlset.groups[groupIndex].controls[controlIndex].parameters;
    } else {
        /// Adding a new node
        data["id"]          = "newcolorpickerid";
        data["description"] = "Colorpicker description";
        data["mode"]        = "rgb";
        data["shortcut"]    = 0;
        data["hidden"]      = false;
        data["retention"]   = false;
        data["extra"]       = "";
        data["type"]        = "colorpicker";
        data["datatype"]    = "color";
        data["modifier"]    = false;
        if(globalDevice.controlset.groups[groupIndex].controls === undefined ){
            globalDevice.controlset.groups[groupIndex].controls = new Array();
        }
        controlIndex = globalDevice.controlset.groups[groupIndex].controls.length;
        data["id"]+=controlIndex;
        globalDevice.controlset.groups[groupIndex].controls.push(data);
        globalDevice.controlset.groups[groupIndex].controls[controlIndex]["parameters"] = {"label":"Set","value":"set"};
        uniqueId = addControl(groupIndex, controlIndex, "colorpicker", data["description"]);
        createcpickerButton("Set color", "btn_1");
    }
    if(typeof data.modifier === "undefined" || data.modifier === null || data.modifier === "undefined"){
        data.modifier = false;
    }
    $("#colorpicker_control_id").val(data.id);
    $("#colorpicker_control_modifier").val(data.modifier.toString());
    $("#colorpicker_control_description").val(data.description);
    $("#colorpicker_control_mode").val(data.mode);
    $("#colorpicker_control_extra").val(data.extra);
    $("#colorpicker_control_shortcut").val(data.shortcut===undefined?0:data.shortcut);
    $("#colorpicker_control_hidden").val(data.hidden.toString());
    $("#colorpicker_control_retention").val(data.retention.toString());
    
    if(typeof buttonsList !== "undefined"){
        for(var i=0; i < buttonsList.length; i++){
            var buttonData = buttonsList[i];
            createcpickerButton(buttonData["label"], buttonData["value"]);
        }
    }
    
    $('#dialog-colorpickeredit').modal("show");

    $('#colorpickeredit-ok').off("click").on("click", function () { 
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].id            = $('#colorpicker_control_id').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].modifier      = $('#colorpicker_control_modifier').val()==="true";
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].description   = $('#colorpicker_control_description').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].mode          = $('#colorpicker_control_mode').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].shortcut      = parseInt($('#colorpicker_control_shortcut').val());
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].hidden        = $('#colorpicker_control_hidden').val()==="true";
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].retention     = $('#colorpicker_control_retention').val()==="true";
        globalDevice.controlset.groups[groupIndex].controls[controlIndex].extra         = $('#colorpicker_control_extra').val();
        globalDevice.controlset.groups[groupIndex].controls[controlIndex]["parameters"] = new Array();
        $("#colorpickerbuttonslist .cpickereditbutton").each(function(){
            globalDevice.controlset.groups[groupIndex].controls[controlIndex]["parameters"].push(
                {
                    "label":$('#' + $(this).attr("id") + ' input[data-buttonvis="label"]').val(),
                    "value":$('#' + $(this).attr("id") + ' input[data-buttonvis="value"]').val()
                }
            );
        });
        $("#editcontrol_"+groupIndex+"_"+uniqueId+"_name").html($('#colorpicker_control_description').val());
        $('#dialog-colorpickeredit').modal('hide');
    });
}

function createcpickerButton(label, value){
    var randId = createUUID();
    var bhtml ='<div class="cpickereditbutton" id="editcpickerbutton_'+randId+'_holder" style="clear:both;">\n\
                    <div style="float:right;cursor:pointer;"><span style="float:left;" id="editcpickerbutton_'+randId+'_delete"><img src="/shared/images/deviceeditor/circle-x-2x.png" class="groupEditAction" alt="Remove button"/></div>\n\
                    <div style="float:left;">Button label</div>\n\
                    <div style="clear:both;float:left;"><input type="text" data-buttonvis="label" name="editcpickerbutton_'+randId+'_label" id="editcpickerbutton_'+randId+'_label" value="'+label+'" /></div>\n\
                    <div style="clear:both;float:left;">Button value</div>\n\
                    <div style="clear:both;float:left;"><input type="text" data-buttonvis="value" name="editcpickerbutton_'+randId+'_value" id="editcpickerbutton_'+randId+'_value" value="'+value+'" /></div>\n\
                <div style="clear:both;"></div></div>';

    $('#colorpickerbuttonslist').append(bhtml);
    $('#editcpickerbutton_'+randId+'_delete').on("click", function(){
        $(this).parent().parent().remove();
    });
}