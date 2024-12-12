package socialnetwork.socialnetwork.gui;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import socialnetwork.socialnetwork.domain.*;
import socialnetwork.socialnetwork.observer.ChatObserver;
import socialnetwork.socialnetwork.observer.Observer;
import socialnetwork.socialnetwork.repository.NotificationRepoDB;
import socialnetwork.socialnetwork.service.ChatRoomService;
import socialnetwork.socialnetwork.service.FriendshipService;
import socialnetwork.socialnetwork.service.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainController implements Observer, ChatObserver {
    @FXML
    private Button removeFriendButton;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private Button addFriendButton;
    @FXML
    private TableView<User> friendsTable;
    @FXML
    private TableView<User> requestsTable;
    @FXML
    private Button acceptRequestButton;
    @FXML
    private Button rejectRequestButton;
    @FXML
    private TableView<ChatRoom> roomsTable; // Corrected type
    @FXML
    private ListView<String> chatListView;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;
    @FXML
    private Button createRoomButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button deleteAccountButton;
    @FXML
    private TextField chatInput;
    @FXML
    private ListView<String> chatRoomsListView;
    @FXML
    private TextField roomNameField;
    @FXML
    private TextField participantsField;
    @FXML
    private Pagination friendsPagination;
    @FXML
    private Pagination requestsPagination;
    private ChatRoom currentChatRoom;
    @FXML
    private Text usernameText;
    private FriendshipService friendshipService;
    private UserService userService;
    private Integer currentUser;
    private ChatRoomService chatRoomService;
    @FXML
    private TableView<Notification> notificationsTable;
    @FXML
    private TableColumn<Notification, String> notificationColumn;
    private NotificationRepoDB notificationRepo;

    public void setUserService(UserService userService, FriendshipService friendshipService, ChatRoomService chatRoomService , Integer currentUser) throws IOException, SQLException {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.chatRoomService = chatRoomService;
        this.currentUser = currentUser;
        User user = userService.findUserById(currentUser);
        if (user != null) {
            usernameText.setText(user.getUsername());
        }
        this.notificationRepo = new NotificationRepoDB();
        friendshipService.registerObserver(this);
        loadFriends();
        loadRequests();
        loadChatRooms();
        loadAllUsers();
        loadNotifications();
        initializeTables();
    }

    private void initializeTables() {
        usersTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("username"));
        friendsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("username"));
        requestsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("username"));
        roomsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("name"));
        notificationsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("message"));        usernameColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getUsername()));
        friendsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        requestsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        initializeChat();
    }

    @FXML
    private void initializeChat() {
        roomsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    loadChatMessages(newValue.getId());
                    loadChatParticipants(newValue.getId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void initialize() {
        roomsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (oldSelection != null) {
                chatRoomService.removeChatObserver(oldSelection.getId(), this);
            }

            if (newSelection != null) {
                currentChatRoom = newSelection;
                chatRoomService.registerChatObserver(newSelection.getId(), this);
                try {
                    loadChatMessages(newSelection.getId());
                    handleRoomSelection(newSelection);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        notificationColumn.setCellValueFactory(cellData -> {
            Notification notification = cellData.getValue();
            return new SimpleStringProperty(notification.getMessage());
        });

        // Optional: adaugă un row factory pentru stilizare
        notificationsTable.setRowFactory(tv -> {
            TableRow<Notification> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    Notification notification = row.getItem();
                    try {
                        notificationRepo.markAsRead(notification.getId());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
            return row;
        });

        Platform.runLater(() -> {
            try {
                loadNotifications();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    @FXML
    private void handleAddFriend() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                friendshipService.addFriend(currentUser, selectedUser.getId());
                loadFriends();
                loadRequests();
            } catch (IOException e) {
                showAlert(Alert.AlertType.WARNING, e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Please select a user to add as friend.");
        }
    }

    @FXML
    private void handleRemoveFriend() throws IOException {
        User selectedUser = friendsTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            friendshipService.removeFriend(currentUser, selectedUser.getId());
            loadFriends();
            loadRequests();
        } else {
            showAlert(Alert.AlertType.WARNING, "Please select a friend to remove.");
        }
    }

    @FXML
    private void handleAcceptFriendRequest() throws IOException {
        User selectedUser = requestsTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            friendshipService.acceptFriendRequest(selectedUser.getId(), currentUser);
            loadFriends();
            loadRequests();
        } else {
            showAlert(Alert.AlertType.WARNING, "Please select a request to accept.");
        }
    }

    @FXML
    private void handleRejectFriendRequest() throws IOException {
        User selectedUser = requestsTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            friendshipService.rejectFriendRequest(currentUser, selectedUser.getId());
            loadRequests();
        } else {
            showAlert(Alert.AlertType.WARNING, "Please select a request to reject.");
        }
    }

    @FXML
    private void handleLogout() throws IOException {
        cleanup();
        LoginController.logoutUser(friendshipService.findUserById(currentUser).getUsername());
        Stage stage = (Stage) searchField.getScene().getWindow();
        stage.close();
    }

    public void initializeWindowCloseHandler(Stage stage) {
        stage.setOnCloseRequest(event -> cleanup());
    }

    @FXML
    private void handleDeleteAccount() throws IOException {
        friendshipService.garbageFrindships(friendshipService.findUserById(currentUser));
        userService.deleteUser(friendshipService.findUserById(currentUser));
        LoginController.logoutUser(friendshipService.findUserById(currentUser).getUsername());
        Stage stage = (Stage) searchField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSendMessage() {
        String messageText = messageField.getText().trim();

        if (currentChatRoom == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a chat room.");
            return;
        }

        if (messageText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Message cannot be empty.");
            return;
        }

        try {
            User currentUser = userService.findUserById(this.currentUser);
            chatRoomService.sendMessage(currentChatRoom.getId(), currentUser, messageText);
            messageField.clear();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to send message: " + e.getMessage());
        }
    }

    public void cleanup() {
        if (currentChatRoom != null) {
            chatRoomService.removeChatObserver(currentChatRoom.getId(), this);
        }
        chatRoomService.removeObserver(this);
    }

    private void handleRoomSelection(ChatRoom selectedRoom) {
        try {
            User currentUser = userService.findUserById(this.currentUser);
            boolean isParticipant = chatRoomService.isUserInChatRoom(selectedRoom.getId(), currentUser);

            // Disable or enable message input based on participation
            messageField.setDisable(!isParticipant);
            sendButton.setDisable(!isParticipant);

            if (!isParticipant) {
                messageField.setPromptText("You are not a participant in this chat room");
            } else {
                messageField.setPromptText("Type your message here");
            }

            loadChatMessages(selectedRoom.getId());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error checking room participation: " + e.getMessage());
        }
    }

    @FXML
    public void handleCreateChatRoom() {
        String roomName = roomNameField.getText().trim();
        String participantsText = participantsField.getText().trim();

        // Validate input
        if (roomName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please enter a room name.");
            return;
        }

        if (participantsText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please enter participants.");
            return;
        }

        try {
            List<String> participantUsernames = List.of(participantsText.split(","));

            // Get current user and add to participants if not already included
            User creator = userService.findUserById(currentUser);
            List<User> users = participantUsernames.stream()
                    .map(username -> {
                        try {
                            return userService.findUserByUsername(username.trim());
                        } catch (IOException e) {
                            showAlert(Alert.AlertType.ERROR, "Error finding user: " + username);
                            return null;
                        }
                    })
                    .filter(user -> user != null)
                    .collect(Collectors.toList());

            // Ensure creator is in the list
            if (!users.contains(creator)) {
                users.add(creator);
            }

            Optional<ChatRoom> newRoom = chatRoomService.createChatRoom(roomName, users, creator);

            if (newRoom.isPresent()) {
                showAlert(Alert.AlertType.INFORMATION, "Chat room created successfully!");
                loadChatRooms();
                // Clear input fields
                roomNameField.clear();
                participantsField.clear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed to create chat room.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error creating chat room: " + e.getMessage());
        }
    }

    private void loadChatRooms() throws IOException {
        List<ChatRoom> chatRooms = chatRoomService.getAllChatRooms();
        roomsTable.getItems().setAll(chatRooms); // Corrected type
    }

    private void loadChatMessages(int chatRoomId) throws IOException {
        Optional<ChatRoom> chatRoomOpt = chatRoomService.getChatRoomWithMessages(chatRoomId);
        if (chatRoomOpt.isPresent()) {
            ChatRoom chatRoom = chatRoomOpt.get();
            chatListView.getItems().clear();
            for (Message message : chatRoom.getMessages()) {
                chatListView.getItems().add(message.getFrom().getUsername() + ": " + message.getMessage());
            }
            // Scroll to bottom to show latest messages
            chatListView.scrollTo(chatListView.getItems().size() - 1);
        }
    }

    private void loadChatParticipants(int chatRoomId) throws IOException {
        Optional<ChatRoom> chatRoomOpt = chatRoomService.getChatRoomById(chatRoomId);
        if (chatRoomOpt.isPresent()) {
            ChatRoom chatRoom = chatRoomOpt.get();
            String participants = chatRoom.getParticipants().stream()
                    .map(User::getUsername)
                    .collect(Collectors.joining(", "));
            chatListView.getItems().add(0, "Participants: " + participants);
        }
    }

    private void loadFriends() throws IOException {
        String username = friendshipService.findUserById(currentUser).getUsername();
        friendsPagination.setPageFactory(pageIndex -> {
            try {
                Page<User> friendsPage = friendshipService.getFriendsPaginated(username, pageIndex);
                friendsTable.getItems().setAll(friendsPage.getItems());
                friendsPagination.setPageCount(friendsPage.getTotalPages());
                return friendsTable;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private void loadRequests() throws IOException {
        String username = friendshipService.findUserById(currentUser).getUsername();
        requestsPagination.setPageFactory(pageIndex -> {
            try {
                Page<User> requestsPage = friendshipService.getPendingRequestsPaginated(username, pageIndex);
                requestsTable.getItems().setAll(requestsPage.getItems());
                requestsPagination.setPageCount(requestsPage.getTotalPages());
                return requestsTable;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private void loadAllUsers() {
        try {
            usersTable.getItems().setAll(userService.getAllUsers());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load users: " + e.getMessage());
        }
    }

    private void loadNotifications() {
        try {
            User currentUserObj = userService.findUserById(currentUser);
            if (currentUserObj != null) {
                List<Notification> notifications = notificationRepo.getNotificationsForUser(currentUserObj);
                notificationsTable.getItems().clear();
                notificationsTable.getItems().addAll(notifications);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load notifications: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleSearch() throws IOException {
        String searchText = searchField.getText();
        List<User> users = userService.findUsersByUsernames(List.of(searchText));
        usersTable.getItems().setAll(users);
    }

    @Override
    public void update() {
        try {
            loadFriends();
            loadRequests();
            loadChatRooms();
            loadNotifications();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewMessage(int chatRoomId, Message message) {
        if (currentChatRoom != null && currentChatRoom.getId() == chatRoomId) {
            Platform.runLater(() -> {
                chatListView.getItems().add(
                        message.getFrom().getUsername() + ": " + message.getMessage()
                );
                chatListView.scrollTo(chatListView.getItems().size() - 1);
            });
        }
    }
}