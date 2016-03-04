/**
 * Graphing class using HighCharts.
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
 * @param {type} title
 * @param {type} clean
 * @returns {Graphing}
 */

function Graphing(divId, serverLocation, itemId, groupId, controlId, description, datacattype, sourceType, graphType, decimals, title, clean){
    this.divId          = divId;
    this.serverLocation = serverLocation;
    this.itemId         = itemId;
    this.groupId        = groupId;
    this.itemCategory   = controlId;
    this.description    = description;
    this.width          = 1024;
    this.height         = 512;
    this.sourceType     = sourceType;
    this.decimals       = decimals;
    this.theme          = siteSettings.getTheme();
    this.dataUrl        = "";
    this.choiceIndex    = 1;
    if(clean===undefined){
        this.onlyGraph = false;
    } else if (clean===false){
        this.onlyGraph = false;
    } else {
        this.onlyGraph = true;
    }
    if(title===undefined){
        this.title = null;
    } else {
        this.title = title;
    }
    if(graphType==="time-log"){
        this.graphType = "spline";
        this.yAxisType = "logarithmic";
    } else if (graphType==="time-totals"){
        this.graphType = "column";
    } else if (graphType==="time-series"){
        this.graphType = "spline";
    }
    
    if(graphType==="time-log"){
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
    
    Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });

    var menuButtons = {};
    if(this.onlyGraph){
        menuButtons = {
            buttonOptions: {
                enabled: false
            }
        };
    } else {
        menuButtons = {
            buttonOptions: {
                enabled: true
            }
        };
    }
    var yAxisOptions = {
                            title: {
                                    text: this.datacat
                            }
                        };
    if(typeof this.yAxisType!=="undefined"){
        yAxisOptions.type = 'logarithmic';
        yAxisOptions.minorTickInterval= 0.1;
    }

    this.options = {
        chart: {
                renderTo: this.divId + "_chart",
                width: this.width,
                height: this.height,
                type: this.graphType,
                backgroundColor:'rgba(255, 255, 255, 0)'
        },
        title: {
                text: this.title
        },
        xAxis: {
            type: 'datetime'
        },
        yAxis: yAxisOptions,
        tooltip: {
            valueDecimals: this.decimals
        },
        credits: {
            enabled: false
        },
        series: [],
        navigation: menuButtons
    };
    
    this.seriesOptions = {
        name: this.description,
        data: [],
        id: this.divId
    };
    
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
  
};

Graphing.prototype.createTooltip = function(){
    
}

Graphing.prototype.getTheme = function (){
    return {
       colors: ["#2b908f", "#90ee7e", "#f45b5b", "#7798BF", "#aaeeee", "#ff0066", "#eeaaee", "#55BF3B", "#DF5353", "#7798BF", "#aaeeee"],
       chart: {
          backgroundColor:'rgba(255, 255, 255, 0)',
          style: {
             fontFamily: "'Unica One', sans-serif"
          },
          plotBorderColor: '#606063',
          borderWidth: 0
       },
       title: {
          style: {
             color: '#E0E0E3',
             textTransform: 'uppercase',
             fontSize: '20px'
          }
       },
       subtitle: {
          style: {
             color: '#E0E0E3',
             textTransform: 'uppercase'
          }
       },
       xAxis: {
          gridLineColor: '#707073',
          labels: {
             style: {
                color: '#E0E0E3'
             }
          },
          lineColor: '#707073',
          minorGridLineColor: '#505053',
          tickColor: '#707073',
          title: {
             style: {
                color: '#A0A0A3'

             }
          }
       },
       yAxis: {
          gridLineColor: '#707073',
          labels: {
             style: {
                color: '#E0E0E3'
             }
          },
          lineColor: '#707073',
          minorGridLineColor: '#505053',
          tickColor: '#707073',
          tickWidth: 1,
          title: {
             style: {
                color: '#A0A0A3'
             }
          }
       },
       tooltip: {
          backgroundColor: 'rgba(0, 0, 0, 0.85)',
          style: {
             color: '#F0F0F0'
          }
       },
       plotOptions: {
          series: {
             dataLabels: {
                color: '#B0B0B3'
             },
             marker: {
                lineColor: '#333'
             }
          },
          boxplot: {
             fillColor: '#505053'
          },
          candlestick: {
             lineColor: 'white'
          },
          errorbar: {
             color: 'white'
          }
       },
       legend: {
          itemStyle: {
             color: '#E0E0E3'
          },
          itemHoverStyle: {
             color: '#FFF'
          },
          itemHiddenStyle: {
             color: '#606063'
          }
       },
       credits: {
          style: {
             color: '#666'
          }
       },
       labels: {
          style: {
             color: '#707073'
          }
       },

       drilldown: {
          activeAxisLabelStyle: {
             color: '#F0F0F3'
          },
          activeDataLabelStyle: {
             color: '#F0F0F3'
          }
       },

       navigation: {
          buttonOptions: {
             symbolStroke: '#DDDDDD',
             theme: {
                fill: '#505053'
             }
          }
       },

       // scroll charts
       rangeSelector: {
          buttonTheme: {
             fill: '#505053',
             stroke: '#000000',
             style: {
                color: '#CCC'
             },
             states: {
                hover: {
                   fill: '#707073',
                   stroke: '#000000',
                   style: {
                      color: 'white'
                   }
                },
                select: {
                   fill: '#000003',
                   stroke: '#000000',
                   style: {
                      color: 'white'
                   }
                }
             }
          },
          inputBoxBorderColor: '#505053',
          inputStyle: {
             backgroundColor: '#333',
             color: 'silver'
          },
          labelStyle: {
             color: 'silver'
          }
       },

       navigator: {
          handles: {
             backgroundColor: '#666',
             borderColor: '#AAA'
          },
          outlineColor: '#CCC',
          maskFill: 'rgba(255,255,255,0.1)',
          series: {
             color: '#7798BF',
             lineColor: '#A6C7ED'
          },
          xAxis: {
             gridLineColor: '#505053'
          }
       },

       scrollbar: {
          barBackgroundColor: '#808083',
          barBorderColor: '#808083',
          buttonArrowColor: '#CCC',
          buttonBackgroundColor: '#606063',
          buttonBorderColor: '#606063',
          rifleColor: '#FFF',
          trackBackgroundColor: '#404043',
          trackBorderColor: '#404043'
       },

       legendBackgroundColor: 'rgba(0, 0, 0, 0.5)',
       background2: '#505053',
       dataLabelsColor: '#B0B0B3',
       textColor: '#C0C0C0',
       contrastTextColor: '#F0F0F3',
       maskColor: 'rgba(255,255,255,0.3)'
    };

}

Graphing.prototype.updateConfig = function ( choiceIndex ){
    
    var self = this;
    
    var urlToUse = "getDeviceGraph";
    
    this.choiceIndex = choiceIndex;
    
    switch(this.sourceType){
        case "DEVICE":
            urlToUse = "getDeviceGraph";
        break;
        case "PLUGIN":
            urlToUse = "getUtilityGraph";
        break;
    }
    
    this.dataUrl = '/jsonrpc.json?rpc={"jsonrpc": "2.0", "method": "GraphService.'+urlToUse+'", "params":{"id":'+this.itemId+',"group":"'+this.groupId+'","control":"' + this.itemCategory + '","range":["'+this.dataSelectSource[choiceIndex]+'"], "calculation":null}, "id":"GraphService.'+urlToUse+'"}';
    
    $.get(this.dataUrl, function(resultData) {
        var seriesData = [];
        for(var i=0; i < resultData.result.data[self.dataSelectSource[self.choiceIndex]].length;i++){
            var dataSet = resultData.result.data[self.dataSelectSource[self.choiceIndex]][i];
            seriesData.push(
                [dataSet.key,dataSet.value]
            );
        }
        if(self.chart!==undefined){
            self.chart.get(self.divId).setData(seriesData,false, false);
            self.chart.redraw();
        } else {
            self.seriesOptions.data = seriesData;
            self.options.series.push(self.seriesOptions);
            // Apply the theme
            Highcharts.setOptions(self.getTheme());
            self.chart = new Highcharts.Chart(self.options);
        }
    });
    
};

Graphing.prototype.updateGraph = function (datacollectionIndex){
    this.updateConfig(datacollectionIndex);
};

Graphing.prototype.createGraph = function (width, height, startOptionIndex){
    this.width = width;
    this.height= height;
    this.options.chart.width = width;
    this.options.chart.height= height;
    var self = this;
    if(startOptionIndex===undefined){
        this.updateConfig(1);
    } else {
        this.updateConfig(startOptionIndex);
    }
    
    var uid = createUUID();
    
    $('#' + this.divId).empty();
    if(this.onlyGraph){
        $('#' + this.divId).html('<div class="devicechart" id="'+this.divId+'_chart"></div>');
    } else {
        $('#' + this.divId).html('<div style="clear:both;"><div id="'+this.divId+'_dataselect" class="graphdataselect" style="float:left;"></div><div style="float:left;">&nbsp;<button id="graphdataselect_'+uid+'">Graph data</button></div></div><div class="devicechart" id="'+this.divId+'_chart"></div>');        
    }
    $('#' + this.divId+'_chart').jqxChart(this.dataSettings);
    
    if(!this.onlyGraph){
        $('#' + this.divId+'_dataselect').jqxDropDownList({source: this.visibleSelectSet, selectedIndex: 1, width: '200', height: '25', theme: this.theme});
        $('#' + this.divId+'_dataselect').on('select', function(event) {
            self.updateGraph(event.args.index);
        });
        $("#graphdataselect_"+uid).jqxButton({ width: '100', theme: this.theme});
        $("#graphdataselect_"+uid).on("click", function(){
            window.open(self.dataUrl,'_blank');
            return false;
        });
    }
    
    if(this.sourceType=="DEVICE"){
        pidomeRPCSocket.addCallback(function(params) {
            try {
                var paramSet = params.params;
                if(self.itemId === paramSet.id){
                    for(var i=0;i<paramSet.groups.length;i++){
                        if(self.groupId === paramSet.groups[i].groupid){
                            for(var controlSetId in paramSet.groups[i].controls){
                                if(controlSetId === self.itemCategory){
                                    if (self.dataSelectSource[self.choiceIndex] === "hour" && self.chart!==undefined && self.chart.series.length===1) {
                                        var series = self.chart.series[0];
                                        series.addPoint([(new Date()).getTime(), paramSet.groups[i].controls[controlSetId]], true, true);
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            } catch (err) {}
        }, "DeviceService.sendDevice");
    }
    
};