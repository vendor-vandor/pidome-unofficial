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

package org.pidome.client.system.scenes.windows;

import java.util.Calendar;
import java.util.GregorianCalendar;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import org.pidome.client.utils.MiscImpl;

/**
 *
 * @author John
 */
public class SimpleQuickNotificationMessage extends SimpleErrorMessage {

    public SimpleQuickNotificationMessage(String iconMessage) {
        super(iconMessage);
        autoClose(2);
        windowMessagePane.getStyleClass().add("error-message");
        windowMessagePane.setPrefSize(500, 110);
        setSize(500, 110);
        windowMessagePane.setAlignment(Pos.TOP_LEFT);
        this.setAlignment(Pos.TOP_LEFT);
        this.message.getStyleClass().add("message");
        this.message.setTranslateX(20);
        this.message.setTranslateY(39);
        Calendar cal = new GregorianCalendar();
        date = new Label("Date: " + MiscImpl.setZeroLeading(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)), 2) + 
                              "." + MiscImpl.setZeroLeading(String.valueOf(cal.get(Calendar.MONTH))+1, 2) + 
                              "." + cal.get(Calendar.YEAR) +
                              " " + MiscImpl.setZeroLeading(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)), 2) + 
                              ":" + MiscImpl.setZeroLeading(String.valueOf(cal.get(Calendar.MINUTE)), 2));
        date.getStyleClass().add("date");
        date.setTranslateX(20);
        date.setTranslateY(11);
        subMessage.getStyleClass().add("submessage");
        subMessage.setTranslateX(20);
        subMessage.setTranslateY(77);
    }
    
}
