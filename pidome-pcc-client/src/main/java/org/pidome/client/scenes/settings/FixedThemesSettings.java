/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.settings;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import org.pidome.client.css.themes.AppTheme;
import org.pidome.client.css.themes.AppTheme.ThemeTypes;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.scenes.panes.popups.PopUp;
import org.pidome.client.services.ServiceConnector;

/**
 *
 * @author John
 */
public class FixedThemesSettings extends PopUp {

    private VBox content = new VBox(5);
    
    List<AppTheme> themes;
    
    private final ObservableList<String> observableThemesList = FXCollections.observableArrayList();
    private final ObservableList<String> observableSchemesListApp = FXCollections.observableArrayList();
    private final ObservableList<String> observableSchemesListDashboard = FXCollections.observableArrayList();
    
    private final ComboBox themesBox = new ComboBox(observableThemesList);
    private final ComboBox schemesBoxApp = new ComboBox(observableSchemesListApp);
    private final ComboBox schemesBoxDashboard = new ComboBox(observableSchemesListDashboard);
    
    public FixedThemesSettings(ServiceConnector serviceConnector) {
        super(MaterialDesignIcon.THEME_LIGHT_DARK, "Select theme");
        this.themes = ScenesHandler.getThemes().getRegisteredThemes();
        
        if(serviceConnector.userDisplayType()==ServiceConnector.DisplayType.TINY){
            double width = (serviceConnector.getMaxWorkWidth()/Screen.getPrimary().getBounds().getWidth())*300;
            themesBox.setPrefWidth(width);
            schemesBoxApp.setPrefWidth(width);
            schemesBoxDashboard.setPrefWidth(width);
        } else {
            themesBox.setPrefWidth(300);
            schemesBoxApp.setPrefWidth(300);
            schemesBoxDashboard.setPrefWidth(300);
        }
        
        themesBox.setMinWidth(USE_PREF_SIZE);
        themesBox.setMaxWidth(USE_PREF_SIZE);
        
        schemesBoxApp.setMinWidth(USE_PREF_SIZE);
        schemesBoxApp.setMaxWidth(USE_PREF_SIZE);
        
        schemesBoxDashboard.setMinWidth(USE_PREF_SIZE);
        schemesBoxDashboard.setMaxWidth(USE_PREF_SIZE);
        
    }

    protected void setup(){
        Label selectTheme = new Label("Select theme");
        Label selectAppScheme = new Label("App settings");
        Label selectDashScheme = new Label("Dashboard settings");
        
        for(AppTheme theme:this.themes){
            observableThemesList.add(theme.getName());
        }
        String currentTheme = ScenesHandler.getThemes().getCurrentTheme().getName();
        
        themesBox.getSelectionModel().selectedItemProperty().addListener((ChangeListener<String>)(ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            observableSchemesListApp.clear();
            observableSchemesListDashboard.clear();
            schemesBoxApp.setDisable(true);
            schemesBoxDashboard.setDisable(true);
            for(AppTheme theme:this.themes){
                if(theme.getName().equals(newValue)){
                    if(theme.getThemeType().contains(ThemeTypes.APP)){
                        for(Map.Entry<String,String> set:theme.getDisplayOptions().entrySet()){
                            observableSchemesListApp.add(set.getValue());
                        }
                        if(observableSchemesListApp.isEmpty()){
                            observableSchemesListApp.add("Default scheme");
                        } else {
                            schemesBoxApp.setDisable(false);
                        }
                        schemesBoxDashboard.getSelectionModel().select(0);
                    } else {
                        observableSchemesListApp.add("Theme not for app");
                    }
                    if(theme.getThemeType().contains(ThemeTypes.DASHBOARD)){
                        for(Map.Entry<String,String> set:theme.getDashboardTheme().getDisplayOptions().entrySet()){
                            observableSchemesListDashboard.add(set.getValue());
                        }
                        if(observableSchemesListDashboard.isEmpty()){
                            observableSchemesListDashboard.add("Default scheme");
                        } else {
                            schemesBoxDashboard.setDisable(false);
                        }
                        schemesBoxDashboard.getSelectionModel().select(0);
                    } else {
                        observableSchemesListDashboard.add("Theme not for dashboard");
                    }
                }
            }
        });
        
        themesBox.getSelectionModel().select(currentTheme);
        content.getChildren().addAll(selectTheme, themesBox, selectAppScheme, schemesBoxApp, selectDashScheme, schemesBoxDashboard);
        this.setContent(content);
        setButtons(new PopUp.PopUpButton[]{new PopUp.PopUpButton("CANCEL", "Cancel"), new PopUp.PopUpButton("OK", "Set")});
        addListener((String buttonId) -> {
            switch(buttonId){
                case "CANCEL":
                    
                break;
                case "OK":
                    AppTheme newTheme = null;
                    for(AppTheme theme:this.themes){
                        if(theme.getName().equals(themesBox.getSelectionModel().getSelectedItem())){
                            newTheme = theme;
                            if(schemesBoxApp.isDisabled()==false){
                                String selected = (String)schemesBoxApp.getSelectionModel().getSelectedItem();
                                for(Map.Entry<String,String> set:theme.getDisplayOptions().entrySet()){
                                    if(selected.equals(set.getValue())){
                                        theme.setDisplayOption(set.getKey());
                                        break;
                                    }
                                }
                            }
                            if(schemesBoxDashboard.isDisabled()==false){
                                String selected = (String)schemesBoxDashboard.getSelectionModel().getSelectedItem();
                                for(Map.Entry<String,String> set:theme.getDashboardTheme().getDisplayOptions().entrySet()){
                                    if(selected.equals(set.getValue())){
                                        theme.getDashboardTheme().setDisplayOption(set.getKey());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    this.themes.clear();
                    if(newTheme!=null){
                        final AppTheme setTheme = newTheme;
                        ScenesHandler.getThemes().switchTheme(setTheme, true);
                    }
                break;
            }
        });
        build();
    }
    
    @Override
    public void unload() {
        
    }
    
}