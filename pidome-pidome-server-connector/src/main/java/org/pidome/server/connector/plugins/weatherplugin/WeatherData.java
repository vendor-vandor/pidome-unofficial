/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.weatherplugin;

import java.util.Date;

/**
 *
 * @author John
 */
public class WeatherData {
    
    /**
     * Icon used to display the data.
     */
    public enum Icon {
        /**
         * Not available.
         */
        NOT_AVAILABLE(-1, "NOT_AVAILABLE"),
        
        /**
         * Daily clear.
         */
        CLEAR(1, "CLEAR"),
        /**
         * A little bit cloudy.
         */
        MOSTLY_CLEAR(2, "MOSTLY_CLEAR"),
        /**
         * Partly cloudy.
         */
        PARTLY_CLEAR(3, "PARTLY_CLEAR"),
        /**
         * Broken clouds day.
         */
        INTERMITTENT_CLOUDS(4, "INTERMITTENT_CLOUDS"),
        /**
         * Haze day.
         */
        HAZE(5, "HAZE"),
        /**
         * Most cloudy.
         */
        MOSTLY_CLOUDY(6, "MOSTLY_CLOUDY"),
        /**
         * Cloudy.
         */
        CLOUDY(7, "CLOUDY"),
        /**
         * Dreary.
         */
        DREARY(8, "DREARY"),
        /**
         * Fog.
         */
        FOG(11, "FOG"),
        /**
         * Showers.
         */
        SHOWERS(12, "SHOWERS"),
        /**
         * Mostly cloudy with showers.
         */
        MOSTLY_CLOUDY_SHOWERS(13, "MOSTLY_CLOUDY_SHOWERS"),
        /**
         * Partly clear with showers.
         */
        PARTLY_CLEAR_SHOWERS(14, "PARTLY_CLEAR_SHOWERS"),
        /**
         * Bang bang bang.
         */
        THUNDERSTORMS(15, "THUNDERSTORMS"),
        /**
         * Mostly cloudy with thunderstorms.
         */
        MOSTLY_CLOUDY_THUNDERSTORMS(16, "MOSTLY_CLOUDY_THUNDERSTORMS"),
        /**
         * Partly clear with thunderstorms.
         */
        PARTLY_CLEAR_THUNDERSTORMS(17, "PARTLY_CLEAR_THUNDERSTORMS"),
        /**
         * Where we sing in.
         */
        RAIN(18, "RAIN"),
        /**
         * Flurry snow.
         */
        FLURRIES(19, "FLURRIES"),
        /**
         * Mostly cloudy with flurries.
         */
        MOSTLY_CLOUDY_FLURRIES(20, "MOSTLY_CLOUDY_FLURRIES"),
        /**
         * Quite clear with flurries.
         */
        PARTLY_CLEAR_FLURRIES(21, "PARTLY_CLEAR_FLURRIES"),
        /**
         * Fun!.
         */
        SNOW(22, "SNOW"),
        /**
         * Mostly cloudy and snow.
         */
        MOSTLY_CLOUDY_SNOW(23, "MOSTLY_CLOUDY_SNOW"),
        /**
         * Ice.
         */
        ICE(24, "ICE"),
        /**
         * Sleet.
         */
        SLEET(25, "SLEET"),
        /**
         * Freezing rain.
         */
        FREEZING_RAIN(26, "FREEZING_RAIN"),
        /**
         * Rain and snow.
         */
        RAIN_AND_SNOW(29, "RAIN_AND_SNOW"),
        /**
         * Very hot.
         */
        HOT(30, "EXTREME_HOT"),
        /**
         * Very cold.
         */
        COLD(31,"EXTREME_COLD"),
        /**
         * A lot of wind.
         */
        WINDY(32, "WINDY"),
        
        
        /**
         * Clear night.
         */
        CLEAR_NIGHT(33, "CLEAR"),
        /**
         * Mostly clear at night
         */
        MOSTLY_CLEAR_NIGHT(34, "MOSTLY_CLEAR"),
        /**
         * Less clear at night.
         */
        PARTLY_CLEAR_NIGHT(35, "PARTLY_CLEAR"),
        /**
         * A lot of clouds in the night, probably only clouds.
         */
        INTERMITTENT_CLOUDS_NIGHT(36, "INTERMITTENT_CLOUDS"),
        /**
         * Haze night.
         */
        HAZE_NIGHT(37, "HAZE"),
        /**
         * More clouds then night.
         */
        MOSTLY_CLOUDY_NIGHT(38, "MOSTLY_CLOUDY"),
        /**
         * Partly clear with rain.
         */
        PARTLY_CLEAR_SHOWERS_NIGHT(39, "PARTLY_CLEAR_SHOWERS"),
        /**
         * More cloudy at night with rain.
         */
        MOSTLY_CLOUDY_SHOWERS_NIGHT(40, "MOSTLY_CLOUDY_SHOWERS"),
        /**
         * Bang bang in the night which is mostly clouded.
         */
        MOSTLY_CLOUDY_THUNDERSTORMS_NIGHT(41, "MOSTLY_CLOUDY_THUNDERSTORMS"),
        /**
         * Partly clear with thunderstorms at night.
         */
        PARTLY_CLEAR_THUNDERSTORMS_NIGHT(42, "PARTLY_CLEAR_THUNDERSTORMS"),
        /**
         * Mostly cloudy with some flurry snow.
         */
        MOSTLY_CLOUDY_FLURRIES_NIGHT(43, "MOSTLY_CLOUDY_FLURRIES"),
        /**
         * Snow at night.
         */
        MOSTLY_CLOUDY_SNOW_NIGHT(44, "MOSTLY_CLOUDY_SNOW"),
        
        
        //// Specials
        /**
         * Smog/Sand like
         */
        SMOG(45, "SMOG"),SAND(45, "SAND"),
        
        /**
         * Tornado
         */
        TORNADO(46, "TORNADO"),
        
        /**
         * Hail.
         */
        HAIL(47, "HAIL");
        
        /**
         * Icon number;
         */
        private final int value;

        private final String baseValue;
        
        /**
         * Set icon number.
         * @param value 
         */
        private Icon(int value, String baseValue) {
            this.value = value;
            this.baseValue = baseValue;
        }
        
        /**
         * Returns the icon number.
         * @return 
         */
        public int getValue(){
            return this.value;
        }
        
        public String getBaseValue(){
            return this.baseValue;
        }
        
    }
    
    /**
     * State icon.
     */
    Icon icon = Icon.NOT_AVAILABLE;
    
    /**
     * State;
     */
    String state = "N/A";
    
    /**
     * Temperature.
     */
    float temp = 0.0f;
    /**
     * Humidity
     */
    int hum = 0;
    /**
     * Wind speed.
     */
    float windSpeed = 0;
    /**
     * Wind gusts speed.
     */
    float gustsSpeed = 0.0f;
    /**
     * Wind direction.
     */
    String windDirection = "N/A";
    /**
     * Wind direction degrees
     */
    float windDirectionDegrees = 0.0f;
    /**
     * Pressure
     */
    float pres = 0.0f;
    /**
     * Description of the data.
     */
    String description = "";
    /**
     * Date of data fetched.
     */
    Date date;
    /**
     * Date of the data as reported by the remote resource.
     */
    int weatherDate = 0;
    
    /**
     * A StringBuilder of extremes data which can be appended to.
     */
    StringBuilder extremes;
    
    /**
     * Sets the date.
     * @param date 
     */
    public final void setDate(Date date){
        this.date = date;
    }
    
    /**
     * Returns the date.
     * @return 
     */
    public final Date getDate(){
        return this.date;
    }
    
    /**
     * Sets the remote resource's update date.
     * @param date 
     */
    public final void setWeatherDate(int date){
        this.weatherDate = date;
    }
    
    /**
     * Return the server's update date.
     * @return 
     */
    public final int getWeatherDate(){
        return weatherDate;
    }
    
    /**
     * Set the current weather icon.
     * @param icon 
     */
    public final void setStateIcon(Icon icon){
        this.icon = icon;
    }
    
    /**
     * Returns the icon.
     * @return 
     */
    public final Icon getStateIcon(){
        return this.icon;
    }
    
    /**
     * Set the current weather state.
     * @param state 
     */
    public final void setStateName(String state){
        this.state = state;
    }
    
    /**
     * Returns state name.
     * @return 
     */
    public final String getStateName(){
        return this.state;
    }
    
    /**
     * Set the current weather state.
     * @param state 
     */
    public final void setStateDescription(String state){
        this.description = state;
    }
    
    /**
     * Returns state name.
     * @return 
     */
    public final String getStateDescription(){
        return this.description;
    }
    
    /**
     * Sets the current temperature.
     * @param temp 
     */
    public final void setTemperature(float temp){
        this.temp = temp;
    }
    
    /**
     * Returns the temperature.
     * @return 
     */
    public final float getTemperature(){
        return this.temp;
    }

    /**
     * Sets the current humidity.
     * @param hum 
     */
    public final void setHumidity(int hum){
        this.hum = hum;
    }
    
    /**
     * Returns humidity.
     * @return 
     */
    public final int getHumidity(){
        return this.hum;
    }
    
    /**
     * Sets the current humidity.
     * @param pres 
     */
    public final void setPressure(float pres){
        this.pres = pres;
    }
    
    /**
     * Returns pressure.
     * @return 
     */
    public final float getPressure(){
        return this.pres;
    }
    
    /**
     * Sets the wind speed.
     * @param speed 
     */
    public final void setWindSpeed(float speed){
        this.windSpeed = speed;
    }
    
    /**
     * Returns wind speed.
     * @return 
     */
    public final float getWindSpeed(){
        return this.windSpeed;
    }
    
    /**
     * Sets the wind gusts speed.
     * @param gustsSpeed 
     */
    public final void setWindGusts(float gustsSpeed){
        this.gustsSpeed = gustsSpeed;
    }
    
    /**
     * Returns the wind gusts.
     * @return 
     */
    public final float getWindGusts(){
        return this.gustsSpeed;
    }
    
    /**
     * Sets the wind direction.
     * @param direction 
     */
    public final void setWindDirection(String direction){
        this.windDirection = direction;
    }
    
    /**
     * Returns the wind direction.
     * @return 
     */
    public final String getWindDirection(){
        return this.windDirection;
    }
    
    /**
     * Sets the wind direction in degrees. 
     * @param windDirectionDegrees
     */
    public final void setWindDirectionDegrees(float windDirectionDegrees){
        this.windDirectionDegrees = windDirectionDegrees;
    }
    
    /**
     * Returns the wind direction in degrees.
     * @return 
     */
    public final float getWindDirectionDegrees(){
        return this.windDirectionDegrees;
    }
    
    /**
     * add an extreme to the extremes.
     * @param desc 
     */
    public final void addExtremesDescription(String desc){
        if(extremes == null){
            extremes = new StringBuilder();
        }
        extremes.append(desc).append(", ");
    }
    
    /**
     * Return all extremes.
     * @return 
     */
    public final String getExtremesDescription(){
        if(extremes!=null){
            return extremes.substring(0, extremes.length()-2);
        } else {
            return "";
        }
    }
    
    /**
     * Creates a wind direction based on the given degrees.
     * It uses 16 part wind rose, 360 and 0 are north.
     * @param direction
     * @return 
     */
    public static String getWindDirection(float direction){
        if(direction < 11){
            return "N";
        } else if(direction < 33){
            return "NNE";
        } else if(direction < 56){
            return "NE";
        } else if(direction < 78){
            return "ENE";
        } else if(direction < 101){
            return "E";
        } else if(direction < 123){
            return "ESE";
        } else if(direction < 146){
            return "SE";
        } else if(direction < 168){
            return "SSE";
        } else if(direction < 191){
            return "S";
        } else if(direction < 213){
            return "SSW";
        } else if(direction < 236){
            return "SW";
        } else if(direction < 258){
            return "WSW";
        } else if(direction < 281){
            return "W";
        } else if(direction < 303){
            return "WNW";
        } else if(direction < 326){
            return "NW";
        } else if(direction < 348){
            return "NNW";
        } else {
            return "N";
        }
    }
    
}