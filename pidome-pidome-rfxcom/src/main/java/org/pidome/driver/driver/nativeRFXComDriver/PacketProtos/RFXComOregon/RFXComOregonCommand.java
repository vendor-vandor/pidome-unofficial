/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon;

import org.pidome.driver.device.rfxcom.RFXComDevice;
import org.pidome.driver.device.rfxcom.definitions.RFXComDefinitions;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComCommand;
import org.pidome.server.connector.drivers.devices.DeviceNotification;

/**
 *
 * @author John
 */
public final class RFXComOregonCommand extends RFXComCommand {

    double temperature  = 0;
    boolean hasTemp     = false;
    
    double humidity     = 0;
    boolean hasHumidity = false;
    
    String humidityText = "Unknown";
    boolean hasText     = false;
    
    double battery      = 0;
    boolean hasBattery  = false;
    
    double signal       = 0;
    boolean hasSignal   = false;
    
    double pressure     = 0;
    boolean hasPressure = false;

    double curRain      = 0;
    boolean hasCurRain  = false;
    
    double totalRain    = 0;
    boolean hasTotRain  = false;
    
    String forecastText = "Unknown";
    boolean hasForecast = false;
    
    double windSpeed     = 0;
    boolean hasWindSpeed = false;
    
    double windGust     = 0;
    boolean hasWindGust = false;
    
    double windChill     = 0;
    boolean hasWindChill = false;
    
    double windDirection     = 0;
    boolean hasWindDirection = false;
    
    protected void setWindSpeed(double windSpeed){
        this.windSpeed = windSpeed;
        hasWindSpeed = true;
    }
    
    protected void setWindGust(double gust){
        this.windGust = gust;
        hasWindGust = true;
    }
    
    protected void setWindChill(double chill){
        this.windChill = chill;
        hasWindChill = true;
    }
    
    protected void setWindDirection(double direction){
        this.windDirection = direction;
        hasWindDirection = true;
    }
    
    protected void setTotalRainRate(double totalRate){
        totalRain  = totalRate;
        hasTotRain = true;
    }
    
    protected void setCurrentRainRate(double currentRate){
        curRain    = currentRate;
        hasCurRain = true;
    }
    
    protected void setTemperature(double tmp){
        temperature = tmp;
        hasTemp     = true;
    }
    
    protected void setHumidity(double hum){
        humidity    = hum;
        hasHumidity = true;
    }
    
    protected void setHumidityText(RFXComDefinitions.PacketType type, int humInt){
        switch(type){
            case OREGON51:
            case OREGON52:
            case OREGON54:
                switch(humInt){
                    case 0:
                        humidityText = "Dry";
                    break;
                    case 1:
                        humidityText = "Comfort";
                    break;
                    case 2:
                        humidityText = "Normal";
                    break;
                    case 3:
                        humidityText = "Wet";
                    break;
                }
            break;
        }
        hasText = true;
    }
    
    protected void setPressure(double pressure){
        this.pressure = pressure;
        this.hasPressure = true;
    }
    
    protected void setForecastText(RFXComDefinitions.PacketType type,int forecast){
        switch(type){
            case OREGON54:
                switch(forecast){
                    case 0:
                        this.forecastText = "Sunny";
                    break;
                    case 1:
                        this.forecastText = "Partly cloudy";
                    break;
                    case 2:
                        this.forecastText = "Cloudy";
                    break;
                    case 3:
                        this.forecastText = "Rainy";
                    break;
                }
            break;
        }
        hasText = true;
    }
    
    protected void setBattery(double bat){
        this.battery = bat;
        this.hasBattery = true;
    }
    
    protected void setSignal(double signal){
        this.signal = signal;
        this.hasSignal = true;
    }
    
    @Override
    public void handle(RFXComDevice device) {
        DeviceNotification notification = new DeviceNotification();
        if(hasTemp){
            notification.addData("environment", "temperature", temperature);
        }
        if(hasHumidity){
            notification.addData("environment", "humidity", humidity);
        }
        if (hasText){
            notification.addData("environment", "humiditystat", humidityText);
        }
        if(hasForecast){
            notification.addData("environment", "forecast", forecastText);
        }
        if(hasPressure){
            notification.addData("environment", "pressure", pressure);
        }
        if(hasCurRain){
            notification.addData("environment", "raincurrent", curRain);
        }
        if(hasTotRain){
            notification.addData("environment", "raintotal", totalRain);
        }
        if(hasWindDirection){
            notification.addData("environment", "winddirection", windDirection);
        }
        if(hasWindChill){
            notification.addData("environment", "windchill", windChill);
        }
        if(hasWindGust){
            notification.addData("environment", "windgust", windGust);
        }
        if(hasWindSpeed){
            notification.addData("environment", "windspeed", windSpeed);
        }
        if(hasBattery){
            notification.addData("device", "battery", battery);
        }
        if(hasSignal){
            notification.addData("device", "signal", signal);
        }
        device.dispatchToHost(notification);
    }
    
}
