package socialnetwork.socialnetwork.service;

import socialnetwork.socialnetwork.domain.ChatRoom;
import socialnetwork.socialnetwork.domain.Message;
import socialnetwork.socialnetwork.domain.MessageType;
import socialnetwork.socialnetwork.domain.User;
import socialnetwork.socialnetwork.observer.Observer;
import socialnetwork.socialnetwork.observer.Subject;
import socialnetwork.socialnetwork.repository.ChatRoomRepoDB;
import socialnetwork.socialnetwork.repository.MessageRepository;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatRoomService implements Subject {
    private final ChatRoomRepoDB chatRoomRepo;
    private final MessageRepository messageRepo;
    private final List<Observer> observers = new ArrayList<>();

    public ChatRoomService(ChatRoomRepoDB chatRoomRepo, MessageRepository messageRepo) {
        this.chatRoomRepo = chatRoomRepo;
        this.messageRepo = messageRepo;
    }

    public Optional<ChatRoom> createChatRoom(String name, List<User> participants, User creator) throws IOException {
        if (!participants.contains(creator)) {
            participants.add(creator);
        }
        ChatRoom chatRoom = new ChatRoom(name, participants, List.of());
        return chatRoomRepo.save(chatRoom);
    }

    public List<ChatRoom> getAllChatRooms() throws IOException {
        return chatRoomRepo.findAll();
    }

    public Optional<ChatRoom> getChatRoomById(int id) throws IOException {
        return chatRoomRepo.findAll().stream()
                .filter(chatRoom -> chatRoom.getId() == id)
                .findFirst();
    }

    public void addMessageToChatRoom(int chatRoomId, Message message) throws IOException {
        Optional<ChatRoom> chatRoomOpt = getChatRoomById(chatRoomId);
        if (chatRoomOpt.isPresent()) {
            ChatRoom chatRoom = chatRoomOpt.get();
            chatRoom.getMessages().add(message);
            chatRoomRepo.save(chatRoom);
        } else {
            throw new IOException("ChatRoom not found");
        }
    }

    public List<Message> getMessagesByChatRoomId(int chatRoomId) throws IOException {
        Optional<ChatRoom> chatRoomOpt = getChatRoomById(chatRoomId);
        if (chatRoomOpt.isPresent()) {
            return chatRoomOpt.get().getMessages();
        } else {
            throw new IOException("ChatRoom not found");
        }
    }

    public void sendMessage(int chatRoomId, User fromUser, String messageText) throws IOException {
        Optional<ChatRoom> chatRoomOpt = getChatRoomById(chatRoomId);
        if (chatRoomOpt.isPresent()) {
            ChatRoom chatRoom = chatRoomOpt.get();

            // Create message with all participants of the chat room
            Message message = new Message(fromUser, chatRoom.getParticipants(), messageText, LocalDateTime.now(), MessageType.Message);
            message.setChatRoomId(chatRoomId); // Add this method to the Message class

            try {
                // Save message to database
                messageRepo.save(message);

                // Add message to chat room
                chatRoom.getMessages().add(message);
                chatRoomRepo.save(chatRoom);

                // Notify observers
                notifyObservers();
            } catch (SQLException e) {
                throw new IOException("Failed to save message", e);
            }
        } else {
            throw new IOException("ChatRoom not found");
        }
    }

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

    public Iterable<Message> getAllMessages() {
        try {
            return messageRepo.findAll();
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }
}