/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.css.themes.lcd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.pidome.client.css.themes.DashboardTheme;

/**
 *
 * @author John
 */
public class LCDDashboardTheme extends DashboardTheme {

    String appliedScheme = "DEFAULT";
            
    @Override
    public List<CodeSupports> getCodeSupports() {
        return new ArrayList<CodeSupports>(){{ add (CodeSupports.BACKGOUND); }};
    }

    @Override
    public List<ThemeFeature> getThemeFeatures() {
        return new ArrayList<ThemeFeature>(){{ add (ThemeFeature.EMULATE_BRIGHTNESS); add ( ThemeFeature.DISPLAY_OPTIONS ); }};
    }
    
    @Override
    public String getCSSPath() {
        return "/org/pidome/client/css/themes/lcd/dashboard.css";
    }
    
    @Override
    public String getSetDisplayOption(){
        return appliedScheme;
    }
    
    @Override
    public void applyDisplayOptions(){
        switch(appliedScheme){
            case "WHITE_BLUE":
                this.getSceneLink().getStylesheets().add(getClass().getResource("/org/pidome/client/css/themes/lcd/whitebluelcd.css").toExternalForm());
            break;
            case "OLDSCHOOL_GREEN":
                this.getSceneLink().getStylesheets().add(getClass().getResource("/org/pidome/client/css/themes/lcd/greenlcd.css").toExternalForm());
            break;
            case "BLUEBLUE":
                this.getSceneLink().getStylesheets().add(getClass().getResource("/org/pidome/client/css/themes/lcd/blueblue.css").toExternalForm());
            break;
            case "SILVER_GREY":
                this.getSceneLink().getStylesheets().add(getClass().getResource("/org/pidome/client/css/themes/lcd/silvergrey.css").toExternalForm());
            break;
            default:
                this.getSceneLink().getStylesheets().add(getClass().getResource("/org/pidome/client/css/themes/lcd/defaultlcd.css").toExternalForm());
            break;
        }
    }
    
    @Override
    public void clearDisplayOptions(){
        switch(appliedScheme){
            case "WHITE_BLUE":
                this.getSceneLink().getStylesheets().remove(getClass().getResource("/org/pidome/client/css/themes/lcd/whitebluelcd.css").toExternalForm());
            break;
            case "OLDSCHOOL_GREEN":
                this.getSceneLink().getStylesheets().remove(getClass().getResource("/org/pidome/client/css/themes/lcd/greenlcd.css").toExternalForm());
            break;
            case "BLUEBLUE":
                this.getSceneLink().getStylesheets().remove(getClass().getResource("/org/pidome/client/css/themes/lcd/blueblue.css").toExternalForm());
            break;
            case "SILVER_GREY":
                this.getSceneLink().getStylesheets().remove(getClass().getResource("/org/pidome/client/css/themes/lcd/silvergrey.css").toExternalForm());
            break;
            default:
                this.getSceneLink().getStylesheets().remove(getClass().getResource("/org/pidome/client/css/themes/lcd/defaultlcd.css").toExternalForm());
            break;
        }
    }
    
    @Override
    public void setDisplayOption(String selectedOption){
        this.appliedScheme = selectedOption;
    }
    
    @Override
    public Map<String,String> getDisplayOptions(){
        Map optionList = new HashMap<>();
        optionList.put("DEFAULT", "General LCD");
        optionList.put("OLDSCHOOL_GREEN", "Hobbyist green");
        optionList.put("WHITE_BLUE", "White on blue");
        optionList.put("BLUEBLUE", "A lot of blue");
        optionList.put("SILVER_GREY", "Black on silver grey");
        return optionList;
    }
    
    @Override
    public final Image getBackGroundImage(double width, double height){
        Rectangle bgImage = new Rectangle(0, 0, width, height);
        Color mainBG;
        Color mainHigh;
        Color mainLow;
        switch(appliedScheme){
            case "OLDSCHOOL_GREEN":
                mainBG   = Color.rgb( 79, 121, 19);
                mainHigh = Color.rgb( 96, 169,   0);
                mainLow  = Color.rgb(120, 201,   2);
            break;
            case "WHITE_BLUE":
                mainBG   = Color.rgb( 38, 92, 132);
                mainHigh = Color.rgb( 53,  74, 104);
                mainLow  = Color.rgb( 27,  37,  65);
            break;
            case "BLUEBLUE":
                mainBG   = Color.rgb( 14, 24, 31);
                mainHigh = Color.rgb( 35, 35, 65);
                mainLow  = Color.rgb( 30, 30, 60);
            break;
            case "SILVER_GREY":
                mainBG   = Color.rgb( 178, 178, 178);
                mainHigh = Color.rgb( 255, 255, 255);
                mainLow  = Color.rgb( 196, 196, 196);
            break;
            default:
                mainBG   = Color.rgb(131, 133, 119);
                mainHigh = Color.rgb(176, 183, 167);
                mainLow  = Color.rgb(165, 174, 153);
            break;
        }
        bgImage.setFill(mainBG);
        ImageView grained = new ImageView(createNoiseImage(width, height, mainHigh, mainLow , 8));
        Group blend = new Group(
                bgImage,
                grained
        );
        Image img = blend.snapshot(null, null);
        return img;
    }
    
    /**
     * Creates a noise image.
     * This image can be used in conjunction with other images as overlay (like group blending).
     * @param WIDTH The width of the resulting image.
     * @param HEIGHT The height of the resulting image
     * @param DARK_COLOR The dark variation color.
     * @param BRIGHT_COLOR The light variation color
     * @param ALPHA_VARIATION_IN_PERCENT Alpha variation in the noise result.
     * @return Image noise.
     * @author Hansolo_ (https://twitter.com/hansolo , http://harmoniccode.blogspot.nl/)
     */
    private Image createNoiseImage(final double WIDTH, final double HEIGHT, final Color DARK_COLOR, final Color BRIGHT_COLOR, final double ALPHA_VARIATION_IN_PERCENT) {
        int width  = (int) WIDTH;
        int height = (int) HEIGHT;
        double alphaVariationInPercent      = clamp(0d, 100d, ALPHA_VARIATION_IN_PERCENT);
        final WritableImage IMAGE           = new WritableImage(width, height);
        final PixelWriter   PIXEL_WRITER    = IMAGE.getPixelWriter();
        final Random        BW_RND          = new Random();
        final Random        ALPHA_RND       = new Random();
        final double        ALPHA_START     = alphaVariationInPercent / 100 / 2;
        final double        ALPHA_VARIATION = alphaVariationInPercent / 100;        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final Color NOISE_COLOR = BW_RND.nextBoolean() == true ? BRIGHT_COLOR : DARK_COLOR;
                final double NOISE_ALPHA = clamp(0, 1, ALPHA_START + ALPHA_RND.nextDouble() * ALPHA_VARIATION);
                PIXEL_WRITER.setColor(x, y, Color.color(NOISE_COLOR.getRed(), NOISE_COLOR.getGreen(), NOISE_COLOR.getBlue(), NOISE_ALPHA));
            }
        }
        return IMAGE;
    }
    
    private static double clamp(final double MIN, final double MAX, final double VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
    }
    
}
