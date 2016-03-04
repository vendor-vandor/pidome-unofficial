/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.photoframe.screens.loginloader;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.pidome.client.photoframe.FrameSettings;
import org.pidome.client.photoframe.RaspberryMirror;
import org.pidome.client.photoframe.ScreenDisplay;
import org.pidome.client.photoframe.utils.ShadedLabel;
import org.pidome.client.system.PCCCLientStatusListener;
import org.pidome.client.system.PCCClient;
import org.pidome.client.system.PCCClientEvent;
import org.pidome.client.system.PCCConnection;
import org.pidome.client.system.PCCConnectionEvent;
import org.pidome.client.system.PCCConnectionListener;
import org.pidome.client.system.PCCSystem;
import org.pidome.client.system.UnknownPCCSettingException;

/**
 *
 * @author John
 */
public class LoginPreloadProgressScreen implements Screen, InputProcessor, PCCCLientStatusListener,PCCConnectionListener {
    
    private final PCCSystem clientSystem;
    
    private Image splashImage;
    private Stage stage;
    
    RaspberryMirror app;
    
    LoginPreloadProgressListener listener;
    
    ShadedLabel  font=new ShadedLabel("Loading", .7f);
    SpriteBatch batch=new SpriteBatch();
    Texture     emptyT=new Texture(Gdx.files.internal("resources/appimages/progressbar/progressbar_empty.png"));
    Texture     fullT=new Texture(Gdx.files.internal("resources/appimages/progressbar/progressbar_full.png"));
    NinePatch   empty=new NinePatch(new TextureRegion(emptyT,24,24),8,8,8,8);
    NinePatch   full=new NinePatch(new TextureRegion(fullT,24,24),8,8,8,8);

    BitmapFont defaultFont = new BitmapFont(Gdx.files.internal("resources/fonts/default.fnt"));
    
    private final float progressbarWidth  = 600;
    private final float progressbarHeight = 32;
    
    private final float progressPositionX;
    private final float progressPositionY;
    
    boolean disposed = false;
    
    int totalLoadItems = 10;
    int loadedItems = 0;
    
    int lastAssetProgress = 0;
    
    int showProgress = 0;

    Skin skin = new Skin(Gdx.files.internal("resources/skins/skin.json"));
    static org.lwjgl.input.Cursor emptyCursor;
    Texture cursor;
    int xHotspot, yHotspot;
    
    Window connectWindow;
    TextField ipAddress;
    TextField portAddress;
    TextField httpPortAddress;
    CheckBox checkSecure;
    
    Window loginWindow;
    Label loginLabel;
    TextField loginField;
    Label passLabel;
    TextField passField;
    
    TextButton connectButton;
    
    InputMultiplexer inputMultiplexer = new InputMultiplexer();
    
    /**
     * Constructor.
     * Sets the client system link and initializes the progress display.
     * @param listener Listener to report back to.
     * @param system the PCC system backend. 
     */
    public LoginPreloadProgressScreen(LoginPreloadProgressListener listener, PCCSystem system){
        super();
        this.listener = listener;
        this.clientSystem = system;
        
        this.app = (RaspberryMirror)listener;
        
        progressPositionX = (ScreenDisplay.getStageWidth() - progressbarWidth) / 2.0f;
        progressPositionY = (ScreenDisplay.getStageHeight() - progressbarHeight) / 2.0f;
        
        /**
         * Workaround until fixed, overwriting values in the skin.
         */
        Label fakeLabel = new Label("", skin);
        fakeLabel.getStyle().font = defaultFont;
        TextButton fakeButton = new TextButton("Connect", skin);
        fakeButton.getStyle().font = defaultFont;
        CheckBox fakeCheck = new CheckBox(" Yes", skin);
        fakeCheck.getStyle().font = defaultFont;
        
        stage = new Stage();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        font.setWrap(true);
        font.setWidth(ScreenDisplay.getStageWidth()*.7f);
        font.setAlignment(Align.center);
        
    }

    /**
     * Starts the login routines.
     */
    private void startRoutines(){
        this.clientSystem.getConnection().addPCCConnectionListener(this);
        this.clientSystem.getClient().addListener(this);
        new Thread(() -> { 
            loadedItems=1;
            if(this.clientSystem.getConnection().hasInitialManualConnectData()){
                setMessage("Connecting");
                this.clientSystem.getConnection().startInitialConnection();
            } else {
                setMessage("Searching server");
                this.clientSystem.getConnection().startSearch(); 
            }
        }).start();
    }
    
    @Override
    public void render(float f) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(f);
        stage.draw();
        batch.begin();
        empty.draw(batch, progressPositionX, progressPositionY, progressbarWidth, progressbarHeight);
        int assetProgess = ((int)(3 * this.app.getAssetManager().getProgress()));
        if(assetProgess!=0){
            lastAssetProgress = assetProgess;
        }
        showProgress = (100/totalLoadItems)*(loadedItems + lastAssetProgress);
        full.draw(batch, progressPositionX, progressPositionY, (float)(progressbarWidth*(showProgress/100f)), progressbarHeight);
        
        font.setPosition(ScreenDisplay.getStageCenterX()-(font.getWidth()/2), ScreenDisplay.getStageCenterY() - progressbarHeight - font.getHeight());
        try {
            font.draw(batch, 1f);
        } catch (StringIndexOutOfBoundsException ex){
            /// Sometimes we write to fast.
        }

        int x = Gdx.input.getX();
        int y = Gdx.input.getY();
        if(connectWindow.isVisible() || loginWindow.isVisible()) batch.draw(cursor, x - xHotspot, Gdx.graphics.getHeight() - y - 1 - yHotspot);
        
        batch.end();
        if(showProgress >= 90){
            setMessage("Loading done");
            try {
                Thread.sleep(1000);
            } catch (Exception ex){}
            try {
                this.clientSystem.getLocalSettings().save();
            } catch (IOException ex){
                /// could not save settings
            }
            try {
                this.clientSystem.getPreferences().save();
            } catch (IOException ex){
                /// could not save settings
            }
            inputMultiplexer.removeProcessor(this);
            inputMultiplexer.removeProcessor(stage);
            listener.preleadDone();
            dispose();
            return;
        }
    }

    @Override
    public void resize(int i, int i1) {

    }

    @Override
    public void show() {
        
        splashImage = new Image(new Texture(Gdx.files.internal("resources/appimages/logoimages/full-white-very-small.png")));
        
        splashImage.setPosition(10, 10);
        
        splashImage.setWidth(300 * ScreenDisplay.getCurrentScale());
        splashImage.setHeight(66 * ScreenDisplay.getCurrentScale());
        stage.addActor(splashImage);
        splashImage.addAction(Actions.sequence(Actions.alpha(0),Actions.fadeIn(1.0f),Actions.delay(2),Actions.run(() -> {
            if(!FrameSettings.isStandAlone()) {
                startRoutines();
            } else {
                new Thread(() -> { loadedItems=7; startAssetsPreloading(); }).start();
            }
        })));
        
        cursor = new Texture(Gdx.files.internal("resources/skins/cursor.png"));
        xHotspot = 0;
        yHotspot = cursor.getHeight();
        
        createConnectWindow();
        createLoginWindow();
        
    }
    
    private void createLoginWindow(){
        
        loginLabel = new Label("Login name", skin);
        loginField = new TextField("", skin);
        
        passLabel = new Label("Password", skin);
        passField = new TextField("", skin);
        passField.setPasswordCharacter('*');
        passField.setPasswordMode(true);
        
        loginWindow = new Window("Login data", skin);
        loginWindow.row().fill().expandX().left();
        loginWindow.add(loginLabel).width(200).padRight(75);
        loginWindow.add(loginField).width(300);
        loginWindow.row().left();
        loginWindow.add(passLabel).width(200);
        loginWindow.add(passField).width(300);
        
        try {
            loginField.setText(clientSystem.getLocalSettings().get("user.login"));
        } catch (UnknownPCCSettingException ex) {
            Logger.getLogger(LoginPreloadProgressScreen.class.getName()).log(Level.INFO, "Fresh?", ex);
        }
        
        TextButton loginButton = new TextButton("Login", skin);
        loginButton.getStyle().font = defaultFont;

        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.getStyle().font = defaultFont;
        
        loginWindow.row().right();
        loginWindow.add(exitButton).left();
        loginWindow.add(loginButton).right();
        
        loginButton.addListener(new ClickListener(){
            
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showLoginPopup(false);
                setMessage("Logging in");
                new Thread(() -> { clientSystem.getClient().login(loginField.getText(), passField.getText()); }).start();
            }
        });
        
        exitButton.addListener(new ClickListener(){
            
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(clientSystem.getClient().isloggedIn()){
                    clientSystem.getClient().logout();
                }
                Gdx.app.exit();
            }
        });
        
        loginWindow.pack();
        loginWindow.setPosition(ScreenDisplay.getStageCenterX() - (loginWindow.getWidth()/2), ScreenDisplay.getStageCenterY() + (progressbarHeight*2));
        showLoginPopup(false);
        stage.addActor(loginWindow);
        
    }
    
    private void showLoginPopup(boolean show){
        loginWindow.setVisible(show);
    }
    
    private void createConnectWindow(){
        connectWindow = new Window("Connect data", skin);
        
        Label ipLabel = new Label("Ip", skin);
        ipAddress = new TextField("", skin);
        ipAddress.getStyle().font = defaultFont;
        
        final Label portLabel = new Label("Socket port", skin);
        portAddress = new TextField("", skin);
        
        final Label httpPortLabel = new Label("HTTP Port", skin);
        httpPortAddress = new TextField("", skin);
        
        final Label secureLabel = new Label("Secure", skin);
        checkSecure = new CheckBox(" Yes", skin);
        checkSecure.getStyle().font = defaultFont;
        
        connectButton = new TextButton("Connect", skin);
        connectButton.getStyle().font = defaultFont;
        
        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.getStyle().font = defaultFont;
        
        connectWindow.row().left();
        connectWindow.add(ipLabel).width(200);
        connectWindow.add(ipAddress).width(300);
        connectWindow.row().left();
        connectWindow.add(portLabel).width(200);
        connectWindow.add(portAddress).width(300);
        connectWindow.row().left();
        connectWindow.add(httpPortLabel).width(200);
        connectWindow.add(httpPortAddress).width(300);
        connectWindow.row().left();
        connectWindow.add(secureLabel).width(200);
        connectWindow.add(checkSecure).width(300);
        connectWindow.row();
        connectWindow.add(exitButton).left();
        connectWindow.add(connectButton).right();
        
        exitButton.addListener(new ClickListener(){
            
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(clientSystem.getClient().isloggedIn()){
                    clientSystem.getClient().logout();
                }
                Gdx.app.exit();
            }
        });
        
        try {
            
            ipAddress.setText(clientSystem.getLocalSettings().get("server.address"));
            httpPortAddress.setText(clientSystem.getLocalSettings().get("server.http.port"));
            
            String port = clientSystem.getLocalSettings().get("server.socket.port");
            if(!clientSystem.getLocalSettings().get("server.socket.port.ssl").equals("0")){
                port = clientSystem.getLocalSettings().get("server.socket.port.ssl");
                checkSecure.setChecked(true);
            }
            portAddress.setText(port);
            ipAddress.setText(clientSystem.getLocalSettings().get("server.address"));
        } catch (UnknownPCCSettingException ex) {
            Logger.getLogger(LoginPreloadProgressScreen.class.getName()).log(Level.INFO, "fresh?", ex);
        }
        
        connectButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showConnectPopup(false);
                setMessage("Going to connect");
                new Thread(() -> { clientSystem.getClient().manualConnect(ipAddress.getText(), Integer.parseInt(portAddress.getText()), Integer.parseInt(httpPortAddress.getText()), checkSecure.isChecked()); }).start();
            }
                        
        });
        
        connectWindow.pack();
        connectWindow.setPosition(ScreenDisplay.getStageCenterX() - (connectWindow.getWidth()/2), ScreenDisplay.getStageCenterY() + (progressbarHeight*2));
        showConnectPopup(false);
        stage.addActor(connectWindow);

    }
    
    private void showConnectPopup(boolean show){
        connectWindow.setVisible(show);
    }
    
    public static void setHWCursorVisible(boolean visible) throws LWJGLException {
        if (Gdx.app.getType() != ApplicationType.Desktop && Gdx.app instanceof LwjglApplication) {
            return;
        }
        if (emptyCursor == null) {
            if (Mouse.isCreated()) {
                int min = org.lwjgl.input.Cursor.getMinCursorSize();
                IntBuffer tmp = BufferUtils.createIntBuffer(min * min);
                emptyCursor = new org.lwjgl.input.Cursor(min, min, min / 2, min / 2, 1, tmp, null);
            } else {
                throw new LWJGLException("Could not create empty cursor before Mouse object is created");
            }
        }
        if (Mouse.isInsideWindow()) {
            Mouse.setNativeCursor(visible ? null : emptyCursor);
        }
    }
    
    
    @Override
    public void hide() {
        ///dispose();
    }

    @Override
    public void pause() {
        /// not used
    }

    @Override
    public void resume() {
        /// Not used
    }

    @Override
    public void dispose() {
        this.clientSystem.getConnection().removePCCConnectionListener(this);
        this.clientSystem.getClient().removeListener(this);
        stage.dispose();
        batch.dispose();
        emptyT.dispose();
        fullT.dispose();
        this.listener = null;
        
        skin.dispose();
        
        cursor.dispose();
    }

    private void setMessage(String message){
        font.setText(new StringBuilder(message).append(" (").append(showProgress).append("%)"));
    }
    
    private void startAssetsPreloading(){
        setMessage("Loading app assets");
        this.app.getAssetManager().load("resources/appimages/baseTextures.pack",TextureAtlas.class);
	this.app.getAssetManager().load("resources/skins/skin.json", Skin.class, new SkinLoader.SkinParameter("resources/appimages/baseTextures.pack"));
	this.app.getAssetManager().load("resources/fonts/freesans.fnt",BitmapFont.class);
    }
    
    @Override
    public void handlePCCClientEvent(PCCClientEvent event) {
        if(event.getStatus().equals(PCCClient.ClientStatus.BUSY_LOGIN) || 
           event.getStatus().equals(PCCClient.ClientStatus.NO_DATA) || 
           event.getStatus().equals(PCCClient.ClientStatus.FAILED_LOGIN)){
            //// Future use
        } else {
            /// Future use
        }
        switch(event.getStatus()){
            case BUSY_LOGIN:
                loadedItems = 5;
                setMessage("Logging in");
            break;
            case LOGGED_IN:
                loadedItems = 6;
                setMessage("Logged in");
                startAssetsPreloading();
            break;
            case LOGGED_OUT:
                setMessage("Logged out");
            break;
            case BUSY_LOGOUT:
                setMessage("Busy logging out");
            break;
            case FAILED_LOGIN:
                String addendum = "";
                switch(event.getErrorCode()){
                    case 401:
                        addendum = " If name taken and this is the correct client reset it on the server and try again.";
                    break;
                }
                setMessage("Login failed " + event.getMessage() + " ("+event.getErrorCode()+addendum+")");
                showLoginPopup(true);
            break;
            case NO_DATA:
                setMessage("No login known, give username and password or set data in config file and restart.");
                showLoginPopup(true);
            break;
            case INIT_DONE:
                loadedItems = 7;
                setMessage("Init done");
            break;
            case INIT_ERROR:
                setMessage("Unable to handle server provided resources. hard fail, details in log and press esc to exit");
            break;
        }
    }
    
    @Override
    public void handlePCCConnectionEvent(PCCConnection.PCCConnectionStatus status, PCCConnectionEvent event) {
        switch(status){
            case NOT_FOUND:
                setMessage("Server not found, if using IPv6 or server broadcast is disabled, connect manual with an IPv4 address.");
                showConnectPopup(true);
            break;
            case UNAVAILABLE:
                setMessage("Not finding a method to connect, make sure network is available. Press escape to exit");
            break;
            case FOUND:
                loadedItems = 2;
                setMessage("Server found at: " + event.getRemoteSocketAddress()+ ", using port: " + event.getRemoteSocketPort());
            break;
            case CONNECTING:
                loadedItems = 3;
                setMessage("Connecting to: " + event.getRemoteSocketAddress() + ", using port: " + event.getRemoteSocketPort());
            break;
            case CONNECTED:
                loadedItems = 4;
                setMessage("Connected to: " + event.getRemoteSocketAddress() + ", using port: " + event.getRemoteSocketPort());
                new Thread(() -> { this.clientSystem.getClient().login(); }).start();
            break;
            case DISCONNECTED:
                setMessage("Disconnected from: " + event.getRemoteSocketAddress() + ", using port: " + event.getRemoteSocketPort());
            break;
            case CONNECT_FAILED:
                setMessage("Connection failed with: " + event.getRemoteSocketAddress() + ", using port: " + event.getRemoteSocketPort() + ". Check connect settings");
                showConnectPopup(true);
            break;
        }
    }
    
    @Override
    public boolean keyDown(int i) {
        if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
            if(clientSystem.getClient().isloggedIn()){
                clientSystem.getClient().logout();
            }
            Gdx.app.exit();
            return false;
        }
        return true;
    }

    @Override
    public boolean keyUp(int i) {
        return true;
    }

    @Override
    public boolean keyTyped(char c) {
        return true;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        return true;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return true;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return true;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return true;
    }

    @Override
    public boolean scrolled(int i) {
        return true;
    }
    
}