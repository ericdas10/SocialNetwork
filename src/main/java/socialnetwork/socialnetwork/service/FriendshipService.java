package socialnetwork.socialnetwork.service;

import socialnetwork.socialnetwork.domain.Friendship;
import socialnetwork.socialnetwork.domain.FriendshipStatus;
import socialnetwork.socialnetwork.domain.User;
import socialnetwork.socialnetwork.observer.Observer;
import socialnetwork.socialnetwork.observer.Subject;
import socialnetwork.socialnetwork.repository.AbstractRepo;
import socialnetwork.socialnetwork.repository.FriendshipRepoDB;

import java.time.LocalDate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FriendshipService implements Subject {
    private final FriendshipRepoDB friendshipRepo;
    private final AbstractRepo<Integer, User> userRepo;
    private final List<Observer> observers = new ArrayList<>();

    public FriendshipService(FriendshipRepoDB friendshipRepo, AbstractRepo<Integer, User> userRepo) {
        this.friendshipRepo = friendshipRepo;
        this.userRepo = userRepo;
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

    public void addFriend(Integer id1, Integer id2) throws IOException {
        List<User> users = (List<User>) userRepo.findAll();

        Optional<User> user1Opt = users.stream()
                .filter(user -> user.getId().equals(id1))
                .findFirst();
        Optional<User> user2Opt = users.stream()
                .filter(user -> user.getId().equals(id2))
                .findFirst();

        if (user1Opt.isPresent() && user2Opt.isPresent()) {
            Friendship friendship = new Friendship(1 + findLastId(), user1Opt.get(), user2Opt.get(), LocalDate.now());
            friendshipRepo.save(friendship);
            notifyObservers();
        } else {
            throw new IOException("One or both users not found.");
        }
    }

    public void acceptFriendRequest(Integer id1, Integer id2) throws IOException {
        StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .filter(friendship -> isPendingFriendshipBetweenUsers(friendship, id1, id2))
                .findFirst()
                .ifPresentOrElse(friendship -> {
                    try {
                        friendship.setStatus(FriendshipStatus.ACCEPTED);
                        friendshipRepo.remove(friendship.getId());
                        friendshipRepo.save(friendship);
                        notifyObservers();
                    } catch (IOException e) {
                        System.err.println("Failed to update friendship status: " + e.getMessage());
                    }
                }, () -> {
                    try {
                        throw new IOException("No pending friend request found between these users.");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public void removeFriend(Integer id1, Integer id2) throws IOException {
        StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .filter(friendship -> isFriendshipBetweenUsers(friendship, id1, id2))
                .findFirst()
                .ifPresentOrElse(friendship -> {
                    try {
                        friendshipRepo.remove(friendship.getId());
                        notifyObservers();
                    } catch (IOException e) {
                        System.err.println("Failed to remove friendship: " + e.getMessage());
                    }
                }, () -> {
                    try {
                        throw new IOException("No friendship found between these users.");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public void handleUserDeletion(User user) throws IOException {
        List<Friendship> friendships = StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .filter(friendship -> friendship.getUser1().getId().equals(user.getId()) || friendship.getUser2().getId().equals(user.getId()))
                .toList();

        for (Friendship friendship : friendships) {
            friendshipRepo.remove(friendship.getId());
        }
        notifyObservers();
    }

    private boolean isPendingFriendshipBetweenUsers(Friendship friendship, Integer id1, Integer id2) {
        return (friendship.getUser1().getId().equals(id1) && friendship.getUser2().getId().equals(id2)
                || friendship.getUser1().getId().equals(id2) && friendship.getUser2().getId().equals(id1))
                && friendship.getStatus() == FriendshipStatus.PENDING;
    }

    private boolean isFriendshipBetweenUsers(Friendship friendship, Integer id1, Integer id2) {
        return (friendship.getUser1().getId().equals(id1) && friendship.getUser2().getId().equals(id2)
                || friendship.getUser1().getId().equals(id2) && friendship.getUser2().getId().equals(id1));
    }

    public User findUserById(Integer id) throws IOException {
        return StreamSupport.stream(userRepo.findAll().spliterator(), false)
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public User findUserByUsername(String username) throws IOException {
        return StreamSupport.stream(userRepo.findAll().spliterator(), false)
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public void garbageFrindships(User user) throws IOException {
        List<Friendship> allFriendships = StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .toList();

        for (Friendship friendship : allFriendships) {
            if (friendship.getUser1().getId().equals(user.getId()) || friendship.getUser2().getId().equals(user.getId())) {
                friendshipRepo.remove(friendship.getId());
            }
        }
    }

    public List<String> getFriends(String currentUser) throws IOException {
        User user = findUserByUsername(currentUser);
        return StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.ACCEPTED &&
                        (friendship.getUser1().getId().equals(user.getId()) || friendship.getUser2().getId().equals(user.getId())))
                .map(friendship -> friendship.getUser1().getId().equals(user.getId()) ? friendship.getUser2().getUsername() + " is " + friendship.getStatus() + " from date " + friendship.getDate() : friendship.getUser1().getUsername() + " is " + friendship.getStatus() + " from date " + friendship.getDate())
                .collect(Collectors.toList());
    }

    public List<String> getPendingRequests(String currentUser) throws IOException {
        User user = findUserByUsername(currentUser);
        return StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.PENDING && friendship.getUser2().getId().equals(user.getId()))
                .map(friendship -> friendship.getUser1().getUsername() + " is " + friendship.getStatus() + " from date " + friendship.getDate())
                .collect(Collectors.toList());
    }

    private int findLastId() throws IOException {
        List<Friendship> friendships = (List<Friendship>) friendshipRepo.findAll();

        if (friendships.isEmpty()) {
            return 0;
        } else {
            return friendships.size();
        }
    }
}