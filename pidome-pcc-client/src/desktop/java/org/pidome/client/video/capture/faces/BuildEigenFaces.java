package org.pidome.client.video.capture.faces;

import java.awt.image.*;
import java.util.*;

/**
 * BuildEigenFaces.java
 * Sajan Joseph, sajanjoseph@gmail.com
 * http://code.google.com/p/javafaces/
 * Modified by Andrew Davison, April 2011, ad@fivedots.coe.psu.ac.th
 * Modified by John Sirach, Juli 18th 2015, john.sirach@pidome.org
 * 
 * Use the training face images in trainingImages\ to create eigenvector and eigenvalue,
 * information using PCA. The information is stored in eigen.cache.
 * Two subdirectories, eigenfaces\ and reconstructed\, are also generated which
 * contain eigenfaces and regenerated images. These are only produced to allow the
 * eigenface process to be checked. 
 *
 * Only eigen.ccache is used by the recognition process, which is separated out
 * into FaceRecognizer.java
 *
 * This code is a refactoring of the JavaFaces package by Sajan Joseph, available
 * at http://code.google.com/p/javafaces/ The current version includes a GUI.
 * 
 * Update (John): The in the end used functionalities does not implement a gui.
 * 
 * @author Sajan Joseph, Andrew Davison, John Sirach
 */
public final class BuildEigenFaces {

    /**
     * Build the Eigenfaces.
     * @param numEFs 
     */
    public static void build(int numEFs) {
        ArrayList<String> fnms = FileUtils.getTrainingFnms();
        int numIms = fnms.size();
        if ((numEFs < 1) || (numEFs >= numIms)) {
            System.out.println("Number of eigenfaces must be in range (1-" + (numIms - 1) + ")" + "; using " + (numIms - 1));
            numEFs = numIms - 1;
        } else {
            System.out.println("Number of eigenfaces: " + numEFs);
        }
        FaceBundle bundle = makeBundle(fnms);
        FileUtils.writeCache(bundle);
        reconstructIms(numEFs, bundle);  // optional: rebuild the original images from the bundle, comment out for faster function return.
    }

    /**
     * Create eigenvectors/eigenvalue bundle for the specified training image filenames, also save each eigenface (eigenvector) as an image file.
     * @param fnms
     * @return 
     */
    private static FaceBundle makeBundle(ArrayList<String> fnms){
        BufferedImage[] ims = FileUtils.loadTrainingIms(fnms);

        Matrix2D imsMat = convertToNormMat(ims);   // each row is a normalized image
        double[] avgImage = imsMat.getAverageOfEachColumn();
        imsMat.subtractMean();   // subtract mean face from each image (row)
        // each row now contains only distinguishing features from a training image 

        // calculate covariance matrix
        Matrix2D imsDataTr = imsMat.transpose();
        Matrix2D covarMat = imsMat.multiply(imsDataTr);

        // calculate Eigenvalues and Eigenvectors for covariance matrix
        EigenvalueDecomp egValDecomp = covarMat.getEigenvalueDecomp();
        double[] egVals = egValDecomp.getEigenValues();
        double[][] egVecs = egValDecomp.getEigenVectors();

        sortEigenInfo(egVals, egVecs);   // sort Eigenvectos and Eigenvariables

        Matrix2D egFaces = getNormEgFaces(imsMat, new Matrix2D(egVecs));

        System.out.println("\nSaving Eigenfaces as images...");
        FileUtils.saveEFIms(egFaces, ims[0].getWidth());
        System.out.println("Saving done\n");

        return new FaceBundle(fnms, imsMat.toArray(), avgImage,egFaces.toArray(), egVals, ims[0].getWidth(), ims[0].getHeight());
    }

    private static Matrix2D convertToNormMat(BufferedImage[] ims) /* convert array of  images into a matrix; each row is an image
     and the number of columns is the number of pixels in the image.
     The array is normalized.
     */ {
        int imWidth = ims[0].getWidth();
        int imHeight = ims[0].getHeight();

        int numRows = ims.length;
        int numCols = imWidth * imHeight;
        double[][] data = new double[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            ims[i].getData().getPixels(0, 0, imWidth, imHeight, data[i]);    // one image per row
        }
        Matrix2D imsMat = new Matrix2D(data);
        imsMat.normalise();
        return imsMat;
    }		// end of convertToNormMat()

    /**
     * calculate normalized Eigenfaces for the training images by multiplying the eigenvectors to the training images matrix
     * @param imsMat
     * @param egVecs
     * @return 
     */
    private static Matrix2D getNormEgFaces(Matrix2D imsMat, Matrix2D egVecs){
        Matrix2D egVecsTr = egVecs.transpose();
        Matrix2D egFaces = egVecsTr.multiply(imsMat);
        double[][] egFacesData = egFaces.toArray();
        for (double[] egFacesData1 : egFacesData) {
            double norm = Matrix2D.norm(egFacesData1); // get normal
            for (int col = 0; col < egFacesData1.length; col++) {
                egFacesData1[col] = egFacesData1[col] / norm;
            }
        }
        return new Matrix2D(egFacesData);
    } 

    /**
     * Sort the Eigenvalues and Eigenvectors arrays.
     * Sort the Eigenvalues and Eigenvectors arrays into descending order
     * by eigenvalue. Add them to a table so the sorting of the values adjusts the
     * corresponding vectors
     * @param egVals
     * @param egVecs 
     */
    private static void sortEigenInfo(double[] egVals, double[][] egVecs){
        Double[] egDvals = getEgValsAsDoubles(egVals);
        // create table whose key == eigenvalue; value == eigenvector
        HashMap<Double, double[]> table = new HashMap<>();
        for (int i = 0; i < egDvals.length; i++) {
            table.put(egDvals[i], getColumn(egVecs, i));
        }
        ArrayList<Double> sortedKeyList = sortKeysDescending(table);
        updateEgVecs(egVecs, table, egDvals, sortedKeyList);
        // use the sorted key list to update the Eigenvectors array

        // convert the sorted key list into an array
        Double[] sortedKeys = new Double[sortedKeyList.size()];
        sortedKeyList.toArray(sortedKeys);

        // use the sorted keys array to update the Eigenvalues array
        for (int i = 0; i < sortedKeys.length; i++) {
            egVals[i] = sortedKeys[i];
        }

    }

    /**
     * Convert double Eigenvalues to Double objects, suitable for HashMap keys
     * @param egVals
     * @return 
     */
    private static Double[] getEgValsAsDoubles(double[] egVals) {
        Double[] egDvals = new Double[egVals.length];
        for (int i = 0; i < egVals.length; i++) {
            egDvals[i] = egVals[i];
        }
        return egDvals;
    }

    /**
     * Return the vector in column col.
     * the Eigenvectors array is in column order (one vector per column);
     * @param vecs
     * @param col
     * @return 
     */
    private static double[] getColumn(double[][] vecs, int col){
        double[] res = new double[vecs.length];
        for (int i = 0; i < vecs.length; i++) {
            res[i] = vecs[i][col];
        }
        return res;
    }

    /**
     * Sort the keylist part of the HashMap into descending order
     * @param table
     * @return 
     */
    private static ArrayList<Double> sortKeysDescending(HashMap<Double, double[]> table){
        ArrayList<Double> keyList = Collections.list(Collections.enumeration(table.keySet()));
        Collections.sort(keyList, Collections.reverseOrder()); // largest first
        return keyList;
    }

    /**
     * Get vectors from the table in descending order of sorted key and update the original vectors array.
     */
    private static void updateEgVecs(double[][] egVecs, HashMap<Double, double[]> table, Double[] egDvals, ArrayList<Double> sortedKeyList){
        for (int col = 0; col < egDvals.length; col++) {
            double[] egVec = table.get(sortedKeyList.get(col));
            for (int row = 0; row < egVec.length; row++) {
                egVecs[row][col] = egVec[row];
            }
        }
    }

    /**
     * Reconstruct images from a facebundle.
     * @param numEFs
     * @param bundle 
     */
    private static void reconstructIms(int numEFs, FaceBundle bundle) {
        System.out.println("\nReconstructing training images...");

        Matrix2D egFacesMat = new Matrix2D(bundle.getEigenFaces());
        Matrix2D egFacesSubMat = egFacesMat.getSubMatrix(numEFs);

        Matrix2D egValsMat = new Matrix2D(bundle.getEigenValues(), 1);
        Matrix2D egValsSubMat = egValsMat.transpose().getSubMatrix(numEFs);

        double[][] weights = bundle.calcWeights(numEFs);
        double[][] normImgs = getNormImages(weights, egFacesSubMat, egValsSubMat);
        // the mean-subtracted (normalized) training images
        double[][] origImages = addAvgImage(normImgs, bundle.getAvgImage());
        // original training images = normalized images + average image
        FileUtils.saveReconIms2(origImages, bundle.getImageWidth());
        System.out.println("Reconstruction done\n");
    }

    /**
     * Create normalized training images.
     * Calculate weights x eigenfaces, which generates mean-normalized training images.
     * There is one image per row in the returned array
     * @param weights
     * @param egFacesSubMat
     * @param egValsSubMat
     * @return 
     */
    private static double[][] getNormImages(double[][] weights,Matrix2D egFacesSubMat, Matrix2D egValsSubMat){
        double[] egDValsSub = egValsSubMat.flatten();
        Matrix2D tempEvalsMat = new Matrix2D(weights.length, egDValsSub.length);
        tempEvalsMat.replaceRowsWithArray(egDValsSub);

        Matrix2D tempMat = new Matrix2D(weights);
        tempMat.multiplyElementWise(tempEvalsMat);

        Matrix2D normImgsMat = tempMat.multiply(egFacesSubMat);
        return normImgsMat.toArray();
    }

    /**
     * Add an averaged image to each normalized image.
     * add the average image to each normalized image (each row) and store in a new array. The result are the original training images; one per row
     * @param normImgs
     * @param avgImage
     * @return 
     */
    private static double[][] addAvgImage(double[][] normImgs, double[] avgImage){
        double[][] origImages = new double[normImgs.length][normImgs[0].length];
        for (int i = 0; i < normImgs.length; i++) {
            for (int j = 0; j < normImgs[i].length; j++) {
                origImages[i][j] = normImgs[i][j] + avgImage[j];
            }
        }
        return origImages;
    }
}