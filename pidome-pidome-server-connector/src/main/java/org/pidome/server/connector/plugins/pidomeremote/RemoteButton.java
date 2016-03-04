/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.pidomeremote;

/**
 *
 * @author John
 */
public abstract class RemoteButton {
    
    String buttonType = "default";
    long buttonDelay = 500;
    int row = 0;
    int col = 0;
    
    PiDomeRemote plugin;
    
    String buttonAction = "";
    
    String buttonId;
    Boolean isUniversal = false;
    
    /**
     * Constructs a button.
     * @param id 
     */
    public RemoteButton(String id){
        this(id, false);
    }
    
    /**
     * Constructs a button.
     * @param id 
     * @param isUniversal 
     */
    public RemoteButton(String id, boolean isUniversal){
        this.buttonId = id;
        this.isUniversal = isUniversal;
    }
    
    /**
     * Returns the button id.
     * @return 
     */
    public final String getId(){
        return this.buttonId;
    }
    
    
    /**
     * Sets the plugin id for this button in case it is used in a universal remote. 
     * @param plugin
     */
    protected final void setRemotePlugin(PiDomeRemote plugin){
        this.plugin = plugin;
    }
    
    /**
     * Returns the plugin id this belongs to.
     * @return 
     */
    public final PiDomeRemote getRemotePlugin(){
        return this.plugin;
    }
    
    /**
     * Returns if an universal button or not.
     * @return 
     */
    public final boolean getIsUniversal(){
        return this.isUniversal;
    }
    
    /**
     * Sets a button type.
     * With the button type it can be determined if a button has a special purpose.
     * Also this gives the possibility to implementing clients to set a different visual.
     * @param type 
     */
    public final void setType(String type){
        buttonType = type;
    }
    
    /**
     * Returns the type.
     * @return 
     */
    public final String getType(){
        return this.buttonType;
    }
    
    /**
     * Sets a button delay AFTER a button has pressed.
     * This means that in the queue a wait is executed based on the delay set. The delay is activated AFTER that a button press has been send.
     * Because of a universal remote uses the created remotes it has it's own possible internal build in delay so 0.5 seconds can for example in 
     * real send become 0.6 seconds. Do not take this into account because it is depending on the device driver's queue implementation.
     * @param delayMilliSeconds 
     */
    public final void setButtonDelay(long delayMilliSeconds){
        buttonDelay = delayMilliSeconds;
    }
    
    /**
     * Returns the button delay.
     * @return 
     */
    public final long getButtonDelay(){
        return this.buttonDelay;
    }
    
    /**
     * Used to set a button in a grid.
     * @param row
     * @param col 
     */
    public final void setButtonLocation(int row, int col){
        this.row = row;
        this.col = col;
    }
    
    /**
     * Returns the button row set by setButtonLocation.
     * @return 
     */
    public final int getButtonRow(){
        return this.row;
    }

    /**
     * Returns the button column set by setButtonLocation.
     * @return 
     */
    public final int getButtonColumn(){
        return this.col;
    }
    
}
