/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
$.ajaxPrefilter('script', function(options) {
    options.cache = true;
});

$(document).ajaxSend(function(event, jqXHR, ajaxOptions) {
    jqXHR.setRequestHeader('client-api-key', getCookie("key"));
});

function SiteSettings(){
    var deviceType = window.location.pathname.split("/")[1];
    if (deviceType === "tablet") {
        this.theme = "android";
    } else if (deviceType === "phone") {
        this.theme = "android";
    } else {
        this.theme = "metrodark";
    }
}
SiteSettings.prototype.getTheme = function(){
    return this.theme;
};
var siteSettings = new SiteSettings();

window.addEventListener('resize', function(event){
  try {
      setSizes();
  } catch (err){}
});

/**
 * Center a div absolutely on screen.
 * @returns {jQuery.fn}
 */
jQuery.fn.center = function () {
    this.css("position","absolute");
    this.css("top", Math.max(0, (($(window).height() - $(this).outerHeight()) / 2) + 
                                                $(window).scrollTop()) + "px");
    this.css("left", Math.max(0, (($(window).width() - $(this).outerWidth()) / 2) + 
                                                $(window).scrollLeft()) + "px");
    var self = this;
    $(window).resize(function(){
        self.css("top", Math.max(0, (($(window).height() - $(self).outerHeight()) / 2) + 
                                                    $(window).scrollTop()) + "px");
        self.css("left", Math.max(0, (($(window).width() - $(self).outerWidth()) / 2) + 
                                                    $(window).scrollLeft()) + "px");
    });
    return this;
}

/**
 * Center a div absolutely on screen.
 * @returns {jQuery.fn}
 */
jQuery.fn.centerTop = function () {
    this.css("position","absolute");
    this.css("top", Math.max(0, (($(window).height()/2 - $(this).outerHeight()) / 2) + 
                                                $(window).scrollTop()) + "px");
    this.css("left", Math.max(0, (($(window).width() - $(this).outerWidth()) / 2) + 
                                                $(window).scrollLeft()) + "px");
    var self = this;
    $(window).resize(function(){
        self.css("top", Math.max(0, (($(window).height()/2 - $(self).outerHeight()) / 2) + 
                                                    $(window).scrollTop()) + "px");
        self.css("left", Math.max(0, (($(window).width() - $(self).outerWidth()) / 2) + 
                                                    $(window).scrollLeft()) + "px");
    });
    return this;
}

/**
 * Generates a random id.
 * @returns {String}
 */
function makeRandId(){
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    for( var i=0; i < 10; i++ ){
        text += possible.charAt(Math.floor(Math.random() * possible.length));
    }
    return text;
}

/**
 * Get week number convenience method.
 * @returns {Number}
 */
Date.prototype.getWeek = function () { return $.datepicker.iso8601Week(this); }
/**
 * Global for selected menu item
 * @type Number|menuId
 */
var menuindexselecteditem = 0;
/**
 * Sets the breadcrumbs
 * @param {type} menuIndex
 * @param {type} selectedId
 * @param {type} menuItem
 * @param {type} href
 * @returns {undefined}
 */
function setPageBreadcrumbs(menuIndex, selectedId, menuItem, href, subItem) {
    var path = [];
    $('#mainmenubar *').each(function(){
        $(this).parent().removeClass("active");
    })
    switch (menuIndex) {
        case 1:
            path[0] = "Control";
            break;
        case 2:
            path[0] = "Management";
            break;
        case 3:
            path[0] = "Automation";
            break;
        case 4:
            path[0] = "Designers";
            break;
        case 5:
            path[0] =  "Community";
            break;
        case 6:
            path[0] =  "Audit/Logs";
            break;
        case 7:
            path[0] =  "Security";
            break;
        case 8:
            path[0] =  "System";
            break;
        default:
            path[0] =  "";
            break;
    }
    $('.menu-parent[data-index="'+menuIndex+'"]').parent().addClass("active");
    $('#' + selectedId).addClass("active");
    $('#' + selectedId).parent().parent().addClass("active");
    path[1] = menuItem;
    var fullstring = "<li><a href=\"/index.html\" onclick=\"return loadPageContent(this, 0, 'page_dashboard');\"><img src=\"/shared/images/icons/home.png\" alt=\"Dashboard\" /> Dashboard</a><span class=\"breadcrumbsarrow first\"  style=\"display:none;\">&gt;</span></li>";
    if (path[0]!==""){
        fullstring += "<li><a href=\"#\">"+path[0]+"</a><span class=\"breadcrumbsarrow\">&gt;</span></li>";
        fullstring += "<li><a href=\""+href+"\" onclick=\"return loadPageContent($('#"+selectedId+" a'), "+menuIndex+", '"+selectedId+"');\">"+menuItem+"</a><span class=\"breadcrumbsarrow last\" style=\"display:none;\">&gt;</span></li>";
        if(subItem!==undefined){
            fullstring += "<li>"+subItem+"</li>";
        }
    }
    setPageTitleHeader(menuItem);
    $("#breadcrumbpath").html(fullstring);
    if (path[0]!==""){
        $(".breadcrumbsarrow.first").show();
        if(subItem!==undefined){
            $(".breadcrumbsarrow.last").show();
        }
    }
};
/**
 * loads the page contents via ajax
 * @param {type} aEl
 * @param {type} menuId
 * @param {type} selectedId
 * @returns {Boolean}
 */
function loadPageContent(aEl, menuId, selectedId){
    pidomeRPCSocket.clearFallbacks();
    if (typeof clearHandlers === 'function') { 
        try {
            clearHandlers(); 
        } catch (err){ }
    }
    try {
        clearInternalWidgetHandlers("#contentbody");
    } catch (err) {}
    try {
        window.history.pushState("", "", $(aEl).attr("href"));
    } catch (err){}
    $("#contentbody").empty();
    $("#contentbody").load($(aEl).attr("href") + "?requesttype=ajax", function( response, status, xhr ) {
        if ( status === "error" ) {
          showErrorMessage("Navigation error","Could not load page, Please report this error" + "<br/><br/>" + 
                  "Additional data: <br/>" +
                  "Page: " + $(aEl).text() + "<br/>" + 
                  "Url: " + $(aEl).attr("href") + "<br/>" + 
                  "Error: " + xhr.status + " " + xhr.statusText);
        } else {
            $("#mainmenu li").each(function(){
                $(this).removeClass("selected");
            });
        }
        setPageBreadcrumbs(menuId, selectedId, $(aEl).text(), $(aEl).attr("href"));
        document.title = "PiDome - " + $(aEl).text();
    });
    return false;
}

function preparePageInfo(){
    if ($("#pageinfotext").length>0) {
        $("#pageinfoicon").show();
        if(!$("#pageinfotext").hasClass("created")){
            $("#pageinfocontent").empty();
            $("#dialog-pageinfo-title").text($("#pageheadertitle h1").text());
            $("#pageinfocontent").html($("#pageinfotext").html());
            $("#pageinfotext").remove();
        }
        $("#pageinfoicon").on("click",function(){
            if($("#dialog-pageinfo").is(':visible')){
                $("#dialog-pageinfo").modal("hide");
            } else {
                $("#dialog-pageinfo").modal("show");
            }
        });
    } else {
        $("#pageinfoicon").hide();
        $("#pageinfoicon").off("click");
    }
}

function clearInternalWidgetHandlers(masterFieldName){
    $("#pageinfoicon").hide();
    $("#pageinfoicon").off("click");
    $(masterFieldName + ">").find('*').each(function() {
        try { $(this).jqxDropDownButton('destroy'); } catch (err) {}
        try { $(this).jqxColorPicker('destroy'); } catch (err) {}
        try { $(this).jqxComboBox('destroy'); } catch (err) {}
        try { $(this).jqxDropDownList('destroy'); } catch (err) {}
        try { $(this).jqxTooltip('destroy'); } catch (err) {}
        try { $(this).jqxChart('destroy'); } catch (err) {}
        try { $(this).jqxSlider('destroy'); } catch (err) {}
        try { $(this).jqxButton('destroy'); } catch (err) {}
        try { $(this).jqxWindow('destroy'); } catch (err) {}
    });
    $(masterFieldName).empty();
}

/**
 * Used to reload the same page from ajax loaded content.
 * @param {type} href
 * @returns {undefined}
 */
function refreshPageContent(href){
    pidomeRPCSocket.clearFallbacks();
    if (typeof clearHandlers === 'function') { 
        try {
            clearHandlers(); 
        } catch (err){}
    }
    var loadRef;
    if(href.indexOf("?")!==-1){
        loadRef = href + "&requesttype=ajax";
    } else {
        loadRef = href + "?requesttype=ajax";
    }
    try {
        window.history.pushState("", "", href);
    } catch (err){}
    $("#contentbody").load(loadRef, function( response, status, xhr ) {
        if ( status === "error" ) {
          showErrorMessage("Refresh error","Could not refresh page, Please report this error" + "<br/><br/>" + 
                  "Additional data: <br/>" + 
                  "Url    : " + href + "<br/>" + 
                  "Error  : " + xhr.status + " " + xhr.statusText);
        }
    });
    return false;
}

/**
 * Sets the h1 on the page
 * @param {type} title
 * @returns {undefined}
 */
function setPageTitleHeader(title){
    $("#pageheadertitle h1").html(title);
    $("#pageheadertitle h1").show();
}

function setPageTitleDescription(description){
    $("#simpleheaderdescription").html(description);
    $("#simpleheaderdescription").show();
}

function runDeviceCommand(id, group, control, value, extra, dataType){
    var url;
    if(typeof dataType !== "undefined" && dataType==="string"){
        url = "{\"jsonrpc\": \"2.0\", \"method\": \"DeviceService.sendDevice\", \"params\": ["+id+", \""+group+"\", \""+control+"\", {\"value\":\""+value+"\",\"extra\":\""+extra+"\"}], \"id\": \"DeviceService.sendDevice\"}";
    } else {
        url = "{\"jsonrpc\": \"2.0\", \"method\": \"DeviceService.sendDevice\", \"params\": ["+id+", \""+group+"\", \""+control+"\", {\"value\":"+value+",\"extra\":\""+extra+"\"}], \"id\": \"DeviceService.sendDevice\"}";
    }
    getHttpJsonRPC(url, null);
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
            if ($("#"+deviceId+"_deviceSelectFor_" + controlId).length !== 0) {
                setDeviceSelectValue(deviceId, controlId, paramSet.groups[i].controls[controlSetId]);
            } else if ($("."+deviceId+"_deviceLabelFor_" + controlId).length !== 0) {
                setDeviceDataValues(deviceId, controlId, paramSet.groups[i].controls[controlSetId]);
            } else if ($("#"+deviceId+"_deviceColorPickerFor_" + controlId).length !== 0 || $('.blockTypePreview_'+deviceId+'_'+controlId).length!==0 || $('.dashDeviceColorPickerFor_' + deviceId + '-' + controlId + ' path').length!==0) {
                setDeviceColorPickerValues(deviceId, controlId, paramSet.groups[i].controls[controlSetId]);
            }
            if($('.deviceCommandToggle_' + deviceId + '-' + groupId + '-' + controlSetId).length !== 0) {
                switchDeviceToggleValue(deviceId, groupId, controlSetId, paramSet.groups[i].controls[controlSetId]);
            }
            if($('#deviceCommandPush_' + deviceId + '-' + groupId + '-' + controlSetId).length !== 0) {
                switchDevicePushValue(deviceId, groupId, controlSetId, paramSet.groups[i].controls[controlSetId]);
            }
            if($('.deviceSliderFor_' + deviceId + '-' + groupId + '-' + controlSetId).length !== 0) {
                switchDeviceSliderValue(deviceId, groupId, controlSetId, paramSet.groups[i].controls[controlSetId]);
            }
            if($('.deviceCommandSelect_' + deviceId + '-' + groupId + '-' + controlSetId).length !== 0) {
                switchDeviceSelectValue(deviceId, groupId, controlSetId, paramSet.groups[i].controls[controlSetId]);
            }
        }
    }
}

function setDeviceColorPickerValues(deviceId, controlId, value){
    var localSetColorObject = {h: value.hsb.h*360, s: value.hsb.s, v: value.hsb.b};
    if ($("."+deviceId+"_deviceColorPickerFor_" + controlId).length !== 0) {
        try {  
            $("."+deviceId+"_deviceColorPickerFor_" + controlId).trigger("colorpickersliders.updateColor", localSetColorObject);
        } catch (err){}
    }
    $('.blockTypePreview_'+deviceId+'_'+controlId).attr("style", "width: 100px; " + createGradientCSSForButtonFromTinyColors(tinycolor(localSetColorObject)));
    $('.dashDeviceColorPickerFor_' + deviceId + '-' + controlId + ' path').css("fill", value.hex);
}
/**
 * Sets the values for the data labels for devices.
 * @param {type} deviceId
 * @param {type} controlId
 * @param {type} value
 * @returns {undefined}
 */
function setDeviceDataValues(deviceId, controlId, value){
    if ($("."+deviceId+"_deviceLabelFor_" + controlId).length !== 0) {
        $("."+deviceId+"_deviceLabelFor_" + controlId).each(function(){
            if($(this).attr("data-datatype")==="boolean"){
                setBooleanColor($(this), value);
            } else {
                $(this).text(value);
            }
        });
    }
}

function switchDeviceSelectValue(deviceId, groupId, controlId, value){
    $('.deviceCommandSelect_' + deviceId + '-' + groupId + '-' + controlId).val(value.toString());
}

function setBooleanColor(element, value){
    switch(element.attr("data-booltype")){
        case "color":
            element.parent().parent().css("background-color", ((value===true)?"#42c873":"#dc5945"));
            element.html("&nbsp;&nbsp;&nbsp;&nbsp;");
        break;
        case "text_color":
            element.parent().parent().css("background-color", ((value===true)?"#42c873":"#dc5945"));
        break;
    }
    if(element.attr("data-booltype")!=="color"){
        if(value===true){
            element.text(((element.attr("data-booltrue")==="undefined")?value:element.attr("data-booltrue")));
        } else {
            element.text(((element.attr("data-boolfalse")==="undefined")?value:element.attr("data-boolfalse")));
        }
    }
}


function switchDeviceToggleValue(deviceId, groupId, controlId, buttonValue) {
    $('.deviceCommandToggle_' + deviceId + '-' + groupId + '-' + controlId).each(function(){
        if($(this).attr("data-basecontroltype")){
            $(this).bootstrapSwitch('state', buttonValue, true);
        } else {
            var switchComponent = $(this);
            if (switchComponent) {
                if (buttonValue === true) {
                    if (!switchComponent.hasClass("on")) {
                        switchComponent.addClass("on");
                    }
                    switchComponent.removeClass("off");
                } else {
                    if (!switchComponent.hasClass("off")) {
                        switchComponent.addClass("off");
                    }
                    switchComponent.removeClass("on");
                }
            }
        }
    });
}
function switchDevicePushValue(deviceId, groupId, controlId, buttonValue) {
    var switchComponent = $('#deviceCommandPush_' + deviceId + '-' + groupId + '-' + controlId);
    if (switchComponent) {
        if (!switchComponent.hasClass("on")) {
            setTimeout( function(){
                switchComponent.removeClass("on");
            }, 1000 );
            switchComponent.addClass("on");
        }
    }
}
function switchDeviceSliderValue(deviceId, groupId, controlId, newVal) {
    $('.deviceSliderFor_' + deviceId + '-' + groupId + '-' + controlId).each(function(){
        if($(this).attr("data-basecontroltype")){
            /// Bootstrap
            $('.deviceSliderFor_' + deviceId + '-' + groupId + '-' + controlId).bootstrapSlider('setValue', newVal);
            $('.deviceSliderFor_' + deviceId + '-' + groupId + '-' + controlId + '_visibleValue').text(newVal);
        } else {
            /// gauge like
            $('.deviceSliderFor_' + deviceId + '-' + groupId + '-' + controlId).val(newVal).trigger('change');
        }
    });
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
        $("#"+deviceId+"_deviceSliderFor_" + selectId).jqxSlider('setValue', valueId);
    }
}
function createPiDomeRangedSlider(el, min, max, cur, datatype, callBack, width, height){
    el.knob({
        'min':min,
        'max':max,
        'width': ((typeof width === "undefined")?el.parent().width():width),
        'height': ((typeof height === "undefined")?el.parent().height():height),
        'thickness': .42,
        'angleArc': 250,
        'angleOffset': -125,
        'displayPrevious': true,
        'displayInput':true,
        'step': ((datatype==="integer")?1:0.01),
        'release' : function (v) { if(typeof callBack !=="undefined") callBack(v); }
    });
    el.val(cur).trigger('change');
}