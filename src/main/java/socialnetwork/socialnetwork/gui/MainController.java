package socialnetwork.socialnetwork.gui;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import socialnetwork.socialnetwork.domain.ChatRoom;
import socialnetwork.socialnetwork.domain.Message;
import socialnetwork.socialnetwork.domain.User;
import socialnetwork.socialnetwork.observer.Observer;
import socialnetwork.socialnetwork.service.ChatRoomService;
import socialnetwork.socialnetwork.service.FriendshipService;
import socialnetwork.socialnetwork.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainController implements Observer {
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
    private TableView<String> notificationsTable;
    @FXML
    private TextField roomNameField;
    @FXML
    private TextField participantsField;
    @FXML
    private Pagination friendsPagination;
    @FXML
    private Pagination requestsPagination;

    private static final int ITEMS_PER_PAGE = 10;

    private FriendshipService friendshipService;
    private UserService userService;
    private Integer currentUser;
    private ChatRoomService chatRoomService;

    public void setUserService(UserService userService, FriendshipService friendshipService, ChatRoomService chatRoomService, Integer currentUser) throws IOException {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.chatRoomService = chatRoomService;
        this.currentUser = currentUser;
        friendshipService.registerObserver(this);
        loadFriends();
        loadRequests();
        loadChatRooms();
        loadAllUsers();
        initializeTables();
    }

    private void initializeTables() {
        usersTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("username"));
        friendsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("username"));
        requestsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("username"));
        roomsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("name"));
        notificationsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("notification"));
        usernameColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getUsername()));
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
    private void handleAddFriend() throws IOException {
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
        LoginController.logoutUser(friendshipService.findUserById(currentUser).getUsername());
        Stage stage = (Stage) searchField.getScene().getWindow();
        stage.close();
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
        ChatRoom selectedRoom = roomsTable.getSelectionModel().getSelectedItem();

        if (selectedRoom == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a chat room.");
            return;
        }

        if (messageText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Message cannot be empty.");
            return;
        }

        try {
            User currentUser = userService.findUserById(this.currentUser);
            chatRoomService.sendMessage(selectedRoom.getId(), currentUser, messageText);

            loadChatMessages(selectedRoom.getId());
            messageField.clear();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to send message: " + e.getMessage());
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
        List<Message> messages = chatRoomService.getMessagesByChatRoomId(chatRoomId);
        chatListView.getItems().clear();
        for (Message message : messages) {
            chatListView.getItems().add(message.getFrom().getUsername() + ": " + message.getMessage());
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
        List<User> friends = friendshipService.getFriends(friendshipService.findUserById(currentUser).getUsername());
        friendsPagination.setPageCount((int) Math.ceil((double) friends.size() / ITEMS_PER_PAGE));
        friendsPagination.setPageFactory(pageIndex -> {
            updateTableView(friendsTable, friends, pageIndex);
            return friendsTable;
        });
    }

    private void loadRequests() throws IOException {
        List<User> requests = friendshipService.getPendingRequests(friendshipService.findUserById(currentUser).getUsername());
        requestsPagination.setPageCount((int) Math.ceil((double) requests.size() / ITEMS_PER_PAGE));
        requestsPagination.setPageFactory(pageIndex -> {
            updateTableView(requestsTable, requests, pageIndex);
            return requestsTable;
        });
    }

    private void updateTableView(TableView<User> tableView, List<User> users, int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, users.size());
        List<User> subList = users.subList(fromIndex, toIndex);
        tableView.getItems().setAll(subList);
    }

    private Node createPage(List<User> users, int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, users.size());
        List<User> subList = users.subList(fromIndex, toIndex);

        TableView<User> tableView = new TableView<>();
        TableColumn<User, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        tableView.getColumns().add(usernameColumn);
        tableView.getItems().setAll(subList);

        return tableView;
    }

    private void loadAllUsers() {
        try {
            usersTable.getItems().setAll(userService.getAllUsers());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load users: " + e.getMessage());
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

//    @FXML
//    private void initialize() {
//        roomsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
//            if (newSelection != null) {
//                try {
//                    loadChatMessages(newSelection.getId()); // Corrected method call
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    @Override
    public void update() {
        try {
            loadFriends();
            loadRequests();
            loadChatRooms(); // Corrected method call
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}