// UserService.java
package socialnetwork.socialnetwork.service;

import socialnetwork.socialnetwork.domain.User;
import socialnetwork.socialnetwork.domain.validators.ValidationException;
import socialnetwork.socialnetwork.domain.validators.Validator;
import socialnetwork.socialnetwork.repository.AbstractRepo;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UserService {
    private final AbstractRepo<Integer, User> userRepo;
    private final Validator<User> userValidator;
    private FriendshipService friendshipService;

    public UserService(AbstractRepo<Integer, User> userRepo, Validator<User> userValidator) {
        this.userRepo = userRepo;
        this.userValidator = userValidator;
    }

    public boolean signUp(String username, String password) throws IOException {
        boolean userExists = StreamSupport.stream(userRepo.findAll().spliterator(), false)
                .anyMatch(user -> user.getUsername().equals(username));

        if (userExists) {
            return false;
        }
        try {
            User user = new User(username, password);
            userValidator.validate(user);
            userRepo.save(user);
            return true;
        } catch (ValidationException e) {
            System.out.println("Username or password invalid!");
            return false;
        }
    }

    public User signIn(String username, String password) throws IOException {
        return StreamSupport.stream(userRepo.findAll().spliterator(), false)
                .filter(user -> user.getUsername().equals(username) && user.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    public void deleteUser(User user) throws IOException {
        userRepo.remove(user.getId());
        if (friendshipService != null) {
            friendshipService.handleUserDeletion(user);
        }
    }

    public List<User> getAllUsers() throws IOException {
        return (List<User>) userRepo.findAll();
    }

    public User findUserByUsername(String username) throws IOException {
        return StreamSupport.stream(userRepo.findAll().spliterator(), false)
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public List<User> findUsersByUsernames(List<String> usernames) throws IOException {
        return StreamSupport.stream(userRepo.findAll().spliterator(), false)
                .filter(user -> usernames.contains(user.getUsername()))
                .collect(Collectors.toList());
    }

    public User findUserById(Integer id) throws IOException {
        return StreamSupport.stream(userRepo.findAll().spliterator(), false)
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}