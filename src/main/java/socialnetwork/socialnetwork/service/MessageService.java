//package socialnetwork.socialnetwork.service;
//
//import socialnetwork.socialnetwork.domain.ChatRoom;
//import socialnetwork.socialnetwork.domain.Message;
//import socialnetwork.socialnetwork.domain.MessageType;
//import socialnetwork.socialnetwork.domain.User;
//import socialnetwork.socialnetwork.observer.Observer;
//import socialnetwork.socialnetwork.observer.Subject;
//import socialnetwork.socialnetwork.repository.MessageRepository;
//
//import java.io.IOException;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class MessageService implements Subject {
//    private final MessageRepository messageRepo;
//    private final List<Observer> observers = new ArrayList<>();
//
//    public MessageService(MessageRepository messageRepo) {
//        this.messageRepo = messageRepo;
//    }
//
//    public void sendMessage(User from, List<User> to, String messageText) throws IOException {
//        Message newMessage = new Message(from, to, messageText, MessageType.Message);
//        try {
//            messageRepo.save(newMessage);
//        } catch (SQLException e) {
//            throw new IOException("Failed to save message", e);
//        }
//        notifyObservers();
//    }
//
//    @Override
//    public void registerObserver(Observer observer) {
//        observers.add(observer);
//    }
//
//    @Override
//    public void removeObserver(Observer observer) {
//        observers.remove(observer);
//    }
//
//    @Override
//    public void notifyObservers() {
//        for (Observer observer : observers) {
//            observer.update();
//        }
//    }
//
//    public Iterable<Message> getAllMessages() {
//        try {
//            return messageRepo.findAll();
//        } catch (SQLException e) {
//            return new ArrayList<>();
//        }
//    }
//
//    public void createChatRoom(String chatRoomName, List<User> users) {
//
//    }
//
//    public List<ChatRoom> getAllChatRooms() throws IOException {
//        return chatRoomRepoDB.findAll();
//    }
//
//    public List<Message> getMessagesByChatRoomId(int chatRoomId) throws IOException {
//        Optional<ChatRoom> chatRoomOpt = chatRoomRepo.findById(chatRoomId);
//        if (chatRoomOpt.isPresent()) {
//            return chatRoomOpt.get().getMessages();
//        } else {
//            throw new IOException("ChatRoom not found");
//        }
//    }
//}