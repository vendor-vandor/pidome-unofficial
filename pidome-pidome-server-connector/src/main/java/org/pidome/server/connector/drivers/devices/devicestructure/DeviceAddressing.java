/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.drivers.devices.devicestructure;

import java.util.Map;

/**
 *
 * @author John
 */
public class DeviceAddressing {
    
    boolean hasAddress = false;
    DeviceControlDataType datatype = DeviceControlDataType.STRING;
    String addressDescription = "";
    
    String addressInputType   = "text";
    String inputDescription   = "";
    
    Object address = null;
    
    /**
     * Constructor.
     * @param addressData
     * @throws org.pidome.server.connector.drivers.devices.devicestructure.DeviceAddressException 
     */
    public DeviceAddressing(Map<String,Object> addressData) throws DeviceAddressException {
        if(addressData.containsKey("description") && addressData.containsKey("input")){
            try {
                addressDescription = (String)addressData.get("description");
                setAddressDetails((Map<String,Object>)addressData.get("input"));
                hasAddress = true;
            } catch (DeviceControlException ex) {
                throw new DeviceAddressException(ex);
            }
        }
    }
    
    /**
     * Sets the address.
     * This has the input from a string and will be converted if necessary. 
     * @param address 
     */
    public final void setAddress(String address){
        if(this.address==null){
            switch(datatype){
                case INTEGER:
                    this.address = Integer.parseInt(address);
                break;
                case FLOAT:
                    this.address = Float.parseFloat(address);
                break;
                case BOOLEAN:
                    this.address = Boolean.parseBoolean(address);
                break;
                default:
                    this.address = address;
                break;
            }
        }
    }
    
    /**
     * Returns the device address.
     */
    public final Object getAddress(){
        return address;
    }
    
    /**
     * Returns the main address description.
     * @return 
     */
    public final String getLargeAddressDescription(){
        return this.addressDescription;
    }
    
    /**
     * Returns the address input field type.
     * @return 
     */
    public final String getInputType(){
        return this.addressInputType;
    }
    
    /**
     * Returns the input label description.
     * @return 
     */
    public final String getAddressInputDescription(){
        return this.inputDescription;
    }
    
    /**
     * Sets the details about the address.
     * @param data
     * @throws DeviceControlException 
     */
    private void setAddressDetails(Map<String,Object> data) throws DeviceControlException {
        createDataType((String)data.get("datatype"));
        createAddressInputType((String)data.get("datatype"));
        createAddressInputDescription((String)data.get("description"));
    }
    
    /**
     * Creates the input's field label text.
     * @param desc
     * @throws DeviceControlException 
     */
    private void createAddressInputDescription(String desc) throws DeviceControlException {
        if(desc.length()==0) throw new DeviceControlException("Device input description empty. If no address is used please mention it");
        inputDescription = desc;
    }
    
    /**
     * Creates the input type for the address selection.
     * @param addressType 
     */
    private void createAddressInputType(String addressType){
        ///addressInputType = addressType;
    }
    
    /**
     * Creates the datatype.
     * @param type
     * @throws DeviceControlException 
     */
    private void createDataType(String type) throws DeviceControlException {
        if(type==null || type.isEmpty()) throw new DeviceControlException("Need to supply a correct datatype");
        switch(type){
            case "string":
                datatype = DeviceControlDataType.STRING;
            break;
            case "integer":
                datatype = DeviceControlDataType.INTEGER;
            break;
            case "float":
                datatype = DeviceControlDataType.FLOAT;
            break;
            case "boolean":
                datatype = DeviceControlDataType.BOOLEAN;
            break;
            case "hex":
                datatype = DeviceControlDataType.HEX;
            break;
            default:
                throw new DeviceControlException("Datatype " + type + " is unsupported");
        }
    }
    
    /**
     * Returns true if an device has an address.
     * @return 
     */
    public boolean hasAddress(){
        return hasAddress;
    }
    
}
