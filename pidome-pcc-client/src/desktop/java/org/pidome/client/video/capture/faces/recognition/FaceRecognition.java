package org.pidome.client.video.capture.faces.recognition;

import java.awt.image.*;
import java.util.*;
import org.pidome.client.video.capture.faces.FaceBundle;
import org.pidome.client.video.capture.faces.FileUtils;
import org.pidome.client.video.capture.faces.ImageDistanceInfo;
import org.pidome.client.video.capture.faces.ImageUtils;
import org.pidome.client.video.capture.faces.MatchResult;
import org.pidome.client.video.capture.faces.Matrix2D;


/**
 * FaceRecognition.java
 * Sajan Joseph, sajanjoseph@gmail.com
 * http://code.google.com/p/javafaces/
 * Modified by Andrew Davison, April 2011, ad@fivedots.coe.psu.ac.th
 * Modified by John Sirach, Juli 18th 2015, john.sirach@pidome.org
 *
 * Use the eigen.cache containing eigenfaces, eigenvalues, and training
 * image info to find the training image which most cloesly resembles 
 * an input image.
 * 
 * This code is a refactoring of the JavaFaces package by Sajan Joseph, available
 * at http://code.google.com/p/javafaces/ The current version includes a GUI.
 * 
 * Update (John): The in the end used functionalities does not implement a gui.
 * 
 * @author John
 */
public class FaceRecognition {

    private static final float FACES_FRAC = 0.75f;
    // default fraction of eigenfaces used in a match

    private FaceBundle bundle = null;
    private double[][] weights = null;    // training image weights
    private int numEFs = 0;     // number of eigenfaces to be used in the recognition

    public FaceRecognition() throws FaceRecognitionException {
        this(0);
    }

    
    public FaceRecognition(int numEigenFaces) throws FaceRecognitionException {
        bundle = FileUtils.readCache();
        if (bundle == null) {
            throw new FaceRecognitionException("You must build an Eigenfaces cache before any matching");
        }
        int numFaces = bundle.getNumEigenFaces();
        System.out.println("No of eigenFaces: " + numFaces);
        numEFs = numEigenFaces;
        if ((numEFs < 1) || (numEFs > numFaces - 1)) {
            numEFs = Math.round((numFaces - 1) * FACES_FRAC);     // set to less than max
            System.out.println("Number of matching eigenfaces must be in the range (1-" + (numFaces - 1) + ")" + "; using " + numEFs);
        } else {
            System.out.println("Number of eigenfaces: " + numEFs);
        }
        weights = bundle.calcWeights(numEFs);
    }

    /**
     * Match an image file against trained images.
     * @param imFnm
     * @return 
     * @throws org.pidome.client.video.capture.faces.recognition.FaceRecognitionException 
     */
    public MatchResult match(String imFnm) throws FaceRecognitionException {
        if (!imFnm.endsWith(".png")) {
            throw new FaceRecognitionException("Input image must be a PNG file");
        } else {
            System.out.println("Matching " + imFnm);
        }
        BufferedImage image = FileUtils.loadImage(imFnm);
        if (image == null) {
            return null;
        }
        return match(image);
    }

    /**
     * Match a buffered image against training images.
     * @param im
     * @return 
     * @throws org.pidome.client.video.capture.faces.recognition.FaceRecognitionException 
     */
    public MatchResult match(BufferedImage im) throws FaceRecognitionException{
        if (bundle == null) {
            throw new FaceRecognitionException("You must build an Eigenfaces cache before any matching");
        }
        return findMatch(im);
    }

    /**
     * Find a match from a given buffered image
     * @param im
     * @return 
     */
    private MatchResult findMatch(BufferedImage im) {
        double[] imArr = ImageUtils.createArrFromIm(im);    // change image into an array
        // convert array to normalized 1D matrix
        Matrix2D imMat = new Matrix2D(imArr, 1);
        imMat.normalise();
        imMat.subtract(new Matrix2D(bundle.getAvgImage(), 1));  // subtract mean image
        Matrix2D imWeights = getImageWeights(numEFs, imMat);
       // map image into eigenspace, returning its coordinates (weights);
       // limit mapping to use only numEFs eigenfaces

        double[] dists = getDists(imWeights);
        ImageDistanceInfo distInfo = getMinDistInfo(dists);
        // find smallest Euclidian distance between image and training image

        ArrayList<String> imageFNms = bundle.getImageFnms();
        String matchingFNm = imageFNms.get(distInfo.getIndex());
        // get the training image filename that is closest 

        double minDist = Math.sqrt(distInfo.getValue());

        return new MatchResult(matchingFNm, minDist);
    }

    /**
     * map image onto numEFs eigenfaces returning its weights.
     * (i.e. its coordinates in eigenspace)
     * @param numEFs
     * @param imMat
     * @return 
     */
    private Matrix2D getImageWeights(int numEFs, Matrix2D imMat) {
        Matrix2D egFacesMat = new Matrix2D(bundle.getEigenFaces());
        Matrix2D egFacesMatPart = egFacesMat.getSubMatrix(numEFs);
        Matrix2D egFacesMatPartTr = egFacesMatPart.transpose();
        return imMat.multiply(egFacesMatPartTr);
    }

    /**
     * Return an array of the sum of the squared Euclidian distance
     * between the input image weights and all the training image weights
     * @param imWeights
     * @return 
     */
    private double[] getDists(Matrix2D imWeights) {
        Matrix2D tempWt = new Matrix2D(weights);   // training image weights
        double[] wts = imWeights.flatten();

        tempWt.subtractFromEachRow(wts);
        tempWt.multiplyElementWise(tempWt);
        double[][] sqrWDiffs = tempWt.toArray();
        double[] dists = new double[sqrWDiffs.length];

        for (int row = 0; row < sqrWDiffs.length; row++) {
            double sum = 0.0;
            for (int col = 0; col < sqrWDiffs[0].length; col++) {
                sum += sqrWDiffs[row][col];
            }
            dists[row] = sum;
        }
        return dists;
    }

    /**
     * Return minimum distance info.
     * @param dists
     * @return 
     */
    private ImageDistanceInfo getMinDistInfo(double[] dists) {
        double minDist = Double.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < dists.length; i++) {
            if (dists[i] < minDist) {
                minDist = dists[i];
                index = i;
            }
        }
        return new ImageDistanceInfo(dists[index], index);
    }
}