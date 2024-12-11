package socialnetwork.socialnetwork.gui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import socialnetwork.socialnetwork.domain.Message;
import socialnetwork.socialnetwork.domain.User;
import socialnetwork.socialnetwork.service.MessageService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ChatRoomController {
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField chatInput;
    @FXML
    private Button sendButton;

    private String chatRoomName;
    private List<User> participants;
    private User currentUser;
    private MessageService messageService;

    public void setChatRoomName(String chatRoomName) {
        this.chatRoomName = chatRoomName;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @FXML
    private void handleSendMessage() throws IOException {
        String messageText = chatInput.getText();
        if (!messageText.isEmpty()) {
            Message message = new Message(currentUser, participants, messageText, LocalDateTime.now());
            messageService.sendMessage(currentUser, participants, messageText);
            chatArea.appendText(currentUser.getUsername() + ": " + messageText + "\n");
            chatInput.clear();
        }
    }
}