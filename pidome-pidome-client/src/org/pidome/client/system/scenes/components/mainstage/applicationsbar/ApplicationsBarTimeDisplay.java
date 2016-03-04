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

package org.pidome.client.system.scenes.components.mainstage.applicationsbar;

import eu.hansolo.enzo.clock.Clock;
import eu.hansolo.enzo.clock.ClockBuilder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.scenes.windows.TitledWindow;
import org.pidome.client.system.time.ClientTime;

/**
 *
 * @author John
 */
public final class ApplicationsBarTimeDisplay extends TitledWindow {
    
    final Clock clock = ClockBuilder.create()
                             .prefSize(200*DisplayConfig.getWidthRatio(), 200*DisplayConfig.getHeightRatio())
                             .design(Clock.Design.BOSCH)
                             .build();
    
    final GridPane calendarPane = new GridPane();
    
    Label currentDateString = new Label(ClientTime.getLongDateString().getValueSafe());
    
    final ClientDataConnectionListener timeUpdater = this::timeUpdateHelper;
    final ChangeListener<String> dateUpdater = this::dateTextUpdateHelper;
    
    public ApplicationsBarTimeDisplay(){
        super("timedatewindow", "Time and Date");
    }

    final void dateTextUpdateHelper(ObservableValue<? extends String> ov, String t, String t1){
        Platform.runLater(() -> { currentDateString.setText(t1); });
    }
    
    @Override
    protected void setupContent() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        Text descText = new Text("A quick overview of current time, date, calendar and your alarms");
        GridPane.setMargin(descText, new Insets(10, 0, 10, 10));
        GridPane.setHalignment(currentDateString, HPos.CENTER);
        GridPane.setValignment(currentDateString, VPos.TOP);
        
        LocalDateTime now = LocalDateTime.of(ClientTime.getYearNumberProperty().get(), 
                                              ClientTime.getMonthNumberProperty().get(), 
                                              ClientTime.getDayNumberProperty().get(), 
                                              Integer.valueOf(ClientTime.get24HourNameProperty().get().split(":")[0]),
                                              Integer.valueOf(ClientTime.get24HourNameProperty().get().split(":")[0]));
        clock.setRunning(true);
        clock.setDateTime(now);
        grid.add(descText, 0, 0, 3, 1);
        GridPane.setMargin(clock, new Insets(0,0,0,10));
        grid.add(clock, 0, 1);
        grid.add(currentDateString, 0,2,1,2);
        grid.add(calendarBox(), 1,1,1,2);
        grid.add(alarmsBox(), 2,1);
        
        Text explanationText = new Text("If you notice a difference between your system and application time then this is because the "
                                      + "client application uses the time and date settings of the server. This also means when time, "
                                      + "date or timezone settings are changed on the server these will be reflected in the client. "
                                      + "Remark: Only time and date is implemented as of this moment");
        explanationText.setWrappingWidth(760);
        GridPane.setMargin(explanationText, new Insets(20,0,0,10));
        grid.add(explanationText, 0, 3, 3, 1);
        
        ClientTime.getLongDateString().addListener(dateUpdater);
        ClientData.addClientDataConnectionListener(timeUpdater);
        setContent(grid);
    }

    @Override
    protected void removeContent() {
        ClientTime.getLongDateString().removeListener(dateUpdater);
        ClientData.removeClientDataConnectionListener(timeUpdater);
    }
    
    final Node alarmsBox(){
        VBox alarms = new VBox();
        Text notif = new Text("There is no alarm plugin yet. But when available your time based alarms will show up here.");
        notif.setWrappingWidth(180);
        alarms.getChildren().addAll(notif);
        return alarms;
    }
    
    final void drawCalendarBox(){
        calendarPane.getChildren().clear();
        calendarPane.setHgap(3);
        calendarPane.setVgap(3);
        calendarPane.setGridLinesVisible(false);
        Calendar calendar = new GregorianCalendar();
        int curDay = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int curMonth = calendar.get(Calendar.MONTH);
        int curWeek  = calendar.get(Calendar.WEEK_OF_YEAR);
        int curRow = 1;
        int curColumn = 1; /// One is the first day of the week (will be mapped later on).
        int startDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        /// Add the week number
        calendarPane.add(new Label(String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR))),0,1);
        ArrayList<String> dayNames = new ArrayList<>();
        dayNames.add("Sun");
        dayNames.add("Mon");
        dayNames.add("Tue");
        dayNames.add("Wed");
        dayNames.add("Thu");
        dayNames.add("Fri");
        dayNames.add("Sat");
        //// First show the days
        if(calendar.getFirstDayOfWeek()==1){
            calendarPane.add(new Label(dayNames.get(0)),1,0);
            calendarPane.add(new Label(dayNames.get(1)),2,0);
            calendarPane.add(new Label(dayNames.get(2)),3,0);
            calendarPane.add(new Label(dayNames.get(3)),4,0);
            calendarPane.add(new Label(dayNames.get(4)),5,0);
            calendarPane.add(new Label(dayNames.get(5)),6,0);
            calendarPane.add(new Label(dayNames.get(6)),7,0);
            curColumn = startDayOfWeek;
        } else {
            calendarPane.add(new Label(dayNames.get(1)),1,0);
            calendarPane.add(new Label(dayNames.get(2)),2,0);
            calendarPane.add(new Label(dayNames.get(3)),3,0);
            calendarPane.add(new Label(dayNames.get(4)),4,0);
            calendarPane.add(new Label(dayNames.get(5)),5,0);
            calendarPane.add(new Label(dayNames.get(6)),6,0);
            calendarPane.add(new Label(dayNames.get(0)),7,0);
            curColumn = startDayOfWeek - 1;
        }
        /// Get the weekday this month start, which should correspond with the day column.
        /// looping!
        while(curMonth == calendar.get(Calendar.MONTH)){
            if(curWeek!=calendar.get(Calendar.WEEK_OF_YEAR)){
                curWeek = calendar.get(Calendar.WEEK_OF_YEAR);
                curRow++;
                calendarPane.add(new Label(String.valueOf(curWeek)),0,curRow);
                curColumn = 1;
            }
            StackPane curDayStack = new StackPane();
            Text day = new Text(String.valueOf(calendar.get(Calendar.DATE)));
            if(curDay == calendar.get(Calendar.DAY_OF_MONTH)){
                curDayStack.setStyle("-fx-background-color: #e68400;");
                day.setStyle("-fx-fill: #222222;");
            } else {
                curDayStack.setStyle("-fx-background-color: #00669d;");
            }
            curDayStack.setAlignment(Pos.TOP_RIGHT);
            StackPane.setMargin(day, new Insets(2,4,0,0));
            curDayStack.getChildren().add(day);
            calendarPane.add(curDayStack,curColumn,curRow);
            curColumn++;
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)+1);
        }
        calendarPane.getRowConstraints().add(new RowConstraints());
        RowConstraints row = new RowConstraints();
        row.setMinHeight(40);
        for(int i = 1; i < curRow+1; i++) {
            calendarPane.getRowConstraints().add(row);
        }
        ColumnConstraints column = new ColumnConstraints();
        column.setMinWidth(40);
        for(int i = 0; i < 8; i++) {
            calendarPane.getColumnConstraints().add(column);
        }
        calendarPane.getChildren().stream().filter((n) -> (n instanceof Label)).map((n) -> (Label) n).map((control) -> {
            control.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            return control;
        }).forEach((control) -> {
            control.setStyle("-fx-alignment: center;");
        });
        calendarPane.getChildren().stream().filter((n) -> (n instanceof StackPane)).map((n) -> (StackPane) n).map((control) -> {
            control.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            return control;
        });
    }
    
    final Node calendarBox(){
        VBox cal = new VBox(3);
        drawCalendarBox();
        cal.getChildren().addAll(calendarPane);
        return cal;
    }
    
    final void timeUpdateHelper(ClientDataConnectionEvent event){
        switch (event.getEventType()) {
            case ClientDataConnectionEvent.SYSRECEIVED:
                Map<String, Object> workData = (Map<String, Object>) event.getData();
                switch (event.getMethod()) {
                    case "time":
                        LocalDateTime now = LocalDateTime.of(((Long) workData.get("year")).intValue(), 
                                                              ((Long) workData.get("month")).intValue(), 
                                                              ((Long) workData.get("day")).intValue(), 
                                                              Integer.valueOf(((String)workData.get("time")).split(":")[0]),
                                                              Integer.valueOf(((String)workData.get("time")).split(":")[0]));
                        Platform.runLater(() -> { clock.setDateTime(now); });
                    break;
                }
            break;
        }
    }
    
}