/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Creates random uuid.
 * @returns {String}
 */
function createUUID() {
    // http://www.ietf.org/rfc/rfc4122.txt
    var s = [];
    var hexDigits = "0123456789abcdef";
    for (var i = 0; i < 36; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
    }
    s[14] = "4";  // bits 12-15 of the time_hi_and_version field to 0010
    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);  // bits 6-7 of the clock_seq_hi_and_reserved to 01
    s[8] = s[13] = s[18] = s[23] = "-";

    var uuid = s.join("");
    return uuid;
}
/**
 * Creates a String representation of an xml document.
 * @param {type} xmlData
 * @returns {unresolved}
 */
function xmlToString(xmlData) { // this functions waits jQuery XML 
    var xmlString = undefined;
    if (window.ActiveXObject) {
        xmlString = xmlData[0].xml;
    }
    if (xmlString === undefined) {
        var oSerializer = new XMLSerializer();
        xmlString = oSerializer.serializeToString(xmlData[0]);
    }
    return xmlString;
}

/**
 * Makes a json representation from the device xml
 * @param {type} xml
 * @returns {Array|xmlToJson.obj}
 */
function xmlToJson(xml) {
    // Create the return object
    var obj = {};
    if (xml.nodeType === 1) { // element
        // do attributes
        if (xml.attributes.length > 0) {
            obj["@attributes"] = {};
            for (var j = 0; j < xml.attributes.length; j++) {
                var attribute = xml.attributes.item(j);
                obj["@attributes"][attribute.nodeName] = attribute.value.trim();
            }
        }
    } else if (xml.nodeType === 3) { // text
        obj = xml.nodeValue.trim();
    }

    // do children
    if (xml.hasChildNodes()) {
        for (var i = 0; i < xml.childNodes.length; i++) {
            var item = xml.childNodes.item(i);
            var nodeName = item.nodeName;
            if (typeof (obj[nodeName]) == "undefined") {
                obj[nodeName] = [];
            }
            obj[nodeName].push(xmlToJson(item));
        }
    }
    return obj;
}

/**
 * Set a cookie
 * @param {type} cname
 * @param {type} cvalue
 * @param {type} exdays
 * @returns {undefined}
 */
function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
    var expires = "expires=" + d.toUTCString();
    document.cookie = cname + "=" + cvalue + "; " + expires;
}

/**
 * Gets a cookie.
 * @param {type} cname
 * @returns {String}
 */
function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ')
            c = c.substring(1);
        if (c.indexOf(name) != -1)
            return c.substring(name.length, c.length);
    }
    return "";
}

/**
 * Extending javascript adding an convenient function startswith
 * @param {type} string
 * @returns {boolean} if a string starts with given string.
 */
if (typeof String.prototype.startsWith !== 'function') {
    String.prototype.startsWith = function (str) {
        return this.indexOf(str) === 0;
    };
}

jQuery.fn.extend({
    startsWith: function (str) {
        return this.indexOf(str) === 0;
    }
});

$.urlParam = function (name) {
    var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
    if (results === null) {
        return null;
    }
    else {
        return results[1] || 0;
    }
}

var Base64 = {
    _keyStr: "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",
    encode: function (input) {
        var output = "";
        var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
        var i = 0;

        input = Base64._utf8_encode(input);

        while (i < input.length) {

            chr1 = input.charCodeAt(i++);
            chr2 = input.charCodeAt(i++);
            chr3 = input.charCodeAt(i++);

            enc1 = chr1 >> 2;
            enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
            enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
            enc4 = chr3 & 63;

            if (isNaN(chr2)) {
                enc3 = enc4 = 64;
            } else if (isNaN(chr3)) {
                enc4 = 64;
            }

            output = output + this._keyStr.charAt(enc1) + this._keyStr.charAt(enc2) + this._keyStr.charAt(enc3) + this._keyStr.charAt(enc4);

        }

        return output;
    },
    decode: function (input) {
        var output = "";
        var chr1, chr2, chr3;
        var enc1, enc2, enc3, enc4;
        var i = 0;

        input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

        while (i < input.length) {

            enc1 = this._keyStr.indexOf(input.charAt(i++));
            enc2 = this._keyStr.indexOf(input.charAt(i++));
            enc3 = this._keyStr.indexOf(input.charAt(i++));
            enc4 = this._keyStr.indexOf(input.charAt(i++));

            chr1 = (enc1 << 2) | (enc2 >> 4);
            chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
            chr3 = ((enc3 & 3) << 6) | enc4;

            output = output + String.fromCharCode(chr1);

            if (enc3 != 64) {
                output = output + String.fromCharCode(chr2);
            }
            if (enc4 != 64) {
                output = output + String.fromCharCode(chr3);
            }

        }

        output = Base64._utf8_decode(output);

        return output;

    },
    _utf8_encode: function (string) {
        string = string.replace(/\r\n/g, "\n");
        var utftext = "";

        for (var n = 0; n < string.length; n++) {

            var c = string.charCodeAt(n);

            if (c < 128) {
                utftext += String.fromCharCode(c);
            }
            else if ((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            }
            else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }

        }

        return utftext;
    },
    _utf8_decode: function (utftext) {
        var string = "";
        var i = 0;
        var c = c1 = c2 = 0;

        while (i < utftext.length) {

            c = utftext.charCodeAt(i);

            if (c < 128) {
                string += String.fromCharCode(c);
                i++;
            }
            else if ((c > 191) && (c < 224)) {
                c2 = utftext.charCodeAt(i + 1);
                string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                i += 2;
            }
            else {
                c2 = utftext.charCodeAt(i + 1);
                c3 = utftext.charCodeAt(i + 2);
                string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                i += 3;
            }

        }

        return string;
    }

}

/**
 * Extending the array with a proto to move to an other position within an array.
 * @param {type} from
 * @param {type} to
 * @returns {Array.prototype}
 */
Array.prototype.movePos = function (from, to) {
    this.splice(to, 0, this.splice(from, 1)[0]);
    return this;
};

/**
 * Simple function to add a leading zero.
 * @param {type} n
 * @returns {String}
 */
function padZeros(n) {
    return n < 10 ? '0' + n : n;
}

/**
 * Shows error message.
 * @param {type} title
 * @param {type} message
 * @returns {undefined}
 */
function showErrorMessage(title, message) {
    showModalMessage('error', title, message);
}

/**
 * Shows a warning popup message
 * @param {type} title
 * @param {type} message
 * @returns {undefined}
 */
function showWarningMessage(title, message) {
    showModalMessage('warning', title, message);
}

/**
 * Shows an informational popup
 * @param {type} title
 * @param {type} message
 * @returns {undefined}
 */
function showInfoMessage(title, message) {
    showModalMessage('info', title, message);
}

/**
 * Get an http rpc call.
 * @param {type} rpcCall
 * @param {type} callback
 * @returns {undefined}
 */
function getHttpJsonRPC(rpcCall, callback, silent) {
    $.get('/jsonrpc.json?rpc=' + rpcCall).done(function (data) {
        handleJsonResultData(data, rpcCall, callback, silent);
    });
}

function handleJsonResultData(response, rpcCall, callback, silent) {
    var beQuiet = (silent !== undefined && silent === true);
    if (typeof response.error === 'undefined') {
        if (response.result.success !== true) {
            if (!beQuiet)
                extendedPageError("Error executing request", response.result.data.message, null, rpcCall, response.result.data.trace);
        } else {
            if (typeof callback !== 'undefined' && callback !== null) {
                callback(response.result.data);
            }
        }
    } else {
        if (!beQuiet){
            if (typeof response.error.data.trace !== 'undefined'){
                extendedPageError("Error executing request", response.error.message + "(" + response.error.code + ")", response.error.data.message, rpcCall, response.error.data.trace);
            } else {
                extendedPageError("Error executing request", response.error.message + "(" + response.error.code + ")", response.error.data.message, rpcCall);
            }
        }
    }
}

function postHttpJsonRPC(rpcCall, callback, silent) {
    $.post('/jsonrpc.json', {rpc: rpcCall}).done(function (data) {
        handleJsonResultData(data, rpcCall, callback, silent);
    });
}

function returnResultData(response, rpcCall) {
    if (typeof response.error === 'undefined') {
        if (response.result.success !== true) {
            extendedPageError("Error executing request", response.result.data.message, null, (typeof rpcCall!=='undefined')?rpcCall:null, response.result.data.trace);
        } else {
            return response.result.data;
        }
    } else {
        if (typeof response.error.data.trace !== 'undefined'){
            extendedPageError("Error executing request", response.error.message + "(" + response.error.code + ")", response.error.data.message, (typeof rpcCall!=='undefined')?rpcCall:null, response.error.data.trace);
        } else {
            extendedPageError("Error executing request", response.error.message + "(" + response.error.code + ")", response.error.data.message, (typeof rpcCall!=='undefined')?rpcCall:null);
        }
    }
    return [];
}

function extendedPageError(title, message, reason, request, trace) {
    $("#errorrequestbody").collapse('hide');
    $("#errortracebody").collapse('hide');
    $("#dialog-pageerror-title").text(title);
    $("#pageerrorcontentmessage").html("Error message: " + message);
    if (typeof reason !== 'undefined' && reason !== null) {
        $("#pageerrorreasonmessageholder").show();
        $("#pageerrorreasonmessage").html("Reason: " + reason);
    } else {
        $("#pageerrorreasonmessage").html("");
        $("#pageerrorreasonmessageholder").hide();
    }
    if (typeof request !== 'undefined' && request !== null) {
        $("#pageerrorrequestmessage .panel-body").text(request);
        $("#pageerrorrequestmessage").show();
    } else {
        $("#pageerrorrequestmessage .panel-body").text("");
        $("#pageerrorrequestmessage").hide();
    }
    if (typeof trace !== 'undefined' && trace !== null) {
        $("#tracePreData").html(trace.replace(/(\\n)+/g, '<br />'));
        $("#pageerrortracemessage").show();
    } else {
        $("#tracePreData").html("");
        $("#pageerrortracemessage").hide();
    }
    if ((typeof trace !== 'undefined' && trace !== null) || (typeof request !== 'undefined' && request !== null)) {
        $("#dialog-pageerror-size").addClass("modal-lg");
    } else {
        $("#dialog-pageerror-size").removeClass("modal-lg");
    }
    $("#dialog-pageerror").modal('show');
}

function quickMessage(type, message, length) {
    var classType = '';
    switch (type) {
        case "error":
            classType = 'alert-danger';
            break;
        case "info":
            classType = 'alert-info';
            break;
        case "warning":
            classType = 'alert-warning';
            break;
        default:
            classType = 'alert-success';
            break;
    }
    var tmpId = createUUID();
    $("body").append('<div id="'+tmpId+'" class="alert ' + classType + ' alert-dismissible fade in" role="alert" style="min-width: 300px;box-shadow: 0 0 30px black;">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            message +
            '</div>');
    $('#'+tmpId).centerTop();
    $('#'+tmpId).on('closed.bs.alert', function () {
        $(this).remove();
    })
    setTimeout(function () {
        $('#'+tmpId).alert('close');
    }, ((typeof length==="undefined")?2000:length));
}

function getTableRowData(tableId, rowNumber){
    var data = $(tableId).bootstrapTable('getData');
    var row = parseInt(rowNumber);
    for(var i=0; i<data.length;i++){
        if(i===row){
            return data[i];
        }
    }
}

function yesnoConfirmation(title, message, callback, yesText, noText, noCallBack){
    $("body").append(
        '<div class="modal fade" id="dialog-yesnoconfirmation" tabindex="-1" role="dialog" aria-labelledby="yesnoconfirmation" aria-hidden="true">'+
            '<div class="modal-dialog">'+
                '<div class="modal-content">'+
                    '<div class="modal-header">'+
                        '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'+
                        '<h4 class="modal-title" id="dialog-yesnoconfirmation-title">'+title+'</h4>'+
                    '</div>'+
                    '<div class="modal-body" id="pageinfocontent">'+
                    message +
                    '</div>'+
                    '<div class="modal-footer">'+
                        '<button type="button" class="btn btn-success" id="dialog-yesnoconfirmation-confirmed">'+((typeof yesText==="undefined")?"Yes":yesText)+'</button>'+
                        '<button type="button" class="btn btn-danger" id="dialog-yesnoconfirmation-dismissed" data-dismiss="modal">'+((typeof noText==="undefined")?"No":noText)+'</button>'+
                    '</div>'+
                '</div>'+
            '</div>'+
        '</div>');
    $('#dialog-yesnoconfirmation').on('hidden.bs.modal', function (e) {
        $(this).remove();
    })
    $("#dialog-yesnoconfirmation-confirmed").on("click", function(){
        var result = callback();
        if(result !== false){
            $("#dialog-yesnoconfirmation").modal("hide");
        }
    });
    $("#dialog-yesnoconfirmation-dismissed").on("click", function(){
        if(typeof noCallBack !== "undefined" && noCallBack !== null){
            var result = noCallBack();
            if(result !== false){
                $("#dialog-yesnoconfirmation").modal("hide");
            }
        } else {
            $("#dialog-yesnoconfirmation").modal("hide");
        }
    });
    $("#dialog-yesnoconfirmation").modal("show");
}

function simpleDialog(title, message, callback, closeText){
    $("body").append(
        '<div class="modal fade" id="dialog-simpledialog" tabindex="-1" role="dialog" aria-labelledby="simpledialog" aria-hidden="true">'+
            '<div class="modal-dialog">'+
                '<div class="modal-content">'+
                    '<div class="modal-header">'+
                        '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'+
                        '<h4 class="modal-title" id="dialog-simpledialog-title">'+title+'</h4>'+
                    '</div>'+
                    '<div class="modal-body" id="simpledialog-content">'+
                    message +
                    '</div>'+
                    '<div class="modal-footer">'+
                        '<button type="button" class="btn btn-success" id="dialog-simpledialog-confirmed">'+((typeof closeText==="undefined")?"Close":closeText)+'</button>'+
                    '</div>'+
                '</div>'+
            '</div>'+
        '</div>');
    $('#dialog-simpledialog').on('hidden.bs.modal', function (e) {
        $(this).remove();
    })
    $("#dialog-simpledialog-confirmed").on("click", function(){
        var result = callback();
        if(result !== false){
            $("#dialog-simpledialog").modal("hide");
        }
    });
    $("#dialog-simpledialog").modal("show");
}

/*
 * Convert seconds to HH:mm:ss
 * @returns {String}
 */
Number.prototype.toHHMMSS = function () {
    var sec_num = parseInt(this, 10); // don't forget the second param
    var hours = Math.floor(sec_num / 3600);
    var minutes = Math.floor((sec_num - (hours * 3600)) / 60);
    var seconds = sec_num - (hours * 3600) - (minutes * 60);

    if (hours < 10) {
        hours = "0" + hours;
    }
    if (minutes < 10) {
        minutes = "0" + minutes;
    }
    if (seconds < 10) {
        seconds = "0" + seconds;
    }
    var time = hours + ':' + minutes + ':' + seconds;
    return time;
}


/**
 * Main popup function.
 * @param {type} type
 * @param {type} title
 * @param {type} message
 * @returns {undefined}
 */
function showModalMessage(type, title, message) {
    var content = '<p>' + message + '</p><div><input type="button" id="closemessageNotification" value="Close" /></div>';
    var img = '';
    switch (type) {
        case 'error':
            img = '<img src="/shared/images/icons/error.png" alt="error" style="float:left; height:16px; width:16px; margin-right: 5px;"/>';
            break;
        case 'warning':
            img = '<img src="/shared/images/icons/warning.png" alt="error" style="float:left; height:16px; width:16px; margin-right: 5px;"/>';
            break;
        default:
            img = '<img src="/shared/images/icons/info.png" alt="error" style="float:left; height:16px; width:16px; margin-right: 5px;"/>';
            break;
    }
    $("#messageNotification").jqxWindow({
        width: 400,
        theme: siteSettings.getTheme(),
        autoOpen: false,
        isModal: true,
        okButton: $('#closemessageNotification'),
        title: img + title,
        content: content,
        initContent: function () {
            $('#closemessageNotification').jqxButton({
                width: '65px',
                theme: siteSettings.getTheme()
            });
            $('#closemessageNotification').focus();
            $('#closemessageNotification').on("click", function () {
                try {
                    $('#closemessageNotification').off("click");
                } catch (err) {
                }
                try {
                    $("#messageNotification").destroy();
                } catch (err) {
                }
            });
        }
    });
    $('#messageNotification').jqxWindow({okButton: $('#closemessageNotification')});
    $('#messageNotification').jqxWindow('open');
}

/**
 * Checks if a file exists.
 * @param {type} url
 * @returns {Boolean}
 */
function fileExists(url) {
    if (url) {
        var req = new XMLHttpRequest();
        req.open('GET', url, false);
        req.send();
        return req.status === 200;
    } else {
        return false;
    }
}

/**
 * Loads a device category image, and if not exists the unknown image
 * @param {type} id
 * @param {type} cat
 * @returns {undefined}
 */
function setDeviceCatImage(id, cat) {
    try {
        if (fileExists("/shared/images/devices/" + cat + "-small.png")) {
            $("#" + id).attr("src", "/shared/images/devices/" + cat + "-small.png");
        } else {
            $("#" + id).attr("src", "/shared/images/devices/UNKNOWN-small.png");
        }
        ;
    } catch (err) {
    }
}

/**
 * Loads a device category image, and if not exists the unknown image
 * @param {type} cat
 * @returns {undefined}
 */
function getDeviceCatImage(cat) {
    try {
        if (fileExists("/shared/images/devices/" + cat + "-small.png")) {
            return "/shared/images/devices/" + cat + "-small.png";
        } else {
            return "/shared/images/devices/UNKNOWN-small.png";
        }
        ;
    } catch (err) {
    }
}


/**
 * Used in combination with the color picker.
 * @param {type} color
 * @returns {getTextElementByColor.element}
 */
function getTextElementByColor(color) {
    if (color === 'transparent' || color.hex === "") {
        return $("<div style='text-shadow: none; position: relative; padding-bottom: 2px; margin-top: 2px;'>transparent</div>");
    }
    var element = $("<div style='text-shadow: none; position: relative; padding-bottom: 2px; margin-top: 2px;'>#" + color.hex + "</div>");
    var nThreshold = 105;
    var bgDelta = (color.r * 0.299) + (color.g * 0.587) + (color.b * 0.114);
    var foreColor = (255 - bgDelta < nThreshold) ? 'Black' : 'White';
    element.css('color', foreColor);
    element.css('background', "#" + color.hex);
    element.addClass('jqx-rc-all');
    return element;
}


function geoLocation() {
}

/**
 * Tries to ask for geo data from the browser.
 * @returns {undefined}
 */
geoLocation.prototype.getBrowserDeviceGeoData = function (callbackFunction) {
    var self = this;
    geoaskingcallbackfunction = callbackFunction;
    navigator.geolocation.getCurrentPosition(
            self.gotBrowserDeviceGeoData,
            self.errorBrowserDeviceGeoData,
            {'enableHighAccuracy': true, 'timeout': 10000, 'maximumAge': 0});
};

/**
 * executed when we got browser data.
 * @param {type} pos
 * @returns {undefined}
 */
geoLocation.prototype.gotBrowserDeviceGeoData = function (pos) {
    var lat = pos.coords.latitude;
    var lon = pos.coords.longitude;
    geoaskingcallbackfunction(lat, lon);
};

/**
 * Executed when there is an error retrieving the data
 * @param {type} err
 * @returns {undefined}
 */
geoLocation.prototype.errorBrowserDeviceGeoData = function (err) {
    var message = "Unknown error";
    switch (err.code) {
        case 1:
            message = "You have chosen to deny access to guess your position, please use an other resource.";
            break;
        case 2:
            message = "Position allocation is not supported via the browser or is unavailable.";
            break;
        case 3:
            message = "It took to long to acquire you're position, you can try it again/later or use an other resource.";
            break;
        default:
            message = "Undefined error: " + err.message;
            break;
    }
    showErrorMessage("Location retrieval error", message);
};

/**
 * Creates a default input field and puts validation on it.
 * @param {type} field
 * @returns {undefined}
 */
function createWebInputField(field) {
    createSizedWebInputField(field, 250);
}

/**
 * Creates an input field with a defined width.
 * @param {type} field
 * @param {type} sizeWidth
 * @returns {undefined}
 * @obsolete Not used anymore
 */
function createSizedWebInputField(field, sizeWidth, sizeHeight) {}

/**
 * Returns if a field is valid
 * @param {type} field
 * @returns {Boolean}
 */
function inputFieldValid(field) {
    return !$(field).hasClass("jqx-input-invalid");
}

/**
 * Only show ok when there is a positive value.
 * @param {type} field
 * @returns {Boolean}
 */
function inputOkNokPositiveInt(field) {
    if (parseInt($(field).val()) > 0) {
        inputOkNokVisible(field, true);
        return true;
    } else {
        inputOkNokVisible(field, false);
        return false;
    }
}

/**
 * Regular expression check
 * @param {type} field
 * @param {type} regEx
 * @returns {undefined}
 */
function inputOkNokVisibleRegExToUppercase(field, regEx) {
    $(field).val($(field).val().toUpperCase());
    inputOkNokVisible(field, new RegExp(regEx).test($(field).val()));
}

/**
 * Regular expression check
 * @param {type} field
 * @param {type} regEx
 * @returns {undefined}
 */
function inputOkNokVisibleRegEx(field, regEx) {
    inputOkNokVisible(field, new RegExp(regEx).test($(field).val()));
}

/**
 * Set input validation visible based on int value
 * @param {type} field
 * @param {type} status
 * @param {type} pos
 * @returns {undefined} */
function inputOkNokVisibleLength(field, status, pos) {
    try {
        if (status.pos >= pos - 1) {
            inputOkNokVisible(field, true);
        } else {
            inputOkNokVisible(field, false);
        }
    } catch (err) {
        inputOkNokVisible(field, false);
    }
}
/**
 * Set input validation visible. 
 * @param {type} field
 * @param {type} status
 * @returns {undefined} */
function inputOkNokVisible(field, status) {
    if (status === true) {
        if ($(field).hasClass("jqx-input-invalid")) {
            $(field).removeClass("jqx-input-invalid");
        }
        if (!$(field).hasClass("input-valid")) {
            $(field).addClass("input-valid");
        }
    } else {
        if ($(field).hasClass("input-valid")) {
            $(field).removeClass("input-valid");
        }
        if (!$(field).hasClass("jqx-input-invalid")) {
            $(field).addClass("jqx-input-invalid");
        }
    }
}

/**
 * Function to check if an element has a specific event attached.
 * @param {type} element
 * @param {type} event
 * @returns {Boolean}
 */
function divHasHandler(element, event) {
    var ev = $._data(element, 'events');
    return (ev && ev[event]) ? true : false;
}

/////////////////////////////////////////////
//// functions for visualization by drivers and plugins.

function WebPresentation() {
    this.functionDataCollection = new Array();

}

WebPresentation.prototype.createDataSet = function (headerName, getUrl, devicesFetchUrl, contentDiv, callBackCustomFunction, callBackDeviceFunction) {
    var self = this;
    $.get(getUrl).success(function (swData) {
        var html = '<div class="defaultcontent driversnvp" style="width:100%;"><h2>' + headerName + '</h2>';
        try {
            var data = swData.result.data;
            for (var i = 0; i < data.presentation.length; i++) {
                var presentData = data.presentation[i];
                html += '<h3>' + presentData.title + '</h3>\n\
                         <p>' + presentData.description + '</p>';
                for (var j = 0; j < presentData.content.length; j++) {
                    var content = presentData.content[j];
                    var endData = "";
                    switch (content.type) {
                        case "SIMPLE_NVP":
                            if (content.label === "custom_driver_function") {
                                var UUID = createUUID().replace(/-/g, '');
                                endData = self.createCustomDriverFunction(UUID, content.content);
                            } else {
                                html += '<div class="nvp">\n\
                                            <div class="n">' + content.label + '</div><span style="float:left">:&nbsp;</span>\n\
                                            <div class="v">' + content.content + '</div>\n\
                                        </div>';
                            }
                            html += endData;
                            break;
                        case "LIST_NVP":
                            for (var k = 0; k < content.content.length; k++) {
                                for (var label in content.content[k]) {
                                    if (label === "custom_driver_function") {
                                        try {
                                            var UUID = createUUID().replace(/-/g, '');
                                            endData = self.createCustomDriverFunction(UUID, content.content[k][label]);
                                        } catch (err) {
                                            alert(err);
                                        }
                                    } else {
                                        html += '<div class="nvp">\n\
                                                    <div class="n">' + label + '</div><span style="float:left">:&nbsp;</span>\n\
                                                    <div class="v">' + content.content[k][label] + '</div>\n\
                                                </div>';
                                    }
                                }
                            }
                            html += endData;
                            break;
                        case "COMPLEX_NVP":
                            for (var key in content.content) {
                                var contentSet = content.content[key];
                                html += '<div class="complexnvpcollapsible" style="margin-bottom: 10px;">\n\
                                            <div>' + key + '</div>\n\
                                            <div>\n\
                                                <div>';
                                for (var k = 0; k < contentSet.length; k++) {
                                    for (var n in contentSet[k]) {
                                        if (n === "custom_driver_function") {
                                            var UUID = createUUID().replace(/-/g, '');
                                            endData = self.createCustomDriverFunction(UUID, contentSet[k][n]);
                                        } else {
                                            html += '<div class="nvp">\n\
                                                        <div class="n" style="margin-left: 10px;">' + n + '</div><span style="float:left">:&nbsp;</span>\n\
                                                        <div class="v">' + contentSet[k][n] + '</div>\n\
                                                    </div>';
                                        }
                                    }
                                    html += '<hr size="1px" color="#007acc" style="clear:both;"/>';
                                }
                                html += endData;
                                html += '       </div>\n\
                                            </div>\n\
                                        </div>';
                            }
                            break;
                    }
                }
            }
        } catch (err) {
            html += '<p>No information available</p>';
        }
        html += '</div>';
        $('#' + contentDiv).html(html);
        try {
            $(".complexnvpcollapsible").jqxExpander({width: 610, theme: siteSettings.getTheme(), expanded: false});
        } catch (err) {
        }
        if ($('.customFunctionExec').length !== 0) {
            $(".customFunctionExec").jqxButton({width: '250', theme: siteSettings.getTheme()});
            $(".customFunctionExec").on('click', function () {
                var selectedFuncId = $(this).attr("id");
                for (var i = 0; i < self.functionDataCollection.length; i++) {
                    if (self.functionDataCollection[i].id === selectedFuncId) {
                        var funcData = self.functionDataCollection[i].content;
                        try {
                            callBackCustomFunction(funcData);
                        } catch (err) {
                            showErrorMessage("Function", "Function not executed " + err);
                        }
                    }
                }
            });
        }
        if ($('.mutationSelectDevice').length !== 0) {
            $(".mutationSelectDevice").jqxButton({width: '250', theme: siteSettings.getTheme()});
            $(".mutationSelectDevice").on('click', function () {
                var selectedFuncId = $(this).attr("id");
                if ($('#selectDeviceItemWindow').length === 0) {
                    $("#" + contentDiv).append('<div id="selectDeviceItemWindow" style="display:none;">' +
                            '<div>Select the device to use</div>' +
                            '<div>' +
                            '<p>Select the device to use. If the specified device does not exist please create one with the visual/xml device editor first.</p>' +
                            '<div class="nvp" style="width:100%">' +
                            '<div class="n" style="width:50%"><label for="useDeviceSelection">Select device to use</label></div>' +
                            '<div class="v" style="width:50%">' +
                            '<div id="useDeviceSelection"></div>' +
                            '</div>' +
                            '<div class="nvp" style="width:100%">' +
                            '<div class="n" style="width:50%">Device name</div>' +
                            '<div class="v" style="width:50%"><input type="text" name="useDeviceSelectionName" data-inputtype="string" id="useDeviceSelectionName" value="" /></div>' +
                            '</div>' +
                            '<div class="nvp" style="width:100%">' +
                            '<div class="n" style="width:50%">Location</div>' +
                            '<div class="v" style="width:50%"><div id="useDeviceSelectionLocation"></div></div>' +
                            '</div>' +
                            '<div class="nvp" style="width:100%">' +
                            '<div class="n" style="width:50%">Category</div>' +
                            '<div class="v" style="width:50%"><div id="useDeviceSelectionCategory"></div></div>' +
                            '</div>' +
                            '<div class="nvp" style="width:100%">' +
                            '<div class="n" style="width:50%"><button name="selectDeviceButton" id="selectDeviceButton" style="margin-top: 10px;">Select and start</button></div>' +
                            '<div class="v" style="width:50%"><button name="cancelSelectDeviceButton" id="cancelSelectDeviceButton" style="margin-top: 10px;">Cancel</button></div>' +
                            '</div>' +
                            '</div>' +
                            '</div>' +
                            '</div>');
                    createSizedWebInputField($("#useDeviceSelectionName"), 200);
                    $('#selectDeviceItemWindow').jqxWindow({
                        width: 550,
                        height: 240,
                        theme: siteSettings.getTheme(),
                        autoOpen: false,
                        isModal: true
                    });
                    var allDevicesList = {
                        datatype: "json",
                        datafields: [
                            {name: 'id', type: 'int'},
                            {name: 'name', type: 'string'}
                        ],
                        url: devicesFetchUrl,
                        root: "result>data"
                    };
                    var DataAllDevicesList = new $.jqx.dataAdapter(allDevicesList);
                    $("#useDeviceSelection").jqxComboBox({source: DataAllDevicesList, valueMember: "id", displayMember: "name", width: '200', theme: siteSettings.getTheme()});

                    var adapterSourceAvailableLocs = {
                        datatype: "json",
                        datafields: [
                            {name: 'id', type: 'int'},
                            {name: 'name', type: 'string'}
                        ],
                        url: '/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "LocationService.getLocations", "id":"LocationService.getLocations"}',
                        root: "result>data"
                    };
                    var allAdapterAvailableLocs = new $.jqx.dataAdapter(adapterSourceAvailableLocs);
                    $("#useDeviceSelectionLocation").jqxComboBox({selectedIndex: 0, autoComplete: true, searchMode: 'containsignorecase', source: allAdapterAvailableLocs, displayMember: "name", valueMember: "id", width: '200', height: '25', theme: siteSettings.getTheme()});

                    var adapterSourceAvailableCats = {
                        datatype: "json",
                        datafields: [
                            {name: 'id', type: 'int'},
                            {name: 'name', type: 'string'}
                        ],
                        url: '/jsonrpc.json?rpc={"jsonrpc":"2.0", "id": "CategoryService.getFullCategoryList","method": "CategoryService.getFullCategoryList"}',
                        root: "result>data"
                    };
                    var allAdapterAvailableLocs = new $.jqx.dataAdapter(adapterSourceAvailableCats);
                    $("#useDeviceSelectionCategory").jqxComboBox({selectedIndex: 9, autoComplete: true, searchMode: 'containsignorecase', source: allAdapterAvailableLocs, displayMember: "name", valueMember: "id", width: '200', height: '25', theme: siteSettings.getTheme()});

                    $('#selectDeviceButton').jqxButton({width: '150', theme: siteSettings.getTheme()});
                    $("#selectDeviceButton").on('click', function () {
                        for (var i = 0; i < self.functionDataCollection.length; i++) {
                            if (self.functionDataCollection[i].id === selectedFuncId) {
                                if ($("#useDeviceSelection").val() === "") {
                                    showErrorMessage("Selection error", "Select a correct device or cancel to dismiss");
                                } else {
                                    var funcData = self.functionDataCollection[i].content;
                                    funcData["device_name"] = $("#useDeviceSelectionName").val();
                                    funcData["device_locationid"] = parseInt($("#useDeviceSelectionLocation").val());
                                    funcData["device_categoryid"] = parseInt($("#useDeviceSelectionCategory").val());
                                    try {
                                        funcData["device_id"] = parseInt($("#useDeviceSelection").val());
                                        callBackDeviceFunction(funcData);
                                    } catch (err) {
                                        showErrorMessage("Device select error", "Incorrect device information, contact author.");
                                    }
                                    $('#selectDeviceItemWindow').jqxWindow('close');
                                }
                            }
                        }
                    });
                    $('#cancelSelectDeviceButton').jqxButton({width: '150', theme: siteSettings.getTheme()});
                    $("#cancelSelectDeviceButton").on('click', function () {
                        $('#selectDeviceItemWindow').jqxWindow('close');
                    });
                }
                $('#selectDeviceItemWindow').jqxWindow('open');
            });
        }
        if ($('.mutationAddNewDevice').length !== 0) {
            $(".mutationAddNewDevice").jqxButton({width: '250', theme: siteSettings.getTheme()});
        }
    }, "json").error(function (jqXHR, textStatus, errorThrown) {
        var html = '<div class="defaultcontent driversnvp" style="width:100%;"><h2>' + headerName + '</h2>';
        html += "<p>Could not interpret data received from server</p>";
        html += "<p>Error: " + textStatus + ", " + errorThrown + "</p>";
        $('#' + contentDiv).html(html);
    });
};


WebPresentation.prototype.createCustomDriverFunction = function (UUID, functionData) {
    var data = {id: "func_" + UUID, type: functionData.function_id, content: functionData};
    this.functionDataCollection.push(data);
    var html = '<div class="nvp">';
    switch (functionData.function_id) {
        case "addExistingNewDevice":
            html += '<button style="float:right; margin:5px; " class="mutationSelectDevice" id="func_' + UUID + '" name="mutationSelectDevice">Select from device list</button>';
            break;
        case "addAndCreateNewDevice":
            html += '<button style="float:right; margin:5px; " class="mutationAddNewDevice" id="func_' + UUID + '" name="mutationAddNewDevice">Auto create device</button>';
            break;
        case "customFunction":
            html += '<button style="float:right; margin:5px; " class="customFunctionExec" id="func_' + UUID + '" name="customFunctionExec">' + functionData.function_label + '</button>';
            break;
    }
    html += '</div>';
    return html;
}



/**
 * Creates device visuals
 * @param {type} tableId
 * @param {type} deviceData
 * @param {type} showHeaders
 * @param {type} onlyShortCut
 * @param {type} dashBlock
 * @returns {undefined}
 */
function composeDeviceVisuals(tableId, deviceData, showHeaders, onlyShortCut) {
    var pos = 1;
    $('#' + tableId).empty();
    for (var i = 0; i < deviceData.commandgroups.length; i++) {
        var groupData = deviceData.commandgroups[i];
        if (showHeaders) $('#' + tableId).append('<tr><td colspan="2"><h3 class="whiteheader devicegroup">' + groupData.name + '</h3></td></tr>');
        for (var j = 0; j < groupData.commands.length; j++) {
            var curCommand = groupData.commands[j];
            var groupCommandSet = groupData.id + '-' + curCommand.typedetails.id;
            var setScript = false;
            switch (curCommand.commandtype) {
                case "data":
                    if((onlyShortCut === true && typeof curCommand.typedetails.shortcut !== "undefined") && pos<=3){
                        pos+=1;
                        var timeout = '';
                        if(curCommand.status==="timeout"){
                            timeout = '<span class="label label-warning" title="'+curCommand.lastdatachange+'">*</span>';
                        }
                        $('#' + tableId).append(
                            '<div style="width:33%; float:left;">' + timeout + curCommand.typedetails.label + ':<br/>' + curCommand.typedetails.prefix + ' <span><span><span class="' + deviceData.id + '_deviceLabelFor_' + groupData.id + '-' + curCommand.typedetails.id + '" data-datatype="'+curCommand.typedetails.datatype+'" data-booltype="'+curCommand.typedetails.boolvis+'" data-booltrue="'+curCommand.typedetails.truetext+'" data-boolfalse="'+curCommand.typedetails.falsetext+'">' + curCommand.currentvalue + '</span> ' + curCommand.typedetails.suffix + '</span></span></div>'
                        );
                    } else if(onlyShortCut === false){
                        var timeout = '';
                        if(curCommand.status==="timeout"){
                            timeout = '<span class="label label-warning" title="'+curCommand.lastdatachange+'">*</span>';
                        }
                        $('#' + tableId).append(
                            '<tr style="padding-top:5px;"><td class="name">' + timeout + curCommand.typedetails.label + '</td>' +
                                '<td class="value" style="border:0px solid #000;"><span><span>' + curCommand.typedetails.prefix + ' <span class="' + deviceData.id + '_deviceLabelFor_' + groupData.id + '-' + curCommand.typedetails.id + '" id="' + deviceData.id + '_deviceLabelFor_' + groupData.id + '-' + curCommand.typedetails.id + '" data-datatype="'+curCommand.typedetails.datatype+'" data-booltype="'+curCommand.typedetails.boolvis+'" data-booltrue="'+curCommand.typedetails.truetext+'" data-boolfalse="'+curCommand.typedetails.falsetext+'">' + curCommand.currentvalue + '</span> ' + curCommand.typedetails.suffix + '</span></span>'+
                                ((curCommand.typedetails.graph === true) ?
                                    '<span class="devicehasdatagraph">'+
                                        '<img id="' + groupCommandSet + '_graphData_img" src="../shared/images/icons/graphicon.png" alt="Open graph" onclick="$(\'#devicegraphwindowpopup\').jqxWindow(\'open\');new Graphing(\'graphWindow\', \'${_SERVER.hostname}:${_SERVER.hostport?c}\', ' + deviceData.id + ', \'' + groupData.id + '\', \'' + curCommand.typedetails.id + '\', \'' + deviceData.friendlyname + ' - ' + curCommand.typedetails.label + '\', \'' + curCommand.typedetails.prefix + ' ' + curCommand.typedetails.suffix + '\', \'DEVICE\', \'' + curCommand.typedetails.graphtype + '\',2).createGraph(640, 300);" />'+
                                    '</span>' : 
                                    "") + 
                                '</td>'+
                            '</tr>'
                        );
                    }
                    if(curCommand.typedetails.datatype==="boolean"){
                        $('#' + tableId).append(
                            '<script>'+
                                'setBooleanColor($(".' + deviceData.id + '_deviceLabelFor_' + groupData.id + '-' + curCommand.typedetails.id + '"), ' + curCommand.currentvalue + ');'+
                            '<\/script>'
                        );
                    }
                break;
                case "button":
                    if((onlyShortCut === true && typeof curCommand.typedetails.shortcut !== "undefined") && pos<=3){
                        pos+=1;
                        $('#' + tableId).append(
                            '<div style="width:33%; float:left;">' +
                                '<button type="button" class="' + deviceData.id + '_deviceAction-' + groupCommandSet + ' btn btn-success" data-command="'+curCommand.typedetails.deviceCommandValue+'" data-group="' + groupData.id + '" data-control="' + curCommand.typedetails.id + '" data-id="' + deviceData.id + '" name="deviceAction-' + groupCommandSet + '">' + curCommand.typedetails.label + '</button>'+
                            '</div>'
                        );
                        setScript = true;
                    } else if(onlyShortCut === false){
                        $('#' + tableId).append(
                            '<tr style="padding-top:5px;"><td class="name">' + curCommand.typedetails.label + '</td>' +
                                '<td class="value">'+
                                '<button type="button" class="' + deviceData.id + '_deviceAction-' + groupCommandSet + ' btn btn-success" data-command="'+curCommand.typedetails.deviceCommandValue+'" data-group="' + groupData.id + '" data-control="' + curCommand.typedetails.id + '" data-id="' + deviceData.id + '" name="deviceAction-' + groupCommandSet + '">' + curCommand.typedetails.label + '</button>'+
                                '</td>'+
                            '</tr>'
                        );
                        setScript = true;
                    }
                    if(setScript){
                        $('#' + tableId).append(
                            '<script>'+
                                '$( ".' + deviceData.id + '_deviceAction-' + groupCommandSet + '" ).off("click").on("click",function(){'+
                                    'runDeviceCommand(parseInt($(this).attr("data-id")), $(this).attr("data-group"), $(this).attr("data-control"), $(this).attr("data-command"),"", "string");'+
                                '});'+
                            '<\/script>'
                        );
                    }
                break;
                case "select":
                    var selectSet = '<select class="deviceCommandSelect_' + deviceData.id + '-' + groupCommandSet + ' form-control" data-group="' + groupData.id + '" data-control="' + curCommand.typedetails.id + '" data-id="' + deviceData.id + '" name="deviceAction-' + groupCommandSet + '">';
                    if(curCommand.currentvalue == null) { curCommand.currentvalue = "Unknown" ; }
                    for(var j=0;j<curCommand.typedetails.commandset.length;j++){
                        var value = curCommand.typedetails.commandset[j].value.toString();
                        selectSet += '<option value="'+value+'" '+((value===curCommand.currentvalue.toString())?' selected':'')+'>'+curCommand.typedetails.commandset[j].label+'</option>';
                    }
                    selectSet += '</select>';
                    if((onlyShortCut === true && typeof curCommand.typedetails.shortcut !== "undefined") && pos<=3){
                        pos+=1;
                        $('#' + tableId).append(
                            '<div style="width:33%; float:left;">' +
                                selectSet+
                            '</div>'
                        );
                        setScript = true;
                    } else if(onlyShortCut === false){
                        $('#' + tableId).append(
                            '<tr style="padding-top:5px;"><td class="name">' + curCommand.typedetails.label + '</td>' +
                                '<td class="value">'+
                                selectSet+
                                '</td>'+
                            '</tr>'
                        );
                        setScript = true;
                    }
                    if(setScript){
                        $('#' + tableId).append(
                            '<script>'+
                                '$( ".deviceCommandSelect_' + deviceData.id + '-' + groupCommandSet + '" ).off("change").on("change",function(){'+
                                    'runDeviceCommand(parseInt($(this).attr("data-id")), $(this).attr("data-group"), $(this).attr("data-control"), $(this).val(),"","string");'+
                                '});'+
                            '<\/script>'
                        );
                    }
                break;
                case "toggle":
                    if((onlyShortCut === true && typeof curCommand.typedetails.shortcut !== "undefined") && pos<=3){
                        pos+=1;
                        $('#' + tableId).append(
                            '<div style="width:33%; float:left;padding-top: 2px;">'+
                                '<input class="deviceCommandToggle_' + deviceData.id + '-' + groupCommandSet + '" data-command="'+curCommand.typedetails.deviceCommandValue+'" data-group="' + groupData.id + '" data-control="' + curCommand.typedetails.id + '" data-id="' + deviceData.id + '" data-basecontroltype="true" data-on-color="success" data-off-color="danger" type="checkbox" data-on-text="' + curCommand.typedetails.commandset.on.label + '" data-off-text="' + curCommand.typedetails.commandset.off.label + '" '+((curCommand.currentvalue===true)?'checked':'')+'>'+
                            '</div>'
                        );
                        setScript = true;
                    } else if(onlyShortCut === false){
                        $('#' + tableId).append(
                            '<tr style="padding-top:5px;"><td class="name">' + curCommand.typedetails.label + '</td>' +
                                '<td><input class="deviceCommandToggle_' + deviceData.id + '-' + groupCommandSet + '" data-command="'+curCommand.typedetails.deviceCommandValue+'" data-group="' + groupData.id + '" data-control="' + curCommand.typedetails.id + '" data-id="' + deviceData.id + '" data-basecontroltype="true" data-on-color="success" data-off-color="danger" type="checkbox" data-on-text="' + curCommand.typedetails.commandset.on.label + '" data-off-text="' + curCommand.typedetails.commandset.off.label + '" '+((curCommand.currentvalue===true)?'checked':'')+'>'+
                            '</td></tr>'
                        );
                        setScript = true;
                    }
                    if(setScript){
                        $('#' + tableId).append(
                            '<script>'+
                                '$(function() {'+
                                    '$(".deviceCommandToggle_' + deviceData.id + '-' + groupCommandSet + '").bootstrapSwitch();'+
                                    '$(".deviceCommandToggle_' + deviceData.id + '-' + groupCommandSet + '").off("switchChange.bootstrapSwitch").on("switchChange.bootstrapSwitch", function(event, state) {'+
                                      'runDeviceCommand(parseInt($(this).attr("data-id")), $(this).attr("data-group"), $(this).attr("data-control"), state,"");'+
                                    '});'+
                                '});'+
                            '<\/script>'
                        );
                    }
                break;
                case "slider":
                    var dataType = curCommand.typedetails.datatype;
                    var randId = createUUID();
                    if((onlyShortCut === true && typeof curCommand.typedetails.shortcut !== "undefined") && ((pos+2)<=3)){
                        pos+=2;
                        $('#' + tableId).append(
                            '<div style="width:63%; float:left; padding-left: 3%;padding-top:7px;padding-left: 3%;">' +
                                '<input style="width: 190px;" id="'+randId+'" data-basecontroltype="true" data-group="' + groupData.id + '" data-control="' + curCommand.typedetails.id + '" data-id="' + deviceData.id + '" type="text" class="deviceSliderFor_' + deviceData.id + '-' + groupCommandSet + '" data-slider-min="'+curCommand.typedetails.min+'" data-slider-max="'+curCommand.typedetails.max+'" data-slider-step="1" data-slider-value="'+curCommand.currentvalue+'"/>'+
                            '</div>'
                        );
                        setScript = true;
                    } else if(onlyShortCut === false){
                        $('#' + tableId).append(
                            '<tr style="padding-top:5px;"><td class="name">' + curCommand.typedetails.label + '</td>' +
                                '<td>'+
                                    '<input id="'+randId+'" data-basecontroltype="true" data-group="' + groupData.id + '" data-control="' + curCommand.typedetails.id + '" data-id="' + deviceData.id + '" type="text" class="deviceSliderFor_' + deviceData.id + '-' + groupCommandSet + '" data-slider-min="'+curCommand.typedetails.min+'" data-slider-max="'+curCommand.typedetails.max+'" data-slider-step="1" data-slider-value="'+curCommand.currentvalue+'"/>'+
                                    '<span>&nbsp;&nbsp;</span><span id="'+randId+'_visibleValue" class="deviceSliderFor_' + deviceData.id + '-' + groupCommandSet + '_visibleValue">'+curCommand.currentvalue+'</span>'+
                               '</td>'+
                            '</tr>'
                        );
                        setScript = true;
                    }
                    if(setScript){
                        $('#' + tableId).append(
                            '<script>'+
                                '$(function() {'+
                                    '$("#' +randId+ '").bootstrapSlider({'+
                                    'value: ' + curCommand.currentvalue + ','+
                                    'formatter: function(value) {'+
                                        'return "Value: " + value;'+
                                    '},'+
                                    'tooltip: "show",'+
                                    'precision: (("'+dataType+'"==="integer")?0:2)'+
                                    '}).off("slideStop").on("slideStop", function(val){'+
                                        'runDeviceCommand(parseInt($(this).attr("data-id")), $(this).attr("data-group"), $(this).attr("data-control"), val.value,"");'+
                                    '}).off("slide").on("slide", function(slideEvt){ $("#'+randId+'_visibleValue").text(slideEvt.value); });'+
                                '});'+
                            '<\/script>'
                        );
                    }
                break;
                case "colorpicker":
                    if((onlyShortCut === true && typeof curCommand.typedetails.shortcut !== "undefined") && (pos<=3)){
                        var localColorGetObject = {h: 0, s: 0, b: 0};
                        var localSetColorObject = {h: curCommand.typedetails.color.hsb.h * 360, s: curCommand.typedetails.color.hsb.s, v: curCommand.typedetails.color.hsb.b};
                        var tinyColorStuff = tinycolor(localSetColorObject);
                        var buttonBg = createGradientCSSForButtonFromTinyColors(tinyColorStuff);
                        var randID = createUUID();
                        var popId = createColorPicker(deviceData, groupCommandSet, curCommand, localColorGetObject, localSetColorObject);
                        $('#' + tableId).append (
                            '<div style="width:33%; float:left;">' +
                                '<button type="button" class="blockTypePreview_' + deviceData.id + '_' + groupCommandSet + ' btn" id="'+randID+'" style="width: 100px; '+buttonBg+'">Set Color</button>' +
                            '</div>'+
                            '<script>'+
                                '$("#'+randID+'").on("click", function () { '+
                                    '$("#'+popId+'").modal("show");'+
                                '})'+
                            '<\/script>'
                        );
                    } else if(onlyShortCut === false){
                        var localColorGetObject = {h: 0, s: 0, b: 0};
                        var localSetColorObject = {h: curCommand.typedetails.color.hsb.h * 360, s: curCommand.typedetails.color.hsb.s, v: curCommand.typedetails.color.hsb.b};
                        var tinyColorStuff = tinycolor(localSetColorObject);
                        var buttonBg = createGradientCSSForButtonFromTinyColors(tinyColorStuff);
                        var randID = createUUID();
                        var popId = createColorPicker(deviceData, groupCommandSet, curCommand, localColorGetObject, localSetColorObject);
                        $('#' + tableId).append ('<tr style="padding-top:5px;"><td class="name">' + curCommand.typedetails.label + '</td><td>' +
                            '<div style="width:33%; float:left;">' +
                                '<button type="button" class="blockTypePreview_' + deviceData.id + '_' + groupCommandSet + ' btn" id="'+randID+'" style="width: 100px; '+buttonBg+'">Set Color</button>' +
                            '</div>'+
                            '<script>'+
                                '$("#'+randID+'").on("click", function () { '+
                                    '$("#'+popId+'").modal("show");'+
                                '})'+
                            '<\/script>' +
                            '</td>'
                        );
                    }
                break;
            }
        }
    }
}

function createColorPicker(deviceData, groupCommandSet, curCommand, localColorGetObject, localSetColorObject){
    var popId = 'openColorPickerPopup' + deviceData.id + '_' + groupCommandSet.replace("-", "_");
    if($('#'+popId).length===0){
        $("#contentbody").append(
            '<div class="modal fade" id="'+popId+'" role="dialog" style="z-index: 15000;">'+
                '<div class="modal-dialog">'+
                    '<div class="modal-content">'+
                        '<div class="modal-header">'+
                            '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'+
                            '<h4 class="modal-title">'+deviceData.friendlyname+' - '+curCommand.typedetails.label+'</h4>'+
                        '</div>'+
                        '<div class="modal-body">'+
                            '<table>' +
                            '<tbody>' +
                                '<tr>' +
                                    '<td style="width:234px;" valign="top">' +
                                        '<div class="' + deviceData.id + '_deviceColorPickerFor_' + groupCommandSet + '"></div>' +
                                    '</td>' +
                                        '<td valign="top" class="' + deviceData.id + '_cellDeviceColorPickerPickerFor_' + groupCommandSet + '">' +
                                    '</td>' +
                                '</tr>' +
                            '</tbody>' +
                            '</table>'+
                        '</div>'+
                        '<div class="modal-footer">'+
                            '<button type="button" class="btn btn-success" data-dismiss="modal">Close</button>'+
                        '</div>'+
                    '</div>'+
                '</div>'+
            '</div>'
        );
    }
    $("." + deviceData.id + '_deviceColorPickerFor_' + groupCommandSet).empty();
    switch (curCommand.typedetails.mode) {
        case "rgb":
            $("." + deviceData.id + '_deviceColorPickerFor_' + groupCommandSet).ColorPickerSliders({
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
                color: localSetColorObject,
                onchange: function (container, color) {
                    var newColor = color.tiny.toHsv();
                    localColorGetObject.h = newColor.h / 360;
                    localColorGetObject.s = newColor.s;
                    localColorGetObject.b = newColor.v;
                }
            });
        break;
        case "cie":
            $("." + deviceData.id + '_deviceColorPickerFor_' + groupCommandSet).ColorPickerSliders({
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
                color: localSetColorObject,
                onchange: function (container, color) {
                    var newColor = color.tiny.toHsv();
                    localColorGetObject.h = newColor.h / 360;
                    localColorGetObject.s = newColor.s;
                    localColorGetObject.b = newColor.v;
                }
            });
        break;
        case "hsb":
            $("." + deviceData.id + '_deviceColorPickerFor_' + groupCommandSet).ColorPickerSliders({
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
                color: localSetColorObject,
                onchange: function (container, color) {
                    var newColor = color.tiny.toHsv();
                    localColorGetObject.h = newColor.h / 360;
                    localColorGetObject.s = newColor.s;
                    localColorGetObject.b = newColor.v;
                }
            });
        break;
    }
    var curButtonContent = '';
    for (var button = 0; button < curCommand.typedetails.commandset.length; button++) {
        var curButton = curCommand.typedetails.commandset[button];
        curButtonContent += '<div style="margin-bottom: 3px;">'+
                                '<button type="button" class="btn btn-info colorActionButton ' + deviceData.id + '_deviceColorPickerFor_' + groupCommandSet + '_button" type="button" data-command="' + deviceData.id + '-' + groupCommandSet + '-' + curButton.value + '" id="deviceColorPickerFor_' + deviceData.id + '-' + groupCommandSet + '-' + curButton.value + '" name="deviceColorPickerFor_' + groupCommandSet + '-' + curButton.value + '" value="' + curButton.label + '">' + curButton.label + '</button>'+
                            '</div>';
    }
    $('.' + deviceData.id + '_cellDeviceColorPickerPickerFor_' + groupCommandSet).html(curButtonContent);
    $("." + deviceData.id + '_deviceColorPickerFor_' + groupCommandSet + '_button').off('click').on("click", function(){
        var deviceStruct = $(this).attr("data-command").split("-");
        runDeviceCommand(parseInt(deviceStruct[0]), deviceStruct[1], deviceStruct[2], JSON.stringify(localColorGetObject), deviceStruct[3]);
    });
    return popId;
}

///////////////////////////////////////////// Notify script.
/** Notify.js - v0.3.1 - 2014/06/29
 * http://notifyjs.com/
 * Copyright (c) 2014 Jaime Pillora - MIT
 */
(function (window, document, $, undefined) {
    'use strict';

    var Notification, addStyle, blankFieldName, coreStyle, createElem, defaults, encode, find, findFields, getAnchorElement, getStyle, globalAnchors, hAligns, incr, inherit, insertCSS, mainPositions, opposites, parsePosition, pluginClassName, pluginName, pluginOptions, positions, realign, stylePrefixes, styles, vAligns,
            __indexOf = [].indexOf || function (item) {
        for (var i = 0, l = this.length; i < l; i++) {
            if (i in this && this[i] === item)
                return i;
        }
        return -1;
    };

    pluginName = 'notify';

    pluginClassName = pluginName + 'js';

    blankFieldName = pluginName + "!blank";

    positions = {
        t: 'top',
        m: 'middle',
        b: 'bottom',
        l: 'left',
        c: 'center',
        r: 'right'
    };

    hAligns = ['l', 'c', 'r'];

    vAligns = ['t', 'm', 'b'];

    mainPositions = ['t', 'b', 'l', 'r'];

    opposites = {
        t: 'b',
        m: null,
        b: 't',
        l: 'r',
        c: null,
        r: 'l'
    };

    parsePosition = function (str) {
        var pos;
        pos = [];
        $.each(str.split(/\W+/), function (i, word) {
            var w;
            w = word.toLowerCase().charAt(0);
            if (positions[w]) {
                return pos.push(w);
            }
        });
        return pos;
    };

    styles = {};

    coreStyle = {
        name: 'core',
        html: "<div class=\"" + pluginClassName + "-wrapper\">\n  <div class=\"" + pluginClassName + "-arrow\"></div>\n  <div class=\"" + pluginClassName + "-container\"></div>\n</div>",
        css: "." + pluginClassName + "-corner {\n  position: fixed;\n  margin: 5px;\n  z-index: 1050;\n}\n\n." + pluginClassName + "-corner ." + pluginClassName + "-wrapper,\n." + pluginClassName + "-corner ." + pluginClassName + "-container {\n  position: relative;\n  display: block;\n  height: inherit;\n  width: inherit;\n  margin: 3px;\n}\n\n." + pluginClassName + "-wrapper {\n  z-index: 1;\n  position: absolute;\n  display: inline-block;\n  height: 0;\n  width: 0;\n}\n\n." + pluginClassName + "-container {\n  display: none;\n  z-index: 1;\n  position: absolute;\n}\n\n." + pluginClassName + "-hidable {\n  cursor: pointer;\n}\n\n[data-notify-text],[data-notify-html] {\n  position: relative;\n}\n\n." + pluginClassName + "-arrow {\n  position: absolute;\n  z-index: 2;\n  width: 0;\n  height: 0;\n}"
    };

    stylePrefixes = {
        "border-radius": ["-webkit-", "-moz-"]
    };

    getStyle = function (name) {
        return styles[name];
    };

    addStyle = function (name, def) {
        var cssText, elem, fields, _ref;
        if (!name) {
            throw "Missing Style name";
        }
        if (!def) {
            throw "Missing Style definition";
        }
        if (!def.html) {
            throw "Missing Style HTML";
        }
        if ((_ref = styles[name]) != null ? _ref.cssElem : void 0) {
            if (window.console) {
                console.warn("" + pluginName + ": overwriting style '" + name + "'");
            }
            styles[name].cssElem.remove();
        }
        def.name = name;
        styles[name] = def;
        cssText = "";
        if (def.classes) {
            $.each(def.classes, function (className, props) {
                cssText += "." + pluginClassName + "-" + def.name + "-" + className + " {\n";
                $.each(props, function (name, val) {
                    if (stylePrefixes[name]) {
                        $.each(stylePrefixes[name], function (i, prefix) {
                            return cssText += "  " + prefix + name + ": " + val + ";\n";
                        });
                    }
                    return cssText += "  " + name + ": " + val + ";\n";
                });
                return cssText += "}\n";
            });
        }
        if (def.css) {
            cssText += "/* styles for " + def.name + " */\n" + def.css;
        }
        if (cssText) {
            def.cssElem = insertCSS(cssText);
            def.cssElem.attr('id', "notify-" + def.name);
        }
        fields = {};
        elem = $(def.html);
        findFields('html', elem, fields);
        findFields('text', elem, fields);
        return def.fields = fields;
    };

    insertCSS = function (cssText) {
        var elem;
        elem = createElem("style");
        elem.attr('type', 'text/css');
        $("head").append(elem);
        try {
            elem.html(cssText);
        } catch (e) {
            elem[0].styleSheet.cssText = cssText;
        }
        return elem;
    };

    findFields = function (type, elem, fields) {
        var attr;
        if (type !== 'html') {
            type = 'text';
        }
        attr = "data-notify-" + type;
        return find(elem, "[" + attr + "]").each(function () {
            var name;
            name = $(this).attr(attr);
            if (!name) {
                name = blankFieldName;
            }
            return fields[name] = type;
        });
    };

    find = function (elem, selector) {
        if (elem.is(selector)) {
            return elem;
        } else {
            return elem.find(selector);
        }
    };

    pluginOptions = {
        clickToHide: true,
        autoHide: true,
        autoHideDelay: 5000,
        arrowShow: true,
        arrowSize: 5,
        breakNewLines: true,
        elementPosition: 'bottom',
        globalPosition: 'top right',
        style: 'bootstrap',
        className: 'error',
        showAnimation: 'slideDown',
        showDuration: 400,
        hideAnimation: 'slideUp',
        hideDuration: 200,
        gap: 5
    };

    inherit = function (a, b) {
        var F;
        F = function () {
        };
        F.prototype = a;
        return $.extend(true, new F(), b);
    };

    defaults = function (opts) {
        return $.extend(pluginOptions, opts);
    };

    createElem = function (tag) {
        return $("<" + tag + "></" + tag + ">");
    };

    globalAnchors = {};

    getAnchorElement = function (element) {
        var radios;
        if (element.is('[type=radio]')) {
            radios = element.parents('form:first').find('[type=radio]').filter(function (i, e) {
                return $(e).attr('name') === element.attr('name');
            });
            element = radios.first();
        }
        return element;
    };

    incr = function (obj, pos, val) {
        var opp, temp;
        if (typeof val === 'string') {
            val = parseInt(val, 10);
        } else if (typeof val !== 'number') {
            return;
        }
        if (isNaN(val)) {
            return;
        }
        opp = positions[opposites[pos.charAt(0)]];
        temp = pos;
        if (obj[opp] !== undefined) {
            pos = positions[opp.charAt(0)];
            val = -val;
        }
        if (obj[pos] === undefined) {
            obj[pos] = val;
        } else {
            obj[pos] += val;
        }
        return null;
    };

    realign = function (alignment, inner, outer) {
        if (alignment === 'l' || alignment === 't') {
            return 0;
        } else if (alignment === 'c' || alignment === 'm') {
            return outer / 2 - inner / 2;
        } else if (alignment === 'r' || alignment === 'b') {
            return outer - inner;
        }
        throw "Invalid alignment";
    };

    encode = function (text) {
        encode.e = encode.e || createElem("div");
        return encode.e.text(text).html();
    };

    Notification = (function () {

        function Notification(elem, data, options) {
            if (typeof options === 'string') {
                options = {
                    className: options
                };
            }
            this.options = inherit(pluginOptions, $.isPlainObject(options) ? options : {});
            this.loadHTML();
            this.wrapper = $(coreStyle.html);
            if (this.options.clickToHide) {
                this.wrapper.addClass("" + pluginClassName + "-hidable");
            }
            this.wrapper.data(pluginClassName, this);
            this.arrow = this.wrapper.find("." + pluginClassName + "-arrow");
            this.container = this.wrapper.find("." + pluginClassName + "-container");
            this.container.append(this.userContainer);
            if (elem && elem.length) {
                this.elementType = elem.attr('type');
                this.originalElement = elem;
                this.elem = getAnchorElement(elem);
                this.elem.data(pluginClassName, this);
                this.elem.before(this.wrapper);
            }
            this.container.hide();
            this.run(data);
        }

        Notification.prototype.loadHTML = function () {
            var style;
            style = this.getStyle();
            this.userContainer = $(style.html);
            return this.userFields = style.fields;
        };

        Notification.prototype.show = function (show, userCallback) {
            var args, callback, elems, fn, hidden,
                    _this = this;
            callback = function () {
                if (!show && !_this.elem) {
                    _this.destroy();
                }
                if (userCallback) {
                    return userCallback();
                }
            };
            hidden = this.container.parent().parents(':hidden').length > 0;
            elems = this.container.add(this.arrow);
            args = [];
            if (hidden && show) {
                fn = 'show';
            } else if (hidden && !show) {
                fn = 'hide';
            } else if (!hidden && show) {
                fn = this.options.showAnimation;
                args.push(this.options.showDuration);
            } else if (!hidden && !show) {
                fn = this.options.hideAnimation;
                args.push(this.options.hideDuration);
            } else {
                return callback();
            }
            args.push(callback);
            return elems[fn].apply(elems, args);
        };

        Notification.prototype.setGlobalPosition = function () {
            var align, anchor, css, key, main, pAlign, pMain, _ref;
            _ref = this.getPosition(), pMain = _ref[0], pAlign = _ref[1];
            main = positions[pMain];
            align = positions[pAlign];
            key = pMain + "|" + pAlign;
            anchor = globalAnchors[key];
            if (!anchor) {
                anchor = globalAnchors[key] = createElem("div");
                css = {};
                css[main] = 0;
                if (align === 'middle') {
                    css.top = '45%';
                } else if (align === 'center') {
                    css.left = '45%';
                } else {
                    css[align] = 0;
                }
                anchor.css(css).addClass("" + pluginClassName + "-corner");
                $("body").append(anchor);
            }
            return anchor.prepend(this.wrapper);
        };

        Notification.prototype.setElementPosition = function () {
            var arrowColor, arrowCss, arrowSize, color, contH, contW, css, elemH, elemIH, elemIW, elemPos, elemW, gap, mainFull, margin, opp, oppFull, pAlign, pArrow, pMain, pos, posFull, position, wrapPos, _i, _j, _len, _len1, _ref;
            position = this.getPosition();
            pMain = position[0], pAlign = position[1], pArrow = position[2];
            elemPos = this.elem.position();
            elemH = this.elem.outerHeight();
            elemW = this.elem.outerWidth();
            elemIH = this.elem.innerHeight();
            elemIW = this.elem.innerWidth();
            wrapPos = this.wrapper.position();
            contH = this.container.height();
            contW = this.container.width();
            mainFull = positions[pMain];
            opp = opposites[pMain];
            oppFull = positions[opp];
            css = {};
            css[oppFull] = pMain === 'b' ? elemH : pMain === 'r' ? elemW : 0;
            incr(css, 'top', elemPos.top - wrapPos.top);
            incr(css, 'left', elemPos.left - wrapPos.left);
            _ref = ['top', 'left'];
            for (_i = 0, _len = _ref.length; _i < _len; _i++) {
                pos = _ref[_i];
                margin = parseInt(this.elem.css("margin-" + pos), 10);
                if (margin) {
                    incr(css, pos, margin);
                }
            }
            gap = Math.max(0, this.options.gap - (this.options.arrowShow ? arrowSize : 0));
            incr(css, oppFull, gap);
            if (!this.options.arrowShow) {
                this.arrow.hide();
            } else {
                arrowSize = this.options.arrowSize;
                arrowCss = $.extend({}, css);
                arrowColor = this.userContainer.css("border-color") || this.userContainer.css("background-color") || 'white';
                for (_j = 0, _len1 = mainPositions.length; _j < _len1; _j++) {
                    pos = mainPositions[_j];
                    posFull = positions[pos];
                    if (pos === opp) {
                        continue;
                    }
                    color = posFull === mainFull ? arrowColor : 'transparent';
                    arrowCss["border-" + posFull] = "" + arrowSize + "px solid " + color;
                }
                incr(css, positions[opp], arrowSize);
                if (__indexOf.call(mainPositions, pAlign) >= 0) {
                    incr(arrowCss, positions[pAlign], arrowSize * 2);
                }
            }
            if (__indexOf.call(vAligns, pMain) >= 0) {
                incr(css, 'left', realign(pAlign, contW, elemW));
                if (arrowCss) {
                    incr(arrowCss, 'left', realign(pAlign, arrowSize, elemIW));
                }
            } else if (__indexOf.call(hAligns, pMain) >= 0) {
                incr(css, 'top', realign(pAlign, contH, elemH));
                if (arrowCss) {
                    incr(arrowCss, 'top', realign(pAlign, arrowSize, elemIH));
                }
            }
            if (this.container.is(":visible")) {
                css.display = 'block';
            }
            this.container.removeAttr('style').css(css);
            if (arrowCss) {
                return this.arrow.removeAttr('style').css(arrowCss);
            }
        };

        Notification.prototype.getPosition = function () {
            var pos, text, _ref, _ref1, _ref2, _ref3, _ref4, _ref5;
            text = this.options.position || (this.elem ? this.options.elementPosition : this.options.globalPosition);
            pos = parsePosition(text);
            if (pos.length === 0) {
                pos[0] = 'b';
            }
            if (_ref = pos[0], __indexOf.call(mainPositions, _ref) < 0) {
                throw "Must be one of [" + mainPositions + "]";
            }
            if (pos.length === 1 || ((_ref1 = pos[0], __indexOf.call(vAligns, _ref1) >= 0) && (_ref2 = pos[1], __indexOf.call(hAligns, _ref2) < 0)) || ((_ref3 = pos[0], __indexOf.call(hAligns, _ref3) >= 0) && (_ref4 = pos[1], __indexOf.call(vAligns, _ref4) < 0))) {
                pos[1] = (_ref5 = pos[0], __indexOf.call(hAligns, _ref5) >= 0) ? 'm' : 'l';
            }
            if (pos.length === 2) {
                pos[2] = pos[1];
            }
            return pos;
        };

        Notification.prototype.getStyle = function (name) {
            var style;
            if (!name) {
                name = this.options.style;
            }
            if (!name) {
                name = 'default';
            }
            style = styles[name];
            if (!style) {
                throw "Missing style: " + name;
            }
            return style;
        };

        Notification.prototype.updateClasses = function () {
            var classes, style;
            classes = ['base'];
            if ($.isArray(this.options.className)) {
                classes = classes.concat(this.options.className);
            } else if (this.options.className) {
                classes.push(this.options.className);
            }
            style = this.getStyle();
            classes = $.map(classes, function (n) {
                return "" + pluginClassName + "-" + style.name + "-" + n;
            }).join(' ');
            return this.userContainer.attr('class', classes);
        };

        Notification.prototype.run = function (data, options) {
            var d, datas, name, type, value,
                    _this = this;
            if ($.isPlainObject(options)) {
                $.extend(this.options, options);
            } else if ($.type(options) === 'string') {
                this.options.className = options;
            }
            if (this.container && !data) {
                this.show(false);
                return;
            } else if (!this.container && !data) {
                return;
            }
            datas = {};
            if ($.isPlainObject(data)) {
                datas = data;
            } else {
                datas[blankFieldName] = data;
            }
            for (name in datas) {
                d = datas[name];
                type = this.userFields[name];
                if (!type) {
                    continue;
                }
                if (type === 'text') {
                    d = encode(d);
                    if (this.options.breakNewLines) {
                        d = d.replace(/\n/g, '<br/>');
                    }
                }
                value = name === blankFieldName ? '' : '=' + name;
                find(this.userContainer, "[data-notify-" + type + value + "]").html(d);
            }
            this.updateClasses();
            if (this.elem) {
                this.setElementPosition();
            } else {
                this.setGlobalPosition();
            }
            this.show(true);
            if (this.options.autoHide) {
                clearTimeout(this.autohideTimer);
                return this.autohideTimer = setTimeout(function () {
                    return _this.show(false);
                }, this.options.autoHideDelay);
            }
        };

        Notification.prototype.destroy = function () {
            return this.wrapper.remove();
        };

        return Notification;

    })();

    $[pluginName] = function (elem, data, options) {
        if ((elem && elem.nodeName) || elem.jquery) {
            $(elem)[pluginName](data, options);
        } else {
            options = data;
            data = elem;
            new Notification(null, data, options);
        }
        return elem;
    };

    $.fn[pluginName] = function (data, options) {
        $(this).each(function () {
            var inst;
            inst = getAnchorElement($(this)).data(pluginClassName);
            if (inst) {
                return inst.run(data, options);
            } else {
                return new Notification($(this), data, options);
            }
        });
        return this;
    };

    $.extend($[pluginName], {
        defaults: defaults,
        addStyle: addStyle,
        pluginOptions: pluginOptions,
        getStyle: getStyle,
        insertCSS: insertCSS
    });

    $(function () {
        insertCSS(coreStyle.css).attr('id', 'core-notify');
        $(document).on('click', "." + pluginClassName + "-hidable", function (e) {
            return $(this).trigger('notify-hide');
        });
        return $(document).on('notify-hide', "." + pluginClassName + "-wrapper", function (e) {
            var _ref;
            return (_ref = $(this).data(pluginClassName)) != null ? _ref.show(false) : void 0;
        });
    });

}(window, document, jQuery));
$.notify.defaults({
    style: "bootstrap",
    autoHide: true,
    clickToHide: true,
    autoHideDelay: 5000,
    globalPosition: 'bottom right',
});
$.notify.addStyle("bootstrap", {
    html: "<div>\n<span data-notify-html=\"title\" style=\"font-weight:bold;\"></span><br/><span data-notify-html=\"text\"></span>\n</div>",
    classes: {
        base: {
            "font-weight": "bold",
            "padding": "8px 15px 8px 14px",
            "text-shadow": "0 1px 0 rgba(255, 255, 255, 0.5)",
            "background-color": "#fcf8e3",
            "border": "1px solid #fbeed5",
            "border-radius": "4px",
            "white-space": "nowrap",
            "padding-left": "25px",
            "background-repeat": "no-repeat",
            "background-position": "3px 7px"
        },
        error: {
            "color": "#B94A48",
            "background-color": "#F2DEDE",
            "border-color": "#EED3D7",
            "background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAtRJREFUeNqkVc1u00AQHq+dOD+0poIQfkIjalW0SEGqRMuRnHos3DjwAH0ArlyQeANOOSMeAA5VjyBxKBQhgSpVUKKQNGloFdw4cWw2jtfMOna6JOUArDTazXi/b3dm55socPqQhFka++aHBsI8GsopRJERNFlY88FCEk9Yiwf8RhgRyaHFQpPHCDmZG5oX2ui2yilkcTT1AcDsbYC1NMAyOi7zTX2Agx7A9luAl88BauiiQ/cJaZQfIpAlngDcvZZMrl8vFPK5+XktrWlx3/ehZ5r9+t6e+WVnp1pxnNIjgBe4/6dAysQc8dsmHwPcW9C0h3fW1hans1ltwJhy0GxK7XZbUlMp5Ww2eyan6+ft/f2FAqXGK4CvQk5HueFz7D6GOZtIrK+srupdx1GRBBqNBtzc2AiMr7nPplRdKhb1q6q6zjFhrklEFOUutoQ50xcX86ZlqaZpQrfbBdu2R6/G19zX6XSgh6RX5ubyHCM8nqSID6ICrGiZjGYYxojEsiw4PDwMSL5VKsC8Yf4VRYFzMzMaxwjlJSlCyAQ9l0CW44PBADzXhe7xMdi9HtTrdYjFYkDQL0cn4Xdq2/EAE+InCnvADTf2eah4Sx9vExQjkqXT6aAERICMewd/UAp/IeYANM2joxt+q5VI+ieq2i0Wg3l6DNzHwTERPgo1ko7XBXj3vdlsT2F+UuhIhYkp7u7CarkcrFOCtR3H5JiwbAIeImjT/YQKKBtGjRFCU5IUgFRe7fF4cCNVIPMYo3VKqxwjyNAXNepuopyqnld602qVsfRpEkkz+GFL1wPj6ySXBpJtWVa5xlhpcyhBNwpZHmtX8AGgfIExo0ZpzkWVTBGiXCSEaHh62/PoR0p/vHaczxXGnj4bSo+G78lELU80h1uogBwWLf5YlsPmgDEd4M236xjm+8nm4IuE/9u+/PH2JXZfbwz4zw1WbO+SQPpXfwG/BBgAhCNZiSb/pOQAAAAASUVORK5CYII=)"
        },
        success: {
            "color": "#468847",
            "background-color": "#DFF0D8",
            "border-color": "#D6E9C6",
            "background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAutJREFUeNq0lctPE0Ecx38zu/RFS1EryqtgJFA08YCiMZIAQQ4eRG8eDGdPJiYeTIwHTfwPiAcvXIwXLwoXPaDxkWgQ6islKlJLSQWLUraPLTv7Gme32zoF9KSTfLO7v53vZ3d/M7/fIth+IO6INt2jjoA7bjHCJoAlzCRw59YwHYjBnfMPqAKWQYKjGkfCJqAF0xwZjipQtA3MxeSG87VhOOYegVrUCy7UZM9S6TLIdAamySTclZdYhFhRHloGYg7mgZv1Zzztvgud7V1tbQ2twYA34LJmF4p5dXF1KTufnE+SxeJtuCZNsLDCQU0+RyKTF27Unw101l8e6hns3u0PBalORVVVkcaEKBJDgV3+cGM4tKKmI+ohlIGnygKX00rSBfszz/n2uXv81wd6+rt1orsZCHRdr1Imk2F2Kob3hutSxW8thsd8AXNaln9D7CTfA6O+0UgkMuwVvEFFUbbAcrkcTA8+AtOk8E6KiQiDmMFSDqZItAzEVQviRkdDdaFgPp8HSZKAEAL5Qh7Sq2lIJBJwv2scUqkUnKoZgNhcDKhKg5aH+1IkcouCAdFGAQsuWZYhOjwFHQ96oagWgRoUov1T9kRBEODAwxM2QtEUl+Wp+Ln9VRo6BcMw4ErHRYjH4/B26AlQoQQTRdHWwcd9AH57+UAXddvDD37DmrBBV34WfqiXPl61g+vr6xA9zsGeM9gOdsNXkgpEtTwVvwOklXLKm6+/p5ezwk4B+j6droBs2CsGa/gNs6RIxazl4Tc25mpTgw/apPR1LYlNRFAzgsOxkyXYLIM1V8NMwyAkJSctD1eGVKiq5wWjSPdjmeTkiKvVW4f2YPHWl3GAVq6ymcyCTgovM3FzyRiDe2TaKcEKsLpJvNHjZgPNqEtyi6mZIm4SRFyLMUsONSSdkPeFtY1n0mczoY3BHTLhwPRy9/lzcziCw9ACI+yql0VLzcGAZbYSM5CCSZg1/9oc/nn7+i8N9p/8An4JMADxhH+xHfuiKwAAAABJRU5ErkJggg==)"
        },
        info: {
            "color": "#3A87AD",
            "background-color": "#D9EDF7",
            "border-color": "#BCE8F1",
            "background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3QYFAhkSsdes/QAAA8dJREFUOMvVlGtMW2UYx//POaWHXg6lLaW0ypAtw1UCgbniNOLcVOLmAjHZolOYlxmTGXVZdAnRfXQm+7SoU4mXaOaiZsEpC9FkiQs6Z6bdCnNYruM6KNBw6YWewzl9z+sHImEWv+vz7XmT95f/+3/+7wP814v+efDOV3/SoX3lHAA+6ODeUFfMfjOWMADgdk+eEKz0pF7aQdMAcOKLLjrcVMVX3xdWN29/GhYP7SvnP0cWfS8caSkfHZsPE9Fgnt02JNutQ0QYHB2dDz9/pKX8QjjuO9xUxd/66HdxTeCHZ3rojQObGQBcuNjfplkD3b19Y/6MrimSaKgSMmpGU5WevmE/swa6Oy73tQHA0Rdr2Mmv/6A1n9w9suQ7097Z9lM4FlTgTDrzZTu4StXVfpiI48rVcUDM5cmEksrFnHxfpTtU/3BFQzCQF/2bYVoNbH7zmItbSoMj40JSzmMyX5qDvriA7QdrIIpA+3cdsMpu0nXI8cV0MtKXCPZev+gCEM1S2NHPvWfP/hL+7FSr3+0p5RBEyhEN5JCKYr8XnASMT0xBNyzQGQeI8fjsGD39RMPk7se2bd5ZtTyoFYXftF6y37gx7NeUtJJOTFlAHDZLDuILU3j3+H5oOrD3yWbIztugaAzgnBKJuBLpGfQrS8wO4FZgV+c1IxaLgWVU0tMLEETCos4xMzEIv9cJXQcyagIwigDGwJgOAtHAwAhisQUjy0ORGERiELgG4iakkzo4MYAxcM5hAMi1WWG1yYCJIcMUaBkVRLdGeSU2995TLWzcUAzONJ7J6FBVBYIggMzmFbvdBV44Corg8vjhzC+EJEl8U1kJtgYrhCzgc/vvTwXKSib1paRFVRVORDAJAsw5FuTaJEhWM2SHB3mOAlhkNxwuLzeJsGwqWzf5TFNdKgtY5qHp6ZFf67Y/sAVadCaVY5YACDDb3Oi4NIjLnWMw2QthCBIsVhsUTU9tvXsjeq9+X1d75/KEs4LNOfcdf/+HthMnvwxOD0wmHaXr7ZItn2wuH2SnBzbZAbPJwpPx+VQuzcm7dgRCB57a1uBzUDRL4bfnI0RE0eaXd9W89mpjqHZnUI5Hh2l2dkZZUhOqpi2qSmpOmZ64Tuu9qlz/SEXo6MEHa3wOip46F1n7633eekV8ds8Wxjn37Wl63VVa+ej5oeEZ/82ZBETJjpJ1Rbij2D3Z/1trXUvLsblCK0XfOx0SX2kMsn9dX+d+7Kf6h8o4AIykuffjT8L20LU+w4AZd5VvEPY+XpWqLV327HR7DzXuDnD8r+ovkBehJ8i+y8YAAAAASUVORK5CYII=)"
        },
        warn: {
            "color": "#C09853",
            "background-color": "#FCF8E3",
            "border-color": "#FBEED5",
            "background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAMAAAC6V+0/AAABJlBMVEXr6eb/2oD/wi7/xjr/0mP/ykf/tQD/vBj/3o7/uQ//vyL/twebhgD/4pzX1K3z8e349vK6tHCilCWbiQymn0jGworr6dXQza3HxcKkn1vWvV/5uRfk4dXZ1bD18+/52YebiAmyr5S9mhCzrWq5t6ufjRH54aLs0oS+qD751XqPhAybhwXsujG3sm+Zk0PTwG6Shg+PhhObhwOPgQL4zV2nlyrf27uLfgCPhRHu7OmLgAafkyiWkD3l49ibiAfTs0C+lgCniwD4sgDJxqOilzDWowWFfAH08uebig6qpFHBvH/aw26FfQTQzsvy8OyEfz20r3jAvaKbhgG9q0nc2LbZxXanoUu/u5WSggCtp1anpJKdmFz/zlX/1nGJiYmuq5Dx7+sAAADoPUZSAAAAAXRSTlMAQObYZgAAAAFiS0dEAIgFHUgAAAAJcEhZcwAACxMAAAsTAQCanBgAAAAHdElNRQfdBgUBGhh4aah5AAAAlklEQVQY02NgoBIIE8EUcwn1FkIXM1Tj5dDUQhPU502Mi7XXQxGz5uVIjGOJUUUW81HnYEyMi2HVcUOICQZzMMYmxrEyMylJwgUt5BljWRLjmJm4pI1hYp5SQLGYxDgmLnZOVxuooClIDKgXKMbN5ggV1ACLJcaBxNgcoiGCBiZwdWxOETBDrTyEFey0jYJ4eHjMGWgEAIpRFRCUt08qAAAAAElFTkSuQmCC)"
        }
    }
});

/**
 * This code is used for creating routines for selecting items.
 * On the page there will be a function needed which is called as callback when a
 * selection is made and will be passed as a javascript object. The page creator has to 
 * take the different objects into account. This functions makes it possible to use the 
 * exact same modal on different pages. The base of the selection is made from the trigger RPC
 * where the trigger RPC takes lead of possible items to be added and because the triggering
 * system arranges all possible items in the server it is possible to apply the trigger item
 * sources to multiple different in server engines like the macro's because they use the same
 * execution styles.
 * Filters can be applied for example for devices to limit the command field types.
 * @returns {itemSelectionModal}
 */

function ItemSelectionModal(){
    
    this.callBack = null;
    this.options = { "devicefilter" :"[null]", "devicetypefilter" : null};
    this.selectionType = "match";
    this.callBackData = {"itemType" : "null"};
    
    this.modalComposed = false;
    
    var self = this;
    
    if ($('#addItemWindow').length === 0) {
        $("#contentbody").append('<div class="modal" id="addItemWindow" style="z-index:10000;">'+
        '<div class="modal-dialog">'+
          '<div class="modal-content">'+
            '<div class="modal-header">'+
              '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'+
              '<h4 class="modal-title">Item selection</h4>'+
            '</div>'+
            '<div class="modal-body">'+
                '<div class="row">' +
                    '<div class="col-md-4">' +
                        '<div class="panel panel-default">'+
                            '<div class="panel-heading">'+
                                '<h3 class="panel-title">Type</h3>'+
                            '</div>'+
                            '<div class="panel-body">Choose</div>'+
                                '<div class="list-group" id="ItemSelectionModalTypes">' +
                                '</div>' +
                        '</div>'+
                    '</div>' +
                    '<div class="col-md-8">' +
                        '<div class="panel panel-default">'+
                            '<div class="panel-heading">'+
                                '<h3 class="panel-title" id="ItemSelectionModalTypeName">Select type</h3>'+
                            '</div>'+
                            '<div class="panel-body">'+
                                '<div id="itemSelectionMatchContent"></div>' +
                            '</div>' +
                        '</div>'+
                    '</div>' +
                '</div>' +
            '</div>'+
            '<div class="modal-footer">'+
              '<button type="button" class="btn btn-info" style="display:none;" id="addMatchItem">Add item</button>'+
              '<button type="button" class="btn btn-danger" data-dismiss="modal" id="cancelAddMatchItem">Cancel</button>'+
            '</div>'+
          '</div>'+
        '</div>'+
      '</div>');
    }
    
    this.sourceDiv = $('#addItemWindow');
    
    $('#addMatchItem').off("click").on('click', function() {
        self.sourceDiv.modal('hide');
        if(typeof self.callBack!=="undefined" && self.callBack!==null) self.callBack(self.callBackData);
        self.callBackData = {"itemType" : "null"};
    });
    
}
/**
 * Opens the modal
 * @returns {undefined}
 */
ItemSelectionModal.prototype.open = function() {
    if(this.modalComposed===false){
        this.createModal();
        this.modalComposed = true;
    }
    $('#addMatchItem').hide();
    $("#ItemSelectionModalTypeName").html('<span class="glyphicon glyphicon-chevron-left"></span>Select type');
    $("#itemSelectionMatchContent").empty();
    this.sourceDiv.modal('show');
};

/**
 * Sets options data.
 * For example sets the type of device fields to return.
 * @param {type} optionData
 * @returns {undefined}
 */
ItemSelectionModal.prototype.setOptions = function (optionData){
    try { this.options.devicefilter = optionData.deviceFilter; } catch(err){}
    try { if(optionData.deviceTypeFilter!==undefined) this.options.devicetypefilter = optionData.deviceTypeFilter; } catch(err){}
};

/**
 * Sets the callback function to be called when a selection is made.
 * @param {type} callBackFunction
 * @returns {undefined}
 */
ItemSelectionModal.prototype.setCallBack = function (callBackFunction){
    this.callBack = callBackFunction;
};

/**
 * Sets the callback function to be called when a selection has been made.
 * @param {type} data
 * @returns {undefined}
 */
ItemSelectionModal.prototype.runCallBack = function (data){
    this.callBack(data);
};

/**
 * Sets the selection type.
 * Set type to match to retrieve the data fields of a device, set to action to retrieve command fields.
 * @param {type} type
 * @returns {undefined}
 */
ItemSelectionModal.prototype.setSelectionType = function (type){
    this.selectionType = type;
};

/**
 * Creates the initial modal and calls all functions on selection
 * @returns {undefined}
 */
ItemSelectionModal.prototype.createModal = function (){
    
    var self = this;
    var optionsSet = self.options;
    var selectionType = this.selectionType;
    
    var triggerMatchTypeUrl;

    if(selectionType==="exec"){
        if(typeof optionsSet.devicetypefilter === "undefined" || optionsSet.devicetypefilter === null){
            triggerMatchTypeUrl = '{"jsonrpc": "2.0", "method": "TriggerService.getTriggerActionTypes", "params":{"filter":[]},"id":"TriggerService.getTriggerActionTypes"}';
        } else {
            triggerMatchTypeUrl = '{"jsonrpc": "2.0", "method": "TriggerService.getTriggerActionTypes", "params":{"filter":'+JSON.stringify(optionsSet.devicetypefilter)+'},"id":"TriggerService.getTriggerActionTypes"}';
        }
    } else if(selectionType==="match"){
        if(typeof optionsSet.devicetypefilter === "undefined" || optionsSet.devicetypefilter === null){
            triggerMatchTypeUrl = '{"jsonrpc": "2.0", "method": "TriggerService.getTriggerMatchTypes", "params":{"filter":[]},"id":"TriggerService.getTriggerMatchTypes"}';
        } else {
            triggerMatchTypeUrl = '{"jsonrpc": "2.0", "method": "TriggerService.getTriggerMatchTypes", "params":{"filter":'+JSON.stringify(optionsSet.devicetypefilter)+'},"id":"TriggerService.getTriggerMatchTypes"}';
        }
    } else {
        if(typeof optionsSet.devicetypefilter === "undefined" || optionsSet.devicetypefilter === null){
            triggerMatchTypeUrl = '{"jsonrpc": "2.0", "method": "TriggerService.getAllTriggerTypes", "params":{"filter":[]},"id":"TriggerService.getAllTriggerTypes"}';
        } else {
            triggerMatchTypeUrl = '{"jsonrpc": "2.0", "method": "TriggerService.getAllTriggerTypes", "params":{"filter":'+JSON.stringify(optionsSet.devicetypefilter)+'},"id":"TriggerService.getAllTriggerTypes"}';
        }
    }
    $("#ItemSelectionModalTypes").empty();
    getHttpJsonRPC(triggerMatchTypeUrl, 
        function(data){
            for(var i=0;i<data.length;i++){
                $("#ItemSelectionModalTypes").append('<a href="#'+data[i].id+'" class="list-group-item ItemSelectionModalTypesSelection">'+data[i].name+'</a>');
            }
            $(".ItemSelectionModalTypesSelection").on("click", function(){
                $("#ItemSelectionModalTypeName").text($(this).text());
                $("#itemSelectionMatchContent").empty();
                $('#addMatchItem').hide();
                switch ($(this).attr("href").replace("#", "")) {
                    case "remotesplugin":
                        $("#itemSelectionMatchContent").html(
                                '<div class="form-group"><select class="form-control" id="itemMatchRemotePlugin"><option value="">Choose remote</option></select></div>');
                        $('#itemMatchRemotePlugin').combobox();
                        getHttpJsonRPC('{"jsonrpc": "2.0", "method": "RemotesService.getRemotes","id":"RemotesService.getRemotes"}', function(pluginData){
                            for(var i=0; i<pluginData.length;i++){
                                $("#itemMatchRemotePlugin").append('<option value="'+pluginData[i].id+'">'+pluginData[i].name+'</option>');
                            }
                            $('#itemMatchRemotePlugin').combobox("refresh");
                            $("#itemMatchRemotePlugin").on('change', function(event) {
                                if ($('#itemMatchRemotePlugin').val()!=="") {
                                    $('#addMatchItem').show();
                                    self.callBackData = { "itemType": "remotesplugin", 
                                                          "remoteId": $('#itemMatchRemotePlugin').val(), 
                                                        "remoteName": $("#itemMatchRemotePlugin option:selected").text()
                                                    };
                                } else {
                                    $('#addMatchItem').hide();
                                    self.callBackData = {};
                                }
                            });
                        });
                    break;  
                    case "messengerplugin":
                        $("#itemSelectionMatchContent").html(
                                '<div class="form-group"><select class="form-control" id="itemMatchMessageType"><option value="">Choose message type</option></select></div>');
                        $('#itemMatchMessageType').combobox();
                        getHttpJsonRPC('{"jsonrpc": "2.0", "method": "MessengerService.getMessageTypes","id":"MessengerService.getMessageTypes"}', function(pluginData){
                            for(var i=0; i<pluginData.length;i++){
                                $("#itemMatchMessageType").append('<option value="'+pluginData[i].id+'">'+pluginData[i].name+'</option>');
                            }
                            $('#itemMatchMessageType').combobox("refresh");
                            $("#itemMatchMessageType").on('change', function(event) {
                                if ($('#itemMatchMessageType').val()!=="") {
                                    $('#addMatchItem').show();
                                    self.callBackData = { "itemType": "messengerplugin", 
                                                       "messageType": $('#itemMatchMessageType').val(), 
                                                          "typeName": $("#itemMatchMessageType option:selected").text(), 
                                                           "message":"" };
                                } else {
                                    $('#addMatchItem').hide();
                                    self.callBackData = {};
                                }
                            });
                        });
                    break;  
                    case "mediaplugin":
                        $("#itemSelectionMatchContent").html(
                                '<div class="form-group"><select class="form-control" id="itemMatchMediaPlugin"><option value="">Select a Media plugin</option></select></div>' +
                                '<div class="form-group"><label>Select command type</label><select class="form-control" id="itemMatchMediaCommandType"><option value="">Select media plugin first</option></select></div>');
                        $('#itemMatchMediaPlugin').combobox();
                        $('#itemMatchMediaCommandType').combobox();
                        $("#itemMatchMediaCommandType").combobox("disable");
                        getHttpJsonRPC('{"jsonrpc": "2.0", "method": "MediaService.getPlugins","id":"MediaService.getPlugins"}', function(pluginData){
                            for(var i=0; i<pluginData.length;i++){
                                $("#itemMatchMediaPlugin").append('<option value="'+pluginData[i].id+'">'+pluginData[i].name+'</option>');
                            }
                            $('#itemMatchMediaPlugin').combobox("refresh");
                            $("#itemMatchMediaPlugin").off("change").on('change', function(event) {
                                if($("#itemMatchMediaPlugin").val()===""){
                                    $("#itemMatchMediaCommandType").combobox("disable");
                                    $('#addMatchItem').hide();
                                } else {
                                    getHttpJsonRPC('{"jsonrpc": "2.0", "method": "MediaService.getCapabilities","params":{"id":'+$("#itemMatchMediaPlugin").val()+', "type":"commandset"}, "id":"MediaService.getCapabilities"}', function(pluginData){
                                        $("#itemMatchMediaCommandType").html('<option value="">Select command type</option>');
                                        for(var i=0; i<pluginData.length;i++){
                                            $("#itemMatchMediaCommandType").append('<option value="'+pluginData[i].id+'">'+pluginData[i].name+'</option>');
                                        }
                                        $('#itemMatchMediaCommandType').combobox("refresh");
                                        $("#itemMatchMediaCommandType").combobox("enable");
                                        $("#itemMatchMediaCommandType").off("change").on('change', function(event) {
                                            if($("#itemMatchMediaCommandType").val()===""){
                                                $('#addMatchItem').hide();
                                                self.callBackData = {};
                                            } else {
                                                $('#addMatchItem').show();
                                                self.callBackData = {
                                                    "itemType" : "mediaplugin",
                                                    "mediaId" : $("#itemMatchMediaPlugin").val(),
                                                    "mediaName" : $("#itemMatchMediaPlugin option:selected").text(),
                                                    "mediaCommandTypeId" : $("#itemMatchMediaCommandType").val(),
                                                    "mediaCommandTypeName" : $("#itemMatchMediaCommandType option:selected").text()
                                                };
                                            }
                                        });
                                    });
                                }
                            });
                        });
                    break;
                    case "device":
                        $("#itemSelectionMatchContent").html(
                                '<div class="form-group"><select class="form-control" id="itemMatchDevice"><option value="">Select a Device</option></select></div>' +
                                '<div class="form-group"><label>'+((self.selectionType==='exec')?'Select control to run':'Select control to match against')+'</label><select class="form-control" id="itemMatchDevicegroup"><option value="">Select device first</option></select></div>');
                        $('#itemMatchDevice').combobox();
                        $('#itemMatchDevicegroup').combobox();
                        $("#itemMatchDevicegroup").combobox("disable");
                        $('#itemMatchDevicegroupaction').combobox();
                        $("#itemMatchDevicegroupaction").combobox("disable");
                        getHttpJsonRPC('{"jsonrpc": "2.0", "method": "DeviceService.getDeclaredDevices","id":"DeviceService.getDeclaredDevices"}', function(deviceData){
                            for(var i=0; i<deviceData.length;i++){
                                $("#itemMatchDevice").append('<option value="'+deviceData[i].id+'">'+deviceData[i].locationname+' - '+deviceData[i].name+'</option>');
                            }
                            $('#itemMatchDevice').combobox("refresh");
                            $("#itemMatchDevice").on('change', function(event) {
                                var deviceId = $(this).val();
                                var deviceName = $("#itemMatchDevice option:selected").text();
                                if (deviceId!=="") {
                                    try {
                                        $("#itemMatchDevicegroup").off('change');
                                        $("#itemMatchDevicegroup").html('<option value="">Select group</option>');
                                        $('#itemMatchDevicegroup').combobox("refresh");
                                        $("#itemMatchDevicegroup").combobox("enable");
                                        $("#itemMatchDevicegroupaction").off('change');
                                        $("#itemMatchDevicegroupaction").html('<option value="">Select group first</option>');
                                        $("#itemMatchDevicegroupaction").combobox("refresh");
                                    } catch (err) {
                                        /// no combobox yet.
                                    }
                                    getHttpJsonRPC('{"jsonrpc": "2.0", "method": "DeviceService.getDeviceActionGroups", "params":{"id":' + deviceId + ', "filter":'+((typeof optionsSet.devicefilter==="undefined")?'[]':'["' + optionsSet.devicefilter + '"]')+'},"id":"DeviceService.getDeviceActionGroups"}', function(data){
                                        for(var i=0; i<data.length;i++){
                                            for(var j=0; j < data[i].controls.length;j++){
                                                var control = data[i].controls[j];
                                                $("#itemMatchDevicegroup").append('<option value="'+data[i].id+'&'+control.typedetails.id+'">'+data[i].name+': '+control.typedetails.label+'</option>');
                                            }
                                        }
                                        $('#itemMatchDevicegroup').combobox("refresh");
                                        $("#itemMatchDevicegroup").on('change', function(event) {
                                            var combinedId = $(this).val().split("&");
                                            var groupId = combinedId[0];
                                            var controlId = combinedId[1];
                                            for(var i=0; i<data.length;i++){
                                                if(data[i].id===groupId){
                                                    for(var j=0; j < data[i].controls.length;j++){
                                                        var control = data[i].controls[j];
                                                        if(control.typedetails.id===controlId){
                                                            var groupName = data[i].name;
                                                            self.callBackData = {
                                                                "itemType" : "device",
                                                                "deviceId" : deviceId,
                                                                "deviceGroupId" : groupId,
                                                                "deviceCommandId" : controlId,
                                                                "deviceName" : deviceName,
                                                                "deviceGroupName" : groupName,
                                                                "deviceCommandName" : control.typedetails.label,
                                                                "deviceCommandValue" : control.typedetails.value,
                                                                "deviceCommandPrefix": ((typeof control.typedetails.prefix==="undefined")?"":control.typedetails.prefix),
                                                                "deviceCommandSuffix" : ((typeof control.typedetails.suffix==="undefined")?"":control.typedetails.suffix),
                                                                "deviceCommandDataType" : ((typeof control.typedetails.datatype==="undefined")?"":control.typedetails.datatype),
                                                                "deviceCommandType" : control.commandtype,
                                                                "deviceCommandSet" : control.typedetails.commandset,
                                                                "deviceCommandMinValue": ((typeof control.typedetails.minvalue==="undefined")?"":control.typedetails.minvalue),
                                                                "deviceCommandMaxValue": ((typeof control.typedetails.maxvalue==="undefined")?"":control.typedetails.maxvalue),
                                                                "deviceCommandWarnValue": ((typeof control.typedetails.warnvalue==="undefined")?"":control.typedetails.warnvalue),
                                                                "deviceCommandHighValue": ((typeof control.typedetails.highvalue==="undefined")?"":control.typedetails.highvalue),
                                                            };
                                                            $('#addMatchItem').show();
                                                            break;
                                                        }
                                                    }
                                                    break;
                                                }
                                            }
                                        });
                                    });
                                } else {
                                    $('#addMatchItem').hide();
                                    $("#itemMatchDevicegroup").off('change');
                                    $("#itemMatchDevicegroup").html('<option value="">Select device first</option>');
                                    $('#itemMatchDevicegroup').combobox("refresh");
                                    $("#itemMatchDevicegroup").combobox("disable");
                                    $("#itemMatchDevicegroupaction").off('change');
                                    $("#itemMatchDevicegroupaction").html('<option value="">Select group first</option>');
                                    $("#itemMatchDevicegroupaction").combobox("refresh");
                                    $("#itemMatchDevicegroupaction").combobox("disable");
                                }
                            });
                        });
                    break;
                    case "daytime":
                        $("#itemSelectionMatchContent").html(
                                '<div class="form-group"><select class="form-control" id="triggermatchitemdaytimereccurrence">'+
                                                            '<option value="">Select day reccurrence</option>' +
                                                            '<option value="ALL">Every day</option>' +
                                                            '<option value="WEEKDAY">Weekdays</option>' +
                                                            '<option value="WEEKEND">Weekends</option>' +
                                                            '<option value="MON">Monday</option>' +
                                                            '<option value="TUE">Tuesday</option>' +
                                                            '<option value="WED">Wednesday</option>' +
                                                            '<option value="THU">Thursday</option>' +
                                                            '<option value="FRI">Friday</option>' +
                                                            '<option value="SAT">Saturday</option>' +
                                                            '<option value="SUN">Sunday</option>' +
                                                         '</select>'+
                                '</div>' +
                                '<div class="form-group"><select class="form-control" id="triggermatchitemdaytimedaytime">'+
                                                            '<option value="">Select when</option>' +
                                                            '<option value="FIXED">Fixed time</option>' +
                                                            '<option value="SUNRISE">At sunrise</option>' +
                                                            '<option value="SUNSET">At sunset</option>' +
                                                         '</select>'+
                                '</div>');
                        $("#triggermatchitemdaytimereccurrence").combobox();
                        $("#triggermatchitemdaytimedaytime").combobox();
                        $("#triggermatchitemdaytimedaytime").combobox("disable");
                        $('#addMatchItem').hide();
                        $("#triggermatchitemdaytimereccurrence").on('change', function(event) {
                            if($("#triggermatchitemdaytimereccurrence").val()===""){
                                $("#triggermatchitemdaytimedaytime").combobox("disable");
                                $('#addMatchItem').hide();
                            } else {
                                $("#triggermatchitemdaytimedaytime").combobox("enable");
                            }
                            $("#triggermatchitemdaytimedaytime").off('change').on('change', function(event) {
                                if($("#triggermatchitemdaytimedaytime").val()===""){
                                    self.callBackData = {};
                                    $('#addMatchItem').hide();
                                } else {
                                    $('#addMatchItem').show();
                                    self.callBackData = {
                                        "itemType" : "daytime",
                                        "reccurrenceId" : $("#triggermatchitemdaytimereccurrence").val(),
                                        "daytimeItemId" : $("#triggermatchitemdaytimedaytime").val(),
                                        "reccurrenceName" : $("#triggermatchitemdaytimereccurrence option:selected").text(),
                                        "daytimeItemName" : $("#triggermatchitemdaytimedaytime option:selected").text()
                                    };
                                }
                            });
                        });
                    break;
                    case "macros":
                        $("#itemSelectionMatchContent").html(
                                '<div class="form-group"><select class="form-control" id="triggermatchitemmacroselect">'+
                                                            '<option value="">Select macro to run</option>' +
                                                         '</select>'+
                                '</div>');
                        $("#triggermatchitemmacroselect").combobox();
                        getHttpJsonRPC('{"jsonrpc": "2.0", "method": "MacroService.getMacros","id":"MacroService.getMacros"}', function(macroData){
                            for(var i=0; i<macroData.length;i++){
                                $("#triggermatchitemmacroselect").append('<option value="'+macroData[i].id+'">'+macroData[i].name+'</option>');
                            }
                            $("#triggermatchitemmacroselect").combobox("refresh");
                            $("#triggermatchitemmacroselect").off("change").on('change', function(event) {
                                if($("#triggermatchitemmacroselect").val()!==""){
                                    $('#addMatchItem').show();
                                    self.callBackData = {
                                        "itemType" : "macro",
                                        "macroId" : $("#triggermatchitemmacroselect").val(),
                                        "macroName" : $("#triggermatchitemmacroselect option:selected").text()
                                    };
                                } else {
                                    $('#addMatchItem').hide();
                                    self.callBackData = {};
                                }
                            });
                        });
                    break;
                    case "scenes":
                        $("#itemSelectionMatchContent").html(
                                '<div class="form-group"><select class="form-control" id="triggermatchitemsceneselect">'+
                                                            '<option value="">Select scene to run</option>' +
                                                         '</select>'+
                                '</div>');
                        $("#triggermatchitemsceneselect").combobox();
                        getHttpJsonRPC('{"jsonrpc": "2.0", "method": "ScenesService.getScenes","id":"ScenesService.getScenes"}', function(scenesData){
                            for(var i=0; i<scenesData.length;i++){
                                $("#triggermatchitemsceneselect").append('<option value="'+scenesData[i].id+'">'+scenesData[i].name+'</option>');
                            }
                            $("#triggermatchitemsceneselect").combobox("refresh");
                            $("#triggermatchitemsceneselect").off("change").on('change', function(event) {
                                if($("#triggermatchitemsceneselect").val()!==""){
                                    $('#addMatchItem').show();
                                    self.callBackData = {
                                        "itemType" : "scene",
                                        "sceneId" : $("#triggermatchitemsceneselect").val(),
                                        "sceneName" : $("#triggermatchitemsceneselect option:selected").text()
                                    };
                                } else {
                                    $('#addMatchItem').hide();
                                    self.callBackData = {};
                                }
                            });
                        });
                    break;
                    case "presence":
                        $("#itemSelectionMatchContent").html('<p>Click add item to add presence options</p>');
                        $('#addMatchItem').show();
                        self.callBackData = { "itemType" : "presence", "matchType" : "", "matchValue": 0 };
                    break;
                    case "daypart":
                        $("#itemSelectionMatchContent").html('<p>Click add item to add day part options</p>');
                        $('#addMatchItem').show();
                        self.callBackData = { "itemType" : "daypart", "matchType" : "", "matchValue": 0 };
                    break;
                    case "userstatus":
                        $("#itemSelectionMatchContent").html('<p>Click add item to add user status options</p>');
                        $('#addMatchItem').show();
                        self.callBackData = { "itemType" : "userstatus", "matchType" : "", "matchValue": 0 };
                    break;
                    case "currenttime":
                        $("#itemSelectionMatchContent").html('<p>Click add item to add current time options</p>');
                        $('#addMatchItem').show();
                        self.callBackData = { "itemType" : "currenttime", "matchType" : "", "matchValue": 0 };
                    break;
                    case "weatherplugin":
                        $("#itemSelectionMatchContent").html('<p>Click add item to add weather options</p>');
                        $('#addMatchItem').show();
                        self.callBackData = { "itemType" : "weatherplugin", "matchType" : "", "matchValue": 0 };
                    break;
                    case "utilityusages":
                        $("#itemSelectionMatchContent").html('<p>Click add item to add utility usages options</p>');
                        $('#addMatchItem').show();
                        self.callBackData = { "itemType" : "utilityusages", "matchType" : "", "matchValue": 0 };
                    break;
                }
            });
        }  
    );
    $("#itemSelectionMatchContent").empty();
};


function createGradientCSSForButtonFromTinyColors(tinyColors){
    var hexValueMiddle = '#' + tinyColors.toHex();
    var hexValueStart = tinycolor.lighten(hexValueMiddle).toString();
    var hexValueEnd = tinycolor.darken(hexValueMiddle).toString();
    return 'background-color: '+hexValueMiddle+';'+
           'background-image: -webkit-linear-gradient('+hexValueStart+', '+hexValueMiddle+' 60%, '+hexValueEnd+');'+
           'background-image: -o-linear-gradient('+hexValueStart+', '+hexValueMiddle+' 60%, '+hexValueEnd+');'+
           'background-image: -webkit-gradient(linear, left top, left bottom, from('+hexValueStart+'), color-stop(60%, '+hexValueMiddle+'), to('+hexValueEnd+'));'+
           'background-image: linear-gradient('+hexValueStart+', '+hexValueMiddle+' 60%, '+hexValueEnd+');'+
           'background-repeat: no-repeat;'+
           'filter: progid:DXImageTransform.Microsoft.gradient(startColorstr=\''+hexValueStart+'\', endColorstr=\''+hexValueEnd+'\', GradientType=0);'+
           '-webkit-filter: none;'+
           'filter: none;';
}