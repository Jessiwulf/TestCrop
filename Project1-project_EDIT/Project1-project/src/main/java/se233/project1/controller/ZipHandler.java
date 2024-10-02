package se233.project1.controller;
import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipHandler {

    private List<Image> imagesInZip; // List to hold images extracted from the zip
    private List<String> fileNamesInZip;// List to hold file name extracted from the zip
    private int currentIndex;

    public ZipHandler() {
        imagesInZip = new ArrayList<>();
        fileNamesInZip = new ArrayList<>();
        currentIndex = -1; // Start at -1 to indicate no images loaded yet
    }

    public void loadZipFile(File zipFile) {
        imagesInZip.clear(); // Clear any previous images
        fileNamesInZip.clear();
        currentIndex = -1; // Reset index

        try (ZipInputStream zis = new ZipInputStream(new java.io.FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // Check if the entry is an image
                if (isImageFile(entry.getName())) {
                    BufferedImage bufferedImage = ImageIO.read(zis);
                    if (bufferedImage != null) {
                        // Convert BufferedImage to JavaFX Image
                        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                        imagesInZip.add(image);
                    }
                }
                zis.closeEntry();
            }

            // Set currentIndex to the first image if any images were loaded
            if (!imagesInZip.isEmpty()) {
                currentIndex = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Optionally, handle the error more gracefully (e.g., show an alert)
        }
    }

    public Image getCurrentImage() {
        if (currentIndex >= 0 && currentIndex < imagesInZip.size()) {
            return imagesInZip.get(currentIndex);
        }
        return null; // Return null if no image is available
    }
    public String getCurrentFile() {
        if (currentIndex >= 0 && currentIndex < fileNamesInZip.size()) {
            return fileNamesInZip.get(currentIndex);
        }
        return null; // Return null if no file is available
    }

    public Image getNextImage() {
        if (hasNext()) {
            currentIndex++;
        }
        return getCurrentImage();
    }

    public Image getPreviousImage() {
        if (hasPrevious()) {
            currentIndex--;
        }
        return getCurrentImage();
    }

    public boolean hasNext() {
        return currentIndex < imagesInZip.size() - 1;
    }

    public boolean hasPrevious() {
        return currentIndex > 0;
    }
    public int getImageCount(){
        return imagesInZip.size();
    }
    public int getCurrentIndex() {
        return currentIndex;
    }

    public List<Image> getImagesInZip() {
        return imagesInZip;
    }

    private boolean isImageFile(String fileName) {
        String lowerCaseFileName = fileName.toLowerCase();
        return lowerCaseFileName.endsWith(".jpg") ||
                lowerCaseFileName.endsWith(".jpeg") ||
                lowerCaseFileName.endsWith(".png") ||
                lowerCaseFileName.endsWith(".bmp");
    }
}
