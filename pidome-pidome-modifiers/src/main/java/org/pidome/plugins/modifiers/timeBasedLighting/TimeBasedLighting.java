/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.plugins.modifiers.timeBasedLighting;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceColorPickerControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.interfaces.web.configuration.WebConfiguration;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.interfaces.web.configuration.WebOption;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.datamodifiers.DataModifierPlugin;
import org.pidome.server.connector.shareddata.SharedServerTimeService;
import org.pidome.server.connector.shareddata.SharedServerTimeServiceListener;
import org.pidome.server.connector.tools.ColorImpl;
import org.pidome.server.connector.tools.MathImpl;

/**
 *
 * @author John
 */
public class TimeBasedLighting extends DataModifierPlugin implements SharedServerTimeServiceListener {

    /// Should be optimized, maybe use their int equalivents.
    private Map<DeviceColorPickerControl,String> startColorValues   = new HashMap<>(); ///white (future is replaced with latest known control color before the plugin sets in (can be a collection of controls))
    private String currentProgress   = "0%"; /// percentage of the program
    
    private int[] endColorValue     = {255, 196, 16}; /// somwehat warmer color.
    
    private long startTime;
    private long endTime;
    private long duration;
    
    private static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(TimeBasedLighting.class);
    
    SimpleDateFormat format = new SimpleDateFormat("M-d-y HH:mm");
    SimpleDateFormat modFormat = new SimpleDateFormat("hh:mm");
    
    private Map<String, String> configuration ;
    
    private boolean mainRunning = false;
    
    public TimeBasedLighting(){
        WebConfiguration conf = new WebConfiguration();
        WebConfigurationOptionSet optionSetTransEnd = new WebConfigurationOptionSet("Light transition options");
        optionSetTransEnd.setConfigurationSetDescription("Time based lighting is used to move lighting colors to a more warmer reddish,orange color. "
                                                      + "This can help to get to sleep faster. It is recommended to set the end time about one hour you normally would go to bed. "
                                                      + "The transition time is by default set to one hour, which means that it will take one hour to go from the current light color setting to the new value. "
                + "Due to some limitations in the implementation the maximum end time is 23:59 and the end time minus the ahead start must be above 00:00");
        WebOption endTimeSetting = new WebOption("TRANSEND", "Select end time", "Select the time in hours:minutes the transition should end", WebOption.WebOptionConfigurationFieldType.TIME);
        endTimeSetting.setDefaultValue("23:00");
        optionSetTransEnd.addOption(endTimeSetting);
        
        WebOption transPeriod = new WebOption("TRANSSTART", "Set ahead start", "Set the amount of hours:minutes the transition should start before the set end time", WebOption.WebOptionConfigurationFieldType.TIME);
        transPeriod.setDefaultValue("01:00");
        optionSetTransEnd.addOption(transPeriod);
        
        conf.addOptionSet(optionSetTransEnd);
        this.setConfiguration(conf);
    }
    
    @Override
    public Object getCurrentValue() {
        return currentProgress;
    }

    @Override
    public DeviceControl.DataModifierDirection getDirection() {
        return DeviceControl.DataModifierDirection.INPUT_OUTPUT;
    }

    @Override
    public void handleInput(DeviceCommandRequest dcr) {
        if(!mainRunning){
            this.passToControl(dcr.getControl(), (Map<String,Object>)dcr.getCommandValue());
        }
    }

    @Override
    public void setConfigurationValues(Map<String, String> map) throws WebConfigurationException {
        this.configuration = map;
        setConfiguration();
    }

    private void setConfiguration() throws WebConfigurationException {
        format.setTimeZone(TimeZone.getDefault());
        modFormat.setTimeZone(TimeZone.getDefault());
        Date dateEnd;
        Date diff;
        GregorianCalendar cal = SharedServerTimeService.getCalendar();
        String date = new StringBuilder().append(cal.get(Calendar.MONTH)+1).append("-").append(cal.get(Calendar.DAY_OF_MONTH)).append("-").append(cal.get(Calendar.YEAR)).toString();
        try {
            if(this.configuration.containsKey("TRANSEND") && this.configuration.get("TRANSEND")!=null && ((String)this.configuration.get("TRANSEND")).matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")){
                dateEnd = format.parse(date + " " + (String)this.configuration.get("TRANSEND"));
            } else {
                dateEnd = format.parse(date + " " + "22:00");
            }
        } catch (ParseException ex) {
            throw new WebConfigurationException(ex);
        }
        try {
            if(this.configuration.containsKey("TRANSSTART") && this.configuration.get("TRANSSTART")!=null && !((String)this.configuration.get("TRANSSTART")).equals("")){
                diff = modFormat.parse((String)this.configuration.get("TRANSSTART"));
            } else {
                diff = modFormat.parse("01:00");
            }
        } catch (ParseException ex){
            throw new WebConfigurationException(ex);
        }
        
        endTime   = dateEnd.getTime() + TimeZone.getDefault().getOffset(dateEnd.getTime());
        startTime = endTime - (diff.getTime() + TimeZone.getDefault().getOffset(dateEnd.getTime()));
        /// get the progress percentage of the program.
        /// First get the total duration
        duration = (endTime - startTime);
    }
    
    @Override
    public void handleNewTimeServiceMinute() {
        try {
            GregorianCalendar cal = SharedServerTimeService.getCalendar();
            if(cal.get(Calendar.HOUR) == 0 && cal.get(Calendar.MINUTE) == 0){
                /// recalculate
                setConfiguration();
            }
            long current = cal.getTime().getTime() + TimeZone.getDefault().getOffset(cal.getTime().getTime());
            /// Get the progress of time since start.
            long progress = current - startTime;
            double progressPercentage = ((100.0/duration) * progress);            
            if ((current >= this.startTime && current <= this.endTime)){
                if(progressPercentage>100){
                    progressPercentage = 100;
                }
                if(!mainRunning){
                    startColorValues.clear();
                    mainRunning = true;
                }
                for(DeviceControl control : this.getBoundControls()){
                    DeviceColorPickerControl cPicker = (DeviceColorPickerControl)control;
                    if(!this.startColorValues.containsKey(cPicker)){
                        this.startColorValues.put(cPicker, cPicker.getHex());
                    }
                }
                LOG.info("Percentage: ({})", progressPercentage);
                currentProgress = progressPercentage + "%";
                for(Map.Entry<DeviceColorPickerControl,String> control:startColorValues.entrySet()){
                    // get current value
                    int[] startValue = ColorImpl.hexToRgb(control.getValue());
                    /// Fortunately we have a map function which can take of the hurdle to map the current progress to the color value (which also works with swapped upper/lower bounds).
                    int[] newRGB = {(int)MathImpl.map(progressPercentage, 0, 100, startValue[0], endColorValue[0]),
                                    (int)MathImpl.map(progressPercentage, 0, 100, startValue[1], endColorValue[1]),
                                    (int)MathImpl.map(progressPercentage, 0, 100, startValue[2], endColorValue[2])};
                    /// Send the new color as hex string to the device.
                    Map<String,Object> colorMap = new HashMap<>();
                    colorMap.put("hex", ColorImpl.RGBToHex(newRGB));
                    LOG.info("{}: Updating {} with {}", this.getPluginName(), control.getKey().getDescription(), newRGB);
                    this.passToControl(control.getKey(), colorMap);
                }
            } else {
                currentProgress = "0%";
                mainRunning = false;
                startColorValues.clear();
            }
        } catch (WebConfigurationException ex){
            LOG.error("Can not continue plugin run: {}", ex.getMessage());
        }
    }    
    
    @Override
    public void startPlugin() throws PluginException {
        SharedServerTimeService.addListener(this);
        this.setRunning(true);
    }

    @Override
    public void stopPlugin() throws PluginException {
        SharedServerTimeService.removeListener(this);
        this.setRunning(false);
    }

    @Override
    public void handleCustomWebCommand(String string, Map<String, String> map) {
        /// Not used.
    }

    @Override
    public void prepareWebPresentation() {
        // not used
    }

    @Override
    public boolean hasGraphData() {
        return false;
    }

    @Override
    public void prepareDelete() {
        startColorValues.clear();
    }
    
}