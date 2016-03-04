/*
 * Copyright 2014 John.
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
package org.pidome.server.services.automations.variables;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.misc.utils.TimeUtils;
import org.pidome.server.connector.tools.properties.ObjectPropertyBindingBean;

/**
 *
 * @author John
 */
public class SunriseSunsetCalulatorVariable extends AutomationVariable implements PropertyChangeListener {
    
    static Logger LOG = LogManager.getLogger(SunriseSunsetCalulatorVariable.class);
    
    private final ObjectPropertyBindingBean sunRiseSet;
    private final String calcTime;
    private final String calcType;
    
    public SunriseSunsetCalulatorVariable(ObjectPropertyBindingBean initialValue, ObjectPropertyBindingBean sunRiseSet, String type, String calcTime){
        super(initialValue, "SunriseSunset");
        this.sunRiseSet = sunRiseSet;
        this.calcTime = calcTime;
        this.calcType = type;
        this.sunRiseSet.bind(this.getProperty());
    }
    
    @Override
    public void destroy() {
        super.unlink();
        this.sunRiseSet.unbind();
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        this.set(TimeUtils.calcTimeDiff((String)pce.getNewValue(), (this.calcType.equals("MIN"))?"-"+this.calcTime:"+"+this.calcTime));
    }
    
}
