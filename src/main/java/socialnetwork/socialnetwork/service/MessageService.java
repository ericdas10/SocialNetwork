package socialnetwork.socialnetwork.service;

import socialnetwork.socialnetwork.domain.Message;
import socialnetwork.socialnetwork.domain.User;
import socialnetwork.socialnetwork.observer.Observer;
import socialnetwork.socialnetwork.observer.Subject;
import socialnetwork.socialnetwork.repository.AbstractRepo;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageService implements Subject {
    private final AbstractRepo<Integer, Message> messageRepo;
    private final List<Observer> observers = new ArrayList<>();

    public MessageService(AbstractRepo<Integer, Message> messageRepo) {
        this.messageRepo = messageRepo;
    }

    public void sendMessage(User from, List<User> to, String message) throws IOException {
        Message newMessage = new Message(from, to, message, LocalDateTime.now());
        messageRepo.save(newMessage);
        notifyObservers();
    }

    public Iterable<Message> getAllMessages() throws IOException {
        return messageRepo.findAll();
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
}