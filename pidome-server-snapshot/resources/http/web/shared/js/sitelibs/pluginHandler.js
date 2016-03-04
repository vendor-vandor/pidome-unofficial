/**
 * Creates a list of plugins for the add plugin selection
 * @param {type} pluginsList
 * @returns {undefined}
 */
function addNewPlugin(pluginsList, nameSpace, callBack){
    var form  = '<h4>Modifier/Supplier selection</h4>'+
                '<form class="form-horizontal">' +
                '<div class="form-group">' +
                '    <label for="addPluginSelect" class="col-sm-4 control-label">Select</label>' +
                '    <div class="col-sm-7" id="selectPluginContent">' +
                '        <select class="form-control" id="addPluginSelect">' +
                '           <option value="0">Select...</option>' +
                '        </select>' +
                '    </div>' +
                '</div>'+
                '<div id="pluginOptionsVisibility" style="display:none;">' +
                '</div>'+
                '</form>';
        form += '<div id="deviceFormVisibility"></div>';
    yesnoConfirmation("Select data modifier / supplier plugin", form, function(){
        var id = $("#pluginid").val();
        var name = $("#pluginMutationName").val();
        var description = $("#pluginMutationDescription").val();
        var optionsSet = {};
        $('#pluginOptionsVisibility input, #pluginOptionsVisibility button, #pluginOptionsVisibility select').each(
            function(index){  
                var input = $(this);
                if(input.attr('id').indexOf("_") > 0){
                    optionsSet[input.attr('id').split("_")[1]] = input.val();
                }
            }
        );
        if(id!="" && name.trim()!==""){
            getHttpJsonRPC('{"jsonrpc": "2.0", "method": "'+ nameSpace + '.savePlugin", "id":"'+ nameSpace + '.savePlugin", "params":{"installid":'+id+', "name":"'+name+'", "description":"'+description+'", "options":'+JSON.stringify(optionsSet)+'}}',function(resultData) {
                quickMessage("success", "Plugin saved");
                callBack();
            });
        } else {
            quickMessage("error", "Check all fields");
        }
    }, "Add plugin", "Cancel");
    
    $("#addPluginSelect").off("change").on("change", function(){
        var installId = $(this).val();
        getHttpJsonRPC('{"jsonrpc": "2.0", "method": "'+ nameSpace + '.getInstalledPluginOptions", "id":"'+ nameSpace + '.getInstalledPluginOptions", "params":{"installid":'+installId+'}}',function(resultData) {
            buildPluginOptions(installId, resultData);
        });
    });
    for(var i=0; i< pluginsList.length;i++){
        $("#addPluginSelect").append('<option value="'+pluginsList[i].id+'">'+pluginsList[i].name+'</option>');
    }
}

/**
 * Creates a list of plugins for the add plugin selection
 * @param {type} pluginsList
 * @returns {undefined}
 */
function editPlugin(pluginData, nameSpace){
    var form  = '<h4>Modifier/Supplier selection</h4>'+
                '<form class="form-horizontal">' +
                '<div class="form-group">' +
                '    <label for="addPluginSelect" class="col-sm-4 control-label">Plugin</label>' +
                '    <div class="col-sm-7" id="selectPluginContent">' +
                '        ' + pluginData.barebone +
                '    </div>' +
                '</div>'+
                '<div id="pluginOptionsVisibility" style="display:none;">' +
                '</div>'+
                '</form>';
        form += '<div id="deviceFormVisibility"></div>';
    yesnoConfirmation("Select data modifier / supplier plugin", form, function(){
        var id = $("#pluginid").val();
        var name = $("#pluginMutationName").val();
        var description = $("#pluginMutationDescription").val();
        var optionsSet = {};
        $('#pluginOptionsVisibility input, #pluginOptionsVisibility button, #pluginOptionsVisibility select').each(
            function(index){  
                var input = $(this);
                if(input.attr('id').indexOf("_") > 0){
                    optionsSet[input.attr('id').split("_")[1]] = input.val();
                }
            }
        );
        if(id!="" && name.trim()!==""){
            getHttpJsonRPC('{"jsonrpc": "2.0", "method": "'+ nameSpace + '.updatePlugin", "id":"'+ nameSpace + '.updatePlugin", "params":{"id":'+id+', "name":"'+name+'", "description":"'+description+'", "options":'+JSON.stringify(optionsSet)+'}}',function(resultData) {
                quickMessage("success", "Plugin saved");
            });
        } else {
            quickMessage("error", "Check all fields");
        }
    }, "Update plugin", "Cancel");
    
    buildPluginOptions(pluginData.id, pluginData);
}

function buildPluginOptions(pluginId, modData){
    $("#pluginOptionsVisibility").empty();
    $("#pluginOptionsVisibility").show();
    
    $("#selectPluginContent").empty();
    $("#selectPluginContent").append('<p style="padding-top:8px;">'+modData.basename+'</p><input type="hidden" name="pluginid" id="pluginid" value="'+pluginId+'" />');
    
    ///Plugin name
    $("#pluginOptionsVisibility").append('<div class="form-group"><label for="pluginMutationName" class="col-sm-4 control-label">Plugin name</label>' +
    '<div class="col-sm-7">' +
    '    <input type="text" class="form-control" id="pluginMutationName" placeholder="Plugin name" value="'+modData.name+'" />' +
    '</div></div>');
    
    ///Plugin description
    $("#pluginOptionsVisibility").append('<div class="form-group"><label for="pluginMutationDescription" class="col-sm-4 control-label">Plugin description</label>' +
    '<div class="col-sm-7">' +
    '    <input type="text" class="form-control" id="pluginMutationDescription" placeholder="Plugin description" value="'+modData.description+'" />' +
    '</div></div>');
    
    for(var i=0; i< modData.configuration.length; i++){
        var parent = modData.configuration[i];
        $("#pluginOptionsVisibility").append('<h4>'+parent.title+'</h4>');
        $("#pluginOptionsVisibility").append('<p>'+parent.description+'</p>');
        for(var j=0; j< parent.optionslist.length;j++){
            var option = parent.optionslist[j];
            var htmlSet = '';
            switch(option.optionfieldtype){
                case 'DEVICEDATA':
                    htmlSet += '<label for="selectionDevice_'+option.id+'" class="col-sm-4 control-label">'+option.name+'</label><div class="col-sm-7"><button class="btn btn-info btn-fill selectDevice" name="selectionDevice_'+option.id+'" id="selectionDevice_'+option.id+'" value="'+option.value+'">' + ((option.value!=="")?'Change':'Set') + '</button>';
                    htmlSet += '<span id="'+option.id+'_device_content">'+option.value+'</span></div>';
                break;
                default:
                    htmlSet += '<label for="pluginOption_'+option.id+'" class="col-sm-4 control-label">'+option.name+'</label><div class="col-sm-7"><input type="text" data-toggle="tooltip" data-placement="top" title="'+option.description+'" class="form-control" data-inputtype="'+option.optionfieldtype+'" name="pluginOption_'+option.id+'" id="pluginOption_'+option.id+'" value="'+((option.value==="")?option.defaultvalue:option.value)+'" /></div>'
                break;
            }
            $("#pluginOptionsVisibility").append('<div class="form-group">'+htmlSet+'</div>');
        }
    }
    
    var itemSelectionModal = new ItemSelectionModal();
    itemSelectionModal.setOptions({"deviceTypeFilter":["devices"]});
    itemSelectionModal.setSelectionType("match");

    $(".selectDevice").on('click', function (e) {
        e.preventDefault();
        var setId = $(this);
        itemSelectionModal.setCallBack(function(returnData){
            setId.val(returnData.deviceId+";"+returnData.deviceGroupId+";"+returnData.deviceCommandId);
        });
        itemSelectionModal.open();   
    });

    
    /*
     * Int test
     */
    $('[data-inputtype="INT"]').keypress(function(key) {
        if(key.charCode >= 48 && key.charCode <= 57){ /// 0-9
            return true;
        } else {
            return false;
        }
    });
    
    /// Double test
    $('[data-inputtype="DOUBLE"]').keypress(function(key) {
        if(key.charCode === 46 ||key.charCode === 110 || key.charCode === 190){ /// Quite often it is one of these, two or all used for period/decimal/period characters
            return true;
        } else if(key.charCode >= 48 && key.charCode <= 57){ /// 0-9
            return true;
        } else {
            return false;
        }
    });
    
    $('[data-toggle="tooltip"]').tooltip();
    
}