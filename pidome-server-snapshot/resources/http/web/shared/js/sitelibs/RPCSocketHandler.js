/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Socket class
 * @param {type} wsSocketEndpoint
 * @returns {RPCSocket}
 */
function RPCSocket(wsSocketEndpoint){
    this.enpoint = wsSocketEndpoint;
    this.defaultCallbacksArray = new Array();
    this.callbacksArray = new Array();
    this.supported = false;
}

/**
 * Adds a callback which can be cleared
 * @param {type} callBackFunction
 * @param {type} watch
 * @returns {undefined}
 */
RPCSocket.prototype.addCallback = function(callBackFunction, watch){
    callBackFunction.watched = watch;
    this.callbacksArray[this.callbacksArray.length] = callBackFunction;
};

/**
 * Adds a callback whom can not be cleared.
 * @param {type} callBackFunction
 * @param {type} watch
 * @returns {undefined}
 */
RPCSocket.prototype.addDefaultCallback = function(callBackFunction, watch){
    callBackFunction.watched = watch;
    this.defaultCallbacksArray[this.defaultCallbacksArray.length] = callBackFunction;
};

/**
 * Clears callbacks.
 * @returns {undefined}
 */
RPCSocket.prototype.clearFallbacks = function (){
    this.callbacksArray.length = 0;
};

/**
 * Action to be taken when a port opens
 * @param {type} evt
 * @returns {undefined}
 */
RPCSocket.prototype.onOpen = function(evt){
    this.socket.send('{"jsonrpc":"2.0", "id":"ClientService.resume", "method":"ClientService.resume", "params": {"key":"'+getCookie("key")+'"}}');
};

/**
 * Action to be taken when a port closes.
 * @param {type} evt
 * @returns {undefined}
 */
RPCSocket.prototype.onClose = function(evt){
    
};

/**
 * Action to take when there is a message received.
 * @param {type} evt
 * @returns {undefined}
 */
RPCSocket.prototype.onMessage = function(evt){
    if(this.defaultCallbacksArray.length>0 || this.callbacksArray.length>0){
        try {
            var parsedObject = jQuery.parseJSON( evt.data );
        } catch(err){};
        if(typeof parsedObject!=="undefined"){
            for (var i = 0; i < this.defaultCallbacksArray.length; i++) {
                if(this.defaultCallbacksArray[i].watched!==undefined && this.defaultCallbacksArray[i].watched===parsedObject.method) {
                    this.defaultCallbacksArray[i](parsedObject);
                }
            }
            for (var i = 0; i < this.callbacksArray.length; i++) {
                if(this.callbacksArray[i].watched!==undefined && this.callbacksArray[i].watched===parsedObject.method) {
                    this.callbacksArray[i](parsedObject);
                }
            }
        }
    }
};

/**
 * Action to take when an error happens.
 * @param {type} evt
 * @returns {undefined}
 */
RPCSocket.prototype.onError = function(evt){
    
};

/**
 * Returns if websockets are supported. Use after init.
 * @returns {Boolean}
 */
RPCSocket.prototype.isSupported = function (){
    return this.supported;
};

/**
 * Initializes a websocket connection.
 * @returns {undefined}
 */
RPCSocket.prototype.Init = function(){
    var self = this;
    try {
        this.socket = new WebSocket(this.enpoint); 
        this.socket.onopen = function(evt) { self.onOpen(evt); }; 
        this.socket.onclose = function(evt) { self.onClose(evt); }; 
        this.socket.onmessage = function(evt) { self.onMessage(evt); }; 
        this.socket.onerror = function(evt) { self.onError(evt); };
        this.supported = true;
    } catch (err){
        //
    }
};
