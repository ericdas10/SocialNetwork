package socialnetwork.socialnetwork.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import socialnetwork.socialnetwork.domain.User;
import socialnetwork.socialnetwork.domain.validators.UserValidator;
import socialnetwork.socialnetwork.repository.ChatRoomRepoDB;
import socialnetwork.socialnetwork.repository.FriendshipRepoDB;
import socialnetwork.socialnetwork.repository.MessageRepository;
import socialnetwork.socialnetwork.repository.UserRepoDB;
import socialnetwork.socialnetwork.service.ChatRoomService;
import socialnetwork.socialnetwork.service.FriendshipService;
import socialnetwork.socialnetwork.service.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

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
//    private MessageService messageService = new MessageService(new MessageRepository());
    private ChatRoomService chatRoomService = new ChatRoomService(new ChatRoomRepoDB(), new MessageRepository());


    private static Set<String> loggedInUsers = new HashSet<>();

    public LoginController() throws SQLException {
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (loggedInUsers.contains(username)) {
            showAlert(Alert.AlertType.ERROR, "User is already logged in.");
            return;
        }

        try {
            User user = userService.signIn(username, password);
            if (user != null) {
                loggedInUsers.add(username);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/socialnetwork/socialnetwork/main.fxml"));
                Parent root = loader.load();
                MainController controller = loader.getController();
                controller.setUserService(userService, fr, chatRoomService, user.getId());
                Stage mainStage = new Stage();
                mainStage.setScene(new Scene(root, 1000, 600));
                mainStage.setTitle("Main Page");
                mainStage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid username or password.");
            }
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "An error occurred: " + ex.getMessage());
        }
    }

    @FXML
    private void handleSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/socialnetwork/socialnetwork/signup.fxml"));
            Stage signUpStage = new Stage();
            signUpStage.setTitle("Sign Up");
            signUpStage.initModality(Modality.WINDOW_MODAL);
            signUpStage.initOwner((Stage) usernameField.getScene().getWindow());
            Scene scene = new Scene(loader.load());
            signUpStage.setScene(scene);

            SignUpController controller = loader.getController();
            controller.setSignUpStage(signUpStage);
            controller.setLoginController(this);
            controller.setUserService(userService);

            signUpStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
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

    public void showLoginWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/socialnetwork/socialnetwork/login.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Login / Sign Up");
            stage.setScene(new Scene(root, 600, 400));
            stage.show();
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "An error occurred: " + ex.getMessage());
        }
    }

    public static void logoutUser(String username) {
        loggedInUsers.remove(username);
    }
}