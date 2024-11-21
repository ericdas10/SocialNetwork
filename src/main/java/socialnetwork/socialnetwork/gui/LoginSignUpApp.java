package socialnetwork.socialnetwork.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class LoginSignUpApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/socialnetwork/socialnetwork/login.fxml")));
        primaryStage.setTitle("Login / Sign Up");
        primaryStage.setScene(new Scene(root, 550, 500));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}