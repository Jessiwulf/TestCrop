package se233.project1.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import se233.project1.model.Cropping.ImageCrop;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static se233.project1.Launcher.primaryStage;

public class AllCustomHandler {
    @FXML
    private Button chooseFileBtn, generateBtn, saveBtn, nextBtn, prevBtn;
    @FXML
    private ImageView preImage, postImage;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label currentFile, fileNumber, currentFileName;
    @FXML
    private ToggleButton edgeBtn, cropBtn,prewittBtn,robertsCrossBtn,laplacianBtn;
    @FXML
    private VBox edgeParamsPane;
    @FXML
    private HBox matrixParamsPane,strengthParamsPane;
    @FXML
    private StackPane parentPane;
    @FXML
    private Group imageLayer; // assuming this is the Group containing the image

    private File currentFilePath;
    private ImageProcessing imageProcessingService;
    private ZipHandler zipHandler;
    private String currentMode,currentEdgeMode;
    private Rectangle cropBox; // The cropping rectangle
    private boolean resizing; // Flag to check if resizing is active
    private double initialWidth; // Initial width of the crop box during resize
    private double initialHeight; // Initial height of the crop box during resize
    private double initialX; // Initial x position during resize
    private double initialY; // Initial y position during resize
    private List<Image> processedImagesList = new ArrayList<>();
    private boolean isGenerate;
    private ImageCrop imageCrop;

    @FXML
    public void initialize() {
        imageProcessingService = new ImageProcessing();
        zipHandler = new ZipHandler();
        nextBtn.setDisable(true);
        prevBtn.setDisable(true);
        saveBtn.setVisible(false);
        // เริ่มการทำงานของ ImageCrop โดยใช้ preImage และ imageLayer จาก FXML
        imageCrop = new ImageCrop(preImage, imageLayer);

        cropBox = new Rectangle(100, 100); // Initial size
        cropBox.setFill(Color.TRANSPARENT); // Make it transparent
        cropBox.setStroke(Color.BLACK); // Set border color
        cropBox.setStrokeWidth(2); // Set border thickness

        // Add crop box to the parent pane
        parentPane.getChildren().add(cropBox);

        // Enable dragging and resizing functionality for the crop box
        //enableCropBoxDragging();
        //enableCropBoxResizing();
        // setMode("crop");
        setEdgeMode("prewitt");

        // Handle file selection
        chooseFileBtn.setOnAction(event -> chooseFile());

        // ปุ่ม generate จะเรียกใช้การประมวลผล (crop หรือ edge detection)
        generateBtn.setOnAction(event -> generateImage());
    }

    private void enableCropBoxDragging() {
        cropBox.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                cropBox.setUserData(new double[]{event.getSceneX(), event.getSceneY()});
            }
        });

        cropBox.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                double[] userData = (double[]) cropBox.getUserData();
                double deltaX = event.getSceneX() - userData[0];
                double deltaY = event.getSceneY() - userData[1];
                cropBox.setX(cropBox.getX() + deltaX);
                cropBox.setY(cropBox.getY() + deltaY);
                cropBox.setUserData(new double[]{event.getSceneX(), event.getSceneY()});
            }
        });
    }

    private void enableCropBoxResizing() {
        // Adding a resize handle to the crop box
        Rectangle resizeHandle = new Rectangle(10, 10);
        resizeHandle.setFill(Color.RED); // Color for the resize handle
        resizeHandle.setX(cropBox.getX() + cropBox.getWidth() - 10); // Position the handle
        resizeHandle.setY(cropBox.getY() + cropBox.getHeight() - 10);

        // Set event for resizing
        resizeHandle.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                initialWidth = cropBox.getWidth();
                initialHeight = cropBox.getHeight();
                initialX = event.getSceneX();
                initialY = event.getSceneY();
                resizing = true; // Start resizing
            }
        });

        resizeHandle.setOnMouseDragged(event -> {
            if (resizing) {
                double newWidth = initialWidth + (event.getSceneX() - initialX);
                double newHeight = initialHeight + (event.getSceneY() - initialY);

                if (newWidth > 20 && newHeight > 20) { // Minimum size
                    cropBox.setWidth(newWidth);
                    cropBox.setHeight(newHeight);
                    resizeHandle.setX(cropBox.getX() + newWidth - 10);
                    resizeHandle.setY(cropBox.getY() + newHeight - 10);
                }
            }
        });

        resizeHandle.setOnMouseReleased(event -> {
            resizing = false; // Stop resizing
        });

        // Add the resize handle to the parent pane
        parentPane.getChildren().add(resizeHandle);
    }

    //file handler
    @FXML
    public void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != preImage && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    @FXML
    public void handleDrop(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;

        if (db.hasFiles()) {
            success = true;
            File file = db.getFiles().get(0);
            loadFile(file);
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void loadFile(File file) {
        String fileType = getFileType(file);

        if ("image".equals(fileType)) {
            loadImage(file);
            updateFileNumber(1, 1); // Assuming single image loaded
        } else if ("zip".equals(fileType)) {
            loadZipFile(file);
            updateFileNumber(zipHandler.getImageCount(), zipHandler.getCurrentIndex() + 1); // Update to reflect total images and current index
            prevBtn.setDisable(false);
            nextBtn.setDisable(false);
        } else {
            showError("Please drop an image file (JPEG, PNG, BMP) or a ZIP file.");
        }
    }

    private void loadImage(File file) {
        try {
            Image image = new Image(file.toURI().toString());
            preImage.setImage(image);
            currentFilePath = file;
            updateCurrentFileNameLabel();
        } catch (Exception e) {
            showError("Failed to load the image. Please try again.");
        }
    }

    private void loadZipFile(File file) {
        try {
            zipHandler.loadZipFile(file);
            showImage(zipHandler.getCurrentImage());
            updateFileNumber(zipHandler.getImageCount(), zipHandler.getCurrentIndex() + 1); // Update current image index
            currentFilePath = file;
            updateCurrentFileLabel();
        } catch (Exception e) {
            showError("Failed to load the ZIP file. Please ensure it contains valid images.");
        }
    }

    private void updateCurrentFileLabel(){
        if(zipHandler != null && zipHandler.getCurrentFile() != null){
            currentFile.setText(zipHandler.getCurrentFile());
        }
    }
    private void updateCurrentFileNameLabel() {
        if (currentFilePath != null) {
            // If it's a normal file, display its name
            currentFileName.setText(currentFilePath.getName());
        }  else {
            // In case there is no file, set a default message
            currentFileName.setText("No file selected");
        }

        // Update the file number based on ZIP or single image
        if (zipHandler != null && zipHandler.getImageCount() > 0) {
            fileNumber.setText((zipHandler.getCurrentIndex() + 1) + "/" + zipHandler.getImageCount());
        } else {
            fileNumber.setText("1/1");
        }
    }

    private boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".bmp");
    }

    @FXML
    public void handleChooseFileAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image or ZIP File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.bmp"),
                new FileChooser.ExtensionFilter("ZIP Files", "*.zip")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            loadFile(file);
        }
    }

    private void updateFileNumber(int totalFiles, int currentIndex) {
        fileNumber.setText(currentIndex + "/" + totalFiles);
    }

    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.jpeg"),
                new FileChooser.ExtensionFilter("ZIP files", "*.zip")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            loadFile(selectedFile);
        }
    }

    private void showImage(Image image) {
        preImage.setImage(image);
        saveBtn.setVisible(false); // Hide save button until processing is done
    }

    @FXML
    private void generateImage() {
        Image processedImage = null;
        if ("edge".equals(currentMode)) {
            processedImage=applyEdgeDetection(preImage.getImage(),currentEdgeMode);
            saveBtn.setVisible(true);
            if(isZipFile(currentFilePath)){
                processAllImageWithEdgeDetection();
            }
        } else if ("crop".equals(currentMode)) {
            imageCrop.cropImage(primaryStage);  // เรียกใช้ crop จากคลาส ImageCrop
            processedImage = preImage.getImage();  // ภาพที่ถูก crop จะแสดงใน preImage
        }

        postImage.setImage(processedImage);
        isGenerate = true;
        saveBtn.setVisible(true);
    }

    private Image applyEdgeDetection(Image image, String edgeMode) {
        switch (edgeMode) {
            case "prewitt":
                return imageProcessingService.prewittProcessingImage(image);
            case "roberts":
                return imageProcessingService.robertProcessingImage(image);
            case "laplacian":
                return imageProcessingService.laplacianProcessingImage(image);
            default:
                return image;
        }
    }
    private boolean isZipFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".zip");
    }

    private void processAllImageWithEdgeDetection(){
        processedImagesList.clear();
        // Load ZIP file and clear previously processed images
        List<Image> imgInZip = zipHandler.getImagesInZip();
        try {
            for (int i = 0; i < zipHandler.getImageCount(); i++) {
                Image originalImage = imgInZip.get(i);
                // Apply the edge detection to each image
                Image processedImage = applyEdgeDetection(originalImage, currentEdgeMode);
                processedImagesList.add(processedImage);
            }
            // Set flag to indicate that images have been processed
            isGenerate = true;
            // If on the last image after processing, enable the previous button
            updateNavigationButtons();
        } catch (Exception e) {
            showError("Failed to process images in the ZIP file.");
        }
    }

    @FXML
    private void saveImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                new FileChooser.ExtensionFilter("JPG Files", "*.jpg"),
                new FileChooser.ExtensionFilter("JPEG files", "*.jpeg")
        );
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                BufferedImage bufferedImage = convertToBufferedImage(postImage.getImage());
                FileChooser.ExtensionFilter selectedExtension = fileChooser.getSelectedExtensionFilter();
                String format = "png"; // set png as default

                if (selectedExtension != null) {
                    if (selectedExtension.getDescription().contains("JPG")) {
                        format = "jpg";
                    }
                    else if (selectedExtension.getDescription().contains("JPEG")) {
                        format = "jpeg";
                    }
                }
                // save file as selected format
                ImageIO.write(bufferedImage, format, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public BufferedImage convertToBufferedImage(Image fxImage) {
        return SwingFXUtils.fromFXImage(fxImage, null);
    }
    //button handler
    @FXML
    private void handleEdgeButtonAction(ActionEvent event) {
        if (edgeBtn.isSelected()) {
            setMode("edge");
            cropBtn.setSelected(false);
        }
    }
    @FXML
    private void handlePrewittButtonAction(ActionEvent event) {
        if(prewittBtn.isSelected()) {
            setEdgeMode("prewitt");
            robertsCrossBtn.setSelected(false);
            laplacianBtn.setSelected(false);
        }
    }
    @FXML
    private void handleRobertsCrossButtonAction(ActionEvent event) {
        if(robertsCrossBtn.isSelected()) {
            setEdgeMode("roberts");
            laplacianBtn.setSelected(false);
            prewittBtn.setSelected(false);
        }
    }
    @FXML
    private void handleLaplacianButtonAction(ActionEvent event) {
        if(laplacianBtn.isSelected()) {
            setEdgeMode("laplacian");
            prewittBtn.setSelected(false);
            robertsCrossBtn.setSelected(false);
        }
    }

//    @FXML
//    private void handleCropButtonAction(ActionEvent event) {
//        if (cropBtn.isSelected()) {
//            setMode("crop");
//            edgeBtn.setSelected(false);
//        }
//    }

    @FXML
    private void handleCropButtonAction(ActionEvent event) {
        if (cropBtn.isSelected()) {
            // เปลี่ยนไปใช้โหมดครอป
            setMode("crop");
            // เริ่มการครอปภาพจากคลาส ImageCrop
            imageCrop.cropImage(primaryStage);
        }
    }

    private void setMode(String mode) {
        this.currentMode = mode;
        refresh();

        if ("edge".equals(mode)) {
            edgeParamsPane.setVisible(true);
            cropBox.setVisible(false);
        } else {
            edgeParamsPane.setVisible(false);
            cropBox.setVisible(true);
        }
    }

    private void setEdgeMode(String mode) {
        this.currentEdgeMode = mode;
    }

    @FXML
    private void navigateNext() throws IndexOutOfBoundsException {
        if (zipHandler.hasNext()) {
            // Load the next image from the zip
            Image nextImage = zipHandler.getNextImage();
            showImage(nextImage); // Show original image
            // Get the current index from zipHandler
            int currentIndex = zipHandler.getCurrentIndex();
            // If processed images exist, show the processed image at this index
            if (currentIndex < processedImagesList.size()) {
                postImage.setImage(processedImagesList.get(currentIndex));
            }

            saveBtn.setVisible(isGenerate);
            updateNavigationButtons();
            updateCurrentFileNameLabel();
            updateCurrentFileLabel();
        }
    }

    @FXML
    private void navigatePrevious() throws IndexOutOfBoundsException {
        if (zipHandler.hasPrevious()) {
            // Load the previous image from the zip
            Image previousImage = zipHandler.getPreviousImage();
            showImage(previousImage); // Show original image
            // Get the current index from zipHandler
            int currentIndex = zipHandler.getCurrentIndex();
            // If processed images exist, show the processed image at this index
            if (currentIndex < processedImagesList.size()) {
                postImage.setImage(processedImagesList.get(currentIndex));
            }

            saveBtn.setVisible(isGenerate);
            updateNavigationButtons();
            updateCurrentFileNameLabel();
            updateCurrentFileLabel();
        }
    }


    private void updateNavigationButtons() {
        nextBtn.setDisable(!zipHandler.hasNext());
        prevBtn.setDisable(!zipHandler.hasPrevious());
    }

    private void refresh() {
        preImage.setImage(null);
        postImage.setImage(null);
        currentFileName.setText("No file chosen");
        fileNumber.setText("0/0");
        progressBar.setProgress(0);
        saveBtn.setVisible(false);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private String getFileType(File file) {
        if (file.getName().toLowerCase().endsWith(".zip")) {
            return "zip";
        } else if (isImageFile(file)) {
            return "image";
        } else {
            return "invalid";
        }
    }

}
