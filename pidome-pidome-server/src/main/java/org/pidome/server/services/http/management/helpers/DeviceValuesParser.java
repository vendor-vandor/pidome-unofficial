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

package org.pidome.server.services.http.management.helpers;

import java.util.Map;

/**
 *
 * @author John Sirach
 */
public class DeviceValuesParser {

    static Map<String, Map<String, Object>> deviceValues;

    public DeviceValuesParser(Map<String, Map<String, Object>> deviceValueSet) {
        deviceValues = deviceValueSet;
    }

    public static Object getValue(String group, String set) {
        if(group==null || set == null){
            return "Unknown";
        }
        if (deviceValues.containsKey(group)) {
            if (deviceValues.get(group).containsKey(set)) {
                return deviceValues.get(group).get(set);
            } else {
                return "Unknown";
            }
        } else {
            return "Unknown";
        }
    }
}
