package socialnetwork.socialnetwork.service;

import socialnetwork.socialnetwork.domain.ChatRoom;
import socialnetwork.socialnetwork.domain.Message;
import socialnetwork.socialnetwork.domain.MessageType;
import socialnetwork.socialnetwork.domain.User;
import socialnetwork.socialnetwork.observer.ChatObserver;
import socialnetwork.socialnetwork.observer.Observer;
import socialnetwork.socialnetwork.observer.Subject;
import socialnetwork.socialnetwork.repository.ChatRoomRepoDB;
import socialnetwork.socialnetwork.repository.MessageRepository;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class ChatRoomService implements Subject {
    private final ChatRoomRepoDB chatRoomRepo;
    private final MessageRepository messageRepo;
    private final List<Observer> observers = new ArrayList<>();
    private final Map<Integer, List<ChatObserver>> chatObservers = new HashMap<>();

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

    public Optional<ChatRoom> getChatRoomWithMessages(int chatRoomId) throws IOException {
        Optional<ChatRoom> chatRoomOpt = getChatRoomById(chatRoomId);
        if (chatRoomOpt.isPresent()) {
            ChatRoom chatRoom = chatRoomOpt.get();
            try {
                List<Message> messages = messageRepo.findMessages(chatRoomId);
                return Optional.of(new ChatRoom(
                        chatRoom.getId(),
                        chatRoom.getName(),
                        chatRoom.getParticipants(),
                        messages
                ));
            } catch (SQLException e) {
                throw new IOException("Failed to load messages", e);
            }
        }
        return Optional.empty();
    }

    public boolean isUserInChatRoom(int chatRoomId, User user) throws IOException {
        Optional<ChatRoom> chatRoomOpt = getChatRoomById(chatRoomId);
        if (chatRoomOpt.isPresent()) {
            ChatRoom chatRoom = chatRoomOpt.get();
            return chatRoom.getParticipants().stream()
                    .anyMatch(participant -> participant.getId().equals(user.getId()));
        }
        return false;
    }

    public void sendMessage(int chatRoomId, User fromUser, String messageText) throws IOException {
        if (!isUserInChatRoom(chatRoomId, fromUser)) {
            throw new IOException("User is not a participant in this chat room");
        }

        Optional<ChatRoom> chatRoomOpt = getChatRoomById(chatRoomId);
        if (chatRoomOpt.isPresent()) {
            ChatRoom chatRoom = chatRoomOpt.get();

            Message message = new Message(fromUser, chatRoom.getParticipants(), messageText, LocalDateTime.now(), MessageType.Message);
            message.setChatRoomId(chatRoomId);

            try {
                messageRepo.save(message);
                chatRoom.getMessages().add(message);

                // Notifică ambele tipuri de observeri
                notifyObservers(); // pentru actualizări generale
                notifyChatObservers(chatRoomId, message); // pentru chat
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

    public void registerChatObserver(int chatRoomId, ChatObserver observer) {
        chatObservers.computeIfAbsent(chatRoomId, k -> new ArrayList<>()).add(observer);
    }

    public void removeChatObserver(int chatRoomId, ChatObserver observer) {
        if (chatObservers.containsKey(chatRoomId)) {
            chatObservers.get(chatRoomId).remove(observer);
        }
    }

    private void notifyChatObservers(int chatRoomId, Message message) {
        if (chatObservers.containsKey(chatRoomId)) {
            for (ChatObserver observer : chatObservers.get(chatRoomId)) {
                observer.onNewMessage(chatRoomId, message);
            }
        }
    }

}