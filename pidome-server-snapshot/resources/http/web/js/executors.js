$.ajaxSetup({
  cache: false //// All the ajax calls we make sould REALLY not be cached. If otherwise use $.ajax with cache true to turn on caching
});
$(function() {
    function menuBarSelected(event, ui) {
        ///// for debugging.
    }
    $(".menubar-icons").menubar({
        autoExpand: true,
        menuIcon: true,
        buttons: true,
        position: {
            within: $("#topBar").add(window).first()
        },
        select: menuBarSelected
    });
    $("#pageTabs").tabs();
});

function ensureNumeric(selector){
    $(selector).keypress(function(event){
        if ((event.which !== 46 || $(this).val().indexOf('.') !== -1) && (event.which < 48 || event.which > 57)) {
            event.preventDefault();
        }
    }).keyup(function(event){
        if($(this).val()===""){
            $(this).val("0.0");
        }
    }).focusout(function() {
        var dotpos=$(this).val().lastIndexOf(".");
        if (!dotpos || dotpos<0){
            $(this).val($(this).val() + ".0");
        } else if (dotpos===0){
            $(this).val("0" + $(this).val());
        } else if (dotpos===$(this).val().length-1){
            $(this).val($(this).val() + "0");
        } else if ($(this).val() === "") {
            $(this).val("0.0");
        }
    });
}


function makeRandId(){
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    for( var i=0; i < 10; i++ ){
        text += possible.charAt(Math.floor(Math.random() * possible.length));
    }
    return text;
}

function runMacro(macroId){
    $.get("/jsonrpc.json?rpc={\"jsonrpc\": \"2.0\", \"method\": \"MacroService.runMacro\", \"params\": ["+macroId+"] \"id\": \"MacroService.runMacro\"}")
        .done(function(data) {
            var obj = jQuery.parseJSON(data);
            if(obj.result[0].exec === true){
            }
    }, "json");
}

function activateSystemState(stateId){
    $("#pleaseWaitProgress").dialog( "open" );
    $.post("macroService.json", { command: "runEvent", eventId: stateId } )
        .done(function(data) {
            var obj = jQuery.parseJSON(data);
            if(obj.result[0].exec === true){
                $("#systemEventList h3.active").removeClass("active");
                $("#activeStateHeader-" + stateId).addClass("active");
                $("#SystemStateActiveStrong").text($("#activeStateHeader-" + stateId).text());
            }
        }, "json")
        .always(function(){
            $("#pleaseWaitProgress").dialog( "close" );
        });
}

function loadPackageDelivers(packageId){
    $("#pleaseWaitProgress").dialog( "open" );
    $.post("packageService.json", { command: "getDriverList", packageId: packageId } )
        .done(function(data) {
            var obj = jQuery.parseJSON(data);
            if(obj.result[0].exec === true){
                $("#driverList").empty();
                $("#deviceList").empty();
                $("#packageDetailsList").empty();
                $("#driverDetailsList").empty();
                $("#deviceDetailsList").empty();
                $.each(obj.result[1], function(key, value) { 
                   $("#driverList").append("<li class=\"ui-widget-content\" id=\""+key+"\">"+value+"</li>");
                });
                $.each(obj.result[2], function(key, value) { 
                   $("#deviceList").append("<li class=\"ui-widget-content\" id=\""+key+"\">"+value+"</li>");
                });
                $.each(obj.result[3], function(key, value) { 
                   $("#packageDetailsList").append("<div class=\"n\">"+key+"</div><div class=\"v\">: "+value+"</div>");
                });
            }
        }, "json")
        .always(function(){
            $("#pleaseWaitProgress").dialog( "close" );
        });
}

function loadDriverDetails(driverId){
    $("#pleaseWaitProgress").dialog( "open" );
    $.post("packageService.json", { command: "getDriverDetails", driverId: driverId } )
        .done(function(data) {
            var obj = jQuery.parseJSON(data);
            if(obj.result[0].exec === true){
                $("#driverDetailsList").empty();
                $.each(obj.result[1], function(key, value) { 
                   $("#driverDetailsList").append("<div class=\"n\">"+key+"</div><div class=\"v\">: "+value+"</div>");
                });
            }
        }, "json")
        .always(function(){
            $("#pleaseWaitProgress").dialog( "close" );
        });
}

function loadDeviceDetails(deviceId){
    $("#pleaseWaitProgress").dialog( "open" );
    $.post("packageService.json", { command: "getDeviceDetails", deviceId: deviceId } )
        .done(function(data) {
            var obj = jQuery.parseJSON(data);
            if(obj.result[0].exec === true){
                $("#deviceDetailsList").empty();
                $.each(obj.result[1], function(key, value) { 
                   $("#deviceDetailsList").append("<div class=\"n\">"+key+"</div><div class=\"v\">: "+value+"</div>");
                });
            }
        }, "json")
        .always(function(){
            $("#pleaseWaitProgress").dialog( "close" );
        });
}

function loadPluginDetails(pluginId){
    $("#pleaseWaitProgress").dialog( "open" );
    $.post("packageService.json", { command: "getPluginDetails", deviceId: pluginId } )
        .done(function(data) {
            var obj = jQuery.parseJSON(data);
            if(obj.result[0].exec === true){
                $("#pluginDetailsList").empty();
                $.each(obj.result[1], function(key, value) { 
                   $("#pluginDetailsList").append("<div class=\"n\">"+key+"</div><div class=\"v\">: "+value+"</div>");
                });
            }
        }, "json")
        .always(function(){
            $("#pleaseWaitProgress").dialog( "close" );
        });
}

function displayDeviceModal(deviceId){
    $("#deviceModalContent").empty();
    $("#deviceModal").dialog( "open" );
    $("#deviceModalContent").append("<p><span style=\"float: left; margin: 0 7px 20px 0;\"><img src=\"images/ajax-loader.gif\" alt=\"Please wait\"/></span>Please wait...</p>");
    
    var postData=deviceId.split("-");
    
    $.post("deviceRender.json", { device: postData[0], deviceid: postData[1] } )
        .done(function(data) {
            $("#deviceModalContent").empty();
            $("#deviceModalContent").append(data);
        });
}

function startSingleDeviceInstance(deviceId){
    $.get("/jsonrpc.json?rpc={\"jsonrpc\": \"2.0\", \"method\": \"DeviceService.startDevice\", \"params\": ["+deviceId+"] \"id\": \"DeviceService.startDevice\"}")
        .done(function(data) {
            var obj = jQuery.parseJSON(data);
            if(obj.result !== true){
                alert("Failed to start device: " + obj.result[0].reason);
            } else {
                alert("Started device");
                $("#mutationStartDevice-" + deviceId).hide();
            }
        }, "json");
}

function runDeviceCommand(action){
    $("#execResult").html("<p>Please wait....</p>");
    var commandSplitted = action.split("-");
    var devicegrp = commandSplitted[1].toString();
    var deviceset = commandSplitted[2].toString();
    var devicecmd = commandSplitted[3].toString();
    $.get("/jsonrpc.json?rpc={\"jsonrpc\": \"2.0\", \"method\": \"DeviceService.sendDevice\", \"params\": ["+$("#deviceActionDeviceId").attr("value")+", \""+devicegrp+"\", \""+deviceset+"\", \""+devicecmd+"\"] \"id\": \"DeviceService.sendDevice\"}")
        .done(function(data) {
            var obj = jQuery.parseJSON(data);
            if(obj.result.success !== true){
                $("#execResult").html("<p class=\"error\">" + obj.result.message + "</p>");
            } else {
                $("#execResult").html("<p class=\"success\">Command send</p>");
            }
        }, "json");
}

function setLocationEditDataValues(locationId){
    if(locationId!=="0"){
        $("#pleaseWaitProgress").dialog( "open" );
        $.get("/jsonrpc.json?rpc={\"jsonrpc\": \"2.0\", \"method\": \"LocationService.getLocation\", \"params\": ["+locationId+"], \"id\": \"LocationService.getLocation\"}")
            .done(function(data) {
                var obj = jQuery.parseJSON(data);
                if(obj.result.success !== true){
                    alert("Could not load location: " + obj.result.message);
                } else {
                    $("#locationname").val(obj.result.data.name);
                    $("#locationdescription").val(obj.result.data.description);
                    $("#locationid").val(obj.result.data.id);
                    $("#locationeditor").css('display', 'block');
                }
            }, "json")        
            .always(function(){
                $("#pleaseWaitProgress").dialog( "close" );
            });
    } else {
        $("#locationname").val("Location name");
        $("#locationdescription").val("Location description");
        $("#locationid").val("0");
        $("#locationeditor").css('display', 'block');
    }
}

function saveLocationSettings(locationId){
    $("#pleaseWaitProgress").dialog( "open" );
    var json;
    var postField = {};
    if(locationId==="0"){
        json = [
            $("#locationname").val(),
            $("#locationdescription").val()
        ];
        postField["rpc"] = "{\"jsonrpc\": \"2.0\", \"method\": \"LocationService.saveLocation\", \"params\": "+JSON.stringify(json)+" \"id\": \"LocationService.saveLocation\"}";
    } else {
        json = [
            parseInt(locationId),
            $("#locationname").val(),
            $("#locationdescription").val()
        ];
        postField["rpc"] = "{\"jsonrpc\": \"2.0\", \"method\": \"LocationService.editLocation\", \"params\": "+JSON.stringify(json)+" \"id\": \"LocationService.editLocation\"}";
    }
    $.post("/jsonrpc.json",postField )
        .done(function(data) {
            var obj = jQuery.parseJSON(data);
            if(obj.result.success !== true){
                alert("Could not save location: " + obj.result.message);
            } else {
                window.location.href=window.location.href;
            }
        }, "json")        
        .always(function(){ 
            $("#pleaseWaitProgress").dialog( "close" );
        });
}

function setCategoryEditDataValues(categoryId){
    if(categoryId!=="-1"){
        $("#pleaseWaitProgress").dialog( "open" );
        $.get("/jsonrpc.json?rpc={\"jsonrpc\": \"2.0\", \"method\": \"CategoryService.getCategory\", \"params\": ["+categoryId+"], \"id\": \"CategoryService.getCategory\"}")
            .done(function(data) {
                var obj = jQuery.parseJSON(data);
                if(obj.result.success !== true){
                    alert("Could not load category: " + obj.result.message);
                } else {
                    if (obj.result.data.fixed === false) {
                        $("#categoryname").val(obj.result.data.name);
                        $("#categorydescription").val(obj.result.data.description);
                        $("#categoryid").val(obj.result.data.id);
                        $("#categoryconstant").val(obj.result.data.constant);
                        $("#categoryeditor").css('display', 'block');
                        $("#subcategoryeditor").css('display', 'none');
                        $("#categoryMessage").hide();
                    } else {
                        $("#categoryeditor").css('display', 'none');
                        $("#subcategoryeditor").css('display', 'none');

                        $("#catMessageName").html(": " + obj.result.data.name);
                        $("#catMessageDesc").html(": " + obj.result.data.description);
                        $("#catMessageConst").html(": " + obj.result.data.constant);
                        $("#categoryMessage").show();
                    }
                    $("#subcategoryMessage").hide();
                    $.get("/jsonrpc.json?rpc={\"jsonrpc\": \"2.0\", \"method\": \"CategoryService.getSubCategoryList\", \"params\": ["+categoryId+"], \"id\": \"CategoryService.getSubCategoryList\"}")
                        .done(function(data) {
                            var obj2 = jQuery.parseJSON(data);
                            if(obj2.result.success !== true){
                                alert("Could not load sub category list: " + obj2.result.message);
                            } else {
                                for(key in obj2.result.data) {
                                    $("#knownsubcategories").empty();
                                    $("#knownsubcategories").append("<li class=\"ui-widget-content\" id=\"subcat_"+obj2.result.data[key].id+"\">"+obj2.result.data[key].name+"</li>");
                                }
                            }
                        }, "json");
                }
            }, "json")        
            .always(function(){
                $("#pleaseWaitProgress").dialog( "close" );
            });
    } else {
        $("#categoryname").val("Category name");
        $("#categorydescription").val("Category description");
        $("#categoryconstant").val("CONSTANT");
        $("#categoryid").val("-1");
        $("#categoryMessage").hide();
        $("#categoryeditor").css('display', 'block');
    }
}

function saveCategorySettings(categoryId){
    $("#pleaseWaitProgress").dialog( "open" );
    var json;
    var postField = {};
    if(categoryId==="-1"){
        var json = [
                $("#categoryname").val(),
                $("#categorydescription").val(),
                $("#categoryconstant").val()
            ];
        postField["rpc"] = "{\"jsonrpc\": \"2.0\", \"method\": \"CategoryService.saveCategory\", \"params\": "+JSON.stringify(json)+" \"id\": \"CategoryService.saveCategory\"}";
    } else {
        var json = [
                parseInt(categoryId),
                $("#categoryname").val(),
                $("#categorydescription").val(),
                $("#categoryconstant").val()
            ];
        postField["rpc"] = "{\"jsonrpc\": \"2.0\", \"method\": \"CategoryService.editCategory\", \"params\": "+JSON.stringify(json)+" \"id\": \"CategoryService.editCategory\"}";
    }
    $.post("/jsonrpc.json", postField )
        .done(function(data) {
            var obj = jQuery.parseJSON(data);
            if(obj.result.success !== true){
                alert("Could not save category: " + obj.result.message);
            } else {
                window.location.href=window.location.href;
            }
        }, "json")        
        .always(function(){ 
            $("#pleaseWaitProgress").dialog( "close" );
        });
}

function setSubCategoryEditDataValues(subcatId){
    var temp = subcatId.split("_");
    subcatId = temp[1];
    if(subcatId!=="-1"){
        $("#pleaseWaitProgress").dialog( "open" );
        $.get("/jsonrpc.json?rpc={\"jsonrpc\": \"2.0\", \"method\": \"CategoryService.getSubCategory\", \"params\": ["+subcatId+"], \"id\": \"CategoryService.getSubCategory\"}")
            .done(function(data) {
                var obj = jQuery.parseJSON(data);
                if(obj.result.success !== true){
                    alert("Could not load sub category: " + obj.result.message);
                } else {
                    if(obj.result.data.cat_id!==0){
                        $("#subcategoryname").val(obj.result.data.name);
                        $("#subcategorydescription").val(obj.result.data.description);
                        $("#subcategoryid").val(obj.result.data.id);
                        $("#subcategoryeditor").css('display', 'block');
                        $("#subcategoryMessage").hide();
                    } else {
                        $("#subcategoryMessage").show();
                    }
                }
            }, "json")        
            .always(function(){
                $("#pleaseWaitProgress").dialog( "close" );
            });
    } else {
        $("#subcategoryname").val("Subcategory name");
        $("#subcategorydescription").val("Subcategory description");
        $("#subcategoryid").val("0");
        $("#subcategoryeditor").css('display', 'block');
    }
}

function saveSubCategorySettings(subCatIdId){
    var temp = subCatIdId.split("_");
    subCatIdId = $("#subcategoryid").val();
    
    var json;
    var postField = {};
    if(subCatIdId==="-1"){
        var json = [
                parseInt($("#categoryid").val()),
                $("#subcategoryname").val(),
                $("#subcategorydescription").val()
            ];
        postField["rpc"] = "{\"jsonrpc\": \"2.0\", \"method\": \"CategoryService.saveSubCategory\", \"params\": "+JSON.stringify(json)+" \"id\": \"CategoryService.saveSubCategory\"}";
    } else {
        var json = [
                parseInt(subCatIdId),
                parseInt($("#categoryid").val()),
                $("#subcategoryname").val(),
                $("#subcategorydescription").val()
            ];
        postField["rpc"] = "{\"jsonrpc\": \"2.0\", \"method\": \"CategoryService.editSubcategory\", \"params\": "+JSON.stringify(json)+" \"id\": \"CategoryService.editSubcategory\"}";
    }
    $("#pleaseWaitProgress").dialog( "open" );
    $.post("/jsonrpc.json", postField )
        .done(function(data) {
            var obj = jQuery.parseJSON(data);
                if(obj.result.success !== true){
                    alert("Could not save sub category: " + obj.result.message);
            } else {
                window.location.href=window.location.href;
            }
        }, "json")        
        .always(function(){ 
            $("#pleaseWaitProgress").dialog( "close" );
        });
}

function displayDeviceEditModal(device, action, step){
    $("#pleaseWaitProgress").dialog( "open" );
    $.post("deviceSettingsRenderer.xhtml", { action: action, step: step, deviceId: device } )
        .done(function(data) {
            $("#deviceLocationModalContent").html(data);
        }, "json")        
        .always(function(){ 
            $("#pleaseWaitProgress").dialog( "close" );
            $("#deviceLocationModal").dialog("open");
        });
}

function saveNewDevice(){
    $("#pleaseWaitProgress").dialog( "open" );
    var $xmlDoc = $( $.parseXML( "<settings/>" ) );
    $("#deviceMutationDeviceXmlSettings input, #deviceMutationDeviceXmlSettings select").each(function() {
        if($(this).attr("id")!==undefined && $(this).attr("id").startsWith("mutationInputDeviceOptionExtend-")){
            var node = $xmlDoc[0].createElement("setting");
            node.setAttribute("identification", $(this).attr("id").replace("mutationInputDeviceOptionExtend-",""));
            node.setAttribute("value", $(this).val());
            $xmlDoc.find("settings").eq(0).append(node);
        }
    });
    var json = [
        parseInt($("#deviceMutationDevice").val()),
        parseInt($("#locationMutationSelect option:selected").val()),
        $("#mutationInputDeviceOptionBase-address").val(),
        $("#mutationInputBase-name").val(),
        parseInt($("#categoryMutationSelect option:selected").val()),
        parseInt($("#favoriteMutationSelect option:selected").val())===0?false:true,
        xmlToString($xmlDoc)
    ];
    var postField = {};
    postField["rpc"] = "{\"jsonrpc\": \"2.0\", \"method\": \"DeviceService.addDevice\", \"params\": "+JSON.stringify(json)+" \"id\": \"DeviceService.saveDevice\"}";
    $.post("/jsonrpc.json",postField)
        .done(function(data) {
            var obj = jQuery.parseJSON(data);
            $("#pleaseWaitProgress").dialog( "close" );
            try {
                if(obj.result.success !== true){
                    alert("Could not save device: " + obj.result.message);
                    window.location.href=window.location.href;
                } else {
                    alert("Device added");
                    window.location.href=window.location.href;
                }
            } catch(err){
                alert("Could not save device, error: " + obj.error.data.message);
                window.location.href=window.location.href;
            }
        }, "json");

}

function saveEditDevice(){
    $("#pleaseWaitProgress").dialog( "open" );
    var postFields = {};
    var $xmlDoc = $( $.parseXML( "<settings/>" ) );
    $("#deviceMutationDeviceXmlSettings input, #deviceMutationDeviceXmlSettings select").each(function() {
        if($(this).attr("id")!==undefined && $(this).attr("id").startsWith("mutationInputDeviceOptionExtend-")){
            var node = $xmlDoc[0].createElement("setting");
            node.setAttribute("identification", $(this).attr("id").replace("mutationInputDeviceOptionExtend-",""));
            node.setAttribute("value", $(this).val());
            $xmlDoc.find("settings").eq(0).append(node);
        }
    });
    postFields["settings"] = xmlToString($xmlDoc);
    var json = [
        parseInt($("#deviceMutationDevice").val()),
        parseInt($("#locationMutationSelect option:selected").val()),
        $("#mutationInputDeviceOptionBase-address").val(),
        $("#mutationInputBase-name").val(),
        parseInt($("#categoryMutationSelect option:selected").val()),
        parseInt($("#favoriteMutationSelect option:selected").val())===0?false:true,
        xmlToString($xmlDoc)
    ];
    var postField = {};
    postField["rpc"] = "{\"jsonrpc\": \"2.0\", \"method\": \"DeviceService.editDevice\", \"params\": "+JSON.stringify(json)+" \"id\": \"DeviceService.editDevice\"}";
    $.post("/jsonrpc.json",postField)
        .done(function(data) {
            var obj = jQuery.parseJSON(data);
            $("#pleaseWaitProgress").dialog( "close" );
            try {
                if(obj.result.success !== true){
                    alert("Could not save device: " + obj.result.message);
                    window.location.href=window.location.href;
                } else {
                    alert("Device modified");
                    window.location.href=window.location.href;
                }
            } catch(err){
                alert("Could not save device, error: " + obj.error.data.message);
                window.location.href=window.location.href;
            }
        }, "json");
}




function creepyDeviceAction(buttonObject){
    var arrDeviceData = $(buttonObject).val().split("||");
    $("#dialog-confirm p").html("Ar you shure you want to delete the device <strong>"+arrDeviceData[0]+"</strong> at <strong>"+arrDeviceData[1]+"</strong><br/><br/>This action can <strong>NOT</strong> be <strong>UNDONE</strong>!");
    $( "#dialog-confirm" ).dialog({
      resizable: false,
      height:180,
      modal: true,
      buttons: {
        "Yes": function() {
            $.get("/jsonrpc.json?rpc={\"jsonrpc\": \"2.0\", \"method\": \"DeviceService.deleteDevice\", \"params\": ["+arrDeviceData[2]+"] \"id\": \"DeviceService.deleteDevice\"}")
                .done(function(data) {
                    var obj = jQuery.parseJSON(data);
                    try {
                        if(obj.result.success !== true){
                            alert("Could not delete device '"+arrDeviceData[0]+"' at '"+arrDeviceData[1]+"' refer to log file");
                        } else {
                            alert("Device '"+arrDeviceData[0]+"' at '"+arrDeviceData[1]+"' is deleted");
                            window.location.href=window.location.href;
                        }
                    } catch(err){
                        alert("Could not delete device, error: " + obj.error.data.message);
                        window.location.href=window.location.href;
                    }
                }, "json");
        },
        Cancel: function() {
          $( this ).dialog( "close" );
        }
      }
    });
}

function displayMediaModal(mediaId){
    $("#mediaModalContent").empty();
    $("#mediaModal").dialog( "open" );
    $("#mediaModalContent").append("<p><span style=\"float: left; margin: 0 7px 20px 0;\"><img src=\"images/ajax-loader.gif\" alt=\"Please wait\"/></span>Please wait...</p>");
    
    var getData=mediaId.split("-");
    
    $.get("mediarenderer.xhtml?mediaid=" + getData[1] )
        .done(function(data) {
            $("#mediaModalContent").empty();
            $("#mediaModalContent").append(data);
        });
}

function displayMediaEdit(mediaId){
    $("#mediaModalContent").empty();
    $("#mediaModal").dialog( "open" );
    $("#mediaModalContent").append("<p><span style=\"float: left; margin: 0 7px 20px 0;\"><img src=\"images/ajax-loader.gif\" alt=\"Please wait\"/></span>Please wait...</p>");
    
    var getData=mediaId.split("-");
    
    $.get("mediaeditor.xhtml?media_id=" + getData[2] )
        .done(function(data) {
            $("#mediaModalContent").empty();
            $("#mediaModalContent").append(data);
        });
}

function setInstalledMediaPlugin(select){
    displayMediaEditInstalledMedia($(select).val());
}

function displayMediaEditInstalledMedia(installedId){
    $("#mediaModalContent").empty();
    $("#mediaModal").dialog( "open" );
    $("#mediaModalContent").append("<p><span style=\"float: left; margin: 0 7px 20px 0;\"><img src=\"images/ajax-loader.gif\" alt=\"Please wait\"/></span>Please wait...</p>");
    
    $.get("mediaeditor.xhtml?installed_id=" + installedId )
        .done(function(data) {
            $("#mediaModalContent").empty();
            $("#mediaModalContent").append(data);
        });
}

function addMediaPlugin(){
    $("#pleaseWaitProgress").dialog( "open" );
    var postData = {};
    postData["installed_id"] = $("#installed_id").val();
    postData["plugin_name"] = $("#plugin_name").val();
    postData["plugin_desc"] = $("#plugin_desc").val();
    postData["plugin_location"] = $("#plugin_location").val();
    $("#optionCollection input").each(function(){
        postData[$(this).attr("id")] = $(this).val();
    });
    $.post("mediaeditor.xhtml?installed_id=" + $("#installed_id").val(), postData)
            .done(function(data) {
                $("#pleaseWaitProgress").dialog( "close" );
                var obj = jQuery.parseJSON(data);
                if (obj.result[0].exec !== true) {
                    alert("Could not add media plugin refer to log file");
                } else {
                    window.location.href = window.location.href;
                }
            }, "json");
}

function updateMediaPlugin(mediaId){
    $("#pleaseWaitProgress").dialog( "open" );
    var postData = {};
    postData["media_id"] = $("#media_id").val();
    postData["plugin_name"] = $("#plugin_name").val();
    postData["plugin_desc"] = $("#plugin_desc").val();
    postData["plugin_location"] = $("#plugin_location").val();
    $("#optionCollection input").each(function(){
        postData[$(this).attr("id")] = $(this).val();
    });
    $.post("mediaeditor.xhtml?media_id=" + mediaId, postData)
            .done(function(data) {
                $("#pleaseWaitProgress").dialog( "close" );
                var obj = jQuery.parseJSON(data);
                if (obj.result[0].exec !== true) {
                    alert("Could not edit media plugin refer to log file");
                } else {
                    window.location.href = window.location.href;
                }
            }, "json");
}

function removeMediaPlugin(pluginId){
    $("#pleaseWaitProgress").dialog("open");
    $.get("mediaeditor.xhtml?remove_id=" + pluginId )
        .done(function(data) {
                $("#pleaseWaitProgress").dialog("close");
                var obj = jQuery.parseJSON(data);
                if (obj.result[0].exec !== true) {
                    alert("Could not delete media plugin refer to log file");
                } else {
                    window.location.href = window.location.href;
                }
        }, "json");
}