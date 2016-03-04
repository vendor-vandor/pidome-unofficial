/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.applicationsbar;

import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.userpresence.UserPresenceEvent;
import org.pidome.client.system.domotics.components.userpresence.UserPresences;
import org.pidome.client.system.domotics.components.userpresence.UserPresencesEventListener;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;
import org.pidome.client.system.scenes.components.mainstage.desktop.NewDesktopShortcut;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.FilteredListItem;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.FilteredList;
import org.pidome.client.system.scenes.windows.TitledWindow;

/**
 *
 * @author John Sirach
 */
public class ApplicationsBarSettingsUserPresence extends TitledWindow implements UserPresencesEventListener {

    Map<String,StackPane> itemBlocks = new HashMap<>();
    
    int curActive = 0;
    
    static Logger LOG = LogManager.getLogger(ApplicationsBarSettingsUserPresence.class);
    
    FilteredList list = new FilteredList(null);
    
    HBox content = new HBox();
    
    StackPane cancelStack = new StackPane();
    StackPane okStack = new StackPane();
    
    PasswordField codeBox = new PasswordField();
    
    GridPane keys = new GridPane();
    
    public ApplicationsBarSettingsUserPresence(){
        super("setuserpresence","Set Presence");
        HBox.setMargin(list, new Insets(0,0,0,10));
        list.setListSize(380, 375);
        list.build();
        createInitialUserPresences();
        content.getChildren().addAll(list, keyPadDisplay());
    }
    
    @Override
    protected void setupContent() {
        content.setMinWidth(775);
        setContent(content);
    }

    @Override
    protected void removeContent() {
        cancelStack.removeEventFilter(MouseEvent.MOUSE_PRESSED, this::cancelHelper);
        okStack.removeEventFilter(MouseEvent.MOUSE_PRESSED, this::okHelper);
        keys.getChildren().stream().filter((key) -> (key.getUserData()!=null && !key.getUserData().equals("actionbutton"))).forEach((key) -> {
            key.removeEventFilter(MouseEvent.MOUSE_PRESSED, this::numberClickHelper);
        });
        list.destroy();
        UserPresences.removePresencesEventListener(this);
    }
    
    final void createInitialUserPresences(){
        Map<Integer,String> presences = UserPresences.getPresences();
        presences.keySet().stream().forEach((presenceId) -> {
            
            FilteredListItem item = new FilteredListItem(String.valueOf(presenceId), presences.get(presenceId), "presence", "Presence");
            item.setContent(createPresenceBar(presenceId, presences.get(presenceId)));
            list.addItem(item);
            
            if(presenceId == UserPresences.getCurrent()){
                list.highlightRow(String.valueOf(presenceId));
                curActive=presenceId;
            }
        });
        UserPresences.addPresencesEventListener(this);
    }
    
    final void cancelHelper(MouseEvent me){
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Platform.runLater(() -> { 
                        codeBox.setText("");
                        cancelStack.setStyle("-fx-background-color: #e68400; -fx-font-size: 1.6em;"); 
                    });
                    Thread.sleep(100);
                    Platform.runLater(() -> { cancelStack.setStyle("-fx-background-color: #00669d; -fx-font-size: 1.6em;"); });
                } catch (InterruptedException ex) {
                    //// no probs dude
                    Platform.runLater(() -> { cancelStack.setStyle("-fx-background-color: #00669d; -fx-font-size: 1.6em;"); });
                }
            }
        };
        thread.start();
    }
    
    final void okHelper(MouseEvent me){
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Platform.runLater(() -> { okStack.setStyle("-fx-background-color: #e68400; -fx-font-size: 1.6em;"); });
                    Thread.sleep(100);
                    Platform.runLater(() -> { okStack.setStyle("-fx-background-color: #00669d; -fx-font-size: 1.6em;"); });
                } catch (InterruptedException ex) {
                    //// no probs dude
                    Platform.runLater(() -> { okStack.setStyle("-fx-background-color: #00669d; -fx-font-size: 1.6em;"); });
                }
            }
        };
        thread.start();
    }
    
    final void numberClickHelper(MouseEvent me){
        StackPane number = (StackPane)me.getSource();
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Platform.runLater(() -> { 
                        codeBox.setText(codeBox.getText()+(String)number.getUserData());
                        number.setStyle("-fx-background-color: #e68400; -fx-font-size: 1.6em;"); 
                    });
                    Thread.sleep(100);
                    Platform.runLater(() -> { number.setStyle("-fx-background-color: #00669d; -fx-font-size: 1.6em;"); });
                } catch (InterruptedException ex) {
                    //// no probs dude
                    Platform.runLater(() -> { number.setStyle("-fx-background-color: #00669d; -fx-font-size: 1.6em;"); });
                }
            }
        };
        thread.start();
    }
    
    final VBox keyPadDisplay(){
        VBox display = new VBox();
        display.setMinWidth(210);
        display.setMaxWidth(210);

        display.setMinHeight(345);
        display.setMaxHeight(345);
        
        VBox.setVgrow(display, Priority.NEVER);
        
        display.setAlignment(Pos.CENTER);
        display.setPadding(new Insets(5,5,5,5));
        display.setStyle("-fx-background-color: #222222;");
        
        HBox.setMargin(display, new Insets(15,0,0,50));
        
        codeBox.setMinWidth(200);
        codeBox.setMaxWidth(200);
        codeBox.setFocusTraversable(false);
        codeBox.setEditable(false);
        codeBox.setAlignment(Pos.CENTER_RIGHT);
        codeBox.setStyle("-fx-background-color: #d2d2d2; -fx-font-size: 1.8em; -fx-border-color: #00669d;");
        
        VBox.setMargin(codeBox, new Insets(10));
        
        keys.setVgap(10);
        keys.setHgap(10);
        int curRow = 0;
        int curColumn = 0;
        for(int i=0; i < 10; i++){
            StackPane curNumberStack = new StackPane();
            curNumberStack.setStyle("-fx-background-color: #00669d; -fx-font-size: 1.6em;");
            Text number = new Text(String.valueOf(i));
            number.setMouseTransparent(true);
            curNumberStack.getChildren().add(number);
            curNumberStack.setUserData(String.valueOf(i));
            curNumberStack.addEventFilter(MouseEvent.MOUSE_PRESSED, this::numberClickHelper);
            if(i==0){
                keys.add(curNumberStack,1,3);
            } else {
                keys.add(curNumberStack,curColumn,curRow);
                curColumn++;
                if ( ( curColumn % 3 ) == 0 ) {
                    curRow++;
                    curColumn = 0;
                }
            }
        }
        
        cancelStack.setStyle("-fx-background-color: #00669d;");
        cancelStack.addEventFilter(MouseEvent.MOUSE_PRESSED, this::cancelHelper);
        cancelStack.getChildren().add(new ImageView(new ImageLoader("controls/simplecancel.png", 38, 37).getImage()));
        cancelStack.setUserData("actionbutton");
        keys.add(cancelStack,0,3);
        
        okStack.setStyle("-fx-background-color: #00669d;");
        okStack.getChildren().add(new ImageView(new ImageLoader("controls/simpleok.png", 38, 37).getImage()));
        okStack.setUserData("actionbutton");
        keys.add(okStack,2,3);
        okStack.addEventFilter(MouseEvent.MOUSE_PRESSED, this::okHelper);
        
        RowConstraints row = new RowConstraints();
        row.setMinHeight(60);
        for(int i = 0; i < curRow+1; i++) {
            keys.getRowConstraints().add(row);
        }
        ColumnConstraints column = new ColumnConstraints();
        column.setMinWidth(60);
        for(int i = 0; i < 3; i++) {
            keys.getColumnConstraints().add(column);
        }
        display.getChildren().addAll(codeBox,keys);
        return display;
    }
    
    final void sendPresenceActionHelper(ActionEvent t){
        try {
            ClientData.sendData(UserPresences.getSetActiveCommand((int)((Node)t.getSource()).getUserData()));
        } catch (DomComponentsException ex) {
            LOG.error("Could not send data: " + ex.getMessage());
        }
    }
    
    HBox createPresenceBar(final int presenceId, final String presenceName){
        final HBox macroBox = new HBox();
        
        final ImageView defaultIconView = new ImageView(new ImageLoader("macros/play-button.png", 33,33).getImage());
        
        final Button play = new Button();
        play.setGraphic(defaultIconView);
        play.setUserData(presenceId);
        
        play.setOnAction(this::sendPresenceActionHelper);
        macroBox.getChildren().add(play);
        VBox macrodetails = new VBox();
        
        Label nameLabel = new Label(presenceName);
        nameLabel.getStyleClass().add("name");
        VBox.setMargin(nameLabel, new Insets(0,
                                             0,
                                             0,
                                             5*DisplayConfig.getWidthRatio()));
        macrodetails.getChildren().add(nameLabel);
        macroBox.getChildren().add(macrodetails);
        HBox.setMargin(macroBox, new Insets(0,
                                            0,
                                            5*DisplayConfig.getHeightRatio(),
                                            0));
        Map<String,Object> shortcutOptions = new HashMap<>();
        shortcutOptions.put("id", presenceId);
        NewDesktopShortcut creator = new NewDesktopShortcut(nameLabel);
        creator.setServerCall("MacroService.setFavorite", shortcutOptions);
        return macroBox;
    }
    
    final void changeSelectedItem(int newItem){
        list.deHighlightRow(String.valueOf(curActive));
        curActive = newItem;
        list.highlightRow(String.valueOf(newItem));
    }

    
    @Override
    public void handleUserPresencesEvent(UserPresenceEvent event) {
        switch(event.getEventType()){
            case UserPresenceEvent.PRESENCECHANGED:
                Platform.runLater(() -> { changeSelectedItem(event.getPresenceId()); });
            break;
        }
    }

}
