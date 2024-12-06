package socialnetwork.socialnetwork.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import socialnetwork.socialnetwork.domain.User;
import socialnetwork.socialnetwork.service.UserService;

import java.io.IOException;
import java.util.List;

public class CreateConversationController {
    @FXML
    private TextField userSearchField;
    @FXML
    private ListView<String> userListView;

    private UserService userService;
    private ObservableList<String> userList = FXCollections.observableArrayList();
    private MainController mainController;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        userListView.setItems(userList);
    }

    @FXML
    private void handleCreateConversation() throws IOException {
        String chatRoomName = userSearchField.getText();
        List<String> selectedUsers = userListView.getSelectionModel().getSelectedItems();
        mainController.createChatRoom(chatRoomName, selectedUsers);
        Stage stage = (Stage) userListView.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSearchUsers() throws IOException {
        String searchText = userSearchField.getText();
        List<User> users = (List<User>) userService.findUserByUsername(searchText);
        userList.clear();
        for (User user : users) {
            userList.add(user.getUsername());
        }
    }
}