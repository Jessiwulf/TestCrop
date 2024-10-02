package se233.project1.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Rectangle;
import se233.project1.model.detector.LaplacianEdgeDetector;
import se233.project1.model.detector.PrewittEdgeDetector;
import se233.project1.model.detector.RobertsCrossEdgeDetector;

import java.awt.image.BufferedImage;
import java.io.File;

public class ImageProcessing {

   public Image prewittProcessingImage(Image inputImage) {
        if (inputImage == null) {
            System.out.println("Input image is null");
            return null;
        }
        try{
            BufferedImage bufferedImage = convertToBufferedImage(inputImage);
            WritableImage fxImage = null;
            PrewittEdgeDetector detector = new PrewittEdgeDetector(bufferedImage);
            fxImage = SwingFXUtils.toFXImage(detector.getEdgeDetectedImage(), null);
            return fxImage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
   }
   public Image robertProcessingImage(Image inputImage) {
        if (inputImage == null) {
            System.out.println("Input image is null");
            return null;
        }
        try {
            BufferedImage bufferedImage = convertToBufferedImage(inputImage);
            WritableImage fxImage = null;
            RobertsCrossEdgeDetector detector = new RobertsCrossEdgeDetector(bufferedImage);
            fxImage = SwingFXUtils.toFXImage(detector.getEdgeDetectedImage(), null);
            return fxImage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
   }
   public Image laplacianProcessingImage(Image inputImage) {
       if (inputImage == null) {
           System.out.println("Input image is null");
           return null;
       }
       try {
           BufferedImage bufferedImage = convertToBufferedImage(inputImage);
           WritableImage fxImage = null;
           LaplacianEdgeDetector detector = new LaplacianEdgeDetector(bufferedImage);
           fxImage = SwingFXUtils.toFXImage(detector.getEdgeDetectedImage(), null);
           return fxImage;
       } catch (Exception e) {
           e.printStackTrace();
       }
       return null;
   }




    public BufferedImage convertToBufferedImage(Image fxImage) {
        return SwingFXUtils.fromFXImage(fxImage, null);
    }
}
