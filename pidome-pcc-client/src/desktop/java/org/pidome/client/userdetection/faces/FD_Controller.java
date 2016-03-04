/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.userdetection.faces;

import org.pidome.client.video.capture.faces.MatchResult;
import org.pidome.client.video.capture.faces.BuildEigenFaces;
import org.pidome.client.video.capture.faces.recognition.FaceRecognition;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.pidome.client.tools.ImgTools;
import org.pidome.client.video.capture.faces.recognition.FaceRecognitionException;

/**
 *
 * @author John
 */
public class FD_Controller {

    // FXML buttons
    @FXML
    private Button cameraButton;
    // the FXML area for showing the current frame
    @FXML
    private Button saveImageButton;
    @FXML
    private TextField userImageName;
    @FXML
    private ImageView originalFrame;
    @FXML
    private ImageView faceFrame;
    
    @FXML
    private CheckBox haarClassifier;
    // checkbox for selecting the LBP Classifier
    @FXML
    private CheckBox lbpClassifier;
    @FXML
    private Button convertAndLearn;

    private String faceName = "";

    private boolean saveImage = false;
    private boolean savingImage = false;

    private static final int FACE_WIDTH = 125;
    private static final int FACE_HEIGHT = 150;
    private static final String FACE_DIR = "savedFaces";

    // a timer for acquiring the video stream
    private Timer timer;
    // the OpenCV object that performs the video capture
    private VideoCapture capture;
    // a flag to change the button behavior
    private boolean cameraActive;
    // the face cascade classifier object
    private CascadeClassifier faceCascade;
    // minimum face size
    private int absoluteFaceSize;
    private Image CamStream;

    // holds the coordinates of the highlighted face
    private Rectangle faceRect;

    FaceRecognition faceRecog;

    @FXML
    TextField foundName;

    boolean startDetection = false;
    
    /**
     * Init the controller variables
     */
    public final void init() {
        this.capture = new VideoCapture();
        this.faceCascade = new CascadeClassifier();
        this.absoluteFaceSize = 0;
    }

    /**
     * The action triggered by pushing the button on the GUI
     */
    @FXML
    protected void startCamera() {
        if (!this.cameraActive) {
            // disable setting checkboxes
            this.haarClassifier.setDisable(true);
            this.lbpClassifier.setDisable(true);

            // start the video capture
            this.capture.open(0);
            this.capture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, 320);
            this.capture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, 240);

            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                // grab a frame every 333 ms (+/- 3 frames/sec)
                TimerTask frameGrabber = new TimerTask() {
                    @Override
                    public void run() {
                        if (!savingImage) {
                            CamStream = grabFrame();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    // show the original frames
                                    originalFrame.setImage(CamStream);
                                    // set fixed width
                                    originalFrame.setFitWidth(600);
                                    // preserve image ratio
                                    originalFrame.setPreserveRatio(true);

                                }
                            });
                        }
                    }
                };
                this.timer = new Timer();
                this.timer.schedule(frameGrabber, 0, 1000);

                // update the button content
                this.cameraButton.setText("Stop Camera");
            } else {
                // log the error
                System.err.println("Failed to open the camera connection...");
            }
        } else {
            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            this.cameraButton.setText("Start Camera");
            // enable setting checkboxes
            this.haarClassifier.setDisable(false);
            this.lbpClassifier.setDisable(false);

            // stop the timer
            if (this.timer != null) {
                this.timer.cancel();
                this.timer = null;
            }
            // release the camera
            this.capture.release();
            // clean the image area
            originalFrame.setImage(null);
        }
    }

    /**
     * Get a frame from the opened video stream (if any)
     *
     * @return the {@link Image} to show
     */
    private Image grabFrame() {
        // init everything
        Image imageToShow = null;
        Mat frame = new Mat();

        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);

                // if the frame is not empty, process it
                if (!frame.empty()) {
                    // face detection
                    this.detectAndDisplay(frame);
                    // convert the Mat object (OpenCV) to Image (JavaFX)
                    imageToShow = mat2Image(frame);
                    if(startDetection){
                        matchClip(ImgTools.fromFXImage(imageToShow, null));
                    }
                    if (saveImage) {
                        savingImage = true;
                        clipSaveFace(imageToShow);
                        saveImage = false;
                    }
                    faceRect = null;
                }

            } catch (Exception e) {
                // log the (full) error
                System.err.print("ERROR");
                e.printStackTrace();
            }
        }

        return imageToShow;
    }

    /**
     * Perform face detection and show a rectangle around the detected face.
     *
     * @param frame the current frame
     */
    private void detectAndDisplay(Mat frame) {
        // init
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);

        // compute minimum face size (20% of the frame height)
        if (this.absoluteFaceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                this.absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        // detect faces
        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2,
                Objdetect.CASCADE_SCALE_IMAGE
                | Objdetect.CASCADE_DO_ROUGH_SEARCH
                | Objdetect.CASCADE_FIND_BIGGEST_OBJECT, new Size(
                        this.absoluteFaceSize, this.absoluteFaceSize), new Size());

        // each rectangle in faces is a face
        Rect[] facesArray = faces.toArray();

        if (facesArray.length == 1) {
            Point loc = facesArray[0].tl();
            Size size = facesArray[0].size();
            faceRect = new Rectangle();
            synchronized (faceRect) {
                faceRect.setRect(loc.x, loc.y, size.width, size.height);
            }
            Core.rectangle(frame, loc, facesArray[0].br(), new Scalar(0, 255, 0, 255), 2);
        }
    }

    /**
     * When the Haar checkbox is selected, deselect the other one and load the
     * proper XML classifier
     *
     */
    @FXML
    protected void haarSelected() {
        // check whether the lpb checkbox is selected and deselect it
        if (this.lbpClassifier.isSelected()) {
            this.lbpClassifier.setSelected(false);
        }
        this.faceCascade = new CascadeClassifier();
        this.checkboxSelection("lib/opencv/haarcascade_frontalface_alt.xml");
    }

    /**
     *
     * When the LBP checkbox is selected, deselect the other one and load the
     * proper XML classifier
     */
    @FXML
    protected void lbpSelected() {
        // check whether the haar checkbox is selected and deselect it
        if (this.haarClassifier.isSelected()) {
            this.haarClassifier.setSelected(false);
        }

        this.checkboxSelection("lib/opencv/lbpcascade_frontalface.xml");

    }

    @FXML
    protected void startSavingImage() {
        this.saveImage = true;
    }

    /**
     * Common operation for both checkbox selections
     *
     * @param classifierPath the absolute path where the XML file representing a
     * training set for a classifier is present
     */
    private void checkboxSelection(String... classifierPath) {
        // load the classifier(s)
        for (String xmlClassifier : classifierPath) {
            this.faceCascade.load(xmlClassifier);
        }

        // now the capture can start
        this.cameraButton.setDisable(false);
    }

    /**
     * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
     *
     * @param frame the {@link Mat} representing the current frame
     * @return the {@link Image} to show
     */
    private Image mat2Image(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Highgui.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the
        // buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    private void clipSaveFace(Image img) /* clip the image using the current face rectangle, and save it into fnm
     The use of faceRect is in a synchronized block since it may be being
     updated or used for drawing at the same time in other threads.
     */ {
        BufferedImage clipIm = null;
        synchronized (faceRect) {
            if (faceRect.width == 0) {
                System.out.println("No face selected");
                return;
            }
            img.getPixelReader();
            BufferedImage im = ImgTools.fromFXImage(img, null);

            try {
                clipIm = im.getSubimage(faceRect.x, faceRect.y, faceRect.width, faceRect.height);
            } catch (RasterFormatException e) {
                System.out.println("Could not clip the image");
            }
        }
        if (clipIm != null) {
            saveClip(clipIm);
        } else {
            System.out.println("Nothing to save");
        }
        savingImage = false;
    }  // end of clipSaveFace()

    private void saveClip(BufferedImage clipIm) /* resizes to at least a standard size, converts to grayscale, 
     clips to an exact size, then saves in a standard location */ {
        try {
            long startTime = System.currentTimeMillis();

            System.out.println("Saving clip for " + userImageName.getText() + " ...");
            BufferedImage grayIm = resizeImage(clipIm);
            BufferedImage faceIm = clipToFace(grayIm);
            File theDir = new File(FACE_DIR + "/" + userImageName.getText());
            System.out.println("Going to output to: " + theDir.getAbsolutePath() + ". Dir exists: " + theDir.exists());
            if (!theDir.exists()) {
                System.out.println("Creating dir");
                theDir.mkdirs();
                System.out.println("Dir exists: " + theDir.exists());
            }
            System.out.println();
            saveImage(faceIm,
                    FACE_DIR + "/"
                    + userImageName.getText() + "/"
                    + (theDir.listFiles().length + 1)
                    + ".png");
            System.out.println("  Save time: " + (System.currentTimeMillis() - startTime) + " ms");
        } catch (Exception ex) {
            System.out.println("Something went wrong: " + ex.getMessage());
            System.out.println(ex);
        }
    }  // end of saveClip()

    private void saveImage(BufferedImage im, String fnm) // save image in fnm
    {
        try {
            ImageIO.write(im, "png", new File(fnm));
            System.out.println("Saved image to " + fnm);
        } catch (IOException e) {
            System.out.println("Could not save image to " + fnm);
        }
    }  // end of saveImage()

    private BufferedImage resizeImage(BufferedImage im) // resize to at least a standard size, then convert to grayscale 
    {
        // resize the image so *at least* FACE_WIDTH*FACE_HEIGHT size
        int imWidth = im.getWidth();
        int imHeight = im.getHeight();
        System.out.println("Original (w,h): (" + imWidth + ", " + imHeight + ")");

        double widthScale = FACE_WIDTH / ((double) imWidth);
        double heightScale = FACE_HEIGHT / ((double) imHeight);
        double scale = (widthScale > heightScale) ? widthScale : heightScale;

        int nWidth = (int) Math.round(imWidth * scale);
        int nHeight = (int) Math.round(imHeight * scale);

        // convert to grayscale while resizing
        BufferedImage grayIm = new BufferedImage(nWidth, nHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2 = grayIm.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(im, 0, 0, nWidth, nHeight, 0, 0, imWidth, imHeight, null);
        g2.dispose();
        System.out.println("Scaled gray (w,h): (" + nWidth + ", " + nHeight + ")");
        return grayIm;
    }  // end of resizeImage()

    private BufferedImage clipToFace(BufferedImage im) // clip image to FACE_WIDTH*FACE_HEIGHT size
    // I assume the input image is face size or bigger
    {
        int xOffset = (im.getWidth() - FACE_WIDTH) / 2;
        int yOffset = (im.getHeight() - FACE_HEIGHT) / 2;
        BufferedImage faceIm = null;
        try {
            faceIm = im.getSubimage(xOffset, yOffset, FACE_WIDTH, FACE_HEIGHT);
            System.out.println("Clipped image to face dimensions: ("
                    + FACE_WIDTH + ", " + FACE_HEIGHT + ")");
        } catch (RasterFormatException e) {
            System.out.println("Could not clip the image");
            faceIm = im;
        }
        return faceIm;
    }  // end of clipToFace()

    @FXML
    protected void convertAndLearnImages() throws FaceRecognitionException {
        BuildEigenFaces.build(100);
        faceRecog = new FaceRecognition(3);
        startDetection = true;
    }

    private BufferedImage clipThisStuff(BufferedImage img){
        BufferedImage clipIm = null;
        synchronized (faceRect) {
            if (faceRect.width == 0) {
                System.out.println("No face selected");
                return null;
            }
            try {
                clipIm = img.getSubimage(faceRect.x, faceRect.y, faceRect.width, faceRect.height);
            } catch (RasterFormatException e) {
                System.out.println("Could not clip the image");
            }
        }
        return clipIm;
    }
    
    private Image toJavaFXImage(BufferedImage bf){
        WritableImage wr = null;
        if (bf != null) {
            wr = new WritableImage(bf.getWidth(), bf.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < bf.getWidth(); x++) {
                for (int y = 0; y < bf.getHeight(); y++) {
                    pw.setArgb(x, y, bf.getRGB(x, y));
                }
            }
        }
        return wr;
    }
    
    ////// Recognizing
    private void matchClip(BufferedImage clipIm) throws FaceRecognitionException // resize, convert to grayscale, clip to FACE_WIDTH*FACE_HEIGHT, recognize
    {
        if(faceRect!=null){
            long startTime = System.currentTimeMillis();

            System.out.println("Matching clip...");
            BufferedImage faceIm = clipToFace(resizeImage(clipThisStuff(clipIm)));
            faceFrame.setImage(toJavaFXImage(faceIm));
            MatchResult result = faceRecog.match(faceIm);
            if (result == null) {
                System.out.println("No match found");
                Platform.runLater(() -> {
                    foundName.setText("Who?");
                });
            } else if (result.getMatchDistance()>0){
                faceName = result.getName();
                String distStr = String.format("%.4f", result.getMatchDistance());
                System.out.println("  Matches " + result.getMatchFileName()
                        + "; distance = " + distStr);
                System.out.println("  Matched name: " + faceName);
                Platform.runLater(() -> {
                    foundName.setText(faceName + " (" + distStr + ")");
                });
            } else {
                Platform.runLater(() -> {
                    foundName.setText("Come closer!");
                });
            }
            System.out.println("Match time: " + (System.currentTimeMillis() - startTime) + " ms");
        } else {
                Platform.runLater(() -> {
                    foundName.setText("No Match");
                });
        }
    }  // end of matchClip()

}
