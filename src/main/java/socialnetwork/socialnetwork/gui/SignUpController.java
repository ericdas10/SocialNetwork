package socialnetwork.socialnetwork.gui;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import socialnetwork.socialnetwork.service.UserService;

import java.io.IOException;

public class SignUpController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private Stage signUpStage;
    private LoginController loginController;
    private UserService userService;

    public void setSignUpStage(Stage signUpStage) {
        this.signUpStage = signUpStage;
    }

    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @FXML
    private void handleSignUp() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Username and password cannot be empty");
            return;
        }

        try {
            boolean success = userService.signUp(username, password);
            if (success) {
                showAlert("Sign up successful! Please log in.");
                signUpStage.close();
//                loginController.showLoginWindow();
            } else {
                showAlert("Username already exists.");
            }
        } catch (IOException ex) {
            showAlert("An error occurred: " + ex.getMessage());
        }
    }

    @FXML
    private void handleExit() {
        signUpStage.close();
    }

    private void showAlert(String message) {
        System.out.println(message);
    }
}