/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.entities.devices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A color picker control.
 * @author John
 */
public class DeviceColorPickerControl extends DeviceControl {

    static {
        Logger.getLogger(DeviceColorPickerControl.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Color picker modes.
     * These are used to visualize a specific color picker type.
     */
    public enum Mode {
        /**
         * RGB color mode.
         */
        RGB,
        /**
         * HSB Color mode.
         */
        HSB,
        /**
         * CIE color mode.
         */
        CIE,
        /**
         * Kelvin color mode.
         */
        KELVIN,
        /**
         * White levels color mode.
         */
        WHITES
    }
    
    /**
     * List of command buttons for the color picker.
     */
    List<CommandButton> buttonsList = new ArrayList<>();
    /**
     * Current mode.
     */
    Mode pickerMode = Mode.HSB;
    
    /**
     * Constructor.
     * @param controlId The control id.
     * @throws DeviceControlException When the control can not be constructed.
     */
    public DeviceColorPickerControl(String controlId) throws DeviceControlException {
        super(DeviceControlType.COLORPICKER, controlId);
    }
 
    
    /**
     * Creates the color picker control.
     * @param data basic control data.
     * @throws DeviceControlException When data is incomplete.
     */
    protected final void setColorPickerControlData(Map<String,Object> data) throws DeviceControlException {
        setInitialDataStructure((Map<String,Object>)data);
        if(!data.containsKey("mode")){
            throw new DeviceControlException("Attribute mode is required in color picker.");
        } else {
            switch((String)data.get("mode")){
                case "rgb":
                    pickerMode = Mode.RGB;
                break;
                case "hsb":
                    pickerMode = Mode.HSB;
                break;
                case "cie":
                    pickerMode = Mode.CIE;
                break;
                case "kelvin":
                    pickerMode = Mode.KELVIN;
                break;
                case "whites":
                    pickerMode = Mode.WHITES;
                break;
                default:
                    throw new DeviceControlException("Unsupported color mode: " + data.get("mode"));
            }
        }
        Map<String,Double> initialHsb = new HashMap<>();
        
        initialHsb.put("h", 0.0d);
        initialHsb.put("s", 0.0d);
        initialHsb.put("b", 0.0d);
        
        Map<String,Integer> initialRGB = new HashMap<>();
        initialRGB.put("r", 0);
        initialRGB.put("g", 0);
        initialRGB.put("b", 0);
        
        String initialHex = "#000000";

        Long initialKelvin = 0L;
        
        Map<String,Object> initialValues = new HashMap<>();
        initialValues.put("hsb", initialHsb);
        initialValues.put("rgb", initialRGB);
        initialValues.put("hex", initialHex);
        initialValues.put("kelvin", initialKelvin);
        
        setLastKnownValue(initialValues);
        for(Map<String,String>buttonData:(List<Map<String,String>>)data.get("commandset")){
            buttonsList.add(new CommandButton(buttonData));
        }
    }
 
    /**
     * Returns the full buttons list.
     * @return A list of possible button actions bound to a color action.
     */
    public final List<CommandButton> getFullButtonsList(){
        return buttonsList;
    }
    

    
    /**
     * Returns the color visible mode.
     * Multiple modes will be supported in the future like hue, daylight etc...
     * @return The visual mode of the color picker.
     */
    public final Mode getMode(){
        return pickerMode;
    }
    
    /**
     * Returns the full color map.
     * @return All the possible color types in a map (rgb,hsb,hex,kelvin)
     */
    public final Map<String,Map<String,Object>> getFullColorMap(){
        return (Map<String,Map<String,Object>>)super.getValue();
    }
    
    /**
     * Returns the mapped real values.
     * This function returns a multidimensional hashmap containing.
     * @return See getFullColorMap();
     */
    @Override
    public final Object getValueData(){
        return this.getValue();
    }
    
    /**
     * Sets the last known value.
     * @param value The last known color values.
     */
    @Override
    public void setLastKnownValue(Object value){
        setLastKnownValueKnownDatatype(value);
    }
    
    /**
     * Return the RGB values
     * This function returns a map with the keys r,g and b
     * @return return Map with r,g,b int values.
     */
    public final Map<String,Integer> getRGB(){
        return ((Map<String,Map<String,Integer>>)super.getValue()).get("rgb");
    }
    
    /**
     * Return the RGB values
     * This function returns a map with the keys h,s and b
     * @return Map with h,s,b values.
     */
    public final Map<String,Double> getHSB(){
        return ((Map<String,Map<String,Double>>)super.getValue()).get("hsb");
    }
    
    /**
     * Returns current kelvin value.
     * Kelvin value is only available when kelvin is used to set the data. Otherwise always returns 0.
     * @return long kelvin number.
     */
    public final Long getKelvin(){
        return ((Map<String,Long>)super.getValue()).get("kelvin");
    }
    
    /**
     * Returns current hex value.
     * Returns the hex values as #000000
     * @return Heax as string.
     */
    public final String getHex(){
        return ((Map<String,String>)super.getValue()).get("hex");
    }
 
    /**
     * Sends a color picker command.
     * @param command command of type ColorPickerCommand.
     * @return Complete structure to be used with the JSONConnector.
     * @throws DeviceControlCommandException When the type is not of type ColorPickerCommand
     */
    public DeviceCommandStructure createSendCommand(ColorPickerCommand command) throws DeviceControlCommandException {
        Map<String,Object> parameters = new HashMap<>();
        switch(command.getColor().getType()){
            case "rgb":
                parameters.put("r", command.getColor().getRGB()[0]);
                parameters.put("g", command.getColor().getRGB()[1]);
                parameters.put("b", command.getColor().getRGB()[2]);
            break;
            case "hsb":
                parameters.put("h", command.getColor().getHSB()[0]);
                parameters.put("s", command.getColor().getHSB()[1]);
                parameters.put("b", command.getColor().getHSB()[2]);
            break;
            case "hex":
                parameters.put("hex", command.getColor().getHex());
            break;
            case "kelvin":
                parameters.put("kelvin", command.getColor().getKelvin());
            break;
        }
        return new DeviceCommandStructure(this, parameters, command.getButton().getValue());
    }
    
    /**
     * Command to use when updating a device's color picker.
     */
    public static class ColorPickerCommand implements DeviceControlCommand {
        
        /**
         * The button used.
         */
        CommandButton button;
        /**
         * The color to be set.
         */
        ColorCommand color;
        
        /**
         * Constructs the color picker command.
         * @param button The button pressed.
         * @param color The color to be used.
         */
        public ColorPickerCommand(CommandButton button, ColorCommand color){
            this.button = button;
            this.color  = color;
        }
        
        /**
         * Returns the button set.
         * @return The button set.
         */
        private CommandButton getButton(){
            return this.button;
        }
        
        /**
         * Returns the color set.
         * @return The color set.
         */
        private ColorCommand getColor(){
            return this.color;
        }
        
    }
    
    /**
     * Constructs a color set.
     */
    public static class ColorCommand {
        
        /**
         * RGB red.
         */
        int rgbR; 
        /**
         * RGB green.
         */
        int rgbG; 
        /**
         * RGB Blue.
         */
        int rgbB;
        
        /**
         * HSB Hue.
         */
        double hsbH; 
        /**
         * HSB saturation.
         */
        double hsbS; 
        /**
         * HSB brightness.
         */
        double hsbB;
        
        /**
         * Hex in string notation including #.
         */
        String hex;
        
        /**
         * Kelvin value in long.
         */
        Long kelvin;
        
        /**
         * Identifies the color type constructed.
         */
        String colorType = "hsb";
        
        /**
         * Construct an RGB color.
         * @param rgbR Red
         * @param rgbG Green
         * @param rgbB Blue
         */
        public ColorCommand(int rgbR, int rgbG, int rgbB){
            this.rgbR = rgbR;
            this.rgbG = rgbG;
            this.rgbB = rgbB;
            colorType = "rgb";
        }

        /**
         * Construct an HSB color.
         * @param hsbH Hue
         * @param hsbS Saturation
         * @param hsbB Value
         */
        public ColorCommand(double hsbH, double hsbS, double hsbB){
            this.hsbH = hsbH;
            this.hsbS = hsbS;
            this.hsbB = hsbB;
            colorType = "hsb";
        }
        
        /**
         * Construct an HEX color.
         * @param hex String hex representation including the # - sign.
         */
        public ColorCommand(String hex){
            this.hex = hex;
            colorType = "hex";
        }
        
        /**
         * Returns the color type.
         * @return The type constructed.
         */
        private final String getType(){
            return this.colorType;
        }
        
        /**
         * Construct an kelvin color.
         * @param kelvin long kelvin number.
         */
        public ColorCommand(Long kelvin){
            this.kelvin = kelvin;
            colorType = "kelvin";
        }
        
        /**
         * Returns an RGB array.
         * @return Array containing r[0], g[1], b[2].
         */
        private int[] getRGB(){
            int[] set = new int[3];
            set[0] = this.rgbR;
            set[1] = this.rgbG;
            set[2] = this.rgbB;
            return set;
        }
        
        /**
         * Returns an HSB array.
         * @return Array containing h[0], s[1], b[2]
         */
        private double[] getHSB(){
            double[] set = new double[3];
            set[0] = this.hsbH;
            set[1] = this.hsbS;
            set[2] = this.hsbB;
            return set;
        }
        
        /**
         * Returns an hex string.
         * @return hex as string including # sign.
         */
        private String getHex(){
            return this.hex;
        }
        
        /**
         * Returns a kelvin long color.
         * @return the kelvin long.
         */
        private long getKelvin(){
            return this.kelvin;
        }
        
    }
    
    /**
     * The command button for a ColorCommand.
     */
    public static class CommandButton {
        /**
         * The command button label.
         */
        private final String label;
        /**
         * The command button value.
         */
        private final String value;
        
        /**
         * Construct a button.
         * @param data The button data.
         */
        private CommandButton(Map<String,String> data){
            label = data.get("label");
            value = data.get("value");
        }
        
        /**
         * Returns a label.
         * @return the button label.
         */
        public final String getLabel(){
            return this.label;
        }
        
        /**
         * Returns the command value.
         * @return the button value.
         */
        public final String getValue(){
            return this.value;
        }
        
    }
    
}