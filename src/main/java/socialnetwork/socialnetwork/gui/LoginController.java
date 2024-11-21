package socialnetwork.socialnetwork.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import socialnetwork.socialnetwork.domain.User;
import socialnetwork.socialnetwork.domain.validators.UserValidator;
import socialnetwork.socialnetwork.repository.FriendshipRepoDB;
import socialnetwork.socialnetwork.repository.UserRepoDB;
import socialnetwork.socialnetwork.service.FriendshipService;
import socialnetwork.socialnetwork.service.UserService;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button signUpButton;

    private UserService userService = new UserService(new UserRepoDB(), new UserValidator());
    private FriendshipService fr = new FriendshipService(new FriendshipRepoDB(), new UserRepoDB());

    public LoginController() throws SQLException {
    }

    @FXML
    private void handleLoginOrSignUp() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            User user = userService.signIn(username, password);
            if (user != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/socialnetwork/socialnetwork/main.fxml"));
                Parent root = loader.load();
                MainController controller = loader.getController();
                controller.setUserService(userService, fr, user.getUsername());
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root, 600, 400));
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid username or password.");
            }
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "An error occurred: " + ex.getMessage());
        }
    }

    @FXML
    private void handleSignUp() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            boolean success = userService.signUp(username, password);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Sign up successful! Please log in.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Username already exists.");
            }
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "An error occurred: " + ex.getMessage());
        }
    }

    @FXML
    private void handleExit() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }
}