package socialnetwork.socialnetwork.service;

import socialnetwork.socialnetwork.domain.*;
import socialnetwork.socialnetwork.observer.Observer;
import socialnetwork.socialnetwork.observer.Subject;
import socialnetwork.socialnetwork.repository.FriendshipRepoDB;
import socialnetwork.socialnetwork.repository.NotificationRepoDB;
import socialnetwork.socialnetwork.repository.UserRepoDB;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FriendshipService implements Subject {
    private final FriendshipRepoDB friendshipRepo;
    private final UserRepoDB userRepo;
    private final List<Observer> observers = new ArrayList<>();
    private final NotificationRepoDB notificationRepo;

    public FriendshipService(FriendshipRepoDB friendshipRepo, UserRepoDB userRepo, NotificationRepoDB notificationRepo) {
        this.friendshipRepo = friendshipRepo;
        this.userRepo = userRepo;
        this.notificationRepo = notificationRepo;
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
        try {
            Optional<User> user1Opt = userRepo.findOne(id1);
            Optional<User> user2Opt = userRepo.findOne(id2);

            if (!findFriendship(id1, id2)) {
                if (user1Opt.isPresent() && user2Opt.isPresent()) {
                    // Save friendship
                    Friendship friendship = new Friendship(1 + findLastId(), user1Opt.get(), user2Opt.get(), LocalDate.now());
                    friendshipRepo.save(friendship);

                    try {
                        // Create and save notification
                        Notification notification = new Notification(
                                null,
                                user2Opt.get(),
                                user1Opt.get(),
                                LocalDateTime.now()
                        );
                        notificationRepo.save(notification);
                    } catch (SQLException e) {
                        System.err.println("Error saving notification: " + e.getMessage());
                        // Don't throw exception here, just log it
                    }

                    notifyObservers();
                } else {
                    throw new IOException("One or both users not found.");
                }
            }
        } catch (Exception e) {
            throw new IOException("Error adding friend: " + e.getMessage());
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

    public void rejectFriendRequest(Integer id1, Integer id2) throws IOException {
        StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .filter(friendship -> isPendingFriendshipBetweenUsers(friendship, id1, id2))
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
                        throw new IOException("No pending friend request found between these users.");
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

    public void garbageFrindships(User user) throws IOException {
        List<Friendship> allFriendships = StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .toList();

        for (Friendship friendship : allFriendships) {
            if (friendship.getUser1().getId().equals(user.getId()) || friendship.getUser2().getId().equals(user.getId())) {
                friendshipRepo.remove(friendship.getId());
            }
        }
    }

    private int findLastId() throws IOException {
        List<Friendship> friendships = (List<Friendship>) friendshipRepo.findAll();

        if (friendships.isEmpty()) {
            return 0;
        } else {
            return friendships.size();
        }
    }

    public boolean findFriendship(Integer id1, Integer id2) throws IOException {
        return StreamSupport.stream(friendshipRepo.findAll().spliterator(), false)
                .anyMatch(friendship -> (friendship.getUser1().getId().equals(id1) && friendship.getUser2().getId().equals(id2))
                        || (friendship.getUser1().getId().equals(id2) && friendship.getUser2().getId().equals(id1)));
    }

    public Page<User> getFriendsPaginated(String currentUser, int page) throws IOException {
        Page<Friendship> friendshipsPage = ((FriendshipRepoDB)friendshipRepo).findAcceptedFriendshipsPaginated(currentUser, page);

        List<User> friends = friendshipsPage.getItems().stream()
                .map(friendship -> {
                    User user1 = friendship.getUser1();
                    User user2 = friendship.getUser2();
                    return user1.getUsername().equals(currentUser) ? user2 : user1;
                })
                .collect(Collectors.toList());

        return new Page<>(friends, friendshipsPage.getCurrentPage(),
                friendshipsPage.getTotalPages(),
                friendshipsPage.getTotalItems());
    }

    public Page<User> getPendingRequestsPaginated(String currentUser, int page) throws IOException {
        Page<Friendship> requestsPage = ((FriendshipRepoDB)friendshipRepo).findPendingFriendshipsPaginated(currentUser, page);

        List<User> requests = requestsPage.getItems().stream()
                .map(Friendship::getUser1)
                .collect(Collectors.toList());

        return new Page<>(requests, requestsPage.getCurrentPage(),
                requestsPage.getTotalPages(),
                requestsPage.getTotalItems());
    }
}