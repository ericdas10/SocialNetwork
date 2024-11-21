package socialnetwork.socialnetwork.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import socialnetwork.socialnetwork.service.FriendshipService;
import socialnetwork.socialnetwork.service.UserService;

import java.io.IOException;
import java.util.List;

public class MainController {
    @FXML
    private TextField searchField;
    @FXML
    private TableView<SimpleStringProperty> friendsTable;
    @FXML
    private TableView<SimpleStringProperty> requestsTable;
    @FXML
    private TableColumn<SimpleStringProperty, String> friendsColumn;
    @FXML
    private TableColumn<SimpleStringProperty, String> requestsColumn;

    private FriendshipService friendshipService;
    private UserService userService;
    private String currentUser;

    private ObservableList<SimpleStringProperty> friendsList = FXCollections.observableArrayList();
    private ObservableList<SimpleStringProperty> requestsList = FXCollections.observableArrayList();

    public void setUserService(UserService userService, FriendshipService friendshipService, String currentUser) throws IOException {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.currentUser = currentUser;
        friendsTable.setItems(friendsList);
        requestsTable.setItems(requestsList);
        loadFriends();
        loadRequests();
    }

    @FXML
    private void handleAddFriend() throws IOException {
        String username = searchField.getText();
        if (!username.isEmpty()) {
            friendshipService.addFriend(friendshipService.findUserByUsername(currentUser).getId(), friendshipService.findUserByUsername(username).getId());
            loadFriends();
            loadRequests();
        }
    }

    @FXML
    private void handleRemoveFriend() throws IOException {
        String username = searchField.getText();
        if (!username.isEmpty()) {
            friendshipService.removeFriend(friendshipService.findUserByUsername(currentUser).getId(), friendshipService.findUserByUsername(username).getId());
            loadFriends();
            loadRequests();
        }
    }

    @FXML
    private void handleAcceptFriendRequest() throws IOException {
        String username = searchField.getText();
        if (!username.isEmpty()) {
            friendshipService.acceptFriendRequest(friendshipService.findUserByUsername(username).getId(), friendshipService.findUserByUsername(currentUser).getId());
            loadFriends();
            loadRequests();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/socialnetwork/socialnetwork/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteAccount() throws IOException {
        userService.deleteUser(friendshipService.findUserByUsername(currentUser));
        Stage stage = (Stage) searchField.getScene().getWindow();
        stage.close();
    }

    private void loadFriends() throws IOException {
        List<String> friends = friendshipService.getFriends(currentUser);
        friendsList.setAll(friends.stream().map(SimpleStringProperty::new).toList());
    }

    private void loadRequests() throws IOException {
        List<String> requests = friendshipService.getPendingRequests(currentUser);
        requestsList.setAll(requests.stream().map(SimpleStringProperty::new).toList());
    }
}