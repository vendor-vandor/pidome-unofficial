/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.plugins.weatherplugins.pidomeOpenWeatherMap;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.pidome.server.connector.interfaces.web.configuration.WebConfiguration;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.interfaces.web.configuration.WebOption;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.weatherplugin.CurrentWeatherData;
import org.pidome.server.connector.plugins.weatherplugin.FiveDayWeatherForecast;
import org.pidome.server.connector.plugins.weatherplugin.ThreeDayWeatherForecast;
import org.pidome.server.connector.plugins.weatherplugin.ThreeHoursWeatherForecast;
import org.pidome.server.connector.plugins.weatherplugin.UpcomingWeatherForecast;
import org.pidome.server.connector.plugins.weatherplugin.WeatherData;
import org.pidome.server.connector.plugins.weatherplugin.WeatherPlugin;
import org.pidome.server.connector.shareddata.SharedServerTimeService;
import org.pidome.server.connector.tools.http.JSONConnector;

/**
 *
 * @author John
 */
public class PidomeOpenWeatherMap extends WeatherPlugin {

    static Logger LOG = LogManager.getLogger(PidomeOpenWeatherMap.class);
    
    private ScheduledExecutorService dataFetchExecutor = Executors.newSingleThreadScheduledExecutor();
    
    private String cityName = "";
    private String cityId = "";
    
    Map<String, String> configuration = new HashMap<>();
    
    private String apiKey = "";
    
    private long lastForecastRequestTime = 0;
    
    WebConfiguration conf = new WebConfiguration();
    
    DecimalFormat tempFormat = new DecimalFormat("#0.##", DecimalFormatSymbols.getInstance(Locale.US));
    
    public PidomeOpenWeatherMap() {
        super(new Capabilities[]{Capabilities.SEARCH_LOCATION_LATLON,Capabilities.CURRENT_WEATHER, Capabilities.THREEDAY_FORECAST, Capabilities.UPCOMING_FORECAST});
        WebConfigurationOptionSet optionSet = new WebConfigurationOptionSet("OpenWeatherMap options");
        optionSet.setConfigurationSetDescription("To be able to use the OpenWeatherMap you need an API key. please go to http://openweathermap.org/ to get one. "
                                               + "this plugin does one request per ten minutes at most. Failed retreivals are not retried and the next request is "
                                               + "made ten minutes later. These are suggested maximums by OpenWeatherMap.<br/><br/>"
                                               + "When the plugin is added, wait for a couple seconds. This plugin will be using your localization data to get a list of possible cities. "
                                               + "Be sure to have these set on the settings page.<br/>"
                                               + "<br/>"
                                               + "This plugin fetches data once per hour (See post: https://openweathermap.desk.com/customer/portal/questions/9787798-what-exactly-is-server-error-51-)");
        optionSet.addOption(new WebOption("APIKEY", "API key", "Enter the api key you got from openweathermap.org", WebOption.WebOptionConfigurationFieldType.STRING));
        
        conf.addOptionSet(optionSet);
        setConfiguration(conf);
        setDataOwner("http://openweathermap.org", "OpenWeatherMap.org");
    }

    /**
     * Sets configuration values.
     * @throws org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException
     */
    @Override
    public void setConfigurationValues(Map<String, String> configuration) throws WebConfigurationException {
        this.configuration = configuration;
        this.apiKey = configuration.get("APIKEY");
        if(configuration.containsKey("SELECTCITY")){
            this.cityName = configuration.get("SELECTCITY");
            this.cityId   = configuration.get("SELECTCITY");
        }
    }
    
    @Override
    public void startPlugin() throws PluginException {
        if(!this.apiKey.isEmpty()){
            String lat = SharedServerTimeService.getLatitude();
            String lon = SharedServerTimeService.getLongitude();
            if(!lat.isEmpty() && !lon.isEmpty()){
                WebConfigurationOptionSet optionSet = new WebConfigurationOptionSet("OpenWeatherMap City");
                optionSet.setConfigurationSetDescription("Select a city or region closest to you. If the list is empty and you have no city selected or want to change the city come "
                                                       + "back in a couple of minutes. It is possible the remote server did not respond. When saving give the plugin some time "
                                                       + "to update. It takes a maximum of about 10 seconds before the plugin quits requesting data from the remote server.");
                Map<String,String> optionSelect = new HashMap<>();
                String cityRequest = new StringBuilder("http://api.openweathermap.org/data/2.5/find?lat=").append(lat).append("&lon=").append(lon).append("&APPID=").append(apiKey).toString();
                try {
                    JSONConnector getCities = new JSONConnector(cityRequest);
                    getCities.setRequestTimeout(10000);
                    Map<String, Object> jsonData = (Map<String, Object>) getCities.getJSON(null, null, null).getObjectData();
                    for(Map<String,String> data:(ArrayList<Map<String, String>>)jsonData.get("list")){
                        optionSelect.put(String.valueOf(data.get("id")), data.get("name"));
                        if(cityId.equals(String.valueOf(data.get("id")))){
                            cityName = data.get("name");
                            setLocationName(cityName);
                        }
                    }
                    if(optionSelect.isEmpty()){
                        optionSelect.put("0", "Please try again later (by re-saving the plugin)");
                    }
                } catch (Exception ex) {
                    LOG.warn("Could not get city data, try again later: {}", ex.getMessage(), ex);
                    if(optionSelect.isEmpty()){
                        optionSelect.put("0", "Please try again later (by re-saving the plugin)");
                    }
                }
                optionSet.addOption(new WebOption("SELECTCITY", "City/District", "Select the city/district closest to you.", WebOption.WebOptionConfigurationFieldType.SELECT, optionSelect));
                conf.addOptionSet(optionSet);
            }
        }
        if(!apiKey.isEmpty() && !cityName.isEmpty() && !cityId.isEmpty()){
            dataFetchExecutor.scheduleAtFixedRate(dataFetcher(), 0, 60, TimeUnit.MINUTES);
        }
    }

    private Runnable dataFetcher(){
        Runnable run = () -> {
            String currentRequest = new StringBuilder("http://api.openweathermap.org/data/2.5/weather?id=").append(cityId).append("&APPID=").append(apiKey).toString();
            LOG.debug("Doing current weather data request using: {}", currentRequest);
            try {
                JSONConnector getCurrent = new JSONConnector(currentRequest);
                getCurrent.setRequestTimeout(20000);
                Map<String,Object> jsonData = (Map<String,Object>)getCurrent.getJSON(null, null, null).getObjectData();
                LOG.debug("Having current weather data: {}", jsonData);
                this.setCurrentWeatherData(createWeatherData(jsonData, true));
            } catch (Exception ex) {
                LOG.warn("Could not get data: {}", ex.getMessage(), ex);
            }
            Long currentRequestTime = new Date().getTime();
            if((currentRequestTime - lastForecastRequestTime) > 3600000){
                String forecastRequest = new StringBuilder("http://api.openweathermap.org/data/2.5/forecast/city?id=").append(cityId).append("&APPID=").append(apiKey).toString();
                try {
                    JSONConnector getCurrent = new JSONConnector(forecastRequest);
                    getCurrent.setRequestTimeout(20000);
                    Map<String,Object> jsonData = (Map<String,Object>)getCurrent.getJSON(null, null, null).getObjectData();
                    List<Map<String,Object>> threeHoursForecastData = (List<Map<String,Object>>)jsonData.get("list");

                    UpcomingWeatherForecast threeHourForecast = new UpcomingWeatherForecast();
                    
                    ThreeDayWeatherForecast threeDayForecast = new ThreeDayWeatherForecast();
                    
                    int forecastCounter = 0;
                    int currentDateTime = (int)(new Date().getTime()/1000);
                    int currentDay = new Date().getDay();
                    
                    for(Map<String,Object> threeHourData: threeHoursForecastData){
                        if(((Number)threeHourData.get("dt")).intValue() > currentDateTime){
                            LOG.debug("Having future weather data: {}", threeHourData);
                            threeHourForecast.addToForecastCollection(createWeatherData(threeHourData, true));
                            forecastCounter++;
                            if(forecastCounter==3) break;
                        }
                    }
                    forecastCounter = 0;
                    for(Map<String,Object> threeHourData: threeHoursForecastData){
                        if(new Date(((Number)threeHourData.get("dt")).intValue()*1000L).getDay() != currentDay && ((String)threeHourData.get("dt_txt")).contains("12:00:00")){
                            LOG.debug("Having future weather data: {}", threeHourData);
                            threeDayForecast.addToForecastCollection(createWeatherData(threeHourData, false));
                            forecastCounter++;
                            if(forecastCounter==3) break;
                        }
                    }
                    this.setThreeDaysForecast(threeDayForecast);
                    this.setUpcomingForecast(threeHourForecast);
                } catch (Exception ex) {
                    LOG.warn("Could not get/set data: {}", ex.getMessage(), ex);
                }
            }
        };
        return run;
    }
    
    /**
     * Creates the weather data.
     * @param rootData
     * @return 
     */
    private CurrentWeatherData createWeatherData(Map<String,Object> rootData, boolean dayData){
        CurrentWeatherData data = new CurrentWeatherData();
        try {
            Map<String,Object> mainData = (Map<String,Object>)rootData.get("main");
            long weatherTime = new DateTime(((Long)rootData.get("dt"))*1000L, DateTimeZone.forTimeZone(TimeZone.getTimeZone("Etc/GMT")))
                                            .toDateTime(DateTimeZone.forTimeZone(SharedServerTimeService.getCurrentTimeZone())).getMillis();
            boolean night = false;
            if(dayData){
                if(weatherTime>SharedServerTimeService.getSunset() || weatherTime<SharedServerTimeService.getSunrise()){
                    night = true;
                }
            }
            data.setWeatherDate((int)(weatherTime/1000));
            data.setDate(new Date());
            data.setTemperature(Float.valueOf(tempFormat.format(((Number)mainData.get("temp")).doubleValue()-273.15d)));
            data.setHumidity(Integer.parseInt(mainData.get("humidity").toString()));
            data.setPressure(Float.parseFloat(mainData.get("pressure").toString()));

            /// Just in case it is not available
            try {
                Map<String,Object> weatherData = ((List<Map<String,Object>>)rootData.get("weather")).get(0);
                data.setStateIcon(getWeatherIcon(((Number)weatherData.get("id")).intValue(), night));
                if(weatherData.containsKey("description")){
                    data.setStateName((String)weatherData.get("description"));
                } else {
                    data.setStateName((String)weatherData.get("main"));
                }
                /// in case there are multiple weather definitions will be looking for extremes.
                try {
                    ((List<Map<String,Object>>)rootData.get("weather")).remove(0);
                    for(Map<String,Object> extremesData: ((List<Map<String,Object>>)rootData.get("weather"))){
                        switch(((Number)weatherData.get("id")).intValue()){
                            case 900:
                            case 901:
                            case 902:
                            case 903:
                            case 904:
                            case 905:
                            case 906:
                                data.addExtremesDescription((String)extremesData.get("description"));
                            break;
                        }
                    }
                } catch (Exception ex){
                    //// Fail silently
                }
                /// Just in case wind is not available
                try {
                    Map<String,Object> windData = (Map<String,Object>)rootData.get("wind");
                    data.setWindSpeed(((Number)windData.get("speed")).floatValue());
                    data.setWindDirection(WeatherData.getWindDirection(((Number)windData.get("deg")).floatValue()));
                    data.setWindDirectionDegrees(((Number)windData.get("deg")).floatValue());
                } catch (Exception ex){
                    LOG.warn("No wind info available: {}", ex.getMessage());
                }
            } catch (Exception ex){
                LOG.warn("No basic weather info available: {}", ex.getMessage());
            }
        } catch (Exception ex){
            LOG.warn("Could not compose weather data: {}, Data: {}", ex.getMessage(), rootData, ex);
        }
        return data;
    }
    
    /**
     * Map weather status to an icon.
     * @param remoteIcon
     * @return 
     */
    private WeatherData.Icon getWeatherIcon(int status, boolean night){
        switch(status){
            case 200:
            case 201:
            case 202:
            case 210:
            case 211:
            case 212:
            case 221:
            case 230:
            case 231:
            case 232:
                return WeatherData.Icon.THUNDERSTORMS;
            case 300:
            case 301:
            case 310:
            case 311:
                return WeatherData.Icon.SHOWERS;
            case 302:
            case 312:
            case 313:
            case 314:
            case 321:
                return WeatherData.Icon.SHOWERS;
            case 500:
            case 501:
            case 520:
                return WeatherData.Icon.SHOWERS;
            case 502:
            case 503:
            case 521:
                return WeatherData.Icon.RAIN;
            case 504:
            case 522:
            case 531:
                return WeatherData.Icon.RAIN;
            case 511:
                return WeatherData.Icon.RAIN;
            case 600:
            case 601:
                return WeatherData.Icon.SNOW;
            case 602:
                return WeatherData.Icon.SNOW;
            case 611:
            case 612:
                return WeatherData.Icon.SLEET;
            case 615:
            case 616:
            case 620:
            case 621:
            case 622:
                return WeatherData.Icon.RAIN_AND_SNOW;
            case 701:
            case 741:
                return WeatherData.Icon.FOG;
            case 711:
                return WeatherData.Icon.SMOG;
            case 721:
                return WeatherData.Icon.HAZE;
            case 731:
            case 751:
                return WeatherData.Icon.SAND;
            case 761:
            case 762:
                return WeatherData.Icon.SMOG;
            case 771:
                return WeatherData.Icon.WINDY;
            case 781:
                return WeatherData.Icon.TORNADO;
            case 800:
                if(night){
                    return WeatherData.Icon.CLEAR_NIGHT;
                } else {
                    return WeatherData.Icon.CLEAR;
                }
            case 801:
                if(night){
                    return WeatherData.Icon.PARTLY_CLEAR_NIGHT;
                } else {
                    return WeatherData.Icon.PARTLY_CLEAR;
                }
            case 802:
                if(night){
                    return WeatherData.Icon.INTERMITTENT_CLOUDS_NIGHT;
                } else {
                    return WeatherData.Icon.INTERMITTENT_CLOUDS;
                }
            case 803:
                return WeatherData.Icon.CLOUDY;
            case 804:
                if(night){
                    return WeatherData.Icon.MOSTLY_CLOUDY_NIGHT;
                } else {
                   return WeatherData.Icon.MOSTLY_CLOUDY;
                }
            case 900:
            case 901:
            case 902:
                return WeatherData.Icon.TORNADO;
            case 903:
                return WeatherData.Icon.COLD;
            case 904:
                return WeatherData.Icon.HOT;
            case 905:
                return WeatherData.Icon.WINDY;
            case 906:
                return WeatherData.Icon.HAIL;
            default:
                return WeatherData.Icon.NOT_AVAILABLE;
        }
    }

    
    @Override
    public void stopPlugin() throws PluginException {
        if(dataFetchExecutor!=null && !dataFetchExecutor.isTerminated()){
            dataFetchExecutor.shutdownNow();
        }
        dataFetchExecutor = null;
    }
    
    @Override
    public CurrentWeatherData getCurrentWeatherData() {
        return this.getCurrentWeatherDataInternal();
    }

    @Override
    public UpcomingWeatherForecast getUpcomingForecast() {
        return this.getUpcomingForecastInternal();
    }
    
    @Override
    public ThreeHoursWeatherForecast getThreeHoursForecast() {
        return this.getThreeHoursForecastInternal();
    }

    @Override
    public ThreeDayWeatherForecast getThreeDayWeatherForecast() {
        return this.getThreeDayWeatherForecastInternal();
    }

    @Override
    public FiveDayWeatherForecast getFiveDayWeatherForecast() {
        return this.getFiveDayWeatherForecastInternal();
    }

    /**
     * Used to fetch a list of available cities.
     */
    @Override
    public void prepareWebPresentation() {

    }

    @Override
    public boolean hasGraphData() {
        return false;
    }

    @Override
    public void prepareDelete() {
        //// not used.
    }

    @Override
    public String getLocationName() {
        return getLocationNameInternal();
    }

}
