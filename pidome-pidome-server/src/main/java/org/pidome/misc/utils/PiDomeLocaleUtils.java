/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.misc.utils;

import java.io.IOException;
import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;

/**
 *
 * @author John
 */
public class PiDomeLocaleUtils {
    
    private static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(PiDomeLocaleUtils.class);
    
    private static Locale currentLocale;
    
    /**
     * Sets the default locale from the settings file.
     */
    public static void setDefaultLocale() throws ConfigPropertiesException {
        Locale.setDefault(Locale.forLanguageTag(SystemConfig.getProperty("system", "system.locale").trim().replace("_", "-")));
        currentLocale = Locale.getDefault();
        LOG.info("Running with locale {}", currentLocale.getDisplayName());
    }
    
    /**
     * Sets a new locale and stores it in the settings file.
     * @param localeName 
     */
    public static void setNewLocale(String localeName) {
        LOG.info("Set default locale input {} to {} as default", localeName, getLocaleFromString(localeName).getDisplayVariant());
        Locale.setDefault(getLocaleFromString(localeName));
        currentLocale = Locale.getDefault();
        SystemConfig.setProperty("system", "system.locale", localeName);
        try {
            SystemConfig.store("system", "save for locale");
        } catch (IOException ex) {
            LOG.error("Could not store locale data");
        }
    }
    
    private static Locale getLocaleFromString(String localeString){
        if (localeString == null){
            return null;
        }
        localeString = localeString.trim();
        if (localeString.toLowerCase().equals("default")){
            return Locale.getDefault();
        }

        // Extract language
        int languageIndex = localeString.indexOf('_');
        String language = null;
        if (languageIndex == -1){
            // No further "_" so is "{language}" only
            return new Locale(localeString, "");
        } else {
            language = localeString.substring(0, languageIndex);
        }

        // Extract country
        int countryIndex = localeString.indexOf('_', languageIndex + 1);
        String country = null;
        if (countryIndex == -1) {
            // No further "_" so is "{language}_{country}"
            country = localeString.substring(languageIndex+1);
            return new Locale(language, country);
        } else {
            // Assume all remaining is the variant so is "{language}_{country}_{variant}"
            country = localeString.substring(languageIndex+1, countryIndex);
            String variant = localeString.substring(countryIndex+1);
            return new Locale(language, country, variant);
        }
    }
    
    /**
     * Returns the current locale, same as Locale.getDefault();
     * @return 
     */
    public static Locale getLocale(){
        return currentLocale;
    }
    
}
