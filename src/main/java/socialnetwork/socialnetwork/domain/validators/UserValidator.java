package socialnetwork.socialnetwork.domain.validators;
import socialnetwork.socialnetwork.domain.User;

public class UserValidator implements Validator<User>{

    @Override
    public void validate(User entity) throws ValidationException{
            validateUsername(entity.getUsername());
            validatePassword(entity.getPassword());
    }

    private void validateUsername(String username) throws ValidationException{
        if (username == null)
            throw new ValidationException("Username must not be null!");
        else if(username.length() >= 100)
            throw new ValidationException("Username too long");
        else if(username.isEmpty())
            throw new ValidationException("Username must not be empty");
        else if(! Character.isAlphabetic(username.charAt(0)))
            throw new ValidationException("First letter of the username must be a letter");
    }

    private void validatePassword(String password) throws ValidationException{
        if (password == null)
            throw new ValidationException("Password must not be null!");
        else if(password.length() >= 100)
            throw new ValidationException("Password too long");
        else if(password.isEmpty())
            throw new ValidationException("Username must not be empty");
    }
}
