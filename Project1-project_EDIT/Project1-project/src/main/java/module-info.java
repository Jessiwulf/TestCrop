module se233.project1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires javafx.swing;


    opens se233.project1.controller to javafx.fxml;
    exports se233.project1;
}
