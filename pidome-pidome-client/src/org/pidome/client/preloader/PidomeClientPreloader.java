/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.preloader;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.Reflection;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.AppProperties;
import org.pidome.client.config.AppPropertiesException;
import org.pidome.client.config.AppResources;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.client.data.ClientSessionException;
import org.pidome.client.system.scenes.components.helpers.Osdk;

/**
 * Simple Preloader.
 *
 * @author John Sirach <john.sirach@gmail.com>
 */
public class PidomeClientPreloader {

    ProgressBar bar;
    Stage stage;
    StackPane root = new StackPane();
    
    double width = 711 * DisplayConfig.getWidthRatio();
    double height = 400 * DisplayConfig.getHeightRatio();
    
    Scene preloaderScene = new Scene(root, width, height);
    
    StackPane background;
    Label loaderText;
    double loadprocess = 0;
    HBox middleSect = new HBox();
    VBox loginClient;
    Text loginErrorText = new Text();
    Button setLogin = new Button("Log in");
    TextField userNameBox = new TextField();
    PasswordField passwordBox = new PasswordField();
    TextField serverBox = new TextField();
    TextField serverPort = new TextField();
    PreloaderCredentials credentials = null;
    
    BooleanProperty preInit;
    
    static Logger LOG = LogManager.getLogger(PidomeClientPreloader.class);
    
    public PidomeClientPreloader(Stage stage, BooleanProperty preInit) {
        this.stage = stage;
        this.preInit = preInit;
        stage.setScene(preloaderScene);
        stage.sizeToScene();
        background = new StackPane();
        background.setId("Window");
        preloaderScene.setFill(Color.TRANSPARENT);
        preloaderScene.getStylesheets().addAll(AppResources.getCss("preloader.css"),
                                               AppResources.getCss("KeyboardButtonStyle.css"));
        background.setStyle("-fx-font-size: 13px;");
        background.getChildren().add(createBootScreen());
        root.setBackground(Background.EMPTY);
        root.getChildren().add(background);

        Osdk.setStage(stage);
        preloaderScene.focusOwnerProperty().addListener(new ChangeListenerImpl());
        
        userNameBox.getStyleClass().add("FormInput");
        userNameBox.setPromptText("Client name");
        
        passwordBox.getStyleClass().add("FormInput");
        passwordBox.setPromptText("Password");
        
        serverBox.getStyleClass().add("FormInput");
        serverBox.setPromptText("Server address");

        serverPort.getStyleClass().add("FormInput");
        serverPort.setPromptText("Default: 11000");
            
        if(preInit.getValue()==true){
            askForSettings();
        }
    }

    final void askForSettings(){
        loaderText.setText("Waiting for user");
        VBox settingsPane = new VBox();
        settingsPane.setStyle("-fx-background-color: #000000; -fx-border-color: lightgrey;");
        settingsPane.setTranslateX(0);
        settingsPane.setTranslateY(0);
        settingsPane.setAlignment(Pos.TOP_LEFT);
        settingsPane.setPrefSize(500, 300);
        settingsPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        settingsPane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        
        Text explain = new Text("Welcome!\n\nBefore to continue, you need to set two settings. If you are on a desktop environment it is possible to choose " +
                                "for the widget or full mode (widget mode is unsupported on the pi). \n "+
                                "The other option is if you go for the full client you can set the graphics quality. On a raspberry pi I would suggest low quality. " + 
                                "The display is the same, but without animations and display expensive effects.");
        explain.setStyle("-fx-fill: lightgrey;");
        explain.setWrappingWidth(490);
        VBox.setMargin(explain, new Insets(5 * DisplayConfig.getHeightRatio(), 5 * DisplayConfig.getWidthRatio(), 10 * DisplayConfig.getWidthRatio(), 5 * DisplayConfig.getWidthRatio()));
        
        final ToggleGroup runMode = new ToggleGroup();
        RadioButton full = new RadioButton("Full mode, full blown fullscreen original mode");
        full.setStyle("-fx-text-fill: lightgrey;");
        full.setToggleGroup(runMode);
        full.setSelected(true);
        full.setUserData(DisplayConfig.RUNMODE_DEFAULT);
        VBox.setMargin(full, new Insets(5 * DisplayConfig.getHeightRatio(), 5 * DisplayConfig.getWidthRatio(), 0, 5 * DisplayConfig.getWidthRatio()));
        
        RadioButton widget = new RadioButton("Widget mode, small application, all functionalities");
        widget.setStyle("-fx-text-fill: lightgrey;");
        widget.setToggleGroup(runMode);
        widget.setUserData(DisplayConfig.RUNMODE_WIDGET);
        VBox.setMargin(widget, new Insets(5 * DisplayConfig.getHeightRatio(), 5 * DisplayConfig.getWidthRatio(), 0, 5 * DisplayConfig.getWidthRatio()));
        widget.setToggleGroup(runMode);
        
        final ToggleGroup qualityMode = new ToggleGroup();
        RadioButton high = new RadioButton("High graphics quality, animations etc enabled");
        high.setStyle("-fx-text-fill: lightgrey;");
        high.setToggleGroup(qualityMode);
        high.setSelected(true);
        high.setUserData(DisplayConfig.QUALITY_HIGH);
        VBox.setMargin(high, new Insets(5 * DisplayConfig.getHeightRatio(), 5 * DisplayConfig.getWidthRatio(), 0, 5 * DisplayConfig.getWidthRatio()));
        
        RadioButton low = new RadioButton("Low graphics quality, animations etc disabled");
        low.setStyle("-fx-text-fill: lightgrey;");
        low.setToggleGroup(qualityMode);
        low.setUserData(DisplayConfig.QUALITY_LOW);
        VBox.setMargin(low, new Insets(5 * DisplayConfig.getHeightRatio(), 5 * DisplayConfig.getWidthRatio(), 0, 5 * DisplayConfig.getWidthRatio()));
        
        Button setSettings = new Button("Save and continue");
        setSettings.setStyle("-fx-text-fill: lightgrey; -fx-background-color:#000000; -fx-border-color: lightgrey;");
        setSettings.setOnAction((ActionEvent t) -> {
            AppProperties.setProperty("system", "client.mode", (String)runMode.getSelectedToggle().getUserData());
            AppProperties.setProperty("system", "display.quality", (String)qualityMode.getSelectedToggle().getUserData());
            AppProperties.setProperty("system", "client.firstrun", "false");
            DisplayConfig.setQuality((String)qualityMode.getSelectedToggle().getUserData());
            try {
                AppProperties.store("system", null);
                preInit.setValue(Boolean.FALSE);
                background.getChildren().remove(settingsPane);
                loaderText.setText("Waiting for server presence notification (max 10 seconds).");
            } catch (IOException ex) {
                /// could not save settings
            }
        });
        VBox.setMargin(setSettings, new Insets(10 * DisplayConfig.getHeightRatio(), 5 * DisplayConfig.getWidthRatio(), 0, 5 * DisplayConfig.getWidthRatio()));
        
        settingsPane.getChildren().addAll(explain,full,widget,high,low,setSettings);
        background.getChildren().add(settingsPane);
    }
    
    final VBox createBootScreen() {
        VBox bootSkel = new VBox();
        bootSkel.setPrefSize(width, height);
        bootSkel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        bootSkel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        bootSkel.setId("BootScreen");
        bar = new ProgressBar();
        bar.setProgress(0);
        bar.setPrefWidth(width - 40 * DisplayConfig.getWidthRatio());
        bootSkel.getChildren().add(bootHeader());
        middleSect.getChildren().add(titleHeader());
        bootSkel.getChildren().add(middleSect);
        bootSkel.getChildren().add(loaderText());
        VBox.setMargin(bar, new Insets(10 * DisplayConfig.getHeightRatio(), 0, 0, 20 * DisplayConfig.getWidthRatio()));
        bootSkel.getChildren().add(bar);
        TextFlow licenseText = licenseText();
        VBox.setVgrow((Node)licenseText,Priority.ALWAYS);
        bootSkel.getChildren().add(licenseText);
        return bootSkel;
    }

    final void showNewClientName(String name, boolean showButton, boolean showServer) {
        if (loginClient == null) {
            loginClient = new VBox();
            HBox.setMargin(loginClient, new Insets(38 * DisplayConfig.getHeightRatio(), 0, 0, 50 * DisplayConfig.getWidthRatio()));
            VBox loginSkel = new VBox();
            loginSkel.setPrefWidth(300 * DisplayConfig.getWidthRatio());
            loginSkel.setMaxWidth(Region.USE_PREF_SIZE);
            loginSkel.setMinWidth(Region.USE_PREF_SIZE);
            
            Label loginHeaderLabel = new Label("Log in");
            loginHeaderLabel.getStyleClass().add("LoginLabelHeader");
            loginSkel.getChildren().add(loginHeaderLabel);
            VBox.setMargin(loginHeaderLabel, new Insets(0, 10 * DisplayConfig.getWidthRatio(), 0, 0));
            
            loginErrorText.getStyleClass().add("LoginDesc");
            loginErrorText.setWrappingWidth(350*DisplayConfig.getWidthRatio());
            loginSkel.getChildren().add(loginErrorText);
            
            if(showServer){
                HBox serverForm = new HBox();

                VBox.setMargin(serverForm, new Insets(5 * DisplayConfig.getHeightRatio(), 10 * DisplayConfig.getWidthRatio(), 0, 0));
                Label serverBoxLabel = new Label("Server address");
                serverBoxLabel.setMinWidth(100);
                serverBoxLabel.getStyleClass().add("FormLabel");
                serverForm.getChildren().add(serverBoxLabel);
                HBox.setMargin(serverBox, new Insets(0, 0, 0, 10 * DisplayConfig.getWidthRatio()));
                serverForm.getChildren().add(serverBox);
                loginSkel.getChildren().add(serverForm);

                HBox serverPortForm = new HBox();

                VBox.setMargin(serverPortForm, new Insets(5 * DisplayConfig.getHeightRatio(), 10 * DisplayConfig.getWidthRatio(), 0, 0));
                Label portNameLabel = new Label("Server port");
                portNameLabel.setMinWidth(100);
                portNameLabel.getStyleClass().add("FormLabel");
                serverPortForm.getChildren().add(portNameLabel);
                HBox.setMargin(serverPort, new Insets(0, 0, 0, 10 * DisplayConfig.getWidthRatio()));
                serverPortForm.getChildren().add(serverPort);
                loginSkel.getChildren().add(serverPortForm);
            } else {
                HBox nameForm = new HBox();

                VBox.setMargin(nameForm, new Insets(5 * DisplayConfig.getHeightRatio(), 10 * DisplayConfig.getWidthRatio(), 0, 0));
                
                Label clientNameLabel = new Label("Client name");
                clientNameLabel.setMinWidth(100);
                clientNameLabel.getStyleClass().add("FormLabel");
                nameForm.getChildren().add(clientNameLabel);
                HBox.setMargin(userNameBox, new Insets(0, 0, 0, 10 * DisplayConfig.getWidthRatio()));
                nameForm.getChildren().add(userNameBox);
                
                HBox passForm = new HBox();

                VBox.setMargin(passForm, new Insets(5 * DisplayConfig.getHeightRatio(), 10 * DisplayConfig.getWidthRatio(), 0, 0));
                
                Label clientPassLabel = new Label("Password");
                clientPassLabel.setMinWidth(100);
                clientPassLabel.getStyleClass().add("FormLabel");
                passForm.getChildren().add(clientPassLabel);
                
                HBox.setMargin(passwordBox, new Insets(0, 0, 0, 10 * DisplayConfig.getWidthRatio()));
                passForm.getChildren().add(passwordBox);
                
                loginSkel.getChildren().addAll(nameForm,passForm);
                
            }
            
            if(showButton==true){
                passwordBox.setOnKeyPressed((KeyEvent ke) -> {
                    if (ke.getCode().equals(KeyCode.ENTER)){
                        try {
                            if(AppProperties.getProperty("system", "client.mode").equals(DisplayConfig.RUNMODE_DEFAULT)){
                                Osdk.show(false, null);
                            }
                        } catch (AppPropertiesException ex) {

                        }
                        goLogin();
                    }
                });
                setLogin.setOnAction((ActionEvent t) -> {
                    if(showServer){
                        goConnect();
                    } else {
                        goLogin();
                    }
                });
                VBox.setMargin(setLogin, new Insets(20 * DisplayConfig.getHeightRatio(), 0, 0, 110 * DisplayConfig.getWidthRatio()));
                setLogin.getStyleClass().add("FormButton");
                loginSkel.getChildren().add(setLogin);
            }
            loginSkel.setTranslateX(50*DisplayConfig.getWidthRatio());
            loginClient.getChildren().add(loginSkel);
        } else {
            setLogin.setDisable(false);
        }
        LOG.debug("Showing new client name form");
        if (!middleSect.getChildren().contains(loginClient)) {
            setLogin.setDisable(false);
            middleSect.getChildren().add(loginClient);
        }
    }

    final void goLogin(){
        String clientName = userNameBox.getText();
        String password = passwordBox.getText();
        // Do not allow any further edits
        setLogin.setDisable(true);
        try {
            ClientData.login(clientName,password);
        } catch (ClientSessionException ex) {
            handleLoginErrorEvent(clientName, 500, ex.getMessage());
            setLogin.setDisable(false);
        }
    }
    
    final void goConnect(){
        setLogin.setDisable(true);
        ClientData.goCustomConnect(serverBox.getText(), (serverPort.getText().equals("")?0:Integer.parseInt(serverPort.getText())), false);
        setLogin.setDisable(false);
    }
    
    final GridPane bootHeader() {
        GridPane titleSkel = new GridPane();
        titleSkel.getStyleClass().add("BootTopBox");

        titleSkel.setPrefSize(width-1, 21);
        titleSkel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        titleSkel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        ColumnConstraints leftConstraint = new ColumnConstraints(width / 2);
        leftConstraint.setHalignment(HPos.LEFT);
        ColumnConstraints rightConstraint = new ColumnConstraints(width / 2);
        rightConstraint.setHalignment(HPos.RIGHT);

        titleSkel.getColumnConstraints().add(leftConstraint);
        titleSkel.getColumnConstraints().add(rightConstraint);

        Text title = new Text("  PiDome Client");
        title.getStyleClass().add("bootTopText");
        GridPane.setMargin(title, new Insets(1, 0, 0, 0));
        titleSkel.add(title, 0, 0);

        Text vers;
        try {
            vers = new Text("Version: " + AppProperties.getProperty("system", "client.version") + "   ");
        } catch (AppPropertiesException ex) {
            vers = new Text("Version: Unknown   ");
        }
        vers.getStyleClass().add("bootTopText");
        GridPane.setMargin(vers, new Insets(1, 0, 0, 0));
        titleSkel.add(vers, 1, 0);

        return titleSkel;
    }

    final VBox titleHeader() {
        VBox titleSkel = new VBox();

        titleSkel.setMinSize(200 * DisplayConfig.getWidthRatio(), 200 * DisplayConfig.getHeightRatio());
        titleSkel.setPrefSize(200 * DisplayConfig.getWidthRatio(), 200 * DisplayConfig.getHeightRatio());
        titleSkel.setMaxSize(200 * DisplayConfig.getWidthRatio(), 200 * DisplayConfig.getHeightRatio());

        Reflection reflection = new Reflection();
        reflection.setFraction(0.4);
        reflection.setTopOpacity(0.2);
        reflection.setTopOffset(-20 * DisplayConfig.getHeightRatio());

        Text title = new Text("PiDome");
        title.getStyleClass().add("appTitle");
        title.setEffect(reflection);
        titleSkel.getChildren().add(title);

        Text desc = new Text("Open source Domotica");
        desc.getStyleClass().add("bootTitleDesc");
        VBox.setMargin(desc, new Insets(0, 0, 0, 3 * DisplayConfig.getWidthRatio()));
        titleSkel.getChildren().add(desc);

        HBox.setMargin(titleSkel, new Insets(40 * DisplayConfig.getHeightRatio(), 0, 0, 20 * DisplayConfig.getWidthRatio()));

        return titleSkel;

    }

    final Label loaderText() {
        loaderText = new Label("Waiting for server presence notification (max 10 seconds).");
        loaderText.getStyleClass().add("bootLoadingDesc");
        VBox.setMargin(loaderText, new Insets(0, 0, 0, 20 * DisplayConfig.getWidthRatio()));
        return loaderText;
    }

    final TextFlow licenseText() {
        TextFlow textSkel = new TextFlow ();
        textSkel.getStyleClass().add("BootBottomBox");
        textSkel.setPrefWidth(width);

        Text licenseText = new Text();
        licenseText.setText("The PiDome Client and PiDome Server are both in development and in an experimental state. The license has yet to be determined as different components are used, but will likely be GPLv2. "
                + "Current license model is GPLv2 for the PiDome Server/Client software. Used libraries by they're respective owner(s) and available online. "
                + "For more information about the software, license, help, bug reports or feature requests please visit: http://pidome.org");
        licenseText.getStyleClass().add("BootBottomBoxText");
        VBox.setMargin(licenseText, new Insets(5 * DisplayConfig.getHeightRatio(), 5 * DisplayConfig.getWidthRatio(), 0, 5 * DisplayConfig.getWidthRatio()));
        textSkel.getChildren().add(licenseText);

        VBox.setMargin(textSkel, new Insets(30 * DisplayConfig.getHeightRatio(), 0, 0, 0));

        return textSkel;
    }

    public void handleApplicationProgress(final Double progressValue) {
        Platform.runLater(() -> {
            loadprocess += progressValue;
            if (loadprocess == 15) {
                loaderText.setText("Connecting");
            } else if (loadprocess == 30) {
                loaderText.setText("Logging in");
            } else if (loadprocess == 45) {
                if (middleSect.getChildren().contains(loginClient)) {
                    middleSect.getChildren().remove(loginClient);
                }
                loaderText.setText("Retrieving data from server");
            } else if (loadprocess == 60) {
                loaderText.setText("Setting up/preloading");
            } else if (loadprocess == 75) {
                loaderText.setText("Rock and rollin");
            } else if (loadprocess >= 100) {
                loaderText.setText("Here we go");
            }
            bar.setProgress(loadprocess * 0.01);
        });
    }

    public final void handleLoginErrorEvent(final String loginName, int loginError, final String loginErrorMessage) {
        Platform.runLater(() -> {
            if (middleSect.getChildren().contains(loginClient)) {
                middleSect.getChildren().remove(loginClient);
                loginClient = null;
            }
            switch (loginError) {
                case 0:
                    loginErrorText.setText("This is the first time starting the client, please give this client a name.");
                    showNewClientName("", true, false);
                    break;
                case 202:
                    loginErrorText.setText(loginErrorMessage);
                    showNewClientName("", false, false);
                    break;
                case 401:
                    loginErrorText.setText(loginErrorMessage);
                    showNewClientName("", true,false);
                    break;
                case 400:
                    switch(loginErrorMessage){
                        case "SHORT_DEVICE_NAME":
                            loginErrorText.setText("'" + loginName + "' is too short to use, minimum of 4 characters needed.");
                        break;
                        default:
                            loginErrorText.setText(loginErrorMessage);
                        break;
                    }
                    showNewClientName("", true,false);
                    break;
                case -1:
                    loginErrorText.setText("Could not find the server, please give server address and port.");
                    showNewClientName("", true,true);
                break;
                case -2:
                    loginErrorText.setText("Could not connect: " + loginErrorMessage);
                    showNewClientName("", true,true);
                break;
                default:
                    showNewClientName("", true,false);
                    break;
            }
        });
    }

    public final void removeFromStage() {
        FadeTransition ft = new FadeTransition(Duration.millis(1000), root);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        EventHandler<ActionEvent> eh = (ActionEvent t) -> {
            root.setVisible(false);
        };
        ft.setOnFinished(eh);
        ft.play();
    }

    public static interface PreloaderCredentials {

        public void setClientName(String clientName, String password);

        public void setServerCredentials(String serverIp, String serverPort);
    }

    public static class LoginErrorEvent {

        private final String loginName;
        private final String loginError;

        public LoginErrorEvent(String loginName, String loginError) {
            this.loginName = loginName;
            this.loginError = loginError;
        }

        public final String getLoginName() {
            return this.loginName;
        }

        public final String getLoginError() {
            return this.loginError;
        }
    }

    private static class ChangeListenerImpl implements ChangeListener<Node> {

        public ChangeListenerImpl() {
        }

        @Override
        public void changed(ObservableValue<? extends Node> value, Node n1, Node n2) {
            if (n2 != null && n2 instanceof TextInputControl) {
                Osdk.show(true, (TextInputControl) n2);
            } else {
                Osdk.show(false, null);
            }
        }
    }
}
