/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.nativeZWaveDriver;

import org.pidome.driver.nativeZWaveDriver.zwave.ZWaveCommandClass;
import org.pidome.driver.nativeZWaveDriver.zwave.ZWaveCommandClass.ZWaveCommand;
import org.zwave4j.ValueGenre;
import org.zwave4j.ValueId;
import org.zwave4j.ValueType;

/**
 *
 * @author John
 */
public final class ZwaveOutgoingMessage {
    
    private boolean exit = false;

    private short deviceAddress = (short)0x00;
    private ValueGenre valueGenre = ValueGenre.USER;
    private ValueType valueType = ValueType.NOT_SUPPORTED;
    private ZWaveCommandClass.ZWaveCommand commandClass = ZWaveCommandClass.ZWaveCommand.COMMAND_CLASS_NO_OPERATION;
    
    private short instance = (short)0x01;
    private short index = (short)0x00;
    
    private Object payload;
    
    private final long homeId;
    
    public ZwaveOutgoingMessage(long homeId, String commandClass, String valueType){
        this.homeId = homeId;
        this.setCommandClassFromString(commandClass);
        this.setValueTypeFromString(valueType);
    }

    public final void setPayload(Object payload) throws UnsupportedOperationException {
        switch(this.valueType){
            case BYTE:
                this.payload = Short.valueOf(payload.toString());
            break;
            case STRING:
            case LIST:
                if(payload instanceof String){
                    this.payload = payload;
                } else {
                    this.payload = String.valueOf(payload);
                }
            break;
            case BOOL:
                String tmp = payload.toString();
                this.payload = tmp.equals("1") || tmp.toLowerCase().equals("true");
            break;
            case DECIMAL:
                this.payload = Float.parseFloat(payload.toString());
            break;
            case INT:
                this.payload = Integer.valueOf(payload.toString());
            break;
            case SHORT:
                this.payload = Short.parseShort(payload.toString());
            break;
            case BUTTON:
                this.payload = null;
            break;
            case RAW:
            case SCHEDULE: //// schulde is weekday,hour,minute,(second?,)point
                int length = ((Object[])payload).length;
                this.payload = new short[length];
                for(int i=0; i<length;i++){
                    ((short[])this.payload)[i] = Short.parseShort(((Object[])payload)[i].toString());
                }
            break;
        }
    }
    
    /**
     * Return the payload as object.
     * @return 
     */
    public final Object getObjectPayload(){
        return this.payload;
    }
    
    /**
     * Returns the payload as byte.
     * @return 
     */
    public final short getBytePayload(){
        return (short)payload;
    }
    
    /**
     * Returns the payload as string.
     * @return 
     */
    public final String getStringPayload(){
        return (String)payload;
    }
    
    /**
     * Returns the payload as boolean
     * @return 
     */
    public final boolean getBooleanPayload(){
        return (boolean)payload;
    }
    
    /**
     * Return the foat payload.
     * @return 
     */
    public final float getFloatPayload(){
        return (float)payload;
    }
    
    /**
     * Returns the interger payload.
     * @return 
     */
    public final int getIntegerPayload(){
        return (int)payload;
    }
    
    /**
     * Return the short payload.
     * @return 
     */
    public final short getShortPayload() {
        return (short)payload;
    }
    
    /**
     * Return the short payload as array.
     * @return 
     */
    public final short[] getShortArrayPayload(){
        return (short[])payload;
    }
    /**
     * Returns a byte value id.
     * @return 
     */
    public final ValueId getValueId(){
        return new ValueId(homeId, deviceAddress, valueGenre, commandClass.getAsShort(), instance, index, valueType);
    }
    
    /**
     * Sets the index number.
     * Number is converted to short. It defaults to 0x00
     * @param indexNumber 
     */
    public final void setIndexFromNumber(Number indexNumber){
        this.index = indexNumber.shortValue();
    }

    /**
     * Returns the index.
     * @return 
     */
    public final short getIndex(){
        return index;
    }
    
    /**
     * Sets the instance number.
     * Number is converted to short. it defaults to 0x01
     * @param instanceNumber 
     */
    public final void setInstanceFromNumber(Number instanceNumber){
        this.instance = instanceNumber.shortValue();
    }

    /**
     * Returns the instance.
     * @return 
     */
    public final short getInstance(){
        return instance;
    }
    
    
    /**
     * Sets the valuetype to be used.
     * @param valueType 
     */
    private void setValueTypeFromString(String valueType){
        for(ValueType val:ValueType.values()){
            if(val.toString().equals(valueType)){
                this.valueType = val;
            }
        }
    }
    
    /**
     * Returns the valueType to be used.
     * @return 
     */
    public final ValueType getValueType(){
        return this.valueType;
    }
    
    /**
     * Sets the device address from string.
     * @param address The address is an hex presented string.
     */
    public final void setDeviceAddressFromString(String address){
        deviceAddress =  Short.decode(address);
    }
    
    /**
     * Returns the device address.
     */
    public final short getDeviceAddress(){
        return this.deviceAddress;
    }
    
    /**
     * Sets the command class from a string.
     * Defaults to No operation.
     * @param cmdClass 
     */
    private void setCommandClassFromString(String cmdClass){
        for(ZWaveCommand cmd:ZWaveCommandClass.ZWaveCommand.values()){
            if(cmd.toString().equals(cmdClass)){
                commandClass = cmd;
            }
        }
    }

    /**
     * Returns the command class.
     * @return 
     */
    public final short getCommandClass(){
        return commandClass.getAsShort();
    }
    
    /**
     * Sets the value genre.
     * @param valueGenre 
     */
    public final void setValueGenreFromString(String valueGenre){
        for(ValueGenre val:ValueGenre.values()){
            if(val.toString().equals(valueGenre)){
                this.valueGenre = val;
            }
        }
    }
    
    /**
     * Returns the value genre.
     * @return 
     */
    public final ValueGenre getValueGenre(){
        return valueGenre;
    }
    
    ///////////// Leaving ZWave
    public final ZwaveOutgoingMessage done(){
        this.exit = true;
        return this;
    }
    
    public final boolean exit(){
        return this.exit;
    }
    
}