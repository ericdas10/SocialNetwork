module socialnetwork.socialnetwork {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens socialnetwork.socialnetwork.gui to javafx.fxml;
    exports socialnetwork.socialnetwork.gui;
}