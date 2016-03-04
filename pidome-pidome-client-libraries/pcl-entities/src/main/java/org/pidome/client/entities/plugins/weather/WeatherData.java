/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.plugins.weather;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class holding weather data.
 * Used for current and upcoming weather.
 * @author John
 */
public final class WeatherData {
    
    static {
        Logger.getLogger(WeatherData.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Icon used to display the data.
     */
    public enum WeatherIcon {
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
        CLEAR_NIGHT(33, "CLEAR_NIGHT"),
        /**
         * Mostly clear at night
         */
        MOSTLY_CLEAR_NIGHT(34, "MOSTLY_CLEAR_NIGHT"),
        /**
         * Less clear at night.
         */
        PARTLY_CLEAR_NIGHT(35, "PARTLY_CLEAR_NIGHT"),
        /**
         * A lot of clouds in the night, probably only clouds.
         */
        INTERMITTENT_CLOUDS_NIGHT(36, "INTERMITTENT_CLOUDS_NIGHT"),
        /**
         * Haze night.
         */
        HAZE_NIGHT(37, "HAZE_NIGHT"),
        /**
         * More clouds then night.
         */
        MOSTLY_CLOUDY_NIGHT(38, "MOSTLY_CLOUDY_NIGHT"),
        /**
         * Partly clear with rain.
         */
        PARTLY_CLEAR_SHOWERS_NIGHT(39, "PARTLY_CLEAR_SHOWERS_NIGHT"),
        /**
         * More cloudy at night with rain.
         */
        MOSTLY_CLOUDY_SHOWERS_NIGHT(40, "MOSTLY_CLOUDY_SHOWERS_NIGHT"),
        /**
         * Bang bang in the night which is mostly clouded.
         */
        MOSTLY_CLOUDY_THUNDERSTORMS_NIGHT(41, "MOSTLY_CLOUDY_THUNDERSTORMS_NIGHT"),
        /**
         * Partly clear with thunderstorms at night.
         */
        PARTLY_CLEAR_THUNDERSTORMS_NIGHT(42, "PARTLY_CLEAR_THUNDERSTORMS_NIGHT"),
        /**
         * Mostly cloudy with some flurry snow.
         */
        MOSTLY_CLOUDY_FLURRIES_NIGHT(43, "MOSTLY_CLOUDY_FLURRIES_NIGHT"),
        /**
         * Snow at night.
         */
        MOSTLY_CLOUDY_SNOW_NIGHT(44, "MOSTLY_CLOUDY_SNOW_NIGHT"),
        
        
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
        private WeatherIcon(int value, String baseValue) {
            this.value = value;
            this.baseValue = baseValue;
        }
        
        /**
         * Returns the icon number.
         * @return The internal icon id.
         */
        public int getValue(){
            return this.value;
        }
        
        public String getBaseValue(){
            return this.baseValue;
        }
        
    }
    
    /**
     * Temperature.
     */
    float temp;
    /**
     * Humidity.
     */
    float humid;
    /**
     * Wind speed in m/s
     */
    float windspeed;
    /**
     * Icon constant.
     */
    WeatherIcon icon = WeatherIcon.NOT_AVAILABLE;
    /**
     * Icon image.
     */
    String iconImage;
    /**
     * Weather description.
     */
    String text;
    /**
     * Pressure.
     */
    float pressure;
    /**
     * Wind direction name.
     */
    String windDirectionName;
    /**
     * When there are extremes non empty.
     */
    String extremesText;
    /**
     * Date of the weather data.
     */
    Date weatherDate;
    /**
     * Wind direction in degrees.
     */
    float windDirectionDegrees;
    
    /**
     * Constructor.
     */
    protected WeatherData(){}
    
    /**
     * Set the temperature.
     * @param temp temperature.
     */
    protected void setTemperature(float temp){
        this.temp = temp;
    }
    /**
     * Sets the humidity.
     * @param humid Humidity
     */
    protected void setHumidity(float humid){
        this.humid = humid;
    }
    
    /**
     * Sets the wind speed
     * @param windspeed Speed in m/s
     */
    protected void setWindSpeed(float windspeed){
        this.windspeed = windspeed;
    }
    
    /**
     * Sets the icon constant.
     * @param icon Icon constant.
     */
    protected void setIcon(String icon){
        for(WeatherIcon weatherIcon:WeatherIcon.values()){
            if(weatherIcon.getBaseValue().equals(icon)){
                this.icon = weatherIcon;
                break;
            }
        }
    }
    
    /**
     * Sets the image.
     * @param iconImage image name.
     */
    protected void setIconImage(String iconImage){
        this.iconImage = iconImage;
    }
    
    /**
     * Sets the weather description.
     * @param text description.
     */
    protected void setDescription(String text){
        this.text = text;
    }
    
    /**
     * Sets the pressure.
     * @param pressure Pressure in hpa.
     */
    protected void setPressure(float pressure){
        this.pressure = pressure;
    }
    
    /**
     * Sets the wind direction name.
     * @param directionName String direction (like N,NNW,S etc..)
     */
    protected void setWindDirection(String directionName){
        this.windDirectionName = directionName;
    }
    
    /**
     * Sets the degrees the wind goes.
     * @param directionDegrees amount in degrees.
     */
    protected void setWindDirectionDegrees(float directionDegrees){
        this.windDirectionDegrees = directionDegrees;
    }
    
    /**
     * Sets weather extremes.
     * @param extremesText Extremes text.
     */
    protected void setWeatherExtremes(String extremesText){
        this.extremesText = extremesText;
    }
    
    /**
     * Sets weather date.
     * @param epoch unix epoch.
     */
    protected void setWeatherDate(int epoch){
        this.weatherDate = new Date(epoch*1000L);
    }
    
    
    /**
     * Returns the temperature
     * @return temperature.
     */
    public float getTemperature(){
        return this.temp;
    }
    /**
     * Returns the humidity.
     * @return humidity.
     */
    public float getHumidity(){
        return this.humid;
    }
    /**
     * Returns the wind speed
     * @return wind speed in m/s
     */
    public float getWindSpeed(){
        return this.windspeed;
    }
    /**
     * Returns the icon constant.
     * @return icon constant name.
     */
    public WeatherIcon getIcon(){
        return this.icon;
    }
    
    /**
     * Returns the icon image name.
     * @return icon image like 1.png, -1.png, 10.png, etc...
     */
    public String getIconImage(){
        return (this.iconImage.isEmpty())?"":this.iconImage;
    }
    
    /**
     * Returns the weather description.
     * @return description text.
     */
    public String getDescription(){
        return (this.text.isEmpty())?"":this.text;
    }
    
    /**
     * Returns the pressure.
     * @return Pressure.
     */
    public float getPressure(){
        return this.pressure;
    }
    
    /**
     * Returns the wind direction.
     * @return Returns the wind direction named as one of the parts on the 16 part wind rose.
     */
    public String getWindDirection(){
        return (this.windDirectionName.isEmpty())?"":this.windDirectionName;
    }
    
    /**
     * Returns the wind direction in degrees.
     * @return Degrees
     */
    public float getWindDirectionDegrees(){
        return this.windDirectionDegrees;
    }
    
    /**
     * Returns the weather extremes.
     * @return When extremes are available non empty.
     */
    public String getWeatherExtremes(){
        return (this.extremesText.isEmpty())?"":this.extremesText;
    }
    
    /**
     * Returns the water date.
     * @return weather date as Date.
     */
    public Date getWeatherDate(){
        return this.weatherDate;
    }
    
}
