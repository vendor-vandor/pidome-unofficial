/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.plugins.weatherplugins.pidomeBuienRadar;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.interfaces.web.configuration.WebConfiguration;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.interfaces.web.configuration.WebOption;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.weatherplugin.CurrentWeatherData;
import org.pidome.server.connector.plugins.weatherplugin.FiveDayWeatherForecast;
import org.pidome.server.connector.plugins.weatherplugin.ForecastWeatherDataException;
import org.pidome.server.connector.plugins.weatherplugin.ThreeDayWeatherForecast;
import org.pidome.server.connector.plugins.weatherplugin.ThreeHoursWeatherForecast;
import org.pidome.server.connector.plugins.weatherplugin.UpcomingWeatherForecast;
import org.pidome.server.connector.plugins.weatherplugin.WeatherData;
import org.pidome.server.connector.plugins.weatherplugin.WeatherPlugin;
import org.pidome.server.connector.tools.http.HTTPConnector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author John
 */
public class PidomeBuienRadar extends WeatherPlugin {

    static Logger LOG = LogManager.getLogger(PidomeBuienRadar.class);
    
    private ScheduledExecutorService dataFetchExecutor = Executors.newSingleThreadScheduledExecutor();
    
    private String weatherStation = "";
    private String weatherStationName = "Unknown";
    
    Map<String, String> configuration = new HashMap<>();
    
    WebConfiguration conf = new WebConfiguration();

    Map<String,String> stationsList = new HashMap<>();
    
    public PidomeBuienRadar() {
        super(new Capabilities[]{Capabilities.CURRENT_WEATHER, Capabilities.THREEDAY_FORECAST});
        this.setDataOwner("http://www.buienradar.nl", "Buienradar.nl");
        WebConfigurationOptionSet optionSet = new WebConfigurationOptionSet("Buienradar measurements station");
        optionSet.setConfigurationSetDescription("Select the measurements station you wish to use.");
        try {
            
            Document document = getRemoteXml();
            
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("/buienradarnl/weergegevens/actueel_weer/weerstations/weerstation");
            
            NodeList stations = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            
            for(int i=0; i < stations.getLength(); i++){
                Node station = stations.item(i);
                String id = "";
                String name = "";
                if(station.hasChildNodes()){
                    for(int j=0; j < station.getChildNodes().getLength(); j++){
                        if ( station.getChildNodes().item(j).getNodeType() == Node.ELEMENT_NODE ) {
                            switch (station.getChildNodes().item(j).getNodeName()){
                                case "stationcode":
                                    id = station.getChildNodes().item(j).getTextContent();
                                break;
                                case "stationnaam":
                                    name = station.getChildNodes().item(j).getTextContent().replace("Meetstation ", "");
                                break;
                            }
                        }
                    }
                }
                if(!id.isEmpty() && !name.isEmpty()){
                    stationsList.put(id, name);
                }
            }
            
            
        } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException ex) {
            LOG.error("Could not get Buienradar weather stations: {}", ex.getMessage(), ex);
            stationsList.put("", "No stations available, try again later.");
        }
        
        optionSet.addOption(new WebOption("WEATHERSTATION", "Weather station", "Select the weather station you wish to use..", WebOption.WebOptionConfigurationFieldType.SELECT, stationsList));
        conf.addOptionSet(optionSet);
        this.setConfiguration(conf);
    }

    /**
     * Sets configuration values.
     * @throws org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException
     */
    @Override
    public void setConfigurationValues(Map<String, String> configuration) throws WebConfigurationException {
        this.configuration = configuration;
        if(configuration.containsKey("WEATHERSTATION")){
            this.weatherStation = configuration.get("WEATHERSTATION");
            if(stationsList.containsKey(this.weatherStation)){
                this.weatherStationName = stationsList.get(this.weatherStation);
            }
        }
    }
    
    /**
     * Returns the XML document to work on.
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException 
     */
    private Document getRemoteXml() throws MalformedURLException, IOException, ParserConfigurationException, SAXException {
        HTTPConnector connector = new HTTPConnector("http://xml.buienradar.nl", false);
        connector.setRequestTimeout(5000);
        String remoteData = connector.getData();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse( new InputSource( new StringReader( remoteData ) ) );
    }
    
    /**
     * Starts the plugin.
     * @throws PluginException 
     */
    @Override
    public void startPlugin() throws PluginException {
        if(!this.weatherStation.isEmpty()){
            Runnable run = () -> {
                ThreeDayWeatherForecast threeDayForecast = new ThreeDayWeatherForecast();
                CurrentWeatherData data = new CurrentWeatherData();
                try {
                    Document document = getRemoteXml();
                    XPathFactory xPathfactory = XPathFactory.newInstance();
                    XPath xpath = xPathfactory.newXPath();
                    XPathExpression expr = xpath.compile("/buienradarnl/weergegevens/actueel_weer/weerstations/weerstation[@id='"+this.weatherStation+"']");
                    
                    Node station = (Node) expr.evaluate(document, XPathConstants.NODE);
                    
                    
                    if(station.hasChildNodes()){
                        for(int j=0; j < station.getChildNodes().getLength(); j++){
                            if ( station.getChildNodes().item(j).getNodeType() == Node.ELEMENT_NODE ) {
                                switch (station.getChildNodes().item(j).getNodeName()){
                                    case "datum":
                                        DateFormat df = new SimpleDateFormat("MM/dd/yyyy kk:mm:ss");
                                        try {
                                            Date d = df.parse(station.getChildNodes().item(j).getTextContent());
                                            data.setWeatherDate((int)(d.getTime()/1000));
                                        } catch (ParseException ex) {
                                            LOG.warn("Setting local time/data for weather ({})", ex.getMessage());
                                            data.setWeatherDate((int)(new Date().getTime()/1000));
                                        }
                                    break;
                                    case "luchtvochtigheid":
                                        try {
                                            data.setHumidity(Integer.parseInt(station.getChildNodes().item(j).getTextContent()));
                                        } catch (Exception ex){
                                            data.setHumidity(0);
                                        }
                                    break;
                                    case "temperatuurGC":
                                        try {
                                            data.setTemperature(Float.parseFloat(station.getChildNodes().item(j).getTextContent()));
                                        } catch (Exception ex){
                                            data.setTemperature(0);
                                        }
                                    break;
                                    case "windsnelheidMS":
                                        try {
                                            data.setWindSpeed(Float.parseFloat(station.getChildNodes().item(j).getTextContent()));
                                        } catch (Exception ex){
                                            data.setWindSpeed(0);
                                        }
                                    break;
                                    case "windstotenMS":
                                        try {
                                            data.setWindGusts(Float.parseFloat(station.getChildNodes().item(j).getTextContent()));
                                        } catch (Exception ex){
                                            data.setWindGusts(0);
                                        }
                                    break;
                                    case "windrichtingGR":
                                        try {
                                            data.setWindDirectionDegrees(Float.parseFloat(station.getChildNodes().item(j).getTextContent()));
                                        } catch (Exception ex){
                                            data.setWindDirectionDegrees(0);
                                        }
                                    break;
                                    case "windrichting":
                                        try {
                                            data.setWindDirection(station.getChildNodes().item(j).getTextContent());
                                        } catch (Exception ex){
                                            data.setWindDirection("");
                                        }
                                    break;
                                    case "luchtdruk":
                                        try {
                                            data.setPressure(Float.parseFloat(station.getChildNodes().item(j).getTextContent()));
                                        } catch (Exception ex){
                                            data.setPressure(0);
                                        }
                                    break;
                                    case "icoonactueel":
                                        data.setStateIcon(getWeatherIcon(station.getChildNodes().item(j).getAttributes().getNamedItem("ID").getTextContent()));
                                        data.setStateName(station.getChildNodes().item(j).getAttributes().getNamedItem("zin").getTextContent());
                                    break;
                                }
                            }
                        }
                    }
                    
                    data.setDate(new Date());
                    this.setCurrentWeatherData(data);
                    
                    XPathFactory xPathfactory2 = XPathFactory.newInstance();
                    XPath xpath2 = xPathfactory2.newXPath();
                    XPathExpression expr2 = xpath2.compile("/buienradarnl/weergegevens/verwachting_meerdaags/*");
                    
                    NodeList stationfuture = (NodeList) expr2.evaluate(document, XPathConstants.NODESET);
                    
                    for(int j=0; j<stationfuture.getLength();j++){
                        Node futureNode = stationfuture.item(j);
                        if ( futureNode.getNodeType() == Node.ELEMENT_NODE ) {
                            switch (futureNode.getNodeName()){
                                case "dag-plus1":
                                case "dag-plus2":
                                case "dag-plus3":
                                    if(futureNode.hasChildNodes()){
                                        CurrentWeatherData futureData = new CurrentWeatherData();
                                        futureData.setDate(new Date());
                                        for(int k=0; k < futureNode.getChildNodes().getLength(); k++){
                                            switch (futureNode.getChildNodes().item(k).getNodeName()){
                                                case "datum":
                                                    try {
                                                        DateFormat df = new SimpleDateFormat("EEE d MMM yyyy", new Locale("nl")); ////donderdag 15 januari 2015
                                                        Date d = df.parse(futureNode.getChildNodes().item(k).getTextContent());
                                                        futureData.setWeatherDate((int)(d.getTime()/1000));
                                                    } catch (ParseException ex) {
                                                        futureData.setWeatherDate((int)(new Date().getTime()/1000));
                                                    }
                                                break;
                                                case "maxtemp":
                                                    futureData.setTemperature(Float.parseFloat(futureNode.getChildNodes().item(k).getTextContent()));
                                                break;
                                                case "windrichting":
                                                    futureData.setWindDirection(futureNode.getChildNodes().item(k).getTextContent());
                                                break;
                                                case "windkracht":
                                                    futureData.setWindSpeed(Float.parseFloat(futureNode.getChildNodes().item(k).getTextContent()));
                                                break;
                                                case "icoon":
                                                    futureData.setStateIcon(getWeatherIcon(futureNode.getChildNodes().item(k).getAttributes().getNamedItem("ID").getTextContent()));
                                                break;
                                            }
                                        }
                                        try {
                                            threeDayForecast.addToForecastCollection(futureData);
                                        } catch (ForecastWeatherDataException ex) {
                                            LOG.warn("Could not add forecast data: {}", ex.getMessage());
                                        }
                                    }
                                break;
                            }
                        }
                    }
                    this.setThreeDaysForecast(threeDayForecast);
                    
                } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException ex) {
                    LOG.error("Problem setting forecast weather data: {}", ex.getMessage(), ex);
                }
                
            };
            dataFetchExecutor.scheduleAtFixedRate(run, 0, 15, TimeUnit.MINUTES);
        }
    }
    
    @Override
    public void stopPlugin() throws PluginException {
        stopRunner();
    }
    
    private void stopRunner(){
        if(dataFetchExecutor!=null && !dataFetchExecutor.isTerminated()){
            dataFetchExecutor.shutdownNow();
        }
        dataFetchExecutor = null;
    }
    
        /**
     * Map weather status to an icon.
     * @param remoteIcon
     * @return 
     */
    private WeatherData.Icon getWeatherIcon(String id){
        WeatherData.Icon icon = WeatherData.Icon.NOT_AVAILABLE;
        switch(id){
            case "a":
                return WeatherData.Icon.CLEAR;
            case "aa":
                return WeatherData.Icon.CLEAR_NIGHT;
            case "b":
                return WeatherData.Icon.PARTLY_CLEAR;
            case "bb":
                return WeatherData.Icon.PARTLY_CLEAR_NIGHT;
            case "c":
            case "cc":
                return WeatherData.Icon.DREARY;
            case "d":
            case "dd":
                return WeatherData.Icon.FOG;
            case "f":
            case "ff":
                return WeatherData.Icon.SHOWERS;
            case "g":
            case "gg":
                return WeatherData.Icon.THUNDERSTORMS;
            case "h":
                return WeatherData.Icon.MOSTLY_CLOUDY_SHOWERS;
            case "hh":
                return WeatherData.Icon.MOSTLY_CLOUDY_SHOWERS_NIGHT;
            case "i":
                return WeatherData.Icon.MOSTLY_CLOUDY_SNOW;
            case "ii":
                return WeatherData.Icon.MOSTLY_CLOUDY_SNOW_NIGHT;
            case "m":
                return WeatherData.Icon.MOSTLY_CLOUDY_SHOWERS;
            case "mm":
                return WeatherData.Icon.MOSTLY_CLOUDY_SHOWERS_NIGHT;
            case "n":
                return WeatherData.Icon.HAZE;
            case "nn":
                return WeatherData.Icon.HAZE_NIGHT;
            case "o":
                return WeatherData.Icon.MOSTLY_CLEAR;
            case "oo":
                return WeatherData.Icon.MOSTLY_CLEAR_NIGHT;
            case "p":
                return WeatherData.Icon.PARTLY_CLEAR;
            case "pp":
                return WeatherData.Icon.PARTLY_CLEAR_NIGHT;
            case "q":
            case "qq":
                return WeatherData.Icon.RAIN; 
            case "s":
            case "ss":
                return WeatherData.Icon.THUNDERSTORMS; 
            case "t":
            case "tt":
                return WeatherData.Icon.SNOW; 
            case "u":
                return WeatherData.Icon.MOSTLY_CLOUDY_SNOW;
            case "uu":
                return WeatherData.Icon.MOSTLY_CLOUDY_SNOW_NIGHT;
            case "w":
            case "ww":
                return WeatherData.Icon.RAIN_AND_SNOW;
        }
        return icon;
    }
    
    @Override
    public CurrentWeatherData getCurrentWeatherData() {
        return this.getCurrentWeatherDataInternal();
    }

    @Override
    public ThreeHoursWeatherForecast getThreeHoursForecast() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ThreeDayWeatherForecast getThreeDayWeatherForecast() {
        return this.getThreeDayWeatherForecastInternal();
    }

    @Override
    public FiveDayWeatherForecast getFiveDayWeatherForecast() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getLocationName() {
        return this.weatherStationName;
    }

    @Override
    public void prepareWebPresentation() {
        /// not present.
    }

    @Override
    public boolean hasGraphData() {
        return false;
    }

    @Override
    public void prepareDelete() {
        /// not needed
    }

    @Override
    public UpcomingWeatherForecast getUpcomingForecast() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

