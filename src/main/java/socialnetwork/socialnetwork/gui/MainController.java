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
import socialnetwork.socialnetwork.domain.Message;
import socialnetwork.socialnetwork.domain.MessageType;
import socialnetwork.socialnetwork.domain.User;
import socialnetwork.socialnetwork.observer.Observer;
import socialnetwork.socialnetwork.service.FriendshipService;
import socialnetwork.socialnetwork.service.MessageService;
import socialnetwork.socialnetwork.service.UserService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class MainController implements Observer {
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
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField chatInput;
    @FXML
    private Button sendButton;

    @FXML
    private ListView<String> chatRoomsListView;

    private final ObservableList<String> chatRoomsList = FXCollections.observableArrayList();

    private FriendshipService friendshipService;
    private UserService userService;
    private MessageService messageService;
    private String currentUser;

    private final ObservableList<SimpleStringProperty> friendsList = FXCollections.observableArrayList();
    private final ObservableList<SimpleStringProperty> requestsList = FXCollections.observableArrayList();
    private ObservableList<Message> chatMessages = FXCollections.observableArrayList();

    public void setUserService(UserService userService, FriendshipService friendshipService, MessageService messageService, String currentUser) throws IOException {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.currentUser = currentUser;
        friendsTable.setItems(friendsList);
        requestsTable.setItems(requestsList);
        loadFriends();
        loadRequests();
        loadChatMessages();
        friendshipService.registerObserver(this);
        messageService.registerObserver(this);
        chatRoomsListView.setItems(chatRoomsList);
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
        LoginController.logoutUser(currentUser);
        Stage stage = (Stage) searchField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleDeleteAccount() throws IOException {
        friendshipService.garbageFrindships(friendshipService.findUserByUsername(currentUser));
        userService.deleteUser(friendshipService.findUserByUsername(currentUser));
        LoginController.logoutUser(currentUser);
        Stage stage = (Stage) searchField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSendMessage() throws IOException {
        String messageText = chatInput.getText();
        if (!messageText.isEmpty()) {
            User fromUser = friendshipService.findUserByUsername(currentUser);
            List<User> toUsers = friendsTable.getSelectionModel().getSelectedItems().stream()
                    .map(item -> {
                        try {
                            return friendshipService.findUserByUsername(item.getValue());
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .toList();
            messageService.sendMessage(fromUser, toUsers, messageText);
            chatInput.clear();
            loadChatMessages();
        }
    }

    private void loadFriends() throws IOException {
        List<String> friends = friendshipService.getFriends(currentUser);
        friendsList.setAll(friends.stream().map(SimpleStringProperty::new).toList());
    }

    private void loadRequests() throws IOException {
        List<String> requests = friendshipService.getPendingRequests(currentUser);
        requestsList.setAll(requests.stream().map(SimpleStringProperty::new).toList());
    }

    private void loadChatMessages() throws IOException {
        Iterable<Message> messages = messageService.getAllMessages();
        chatArea.clear();
        for (Message message : messages) {
            chatArea.appendText(message.getFrom().getUsername() + ": " + message.getMessage() + "\n");
        }
    }

    @FXML
    private void handleCreateConversation() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/socialnetwork/socialnetwork/create_conversation.fxml"));
        Parent root = loader.load();
        CreateConversationController controller = loader.getController();
        controller.setUserService(userService);
        controller.setMainController(this);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Create Conversation");
        stage.show();
    }


    @FXML
    private void handleSearch() throws IOException {
        String searchText = searchField.getText();
        List<User> users = userService.findUsersByUsernames(searchText);
        usersTable.getItems().setAll(users);
    }

    @FXML
    private void handleRejectFriendRequest() throws IOException {
        User selectedUser = requestsTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            friendshipService.rejectFriendRequest(currentUser, selectedUser.getUsername());
            updateRequestsTable();
        }
    }

    public void createConversation(List<String> usernames) throws IOException {
        List<User> users = userService.findUsersByUsernames(usernames);
        if (users.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No users found for the provided usernames.");
            alert.showAndWait();
            return;
        }

        User fromUser = userService.findUserByUsername(currentUser);
        String initialMessage = "Conversation started with " + String.join(", ", usernames);
        Message newMessage = new Message(fromUser, users, initialMessage, LocalDateTime.now(), MessageType.Message);

        messageService.sendMessage(fromUser, users, initialMessage);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Conversation Created");
        alert.setHeaderText(null);
        alert.setContentText("Conversation successfully created with " + String.join(", ", usernames));
        alert.showAndWait();
    }

    @FXML
    private void initialize() {
        chatRoomsListView.setItems(chatRoomsList);
    }

    @FXML
    private void handleCreateChatRoom() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/socialnetwork/socialnetwork/create_conversation.fxml"));
        Parent root = loader.load();
        CreateConversationController controller = loader.getController();
        controller.setUserService(userService);
        controller.setMainController(this);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Create Chat Room");
        stage.show();
    }

    public void createChatRoom(String chatRoomName, List<String> usernames) throws IOException {
        chatRoomsList.add(chatRoomName);
        List<User> participants = userService.findUsersByUsernames(usernames);
        openChatRoom(chatRoomName, participants);
    }

    @FXML
    private void handleOpenChatRoom() throws IOException {
        String selectedChatRoom = chatRoomsListView.getSelectionModel().getSelectedItem();
        if (selectedChatRoom != null) {
            List<User> participants = userService.findUsersByUsernames(List.of(selectedChatRoom.split(", ")));
            openChatRoom(selectedChatRoom, participants);
        }
    }

    private void openChatRoom(String chatRoomName, List<User> participants) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/socialnetwork/socialnetwork/chat_room.fxml"));
        Parent root = loader.load();
        ChatRoomController controller = loader.getController();
        controller.setChatRoomName(chatRoomName);
        controller.setParticipants(participants);
        controller.setCurrentUser(userService.findUserByUsername(currentUser));
        controller.setMessageService(messageService);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Chat Room - " + chatRoomName);
        stage.show();
    }


    @Override
    public void update() {
        try {
            loadFriends();
            loadRequests();
            loadChatMessages();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}