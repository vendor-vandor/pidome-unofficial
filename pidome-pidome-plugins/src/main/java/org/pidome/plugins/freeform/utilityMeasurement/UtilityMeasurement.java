/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.freeform.utilityMeasurement;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.interfaces.web.configuration.WebConfiguration;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.interfaces.web.configuration.WebOption;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.graphdata.RoundRobinDataGraphItem;
import org.pidome.server.connector.plugins.hooks.DeviceHook;
import org.pidome.server.connector.plugins.hooks.DeviceHookListener;
import org.pidome.server.connector.plugins.utilitydata.UtilityData;
import org.pidome.server.connector.plugins.utilitydata.UtilityDataException;
import org.pidome.server.connector.plugins.utilitydata.UtilityDataGas;
import org.pidome.server.connector.plugins.utilitydata.UtilityDataPower;
import org.pidome.server.connector.plugins.utilitydata.UtilityDataWater;

/**
 *
 * @author John
 */
public class UtilityMeasurement extends UtilityData implements DeviceHookListener {
    
    static Logger LOG = LogManager.getLogger(UtilityMeasurement.class);
    
    Map<String, String> configuration;
    
    int powerDeviceId;
    String powerDeviceValueSet;
    
    int waterDeviceId;
    String waterDeviceValueSet;
    
    int gasDeviceId;
    String gasDeviceValueSet;
    
    int today =new Date().getDate();
    
    String powerUnitName       = "kW/h";
    String powerSingleUnitName = "Watt";
    double powerUnitValue      = 0D;
    double powerThresholdAnn   = 11000D;
    boolean isAbsolutePowerWatt= false;
    boolean isAbsolutePowerKWH = false;
    boolean isAbsolutePowerKWHTotal = false;
    
    String waterUnitName       = "Liter³";
    double waterUnitValue      = 0D;
    double waterThresholdAnn   = 550D;
    boolean isAbsoluteWater    = false;
    boolean isAbsoluteWaterTotal = false;
    
    String gasUnitName         = "M³";
    double gasUnitValue        = 0D;
    double gasThresholdAnn     = 425D;
    boolean isAbsoluteGas      = false;
    boolean isAbsoluteGasTotal = false;
    
    Long lastPowerMeasurementTime    = 0L;
    Long currentPowerMeasurementTime = 0L;
    
    double lastWaterMeasurement = 0D;
    double prevWaterMeasurement = 0D;
    
    double lastGasMeasurement   = 0D;
    double prevGasMeasurement   = 0D;
    
    double currentPowerUsageWatt = 0;
    double currentPowerUsageKWh  = 0;
    double prevPowerUsageKWh     = 0;
    
    double kwhToday              = 0;
    double waterToday            = 0;
    double gasToday              = 0;
    
    public UtilityMeasurement(){
        constructSettings();
    }

    public final void constructSettings(){
        WebConfiguration conf = new WebConfiguration();
        WebConfigurationOptionSet optionSetPower = new WebConfigurationOptionSet("Power usage settings");
        optionSetPower.setConfigurationSetDescription("Select 'Uses absolute values' and the correct type if your device delivers absolute current values and not pulses. If selected measurement type and units are discarded");
        optionSetPower.addOption(new WebOption("POWERDEVICE", "Select device for power", "Select device to use which delivers power pulse usage", WebOption.WebOptionConfigurationFieldType.DEVICEDATA));
        
        Map<String,String> absoluteOptions = new HashMap<>();
        absoluteOptions.put("ABSOLUTE_NONE", "No, Use pulses");
        absoluteOptions.put("ABSOLUTE_WATT", "Current WATT");
        absoluteOptions.put("ABSOLUTE_KWATTH", "Current KW/H");
        absoluteOptions.put("ABSOLUTE_KWATTHTOTAL", "Total KW/H");
        
        WebOption absolutePowerOption = new WebOption("POWERABSOLUTETYPE", "Uses abolute values", "The device must deliver WATT and not KWH", WebOption.WebOptionConfigurationFieldType.SELECT,absoluteOptions);
        absolutePowerOption.setDefaultValue("ABSOLUTE_NONE");
        optionSetPower.addOption(absolutePowerOption);
        
        Map<String,String> measurementOptions = new HashMap<>();
        measurementOptions.put("POWER_TIMED_RC", "R/C - Time period measurement");
        measurementOptions.put("POWER_CONTINUOUS_RC", "R/C - Per pulse/rotation measurement");
        measurementOptions.put("POWER_TIMED_KH", "Kh - Time period measurement");
        measurementOptions.put("POWER_CONTINUOUS_KH", "Kh - Per pulse/rotation measurement Kh");
        optionSetPower.addOption(new WebOption("POWERMEASUREMENT", "Select measurement type", "If you do not have Kh on your meter user the R/C types. If you have a device that sends an amount of pulses/rotations per time period, for example per minute, choose time period, otherwise per pulse/rotation.", WebOption.WebOptionConfigurationFieldType.SELECT,measurementOptions));
        optionSetPower.addOption(new WebOption("POWERUNITS", "Fill in the unit your meter uses", "Fill in Kh or R/C value the meter uses per rotation/pulse", WebOption.WebOptionConfigurationFieldType.DOUBLE));
        WebOption powerSingleName = new WebOption("POWERSINGLENAME", "Fill in the single unit name", "Fill in the single unit name used (watt etc...)", WebOption.WebOptionConfigurationFieldType.STRING);
        powerSingleName.setDefaultValue(powerSingleUnitName);
        optionSetPower.addOption(powerSingleName);
        WebOption powerName = new WebOption("POWERNAME", "Fill in the total unit name", "Fill in the unit name used (kWh/kh/etc...)", WebOption.WebOptionConfigurationFieldType.STRING);
        powerName.setDefaultValue(powerUnitName);
        optionSetPower.addOption(powerName);
        WebOption powerUsed = new WebOption("POWERUSED", "Fill in last year usage", "Fill in the last years power usage, it will be used as threshold.", WebOption.WebOptionConfigurationFieldType.DOUBLE);
        powerUsed.setDefaultValue(String.valueOf(powerThresholdAnn));
        optionSetPower.addOption(powerUsed);
        
        conf.addOptionSet(optionSetPower);
        
        WebConfigurationOptionSet optionSetWater = new WebConfigurationOptionSet("Water usage settings");
        optionSetWater.setConfigurationSetDescription("Select 'Uses absolute values' if your device delivers absolute current values and not pulses. If selected the units are discarded.");
        optionSetWater.addOption(new WebOption("WATERDEVICE", "Select device for water", "Select device to use which delivers water pulse usage", WebOption.WebOptionConfigurationFieldType.DEVICEDATA));
        
        Map<String,String> absoluteWaterOptions = new HashMap<>();
        absoluteWaterOptions.put("ABSOLUTE_NONE", "No, Use pulses");
        absoluteWaterOptions.put("ABSOLUTE_WATER", "Current Usage");
        absoluteWaterOptions.put("ABSOLUTE_WATERTOTAL", "Total Usage");
        WebOption absoluteWaterOption = new WebOption("WATERABSOLUTETYPE", "Uses abolute current values", "Select this if the device reports usage instead of pules", WebOption.WebOptionConfigurationFieldType.SELECT, absoluteWaterOptions);
        absoluteWaterOption.setDefaultValue("ABSOLUTE_NONE");
        optionSetWater.addOption(absoluteWaterOption);
        
        optionSetWater.addOption(new WebOption("WATERUNITS", "Fill in the unit your meter uses", "Fill in the amount of water used per rotation/pulse", WebOption.WebOptionConfigurationFieldType.DOUBLE));
        WebOption waterName = new WebOption("WATERNAME", "Fill in the unit name", "Fill in the unit name used (Liter³)", WebOption.WebOptionConfigurationFieldType.STRING);
        waterName.setDefaultValue(waterUnitName);
        optionSetWater.addOption(waterName);
        WebOption waterUsed = new WebOption("WATERUSED", "Fill in last year usage", "Fill in the last years water usage, it will be used as threshold.", WebOption.WebOptionConfigurationFieldType.DOUBLE);
        waterUsed.setDefaultValue(String.valueOf(waterThresholdAnn));
        optionSetWater.addOption(waterUsed);
        
        conf.addOptionSet(optionSetWater);
        
        WebConfigurationOptionSet optionSetGas = new WebConfigurationOptionSet("Gas usage settings");
        optionSetGas.setConfigurationSetDescription("Select 'Uses absolute values' if your device delivers absolute current values and not pulses. If selected the units are discarded.");
        optionSetGas.addOption(new WebOption("GASDEVICE", "Select device for gas", "Select device to use which delivers gas pulse usage", WebOption.WebOptionConfigurationFieldType.DEVICEDATA));
        
        Map<String,String> absoluteGasOptions = new HashMap<>();
        absoluteGasOptions.put("ABSOLUTE_NONE", "No, Use pulses");
        absoluteGasOptions.put("ABSOLUTE_GAS", "Current Usage");
        absoluteGasOptions.put("ABSOLUTE_GASTOTAL", "Total Usage");
        WebOption absoluteGaseOption = new WebOption("GASABSOLUTETYPE", "Uses abolute current values", "Select this if the device reports usage instead of pules", WebOption.WebOptionConfigurationFieldType.SELECT, absoluteGasOptions);
        absoluteGaseOption.setDefaultValue("ABSOLUTE_NONE");
        optionSetGas.addOption(absoluteGaseOption);
        
        optionSetGas.addOption(new WebOption("GASUNITS", "Fill in the unit your meter uses", "Fill in the amount of gas used per rotation/pulse", WebOption.WebOptionConfigurationFieldType.DOUBLE));
        WebOption gasName = new WebOption("GASNAME", "Fill in the unit name", "Fill in the unit name used (M³)", WebOption.WebOptionConfigurationFieldType.STRING);
        gasName.setDefaultValue(gasUnitName);
        optionSetGas.addOption(gasName);
        WebOption gasUsed = new WebOption("GASUSED", "Fill in last year usage", "Fill in the last years gas usage, it will be used as threshold.", WebOption.WebOptionConfigurationFieldType.DOUBLE);
        gasUsed.setDefaultValue(String.valueOf(gasThresholdAnn));
        optionSetGas.addOption(gasUsed);

        conf.addOptionSet(optionSetGas);
        
        /*
        WebConfigurationOptionSet optionSetCurrent = new WebConfigurationOptionSet("Start values");
        optionSetCurrent.setConfigurationSetDescription("When you want to use start values enter them here. They are used only once and this is when you save the plugin.");
        
        WebOption powerCurrent = new WebOption("POWERCURRENT", "Current total power usage", "Fill in your current total power usage.", WebOption.WebOptionConfigurationFieldType.DOUBLE);
        WebOption waterCurrent = new WebOption("WATERCURRENT", "Current total water usage", "Fill in your current total water usage.", WebOption.WebOptionConfigurationFieldType.DOUBLE);
        WebOption gasCurrent = new WebOption("GASCURRENT", "Current total gas usage", "Fill in your current total gas usage.", WebOption.WebOptionConfigurationFieldType.DOUBLE);
        
        optionSetCurrent.addOption(powerCurrent);
        optionSetCurrent.addOption(waterCurrent);
        optionSetCurrent.addOption(gasCurrent);
        conf.addOptionSet(optionSetCurrent);
        */
        
        setConfiguration(conf);
        
    }
    
    /**
     * Sets configuration values.
     * @param configuration
     * @throws WebConfigurationException 
     */
    @Override
    public final void setConfigurationValues(Map<String, String> configuration) throws WebConfigurationException {
        this.configuration = configuration;
        LOG.debug("Having configuration values: {}", configuration);
        
        if(configuration.get("POWERSINGLENAME")!=null && !configuration.get("POWERSINGLENAME").equals("")){
            powerSingleUnitName = configuration.get("POWERSINGLENAME");
        }
        if(configuration.get("POWERNAME")!=null && !configuration.get("POWERNAME").equals("")){
            powerUnitName = configuration.get("POWERNAME");
        }
        if(configuration.get("WATERNAME")!=null && !configuration.get("WATERNAME").equals("")){
            waterUnitName = configuration.get("WATERNAME");
        }
        if(configuration.get("GASNAME")!=null && !configuration.get("GASNAME").equals("")){
            gasUnitName = configuration.get("GASNAME");
        }
        if(configuration.get("POWERUSED")!=null && !configuration.get("POWERUSED").equals("")){
            powerThresholdAnn = Double.valueOf(configuration.get("POWERUSED"));
        }
        if(configuration.get("WATERUSED")!=null && !configuration.get("WATERUSED").equals("")){
            waterThresholdAnn = Double.valueOf(configuration.get("WATERUSED"));
        }
        if(configuration.get("GASUSED")!=null && !configuration.get("GASUSED").equals("")){
            gasThresholdAnn = Double.valueOf(configuration.get("GASUSED"));
        }
        
        ArrayList<RoundRobinDataGraphItem> dataTypes = new ArrayList();
        dataTypes.add(new RoundRobinDataGraphItem("UTILITY", "WATT", RoundRobinDataGraphItem.FieldType.AVERAGE));
        
        Measurement measurePower = null;
        Measurement measureGas   = Measurement.GAS_DEFAULT_SERIES;
        Measurement measureWater = Measurement.WATER_DEFAULT_SERIES;
        
        if(configuration.get("POWERABSOLUTETYPE")!=null && !configuration.get("POWERABSOLUTETYPE").equals("ABSOLUTE_NONE")){
            configuration.put("POWERUNITS", "0");
            switch(configuration.get("POWERABSOLUTETYPE")){
                case "ABSOLUTE_WATT":
                    measurePower = Measurement.POWER_ABSOLUTE;
                    LOG.info("Using absolute watt values, current usage");
                    dataTypes.add(new RoundRobinDataGraphItem("UTILITY", "KWH", RoundRobinDataGraphItem.FieldType.SUM));
                break;
                case "ABSOLUTE_KWATTH":
                    measurePower = Measurement.POWER_ABSOLUTE_KWH;
                    LOG.info("Using absolute KW/h values, current usage");
                    dataTypes.add(new RoundRobinDataGraphItem("UTILITY", "KWH", RoundRobinDataGraphItem.FieldType.SUM));
                break;
                case "ABSOLUTE_KWATTHTOTAL":
                    measurePower = Measurement.POWER_ABSOLUTE_KWHTOT;
                    LOG.info("Using absolute KW/h values, total usage");
                    dataTypes.add(new RoundRobinDataGraphItem("UTILITY", "KWH", RoundRobinDataGraphItem.FieldType.ABSOLUTE));
                break;
            }
        } else {
            dataTypes.add(new RoundRobinDataGraphItem("UTILITY", "KWH", RoundRobinDataGraphItem.FieldType.SUM));
        }
        if(configuration.get("GASABSOLUTETYPE")!=null && !configuration.get("GASABSOLUTETYPE").equals("ABSOLUTE_NONE")){
            configuration.put("GASUNITS", "0");
            switch(configuration.get("GASABSOLUTETYPE")){
                case "ABSOLUTE_GAS":
                    measureGas = Measurement.GAS_ABSOLUTE;
                    LOG.info("Using absolute gas values, current usage");
                    dataTypes.add(new RoundRobinDataGraphItem("UTILITY", "GAS", RoundRobinDataGraphItem.FieldType.SUM));
                break;
                case "ABSOLUTE_GASTOTAL":
                    measureGas = Measurement.GAS_ABSOLUTE_TOTAL;
                    LOG.info("Using absolute gas values, total usage");
                    dataTypes.add(new RoundRobinDataGraphItem("UTILITY", "GAS", RoundRobinDataGraphItem.FieldType.ABSOLUTE));
                break;
            }
        } else {
            dataTypes.add(new RoundRobinDataGraphItem("UTILITY", "GAS", RoundRobinDataGraphItem.FieldType.SUM));
        }
        if(configuration.get("WATERABSOLUTETYPE")!=null && !configuration.get("WATERABSOLUTETYPE").equals("ABSOLUTE_NONE")){
            configuration.put("WATERUNITS", "0");
            switch(configuration.get("WATERABSOLUTETYPE")){
                case "ABSOLUTE_WATER":
                    measureWater = Measurement.WATER_ABSOLUTE;
                    LOG.info("Using absolute water values, current usage");
                    dataTypes.add(new RoundRobinDataGraphItem("UTILITY", "WATER", RoundRobinDataGraphItem.FieldType.SUM));
                break;
                case "ABSOLUTE_WATERTOTAL":
                    measureWater = Measurement.WATER_ABSOLUTE_TOTAL;
                    LOG.info("Using absolute water values, total usage");
                    dataTypes.add(new RoundRobinDataGraphItem("UTILITY", "WATER", RoundRobinDataGraphItem.FieldType.ABSOLUTE));
                break;
            }
        } else {
            dataTypes.add(new RoundRobinDataGraphItem("UTILITY", "WATER", RoundRobinDataGraphItem.FieldType.SUM));
        }
        if(configuration.get("POWERDEVICE")!=null && !configuration.get("POWERDEVICE").equals("")){
            String[] powerSplitted = configuration.get("POWERDEVICE").split(";");
            try {
                if(measurePower==null){
                    switch(configuration.get("POWERMEASUREMENT")){
                        case "POWER_TIMED_RC":
                            measurePower = Measurement.POWER_TIMED_RC;
                        break;
                        case "POWER_CONTINUOUS_RC":
                            measurePower = Measurement.POWER_CONTINUOUS_RC;
                        break;
                        case "POWER_TIMED_KH":
                            measurePower = Measurement.POWER_TIMED_KH;
                        break;
                        case "POWER_CONTINUOUS_KH":
                            measurePower = Measurement.POWER_CONTINUOUS_KH;
                        break;
                    }
                }
                setPowerDevice(Integer.valueOf(powerSplitted[0]), powerSplitted[1], powerSplitted[2], measurePower, Double.valueOf(configuration.get("POWERUNITS")));
            } catch (Exception ex){
                LOG.warn("No power device used due to setup: {}, config: {} - units: {}, measurement type: {}",ex.getMessage(), configuration.get("POWERDEVICE"),configuration.get("POWERUNITS"), configuration.get("POWERMEASUREMENT"));
            }
        }
        if(configuration.get("WATERDEVICE")!=null && !configuration.get("WATERDEVICE").equals("")){
            String[] waterSplitted = configuration.get("WATERDEVICE").split(";");
            try {
                setWaterDevice(Integer.valueOf(waterSplitted[0]), waterSplitted[1], waterSplitted[2], Double.valueOf(configuration.get("WATERUNITS")),measureWater);
            } catch (Exception ex){
                LOG.warn("No water device used due to setup: {}, config: {} - units: {}",ex.getMessage(), configuration.get("WATERDEVICE"),configuration.get("WATERUNITS"));
            }
        }
        if(configuration.get("GASDEVICE")!=null && !configuration.get("GASDEVICE").equals("")){
            String[] gasSplitted = configuration.get("GASDEVICE").split(";");
            try {
                setGasDevice(Integer.valueOf(gasSplitted[0]), gasSplitted[1], gasSplitted[2], Double.valueOf(configuration.get("GASUNITS")),measureGas);
            } catch (Exception ex){
                LOG.warn("No water device used due to setup: {}, config: {} - units: {}",ex.getMessage(), configuration.get("GASDEVICE"),configuration.get("GASUNITS"));
            }
        }
        this.registerGraphDataTypes(dataTypes);
        try {
            this.getPowerMeasurement("UTILITY").setTodayKwh(getTodayGraphTotal("UTILITY", "KWH"));
        } catch (UtilityDataException | NullPointerException ex) {
            /// No KWH
            LOG.warn("No KWH history registration: {}", ex.getMessage());
        }
        try {
            this.getWaterMeasurement("UTILITY").setTodayValue(getTodayGraphTotal("UTILITY", "WATER"));
        } catch (UtilityDataException | NullPointerException ex) {
            /// No Water
            LOG.warn("No Water history registration: {}", ex.getMessage());
        }
        try {
            this.getGasMeasurement("UTILITY").setTodayValue(getTodayGraphTotal("UTILITY", "GAS"));
        } catch (UtilityDataException | NullPointerException ex) {
            /// No Gas
            LOG.warn("No Gas history registration: {}", ex.getMessage());
        }
    }

    /**
     * Sets the power device used.
     * @param deviceId
     * @param deviceGroup
     * @param deviceSet
     * @param measurementType
     * @param units 
     */
    public final void setPowerDevice(int deviceId, String deviceGroup, String deviceSet, Measurement measurementType, double units) {
        DeviceHook.remove(this, deviceId, deviceSet);
        powerDeviceId = deviceId;
        powerDeviceValueSet = deviceSet;
        powerUnitValue = units;
        LOG.info("Creating power: unit name: {}, full name: {}, units: {}, type: {}, threshold: {}", powerUnitName, powerSingleUnitName, units, measurementType,powerThresholdAnn);
        UtilityDataPower power = new UtilityDataPower(powerSingleUnitName, powerUnitName, units, measurementType);
        power.getKwh().setAnnualThreshold(powerThresholdAnn);
        this.addPowerMeasurement("UTILITY", power);
        DeviceHook.addDevice(this, deviceId, deviceSet);
    }

    /**
     * Sets the water device used.
     * @param deviceId
     * @param deviceGroup
     * @param deviceSet
     * @param units 
     * @param measurementType 
     */
    public final void setWaterDevice(int deviceId, String deviceGroup, String deviceSet, double units, Measurement measurementType) {
        DeviceHook.remove(this, deviceId, deviceSet);
        waterDeviceId = deviceId;
        waterDeviceValueSet = deviceSet;
        LOG.info("Creating water: unit name: {}, units: {}, type: {}, threshold: {}", waterUnitName, units, measurementType,waterThresholdAnn);
        UtilityDataWater water = new UtilityDataWater(waterUnitName, units, measurementType);
        water.setAnnualThreshold(waterThresholdAnn);
        this.addWaterMeasurement("UTILITY", water);
        DeviceHook.addDevice(this, deviceId, deviceSet);
    }

    /**
     * Sets the gas device used.
     * @param deviceId
     * @param deviceGroup
     * @param deviceSet
     * @param units 
     * @param measurementType 
     */
    public final void setGasDevice(int deviceId, String deviceGroup, String deviceSet, double units, Measurement measurementType) {
        DeviceHook.remove(this, deviceId, deviceSet);
        gasDeviceId = deviceId;
        gasDeviceValueSet = deviceSet;
        LOG.info("Creating gas: unit name: {}, units: {}, type: {}, threshold: {}", gasUnitName, units, measurementType,gasThresholdAnn);
        UtilityDataGas gas = new UtilityDataGas(gasUnitName, units, measurementType);
        gas.setAnnualThreshold(gasThresholdAnn);
        this.addGasMeasurement("UTILITY", gas);
        DeviceHook.addDevice(this, deviceId, deviceSet);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void handleDeviceData(final Device device, final String group, final String control, final DeviceControl deviceControl, final Object deviceValue) {
        LOG.debug("Recieved data device: {}, command: {}, command value: {}", device.getName(), control, deviceValue);
        try {
            double value = 0;
            if(deviceValue instanceof Float){
                value = (float)deviceValue/1D;
            } else if(deviceValue instanceof Integer) {
                value = (int)deviceValue/1.0D;
            } else {
                value = (double)deviceValue;
            }
            if(device.getId()==powerDeviceId && powerDeviceValueSet.equals(control)){
                this.getPowerMeasurement("UTILITY").setCurrentValue(value);
                storeGraphData("UTILITY","KWH", this.getPowerMeasurement("UTILITY").getKwh().getCurrentValue());
                storeGraphData("UTILITY","WATT", this.getPowerMeasurement("UTILITY").getCurrentValue());
                broadcastResultValue(Type.POWER, "UTILITY");
            } else if (device.getId()==waterDeviceId && waterDeviceValueSet.equals(control)) {
                this.getWaterMeasurement("UTILITY").setCurrentValue(value);
                storeGraphData("UTILITY","WATER", this.getWaterMeasurement("UTILITY").getCurrentValue());
                broadcastResultValue(Type.WATER, "UTILITY");
            } else if (device.getId()==gasDeviceId && gasDeviceValueSet.equals(control)) {
                this.getGasMeasurement("UTILITY").setCurrentValue(value);
                storeGraphData("UTILITY","GAS", this.getGasMeasurement("UTILITY").getCurrentValue());
                broadcastResultValue(Type.GAS, "UTILITY");
            } else {
                LOG.debug("Did not handle data from device: {}, command: {}, command value: {}. Correct hook setup?", device.getName(), control, deviceValue);
            }
        } catch (UtilityDataException ex){
            LOG.error("Could not handle data for: {}, {}, {}: {}",device.getName(),control,deviceValue,ex.getMessage(), ex);
        }
    }
    
    /**
     * Prepares for deletion.
     */
    @Override
    public void prepareDelete() {
        /// Not used
    }

    /**
     * Starts the plugin.
     * @throws PluginException 
     */
    @Override
    public void startPlugin() throws PluginException {
        this.setRunning(true);
    }

    /**
     * Stops the plugin.
     * @throws PluginException 
     */
    @Override
    public void stopPlugin() throws PluginException {
        this.setRunning(false);
    }

    @Override
    public void prepareWebPresentation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasGraphData() {
        return true;
    }
    
}