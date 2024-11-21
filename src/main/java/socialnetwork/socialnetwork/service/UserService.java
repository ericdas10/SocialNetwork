package socialnetwork.socialnetwork.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.StreamSupport;

import socialnetwork.socialnetwork.domain.*;
import socialnetwork.socialnetwork.domain.validators.*;
import socialnetwork.socialnetwork.repository.*;

public class UserService {
    private final AbstractRepo<Integer, User> userRepo;
    private final Validator<User> userValidator;

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
        try{
            User user = new User( username, password);
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
    }

    private int findLastId() throws IOException {
        List<User> users = (List<User>) userRepo.findAll();

        if (users.isEmpty()){
            return 0;
        }else{
//            User user = users.getFirst();
            return users.size();
        }
    }
}
