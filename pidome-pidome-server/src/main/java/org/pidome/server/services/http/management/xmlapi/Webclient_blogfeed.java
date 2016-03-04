/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.server.services.http.management.xmlapi;

import com.sun.syndication.feed.synd.SyndEntry;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.pidome.server.services.http.Webservice_renderer;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.IOException;
import java.util.Iterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John Sirach
 */
public class Webclient_blogfeed extends Webservice_renderer {
    
    static Logger LOG = LogManager.getLogger(Webclient_blogfeed.class);
    
    public Webclient_blogfeed(){
        super();
    }
    
    @Override
    public void collect(){
        
        Map<String,Map<String,String>> content = new TreeMap<>(Collections.reverseOrder());
        try {
            URL feedUrl = new URL(getDataMap.get("feed"));
            XmlReader reader = new XmlReader(feedUrl);
            SyndFeed feed = new SyndFeedInput().build(reader);
            
            for (Iterator i = feed.getEntries().iterator(); i.hasNext();) {
                SyndEntry message = (SyndEntry) i.next();
                Map<String,String> tmp = new HashMap<>();
                tmp.put("title", message.getTitle());
                tmp.put("description", message.getDescription().getValue());
                tmp.put("url", message.getLink());
                tmp.put("date", message.getPublishedDate().toLocaleString());
                content.put(String.valueOf((message.getPublishedDate().getTime()/1000)),tmp);
            }
        } catch (IllegalArgumentException | FeedException | IOException ex) {
            LOG.warn("Could not read remote rss feed '{}'. Reason: {}", getDataMap.get("feed"), ex.getMessage());
        }
        setData("content", content);
    }
}