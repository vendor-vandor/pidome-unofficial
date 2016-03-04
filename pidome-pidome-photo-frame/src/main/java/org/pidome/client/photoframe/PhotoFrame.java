package org.pidome.client.photoframe;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.system.PCCSystem;

public class PhotoFrame {
    
    private static PCCSystem system = new PCCSystem(LocalPathResolver.getLocalBasePath());
    private static LwjglApplication lwjglApplication;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        addShutdownHook();
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        lwjglApplication = new LwjglApplication(new RaspberryMirror(system), config);
        FrameSettings.setPreferences(system.getPreferences());
    }
    
    private static void addShutdownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    system.getLocalSettings().save("Automatic save");
                } catch (IOException ex) {
                    Logger.getLogger(PhotoFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
}
