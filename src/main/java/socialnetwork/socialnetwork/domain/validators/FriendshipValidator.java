package socialnetwork.socialnetwork.domain.validators;

import socialnetwork.socialnetwork.domain.Friendship;

public class FriendshipValidator implements Validator<Friendship> {
    /**
     * @param entity - Friendship
     * @throws ValidationException if one of the users given doesnt exist
     */
    @Override
    public void validate(Friendship entity) throws ValidationException {
        String errorMessage = "";
        if(entity.getUser1() == null || entity.getUser2() == null) {
            errorMessage += "User doesnt exist";
        }

        if(entity.getUser1().equals(entity.getUser2())) {
            errorMessage += "Users can't be the same";
        }

        if(!errorMessage.isEmpty())
            throw new ValidationException(errorMessage);

    }
}
