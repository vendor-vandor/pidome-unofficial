/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * DeviceGraph classe using jqwidget graphs.
 * @param {type} divId
 * @param {type} serverLocation
 * @param {type} itemId
 * @param {type} groupId
 * @param {type} controlId
 * @param {type} description
 * @param {type} datacattype
 * @param {type} sourceType
 * @param {type} graphType
 * @param {type} decimals
 * @returns {DeviceGraph}
 */
function DeviceGraph(divId, serverLocation, itemId, groupId, controlId, description, datacattype, sourceType, graphType, decimals){
    this.divId          = divId;
    this.serverLocation = serverLocation;
    this.itemId         = itemId;
    this.groupId        = groupId;
    this.itemCategory   = controlId;
    this.description    = description;
    this.width          = 1024;
    this.height         = 512;
    this.sourceType     = sourceType;
    this.graphType      = graphType;
    this.decimals       = decimals;
    this.dataUrl        = "";
    this.theme = siteSettings.getTheme();
    if(this.graphType==="time-log"){
        this.decimals = 4;
    }
    
    switch(datacattype){
        case "TEMP":
            this.datacat = "Temperature in 'C";
        break;
        case "LUX":
            this.datacat = "Light intensity in Lux";
        break;
        case "MB":
            this.datacat = "Mega Bytes";
        break;
        case "PERC":
            this.datacat = "Percentage";
        break;
        case "TOTALS":
            this.datacat = "Totals";
        break;
        default:
            this.datacat = datacattype;
        break;
    }
    
    this.dataSelectSource = [
        "hour",
        "day",
        "week",
        "month",
        "year"
    ];
    
    this.visibleSelectSet= [
        "Last hour",
        "Last day (24h)",
        "Last week",
        "Last month",
        "Last year"
    ];
    
    this.descriptionSet = [
        "Showing last hour",
        "Showing last day",
        "Last week in weekdays",
        "Last month in days",
        "Last year in weeknumbers"
    ];
    
}

DeviceGraph.prototype.updateConfig = function ( choiceIndex ){
    
    var self = this;
    
    var urlToUse = "devicehistdata";
    
    switch(this.sourceType){
        case "DEVICE":
            urlToUse = "devicehistdata";
        break;
        case "PLUGIN":
            urlToUse = "utilityhistory";
        break;
    }
    
    this.dataUrl = "/xmlapi/"+urlToUse+".xml?id="+this.itemId+"&dataGroup="+this.groupId+"&dataItem=" + this.itemCategory + "&" + this.dataSelectSource[choiceIndex] + "=true";
    
    this.dataSource = {
        datafields: [{name: 'data', type: 'float'}, {name: 'time', map: 'time', type: 'int'}],
        root: "data>history>" + this.dataSelectSource[choiceIndex],
        record: "data",
        datatype: "xml",
        url: this.dataUrl
    };
    
    this.dataAdapter = new $.jqx.dataAdapter(this.dataSource);
    
    var days = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];
    var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Okt", "Nov", "Dec"];
    
    var baseUnitInterval = 300;
    var labelRotation    = 0;
    
    switch(choiceIndex){
        case 1:
            baseUnitInterval = 3600;
            labelRotation = -45;
            break;
        case 2:
        case 3:
            baseUnitInterval = 86400;
            break;
        case 4:
            baseUnitInterval = 604800;
            break;
        default:
            baseUnitInterval = 300;
            break;
    }
    

    var toolTipCustomFormatFn = function (value, itemIndex, serie, group, categoryValue, categoryAxis) {
        var date = new Date((categoryValue) * 1000);
        var dateLabel;
        switch (choiceIndex) {
            case 1:
                dateLabel = date.getHours() + ":" + padZeros(date.getMinutes());
                break;
            case 2:
            case 3:
                dateLabel = days[date.getDay()] + ", " + months[date.getMonth()] + " " + date.getDate();
                break;
            case 4:
                dateLabel = "Week " + date.getWeek() + " (" + months[date.getMonth()] + " " + date.getFullYear()+ ") ";
                break;
            default:
                dateLabel = date.getHours() + ":" + padZeros(date.getMinutes());
                break;
        }        
        return '<div style="text-align:left"><b>' + dateLabel + '</b> - '+value.toFixed(self.decimals)+'</div>';
    };
    
    var valueAxisType;
    if(this.graphType==="time-log"){
        valueAxisType = {
                    displayValueAxis: true,
                    axisSize: 'auto',
                    logarithmicScale: true,
                    logarithmicScaleBase: 10,
                    tickMarksColor: '#888888',
                    horizontalTextAlignment: 'right', 
                    formatSettings: { decimalPlaces: self.decimals }
                };
    } else {
        valueAxisType = {
                    displayValueAxis: true,
                    axisSize: 'auto',
                    tickMarksColor: '#888888',
                    horizontalTextAlignment: 'right', 
                    formatFunction: function (value, a, b, c) {
                        var float = parseFloat(value);
                        var int = parseInt(value);
                        if (float === int) {
                            return int;
                        } else {
                            return float.toFixed(self.decimals);
                        }
                    }
                };
    }
    
    this.dataSettings = {
        title: this.description,
        description: this.descriptionSet[choiceIndex],
        showLegend: true,
        source: this.dataAdapter,
        backgroundColor: "#252526",
        categoryAxis: {
            dataField: 'time',
            textRotationAngle : labelRotation,
            formatFunction: function(value) {
                var date = new Date(value * 1000);
                switch(choiceIndex){
                    case 1:
                        return date.getHours() + ":" + padZeros(date.getMinutes());
                    break;
                    case 2:
                        return days[date.getDay()];
                    break;
                    case 3:
                        return date.getDate();
                    break;
                    case 4:
                        return date.getWeek();
                    break;
                    default:
                        return date.getHours() + ":" + padZeros(date.getMinutes());
                    break;
                }
            },
            showGridLines: true,
            unitInterval: baseUnitInterval
        },
        seriesGroups: [{
                type: (this.graphType==="time-series"||this.graphType==="time-log")?"line":"column",
                toolTipFormatFunction: toolTipCustomFormatFn,
                valueAxis: valueAxisType,
                series: [{
                    dataField: 'data', 
                    color: "#be1d1d", 
                    lineColor: "#be1d1d", 
                    displayText: this.datacat
                }]
            }]
    };  
};


DeviceGraph.prototype.updateGraph = function (datacollectionIndex){
    this.updateConfig(datacollectionIndex);
    $('#' + this.divId+'_chart').jqxChart(this.dataSettings);
    $('#' + this.divId+'_chart').jqxChart('refresh');
};

/**
 * Creates the graph in the given div id.
 * @param {type} divId
 * @returns {undefined}
 */
DeviceGraph.prototype.createGraph = function (width, height){
    this.width = width;
    this.height= height;
    this.updateConfig(1);
    
    var uid = createUUID();
    
    $('#' + this.divId).empty();
    $('#' + this.divId).html('<div style="clear:both;"><div id="'+this.divId+'_dataselect" class="graphdataselect" style="float:left;"></div><div style="float:left;">&nbsp;<button id="graphdataselect_'+uid+'">Graph data</button></div></div><div class="devicechart" style="width: '+width+'px; height: '+height+'px;" id="'+this.divId+'_chart"></div>');
    $('#' + this.divId+'_chart').jqxChart(this.dataSettings);
    
    $('#' + this.divId+'_dataselect').jqxDropDownList({source: this.visibleSelectSet, selectedIndex: 1, width: '200', height: '25', theme: this.theme});
    var self = this;
    $('#' + this.divId+'_dataselect').on('select', function(event) {
        self.updateGraph(event.args.index);
    });
    $("#graphdataselect_"+uid).jqxButton({ width: '100', theme: this.theme});
    $("#graphdataselect_"+uid).on("click", function(){
        window.open(self.dataUrl,'_blank');
        return false;
    });
    
};