package se233.project1.model.Cropping;

import java.awt.image.BufferedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;


/**
 * Load image, provide rectangle for rubberband selection. Press right mouse button for "crop" context menu which then crops the image at the selection rectangle and saves it as jpg.
 */
public class ImageCrop {

    @FXML
    private ImageView preImage, postImage;
    @FXML
    private Group imageLayer; // assuming this is the Group containing the image

    private final RubberBandSelection rubberBandSelection;
    private final ImageView imageView;

    public ImageCrop(ImageView imageView, Group imageLayer) {
        this.imageView = imageView;
        this.rubberBandSelection = new RubberBandSelection(imageLayer);
    }

    public void cropImage(Stage primaryStage) {
        Bounds selectionBounds = rubberBandSelection.getBounds();
        if (selectionBounds.isEmpty()) {
            System.out.println("No area selected for cropping.");
            return;
        }
        Image inputImage = preImage.getImage();

        //processCropImage(inputImage);
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Save Image");
//        File file = fileChooser.showSaveDialog(primaryStage);
//        if (file == null) return;

        int width = (int) selectionBounds.getWidth();
        int height = (int) selectionBounds.getHeight();

        WritableImage wi = new WritableImage(width, height);
        imageView.snapshot(new SnapshotParameters(), wi);

//        saveImage(wi, file);
    }

    public void processCropImage(Image inputImage, double cropX, double cropY, double cropWidth, double cropHeight) {
        BufferedImage bufferedImage = convertToBufferedImage(inputImage);

        // Get the width and height of the original image
        double imageWidth = inputImage.getWidth();
        double imageHeight = inputImage.getHeight();

        // Get the width and height of the ImageView (preImage)
        double viewWidth = preImage.getFitWidth();
        double viewHeight = preImage.getFitHeight();

        // Calculate the scale factors for X and Y based on how the image is resized in the ImageView
        double scaleX = imageWidth / viewWidth;
        double scaleY = imageHeight / viewHeight;

        // Adjust the crop coordinates according to the scaling factors
        int adjustedX = (int) (cropX * scaleX);
        int adjustedY = (int) (cropY * scaleY);
        int adjustedWidth = (int) (cropWidth * scaleX);
        int adjustedHeight = (int) (cropHeight * scaleY);

        // Ensure the adjusted dimensions are within the bounds of the original image
        adjustedWidth = Math.min(adjustedWidth, bufferedImage.getWidth() - adjustedX);
        adjustedHeight = Math.min(adjustedHeight, bufferedImage.getHeight() - adjustedY);

        // Crop the image
        BufferedImage croppedImage = bufferedImage.getSubimage(adjustedX, adjustedY, adjustedWidth, adjustedHeight);

        SwingFXUtils.toFXImage(croppedImage, null);
    }

    public BufferedImage convertToBufferedImage(Image fxImage) {
        return SwingFXUtils.fromFXImage(fxImage, null);
    }


//    private void saveImage(WritableImage wi, File file) {
//        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(wi, null);
//        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(), BufferedImage.OPAQUE);
//
//        Graphics2D graphics = bufImageRGB.createGraphics();
//        graphics.drawImage(bufImageARGB, 0, 0, null);
//
//        try {
//            ImageIO.write(bufImageRGB, "jpg", file);
//            System.out.println("Image saved to " + file.getAbsolutePath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        graphics.dispose();
//    }

    /**
     * Drag rectangle with mouse cursor in order to get selection bounds
     */
    public static class RubberBandSelection {

        final DragContext dragContext = new DragContext();
        Rectangle rect = new Rectangle();

        Group group;

        public DragContext getDragContext() {
            return dragContext;
        }

        public Rectangle getRect() {
            return rect;
        }

        public void setRect(Rectangle rect) {
            this.rect = rect;
        }

        public Group getGroup() {
            return group;
        }

        public void setGroup(Group group) {
            this.group = group;
        }

        public EventHandler<MouseEvent> getOnMousePressedEventHandler() {
            return onMousePressedEventHandler;
        }

        public void setOnMousePressedEventHandler(EventHandler<MouseEvent> onMousePressedEventHandler) {
            this.onMousePressedEventHandler = onMousePressedEventHandler;
        }

        public EventHandler<MouseEvent> getOnMouseDraggedEventHandler() {
            return onMouseDraggedEventHandler;
        }

        public void setOnMouseDraggedEventHandler(EventHandler<MouseEvent> onMouseDraggedEventHandler) {
            this.onMouseDraggedEventHandler = onMouseDraggedEventHandler;
        }

        public EventHandler<MouseEvent> getOnMouseReleasedEventHandler() {
            return onMouseReleasedEventHandler;
        }

        public void setOnMouseReleasedEventHandler(EventHandler<MouseEvent> onMouseReleasedEventHandler) {
            this.onMouseReleasedEventHandler = onMouseReleasedEventHandler;
        }

        public Bounds getBounds() {
            return rect.getBoundsInParent();
        }

        public RubberBandSelection( Group group) {

            this.group = group;

            rect = new Rectangle( 0,0,0,0);
            rect.setStroke(Color.BLUE);
            rect.setStrokeWidth(1);
            rect.setStrokeLineCap(StrokeLineCap.ROUND);
            rect.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));

            group.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
            group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
            group.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);

        }

        EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                if( event.isSecondaryButtonDown())
                    return;

                // remove old rect
                rect.setX(0);
                rect.setY(0);
                rect.setWidth(0);
                rect.setHeight(0);

                group.getChildren().remove( rect);


                // prepare new drag operation
                dragContext.mouseAnchorX = event.getX();
                dragContext.mouseAnchorY = event.getY();

                rect.setX(dragContext.mouseAnchorX);
                rect.setY(dragContext.mouseAnchorY);
                rect.setWidth(0);
                rect.setHeight(0);

                group.getChildren().add( rect);

            }
        };

        EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                if( event.isSecondaryButtonDown())
                    return;

                double offsetX = event.getX() - dragContext.mouseAnchorX;
                double offsetY = event.getY() - dragContext.mouseAnchorY;

                if( offsetX > 0)
                    rect.setWidth( offsetX);
                else {
                    rect.setX(event.getX());
                    rect.setWidth(dragContext.mouseAnchorX - rect.getX());
                }

                if( offsetY > 0) {
                    rect.setHeight( offsetY);
                } else {
                    rect.setY(event.getY());
                    rect.setHeight(dragContext.mouseAnchorY - rect.getY());
                }
            }
        };


        EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                if( event.isSecondaryButtonDown())
                    return;

                // remove rectangle
                // note: we want to keep the ruuberband selection for the cropping => code is just commented out
                /*
                rect.setX(0);
                rect.setY(0);
                rect.setWidth(0);
                rect.setHeight(0);

                group.getChildren().remove( rect);
                */

            }
        };
        private static final class DragContext {

            public double mouseAnchorX;
            public double mouseAnchorY;


        }
    }

}